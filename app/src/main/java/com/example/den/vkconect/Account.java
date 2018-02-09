package com.example.den.vkconect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.ArrayMap;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.den.vkconect.MainActivity.PHOTO_KEY;
import static com.example.den.vkconect.MainActivity.STATUS_AND_PHOTO_KEY;
import static com.example.den.vkconect.MainActivity.STATUS_KEY;

public class Account {
    public String access_token;
    public long user_id;
    public String TIME_STATUS_KEY = "timeStatus";
    public String TIME_STATUS_AND_PHOTO_KEY = "timeStatusAndPhoto";
    public String TIME_PHOTO_KEY = "timePhoto";

    public void save(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString("access_token", access_token);
        editor.putLong("user_id", user_id);
        editor.apply();
    }

    public void saveStatus(Context context, String value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(STATUS_KEY, value);
        editor.putString(TIME_STATUS_KEY, saveTime());
        editor.apply();
    }

    public void savePhoto(Context context, String value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(PHOTO_KEY, value);
        editor.putString(TIME_PHOTO_KEY, saveTime());
        editor.apply();
    }

    public void saveStatusAndPhoto(Context context, String value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(STATUS_AND_PHOTO_KEY, value);
        editor.putString(TIME_STATUS_AND_PHOTO_KEY, saveTime());
        editor.apply();
    }

    public String restoreStat(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(STATUS_KEY, null);
    }

    public String restorePhoto(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PHOTO_KEY, null);
    }

    public String restorePhotoAndStat(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(STATUS_AND_PHOTO_KEY, null);
    }

    public String restorePhotoTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(TIME_PHOTO_KEY, null);
    }

    public String restorePhotoAndStatTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(TIME_STATUS_AND_PHOTO_KEY, null);
    }

    public String restoreStatTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(TIME_STATUS_KEY, null);
    }

    public void restore(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        access_token = prefs.getString("access_token", null);
        user_id = prefs.getLong("user_id", 0);
    }
    private String saveTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy : HH:mm:ss");
      return sdf.format(new Date());
    }

}
