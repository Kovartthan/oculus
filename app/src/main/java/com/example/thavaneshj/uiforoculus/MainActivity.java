package com.example.thavaneshj.uiforoculus;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class MainActivity extends AppCompatActivity implements SpeechDelegate, TextToSpeech.OnInitListener,TextToSpeech.OnUtteranceCompletedListener {
    private static final int PERMISSIONS_REQUEST = 1051;
    private SpeechProgressView progress;
    private ImageView imgMic;
    private TextToSpeech tts;
    private TextView txtInfo;
    private boolean isEmergency;
    private LocationTracker tracker;
    private String text;
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
    }

    private void onStartToListen() {
        if (Speech.getInstance().isListening()) {
            Speech.getInstance().stopListening();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
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
            Speech.getInstance().stopTextToSpeech();
            Speech.getInstance().startListening(progress, MainActivity.this);

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
            Toast.makeText(this, "result "+result, Toast.LENGTH_SHORT).show();
            if (result.contains("object detection") || result.contains("Object Detection")
                    || result.contains("Object detection")
                    || result.equalsIgnoreCase("object Detection")) {

                startActivity(new Intent(MainActivity.this, DetectorActivity.class));
                finish();

            } else if (result.contains("read text") || result.contains("Read Text")
                    || result.contains("Read text")
                    || result.equalsIgnoreCase("read Text")) {


            } else if (result.contains("emergency") || result.contains("Emergency") || result.contains("help") || result.contains("Help")) {

                invokeEmergencyMode();


            } else if (result.matches("[0-9]+") && result.length() == 10 && isEmergency) {

                isEmergency = false;
                startTrackLocation(result);

            }else if(result.contains("stop emergency") || result.contains("Stop emergency")
                    || result.contains("stop Emergency") || result.contains("Stop Emergency")) {
                stopTracking();
            } else {
                if(isEmergency){
                    isEmergency = false;
                    Toast.makeText(this, "Say again emergency", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Say clearly to detect", Toast.LENGTH_SHORT).show();
                }
                txtInfo.setVisibility(View.VISIBLE);
            }
        }
    }

    private void invokeEmergencyMode() {
        text = "Say a phone number so your friend can help you";
        isEmergency = true;
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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



    private void startTrackLocation(final String phoneNo) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //TODO Change setMetersBetweenUpdates from one to desired meters
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(30 * 60 * 1000)
                        .setMetersBetweenUpdates(1);


        tracker = new LocationTracker(this, settings) {

            @Override
            public void onLocationFound(Location location) {
                Log.e("tag","lcoation tracked");
                String message  = "Your friend needs your help here  his latitude "+location.getLatitude()+ " and loingitude "+location.getLongitude();
                sendSMS(phoneNo,message);
            }

            @Override
            public void onTimeout() {
                Log.e("tag","lcoation timeout");
            }
        };

        tracker.startListening();
    }

    private void stopTracking(){
        if(tracker != null) {
            tracker.stopListening();
        }
    }


    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    @Override
    public void onUtteranceCompleted(String utteranceId) {
        if(text.equalsIgnoreCase(utteranceId)){
                onStartToListen();
        }
    }
}
