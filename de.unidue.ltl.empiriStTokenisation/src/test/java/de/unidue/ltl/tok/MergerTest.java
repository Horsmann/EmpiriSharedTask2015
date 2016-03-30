package de.unidue.ltl.tok;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.tok.ListUtil;
import de.unidue.ltl.tok.Merger;

public class MergerTest
{

    Merger merger = new Merger();

    @Before
    public void setUp()
    {
        merger = new Merger();
        ListUtil.abbreviationFile = "src/main/resources/lists/abbreviations/list.txt";
        ListUtil.apostrophJoinedWords = "src/main/resources/lists/apostroph_s_joinedWords/list.txt";
    }
    
    @Test
    public void integrationTest() throws IOException{
        merger.doMerging(new String[] {":", ")", "haste", ".", ".", "..", "weis", "schon", "(", "-", "8", ".", "Die", "arbeiter", "Innen", "hams", "ja", "um", "300", "fache", "verschlimmert", "?", "!",  "u", ".", "a", ".", "#", "truth", ":", "-", "D", "Wo", "is", "mein", "iPod", "?", "btw", ".", "ausleihen", "u", ".", "schenken", "sind", "nicht", "das", "gleiche", ":", "wink", ":", "hehe", "*", "g", "*"});
        merger.doMerging(new String [] {"tom", "'", "s", "bildungs", "-", "und", "akademische", "inhalte", "oder", "wissens", "-", "darstellung"});
    }

    @Test
    public void testIsSmileyElement()
    {
        assertTrue(merger.isSmileyElement(new String[] { ":" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { ";" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { ")" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "(" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "8" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "p" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "P" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "d" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "D" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "-" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "x" }, 0));
        assertTrue(merger.isSmileyElement(new String[] { "X" }, 0));

        assertFalse(merger.isSmileyElement(new String[] { "?" }, 0));
        assertFalse(merger.isSmileyElement(new String[] { "!" }, 0));
    }

    @Test
    public void testIsSingleAlphaLetter()
    {
        assertTrue(merger.isSingleLetter(new String[] { "a" }, 0));
        assertTrue(merger.isSingleLetter(new String[] { "Z" }, 0));
        assertTrue(merger.isSingleLetter(new String[] { "ä" }, 0));
        assertTrue(merger.isSingleLetter(new String[] { "ö" }, 0));

        assertFalse(merger.isSingleLetter(new String[] { "1" }, 0));
        assertFalse(merger.isSingleLetter(new String[] { "3" }, 0));
        assertFalse(merger.isSingleLetter(new String[] { "_" }, 0));
    }

    @Test
    public void testIsHash()
    {
        assertTrue(merger.isHash(new String[] { "#" }, 0));
    }

    @Test
    public void testIsAlpha()
    {
        assertTrue(merger.isAlpha(new String[] { "auto_" }, 0));
        assertFalse(merger.isAlpha(new String[] { "auto_232" }, 0));
    }

    @Test
    public void testIsAlphaNum()
    {
        assertTrue(merger.isAlphaNum(new String[] { "auto" }, 0));
        assertTrue(merger.isAlphaNum(new String[] { "auto232" }, 0));
    }

    @Test
    public void testIsNumber()
    {
        assertTrue(merger.isNumber(new String[] { "23" }, 0));
    }

    @Test
    public void testIsDot()
    {
        assertTrue(merger.isDot(new String[] { ".." }, 0));
    }

    @Test
    public void isAtChar()
    {
        assertTrue(merger.isAtChar(new String[] { "@" }, 0));
    }

    @Test
    public void isOnlyCapitalLetter()
    {
        assertTrue(merger.isOnlyCapitalLetter(new String[] { "ABCDE" }, 0));
        assertFalse(merger.isOnlyCapitalLetter(new String[] { "ABCDe" }, 0));
    }

    @Test
    public void isSeq()
    {
        assertTrue(merger.isSeq(new String[] { "A" }, 0, "A"));
    }

    @Test
    public void isFwdThreeCharSmiley()
    {
        assertTrue(merger.isFwdThreeCharSmiley(new String[] { ":", "-", ")" }, 0, 1, 2));
        assertFalse(merger.isFwdThreeCharSmiley(new String[] { ":", "-", "K" }, 0, 1, 2));
    }

    @Test
    public void isClosingRoundBracketWithFollowingColonWhichIsNoSmiley()
    {
        assertTrue(merger.isClosingRoundBracketWithFollowingColonWhichIsNoSmiley(new String[] {
                "(", "blabla", ")", ":" }, 2, 3));
    }

    @Test
    public void isBckwdThreeCharSmiley()
    {
        assertTrue(merger.isBckwdThreeCharSmiley(new String[] { "(", "-", ":" }, 0, 1, 2));
        assertTrue(merger.isBckwdThreeCharSmiley(new String[] { "P", "-", ":" }, 0, 1, 2));
    }

    @Test
    public void isBckwdTwoCharSmiley()
    {
        assertTrue(merger.isBckwdTwoCharSmiley(new String[] { "(", ":" }, 0, 1));
        assertFalse(merger.isBckwdTwoCharSmiley(new String[] { "Ä", ":" }, 0, 1));
    }

    @Test
    public void isAposthrophTruncuationThatNeedsMerge() throws IOException{
        assertTrue(merger.isAposthrophTruncuationThatNeedsMerge(new String [] {"raffen", "'", "s"}, 0, 1, 2));
    }
    
    @Test
    public void isSingleDot(){
        assertTrue(merger.isSingleDot(new String [] {"."}, 0));
    }
    
    @Test
    public void isWord(){
        assertTrue(merger.isWord(new String []{"hi"}, 0, "hi" ));
    }
    
    @Test
    public void isSuffixFache(){
        assertTrue(merger.isSuffixFache(new String [] {"fache"}, 0));
    }
    
    @Test
    public void isSingleAlphaChar(){
        assertTrue(merger.isSingleAlphaChar(new String [] {"a"}, 0));
        assertFalse(merger.isSingleAlphaChar(new String [] {"aa"}, 0));
    }
    
    @Test
    public void isWordUnd(){
        assertTrue(merger.isWordUnd(new String [] {"und"}, 0));
        assertTrue(merger.isWordUnd(new String [] {"u."}, 0));
        assertFalse(merger.isWordUnd(new String [] {"a."}, 0));
    }
    
    @Test
    public void isOptionalFemaleNounSuffix(){
        assertTrue(merger.isOptionalFemaleNounSuffix(new String [] {"Innen"}, 0));
    }
    
    @Test
    public void isSemicolonFollowedByAlpha(){
        assertTrue(merger.isSemicolonFollowedByAlpha(new String [] {"'", "s"}, 0, 1));
    }
    
    @Test
    public void isDotSeparatedAbbreviation() throws IOException{
        assertTrue(merger.isDotSeparatedAbbreviation(new String[] {"usw", "."}, 0,1 ));
    }
}
