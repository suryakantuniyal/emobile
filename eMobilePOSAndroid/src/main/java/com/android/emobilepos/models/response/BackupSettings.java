package com.android.emobilepos.models.response;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

public class BackupSettings {

    Gson gson = new Gson();

    public void restoreMySettings(JsonArray mSettings) {
//        preferences.setPreferences("pref_fast_scanning_mode",false);
//        for (JsonElement element : mSettings) {
//            if(element.isJsonObject()){
//                JsonObject object = element.getAsJsonObject();
//                if(object.has("generalSettings")){
//                    Log.e("TEST","HELLO");
//                }
//            }
//        }
        for (JsonElement element : mSettings){
            JsonObject object = element.getAsJsonObject();
//            Type listType = new com.google.gson.reflect.TypeToken<BuildSettings>(){}.getType();
            BuildSettings settings = gson.fromJson(mSettings,BuildSettings.class);
        }
//                settings.getGeneralSettings();
    }

    public void backupMySettings(List<BuildSettings> mSettings) {

    }

    private void ConfigSettings(){

    }
}
