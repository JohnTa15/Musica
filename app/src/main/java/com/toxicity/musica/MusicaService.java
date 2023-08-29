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
import java.util.ArrayList;

public class MusicaService extends Service implements MediaPlayer.OnCompletionListener
{
    final int TimeToPlay = 0;
    int[] SongsIDs;
    ArrayList<File> audiofiles;
    String[] SongTitles;
    int CurrentSong = 0;
    int[] IDs;
    Boolean Act;
    int i;
    MediaPlayer MP;
    MusicaInterface RegUpdates = null;
    private final IBinder Binder = new LocalBinder ();

    public MusicaService()
    {

    }
    public void onCreate()
    {
        CurrentSong = 0;
        Act = false;
        MP = MediaPlayer.create(this, SongsIDs[CurrentSong]);
        audiofiles = MusicFinder();
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

    public void NextSong()
    {
        if(MP.isPlaying())
        {
            MP.stop();
            MP.release();
        }
        for(i = 0; i <= ; i++)
        {
            if(++CurrentSong == )
                CurrentSong = 0;
        }

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

    public String SongTitle()
    {
        return SongTitles[CurrentSong];
    }

    public void UpdateTitle ()
    {
        if (RegUpdates != null)
            RegUpdates.UpdateTitle (SongTitles[CurrentSong]);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

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
