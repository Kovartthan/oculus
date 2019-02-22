/* Copyright 2016 Michael Sladoje and Mike Schälchli. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.thavaneshj.uiforoculus.facerecognistion.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper;
import ch.zhaw.facerecognitionlibrary.Helpers.MatName;
import ch.zhaw.facerecognitionlibrary.Helpers.PreferencesHelper;
import ch.zhaw.facerecognitionlibrary.PreProcessor.PreProcessorFactory;
import ch.zhaw.facerecognitionlibrary.Recognition.Recognition;
import ch.zhaw.facerecognitionlibrary.Recognition.RecognitionFactory;

public class TrainingActivity extends Activity {
    private static final String TAG = "Training";
    private ProgressDialog progressDialog;
    Thread thread;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setFinishOnTouchOutside(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Importing...");
        progressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        final Handler handler = new Handler(Looper.getMainLooper());
        thread = new Thread(new Runnable() {
            public void run() {
                if(!Thread.currentThread().isInterrupted()){
                    PreProcessorFactory ppF = new PreProcessorFactory(getApplicationContext());
                    PreferencesHelper preferencesHelper = new PreferencesHelper(getApplicationContext());
                    String algorithm = preferencesHelper.getClassificationMethod();

                    FileHelper fileHelper = new FileHelper();
                    fileHelper.createDataFolderIfNotExsiting();
                    final File[] persons = fileHelper.getTrainingList();
                    if (persons.length > 0) {
                        Recognition rec = RecognitionFactory.getRecognitionAlgorithm(getApplicationContext(), Recognition.TRAINING, algorithm);
                        for (File person : persons) {
                            if (person.isDirectory()){
                                File[] files = person.listFiles();
                                int counter = 1;
                                for (File file : files) {
                                    if (FileHelper.isFileAnImage(file)){
                                        Mat imgRgb = Imgcodecs.imread(file.getAbsolutePath());
                                        Imgproc.cvtColor(imgRgb, imgRgb, Imgproc.COLOR_BGRA2RGBA);
                                        Mat processedImage = new Mat();
                                        imgRgb.copyTo(processedImage);
                                        List<Mat> images = ppF.getProcessedImage(processedImage, PreProcessorFactory.PreprocessingMode.RECOGNITION);
                                        if (images == null || images.size() > 1) {
                                            // More than 1 face detected --> cannot use this file for training
                                            continue;
                                        } else {
                                            processedImage = images.get(0);
                                        }
                                        if (processedImage.empty()) {
                                            continue;
                                        }
                                        // The last token is the name --> Folder name = Person name
                                        String[] tokens = file.getParent().split("/");
                                        final String name = tokens[tokens.length - 1];

                                        MatName m = new MatName("processedImage", processedImage);
                                        fileHelper.saveMatToImage(m, FileHelper.DATA_PATH);

                                        rec.addImage(processedImage, name, false);

//                                      fileHelper.saveCroppedImage(imgRgb, ppF, file, name, counter);

                                        // Update screen to show the progress
                                        final int counterPost = counter;
                                        final int filesLength = files.length;
                                        counter++;
                                    }
                                }
                            }
                        }
                        boolean isTrainingSuccessfull = false;
                        if (rec.train()) {
                            isTrainingSuccessfull = true;
                        }
                        final boolean finalIsTrainingSuccessfull = isTrainingSuccessfull;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                if(finalIsTrainingSuccessfull){
                                    setResult(RESULT_OK);
                                }else{
                                    setResult(RESULT_CANCELED);
                                }
                                finish();
                            }
                        });
                    } else {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        thread.interrupt();
    }

    @Override
    protected void onStop() {
        super.onStop();
        thread.interrupt();
    }
}
