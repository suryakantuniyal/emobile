package plaintext;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class EMSPlainTextHelper {

    private final String empStr = "";

    public String centeredString(String theString, int theLineWidth) {

        if (theString == null)
            theString = empStr;
        int theStringLength = theString.length();
        StringBuilder sb = new StringBuilder();

        if (theStringLength < (theLineWidth - 2)) {
            try {
                sb.append(this.spaces((theLineWidth - theStringLength) / 2));
                sb.append(theString);
                sb.append(this.spaces((theLineWidth - theStringLength) / 2));
            } catch (Exception ex) {
                sb.append("\n");
            }
        } else {
            try {

                sb.append(theString.substring(0, theLineWidth - 2));
            } catch (Exception ex) {
                sb.append("\n");
            }
        }
        return sb.append("\n").toString();
    }


    public String formatLongString(String input, int maxCharInLine) {

        if (input == null)
            input = empStr;

        maxCharInLine = maxCharInLine - 2;
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            while (word.length() > maxCharInLine) {
                output.append(word.substring(0, maxCharInLine - lineLen) + "\n");
                word = word.substring(maxCharInLine - lineLen);
                lineLen = 0;
            }

            if (lineLen + word.length() > maxCharInLine) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word + " ");

            lineLen += word.length() + 1;
        }
        return output.toString();
    }

    public String[] formatLongStringArray(String input, int maxCharInLine) {

        if (input == null)
            input = empStr;

        maxCharInLine = maxCharInLine - 2;
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            while (word.length() > maxCharInLine) {
                try {
                    if (lineLen >= maxCharInLine) {
                        output.append(word);
                        lineLen = 0;
                    } else {
                        output.append(word.substring(0, maxCharInLine - lineLen) + "\n");
                        word = word.substring(maxCharInLine - lineLen);
                        lineLen = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    lineLen = 0;
                }
            }

            if (lineLen + word.length() > maxCharInLine) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word + " ");

            lineLen += word.length() + 1;
        }
        return output.toString().split("\n");
    }

    public String oneColumnLineWithLeftAlignedText(String columnText, int theLineWidth, int theIndentation) {

        if (columnText == null)
            columnText = empStr;

        int leftCharCount = columnText.length();

        int numSpaces = theLineWidth - leftCharCount;
        StringBuilder sb = new StringBuilder();

        if (numSpaces > 0) {
            sb.append(this.spaces(theIndentation));
            sb.append(columnText).append("\n");
        } else // line exceeds the theLineWidth
        {
            int maxCharCount = theLineWidth - theIndentation;

            int size = 0;
            String[] tempArray;
            tempArray = this.formatLongStringArray(columnText, maxCharCount);
            size = tempArray.length;
            for (int i = 0; i < size && i < 10; i++) {
                sb.append(this.spaces(theIndentation)).append(tempArray[i]).append("\n");
            }
        }

        return sb.toString();
    }

    public String twoColumnLineWithLeftAlignedText(String leftText, String rightText, int theLineWidth, int theIndentation) {
        int numSpaces = 0;

        if (leftText == null)
            leftText = empStr;
        if (rightText == null)
            rightText = empStr;

        if (leftText != null && rightText != null) {
            int leftCharCount = leftText.length();
            int rightCharCount = rightText.length();
            numSpaces = theLineWidth - leftCharCount - rightCharCount;
        }

        StringBuilder sb = new StringBuilder();

        if (numSpaces > 0) {
            sb.append(this.spaces(theIndentation));
            sb.append(leftText);
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(rightText).append("\n");
        } else {
            sb.append(this.spaces(theIndentation));
            sb.append(leftText).append("\n");
            sb.append(" ");
            sb.append(rightText).append("\n");
        }

        return sb.toString();
    }

    public String threeColumnLineWithLeftAlignedText(String first, String second, String third, int theLineWidth, int theIndentation) {
        int numSpaces = 0;

        if (first == null)
            first = empStr;
        if (second == null)
            second = empStr;
        if (third == null)
            third = empStr;

        int firstCharCount = first.length();
        int secondCharCount = second.length();
        int thirdCharCount = third.length();
        numSpaces = theLineWidth - (firstCharCount + secondCharCount + thirdCharCount);
        numSpaces = numSpaces / 3;

        StringBuilder sb = new StringBuilder();

        if (numSpaces > 0) {
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(first);
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(second);
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(third);

        } else {
            sb.append(this.spaces(theIndentation));
            sb.append(first).append("\n");
            sb.append(" ");
            sb.append(second).append("\n");
            sb.append(" ");
            sb.append(third).append("\n");
        }

        return sb.toString();
    }

    public String fourColumnLineWithLeftAlignedText(String first, String second, String third, String fourth, int theLineWidth, int theIndentation) {
        int numSpaces = 0;
        int allTextFieldsLength = 0;

        if (first == null)
            first = empStr;
        if (second == null)
            second = empStr;
        if (third == null)
            third = empStr;
        if (fourth == null)
            fourth = empStr;

        first = first.trim();
        second = second.trim();
        third = third.trim();
        fourth = fourth.trim();

        int firstCharCount = first.length();
        int secondCharCount = second.length();
        int thirdCharCount = third.length();
        int fourthCharCount = fourth.length();

        allTextFieldsLength = firstCharCount + secondCharCount + thirdCharCount + fourthCharCount;
        numSpaces = theLineWidth - allTextFieldsLength;
        numSpaces = numSpaces / 4;

        StringBuilder sb = new StringBuilder();

        if (numSpaces > 0) {
            sb.append("\n");
            sb.append(first);
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(second);
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(third);
            sb.append(this.spaces(numSpaces - theIndentation));
            sb.append(fourth);
            sb.append(this.spaces(numSpaces - theIndentation));
        } else {
            sb.append(this.spaces(theIndentation));
            sb.append(first).append("\n");
            sb.append(" ");
            sb.append(second).append("\n");
            sb.append(" ");
            sb.append(third).append("\n");
            sb.append(" ");
            sb.append(fourth).append("\n");
        }

        return sb.toString();
    }


    public String fourColumnLineWithLeftAlignedTextPercentWidth(String first, int firstWidthPercent, String second, int secondWidthPercent, String third, int thirdWidthPercent, String fourth, int fourthWidthPercent, int lineWidth, int theIndentation) {
        // the sum of all width percents must total 100%
        int numSpaces = 0;

        if (first == null)
            first = empStr;
        if (second == null)
            second = empStr;
        if (third == null)
            third = empStr;
        if (fourth == null)
            fourth = empStr;

        first = first.trim();
        second = second.trim();
        third = third.trim();
        fourth = fourth.trim();

        lineWidth = lineWidth - theIndentation; //count out indentation spaces
        int firstColumnFinalLength = (int) (lineWidth * ((double) firstWidthPercent / 100.0d));
        int secondColumnFinalLength = (int) (lineWidth * ((double) secondWidthPercent / 100.0d));
        int thirdColumnFinalLength = (int) (lineWidth * ((double) thirdWidthPercent / 100.0d));
        int fourthColumnFinalLength = (int) (lineWidth * ((double) fourthWidthPercent / 100.0d));

        int firstCharCount = first.length();
        int secondCharCount = second.length();
        int thirdCharCount = third.length();
        int fourthCharCount = fourth.length();
//        numSpaces = lineWidth - (firstCharCount + secondCharCount + thirdCharCount + fourthCharCount);
//        numSpaces = numSpaces/4;

        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(StringUtils.left(first, firstColumnFinalLength));
        sb.append(this.spaces(firstColumnFinalLength - firstCharCount));
        sb.append(StringUtils.left(second, secondColumnFinalLength));
        sb.append(this.spaces(secondColumnFinalLength - secondCharCount));
        sb.append(StringUtils.left(third, thirdColumnFinalLength));
        sb.append(this.spaces(thirdColumnFinalLength - thirdCharCount));
        sb.append(StringUtils.left(fourth, fourthColumnFinalLength));
        sb.append(this.spaces(fourthColumnFinalLength - fourthCharCount));

        return sb.toString();
    }


    public String fourColumnLineItem(String first, int firstWidthPercent, String second, int secondWidthPercent, String third, int thirdWidthPercent, String fourth, int fourthWidthPercent, int lineWidth, int theIndentation) {
        // the sum of all width percents must total 100%
        int numSpaces = 0;

        if (first == null)
            first = empStr;
        if (second == null)
            second = empStr;
        if (third == null)
            third = empStr;
        if (fourth == null)
            fourth = empStr;

        first = first.trim();
        second = second.trim();
        third = third.trim();
        fourth = fourth.trim();

        lineWidth = lineWidth - theIndentation; //count out indentation spaces
        int firstColumnFinalLength = (int) (lineWidth * ((double) firstWidthPercent / 100.0d));
        int secondColumnFinalLength = (int) (lineWidth * ((double) secondWidthPercent / 100.0d));
        int thirdColumnFinalLength = (int) (lineWidth * ((double) thirdWidthPercent / 100.0d));
        int fourthColumnFinalLength = (int) (lineWidth * ((double) fourthWidthPercent / 100.0d));

        int firstCharCount = first.length();
        int secondCharCount = second.length();
        int thirdCharCount = third.length();
        int fourthCharCount = fourth.length();
//        numSpaces = lineWidth - (firstCharCount + secondCharCount + thirdCharCount + fourthCharCount);
//        numSpaces = numSpaces/4;

        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(this.spaces(theIndentation));
        sb.append(StringUtils.left(first, firstColumnFinalLength));
        sb.append(this.spaces(firstColumnFinalLength - firstCharCount));
        sb.append(StringUtils.left(second, secondColumnFinalLength));
        sb.append(this.spaces(secondColumnFinalLength - secondCharCount));
        sb.append(this.spaces(thirdColumnFinalLength - thirdCharCount));
        sb.append(StringUtils.left(third, thirdColumnFinalLength));
        sb.append(this.spaces(fourthColumnFinalLength - fourthCharCount));
        sb.append(StringUtils.left(fourth, fourthColumnFinalLength));

        return sb.toString();
    }

    public String threeColumnLineItem(String first, int firstWidthPercent, String second, int secondWidthPercent, String third, int thirdWidthPercent, int lineWidth, int theIndentation) {
        // the sum of all width percents must total 100%

        if (first == null)
            first = empStr;
        if (second == null)
            second = empStr;
        if (third == null)
            third = empStr;

        first = first.trim();
        second = second.trim();
        third = third.trim();

        lineWidth = lineWidth - theIndentation; //count out indentation spaces
        int firstColumnFinalLength = (int) (lineWidth * ((double) firstWidthPercent / 100.0d));
        int secondColumnFinalLength = (int) (lineWidth * ((double) secondWidthPercent / 100.0d));
        int thirdColumnFinalLength = (int) (lineWidth * ((double) thirdWidthPercent / 100.0d));

        //adjust for rounding issues with integers, add spaces...
        int totalComputedColumnLengths = firstColumnFinalLength + secondColumnFinalLength + thirdColumnFinalLength;

        if (totalComputedColumnLengths < lineWidth) {
            int theDifference = lineWidth - totalComputedColumnLengths;
            //add spaces to the last column
            thirdColumnFinalLength += theDifference;
        }

        int firstCharCount = first.length();
        int secondCharCount = second.length();
        int thirdCharCount = third.length();

        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(this.spaces(theIndentation));
        sb.append(StringUtils.left(first, firstColumnFinalLength));
        sb.append(this.spaces(firstColumnFinalLength - firstCharCount));
        sb.append(this.spaces(secondColumnFinalLength - secondCharCount));
        sb.append(StringUtils.left(second, secondColumnFinalLength));
        sb.append(this.spaces(thirdColumnFinalLength - thirdCharCount));
        sb.append(StringUtils.left(third, thirdColumnFinalLength));

        return sb.toString();
    }


    public ArrayList<String> lineArrayFromString(String theString, int theLineWidth) {
        String stringCopy = theString;
        stringCopy = stringCopy.replace("\n", empStr);
        stringCopy = stringCopy.replace("\t", empStr);
        int stringCopyLength = stringCopy.length();
        int[] range = new int[2];
        ArrayList<String> lineArray = new ArrayList<String>();

        for (int i = 0; i < stringCopyLength; i += theLineWidth) {
            if (i < stringCopyLength) {
                if (i + theLineWidth > stringCopyLength) {
                    range[0] = i;
                    range[1] = stringCopyLength - i;
                }
                lineArray.add(stringCopy.substring(range[0], range[1]));
            }
        }
        return lineArray;
    }

    public String spaces(int numSpaces) {
        StringBuilder sb = new StringBuilder();
        if (numSpaces > 0) {
            for (int i = 0; i < numSpaces; i++) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public String newLines(int numNewLines) {
        StringBuilder sb = new StringBuilder();
        if (numNewLines > 0)
            for (int i = 0; i < numNewLines; i++)
                sb.append("\n");
        return sb.toString();
    }

    public String newDivider(char c, int lineWidth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lineWidth; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public String lines(int numLines) {
        StringBuilder sb = new StringBuilder();

        if (numLines > 0) {
            for (int i = 0; i < numLines; i++)
                sb.append("_");
        }

        return sb.toString();
    }

    public String ivuLines(int numLines) {
        StringBuilder sb = new StringBuilder();

        if (numLines > 0) {
            for (int i = 0; i < numLines; i++)
                sb.append("=");
        }

        //sb.append("\n");
        return sb.toString();
    }

    public String tabs(int numTabs) {
        StringBuilder sb = new StringBuilder();

        if (numTabs > 0) {
            for (int i = 0; i < numTabs; i++)
                sb.append("\t");
        }

        return sb.toString();
    }

    public String stars(int numLines) {
        StringBuilder sb = new StringBuilder();

        if (numLines > 0) {
            for (int i = 0; i < numLines; i++)
                sb.append("*");
        }

        return sb.toString();
    }

    public String deNullify(String aString) {
        if (aString == null)
            return empStr;

        return aString;
    }

    public String deNullifyCurrency(String aString) {
        if (aString == null)
            return empStr;

        return aString;
    }

}
