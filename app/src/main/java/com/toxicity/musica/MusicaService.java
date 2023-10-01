package com.toxicity.musica;

import static com.toxicity.musica.MainActivity.SongNames;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicaService extends Service
{
    final int TimeToPlay = 0;
    ArrayList<File> filename;
    int[] songID;
    public int CurrentSong = 0;
    Boolean Act;
    static MediaPlayer MP;
    int maxProgress;
    int currentProgress;
    private static final int NOTIFICATION_ID = 1;
    private final IBinder Binder = new LocalBinder();

    public MusicaService()
    {

    }
    public void onCreate()
    {
        CurrentSong = 0;
        Act = false;
//        DirAction.MusicFinder musicFinder = new DirAction.MusicFinder();
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
            MP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    NextSong();
                }
            });
            maxProgress = MP.getDuration();
            currentProgress = MP.getCurrentPosition();
        }
        IDs();
        createNotificationChannel();
        DisplayNotification();
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
            int importance = NotificationManager.IMPORTANCE_LOW;
            String description = "Channel Description";
            NotificationChannel channel = new NotificationChannel("default",
                    "Musica Notification", importance);
            channel.setDescription(description); // Set the channel description

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void DisplayNotification()
    {
        // rewind intent
        Intent rewindIntent = new Intent(this, MusicaService.class);
        rewindIntent.setAction("Rewind");
        PendingIntent rewingIntentPend = PendingIntent.getService(this, 0, rewindIntent, PendingIntent.FLAG_IMMUTABLE);

        // play/pause intent
        Intent ppIntent = new Intent(this, MusicaService.class);
        ppIntent.setAction("PP");
        PendingIntent ppIntentPend = PendingIntent.getService(this, 0, ppIntent, PendingIntent.FLAG_IMMUTABLE);

        // next intent
        Intent forwardIntent = new Intent(this, MusicaService.class);
        forwardIntent.setAction("Forward");
        PendingIntent forwardIntentPend = PendingIntent.getService(this, 0, forwardIntent, PendingIntent.FLAG_IMMUTABLE);

        String textContent = "Now playing: " + SongNames.get(CurrentSong);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notiinv)
                .setContentText(textContent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .addAction(R.drawable.ic_rewindinv,"Rewind",rewingIntentPend)
                .addAction(R.drawable.ic_playinv,"PP",ppIntentPend)
                .addAction(R.drawable.ic_forwardinv,"Forward",forwardIntentPend)
                .setAutoCancel(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_LOW);



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

    public void PreviousSong() {
        if (MP.isPlaying()) {
            MP.stop();
            MP.release();
        }
        if (--CurrentSong == -1) //moving to previous song
            CurrentSong = filename.size() - 1;
        try {
            MP = new MediaPlayer();
            MP.setDataSource(String.valueOf(filename.get(CurrentSong)));
            MP.prepare();
            MP.start();
            MainActivity.UpdateDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                MainActivity.UpdateDuration();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        MainActivity.UpdateDuration();
        Act = true;
    }

    public void PauseSong()
    {
        if(!Act)
            return;
        MP.pause();
        MainActivity.UpdateDuration();
        Act = false;
    }

//    private void SearchingSongs() {
//        String query = searchEditText.getText().toString().toLowerCase();
//        MainActivity.SongNames.clear();
//
//        for(File song : MainActivity.songListView) {
//            String songName = song.getName().toLowerCase();
//            if(songName.contains(query))
//                Song.add(SongNames);
//        }
//    }

    public void IDs() { //creating ID for each song (ex. songID[1] the first one)
        songID = new int[filename.size()];
        for (int i = 0; i < filename.size(); i++)
            songID[i] = i;
    }

    //CHECK THIS AGAIN TO FIX THE SEQUENCE OF THE SONG AFTER FINISHING
//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        NextSong();
////        try {
////            MusicaService.MP.release();
////            MusicaService.MP.setDataSource(String.valueOf(filename.get(CurrentSong)));
////            MusicaService.MP.prepare();
////            MusicaService.MP.start();
////            UpdateDuration();
////            MainActivity.setupSeekBar();
////        } catch (IOException e){
////            e.printStackTrace();
////        }
//    }

    public void playselectedSong(File songFile) {
        if(MP != null)
        {
            MP.stop();
            MP.release();
            MP = null;
        }
        MP = new MediaPlayer();
        try {
            MP.setDataSource(songFile.getAbsolutePath());
            MP.prepare();
            MP.start();
            Act = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
