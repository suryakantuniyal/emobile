package com.android.emobilepos.models.ingenico;

/**
 * Created by Luis Camayd on 1/3/2019.
 */
public class CredentialsResponse {

    private String ApiKey;
    private String Url;
    private String Username;
    private String Password;

    public CredentialsResponse() {
    }


    public String getApiKey() {
        return ApiKey;
    }

    public void setApiKey(String apiKey) {
        ApiKey = apiKey;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
