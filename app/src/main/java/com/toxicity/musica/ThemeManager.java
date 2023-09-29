package com.toxicity.musica;

import android.app.Activity;
    public class ThemeManager{
        public static void changeTheme(Activity activity, String themeName){
            switch (themeName) {
//                case "Dark":
//                    activity.setTheme(R.style.AppTheme_Dark);
//                    break;
//                case "Light":
//                    activity.setTheme(R.style.AppTheme_Light);
//                    break;
//                case "Special":
//                    activity.setTheme(R.style.AppTheme_Special);
//                    break;
            }
            activity.recreate(); //refresh the activity to aply new themes
        }
    }