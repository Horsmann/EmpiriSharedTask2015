package de.unidue.ltl.tok;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Merger
{

    Set<Integer> idxAmongCandidates = null;
    Set<String> abbreviations = null;
    Set<String> aposthrophTruncations = null;

    String regExBottom = "\\)|\\(|D|p|P|o|O";
    String regExTop = ":|=|8|;|x";

    public String[] doMerging(String[] tokens)
        throws IOException
    {

        int oldSize = 0;
        int newSize = tokens.length;
        do {
            oldSize = tokens.length;
            List<Integer[]> todos = selectMergeCandidates(tokens);
            tokens = merge(tokens, todos);
            newSize = tokens.length;
        }
        while (oldSize != newSize);

        return tokens;

    }

    private String[] merge(String[] tokens, List<Integer[]> todos)
    {

        if (todos.isEmpty()) {
            return tokens;
        }

        List<String> merged = new ArrayList<String>();

        int m = 0;
        for (int i = 0; i < tokens.length; i++) {
            if (needsMerge(todos, m, i)) {
                Integer[] integers = todos.get(m);
                StringBuilder sb = new StringBuilder();
                for (int j = integers[0]; j <= integers[integers.length - 1]; j++) {
                    sb.append(tokens[j]);
                }
                i = integers[integers.length - 1];
                merged.add(sb.toString());
                m++;
            }
            else {
                merged.add(tokens[i]);
            }
        }

        return merged.toArray(new String[0]);
    }

    private boolean needsMerge(List<Integer[]> todos, int m, int i)
    {
        if (m >= todos.size()) {
            return false;
        }
        if (todos.get(m)[0] != i) {
            return false;
        }
        return true;
    }

    public List<Integer[]> selectMergeCandidates(String[] tokens)
        throws IOException
    {

        List<Integer[]> candidates = new ArrayList<Integer[]>();

        idxAmongCandidates = new HashSet<Integer>();

        for (int i = 0; i < tokens.length; i++) {

            if (isChar(tokens, i, "-") && isThreeDigitNumber(tokens, i + 1) && !isSuffixFache(tokens, i + 2)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isThreeDigitNumber(tokens, i) && isSuffixFache(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isChar(tokens, i, "\\?") && isChar(tokens, i + 1, "!")) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isThreeDigitNumber(tokens, i) && isDot(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isFwdThreeCharSmiley(tokens, i, i + 1, i + 2)) {
                candidates.add(new Integer[] { i, i + 1, i + 2 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
                continue;
            }

            if (isBckwdThreeCharSmiley(tokens, i, i + 1, i + 2)) {
                candidates.add(new Integer[] { i, i + 1, i + 2 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
                continue;
            }

            if (isFwdTwoCharSmiley(tokens, i, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isBckwdTwoCharSmiley(tokens, i, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isAtChar(tokens, i) && isAlpha(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isHash(tokens, i) && isAlphaNum(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isOnlyCapitalLetter(tokens, i) && isDot(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isDot(tokens, i) && isDot(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                continue;
            }

            if (isSingleLetter(tokens, i) && isDot(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isChar(tokens, i, "'") && isChar(tokens, i + 1, "s")) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isChar(tokens, i, "'") && isSeq(tokens, i + 1, "nem")) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isAlphaFollowedByDotThatIsKnownAbbreviation(tokens, i, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }
            if (isWellKnownCamelCaseWord(tokens, i, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isThreeDigitNumber(tokens, i - 1) && isChar(tokens, i, ":") && isThreeDigitNumber(tokens, i + 1)) {
                candidates.add(new Integer[] { i - 1, i, i + 1 });
                idxAmongCandidates.add(i - 1);
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isDotSeparatedAbbreviation(tokens, i, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isAposthrophTruncuationThatNeedsMerge(tokens, i, i + 1, i + 2)) {
                candidates.add(new Integer[] { i, i + 1, i + 2 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
            }

            if (isWordUnd(tokens, i) && isChar(tokens, i + 1, "-") && isAlphaNum(tokens, i + 2)) {
                candidates.add(new Integer[] { i + 1, i + 2 });
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
            }

            if (isChar(tokens, i, "-") && isSingleAlphaChar(tokens, i + 1)) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isAlpha(tokens, i - 1) && isOptionalFemaleNounSuffix(tokens, i)) {
                candidates.add(new Integer[] { i - 1, i });
                idxAmongCandidates.add(i - 1);
                idxAmongCandidates.add(i);
            }

            if (isAlpha(tokens, i - 1) && isChar(tokens, i, "-") && isChar(tokens, i + 1, "/")) {
                candidates.add(new Integer[] { i - 1, i });
                idxAmongCandidates.add(i - 1);
                idxAmongCandidates.add(i);
            }

            if (isAlpha(tokens, i - 1) && isChar(tokens, i, "'") && isAlpha(tokens, i + 1)) {
                candidates.add(new Integer[] { i - 1, i, i + 1 });
                idxAmongCandidates.add(i - 1);
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isChar(tokens, i, ":") && isWord(tokens, i + 1, "wink")
                    && isChar(tokens, i + 2, ":")) {
                candidates.add(new Integer[] { i, i + 1, i + 2 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
            }

            if ((isWord(tokens, i, "bildungs") || isWord(tokens, i, "welt") || isWord(tokens, i,
                    "forschungs")) && isChar(tokens, i + 1, "-")) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isWord(tokens, i, "o.") && isWord(tokens, i + 1, "k.")) {
                candidates.add(new Integer[] { i, i + 1 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
            }

            if (isAlphaNum(tokens, i) && isSingleDot(tokens, i + 1)
                    && isWord(tokens, i + 2, "info")) {
                candidates.add(new Integer[] { i, i + 1, i + 2 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
            }
            
            if(isWord(tokens, i, "ISBN") && isNumber(tokens, i+1) && isChar(tokens, i+2, "x")){
                candidates.add(new Integer[] { i, i + 1, i + 2 });
                idxAmongCandidates.add(i);
                idxAmongCandidates.add(i + 1);
                idxAmongCandidates.add(i + 2);
            }
        }

        return candidates;
    }

      boolean isSingleDot(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].equals(".");
    }

      boolean isWord(String[] tokens, int i, String word)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].toLowerCase().equals(word.toLowerCase());
    }

     boolean isSuffixFache(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].matches("fache[n]*|mal[e]*");
    }

    boolean isSingleAlphaChar(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].length() == 1 && Pattern.matches("[a-zäöüß]", tokens[i]);
    }

    boolean isWordUnd(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }
        boolean caseA = isSeq(tokens, i, "und");
        boolean caseB = isSeq(tokens, i, "u.");
        return caseA || caseB;
    }

    boolean isOptionalFemaleNounSuffix(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }
        return tokens[i].equals("Innen");
    }

    boolean isAposthrophTruncuationThatNeedsMerge(String[] tokens, int i, int j, int k)
        throws IOException
    {
        initAposthrophJoinedWordPairList();
        if (!validIndices(tokens, i, j, k)) {
            return false;
        }

        String candidate = tokens[i] + tokens[j] + tokens[k];
        return aposthrophTruncations.contains(candidate.toLowerCase());
    }

    void initAposthrophJoinedWordPairList()
        throws IOException
    {
        aposthrophTruncations = ListUtil.loadApostrophConnectedUnits(aposthrophTruncations);
    }

    boolean isSemicolonFollowedByAlpha(String[] tokens, int i, int j)
    {
        if (!validIndices(tokens, i, j)) {
            return false;
        }

        boolean isSemicolon = tokens[i].equals("'");
        if (!isSemicolon) {
            return false;
        }
        return isAlpha(tokens, j);
    }

    boolean isDotSeparatedAbbreviation(String[] tokens, int i, int j)
        throws IOException
    {
        if (!validIndices(tokens, i, j)) {
            return false;
        }
        if (!tokens[j].equals(".")) {
            return false;
        }

        String candidate = tokens[i] + ".";
        return ListUtil.isAbbreviation(candidate.toLowerCase(), abbreviations);
    }

    boolean isBckwdThreeCharSmiley(String[] tokens, int i, int j, int k)
    {
        if (!validIndices(tokens, i, j, k)) {
            return false;
        }

        boolean mouth = isChar(tokens, i, regExBottom);
        boolean nose = isChar(tokens, j, "\\-");
        boolean eyes = isChar(tokens, k, regExTop);

        return mouth && nose && eyes;
    }

    boolean isBckwdTwoCharSmiley(String[] tokens, int i, int j)
    {
        if (!validIndices(tokens, i, j)) {
            return false;
        }

        boolean top = isChar(tokens, i, regExBottom);
        boolean bottom = isChar(tokens, j, regExTop);
        boolean confusionCase = !isClosingRoundBracketWithFollowingColonWhichIsNoSmiley(tokens, i,
                j);
        return bottom && top && confusionCase;
    }

    // for example "blabla (blabla): blabla" the closing round bracket followed
    // by colon is no smiley
    boolean isClosingRoundBracketWithFollowingColonWhichIsNoSmiley(String[] tokens, int i, int j)
    {
        if (!validIndices(tokens, i, j)) {
            return false;
        }

        boolean isBckwdSadSmiley = tokens[i].equals(")") && tokens[j].equals(":");
        boolean hasOpeningRoundBrackInWindow = SharedUtils.checkPreviousNslotsForChar(tokens, "(",
                i, 5);

        return isBckwdSadSmiley && hasOpeningRoundBrackInWindow;
    }

    boolean validIndices(String[] tokens, int... item)
    {
        for (int x : item) {
            if (x >= tokens.length || x < 0
                    || (idxAmongCandidates != null && idxAmongCandidates.contains(x))) {
                return false;
            }
        }
        return true;
    }

    boolean isFwdThreeCharSmiley(String[] tokens, int i, int j, int k)
    {
        if (!validIndices(tokens, i, j, k)) {
            return false;
        }

        boolean top = isChar(tokens, i, regExTop);
        boolean middle = isChar(tokens, j, "\\-|o|O");
        boolean bottom = isChar(tokens, k, regExBottom);
        return top && middle && bottom;
    }

    private boolean isFwdTwoCharSmiley(String[] tokens, int i, int j)
    {
        if (!validIndices(tokens, i, j)) {
            return false;
        }

        boolean eyes = isChar(tokens, i, regExTop);
        boolean mouth = isChar(tokens, j, regExBottom);
        return eyes && mouth;
    }

    private boolean isWellKnownCamelCaseWord(String[] tokens, int i, int j)
    {
        if (!validIndices(tokens, i, j)) {
            return false;
        }

        for (String name : new String[] { "iPad", "iPod", "iMac", "iPhone", "iTunes", "YouTube",
                "eMail" }) {

            String name_sing = name;
            String name_plu = name + "s";

            String pair = new String(tokens[i] + tokens[j]);

            if (pair.equals(name_sing) || pair.equals(name_plu)) {
                return true;
            }
        }

        return false;
    }

    boolean isAlphaNum(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches("[a-zA-ZäÄöÖüÜ][0-9a-zA-ZäÄöÖüÜ]+", tokens[i]);
    }

    boolean isHash(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].equals("#");
    }

    private boolean isAlphaFollowedByDotThatIsKnownAbbreviation(String[] tokens, int i, int j)
        throws IOException
    {
        boolean isAlpha = isAlpha(tokens, i);
        boolean isDot = isDot(tokens, j);

        if (isAlpha && isDot) {
            return ListUtil.isAbbreviation(tokens[i] + tokens[j], abbreviations);
        }

        return false;
    }

    boolean isChar(String[] tokens, int i, String string)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].length() == 1 && Pattern.matches(string, tokens[i]);
    }

    boolean isSeq(String[] tokens, int i, String string)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches(string, tokens[i]);
    }

    boolean isSingleLetter(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].length() == 1 && Pattern.matches("[a-zA-ZäÄüÜöÖ]", tokens[i]);
    }

    boolean isOnlyCapitalLetter(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches("[A-Z]+", tokens[i]);
    }

    boolean isAlpha(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches("[a-zA-ZöÖäÄüÜß_]+", tokens[i]);
    }

    boolean isAtChar(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].equals("@");
    }

    boolean isSmileyElement(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches("[:;pP8xXdDoO\\(\\)\\-]", tokens[i]);
    }

    boolean isDot(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return tokens[i].matches("\\.+");
    }

    boolean isThreeDigitNumber(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches("[0-9]{1,3}", tokens[i]);
    }
    
    boolean isNumber(String[] tokens, int i)
    {
        if (!validIndices(tokens, i)) {
            return false;
        }

        return Pattern.matches("[0-9]+", tokens[i]);
    }


}
