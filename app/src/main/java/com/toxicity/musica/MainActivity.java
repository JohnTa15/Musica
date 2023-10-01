package com.toxicity.musica;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import com.toxicity.musica.DirAction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Switch;
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
    private final int request_code = 1;
    private List<File> songList; //the list of songs that is displayed by choosing a file with searchbutton
    public static ArrayList<String> SongNames; //displaying names
    private TextView songNameTextView;
    private static TextView DurationSong;
    private static SeekBar seekBar; //current min and sec of playing song
    private TextView progressTextView;
    boolean Connected;
    private TextView CurrentSec;
    ImageButton searchbutton;
    Timer timer = null;
    TimerTask timertask;
    private DirAction dirAction = new DirAction();
    ImageButton playbutton;
    ImageButton forwardbutton;
    ImageButton rewindbutton;
    Switch switchtheme;
    static MusicaService musicaService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        songList = new ArrayList<>();
        SongNames = new ArrayList<>();
        SongListView = findViewById(R.id.SongListView);

        songList = DirAction.MusicFinder.findMusicFiles();
        for (File i : songList) {
            String filename = i.getName(); //audio file names
            int lastDotIndex = filename.lastIndexOf("."); //in case it has extensions..

//            SongNames.add(i.getName());
            if (lastDotIndex > 0) {
                String SongNameswithoutExt = filename.substring(0, lastDotIndex);
                SongNames.add(SongNameswithoutExt); //removing extensions
            } else
                SongNames.add(filename); //in any case the audio file does not have any .extension :)
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, SongNames);
        arrayAdapter.notifyDataSetChanged();
        SongListView.setAdapter(arrayAdapter);

        seekBar = findViewById(R.id.seekBar);
        progressTextView = findViewById(R.id.progressTextView);
        CurrentSec = findViewById(R.id.CurrentSec);
        DurationSong = findViewById(R.id.DurationSong);

        searchbutton = findViewById(R.id.searchbutton);
        playbutton = findViewById(R.id.playbutton);
        forwardbutton = findViewById(R.id.forwardbutton);
        rewindbutton = findViewById(R.id.rewindbutton);
//        switchtheme = findViewById(R.id.switchtheme);

        playbutton.setOnClickListener(this);
        forwardbutton.setOnClickListener(this);
        rewindbutton.setOnClickListener(this);
        seekBar.setOnClickListener(this);

//        switchtheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                if (isChecked) {
//                    setTheme(R.style.Theme_Musica);
//                } else {
//                    setTheme(R.style.DarkTheme_Musica);
//                }
//                recreate();
//            }
//        });
//
//        int currentTheme = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        switchtheme.setChecked(currentTheme == Configuration.UI_MODE_NIGHT_YES);

        Connected = false;

        if(SongNames.isEmpty()) {
            searchbutton.setEnabled(false);
            playbutton.setEnabled(false);
            rewindbutton.setEnabled(false);
            forwardbutton.setEnabled(false);
            switchtheme.setEnabled(false);
            seekBar.setVisibility(View.GONE);
        }
        else
            DoStart();

//        switchtheme.setOnClickListener();
        SongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MusicaService.CurrentSong = i;
                progressTextView.setText(SongNames.get(i));
                File songFile = songList.get(i);
                if(musicaService != null)
                    musicaService.playselectedSong(songFile);
                UpdateDuration();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void setupSeekBar(){
        seekBar.setMax(musicaService.maxProgress); //max progress
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

    //Total Duration of a song
    public static void UpdateDuration() {
        int converting = MusicaService.MP.getDuration() / 1000;
        int mins = converting / 60;
        int sec = converting % 60;
        String durationText = String.format("%02d:%02d", mins, sec); //displaying mins and sec to display the progress of a song
        DurationSong.setText(durationText);
//        String currentsectext = String.format("%02d:%02d", mins, sec);
    }

    private void UpdateProgress() {
        timertask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int converting = MusicaService.MP.getCurrentPosition() / 1000;
                        int mins = converting / 60;
                        int sec = converting % 60;
                        String currentDurText = String.format("%02d:%02d", mins, sec); //displaying mins and sec to display the progress of a song
                        CurrentSec.setText(currentDurText); //display the current pos of the song
                        seekBar.setProgress(MusicaService.MP.getCurrentPosition());
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
        playbutton.setImageResource(R.drawable.ic_pauseinv);
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
        playbutton.setImageResource(R.drawable.ic_playinv);
    }

//    public void changeThemeOnClick(View view)
//    {
//        String[] themes = {"Dark", "Light", "Special"};
//        int currentThemeIndex = Arrays.asList(themes).indexOf(getCurrentThemeName());
//
//        String nextTheme = themes[(currentThemeIndex + 1) % themes.length];
//
//        ThemeManager.changeTheme(this, nextTheme);
//    }

//    private String getCurrentThemeName(){
//        int themeResId = getApplicationInfo().theme;
//        if(themeResId == R.style.AppTheme_Dark)
//            return "Dark";
//        else if(themeResId == R.style.AppTheme_Light)
//            return "Light";
//        else
//            return "Special";
//    }
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
            UpdateProgress();
        }
        @Override
        public void onServiceDisconnected(ComponentName CompNam) { Connected = false;}
    };


}