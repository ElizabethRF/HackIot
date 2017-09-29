package com.nc.elizabeth.hackiot;

import java.util.Locale;
import java.io.IOException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.w3c.dom.ls.LSParserFilter;



public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    String hablar;
    CameraSource cameraSource;
    TextToSpeech textToSpeech;
    int bandera;
    String hablarPasado = "";

    final int RequestCameraPermissionID = 1001;
    int result;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestCameraPermissionID:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }

                }
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();



        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");

        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024).setRequestedFps(2.0f)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }

                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0 ){
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i = 0; i<items.size();i++){
                                    TextBlock item =  items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                textView.setText(stringBuilder.toString());
                                hablar = stringBuilder.toString();
                                if(hablarPasado.equals(hablar)) {
                                    bandera++;
                                }else{
                                    hablarPasado = hablar;
                                    bandera = 0;
                                }



                                textToSpeech = new TextToSpeech(MainActivity.this,
                                        new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(final int status) {
                                                if (status == TextToSpeech.SUCCESS) {
                                                    if(bandera<1) {
                                                        TTS();
                                                        bandera++;
                                                    }


                                                    Log.d("TTS", "Text to speech engine started successfully.");
                                                    result = textToSpeech.setLanguage(Locale.US);
                                                } else {
                                                    Log.d("TTS", "Error starting the text to speech engine.");
                                                }
                                            }
                                        });
                            }

                        });
                    }
                }
            });
        }
    }

    public void TTStop(View view ){
            textToSpeech.stop();
           // onDestroy();
    }


    public void TTS(){
        if (result == TextToSpeech.LANG_MISSING_DATA|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
        } else {
            textToSpeech.speak(hablar,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();

        }

    }



}
