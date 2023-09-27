package com.toxicity.musica;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicaService extends Service implements MediaPlayer.OnCompletionListener
{
    final int TimeToPlay = 0;
    ArrayList<File> filename;
    int[] songID;
    ArrayList<String> songTitle; //for displaying and updating Song Title
    public static int CurrentSong = 0;
    Boolean Act;
    static MediaPlayer MP;
    int maxProgress;
    int currentProgress;
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
        DirAction.MusicFinder musicFinder = new DirAction.MusicFinder();
        filename = DirAction.MusicFinder.findMusicFiles();
        if (!filename.isEmpty())
        {
            MP = new MediaPlayer();
            try {
                MP.setDataSource(String.valueOf(filename.get(CurrentSong)));
                MP.prepare();
            } catch (IOException e){
                e.printStackTrace();
            }
            maxProgress = MP.getDuration();
            currentProgress = MP.getCurrentPosition();
        }
        IDs();
        createNotificationChannel();
        DisplayNotification();
//        MP = new MediaPlayer();
//        try {
//            MP.setDataSource(String.valueOf(filename.get(CurrentSong)));
//            MP.prepare();
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//        maxProgress = MP.getDuration();
//        currentProgress = MP.getCurrentPosition();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            if (CurrentSong >= 0 && CurrentSong == intent.getIntExtra("playing_song", 0)) {
                return START_STICKY;
            }
            if (MP.isPlaying()) {
                MP.stop();
                MP.release();
            }
        try {
            MP.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
            MP.start();
            Act = true;
            return START_STICKY;
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
//        RemoteViews notibuttons = new RemoteViews(getPackageName(), R.layout.notifications); // Replace with your actual layout resource ID
//
//        Intent rewindButtonIntent = new Intent(this, NotificationReceiver.class);
//        rewindButtonIntent.setAction("Rewind");
//        PendingIntent rewindPendingIntent = PendingIntent.getBroadcast(this, 0, rewindButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent forwardButtonIntent = new Intent(this, NotificationReceiver.class);
//        forwardButtonIntent.setAction("Forward");
//        PendingIntent forwardPendingIntent = PendingIntent.getBroadcast(this, 1, forwardButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent ppButtonIntent = new Intent(this, NotificationReceiver.class);
//        ppButtonIntent.setAction("Play/Pause");
//        PendingIntent ppPendingIntent = PendingIntent.getBroadcast(this, 2, ppButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        notibuttons.setOnClickPendingIntent(R.id.rewindbutton, rewindPendingIntent);
//        notibuttons.setOnClickPendingIntent(R.id.forwardbutton, forwardPendingIntent);
//        notibuttons.setOnClickPendingIntent(R.id.playbutton, ppPendingIntent);
//        notibuttons.setProgressBar(R.id.progressBar, maxProgress, currentProgress, false);


        String textContent = "Musica is active!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setContentTitle("Musica Player")
                .setContentText(textContent)
                .setSmallIcon(R.mipmap.musica)
//                .setContent(notibuttons) // Set the custom RemoteViews here
                .addAction(R.id.rewindbutton,"Rewind",null)
                .addAction(R.id.forwardbutton,"Forward",null)
                .addAction(R.id.playbutton,"PP",null)
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
        if(!filename.isEmpty()) {
            MP.stop();
            MP.release();
            Act = false;
        }
    }

    public void PreviousSong()
    {
        if(MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
                if(--CurrentSong == -1) //moving to previous song
                    CurrentSong = filename.size() - 1;
                try {
                    MP = new MediaPlayer();
                    MP.setDataSource(String.valueOf(filename.get(CurrentSong)));
                    MP.prepare();
                    MP.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        PlaySong();
    }
    public void NextSong() {

        if(MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
            if (++CurrentSong == filename.size()) //moving to the next song
                CurrentSong = 0;
            try {
                MP = new MediaPlayer();
                MP.setDataSource(String.valueOf(filename.get(CurrentSong)));
                MP.prepare();
                MP.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public void IDs() { //creating ID for each song (ex. songID[1] the first one)
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
        return Binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return false;
    }
}
