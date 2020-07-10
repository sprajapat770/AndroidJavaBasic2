package com.example.androidjavabasic2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.androidjavabasic2.CameraXVideoRecord.CameraXVideoActivity;
import com.example.androidjavabasic2.MotionLayoutTransition.MotionActivity;

public class MainActivity extends AppCompatActivity {
    Button btnCXVideo,btnMTransition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        init();
    }

    private void findViewById() {
        btnCXVideo = findViewById(R.id.btnCXVideo);
        btnMTransition = findViewById(R.id.btnMTransition);

    }

    private void init() {
        btnCXVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraXVideoActivity.class);
                startActivity(intent);
            }
        });
        btnMTransition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MotionActivity.class);
                startActivity(intent);
            }
        });

    }
}