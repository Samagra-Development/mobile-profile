package com.samagra.user_profile.contracts;

public class APIConfig {

    private static String fusionAuthAPIKey;

    public  APIConfig() {

    }
    public static void setAPIKey(String fusionAuthApiKey) {
        fusionAuthAPIKey = fusionAuthApiKey;
    }

    public static String getFusionAuthAPIKey() {
        return fusionAuthAPIKey;
    }
}
