package com.example.pocble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
            Intent serviceIntent = new Intent(this, BeaconService.class);
            startForegroundService(serviceIntent);
        }
    }
}