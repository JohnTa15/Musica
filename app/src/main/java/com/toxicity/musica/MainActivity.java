package com.toxicity.musica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.ArrayAdapter;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_READ_MEDIA_AUDIO = 1;
    private static final int PICK_FOLDER_REQUEST = 2;
    private ListView SongsListView;
    private List<String> FilesList; //the list of songs that is displayed by choosing a file with searchdir
    private ProgressBar progressBar; //current min and sec of playing song


    ImageButton searchdir;
    ImageButton playbutton;
    ImageButton forwardbutton;
    ImageButton rewindbutton;
    SearchView  SearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SongsListView = findViewById(R.id.SongsListView);
        FilesList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);

        searchdir = (ImageButton) findViewById(R.id.searchdir);
        playbutton = (ImageButton) findViewById(R.id.playbutton);
        forwardbutton = (ImageButton) findViewById(R.id.forwardbutton);
        rewindbutton = (ImageButton) findViewById(R.id.rewindbutton);
        SearchButton = (SearchView) findViewById(R.id.SearchButton);
//        SearchButton.setOnClickListener(this);
    }
        searchdir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openFolderPicker();
                String state = Environment.DIRECTORY_DOWNLOADS;
                if(Environment.MEDIA_MOUNTED.equals(state)) {
                    if(Build.VERSION.SDK_INT >= 23) {
                        if(checkPermission()) {
                            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                            if (dir.exists()) {
                                Log.d("path", dir.toString());
                                File list[] = dir.listFiles();
                                for (int i = 0; i < list.length; i++) {
                                    SongsListView.add(list[i].getName());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, FilesList);
                                SongsListView.setAdapter(adapter);
                            }
                        } else {
                            requestStoragePermissionAndOpenFolderPicker();
                        }
                        }
                    }
                }
        });

//        playbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//
//        forwardbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//        rewindbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

    private void requestStoragePermissionAndOpenFolderPicker() { //checking if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permissions not granted, request them
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_AUDIO
                    },
                    PERMISSION_REQUEST_CODE
            );
        } else {
            // Permissions already granted, open folder picker
            openFolderPicker();
        }
    }

        private void openFolderPicker() { //forcing user to choose only mp3 file
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(intent, PICK_FOLDER_REQUEST);
        }

    private boolean isAudioFile(String fileName) { //we want to make sure that those are the specific extensions that we want to insert
        String[] audioExtensions = {".mp3", ".wav", ".aac", ".ogg", ".flac"};

        for (String extension : audioExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FOLDER_REQUEST && resultCode == RESULT_OK) {
            Uri selectedFolderUri = data.getData();
            if (selectedFolderUri != null) {
                listAudioFilesFromFolder(selectedFolderUri);
            }
        }
    }


    private void listAudioFilesFromFolder(Uri folderUri) {
        FilesList.clear();

        String folderPath = folderUri.getPath();
        Log.d("MainActivity", "Selected Folder Path: " + folderPath);
        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isAudioFile(file.getName())) {
                        FilesList.add(file.getName());
                    }
                }
            }
        }
        updateListView();
    }

    private void updateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, FilesList);
        SongsListView.setAdapter(adapter);
    }
}