package com.example.thavaneshj.uiforoculus;

import android.app.Application;

/**
 * Created by admin on 2/12/2019.
 */

public class OculusApp extends Application {
    private AppPreference  appPreference;
    @Override
    public void onCreate() {
        super.onCreate();
        appPreference = new AppPreference(this);
    }

    public AppPreference getAppPreference() {
        return appPreference;
    }
}
