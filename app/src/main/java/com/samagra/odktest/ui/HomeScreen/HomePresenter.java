package com.samagra.odktest.ui.HomeScreen;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.View;

import com.samagra.odktest.MyApplication;
import com.samagra.odktest.base.BasePresenter;
import com.samagra.user_profile.profile.UserProfileElement;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * The Presenter class for Home Screen. This class controls interaction between the View and Data.
 * This class <b>must</b> implement the {@link HomeMvpPresenter} and <b>must</b> be a type of {@link BasePresenter}.
 *
 * @author Pranav Sharma
 */
public class HomePresenter<V extends HomeMvpView, I extends HomeMvpInteractor> extends BasePresenter<V, I> implements HomeMvpPresenter<V, I> {





    /**
     * The injected values is provided through {@link com.samagra.odktest.di.modules.ActivityAbstractProviders}
     */
    @Inject
    public HomePresenter(I mvpInteractor) {
        super(mvpInteractor);
    }


    @Override
    public void onMyVisitClicked(View v) {
        // launchActivity(MyVisitsActivity.class);
    }

    @Override
    public void onInspectSchoolClicked(View v) {

    }

    @Override
    public void onSubmitFormClicked(View v) {

    }




    @Override
    public void onUnSubmittedFormClicked(View v) {
    }

    @Override
    public void onViewIssuesClicked(View v) {

    }

    @Override
    public void onHelplineButtonClicked(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:9673464857"));
        v.getContext().startActivity(callIntent);
    }

    @Override
    public void setWelcomeText() {
        getMvpView().updateWelcomeText(getMvpInteractor().getUserName());
    }



    @Override
    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getMvpView()
                .getActivityContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }



    ArrayList<UserProfileElement> getProfileConfig() {
        String configString = MyApplication.getmFirebaseRemoteConfig().getString("profile_config");
        ArrayList<UserProfileElement> userProfileElements = new ArrayList<>();

        try {
            JSONArray config = new JSONArray(configString);
            for (int i = 0; i < config.length(); i++) {
                JSONArray spinnerExtra = config.getJSONObject(i).optJSONArray("spinnerExtra");
                ArrayList<String> spinnerValues = null;
                if (spinnerExtra != null) {
                    spinnerValues = new ArrayList<>();
                    for (int j = 0; j < spinnerExtra.length(); j++) {
                        spinnerValues.add(spinnerExtra.get(j).toString());
                    }
                }

                userProfileElements.add(new UserProfileElement(config.getJSONObject(i).get("base64Icon").toString(),
                        config.getJSONObject(i).get("title").toString(),
                        config.getJSONObject(i).get("content").toString(),
                        (Boolean) config.getJSONObject(i).get("isEditable"),
                        (int) config.getJSONObject(i).get("section"),
                        UserProfileElement.ProfileElementContentType.valueOf(config.getJSONObject(i).get("type").toString()),
                        spinnerValues,
                        getMvpInteractor().getPreferenceHelper().getValueForKey(config.getJSONObject(i).get("content").toString())
                        ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userProfileElements;
    }


    public void setInitialData() {
        getMvpInteractor().getPreferenceHelper().updateValues("user.fullName", "Test User");
        getMvpInteractor().getPreferenceHelper().updateValues("user.mobilePhone", "7837833100");
        getMvpInteractor().getPreferenceHelper().updateValues("user.email", "test@samagragovernance.in");
        getMvpInteractor().getPreferenceHelper().updateValues("user.username", "testUser");

    }
}