package com.toxicity.musica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("REWIND".equals(action)) {
            // Handle rewind button click
            Toast.makeText(context, "Rewind clicked", Toast.LENGTH_SHORT).show();
        } else if ("PLAY_PAUSE".equals(action)) {
            // Handle play/pause button click
            Toast.makeText(context, "Play/Pause clicked", Toast.LENGTH_SHORT).show();
        } else if ("FORWARD".equals(action)) {
            // Handle forward button click
            Toast.makeText(context, "Forward clicked", Toast.LENGTH_SHORT).show();
        }
    }
}
