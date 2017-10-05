package util.json;

import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.support.DateUtils;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by guarionex on 9/27/16.
 */

public class OrderAttributeRealmListConverter implements JsonSerializer<RealmList<OrderAttributes>> {
    private Gson gson = new GsonBuilder()
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

    @Override
    public JsonElement serialize(RealmList<OrderAttributes> orderAttributes, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray ja = new JsonArray();
        Realm realm = Realm.getDefaultInstance();
        for (OrderAttributes attributes : orderAttributes) {
            if (attributes.isValid() && attributes.isManaged()) {
                ja.add(gson.toJsonTree(realm.copyFromRealm(attributes), OrderAttributes.class));
            } else {
                ja.add(gson.toJsonTree(attributes, OrderAttributes.class));
            }
        }
        realm.close();
        return ja;
    }
}
