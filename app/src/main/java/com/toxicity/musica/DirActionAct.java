package com.toxicity.musica;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirActionAct extends AppCompatActivity implements View.OnClickListener {
    private final int PERMISSION_REQUEST_CODE = 1;
    private ListView SongListView;
    private List<String> songList; //the list of songs that is displayed by choosing a file with searchdir


    @Override
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        this.setContentView(R.layout.searchlayout);
        SongListView = findViewById(R.id.SongListView);
        permission_checker();
    }

    private void permission_checker()
    {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
            {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_AUDIO},PERMISSION_REQUEST_CODE);
                }
            }
        }
    @Override
    public void onClick(View view) {

    }


    //Finding Music Files..
    public class MusicFinder {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File dir = new File(downloadPath);

        songList = new ArrayList<>();



        if(dir.exists() && dir.isDirectory())
        {
            File[] files = dir.listFiles();
            if(files != null)
            {
                for(File file : files)
                {
                    if(file.isFile() && file.getName().endsWith(".mp3")
                            && file.getName().endsWith(".wav") && file.getName().endsWith(".ogg")
                            && file.getName().endsWith(".flac") && file.getName().endsWith(".aac")
                            && file.getName().endsWith(".ogg")) //we want to make sure that those are the specific extensions that we want to insert
                    {
                        songList.add(file.getName());
                    }
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        songListView.setAdapter(adapter);
    }
}
