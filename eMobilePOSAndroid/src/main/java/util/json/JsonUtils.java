package util.json;

import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.support.DateUtils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Annotation;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by guarionex on 06-28-16.
 */
public class JsonUtils {
    private static volatile Gson mInstance;

    private static Gson buildNewInstance(){
        return new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).registerTypeAdapter(new TypeToken<RealmList<DinningTable>>() {
                        }.getType(),
                        new DinningTableRealmListConverter())
                .setDateFormat(DateUtils.DATE_yyyy_MM_ddTHH_mm_ss)
                .registerTypeAdapter(new TypeToken<RealmList<EmobileBiometric>>() {
                        }.getType(),
                        new EmobileBiometricsRealmListConverter())
//                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public static Gson getInstance() {
        if(mInstance == null){
            synchronized (JsonUtils.class){
                mInstance = buildNewInstance();
            }
        }
        return mInstance;/*
        return new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).registerTypeAdapter(new TypeToken<RealmList<DinningTable>>() {
                        }.getType(),
                        new DinningTableRealmListConverter())
                .setDateFormat(DateUtils.DATE_yyyy_MM_ddTHH_mm_ss)
                .registerTypeAdapter(new TypeToken<RealmList<EmobileBiometric>>() {
                        }.getType(),
                        new EmobileBiometricsRealmListConverter())
//                .excludeFieldsWithoutExposeAnnotation()
                .create();*/
    }

    public String readJSONfileFromPath(String path) {
        File file = new File(path);

        FileReader fileReader;
        BufferedReader bufferedReader;
        StringBuffer output = new StringBuffer();
        String line = "";
        try {
            fileReader = new FileReader(file.getAbsolutePath());
            bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                output.append(line+"\n");
            }
            bufferedReader.close();
            return output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveJSONfileInPath(String jsonData, String path) {
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(jsonData);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}