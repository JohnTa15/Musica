package com.toxicity.musica;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicaService extends Service implements MediaPlayer.OnCompletionListener
{
    final int TimeToPlay = 0;
    ArrayList<String> songPath; //path..
    ArrayList<String> songTitles; //for displaying and updating Song Title
    int CurrentSong = 0;
    Boolean Act;
    MediaPlayer MP;
    MusicaInterface RegUpdates = null;
    private final IBinder Binder = new LocalBinder();

    public MusicaService()
    {

    }
    public void onCreate()
    {
        CurrentSong = 0;
        Act = false;
        MP = MediaPlayer.create(this, SongsIDs[CurrentSong]);
        songPaths = MusicFinder();
        MP.setOnCompletionListener(this);
    }

    public void OnDestroy()
    {
        ShowMessage("Service Destroyed");
        MP.stop();
        MP.release();
        Act = false;
    }

    private void ShowMessage(String Mess) //creating Toast Notifications
    {
        Toast Tst = Toast.makeText (getApplicationContext (), "Service: " + Mess, Toast.LENGTH_LONG);
        Tst.show ();
    }

   @Override
    public void OnCompletion(MediaPlayer mp)
    {
        NextSong();
    }


    public void PreviousSong()
    {
        if(Act) {
            MP.stop();
            MP.reset();
        }
            if(CurrentSong > 0) {
                CurrentSong--; //moving to previous song
                String previousSongPath = songPath.get(CurrentSong);
                try {
                    MP.setDataSource(previousSongPath);
                    MP.prepare();
                    MP.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                CurrentSong = songPath.size() - 1;
            }

        PlaySong();
    }
    public void NextSong()
    {
        if(CurrentSong < songPath.size() - 1) //checking the existance of next song
        {
            MP.stop();
            MP.reset();

            CurrentSong++; //moving to the next song
            String nextSongPath = songPath.get(CurrentSong);
            try{
                MP.setDataSource(nextSongPath);
                MP.prepare();
                MP.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            CurrentSong = 0;
        PlaySong();
    }
    public void PlaySong()
    {
        if(Act)
            return;
        MP.start();
        if(TimeToPlay > 0)
            MP.seekTo(MP.getDuration() - TimeToPlay);
        UpdateTitle();
        Act = true;
    }

    public void PauseSong()
    {
        if(!Act)
            return;
        MP.pause();
        Act = false;
    }

    public String SongTitle() {
        if (CurrentSong >= 0 && CurrentSong < songTitles.size()) {
            return songTitles.get(CurrentSong);
        } else {
            return "";
        }
    }

    public void UpdateTitle() {
        if (RegUpdates != null) {
            RegUpdates.UpdateTitle(SongTitle());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        NextSong();
    }

    //Binding Proccess
    public class LocalBinder extends Binder
    {
        MusicaService getService ()
        {
            return MusicaService.this;
        }
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        ShowMessage ("Binding Successfully..");
        return Binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        ShowMessage ("Unbinded..");
        return false;
    }
}
