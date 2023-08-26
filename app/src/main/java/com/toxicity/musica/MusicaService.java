package com.toxicity.musica;

import android.media.MediaPlayer;
import android.widget.ArrayAdapter;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.nio.file.Files;

public class MusicaService extends Service implements MediaPlayer.OnCompletionListener
{

    final int TimeToPlay = 0;
    int[] SongIDs;
    String[] SongTitles;
    int CurrentSong = 0;
    Boolean Act;
    int i;
    int j;
    MediaPlayer MP;
    MusicaInterface RegUpdates = null;

    public MusicaService()
    {

    }
    public void OnCreate()
    {
        for(i = 1; i <= j; i++)
        {
            SongIDs = new int[i];
        }
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

    //Binding Proccess
    private final IBinder Binder = new LocalBinder ();

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
        ShowMessage ("Someone Unbinded..");
        return false;              //Allow Rebind? For started services
    }

}
