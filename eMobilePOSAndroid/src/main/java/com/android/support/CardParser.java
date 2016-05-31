package com.android.support;

import android.app.Activity;

import com.android.emobilepos.payment.ProcessCreditCard_FA;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardParser {

    public static boolean parseCreditCard(Activity activity, String swiped_raw_data, CreditCardInfo tempCardData) {
        boolean isCardParsed = false;
        Pattern track1FormatBPattern = Pattern.compile("(%([A-Z])([0-9]{1,19})\\^([^\\^]{2,26})\\^([0-9]{4}|\\^)([0-9]{3}|\\^)([^\\?]+)\\?)");
        Pattern track2Pattern = Pattern.compile("(;([0-9]{4,19})=?([0-9]{2})?(0[1-9]|1[0-2])?[0-9]{0,50}\\?)");

        String raw_card_data = swiped_raw_data.trim();
        Matcher matchTrack1 = track1FormatBPattern.matcher(raw_card_data);
        Matcher matchTrack2 = track2Pattern.matcher(raw_card_data);

        boolean hasTrack1 = matchTrack1.find();
        boolean hasTrack2 = matchTrack2.find();

        if (hasTrack1 || hasTrack2) {
            tempCardData.setWasSwiped(true);
            Encrypt encrypt = new Encrypt(activity);

            if (hasTrack1) {
                String rawTrack1 = getGroup(matchTrack1, 1);
                String card_num = getGroup(matchTrack1, 3);
                String card_owner_name = getGroup(matchTrack1, 4);
                String card_exp_date = getGroup(matchTrack1, 5);
                tempCardData.setEncryptedAESTrack1(encrypt.encryptWithAES(rawTrack1));
                tempCardData.setCardNumUnencrypted(card_num);
                tempCardData.setCardNumAESEncrypted(encrypt.encryptWithAES(card_num));
                tempCardData.setCardOwnerName(card_owner_name);
                tempCardData.setCardExpYear(card_exp_date.substring(0, 2));
                tempCardData.setCardExpMonth(card_exp_date.substring(2, 4));
                tempCardData.setCardLast4(card_num.substring(card_num.length() - 4));
                tempCardData.setCardType(ProcessCreditCard_FA.getCardType(card_num));

                if (hasTrack2) {
                    String rawTrack2 = getGroup(matchTrack2, 1);
                    tempCardData.setEncryptedAESTrack2(encrypt.encryptWithAES(rawTrack2));
                }

            } else {
                String rawTrack2 = getGroup(matchTrack2, 1);
                String card_num = getGroup(matchTrack2, 2);

                tempCardData.setEncryptedAESTrack2(encrypt.encryptWithAES(rawTrack2));
                tempCardData.setCardNumUnencrypted(card_num);
                tempCardData.setCardNumAESEncrypted(encrypt.encryptWithAES(card_num));

                String year = getGroup(matchTrack2, 3);
                String month = getGroup(matchTrack2, 4);

                if (year == null) year = "";
                if (month == null) month = "";

                if (!year.isEmpty() && !month.isEmpty()) {
                    tempCardData.setCardExpYear(year);
                    tempCardData.setCardExpMonth(month);
                }

                if (rawTrack2.equalsIgnoreCase(";E?")) {
                    isCardParsed = false;
                    return isCardParsed;
                }
            }

            isCardParsed = true;

        }

        return isCardParsed;
    }

    public static boolean parseEncryptedCreditCard(Activity activity, CreditCardInfo cardManager, String track1, String track2,
                                                   String ksn, String name, String first6Num, String last4Num, String expDate, String maskedTrack1, String maskedTrack2) {
        boolean isCardParsed = false;


        Global.isEncryptSwipe = true;

        cardManager.setCardOwnerName(name);
        if (expDate != null && expDate.length() == 4) {
            String year = expDate.substring(0, 2);
            String month = expDate.substring(2, 4);
            cardManager.setCardExpYear(year);
            cardManager.setCardExpMonth(month);
        }
        Encrypt encrypt = new Encrypt(activity);

        if (first6Num != null && !first6Num.isEmpty())
            cardManager.setCardType(ProcessCreditCard_FA.getCardType(first6Num));
        cardManager.setCardLast4(last4Num);
        StringBuilder sb = new StringBuilder();
        sb.append(track1).append(track2);
        cardManager.setEncryptedBlock(sb.toString());
        cardManager.setEncryptedTrack1(track1);
        cardManager.setEncryptedTrack2(track2);
        cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(track2));

        if (maskedTrack1 != null && !maskedTrack1.isEmpty())
            cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(maskedTrack1));
        if (maskedTrack2 != null && !maskedTrack2.isEmpty())
            cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(maskedTrack2));

        cardManager.setTrackDataKSN(ksn);


        return isCardParsed;
    }

    private static String getGroup(Matcher matcher, int group) {
        int groupCount = matcher.groupCount();
        if (groupCount > group - 1) {
            return matcher.group(group);
        } else {
            return "";
        }
    }
}
