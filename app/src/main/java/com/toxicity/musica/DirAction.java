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
    private ListView SongListView;

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_main);
        SongListView = findViewById(R.id.SongListView);
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
