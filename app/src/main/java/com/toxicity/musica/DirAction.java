package com.toxicity.musica;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class DirAction extends AppCompatActivity implements View.OnClickListener {
    private final int PERMISSION_REQUEST_CODE = 1;
    private ListView SongListView;

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_main);
        SongListView = findViewById(R.id.SongListView);
        permission_checker();
        askPerm();
    }

    private void askPerm() {
        boolean perms = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { // if android 12 and lower
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0); //This calls onRequestPermissionsResult()
                perms = true;
            }
        } else if (!perms && Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU){ // if android 13+
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 0); //This calls onRequestPermissionsResult()
            }
        } else if (perms)
            Toast.makeText(DirAction.this, "Already granted..", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(DirAction.this, "Permissions are denied..", Toast.LENGTH_SHORT).show();
    }

    private void permission_checker() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {}

    public static class MusicFinder {

        //Finding Music Files..
        public static ArrayList<File> findMusicFiles() {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            ArrayList<File> songList = new ArrayList<>();
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && (file.getName().endsWith(".mp3")
                                || file.getName().endsWith(".wav")
                                || file.getName().endsWith(".ogg")
                                || file.getName().endsWith(".flac")
                                || file.getName().endsWith(".aac")
                                || file.getName().endsWith(".ogg")
                                || file.getName().endsWith("opus"))) {
                            songList.add(file);
                        }
                    }
                }
            }
            return songList;
        }
    }
}
