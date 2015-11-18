package com.android.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.emobilepos.payment.ProcessCreditCard_FA;

import android.app.Activity;

public class CardParser {
	
	public static boolean parseCreditCard(Activity activity,String swiped_raw_data, CreditCardInfo tempCardData)
    {
        boolean isCardParsed = false;
        Pattern track1FormatBPattern = Pattern.compile("(%?([A-Z])([0-9]{1,19})\\^([^\\^]{2,26})\\^([0-9]{4}|\\^)([0-9]{3}|\\^)?([^\\?]+)?\\??)[\t\n\r ]{0,2}.*");
        Pattern track2Pattern = Pattern.compile(".*[\\t\\n\\r ]?(;([0-9]{1,19})=([0-9]{4})([0-9]{3})(.*)\\?).*");
        Pattern track3Pattern = Pattern.compile(".*?[\t\n\r ]{0,2}(\\+(.*)\\?)");
        //string pattern = "^(%{1}[A-Za-z0-9]+\^{1}[A-Za-z0-9/\-\s.]+\^[0-9]+\?{1})?;{1}[0-9]+={1}[0-9]+\?{1}$";
        String raw_card_data = swiped_raw_data.trim();
        Matcher matchTrack1 = track1FormatBPattern.matcher(raw_card_data);
        Matcher matchTrack2 = track2Pattern.matcher(raw_card_data);
        Matcher matchTrack3 = track3Pattern.matcher(raw_card_data);


        //CreditCardInfo tempCardData = new CreditCardInfo();

        if (matchTrack1.matches() || matchTrack2.matches())
        {
            tempCardData.setWasSwiped(true);
            Encrypt encrypt = new Encrypt(activity);
            
            if (matchTrack1.matches())
            {
                String rawTrack1 = getGroup(matchTrack1, 1);
                String card_num = getGroup(matchTrack1, 3);
                String card_owner_name = getGroup(matchTrack1, 4);
                String card_exp_date = getGroup(matchTrack1, 5);
                tempCardData.setEncryptedAESTrack1(encrypt.encryptWithAES(rawTrack1));
                tempCardData.setCardNumUnencrypted(card_num);
                tempCardData.setCardNumAESEncrypted(encrypt.encryptWithAES(card_num));
                tempCardData.setCardOwnerName(card_owner_name);
                tempCardData.setCardExpYear(card_exp_date.substring(0, 2));
                tempCardData.setCardExpMonth(card_exp_date.substring(2, 2));
                tempCardData.setCardLast4(card_num.substring(card_num.length() - 4));
                
                
                if (matchTrack2.matches())
                {
                    String rawTrack2 = getGroup(matchTrack2, 1);
                    tempCardData.setEncryptedAESTrack2(encrypt.encryptWithAES(rawTrack2));
                }
                
//                tempCardData.cardAESEncryptedTrack1 = EncryptionHelper.encrypt(rawTrack1);
//                tempCardData.cardNumUnencrypted = card_num;
//                tempCardData.cardNumAESEncrypted = EncryptionHelper.encrypt(card_num);
//                tempCardData.cardOwnerName = card_owner_name;
//                tempCardData.cardExpYear = card_exp_date.Substring(0, 2);
//                tempCardData.cardExpMonth = card_exp_date.Substring(2, 2);
//                tempCardData.cardLast4 = card_num.Substring(card_num.Length - 4, 4);
//                tempCardData.cardTypeName = cardType(card_num).ToString();
//                if (matchTrack2.matches())
//                {
//                    String rawTrack2 = getGroup(matchTrack2, 1);
//                    tempCardData.cardAESEncryptedTrack2 = EncryptionHelper.encrypt(rawTrack2);
//                }
            }
            else
            {
                String rawTrack2 = getGroup(matchTrack2, 1);
                String card_num = getGroup(matchTrack2, 2);
                
                tempCardData.setEncryptedAESTrack2(encrypt.encryptWithAES(rawTrack2));
                tempCardData.setCardNumUnencrypted(card_num);
                tempCardData.setCardNumAESEncrypted(encrypt.encryptWithAES(card_num));
                
//                tempCardData.cardAESEncryptedTrack2 = EncryptionHelper.encrypt(rawTrack2);
//                tempCardData.cardNumUnencrypted = card_num;
//                tempCardData.cardNumAESEncrypted = EncryptionHelper.encrypt(card_num);
            }
            isCardParsed = true;
            
        }      

        return isCardParsed;
    }
	
	public static boolean parseEncryptedCreditCard(Activity activity,CreditCardInfo cardManager, String track1, String track2, 
			String ksn, String name,String first6Num,String last4Num, String expDate,String maskedTrack1, String maskedTrack2)
    {
        boolean isCardParsed = false;
        
        
        Global.isEncryptSwipe = true;
        
        cardManager.setCardOwnerName(name);
		if(expDate!=null&&expDate.length()==4)
		{
			String year= expDate.substring(0, 2);
			String month = expDate.substring(2, 4);
			cardManager.setCardExpYear(year);
			cardManager.setCardExpMonth(month);
		}
		Encrypt encrypt = new Encrypt(activity);
		
		if(first6Num!=null&&!first6Num.isEmpty())
			cardManager.setCardType(ProcessCreditCard_FA.cardType(first6Num));
		cardManager.setCardLast4(last4Num);
		StringBuilder sb = new StringBuilder();
		sb.append(track1).append(track2);
		cardManager.setEncryptedBlock(sb.toString());
		cardManager.setEncryptedTrack1(track1);
		cardManager.setEncryptedTrack2(track2);
		cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(track2));
//		if(!Global.isEncryptSwipe)
//			cardManager.setCardNumUnencrypted(maskedNum);
		if(maskedTrack1!=null&&!maskedTrack1.isEmpty())
			cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(maskedTrack1));
		if(maskedTrack2!=null&&!maskedTrack2.isEmpty())
			cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(maskedTrack2));
		//cardManager.setDeviceSerialNumber(mMTSCRA.getDeviceSerial());
		//cardManager.setMagnePrint(mMTSCRA.getMagnePrint());
		//cardManager.setMagnePrintStatus(mMTSCRA.getMagnePrintStatus());
		cardManager.setTrackDataKSN(ksn);
        
        

        return isCardParsed;
    }
	
	private static String getGroup(Matcher matcher, int group)
    {
        int groupCount = matcher.groupCount();
        if (groupCount > group - 1)
        {
            return matcher.group(group);
        }
        else
        {
            return "";
        }
    }
}
