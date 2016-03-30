package de.unidue.ltl.tok;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class Splitter
{

    private Set<String> abbreviations = null;

    private final String w = "\\p{L}\\p{N}";

    private final String regExHasSpecialCharAtTheEnd = "([^" + w + "]*)([" + w + "]+)([^" + w
            + "]+)";
    private final String regExHasSpecialCharAtTheBeginning = "([^" + w + "]+)([" + w + "]+)([^" + w
            + "]*)";

    private final String regExHasSpecialOrNumWithTrailingSentSeparatingOrEndingPunc = "([^\\p{L}]+)([,?!\\.:])";

    private String regExHasTwoOrMoreSpecialCharInTheMiddle = "([\\p{L}\\p{N}]+)([^\\p{L}\\p{N}]{2,})([\\p{L}\\p{N}]+)";

    private String regExTwoAlphaNumSeqAreSeparatedBySingleSpecialChar = "([\\p{L}\\p{N}]+)([^\\p{L}\\p{N}])([\\p{L}\\p{N}]+)";

    private String regExAlphaNumExpressionInBrackets = "(.*)([(\\[{])([\\p{L}\\p{N} ]+)([)}\\]])(.*)";

    private String regExHasStarEnclosedExpression = "(.*)(\\*)([\\p{L}\\p{N}]+)(\\*)(.*)";

    private String regExIsNounSuffixFollowedByMultipleDots = "(.+[ung|heit|keit|scheit])([\\.]{2,})(.*)";

    private String regExOneLetterDotOneLetterAbbreviation = "([a-zA-ZäÄöÖüÜ]\\.)([a-zA-ZäÄöÖüÜ]\\.)";

    private String regExEmbeddedTwoCharSmiley = "(.+)([:|=|x][\\)|\\(|d|D])(.*)";

    private String regExYYYYMMDD = "([12][0-9]{3})([\\-\\./][0-9]{1,2})([\\-\\./][0-9]{1,2})";
    private String regExDDMMYYYY = "([0-9]{1,2}[\\-\\./])([0-9]{1,2}[\\-\\./])([12][0-9]{3})";

    private String regExUndJoinedExpression = "([A-Za-zäÄöÖüß]+[\\-]*)(u\\.|und)([A-Za-zäÄöÖüß]+)";

    private String regExNumFollowedByUnitName = "([0-9]+)([A-Za-z]+[\\^]*[0-9]*)";

    private String regExSpecialCharRepitionFollowedByAlpha = "(([^a-zA-Z0-9])\\2+)([a-zA-Z].*)";

    private String regExCamelCase = "([A-ZÄÖÜ][a-zäöüß]+)([A-ZÄÖÜ][a-zäöüß]+)";

    private String regExQuantificationWithFache="([0-9])+(\\-)([0-9]+fache[n]*)";

    public String[] doSplitting(String[] tokens)
        throws IOException
    {
        int oldSize = 0;
        int newSize = tokens.length;
        do {
            oldSize = tokens.length;
            List<Integer> todos = selectSuspectsForFurtherSplitting(tokens);
            tokens = refineSplitting(tokens, todos);
            newSize = tokens.length;
        }
        while (oldSize != newSize);

        return tokens;
    }

    public List<Integer> selectSuspectsForFurtherSplitting(String[] whiteSpaceSplitted)
    {
        List<Integer> candidateList = new ArrayList<Integer>();
        for (int i = 0; i < whiteSpaceSplitted.length; i++) {
            String unit = whiteSpaceSplitted[i];
            
            if (isPureAlphaUnitEitherNoCapitalLetterOrOnlyAtFirstPosition(unit)) {
                continue;
            }

            if (isPureNumber(unit)) {
                continue;
            }

            if (isArbitraryLongRepitionOfTheSameCharacter(unit) && !containsRoundBracket(unit)) {
                continue;
            }

            if (isTwitterHashtagOrAtmention(unit)) {
                continue;
            }

            if (isUrl(unit)) {
                continue;
            }

            if (isArrow(unit)) {
                continue;
            }

            if (isCompound(unit)) {
                continue;
            }

            if (isNumericalValueWithDot(unit)) {
                continue;
            }

            if (isNumericalValueWithComma(unit)) {
                continue;
            }

            if(isAbbreviatedUnd(unit)){
                continue;
            }

            candidateList.add(i);

        }
        return candidateList;
    }

    private boolean isAbbreviatedUnd(String unit)
    {
        return unit.equals("u.");
    }

    static boolean containsRoundBracket(String unit)
    {
        return unit.contains("(") || unit.contains(")");
    }

    public String[] refineSplitting(String[] whiteSpaceSplitted, List<Integer> todos)
        throws IOException
    {

        List<Update> newSplits = new ArrayList<Update>();

        for (Integer idx : todos) {
            String unit = whiteSpaceSplitted[idx];
            
            if (isOneLetterDotOneLetterAbbreviation(unit)) {
                String[] result = separateAbbreviation(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isDotTerminatedAbbreviationFollowedByWordThatMissesWhiteSpace(unit)) {
                String[] result = splitUpAtCharacters(unit, new String[] { "." });
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isArbitraryLongRepitionOfTheSameCharacter(unit)) {
                String[] result = decompose(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (hasStarSurroundedExpression(unit)) {
                String[] result = separateStarSurroundedExpression(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (hasSpecialCharsAtTheBeginning(unit)) {
                String[] result = separateSpecialCharactersAtTheBeginning(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (hasAlphaNumSequenceInBrackets(unit)) {
                String[] result = separateBracketExpression(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (hasSpecialCharsAtTheEnd(unit)) {
                String[] result = separateSpecialCharactersAtTheEnd(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (specialOrNumWithLastCharPunct(unit)) {
                String[] result = separateLastCharIfSentenceSeparatingOrEndingCharacter(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (hasTwoOrMoreSpecialCharsInTheMiddleOfTheUnit(unit)) {
                String[] result = separateRightLeftSequenceFromSpecialChars(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (lastCharIsPunctuation(unit)) {
                String[] result = splitOfLastChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }
            
            
            if(isRangeWithWordFacheAtTheEnd(unit)){
                String[] result = splitIsRangeWithWordFacheAtTheEnd(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (twoAlphaNumSequencesAreSeparatedBySingleSpecialChar(unit)) {
                String[] result = splitAtSeparatingSpecialCharacter(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isLastCharAtCharacter(unit)) {
                String[] result = splitOfLastChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isPureAlphaSeqWithSingleCapitalLetter(unit)) {
                String[] result = splitAtCapitalLetter(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (hasNounSuffixFollowedByMultipleDots(unit)) {
                String[] result = splitAfterNounSuffixWithFollowingDots(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isSuspiciousSeqWithSeveralSeparatingSpecialChars(unit)) {
                String[] result = splitUpAtCharacters(unit, new String[] { ",", "/", "." });
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isExclamationOrQuestionMarkFollowedBySomethingElse(unit)) {
                String[] result = splitOfFirstChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (containsEmbeddedTwoCharSmiley(unit)) {
                String[] result = splitOfEmbeddedTwoCharSmiley(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isDateYYYYMMDD(unit)) {
                String[] result = splitYYYYMMDD(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isDateDDMMYYYY(unit)) {
                String[] result = splitDDMMYYYY(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (firstCharIsHyphen(unit)) {
                String[] result = splitOfFirstChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }


            if (firstCharIsQuote(unit)) {
                String[] result = splitOfFirstChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (lastCharIsQuote(unit)) {
                String[] result = splitOfLastChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (firstCharIsRoundBracket(unit)) {
                String[] result = splitOfFirstChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (lastCharIsClosingBracket(unit)) {
                String[] result = splitOfLastChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (alphaSeqWithLastCharIsColon(unit)) {
                String[] result = splitOfLastChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (lastCharIsClosingBracketAndNotPartOfASmiley(unit)) {
                String[] result = splitOfLastChar(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isUndJoinedSequence(unit)) {
                String[] result = splitOf_und_joinedExpression(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (isNumFollowedByAlphaThatLooksLikeUnitName(unit)) {
                String[] result = splitNumFromAlphaUnitName(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (containsAlphaSurroundedEqualSign(unit)) {
                String[] result = splitUpAtCharacters(unit, new String[] { "=" });
                newSplits.add(new Update(idx, result));
                continue;
            }

            if (anyRepeatingSpecialCharFollowedByAlpha(unit)) {
                String[] result = splitOffRepeatingSpecialCharRepitionAtBeginning(unit);
                newSplits.add(new Update(idx, result));
                continue;
            }
            
        }

        if (newSplits.isEmpty()) {
            return whiteSpaceSplitted;
        }

        String[] results = removeEmptyStringsThatMightHaveOccuredByEmptyRegExGroups(
                whiteSpaceSplitted, newSplits);
        return results;
    }

    String[] splitIsRangeWithWordFacheAtTheEnd(String unit)
    {
        Pattern p = Pattern.compile(regExQuantificationWithFache);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second , third};
    }

    //2-4fache
    boolean isRangeWithWordFacheAtTheEnd(String unit)
    {
        return Pattern.matches(regExQuantificationWithFache, unit);
    }

    boolean isCamelCase(String unit)
    {
        return Pattern.matches(regExCamelCase, unit);
    }

    String[] splitOfCamelCaseWords(String unit)
    {
        Pattern p = Pattern.compile(regExCamelCase);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        return new String[] { first, second };
    }

    String[] splitOffRepeatingSpecialCharRepitionAtBeginning(String unit)
    {
        Pattern p = Pattern.compile(regExSpecialCharRepitionFollowedByAlpha);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String third = m.group(3);
        return new String[] { first, third };

    }

    boolean anyRepeatingSpecialCharFollowedByAlpha(String unit)
    {
        if (Pattern.matches(regExSpecialCharRepitionFollowedByAlpha, unit)) {
            return true;
        }

        return false;
    }

    boolean isDotTerminatedAbbreviationFollowedByWordThatMissesWhiteSpace(String unit)
        throws IOException
    {
        if (!Pattern.matches("[a-zA-ZäöüÄÖÜß_\\-]+\\.[a-zA-ZäöüÄÖÜß_\\-]+", unit)) {
            return false;
        }

        String[] split = unit.split("\\.");

        String abbrvCandidate = split[0].toLowerCase() + ".";

        return ListUtil.isAbbreviation(abbrvCandidate, abbreviations);
    }

    boolean containsAlphaSurroundedEqualSign(String unit)
    {
        return Pattern.matches(".*[a-zäöü]+=[a-zäöü]+.*", unit);
    }

    private String[] splitNumFromAlphaUnitName(String unit)
    {
        Pattern p = Pattern.compile(regExNumFollowedByUnitName);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        return new String[] { first, second };
    }

    boolean isNumFollowedByAlphaThatLooksLikeUnitName(String unit)
    {
        // 23cent, 3euro, 23mb, etc.
        return Pattern.matches(regExNumFollowedByUnitName, unit);
    }

    String[] splitOf_und_joinedExpression(String unit)
    {
        Pattern p = Pattern.compile(regExUndJoinedExpression);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };
    }

    boolean isUndJoinedSequence(String unit)
    {
        return Pattern.matches(regExUndJoinedExpression, unit);
    }

    boolean lastCharIsClosingBracketAndNotPartOfASmiley(String unit)
    {
        return Pattern.matches(".*[a-zäüö]\\)", unit);
    }

    boolean lastCharIsHyphen(String unit)
    {
        return unit.endsWith("-");
    }

    boolean firstCharIsHyphen(String unit)
    {
        return unit.startsWith("-");
    }

    String[] removeEmptyStringsThatMightHaveOccuredByEmptyRegExGroups(String[] whiteSpaceSplitted,
            List<Update> newSplits)
    {
        List<String> out = new ArrayList<String>();

        int idx = 0;
        for (int i = 0; i < whiteSpaceSplitted.length; i++) {
            if (idx < newSplits.size() && newSplits.get(idx).i == i) {
                for (String s : newSplits.get(idx).split) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    out.add(s);
                }
                idx++;
                continue;
            }
            out.add(whiteSpaceSplitted[i]);
        }

        return out.toArray(new String[0]);
    }

    String[] splitDDMMYYYY(String unit)
    {
        Pattern p = Pattern.compile(regExDDMMYYYY);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };
    }

    boolean isDateDDMMYYYY(String unit)
    {
        return Pattern.matches(regExDDMMYYYY, unit);
    }

    String[] splitYYYYMMDD(String unit)
    {
        Pattern p = Pattern.compile(regExYYYYMMDD);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };
    }

    boolean isDateYYYYMMDD(String unit)
    {
        return Pattern.matches(regExYYYYMMDD, unit);
    }

    String[] splitOfEmbeddedTwoCharSmiley(String unit)
    {
        Pattern p = Pattern.compile(regExEmbeddedTwoCharSmiley);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };

    }

    boolean containsEmbeddedTwoCharSmiley(String unit)
    {
        return Pattern.matches(regExEmbeddedTwoCharSmiley, unit);
    }

    boolean isExclamationOrQuestionMarkFollowedBySomethingElse(String unit)
    {
        return Pattern.matches("[?|!].+", unit);
    }

    String[] decompose(String unit)
    {
        List<String> pieces = new ArrayList<String>();

        for (char c : unit.toCharArray()) {
            pieces.add("" + c);
        }
        return pieces.toArray(new String[0]);
    }

    boolean firstCharIsRoundBracket(String unit)
    {
        return Pattern.matches("(\\(|\\[|\\{)[A-Za-z0-9äöü]+.*", unit);
    }

    private boolean lastCharIsClosingBracket(String unit)
    {
        return Pattern.matches(".*[A-Za-z0-9äöü]+(\\)|\\]|\\})", unit);
    }

    String[] separateAbbreviation(String unit)
    {
        Pattern p = Pattern.compile(regExOneLetterDotOneLetterAbbreviation);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        return new String[] { first, second };
    }

    boolean isOneLetterDotOneLetterAbbreviation(String unit)
    {
        // this comes down to "z.B." or "u.a." which have to split up into
        // "z."/"B." or "u."/"a.".
        return Pattern.matches(regExOneLetterDotOneLetterAbbreviation, unit);
    }

    String[] splitUpAtCharacters(String unit, String[] strings)
    {
        List<String> pieces = new ArrayList<String>();
        pieces.add(unit);

        for (String seperators : strings) {
            List<String> out = new ArrayList<String>();
            for (String piece : pieces) {
                String[] split = splitSequenceAtChar(piece, seperators);
                for (String s : split) {
                    out.add(s);
                }
            }
            pieces = out;
        }
        return pieces.toArray(new String[0]);
    }

    String[] splitSequenceAtChar(String piece, String seperator)
    {

        List<Integer> idx = new ArrayList<Integer>();

        int start = 0;
        int pos = -1;
        while ((pos = piece.indexOf(seperator, start)) > -1) {
            idx.add(pos);
            start = pos + 1;
        }

        if (idx.isEmpty()) {
            return new String[] { piece };
        }

        List<String> split = new ArrayList<String>();
        start = 0;
        for (int j = 0; j < idx.size(); j++) {
            Integer i = idx.get(j);
            split.add(piece.substring(start, i));
            split.add("" + piece.charAt(i));
            start = i + 1;
            if (j + 1 >= idx.size()) {
                split.add(piece.substring(start));
            }
        }

        return split.toArray(new String[0]);
    }

    boolean isSuspiciousSeqWithSeveralSeparatingSpecialChars(String unit)
    {

        int numSlashes = 0;
        int numComma = 0;
        int numDots = 0;

        for (char c : unit.toCharArray()) {
            if (c == '/') {
                numSlashes++;
            }
            if (c == ',') {
                numComma++;
            }
            if (c == '.') {
                numDots++;
            }
        }

        return numSlashes >= 3 || numComma >= 3 || numDots >= 3;
    }

    String[] splitAfterNounSuffixWithFollowingDots(String unit)
    {
        Pattern p = Pattern.compile(regExIsNounSuffixFollowedByMultipleDots);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };
    }

    boolean hasNounSuffixFollowedByMultipleDots(String unit)
    {
        return Pattern.matches(regExIsNounSuffixFollowedByMultipleDots, unit);
    }

    boolean alphaSeqWithLastCharIsColon(String unit)
    {
        return Pattern.matches("[a-zA-Z\\-]+\\)", unit);
    }

    String[] splitAtCapitalLetter(String unit)
    {
        int x = -1;
        for (int i = 1; i < unit.length(); i++) {
            if (StringUtils.isAllUpperCase("" + unit.charAt(i))) {
                x = i;
                break;
            }
        }

        String first = unit.substring(0, x);
        String second = unit.substring(x);

        return new String[] { first, second };
    }

    boolean isPureAlphaSeqWithSingleCapitalLetter(String unit)
    {
        if (unit.length() <= 2) {
            return false;
        }

        boolean isAlpha = Pattern.matches("[a-zöäß][a-zA-ZöÖüÜäÄß]+", unit);
        if (!isAlpha) {
            return false;
        }

        int numCapLet = 0;
        for (int i = 1; i < unit.length(); i++) {
            if (StringUtils.isAllUpperCase("" + unit.charAt(i))) {
                numCapLet++;
            }
        }

        return isAlpha && numCapLet == 1;
    }

    boolean isLastCharAtCharacter(String unit)
    {
        return unit.endsWith("@");
    }

    String[] separateStarSurroundedExpression(String unit)
    {
        Pattern p = Pattern.compile(regExHasStarEnclosedExpression);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        String fourth = m.group(4);
        String five = m.group(5);
        return new String[] { first, second, third, fourth, five };
    }

    boolean hasStarSurroundedExpression(String unit)
    {
        return Pattern.matches(regExHasStarEnclosedExpression, unit);
    }

    String[] separateBracketExpression(String unit)
    {
        Pattern p = Pattern.compile(regExAlphaNumExpressionInBrackets);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        String fourth = m.group(4);
        String five = m.group(5);
        return new String[] { first, second, third, fourth, five };
    }

    boolean hasAlphaNumSequenceInBrackets(String unit)
    {
        return Pattern.matches(regExAlphaNumExpressionInBrackets, unit);
    }

    boolean lastCharIsPunctuation(String unit)
    {
        if (unit.length() <= 1) {
            return false;
        }
        char c = unit.toCharArray()[unit.length() - 1];
        return c == '!' || c == '?' || c == '.' || c == ':' || c == ',' || c == '.';
    }

    String[] splitAtSeparatingSpecialCharacter(String unit)
    {
        Pattern p = Pattern.compile(regExTwoAlphaNumSeqAreSeparatedBySingleSpecialChar);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };
    }

    boolean twoAlphaNumSequencesAreSeparatedBySingleSpecialChar(String unit)
    {
        return Pattern.matches(regExTwoAlphaNumSeqAreSeparatedBySingleSpecialChar, unit);
    }

    String[] splitOfLastChar(String unit)
    {
        String start = unit.substring(0, unit.length() - 1);
        String lastChar = "" + unit.charAt(unit.length() - 1);
        return new String[] { start, lastChar };
    }

    String[] splitOfFirstChar(String unit)
    {
        String firstChar = "" + unit.toCharArray()[0];
        String rest = unit.substring(1);
        return new String[] { firstChar, rest };
    }

    boolean lastCharIsQuote(String unit)
    {

        boolean len = unit.length() > 1;
        boolean lastChar = unit.toCharArray()[unit.length() - 1] == '\u0022'
                || unit.toCharArray()[unit.length() - 1] == '\u201d'
                || unit.toCharArray()[unit.length() - 1] == '\u201c'
                || unit.toCharArray()[unit.length() - 1] == '\u201e';

        return len && lastChar;
    }

    boolean firstCharIsQuote(String unit)
    {
        boolean len = unit.length() > 1;
        boolean firstChar = unit.toCharArray()[0] == '\u0022' || unit.toCharArray()[0] == '\u201d'
                || unit.toCharArray()[0] == '\u201c' || unit.toCharArray()[0] == '\u201e';

        return len && firstChar;
    }

    String[] separateRightLeftSequenceFromSpecialChars(String unit)
    {
        Pattern p = Pattern.compile(regExHasTwoOrMoreSpecialCharInTheMiddle);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        String third = m.group(3);
        return new String[] { first, second, third };
    }

    boolean hasTwoOrMoreSpecialCharsInTheMiddleOfTheUnit(String unit)
    {
        return Pattern.matches(regExHasTwoOrMoreSpecialCharInTheMiddle, unit);
    }

    String[] separateLastCharIfSentenceSeparatingOrEndingCharacter(String unit)
    {
        Pattern p = Pattern.compile(regExHasSpecialOrNumWithTrailingSentSeparatingOrEndingPunc);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2);
        return new String[] { first, second };
    }

    boolean specialOrNumWithLastCharPunct(String unit)
    {
        return Pattern.matches(regExHasSpecialOrNumWithTrailingSentSeparatingOrEndingPunc, unit);
    }

    String[] separateSpecialCharactersAtTheBeginning(String unit)
    {
        Pattern p = Pattern.compile(regExHasSpecialCharAtTheBeginning);
        Matcher m = p.matcher(unit);
        m.find();
        String first = m.group(1);
        String second = m.group(2) + m.group(3);
        return new String[] { first, second };
    }

    boolean hasSpecialCharsAtTheBeginning(String unit)
    {
        return Pattern.matches(regExHasSpecialCharAtTheBeginning, unit);
    }

    String[] separateSpecialCharactersAtTheEnd(String unit)
    {
        Pattern p = Pattern.compile(regExHasSpecialCharAtTheEnd);
        Matcher m = p.matcher(unit);
        m.find();

        String first = null;
        String second = null;
        first = m.group(1) + m.group(2);
        second = m.group(3);
        return new String[] { first, second };
    }

    boolean hasSpecialCharsAtTheEnd(String unit)
    {
        return Pattern.matches(regExHasSpecialCharAtTheEnd, unit);
    }

    boolean isArrow(String unit)
    {
        return Pattern.matches("<[-]+", unit);
    }

    boolean isUrl(String unit)
    {
        return Pattern.matches("^http.+|^https.+|^www.+", unit);
    }

    boolean isNumericalValueWithComma(String unit)
    {
        return Pattern.matches("[0-9]+\\,[0-9]+", unit);
    }

    boolean isNumericalValueWithDot(String unit)
    {
        return Pattern.matches("[0-9]+\\.[0-9]+", unit);
    }

    boolean isCompound(String unit)
    {
        return Pattern.matches("[\\p{L}]+-[\\p{L}]+", unit);
    }

    boolean isTwitterHashtagOrAtmention(String unit)
    {
        return Pattern.matches("[@|#][\\wöÖüÜäÄß]+", unit);
    }

    boolean isArbitraryLongRepitionOfTheSameCharacter(String unit)
    {
        return Pattern.matches("(.)\\1+", unit);
    }

    boolean isPureNumber(String unit)
    {
        return StringUtils.isNumeric(unit);
    }

    boolean isPureAlphaUnitEitherNoCapitalLetterOrOnlyAtFirstPosition(String unit)
    {
        return Pattern.matches("[A-ZÖÜÄ]*[a-z]+[0-9a-z]+", unit);
    }

}
