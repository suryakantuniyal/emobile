package com.enablercorp.oauthclient;

import android.content.Context;

import com.enablercorp.oauthclient.HttpClient;
import com.enablercorp.oauthclient.OAuthClient;
import com.google.gson.Gson;

import java.security.SecureRandom;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.annotations.RealmModule;

/**
 * Created by guarionex on 8/10/16.
 */
public class OAuthManager {
    private HttpClient httpClient = new HttpClient();
    private String urlOAuthParams = "grant_type=client_credentials&client_id=%s&client_secret=%s";
    private String requestTokenUrl = "https://emslogin.enablermobile.com/oauth/token";
    private Realm realm;

    public static OAuthManager getInstance(Context context, String clientId, String clientSecret) {
        return new OAuthManager(context, clientId, clientSecret);
    }

    public OAuthManager(Context context, String clientId, String clientSecret) {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        Realm.init(context);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("oauthclient")
                .deleteRealmIfMigrationNeeded()
                .modules(Realm.getDefaultModule(), new OAuthRealmModule())
//                .encryptionKey(key)
                .build();
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        OAuthClient authClient = realm.createObject(OAuthClient.class);
        authClient.setClient_id(clientId);
        authClient.setClient_secret(clientSecret);
        realm.commitTransaction();
        try {
            requestToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OAuthClient getOAuthClient(Context context) {
        Realm.init(context);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("oauthclient")
                .deleteRealmIfMigrationNeeded()
                .modules(Realm.getDefaultModule(), new OAuthRealmModule())
//                .encryptionKey(key)
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        OAuthClient authClient = realm.where(OAuthClient.class).findFirst();
        return realm.copyFromRealm(authClient);

    }

    public String requestToken() throws Exception {
        final String[] requestToken = new String[1];
        OAuthClient authClient = realm.where(OAuthClient.class).findFirst();
        final String oauthUrl = String.format(urlOAuthParams, authClient.getClient_id(), authClient.getClient_secret());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestToken[0] = httpClient.post(requestTokenUrl, oauthUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (requestToken) {
                    requestToken.notifyAll();
                }
            }
        }).start();
        synchronized (requestToken) {
            requestToken.wait();
        }
        realm.beginTransaction();
        Gson gson = new Gson();
        OAuthClient fromJson = gson.fromJson(requestToken[0], OAuthClient.class);
        authClient.setAccessToken(fromJson.getAccessToken());
        authClient.setExpiresIn(fromJson.getExpiresIn());
        authClient.setRefreshToken(fromJson.getRefreshToken());
        realm.commitTransaction();
        return requestToken[0];
    }

}


@RealmModule(classes = OAuthClient.class)
class OAuthRealmModule {

}