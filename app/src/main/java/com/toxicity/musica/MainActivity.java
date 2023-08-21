package com.toxicity.musica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PICK_AUDIO_FILE_REQUEST = 2;
    private ListView SongsListView;
    private List<String> FilesList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SongsListView = findViewById(R.id.SongsListView);
        FilesList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

        ImageButton searchdir = findViewById(R.id.searchdir);
        ImageButton playbutton = findViewById(R.id.playbutton);
        ImageButton forwardbutton = findViewById(R.id.forwardbutton);
        ImageButton rewindbutton = findViewById(R.id.rewindbutton);

        searchdir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilePicker();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            fetchAudioFiles();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_AUDIO_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            String directoryPath = audioUri.getPath();
            FilesList.clear();
            FilesList.add(directoryPath);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, FilesList);
            SongsListView.setAdapter(adapter);
        }
    }

    private void fetchAudioFiles() {
        progressBar.setVisibility(View.VISIBLE);

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isAudioFile(file.getName())) {
                    FilesList.add(file.getName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, FilesList);
        SongsListView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);
    }

    private boolean isAudioFile(String fileName) {
        String[] audioExtensions = {".mp3", ".wav", ".aac", ".ogg", ".flac"};

        for (String extension : audioExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
