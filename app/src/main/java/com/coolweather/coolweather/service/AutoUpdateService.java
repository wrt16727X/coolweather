package com.coolweather.coolweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import android.os.Handler;
import com.coolweather.coolweather.WeatherActivity;
import com.coolweather.coolweather.gson.Weather;
import com.coolweather.coolweather.util.HttpUtil;
import com.coolweather.coolweather.util.MyApplication;
import com.coolweather.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    String TAG = "tag";

//    private OnWeatherChangeListener onWeatherChangeListener;
    public UpdateBinder mBinder=new UpdateBinder();

    public onUpdateListener listener;
    public interface onUpdateListener{
        void onChanged(String response);
    }
    public void setOnUpdateListener(onUpdateListener onUpdateListener){
        this.listener=onUpdateListener;
    }

    public class UpdateBinder extends Binder{
       // public void putresponseText(){
//            updateWeather();
//            SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
//            String responseText = pres.getString("weather", null);
//            return responseText;
        public AutoUpdateService getService(){
            return AutoUpdateService.this;
        }
    }
    public void onChange(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                    String response = pres.getString("weather", null);
                    if (listener != null) {
                        listener.onChanged(response);
                    }
                    try {
                        Thread.sleep(5000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
//public interface OnWeatherChangeListener{
//        void onWeatherChange(String responseText);
//}
//public void setOnWeatherChangeListener(OnWeatherChangeListener onWeatherChangeListener){
//    this.onWeatherChangeListener=onWeatherChangeListener;
//}
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        updateWeather();
        updateBingPic();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: =======================");
        updateWeather();
        updateBingPic();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 5000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    public void updateWeather() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=b15a8044b0f1405fb6ea57595f402fbc";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        Log.d(TAG, "onResponse: =============================================");
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
//                        Intent intent = new Intent("UPDATE_WEATHER");
//                        sendBroadcast(intent);



                    }

                }
            });
        }
    }

    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }

}
