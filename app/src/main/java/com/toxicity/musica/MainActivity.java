package com.toxicity.musica;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnCloseListener {
    int currentSong;
    int progress;
    private ListView SongListView;
    private List<File> songList; //the list of songs that is displayed by choosing a file with searchbutton
    private ArrayList<String> SongNames; //displaying names
    private TextView songNameTextView;
    private SeekBar seekBar; //current min and sec of playing song
    private TextView progressTextView;
    boolean Connected;
    ImageButton searchbutton;
    Timer timer = null;
    TimerTask timertask;
    ImageButton playbutton;
    ArrayList<File> audiofiles = null;
    ImageButton forwardbutton;
    ImageButton rewindbutton;
    ImageButton themeButton;
    MusicaService musicaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        songList = new ArrayList<>();
        SongNames = new ArrayList<>();
        SongListView = findViewById(R.id.SongListView);

        songList = DirAction.MusicFinder.findMusicFiles();
        for(File i:songList) {
            SongNames.add(i.getName());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, SongNames);
        arrayAdapter.notifyDataSetChanged();
        SongListView.setAdapter(arrayAdapter);

        songNameTextView = findViewById(R.id.songNameTextView);
        seekBar = findViewById(R.id.seekBar);
        progressTextView = findViewById(R.id.progressTextView);

        searchbutton = findViewById(R.id.searchbutton);
        playbutton = findViewById(R.id.playbutton);
        forwardbutton = findViewById(R.id.forwardbutton);
        rewindbutton = findViewById(R.id.rewindbutton);
        themeButton = findViewById(R.id.themeButton);

        playbutton.setOnClickListener(this);
        forwardbutton.setOnClickListener(this);
        rewindbutton.setOnClickListener(this);
        seekBar.setOnClickListener(this);
        themeButton.setOnClickListener(this);
        Connected = false;

        if(SongNames.isEmpty()) {
            searchbutton.setEnabled(false);
            playbutton.setEnabled(false);
            rewindbutton.setEnabled(false);
            forwardbutton.setEnabled(false);
            themeButton.setEnabled(false);
            seekBar.setVisibility(View.GONE);
        }
        else
            DoStart();


        SongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MusicaService.CurrentSong = i;
                progressTextView.setText(SongNames.get(i));
            }
        });

        //registering broadcast receiver to listen for the finish of the song
        IntentFilter filter = new IntentFilter("com.example.SONG_COMPLETED");
        registerReceiver(completionReceiver, filter);
    }

    private void setupSeekBar(){
        seekBar.setMax(MusicaService.MP.getDuration()); //max progress
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b)
                    MusicaService.MP.seekTo(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Method to update the TextView with the current progress value in minutes and seconds
    private void UpdateProgressText(int progress) {
        int mins = progress / 60;
        int sec = progress % 60;
        progressTextView.setText(String.format("%02d:%02d", mins, sec)); //displaying mins and sec to display the progress of a song
        timertask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress((int) MusicaService.MP.getCurrentPosition());
                    }
                });
            }
        };
        if(timer == null)
            timer = new Timer();
        timer.schedule(timertask,0,1000);
    }

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
        if(view == playbutton && musicaService.Act) //first press for play music and second press for pause music
        {
            DoPause();
        } else if(view == playbutton)
        {
            progressTextView.setText(SongNames.get(MusicaService.CurrentSong));
            DoPlay();
        }

        if(view == forwardbutton)
        {
            DoNext();
            progressTextView.setText(SongNames.get(MusicaService.CurrentSong));
        }

        if(view == rewindbutton)
        {
            DoPrevious();
            progressTextView.setText(SongNames.get(MusicaService.CurrentSong));
        }
    }

    @Override
    public void onDestroy() { super.onDestroy(); DoStop();}

    public void onBackPressed()
    { super.onBackPressed(); finish();}

    void DoStart()
    {
        if(!Connected)
        {
            Intent musicaInt = new Intent(this, MusicaService.class);
            musicaInt.putExtra("Playing_song", currentSong);
            bindService(musicaInt, ServConnection, Context.BIND_AUTO_CREATE);
        }
    }

    void DoStop() {
        Connected = false;
        if (!SongNames.isEmpty()) {
            unbindService(ServConnection);
            musicaService.PauseSong();
        }
    }

    void DoPlay()
    {
        if(!Connected) {
            return;
        }
        musicaService.PlaySong();
        playbutton.setImageResource(R.drawable.ic_pause);
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
        playbutton.setImageResource(R.drawable.ic_play);
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
            setupSeekBar();
            UpdateProgressText(progress);
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