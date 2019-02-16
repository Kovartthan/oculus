package com.example.thavaneshj.uiforoculus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thavaneshj.uiforoculus.detection.DetectorActivity;

import net.gotev.speech.GoogleVoiceTypingDisabledException;
import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.SpeechUtil;
import net.gotev.speech.ui.SpeechProgressView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class MainActivity extends AppCompatActivity implements SpeechDelegate, TextToSpeech.OnInitListener {
    private static final int PERMISSIONS_REQUEST = 1051;
    private SpeechProgressView progress;
    private ImageView imgMic;
    private TextToSpeech tts;
    private TextView txtInfo;
    private boolean isEmergency;
    private LocationTracker tracker;
    private String text;
    private boolean isNavigate;
    private String navigationText;
    private String strInfo = "Say 'Object detection' or 'Read text' or 'Emergency' or 'Navigation'";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Speech.init(this, getPackageName());
        initializeViews();
        setupProgressView();
        setupListeners();
    }

    private void initializeViews() {
        progress = findViewById(R.id.progress);
        imgMic = findViewById(R.id.imgMic);
        txtInfo = findViewById(R.id.textView);
        tts = new TextToSpeech(this, this);
    }

    private void setupProgressView() {
        int[] colors = {
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.darker_gray),
                ContextCompat.getColor(this, android.R.color.black),
                ContextCompat.getColor(this, android.R.color.holo_orange_dark),
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
        };
        progress.setColors(colors);
    }

    private void setupListeners() {
        imgMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartToListen();
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                Log.e("tag","utteranceId "+utteranceId);
                if (!TextUtils.isEmpty(text) && text.equalsIgnoreCase(utteranceId)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onStartToListen();
                        }
                    });
                }else if(!TextUtils.isEmpty(navigationText) && navigationText.equalsIgnoreCase(utteranceId)){
                    Log.e("tag","navigationText entered");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onStartToListen();
                        }
                    });
                }else if(!TextUtils.isEmpty(errorText) &&errorText.equalsIgnoreCase(utteranceId)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setEnabled(true);
                        }
                    });
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (text.equalsIgnoreCase(utteranceId)) {
                    isEmergency = false;
                }
                if(navigationText.equalsIgnoreCase(utteranceId)){
                    isNavigate = false;
                }else
                if(errorText.equalsIgnoreCase(utteranceId)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtInfo.setEnabled(true);
                        }
                    });
                }
            }

            @Override
            public void onStart(String utteranceId) {
            }
        });
    }

    private void onStartToListen() {
        if (Speech.getInstance().isListening()) {
            Log.e("tag","stopListening");
            Speech.getInstance().stopListening();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Log.e("tag","onStartToListen");
                onRecordAudioPermissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecordAudioPermissionGranted();
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onRecordAudioPermissionGranted() {
        imgMic.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        txtInfo.setVisibility(View.GONE);
        try {
            Log.e("tag","onRecordAudioPermissionGranted");
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(progress, MainActivity.this);
            Log.e("tag","onRecordAudioPermissionGranted final");
        } catch (SpeechRecognitionNotAvailable exc) {
            showSpeechNotSupportedDialog();

        } catch (GoogleVoiceTypingDisabledException exc) {
            showEnableGoogleVoiceTyping();
        }
    }

    private void showSpeechNotSupportedDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        SpeechUtil.redirectUserToGoogleAppOnPlayStore(MainActivity.this);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.speech_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private void showEnableGoogleVoiceTyping() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_google_voice_typing)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }


    @Override
    public void onStartOfSpeech() {

    }

    @Override
    public void onSpeechRmsChanged(float value) {

    }

    @Override
    public void onSpeechPartialResults(List<String> results) {

    }

    @Override
    public void onSpeechResult(String result) {

        imgMic.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);

        if (result.isEmpty()) {

            txtInfo.setVisibility(View.VISIBLE);
            Speech.getInstance().say(getString(R.string.repeat));

        } else {
            if(isEmergency && result.contains("-")){
                result = result.replaceAll("-","");
            }

            if (result.contains("object detection") || result.contains("Object Detection")
                    || result.contains("Object detection")
                    || result.equalsIgnoreCase("object Detection")) {

                startActivity(new Intent(MainActivity.this, DetectorActivity.class));

            } else if (result.contains("read text") || result.contains("Read Text")
                    || result.contains("Read text")
                    || result.equalsIgnoreCase("read Text")) {

                startActivity(new Intent(MainActivity.this, OcrMainActivity.class));

            } else if (result.contains("emergency") || result.contains("Emergency") || result.contains("help") || result.contains("Help")) {

                invokeEmergencyMode();


            } else if (result.matches("[0-9]+") && result.length() == 10 && isEmergency) {

                isEmergency = false;
                startTrackLocation(result);

            } else if (result.contains("stop") || result.contains("Stop")) {
                stopTracking();
            } else if(result.contains("navigation") || result.contains("Navigation")){
                Log.e("tag","navigation entered");
                invokeNavigationMode();
            }else if(isNavigate){
                isNavigate = false;
                Log.e("tag","fetchAddressForNavigation entered");
                fetchAddressForNavigation(result);
            }else {
                vibrateDevice();
                if (isEmergency) {
                    isEmergency = false;
                }else if(isNavigate) {
                    isNavigate = false;
                }
                progress.setEnabled(false);
                speakOutError();
            }
            txtInfo.setVisibility(View.VISIBLE);
        }
    }

    String errorText = "Say clearly to detect";
    private void speakOutError() {
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, errorText);
        tts.speak(errorText, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    private void vibrateDevice() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }
    }

    private void invokeNavigationMode() {
        navigationText =  "Please tell location to navigate";
        isNavigate = true;
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, navigationText);
        tts.speak(navigationText, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    private void fetchAddressForNavigation(String result) {
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocationName(result, 1);
            if(addresses.size() > 0) {
                double latitude= addresses.get(0).getLatitude();
                double longitude= addresses.get(0).getLongitude();
                openGoogleMapForNavigation(latitude,longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"Cant open navigation",Toast.LENGTH_SHORT);
        }

    }

    private void openGoogleMapForNavigation(double latitude, double longitude) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+String.valueOf(latitude)+","+String.valueOf(longitude));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try{
            startActivity(mapIntent);
        }catch (Exception e){
            e.printStackTrace();
            showErrorMapAppNotFound(gmmIntentUri);
        }

    }

    private void showErrorMapAppNotFound(Uri gmmIntentUri) {
        Toast.makeText(this, "Please install a maps application", Toast.LENGTH_LONG).show();
    }

    private void invokeEmergencyMode() {
        text = "Say a phone number so your friend can help you";
        isEmergency = true;
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        stopTracking();
        Speech.getInstance().shutdown();
        super.onDestroy();
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut() {
        String text = txtInfo.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    boolean isLocationTracking = false;
    private void startTrackLocation(final String phoneNo) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        isLocationTracking = false;

        //TODO Change setMetersBetweenUpdates from one to desired meters
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(1000)
                        .setMetersBetweenUpdates(1000);


        tracker = new LocationTracker(this, settings) {

            @Override
            public void onLocationFound(Location location) {
                if(!isLocationTracking){
                    Toast.makeText(MainActivity.this,"Location tracking started ",Toast.LENGTH_SHORT).show();
                    isLocationTracking = true;
                }
                String message  = "Your friend needs your help here  his latitude "+location.getLatitude()+ " and longitude "+location.getLongitude();
                sendSMS(phoneNo,message,location.getLatitude(),location.getLongitude());
            }

            @Override
            public void onTimeout() {
                Toast.makeText(MainActivity.this,"Location fetch error still an help sms can be send ",Toast.LENGTH_SHORT).show();
                String message  = "Your friend needs your help here call him";
                sendSMS(phoneNo,message, 0, 0);
            }
        };

        tracker.startListening();
    }

    private void stopTracking() {
        if (tracker != null) {
            tracker.stopListening();
        }
    }


    public void sendSMS(String phoneNo, String msg, double latitude, double longitude) {
        if(latitude != 0 && longitude != 0) {
            Geocoder geocoder = new Geocoder(MainActivity.this);
            try {
                List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                if (addressList != null && addressList.size() > 0) {
                    String address = addressList.get(0).getAddressLine(0);

                    String province = addressList.get(0).getAdminArea();

                    String country = addressList.get(0).getCountryName();

                    String postalCode = addressList.get(0).getPostalCode();

                    String knownName = addressList.get(0).getFeatureName();

                    msg = msg + "\nAccurate Latitude: "+latitude +"\nAccurate Longitude: "+longitude +"\n\nStreet: " + address + "\n" + "City/Province: " + province + "\nCountry: " + country
                            + "\nPostal CODE: " + postalCode + "\n" + "Place Name: " + knownName;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(),"Sms cant send",
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!checkGPSOnOrOff()){
            startActivity(new Intent(MainActivity.this,LocationOnOffDialog.class));
        }
    }


    private boolean checkGPSOnOrOff(){
         LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this);
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        return providers != null && providers.contains(LocationManager.GPS_PROVIDER);
    }

}
