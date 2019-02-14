//package com.example.thavaneshj.uiforoculus;
//
//import android.content.pm.PackageManager;
//import android.speech.tts.TextToSpeech;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.SparseArray;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.google.android.gms.vision.CameraSource;
//import com.google.android.gms.vision.Detector;
//import com.google.android.gms.vision.text.TextBlock;
//import com.google.android.gms.vision.text.TextRecognizer;
//
//import java.io.IOException;
//import java.util.Locale;
//
//public class OcrMainActivity extends AppCompatActivity {
//    TextToSpeech textToSpeech;
//    Button button;
//    SurfaceView surfaceView;
//    EditText editText;
//    CameraSource cameraSource;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//
//        button = (Button) findViewById(R.id.button);
//        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
//        editText = (EditText) findViewById(R.id.editText);
//
//
//        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
//        if (!textRecognizer.isOperational()) {
//
//        } else {
//            final CameraSource cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
//                    .setFacing(CameraSource.CAMERA_FACING_BACK)
//                    .setAutoFocusEnabled(true)
//                    .build();
//            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//                @Override
//                public void surfaceCreated(SurfaceHolder holder) {
//                    try {
//                        if (ActivityCompat.checkSelfPermission(OcrMainActivity.this,
//                                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                            return;
//                        }
//                        cameraSource.start(surfaceView.getHolder());
//                    }catch (IOException e)
//                    {
//                        e.printStackTrace();
//                    }
//
//                }
//
//                @Override
//                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//                }
//
//                @Override
//                public void surfaceDestroyed(SurfaceHolder holder) {
//                    cameraSource.stop();
//
//                }
//            });
//        }
//        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
//            @Override
//            public void release() {
//
//            }
//
//            @Override
//            public void receiveDetections(Detector.Detections<TextBlock> detections) {
//                final SparseArray<TextBlock> items= detections.getDetectedItems();
//                if(items.size()!=0){
//                    editText.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            StringBuilder stringBuilder=new StringBuilder();
//                            for (int i=0;i<items.size();++i)
//                            {
//                                TextBlock item=items.valueAt(i);
//                                stringBuilder.append(item.getValue());
//                                stringBuilder.append("\n");
//                            }
//                            editText.setText(stringBuilder.toString());
//                        }
//                    });
//                }
//
//            }
//        });
//        textToSpeech =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if(status!=TextToSpeech.ERROR)
//                {
//                    textToSpeech.setLanguage(Locale.getDefault());
//                }
//            }
//        });
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String toSpeak=editText.getText().toString();
//                Toast.makeText(getApplicationContext(),toSpeak,Toast.LENGTH_SHORT).show();
//                textToSpeech.speak(toSpeak,TextToSpeech.QUEUE_FLUSH,null);
//            }
//        });
//    }
//    public void onPause()
//    {
//        if(textToSpeech!=null)
//        {
//            textToSpeech.stop();
//            textToSpeech.shutdown();
//        }
//        super.onPause();
//    }
//}
