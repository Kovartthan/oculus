package com.example.thavaneshj.uiforoculus;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {
    private static final String[] PERMISSIONS =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS};
    private static final int RC_PERMISSIONS = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestPermissions();
            }
        },1500);
    }


    @AfterPermissionGranted(RC_PERMISSIONS)
    public void requestPermissions() {
        if (hasLocationAndContactsPermissions()) {
            launchHomeScreen();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location_contacts),
                    RC_PERMISSIONS,
                    PERMISSIONS);
        }
    }


    private boolean hasLocationAndContactsPermissions() {
        return EasyPermissions.hasPermissions(this, PERMISSIONS);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.e("tag", "onPermissionsGranted:" + requestCode + ":" + perms.size());
        if(hasLocationAndContactsPermissions()){
            launchHomeScreen();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.e("tag", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }else if(!hasLocationAndContactsPermissions()){
           if(perms.size() == 1){
               EasyPermissions.requestPermissions(
                       this,
                       getString(R.string.rationale_location_contacts),
                       RC_PERMISSIONS,
                       perms.get(0));
           }else if(perms.size() > 1){
               String[] permissionArray = perms.toArray(new String[0]);
               EasyPermissions.requestPermissions(
                       this,
                       getString(R.string.rationale_location_contacts),
                       RC_PERMISSIONS,
                       permissionArray);
           }
        }
    }


    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.e("tag", "onRationaleAccepted");
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.e("tag", "onRationaleDenied");
        requestPermissions();
    }

    private void launchHomeScreen(){

        if(((OculusApp)getApplication()).getAppPreference().isUserLoggedIn()){
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
        }else{
            startActivity(new Intent(SplashActivity.this,SignInActivity.class));
        }
        finish();
    }
}