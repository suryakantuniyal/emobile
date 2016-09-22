package util;

import com.android.support.DateUtils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;

/**
 * Created by guarionex on 06-28-16.
 */
public class JsonUtils {
    public static Gson getInstance() {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).setDateFormat(DateUtils.DATE_yyyy_MM_ddTHH_mm_ss)
                .create();
        return gson;
    }
}
