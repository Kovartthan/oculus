package com.example.thavaneshj.uiforoculus;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;

import java.util.List;

public class HelpActivity extends AppCompatActivity implements SpeechDelegate, TextToSpeech.OnInitListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Speech.init(this, getPackageName());
    }

    @Override
    public void onInit(int i) {

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

    }
}
