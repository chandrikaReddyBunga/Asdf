package com.gtsr.gtsr.testModule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gtsr.gtsr.R;


public class SampleReadyActivity extends AppCompatActivity {
Button btnStart;
RelativeLayout imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_ready);
        casting();
    }
    public void casting(){
        btnStart = findViewById(R.id.btn_start);
        imgBack = findViewById(R.id.back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SampleReadyActivity.this,AnalizingPageViewController.class));
            }
        });
    }
}