package com.toxicity.musica;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnCloseListener {
    private static final int PICK_AUDIO_FILE_REQUEST = 0;
    private ListView SongListView;
    private List<String> songList; //the list of songs that is displayed by choosing a file with searchbutton
    private TextView songNameTextView;
    private ProgressBar progressBar; //current min and sec of playing song
    boolean Connected;
    ImageButton searchbutton;
    ImageButton playbutton;
    ArrayList<File> audiofiles = null;
    ImageButton forwardbutton;
    ImageButton rewindbutton;
    SearchView  SearchButton;
    ImageButton currentsong;
    ImageButton themeButton;

    MusicaService musicaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songList = new ArrayList<>();

        SongListView = findViewById(R.id.SongListView);
        songNameTextView = findViewById(R.id.songNameTextView);
        progressBar = findViewById(R.id.progressBar);

        searchbutton = findViewById(R.id.searchbutton);
        playbutton = findViewById(R.id.playbutton);
        forwardbutton = findViewById(R.id.forwardbutton);
        rewindbutton = findViewById(R.id.rewindbutton);
//        SearchButton = (SearchView) findViewById(R.id.SearchButton);
        currentsong = findViewById(R.id.currentsong);
        themeButton = findViewById(R.id.themeButton);

        playbutton.setOnClickListener(this);
        forwardbutton.setOnClickListener(this);
        rewindbutton.setOnClickListener(this);
//        SearchButton.setOnCloseListener(this);
        progressBar.setOnClickListener(this);
        currentsong.setOnClickListener(this);
        themeButton.setOnClickListener(this);
        Connected = false;

        //registering broadcast receiver to listen for the finish of the song
        IntentFilter filter = new IntentFilter("com.example.SONG_COMPLETED");
        registerReceiver(completionReceiver, filter);
    }
    @Override
    public void onDestroy() { super.onDestroy(); }

    //updating song in the textviewbar
    private void updateSongName(String songName)
    {
        songNameTextView.setText(songName);
    }

    private void onCompletion(MediaPlayer mp){
        String songName = "Next Song";
        updateSongName(songName);

        Intent intent = new Intent("com.example.SONG_COMPLETED");
        intent.putExtra("songName", songName);
        sendBroadcast(intent);
    }
    @Override
    public void onClick(View view)
    {
        setContentView(R.layout.activity_main);

        if(view == playbutton && musicaService.Act) //first press for play music and second press for pause music
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
        musicaService.PauseSong();
    }

    void DoPlay()
    {
        if(!Connected) {
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        musicaService.PlaySong();
    }

    void DoPrevious()
    {
        if(!Connected)
            return;
        musicaService.PreviousSong();
    }

    void DoNext()
    {
        if(!Connected)
            return;
        musicaService.NextSong();
    }

    void DoPause()
    {
        if(!Connected)
            return;
        musicaService.PauseSong();
    }

    public void changeThemeOnClick(View view)
    {
        String[] themes = {"Dark", "Light", "Special"};
        int currentThemeIndex = Arrays.asList(themes).indexOf(getCurrentThemeName());

        String nextTheme = themes[(currentThemeIndex + 1) % themes.length];

        ThemeManager.changeTheme(this, nextTheme);
    }

    private String getCurrentThemeName(){
        int themeResId = getApplicationInfo().theme;
        if(themeResId == R.style.AppTheme_Dark)
            return "Dark";
        else if(themeResId == R.style.AppTheme_Light)
            return "Light";
        else
            return "Special";
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public boolean onClose() { return false; }

    private final ServiceConnection ServConnection = new ServiceConnection() {
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

    private final BroadcastReceiver completionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String songName = intent.getStringExtra("songName");
            updateSongName(songName);
        }
    };
}