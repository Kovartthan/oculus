package com.example.thavaneshj.uiforoculus;


import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {

    private final SharedPreferences mPreference;
    private final SharedPreferences.Editor mEditor;
    private Context context;
    private final static String PREF_NAME = "rockstarfit_preference";

    private final static String PREF_IS_USER_LOGGED_IN ="pref_is_user_logged_in";



    public AppPreference(Context context) {
        this.context = context;
        mPreference =context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPreference.edit();
    }

    public void setIsUserLoggedIn(boolean value){
        mEditor.putBoolean(PREF_IS_USER_LOGGED_IN, value);
        mEditor.commit();
    }

    public boolean isUserLoggedIn(){
        return mPreference.getBoolean(PREF_IS_USER_LOGGED_IN, false);
    }


    public void logout() {
        mEditor.clear();
        mEditor.commit();
    }
}
