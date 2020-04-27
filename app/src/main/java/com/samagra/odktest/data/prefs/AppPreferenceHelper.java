package com.samagra.odktest.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.samagra.odktest.data.models.InstitutionInfo;
import com.samagra.odktest.di.ApplicationContext;
import com.samagra.odktest.di.PreferenceInfo;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Solid implementation of the {@link PreferenceHelper}, performs the read/write operations on the {@link SharedPreferences}
 * used by the app module. The class is injected to all the activities instead of manually creating an object.
 *
 * @author Pranav Sharma
 */
@Singleton
public class AppPreferenceHelper implements PreferenceHelper {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences defaultPreferences;

    @Inject
    public AppPreferenceHelper(@ApplicationContext Context context, @PreferenceInfo String prefFileName) {
        this.sharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getCurrentUserName() {
        return defaultPreferences.getString("user.username", "");
    }

    @Override
    public String getUserRoleFromPref() {
        return defaultPreferences.getString("user.role", "");
//        String json = defaultPreferences.getString("user.data", "");
//
//        try {
//            Gson gson = new Gson();
//            HashMap gabc = gson.fromJson(json, HashMap.class);
//            String roleData =  gabc.get("roleData").toString();
////            HashMap gabc1 = gson.fromJson(roleData, HashMap.class);
////            String roleData1 = (String) gabc.get("designation");
//            return  gson.fromJson(gabc.get("roleData").toString(), HashMap.class).get("designation").toString();
//        } catch (Exception e) {
//            return "SHG";
//        }
        //        return new HashMap<String, String>((Map) new Gson().fromJson(sharedPreferences.getString("user.data", ""),
//                HashMap.class).get("roleData")).get("designation").toString();

    }

    @Override
    public void updateFormVersion(String version) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("formVersion", version);
        editor.apply();
    }

    @Override
    public String getUdiseListVersion() {
        return sharedPreferences.getString("udiseListVersion", "0");
    }

    @Override
    public String getFormVersion() {
        return sharedPreferences.getString("formVersion", "0");
    }


    @Override
    public void updateValues(String key, String value) {
        if(key.equals("user.fullName")){
            if(defaultPreferences.getString("user.fullName","").equals("")){
                SharedPreferences.Editor editor = defaultPreferences.edit();
                editor.putString(key, value);
                editor.apply();
            }
        }
        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putString(key, value);
        editor.apply();

    }

    @Override
    public void updateNewValues(String key, String value) {

        SharedPreferences.Editor editor = defaultPreferences.edit();
        editor.putString(key, value);
        editor.apply();

    }

    @Override
    public String getValueForKey(String content) {
        return defaultPreferences.getString(content, "");
    }

}
