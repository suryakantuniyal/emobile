package util.json;

import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.support.DateUtils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.Annotation;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by guarionex on 06-28-16.
 */
public class JsonUtils {
    public static Gson getInstance() {
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
                .registerTypeAdapter(new TypeToken<RealmList<String>>() {
                        }.getType(),
                        new DeviceRealmListConverter())
//                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }
}