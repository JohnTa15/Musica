package com.toxicity.musica;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnCloseListener {
    private static final int PICK_AUDIO_FILE_REQUEST = 0;
    private ListView SongListView;
    private List<String> songList; //the list of songs that is displayed by choosing a file with searchdir
    private ProgressBar progressBar; //current min and sec of playing song
    boolean Connected;
    ImageButton searchdir;
    ImageButton playbutton;
    ArrayList<File> audiofiles = null;
    ImageButton forwardbutton;
    ImageButton rewindbutton;
    SearchView  SearchButton;
    ImageButton currentsong;
    int currentSongID;

    MusicaService musicaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songList = new ArrayList<>();

        SongListView = findViewById(R.id.SongListView);
        progressBar = findViewById(R.id.progressBar);

        searchdir = (ImageButton) findViewById(R.id.searchdir);
        playbutton = (ImageButton) findViewById(R.id.playbutton);
        forwardbutton = (ImageButton) findViewById(R.id.forwardbutton);
        rewindbutton = (ImageButton) findViewById(R.id.rewindbutton);
        SearchButton = (SearchView) findViewById(R.id.SearchButton);
        currentsong = (ImageButton) findViewById(R.id.currentsong);

        playbutton.setOnClickListener(this);
        forwardbutton.setOnClickListener(this);
        rewindbutton.setOnClickListener(this);
        SearchButton.setOnCloseListener(this);
        progressBar.setOnClickListener(this);
        currentsong.setOnClickListener(this);
        Connected = false;


    }

    @Override
    public void onDestroy() { super.onDestroy(); }

    @Override
    public void onClick(View view)
    {
        if(view == playbutton && MusicaService.active) //first press for play music and second press for pause music
        {
            DoPause();
        } else if(view == playbutton)
        {
            DoPlay();
        }

        if(view == forwardbutton)
        {
            DoNext();
        }

        if(view == rewindbutton)
        {
            DoPrevious();
        }
    }

    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    void DoStart()
    {
        if(!Connected)
        {
            Intent musicaInt = new Intent(this, MusicaService.class);
            bindService(musicaInt, ServConnection, Context.BIND_AUTO_CREATE);
        }
    }

    void DoStop()
    {
        progressBar.setVisibility(View.GONE);
        Connected = false;
        unbindService(ServConnection);
    }

    void DoPlay()
    {
        if(!Connected) {
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        MusicaService.PlaySong();
    }

    void DoPrevious()
    {
        if(!Connected)
            return;
        MusicaService.PreviousSong();
    }

    void DoNext()
    {
        if(!Connected)
            return;
        MusicaService.NextSong();
    }

    void DoPause()
    {
        if(!Connected)
            return;
        MusicaService.PauseSong();
    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public boolean onClose() { return false; }

    private ServiceConnection ServConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            MusicaService.LocalBinder binder = (MusicaService.LocalBinder) service;
            musicaService = binder.getService();
            Connected = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName CompNam) { Connected = false;}
    };
}