package com.android.emobilepos.service;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.landicorp.uns.result;

import java.util.List;

public class CloseBatchPaxHistoryPreference extends ListPreference {


    public CloseBatchPaxHistoryPreference (Context context) {
        this(context, null);
    }
    public CloseBatchPaxHistoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        try{
            CloseBatchPaxServiceHelper helper = new CloseBatchPaxServiceHelper();
            List<CloseBatchPaxResult> results = helper.getResults(context.getContentResolver(), "DESC");
            setEntries(getResults(results));
            setEntryValues(getResults(results));
//            setValueIndex(0);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public CharSequence[] getValues(List<CloseBatchPaxResult> results){
        CharSequence[] charSequences = null;
        CloseBatchPaxResult result = null;
        try{
            charSequences = new CharSequence[results.size()];
            Object[] arr =  results.toArray();
            for(int i = 0; i < results.size(); i++){
                result = ((CloseBatchPaxResult)arr[i]);
                charSequences[i] = "" + result.getId();
            }
        }catch (Exception x){
            x.printStackTrace();
        }
        return charSequences;
    }
    public CharSequence[] getResults(List<CloseBatchPaxResult> results){
        CharSequence[] charSequences = null;
        CloseBatchPaxResult result = null;
        try{
            charSequences = new CharSequence[results.size()];
            Object[] arr =  results.toArray();
            for(int i = 0; i < results.size(); i++){
                result = ((CloseBatchPaxResult)arr[i]);
                charSequences[i] = result.getResult() + " | "+ result.getResultDate();
            }
        }catch (Exception x){
            x.printStackTrace();
        }
        return charSequences;
    }
}
