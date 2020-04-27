package com.samagra.odktest.ui.HomeScreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.androidnetworking.AndroidNetworking;
import com.google.android.material.snackbar.Snackbar;
import com.samagra.commons.Constants;
import com.samagra.commons.CustomEvents;
import com.samagra.commons.ExchangeObject;
import com.samagra.commons.InternetMonitor;
import com.samagra.commons.MainApplication;
import com.samagra.commons.Modules;
import com.samagra.odktest.R;
import com.samagra.odktest.UtilityFunctions;
import com.samagra.odktest.base.BaseActivity;
import com.samagra.user_profile.contracts.ComponentManager;
import com.samagra.user_profile.contracts.IProfileContract;
import com.samagra.user_profile.profile.UserProfileElement;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * View part of the Home Screen. This class only handles the UI operations, all the business logic is simply
 * abstracted from this Activity. It <b>must</b> implement the {@link HomeMvpView} and extend the {@link BaseActivity}
 *
 * @author Pranav Sharma
 */
public class HomeActivity extends BaseActivity implements HomeMvpView, View.OnClickListener {

    @BindView(R.id.view_filled_forms)
    public LinearLayout fillFormLayout;

    @BindView(R.id.inspect_school)
    public LinearLayout viewSubmittedFormLayout;

    @BindView(R.id.submit_form)
    public LinearLayout submitFormsLayout;

    @BindView(R.id.need_help)
    public LinearLayout needHelpLayout;
    @BindView(R.id.circularProgressBar)
    public ProgressBar circularProgressBar;

    @BindView(R.id.parentHome)
    public LinearLayout parentHome;

    @BindView(R.id.welcome_text)
    public TextView welcomeText;

    private PopupMenu popupMenu;
    private Disposable logoutListener = null;
    private Snackbar progressSnackbar = null;
    private ProgressBar formProgressBar;

    private Unbinder unbinder;

    @Inject
    HomePresenter<HomeMvpView, HomeMvpInteractor> homePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getActivityComponent().inject(this);
        unbinder = ButterKnife.bind(this);
        homePresenter.onAttach(this);
        setupToolbar();
        setupListeners();
        checkIntent();
        formProgressBar = findViewById(R.id.form_progressBar);
        homePresenter.setWelcomeText();
        InternetMonitor.startMonitoringInternet();
        homePresenter.setInitialData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        customizeToolbar();
        homePresenter.setWelcomeText();
        renderLayoutVisible();
    }

    private void setupListeners() {
        fillFormLayout.setOnClickListener(this);
        viewSubmittedFormLayout.setOnClickListener(this);
        submitFormsLayout.setOnClickListener(this);
        needHelpLayout.setOnClickListener(this);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("ShowSnackbar", false)) {
            if (homePresenter.isNetworkConnected())
                showSnackbar(getString(R.string.on_internet_saving_complete), Snackbar.LENGTH_SHORT);
            else
                showSnackbar(getString(R.string.no_internet_saving_complete), Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void renderLayoutVisible() {
        formProgressBar.setVisibility(View.GONE);
        parentHome.setVisibility(View.VISIBLE);
        welcomeText.setVisibility(View.VISIBLE);
        circularProgressBar.setVisibility(View.GONE);
    }


    @Override
    public void changeProgressBar(int formProgress) {
        formProgressBar.setVisibility(View.VISIBLE);
        formProgressBar.setProgress(formProgress);
    }

    private  void renderLayoutInvisible() {
        welcomeText.setVisibility(View.GONE);
        formProgressBar.setVisibility(View.VISIBLE);
        formProgressBar.setProgress(0);
        parentHome.setVisibility(View.GONE);
        circularProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_filled_forms:
                homePresenter.onInspectSchoolClicked(v);
                break;
            case R.id.inspect_school:
                homePresenter.onSubmitFormClicked(v);
                break;

            case R.id.submit_form:
                homePresenter.onUnSubmittedFormClicked(v);
                break;
            case R.id.need_help:

                break;
        }
    }

    @SuppressLint("SetTextI18n" )
    @Override
    public void updateWelcomeText(String text) {
        welcomeText.setText("Welcome,\t" + text);

    }

    @Override
    public void showLoading(String message) {
        hideLoading();
        if (progressSnackbar == null) {
            progressSnackbar = UtilityFunctions.getSnackbarWithProgressIndicator(findViewById(android.R.id.content), getApplicationContext(), message);
        }
        progressSnackbar.setText(message);
        progressSnackbar.show();
    }

    @Override
    public void hideLoading() {
        if (progressSnackbar != null && progressSnackbar.isShownOrQueued())
            progressSnackbar.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logoutListener != null && !logoutListener.isDisposed()) {
            AndroidNetworking.cancel(Constants.LOGOUT_CALLS);
            logoutListener.dispose();
        }
        homePresenter.onDetach();
        unbinder.unbind();
    }

    /**
     * Only set the title and action bar here; do not make further modifications.
     * Any further modifications done to the toolbar here will be overwritten. If you wish to prevent modifications
     * from being overwritten, do them after onCreate is complete.
     * This method should be called in onCreate of your activity.
     */
    @Override
    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    public void customizeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getActivityContext().getResources().getColor(R.color.app_blue));
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(this::initAndShowPopupMenu);
    }

    /**
     * Giving Control of the UI to XML file for better customization and easier changes
     */
    private void initAndShowPopupMenu(View v) {

        if (popupMenu == null) {
            popupMenu = new PopupMenu(HomeActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.home_screen_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.about_us:
                        break;
                    case R.id.profile:
                        IProfileContract initializer = ComponentManager.iProfileContract;
                        ArrayList<UserProfileElement> profileElements = homePresenter.getProfileConfig();
                        if (initializer != null) {
                            initializer.launchProfileActivity(getActivityContext(), profileElements
                                    , getActivityContext().getResources().getString(R.string.fusionauth_api_key));
                        }

                        break;
                    case R.id.logout:

                        break;
                }
                return true;
            });
        }
        popupMenu.show();
    }

    @Override
    public void showFormsStillDownloading() {
        showSnackbar("Forms are downloading, please wait..", Snackbar.LENGTH_SHORT);
    }

}
