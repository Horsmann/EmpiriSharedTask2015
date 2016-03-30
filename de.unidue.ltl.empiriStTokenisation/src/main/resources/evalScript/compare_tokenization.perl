#!/usr/bin/perl
# -*-cperl-*-
## Evaluate tokenized files against gold standard, with optional error report

use strict;
use warnings;
use utf8;
binmode(STDOUT, ":encoding(utf-8)");

use Unicode::Normalize;
use Algorithm::Diff qw(LCSidx);
use List::Util qw(min sum);
use Getopt::Long;

our $WithBlanks = 0;
our $WithXML = 0;
our $Report = undef;
our $Help = 0;

my $ok = GetOptions(
  "with-blanks|b" => \$WithBlanks,
  "with-xml|x" => \$WithXML,
  "errors|e=s" => \$Report,
  "help|h" => \$Help,
);
$ok = 0 unless @ARGV == 2;

die <<'USAGE' unless $ok && !$Help;
Usage:  perl compare_tokenization.perl [OPTIONS] file1.txt file2.txt
        perl compare_tokenization.perl [OPTIONS] system/   gold/
Options:
  -b, --with-blanks  include blank lines in comparison (must match)
  -x, --with-xml     inclued XML tags in comparison (must match)
  -e <file>          generate report of tokenization errors with context 
    -errors=<file>     (saved to text file <file>)
  -h, --help         this help page 
USAGE

our ($File1, $File2) = @ARGV;
my $isdir1 = -d $File1;
my $isdir2 = -d $File2;
die "Error: Either both arguments must be directories, or they must be plain files.\n" unless $isdir1 == $isdir2;

if ($Report) {
  open(REPORT, ">:encoding(utf8)", $Report) or die "Error: Can't write error report to file 'Report': $!";
}

if ($isdir1) {
  opendir(DIR, $File2) or die "Error: Can't open directory $File2/ for reading\n";
  my @filenames = grep {/\.txt$/} readdir DIR;
  closedir DIR;
  my @results = ();
  foreach my $file (@filenames) {
    die "Error: No text file found in $File1/ corresponding to $File2/$file\n" unless -f "$File1/$file";
    push @results, [compare_files("$File1/$file", "$File2/$file")];
  }
  my $total_support = sum map {$_->[3]} @results;
  my $total_fp = sum map {$_->[5]} @results;
  my $total_fn = sum map {$_->[6]} @results;
  my $avg_prec = sum map {$_->[0] * $_->[3] / $total_support} @results;
  my $avg_recall = sum map {$_->[1] * $_->[3] / $total_support} @results;
  my $avg_fscore = sum map {$_->[2] * $_->[3] / $total_support} @results;
  print "\n";
  print "Weighted average on $total_support tokens:\n";
  printf "P =%6.2f%%  R =%6.2f%%  F =%6.2f%%\n", $avg_prec, $avg_recall, $avg_fscore;
  printf "%6d false positives,%6d false negatives\n", $total_fp, $total_fn;
  
}
else {
  compare_files($File1, $File2);
}

if ($Report) {
  close REPORT;
}

## compare tokenized file against gold standard
## ($p, $r, $f, $support, $tp, $fp, $fn, $tn) = compare_files($target_file, $gold_file);
sub compare_files {
  my ($file1, $file2) = @_;
  our ($char1, $bdry1, $lnum1, $lines1) = read_file($file1);
  our ($char2, $bdry2, $lnum2, $lines2) = read_file($file2);

  my $nc1 = @$char1;
  my $nc2 = @$char2;

  my @tpfp = ([0, 0], [0, 0]);  # contingency table for token boundary markers
  my ($skip1, $skip2) = (0, 0); # number of characters skipped due to mismatches

  my ($i1, $i2) = (0, 0);

  while ($i1 < $nc1 && $i2 < $nc2) {
    my $c1 = $char1->[$i1];
    my $c2 = $char2->[$i2];
    if ($c1 ne $c2) {
      ## character mismatch --> re-synchronize
      printf "WARNING: character mismatch on line #%d <-> #%d\n", $lnum1->[$i1], $lnum2->[$i2];
      my $span = min(100, $nc1 - $i1, $nc2 - $i2);
      my @chunk1 = @{$char1}[$i1 .. $i1 + $span - 1]; # compare chunks of next 100 chars
      my @chunk2 = @{$char2}[$i2 .. $i2 + $span - 1];
      print "         Text 1: ", join("", @chunk1), "\n";
      print "         Text 2: ", join("", @chunk2), "\n";
      my ($lcs1, $lcs2) = LCSidx(\@chunk1, \@chunk2);
      if (@$lcs1 == 0) {
        ## synchronization failed, which is acceptable if close to end of text
        last if $span < 100;
        die "FATAL ERROR: re-synchronization failed\n";
      }
      my $d1 = $lcs1->[0];
      my $d2 = $lcs2->[0];
      printf "         skipping %d <-> %d characters\n", $d1, $d2;
      $i1 += $d1; # ignore mismatched characters
      $skip1 += $d1;
      $i2 += $d2;
      $skip2 += $d2;
    }
    else {
      my $b1 = $bdry1->[$i1];
      my $b2 = $bdry2->[$i2];
      $tpfp[$b1][$b2]++;
      if ($b1 != $b2) {
        my $type = ($b1) ? "FP" : "FN";
        if ($Report) {
          show_error(\*REPORT, $type, $lnum1->[$i1], $lnum2->[$i2], $file1, $file2, $lines1, $lines2);
        }
      }
      $i1++;
      $i2++;
    }
  }

  ## remaining data counts as false positives / negatives
  while ($i1 < $nc1) {
    $tpfp[1][0]++ if $bdry1->[$i1]; # false positive
    $i1++;
  }
  while ($i2 < $nc2) {
    $tpfp[0][1]++ if $bdry2->[$i2]; # false negative
    $i2++;
  }

  ## compute precision and recall
  my $tp = $tpfp[1][1];
  my $fp = $tpfp[1][0];
  my $fn = $tpfp[0][1];
  my $tn = $tpfp[0][0];

  my $prec = 100 * $tp / ($tp + $fp);
  my $recall = 100 * $tp / ($tp + $fn);
  my $fscore = 2 * $prec * $recall / ($prec + $recall);
  my $support = $tp + $fn; # support = number of tokens in gold standard

  print "\n";
  print "$file1 <=> $file2\n";

  printf "P =%6.2f%%  R =%6.2f%%  F =%6.2f%%\n", $prec, $recall, $fscore;
  printf "%6d false positives,%6d false negatives\n", $fp, $fn;

  if ($skip1 > 0 || $skip2 > 0) {
    print "\nWARNING: character mismatches detected (text has been modified)\n";
    printf  "  %6d / %d unmatched chars in file 1 = %5.2f%%\n", $skip1, $nc1, 100 * $skip1 / $nc1; 
    printf  "  %6d / %d unmatched chars in file 2 = %5.2f%%\n", $skip2, $nc2, 100 * $skip2 / $nc2; 
    print "NOTICE:  mismatched characters have been ignored (for now)\n";
  }

  return ($prec, $recall, $fscore, $support, $tp, $fp, $fn, $tn);
}


## read and normalize file, then return as array of chars with token boundary markers
## ($tokens, $bdrys, $line_nums, $lines) = read_file($filename);
sub read_file {
  my $filename = shift;
  my @chars = (); # normalized input text as sequence of chars without token delimiters
  my @bdry = ();  # boolean (0/1) indicating token boundarys (last char of each token)
  my @lnum = ();  # matching list of line numbers in original file
  my @lines = (); # original input lines (normalized)
  open(IN, "<:crlf:encoding(utf8)", $filename) or die "Error: Can't open input file '$filename': $!";
  while (<IN>) {
    chomp;
    $_ = NFC($_);           # normalize to NFC
    s/%%.*$//;              # remove annotator comments
    s/\p{C}//g;             # scrub any weird non-printing characters
    s/\p{Diacriticals}//g;  # scrub combining diacritical marks
    s/^\s+//; s/\s+$//;     # remove leading/trailing whitespace
    push @lines, $_;
    next if m{^$} && !$WithBlanks; # skip blank lines unless --with-blanks
    next if m{^</?\w+.*>$} && !$WithXML; # skip XML tags unless --with-xml
    s/\s+//g;               # remove whitespace within tokens (or warn?)
    $_ = " " if $_ eq "";   # empty tokens (should only happen --with-blanks) recoded as " "
    my $l = length($_);
    push @chars, split //, $_;      # append token to character sequence
    push @bdry, (0) x ($l - 1), 1;  # mark last character in token as boundary
    push @lnum, ($.) x $l;          # original input line for each char
  }
  close IN;
  die "INTERNAL ERROR" unless @chars == @bdry && @chars == @lnum;
  return \@chars, \@bdry, \@lnum, \@lines;
}

## display tokenization difference with some context, including file names and line numbers
## show_error($FH, $type, $line1, $line2, $fname1, $fname2, \@lines1, \@lines2);
BEGIN {
  my ($last_fname1, $last_fname2) = ("", "");
  
  sub cutstr {
    my $s = shift;
    my $n = (@_) ? shift : 48;
    return (length($s) > $n) ? "...".substr($s, -($n-3)) : $s;
  }

  sub show_error {
    my ($FH, $type, $line1, $line2, $fname1, $fname2, $lines1, $lines2) = @_;
    if ($fname1 ne $last_fname1 || $fname2 ne $last_fname2) {
      print $FH "_" x 98, "\n";
      printf $FH "%-48s  %-48s\n\n", cutstr($fname1), cutstr($fname2);
      $last_fname1 = $fname1;
      $last_fname2 = $fname2;
    }
    if ($type eq "FP") {
      print $FH "False Positive (linebreak inserted left):\n";
    }
    elsif ($type eq "FN") {
      print $FH "False Negative (linebreak inserted right):\n";
    }
    else {
      die "Internal error: invalid mismatch type '$type'";
    }
    foreach my $i (-2 .. +3) {
      my $l1 = $line1 + $i;
      my $l2 = $line2 + $i;
      if ($l1 > 0 && $l1 <= @$lines1) {
        my $ast = ($i == 0 || ($i == 1 && $type eq "FP")) ? "*" : "";
        printf $FH "%6d: %1.1s %-38.38s", $l1, $ast, $lines1->[$l1 - 1];
      }
      else {
        print $FH " " x 48;
      }
      print $FH "  ";
      if ($l2 > 0 && $l2 <= @$lines2) {
        my $ast = ($i == 0 || ($i == 1 && $type eq "FN")) ? "*" : "";
        printf $FH "%6d: %1.1s %-38.38s", $l2, $ast, $lines2->[$l2 - 1];
      }
      else {
        print $FH " " x 48;
      }
      print $FH "\n";
    }
    print $FH "\n";
  }
}