package com.toxicity.musica;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.List;

import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicaService extends Service implements MediaPlayer.OnCompletionListener
{
    final int TimeToPlay = 0;
    ArrayList<String> songPath; //path..
    ArrayList<File> filename;
    int[] songID;
    ArrayList<String> songTitle; //for displaying and updating Song Title
    int CurrentSong = 0;
    Boolean Act;
    MediaPlayer MP;
    private static final int NOTIFICATION_ID = 1;
    MusicaInterface RegUpdates = null;
    private final IBinder Binder = new LocalBinder();

    public MusicaService()
    {

    }
    public void onCreate()
    {
        CurrentSong = 0;
        Act = false;
        IDs();
        DirActionAct.MusicFinder musicFinder = new DirActionAct.MusicFinder();
        filename = musicFinder.findMusicFiles();
        createNotificationChannel();
        DisplayNotification();
        MP = new MediaPlayer();

    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String description = "Channel Desc";

            NotificationChannel channel = new NotificationChannel("Notification Channel",
                    "Notification", importance);
            channel.setDescription(description); // Set the channel description

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void DisplayNotification()
    {
        RemoteViews notibuttons = new RemoteViews(getPackageName(), R.layout.notifications); // Replace with your actual layout resource ID

        Intent rewindButtonIntent = new Intent(this, NotificationReceiver.class);
        rewindButtonIntent.setAction("Rewind");
        PendingIntent rewindPendingIntent = PendingIntent.getBroadcast(this, 0, rewindButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent forwardButtonIntent = new Intent(this, NotificationReceiver.class);
        forwardButtonIntent.setAction("Forward");
        PendingIntent forwardPendingIntent = PendingIntent.getBroadcast(this, 1, forwardButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent ppButtonIntent = new Intent(this, NotificationReceiver.class);
        ppButtonIntent.setAction("Play/Pause");
        PendingIntent ppPendingIntent = PendingIntent.getBroadcast(this, 2, ppButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        notibuttons.setOnClickPendingIntent(R.id.rewindbutton, rewindPendingIntent);
        notibuttons.setOnClickPendingIntent(R.id.forwardbutton, forwardPendingIntent);
        notibuttons.setOnClickPendingIntent(R.id.playbutton, ppPendingIntent);

        String textContent = "Musica is active!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setContentTitle("Musica Player")
                .setContentText(textContent)
                .setSmallIcon(R.mipmap.musica)
                .setContent(notibuttons) // Set the custom RemoteViews here
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }


    }
    public void onDestroy()
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
        if(Act) {
            DisplayNotification();
            return;
        }
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
        if (CurrentSong >= 0 && CurrentSong < songTitle.size()) {
            return songTitle.get(CurrentSong);
        } else {
            return "";
        }
    }
    public void IDs() {
        songID = new int[filename.size()];
        for (int i = 0; i < filename.size(); i++)
            songID[i] = i;
    }
    public void UpdateTitle() {
        if (RegUpdates != null) {
            RegUpdates.UpdateTitle(SongTitle());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
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
