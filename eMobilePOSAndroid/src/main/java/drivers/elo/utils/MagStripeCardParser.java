package drivers.elo.utils;

public class MagStripeCardParser 
{
	private String strInput;
	private String strPAN;
	private String strExpMonth;
	private String strExpYear;
	boolean hasTrack1 = false;
	boolean hasTrack2 = false;

	public MagStripeCardParser(String strInput) throws Exception
	{
		this.strInput = strInput;
	}
	public boolean hasTrack1()
	{
		return hasTrack1;
	}
	public String getAccountNumber()
	{
		return strPAN;
	}
	public void setAccountNumber(String _strPAN) {
		this.strPAN = _strPAN;
	}
	public String getExpirationMonth()
	{
		return this.strExpMonth;
	}
	public void setExpirationMonth(String _strExpMonth)
	{
		this.strExpMonth = _strExpMonth;
	}
	public String getExpirationYear()
	{
		return strExpYear;
	}
	public void setExpirationYear(String _strExpYear)
	{
		this.strExpYear = _strExpYear;
	}
	public void clearData()
	{
		strInput = "";
		strPAN = "";
		strExpMonth = "";
		strExpYear = "";
	}

	public boolean isDataParse() throws Exception
	{
		boolean isParse = true;

		try
		{
			int iHasTrack1 = strInput.indexOf("^");
			int iHasTrack2 = strInput.indexOf("=");

			if(iHasTrack1>0)
			{
				hasTrack1 = true;
			}

			if(iHasTrack2>0)
			{
				hasTrack2 = true;
			}

			String strAcct = null;
			String strExpDate = null;
			if(hasTrack1)
			{
				int iFS2 = strInput.lastIndexOf("^");
				if( iFS2 == -1 )
					throw new Exception("missing 2nd field separator ^ in track 1 data");

				strAcct = strInput.substring(0,iHasTrack1);

				if( strAcct.charAt(0) == '%' )
				{
					if( ! Character.isDigit(strAcct.charAt(1)) )
					{
						strAcct = strAcct.substring(2);
					}
					else
					{
						strAcct = strAcct.substring(1);
					}
				}
				setAccountNumber(strAcct);
				strExpDate = strInput.substring(iFS2+1,iFS2+5);
				setExpirationYear("20" + strExpDate.substring(0,2));
				setExpirationMonth(strExpDate.substring(2,4));

			}

		}
		catch(ArrayIndexOutOfBoundsException ex)
		{
			isParse = false;
			throw new Exception(ex.getMessage());
		}
		catch(NumberFormatException ex)
		{
			isParse = false;
			throw new Exception(ex.getMessage());
		}
		catch(Exception ex)
		{
			isParse = false;
			throw new Exception(ex.getMessage());
		}
		return isParse;

	}
	public static String magSripeTrack(String track)
	{
		String magSripeTrack = track;
		if(track.charAt(0) == '%')
		{
			magSripeTrack = track.substring(1, track.length());
		}
		return magSripeTrack;
	}
	public static String magSripeTrackBase64(String track)
	{
		int intMagTrack = track.indexOf("%");
		String magSripeTrack = track;
		if(intMagTrack>0)
		{
			magSripeTrack = magSripeTrack.substring(1, track.length());
		}
		return magSripeTrack;
	}



}
