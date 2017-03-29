package util.json;

import com.android.emobilepos.models.realms.DinningTable;
import com.android.support.DateUtils;
import com.google.common.primitives.Floats;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

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
                .registerTypeAdapter(Double.class, new TypeAdapter<Double>() {
                    @Override
                    public Double read(JsonReader reader) throws IOException {
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                            return null;
                        }
                        String stringValue = reader.nextString();
                        try {
                            Double value = Double.valueOf(stringValue);
                            return value;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }

                    @Override
                    public void write(JsonWriter writer, Double value) throws IOException {
                        if (value == null) {
                            writer.nullValue();
                            return;
                        }
                        writer.value(value);
                    }
                })
                .registerTypeAdapter(Float.class, new TypeAdapter<Float>() {
                    @Override
                    public Float read(JsonReader reader) throws IOException {
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                            return null;
                        }
                        String stringValue = reader.nextString();
                        try {
                            Float value = Float.valueOf(stringValue);
                            return value;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }

                    @Override
                    public void write(JsonWriter writer, Float value) throws IOException {
                        if (value == null) {
                            writer.nullValue();
                            return;
                        }
                        writer.value(value);
                    }
                })
                .registerTypeAdapter(Integer.class, new TypeAdapter<Integer>() {
                    @Override
                    public Integer read(JsonReader reader) throws IOException {
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                            return null;
                        }
                        String stringValue = reader.nextString();
                        try {
                            Integer value = Integer.valueOf(stringValue);
                            return value;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }

                    @Override
                    public void write(JsonWriter writer, Integer value) throws IOException {
                        if (value == null) {
                            writer.nullValue();
                            return;
                        }
                        writer.value(value);
                    }
                })
                .create();
    }
}
