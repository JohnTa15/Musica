package com.toxicity.musica;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run(){
              Intent intent = new Intent(SplashScreen.this, MainActivity.class);
              startActivity(intent);
              askingPerms();
              finish();
          }
        },3000);
    }

    public void askingPerms() {
        String[] permissions1 = new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE};
        String[] permissions2 = new String[]{
                android.Manifest.permission.POST_NOTIFICATIONS, android.Manifest.permission.READ_MEDIA_AUDIO};

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { //for a12 and lower
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions1, 1);
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions2, 1);
            }
        }
    }
}

