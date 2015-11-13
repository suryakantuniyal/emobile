package plaintext;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class EMSPlainTextHelper {

	private final String empStr = "";

	public String centeredString(String theString, int theLineWidth) {

		if(theString==null)
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

	
	public String formatLongString(String input, int maxCharInLine){

		if(input==null)
			input = empStr;
		
		maxCharInLine = maxCharInLine-2;
	    StringTokenizer tok = new StringTokenizer(input, " ");
	    StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;
	    while (tok.hasMoreTokens()) {
	        String word = tok.nextToken();

	        while(word.length() > maxCharInLine){
	            output.append(word.substring(0, maxCharInLine-lineLen) + "\n");
	            word = word.substring(maxCharInLine-lineLen);
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
	
	public String[] formatLongStringArray(String input, int maxCharInLine){

		if(input == null)
			input = empStr;
		
		maxCharInLine = maxCharInLine-2;
	    StringTokenizer tok = new StringTokenizer(input, " ");
	    StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;
	    while (tok.hasMoreTokens()) {
	        String word = tok.nextToken();

	        while(word.length() > maxCharInLine){
	            output.append(word.substring(0, maxCharInLine-lineLen) + "\n");
	            word = word.substring(maxCharInLine-lineLen);
	            lineLen = 0;
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
		
		if(columnText==null)
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
			String [] tempArray;
			tempArray = this.formatLongStringArray(columnText, maxCharCount);
			size = tempArray.length;
			for(int i = 0 ; i < size && i < 10; i++)
			{
				sb.append(this.spaces(theIndentation)).append(tempArray[i]).append("\n");
			}
		}

		return sb.toString();
	}

	public String twoColumnLineWithLeftAlignedText(String leftText, String rightText, int theLineWidth, int theIndentation) {
		int numSpaces = 0;
		
		if(leftText == null)
			leftText = empStr;
		if(rightText == null)
			rightText = empStr;
		
		if(leftText!=null&&rightText!=null)
		{
			int leftCharCount = leftText.length();
			int rightCharCount = rightText.length();
			numSpaces = theLineWidth - leftCharCount - rightCharCount;
		}
		
		StringBuilder sb = new StringBuilder();

		if (numSpaces > 0) {
			sb.append(this.spaces(theIndentation));
			sb.append(leftText);
			sb.append(this.spaces(numSpaces - theIndentation));
			sb.append(rightText);
		} else {
			sb.append(this.spaces(theIndentation));
			sb.append(leftText).append("\n");
			sb.append(" ");
			sb.append(rightText).append("\n");
		}

		return sb.toString();
	}
	
	
	public String fourColumnLineWithLeftAlignedText(String first, String second,String third,String fourth, int theLineWidth, int theIndentation) {
		int numSpaces = 0;
		
		if(first == null)
			first = empStr;
		if(second == null)
			second = empStr;
		if(third == null)
			third = empStr;
		if(fourth == null)
			fourth = empStr;
		

			int firstCharCount = first.length();
			int secondCharCount = second.length();
			int thirdCharCount = third.length();
			int fourthCharCount = fourth.length();
			numSpaces = theLineWidth - (firstCharCount + secondCharCount+thirdCharCount+fourthCharCount);
			numSpaces = numSpaces/4;
		
		StringBuilder sb = new StringBuilder();

		if (numSpaces > 0) {
			sb.append(this.spaces(numSpaces - theIndentation));
			sb.append(first);
			sb.append(this.spaces(numSpaces - theIndentation));
			sb.append(second);
			sb.append(this.spaces(numSpaces - theIndentation));
			sb.append(third);
			sb.append(this.spaces(numSpaces - theIndentation));
			sb.append(fourth);
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

	public String newLines(int numNewLines)
	{
		StringBuilder sb = new StringBuilder();
		if(numNewLines>0)
			for(int i = 0 ; i < numNewLines; i++)
				sb.append("\n");
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
