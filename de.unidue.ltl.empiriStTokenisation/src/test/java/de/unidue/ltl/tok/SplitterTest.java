package de.unidue.ltl.tok;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.unidue.ltl.tok.ListUtil;
import de.unidue.ltl.tok.Splitter;

public class SplitterTest {
	
	Splitter splitter;
	
	@Before
	public void setUp(){
		splitter=new Splitter();
		ListUtil.abbreviationFile = "src/main/resources/lists/abbreviations/list.txt";
		ListUtil.apostrophJoinedWords = "src/main/resources/lists/apostroph_s_joinedWords/list.txt";
	}

	@Test
	public void integrationTest() throws IOException {
		String text = "Wir trffn uns am 17.12.2016 um punkt\": 12Uhr..!. pünktlichkeit is einBegriff, odea^^?r Jeder ! (auch die \"Juppies\"): bringt u.a. sein essen usw. mit. Wer doch nich kann !!!!! ruft an@ Karsten 0170/11293929 an...:). Wird so (30) euro pro person werden mit 1.2 %%%% zinsen freu mich*G*";
		String[] ws = text.split(" ");

		String[] s = splitter.doSplitting(ws);
		for(String x : s){
			System.out.println(x);
		}

		text = "Kennst 39 de Fette.info\" ? ..:) oder OPML-Import) !* achte auf groß-/kleinschreibung..nicht 'Fehler(unbewusst) )) an….super! Anmerkungen:1. #Hashtag 23,2 <-- !!!) ???( tagesschau.de-Redaktion, 2013-12-12";
		ws = text.split(" ");
		s = splitter.doSplitting(ws);
	}

	@Test
	public void testExpressionJoinedByUnd(){
		assertTrue(splitter.isUndJoinedSequence("Groß-u.Kleinschreibung"));
		
		assertFalse(splitter.isUndJoinedSequence("Verfügung"));
	}
	
	@Test
	public void testSplittingOfExpressionJoinedByUnd(){
		String[] s = splitter.splitOf_und_joinedExpression("Groß-u.Kleinschreibung");
		assertEquals(3, s.length);
		assertEquals("Groß-", s[0]);
		assertEquals("u.", s[1]);
		assertEquals("Kleinschreibung", s[2]);
		
		s = splitter.splitOf_und_joinedExpression("Groß-u.Kleinschreibung");
		assertEquals(3, s.length);
		assertEquals("Groß-", s[0]);
		assertEquals("u.", s[1]);
		assertEquals("Kleinschreibung", s[2]);
	}

	@Test
	public void testEndsWithSpecialChars() {
		assertTrue(splitter.hasSpecialCharsAtTheEnd(":Daha:"));
		assertTrue(splitter.hasSpecialCharsAtTheEnd("haha?"));
		assertTrue(splitter.hasSpecialCharsAtTheEnd("Abfolge\"<--"));
		assertTrue(splitter.hasSpecialCharsAtTheEnd("2006):"));
		assertTrue(splitter.hasSpecialCharsAtTheEnd("..haha."));
		assertTrue(splitter.hasSpecialCharsAtTheEnd("erläutern."));
	}

	@Test
	public void testAbritaryLongRepitionOfCharacter() {
		assertTrue(splitter.isArbitraryLongRepitionOfTheSameCharacter("ffff"));
		assertTrue(splitter.isArbitraryLongRepitionOfTheSameCharacter("!!!!"));
		assertTrue(splitter.isArbitraryLongRepitionOfTheSameCharacter("1111"));
	}

	@Test
	public void testUnitForSpecialCharactersInTheMiddle() {
		assertTrue(splitter
				.hasTwoOrMoreSpecialCharsInTheMiddleOfTheUnit("ehm....yaa"));
		assertTrue(splitter
				.hasTwoOrMoreSpecialCharsInTheMiddleOfTheUnit("abcdef$()/(/&234"));
		assertTrue(splitter
				.hasTwoOrMoreSpecialCharsInTheMiddleOfTheUnit("lol???loool"));
	}

	@Test
	public void testRecognitionOfSpecialCharsWithLastCharSentEndingOrSeparatingChar() {
		assertTrue(splitter.specialOrNumWithLastCharPunct("(§(/,"));
		assertTrue(splitter.specialOrNumWithLastCharPunct("2006,"));
		assertTrue(splitter.specialOrNumWithLastCharPunct("2006."));
		assertTrue(splitter.specialOrNumWithLastCharPunct("2006?"));
		assertTrue(splitter.specialOrNumWithLastCharPunct("2006!"));
		assertTrue(splitter.specialOrNumWithLastCharPunct("\","));
	}

	@Test
	public void testSplittingOfLastCommaInSpecialCharUnit() {
		String[] split = splitter
				.separateLastCharIfSentenceSeparatingOrEndingCharacter("\",");
		assertEquals("\"", split[0]);
		assertEquals(",", split[1]);

		split = splitter
				.separateLastCharIfSentenceSeparatingOrEndingCharacter("983$.");
		assertEquals("983$", split[0]);
		assertEquals(".", split[1]);

		split = splitter
				.separateLastCharIfSentenceSeparatingOrEndingCharacter("983,");
		assertEquals("983", split[0]);
		assertEquals(",", split[1]);
	}

	@Test
	public void testSplittingOfRighLeftSequencesFromMiddleSpecialCharSequence() {
		String[] split = splitter
				.separateRightLeftSequenceFromSpecialChars("lol...alter");
		assertEquals("lol", split[0]);
		assertEquals("...", split[1]);
		assertEquals("alter", split[2]);
	}

	@Test
	public void isNounSuffixWithFollowingSpecialChars() {
		assertTrue(splitter
				.hasNounSuffixFollowedByMultipleDots("groß-/kleinschreibung..nicht "));
	}

	@Test
	public void isNumericalValueWithDotOrComma() {
		assertTrue(splitter.isNumericalValueWithComma("23,2"));
		assertTrue(splitter.isNumericalValueWithDot("23.2"));

		assertFalse(splitter.isNumericalValueWithDot("23."));
	}

	@Test
	public void testStartsWithSpecialChars() {
		assertTrue(splitter.hasSpecialCharsAtTheBeginning("*lol"));
		assertTrue(splitter.hasSpecialCharsAtTheBeginning("*lol*"));
	}

	@Test
	public void testAlphaNumSeqWithSingleSpecialCharInMiddle() {
		assertTrue(splitter
				.twoAlphaNumSequencesAreSeparatedBySingleSpecialChar("könnte,dass"));
	}

	@Test
	public void testSplitUpUnitsWithTrailingSpecialCharacter() {
		String[] split = splitter.separateSpecialCharactersAtTheEnd("haha?");
		assertEquals("haha", split[0]);
		assertEquals("?", split[1]);

		split = splitter.separateSpecialCharactersAtTheEnd("!!!!haha?");
		assertEquals("!!!!haha", split[0]);
		assertEquals("?", split[1]);

		split = splitter.separateSpecialCharactersAtTheEnd("!!!!höhö?");
		assertEquals("!!!!höhö", split[0]);
		assertEquals("?", split[1]);

	}

	@Test
	public void testSplitUpWithBeginningSpecialCharacter() {
		String[] splitPunctuation = splitter
				.separateSpecialCharactersAtTheBeginning("#haha?");
		assertEquals("#", splitPunctuation[0]);
		assertEquals("haha?", splitPunctuation[1]);
	}

	@Test
	public void testIsTwitterAtmentionOrHashtag() {
		assertTrue(splitter.isTwitterHashtagOrAtmention("@Dak_302"));
		assertTrue(splitter.isTwitterHashtagOrAtmention("#LolL00z3r"));
	}

	@Test
	public void testIsFirstCharQuote() {
		String s = "“Fettehenne.info”";
		assertTrue(splitter.firstCharIsQuote(s));
	}

	@Test
	public void testExtractionOfFirstChar() {
		String[] splitPunctuation = splitter.splitOfFirstChar("\"otto\"");
		assertEquals("\"", splitPunctuation[0]);
		assertEquals("otto\"", splitPunctuation[1]);
	}

	@Test
	public void testExtractionOfLastChar() {
		String[] splitPunctuation = splitter.splitOfLastChar("\"otto\"");
		assertEquals("\"otto", splitPunctuation[0]);
		assertEquals("\"", splitPunctuation[1]);
	}

	@Test
	public void testSeparationOfAlphaNumSeqThatAreConnectedBySingleSpecialCharacter() {
		String[] splitPunctuation = splitter
				.splitAtSeparatingSpecialCharacter("gesagt,dass");
		assertEquals("gesagt", splitPunctuation[0]);
		assertEquals(",", splitPunctuation[1]);
		assertEquals("dass", splitPunctuation[2]);
	}

	@Test
	public void testForExpressionWithinBrackets() {
		assertTrue(splitter
				.hasAlphaNumSequenceInBrackets("bewusst(haha)entschieden"));
		assertTrue(splitter
				.hasAlphaNumSequenceInBrackets("bewusst(oder eben nicht)entschieden"));
	}

	@Test
	public void testForStarSurroundedExpression() {
		assertTrue(splitter.hasStarSurroundedExpression("haha*g*"));
		assertTrue(splitter.hasStarSurroundedExpression("haha*g*türlich"));
	}

	@Test
	public void isUrl() {
		assertTrue(splitter.isUrl("http://hastenichtgesehen"));
		assertTrue(splitter.isUrl("https://hastenichtgesehen"));
		assertTrue(splitter.isUrl("www."));
	}

	@Test
	public void testIsCompound() {
		assertTrue(splitter.isCompound("Staats-Geschichte"));
		String s = String.format("\\u%04x", (int) '\'');
		System.out.println(s);
	}

	@Test
	public void testMightBeCamelCased() {
		assertTrue(splitter.isPureAlphaSeqWithSingleCapitalLetter("kommstDu"));
	}

	@Test
	public void testContainsTwoCharSmiley() {
		assertTrue(splitter.containsEmbeddedTwoCharSmiley(".=)."));
		assertTrue(splitter.containsEmbeddedTwoCharSmiley("lolxD."));
	}

	@Test
	public void testsplitOfEmbeddedTwoCharSmiley() {
		String[] s = splitter.splitOfEmbeddedTwoCharSmiley("..:)lol");
		assertEquals(3, s.length);
		assertEquals("..", s[0]);
		assertEquals(":)", s[1]);
		assertEquals("lol", s[2]);
	}

	@Test
	public void isExclamationOrQuestionMarkFollowedBySomethingElse() {
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("?omg"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("?!"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("???"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("?23"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("!omg"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("!?"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("!!"));
		assertTrue(splitter
				.isExclamationOrQuestionMarkFollowedBySomethingElse("!23"));
	}

	@Test
	public void testFullDecomposition() {
		String text = "Hallo";
		String[] x = splitter.decompose(text);
		assertEquals(5, x.length);
		assertEquals(x[0], "H");
		assertEquals(x[1], "a");
		assertEquals(x[2], "l");
		assertEquals(x[3], "l");
		assertEquals(x[4], "o");
	}

	@Test
	public void testFirstCharIsRoundBracket() {
		assertTrue(splitter.firstCharIsRoundBracket("(aaa"));
		assertFalse(splitter.firstCharIsRoundBracket(")bbb"));
	}

	@Test
	public void testFirstCharIsQuote() {
		assertTrue(splitter.firstCharIsQuote("\"lol"));
		assertFalse(splitter.firstCharIsQuote(")\""));
	}

	@Test
	public void isOneLetterDotOneLetterDotAbbreviation() {
		assertTrue(splitter.isOneLetterDotOneLetterAbbreviation("z.b."));
		assertFalse(splitter.isOneLetterDotOneLetterAbbreviation("usw"));
	}

	@Test
	public void testSplitUpAtChar() {
		String[] s = splitter.splitSequenceAtChar("haha,oder?", ",");
		assertEquals(3, s.length);
		assertEquals(s[0], "haha");
		assertEquals(s[1], ",");
		assertEquals(s[2], "oder?");
	}

	@Test
	public void testSuspiciouslyLong() {
		assertTrue(splitter
				.isSuspiciousSeqWithSeveralSeparatingSpecialChars("bmw,mercedes,audi,vm,opel,etc."));
		assertTrue(splitter
				.isSuspiciousSeqWithSeveralSeparatingSpecialChars("bmw/mercedes/audi/vm/opel/etc."));
	}

	@Test
	public void testSplitAfterNounSuffixWithFollowingDots() {
		String[] s = splitter
				.splitAfterNounSuffixWithFollowingDots("Heiterkeit....hurrah");
		assertEquals(3, s.length);
		assertEquals("Heiterkeit", s[0]);
		assertEquals("....", s[1]);
		assertEquals("hurrah", s[2]);
	}

	@Test
	public void testSplitUpAtCaptialLetter() {
		String[] s = splitter.splitAtCapitalLetter("fragUwe");
		assertEquals(2, s.length);
		assertEquals("frag", s[0]);
		assertEquals("Uwe", s[1]);
	}

	@Test
	public void isLastCharAtChar() {
		assertTrue(splitter.isLastCharAtCharacter("asfas@"));
	}

	@Test
	public void testExtractionOfBracketExpression() {
		String[] s = splitter
				.separateBracketExpression("Peter (der aus X) kommen auch");
		assertEquals(5, s.length);

		assertEquals("Peter ", s[0]);
		assertEquals("(", s[1]);
		assertEquals("der aus X", s[2]);
		assertEquals(")", s[3]);
		assertEquals(" kommen auch", s[4]);

		s = splitter.separateBracketExpression("{2005)");
		assertEquals(5, s.length);

		assertEquals("", s[0]);
		assertEquals("{", s[1]);
		assertEquals("2005", s[2]);
		assertEquals(")", s[3]);
		assertEquals("", s[4]);
	}

	@Test
	public void lastCharIsPunctuation() {
		assertTrue(splitter.lastCharIsPunctuation("as."));
		assertTrue(splitter.lastCharIsPunctuation("as?"));
		assertTrue(splitter.lastCharIsPunctuation("as!"));
		assertTrue(splitter.lastCharIsPunctuation("as,"));
		assertTrue(splitter.lastCharIsPunctuation("as:"));

		assertFalse(splitter.lastCharIsPunctuation("?"));
		assertFalse(splitter.lastCharIsPunctuation("!"));
		assertFalse(splitter.lastCharIsPunctuation(":"));
		assertFalse(splitter.lastCharIsPunctuation(","));
		assertFalse(splitter.lastCharIsPunctuation(""));
		assertFalse(splitter.lastCharIsPunctuation("d"));
	}
	
	@Test
	public void lastCharIsQuote() {
		assertTrue(splitter.lastCharIsQuote("asd" + '\u0022'));
		assertTrue(splitter.lastCharIsQuote("asd" + '\u201d'));
		assertTrue(splitter.lastCharIsQuote("asd" + '\u201c'));
		assertTrue(splitter.lastCharIsQuote("asd" + '\u201e'));
	}

	@Test
	public void firstCharIsQuote() {
		assertTrue(splitter.firstCharIsQuote('\u0022' + "asd"));
		assertTrue(splitter.firstCharIsQuote('\u201d' + "asd"));
		assertTrue(splitter.firstCharIsQuote('\u201c' + "asd"));
		assertTrue(splitter.firstCharIsQuote('\u201e' + "asd"));
	}

	@Test
	public void isPureNumber() {
		assertTrue(splitter.isPureNumber("234234"));
	}

	@Test
	public void isNumberWithDot() {
		assertTrue(splitter.isNumericalValueWithDot("23.3"));
	}

	@Test
	public void isNumberWithComma() {
		assertTrue(splitter.isNumericalValueWithComma("23,2"));
	}

	@Test
	public void testIsDateYYYYMMDD() {
		assertTrue(splitter.isDateYYYYMMDD("2013-12-12"));
		assertTrue(splitter.isDateYYYYMMDD("2013/12/12"));
		assertTrue(splitter.isDateYYYYMMDD("2013.12.12"));
	}

	@Test
	public void testIsDateYYYYMMDDsplitting() {
		String[] splitYYYYMMDD = splitter.splitYYYYMMDD("2013-12-12");
		assertEquals(3, splitYYYYMMDD.length);
		assertEquals("2013", splitYYYYMMDD[0]);
		assertEquals("-12", splitYYYYMMDD[1]);
		assertEquals("-12", splitYYYYMMDD[2]);
	}

	@Test
	public void testIsDateDDMMYYYY() {
		assertTrue(splitter.isDateDDMMYYYY("12-12-2013"));
		assertTrue(splitter.isDateDDMMYYYY("12/12/2013"));
		assertTrue(splitter.isDateDDMMYYYY("12.12.2013"));
	}

	@Test
	public void testIsDateDDMMYYYYsplitting() {
		String[] s = splitter.splitDDMMYYYY("12-12-2013");
		assertEquals(3, s.length);
		assertEquals("12-", s[0]);
		assertEquals("12-", s[1]);
		assertEquals("2013", s[2]);
	}

	@Test
	public void testSeparateAbbreviation() {
		String[] s = splitter.separateAbbreviation("u.a.");
		assertEquals(2, s.length);
		assertEquals("u.", s[0]);
		assertEquals("a.", s[1]);
	}

	@Test
	public void testSeparateStarSurroundedExpression() {
		String[] s = splitter.separateStarSurroundedExpression("*g*");
		assertEquals(5, s.length);
		assertEquals("", s[0]);
		assertEquals("*", s[1]);
		assertEquals("g", s[2]);
		assertEquals("*", s[3]);
		assertEquals("", s[4]);

		s = splitter.separateStarSurroundedExpression("haha*g*");
		assertEquals(5, s.length);
		assertEquals("haha", s[0]);
		assertEquals("*", s[1]);
		assertEquals("g", s[2]);
		assertEquals("*", s[3]);
		assertEquals("", s[4]);
	}

	@Test
	public void isArrow() {
		assertTrue(splitter.isArrow("<-----"));
	}

	@Test
	public void testSplitUpAtCharacters() {
		String[] s = splitter.splitUpAtCharacters(
				"peter,gustav,günther,jürgen,usw.", new String[] { "," });
		assertEquals(9, s.length);
		assertEquals("peter", s[0]);
		assertEquals(",", s[1]);
		assertEquals("gustav", s[2]);
		assertEquals(",", s[3]);
		assertEquals("günther", s[4]);
		assertEquals(",", s[5]);
		assertEquals("jürgen", s[6]);
		assertEquals(",", s[7]);
		assertEquals("usw.", s[8]);
	}
	
	@Test
	public void testIsNumberWithUnit() throws IOException{
		assertTrue(splitter.isNumFollowedByAlphaThatLooksLikeUnitName("23mb"));
		assertTrue(splitter.isNumFollowedByAlphaThatLooksLikeUnitName("3cm^2"));
		assertTrue(splitter.isNumFollowedByAlphaThatLooksLikeUnitName("3cm2"));
	}
	
	@Test
	public void testDotTerminatedAbbreviationMissingWhitespaceToNextWord() throws IOException{
	    assertTrue(splitter.isDotTerminatedAbbreviationFollowedByWordThatMissesWhiteSpace("allg.Wissen"));
	}
	
	@Test
	public void testSpecialCharRepitionWithFollowingAlpha(){
	    assertTrue(splitter.anyRepeatingSpecialCharFollowedByAlpha("((Hallo!"));
	}
	
	@Test
    public void testSplitAtSpecialCharRepitionWithFollowingAlpha(){
        String[] s = splitter.splitOffRepeatingSpecialCharRepitionAtBeginning("(((((Hallo!");
        assertEquals(2, s.length);
        assertEquals("(((((", s[0]);
        assertEquals("Hallo!",s[1]);
    }
	
	@Test
	public void testIsCamelCase(){
	    assertTrue(splitter.isCamelCase("IhrDokument"));
	    assertFalse(splitter.isCamelCase("Ihrdokument"));
	}
	
	@Test
	public void testSplitCamelCase(){
	    String [] s = splitter.splitOfCamelCaseWords("IhrDokument");
	    assertEquals(2, s.length);
	    assertEquals("Ihr", s[0]);
	    assertEquals("Dokument", s[1]);
	}
	
	@Test
	public void testIsRangeWithWordFacheAtTheEnd(){
	    assertTrue(splitter.isRangeWithWordFacheAtTheEnd("2-4fache"));
	    assertTrue(splitter.isRangeWithWordFacheAtTheEnd("2-4fachen"));
	}
	
	@Test
    public void testSplitIsRangeWithWordFacheAtTheEnd(){
        String[] s = splitter.splitIsRangeWithWordFacheAtTheEnd("2-4fache");
        assertEquals(3, s.length);
        assertEquals("2", s[0]);
        assertEquals("-", s[1]);
        assertEquals("4fache", s[2]);
    }
	
	@Test
	public void testAlphaSurroundedByEqual(){
	    assertTrue(splitter.containsAlphaSurroundedEqualSign("abc=dce"));
	}
	
	@Test
    public void testSplitAlphaSurroundedByEqual(){
	    String[] s = splitter.splitUpAtCharacters("abc=dce", new String [] {"="});
	    assertEquals(3, s.length);
	    assertEquals("abc", s[0]);
	    assertEquals("=", s[1]);
	    assertEquals("dce", s[2]);
    }
	
	@Test
	public void testLastCharIsClosingBracketAndNotPartOfASmiley(){
	    assertTrue(splitter.lastCharIsClosingBracketAndNotPartOfASmiley("aaa)"));
	}
	
	@Test
	public void testIsDotTerminatedAbbreviationFollowedByWordThatMissesWhiteSpace() throws IOException{
	    assertTrue(splitter.isDotTerminatedAbbreviationFollowedByWordThatMissesWhiteSpace("usw.und"));
	}
	
	@Test
	public void testSplitIsDotTerminatedAbbreviationFollowedByWordThatMissesWhiteSpace() {
	    String[] s = splitter.splitUpAtCharacters("usw.und", new String [] {"."});
	    assertEquals(3, s.length);
	    assertEquals("usw", s[0]);
	    assertEquals(".", s[1]);
	    assertEquals("und", s[2]);
	}
}
