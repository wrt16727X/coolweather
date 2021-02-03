package com.coolweather.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.coolweather.gson.Forecast;
import com.coolweather.coolweather.gson.Weather;
import com.coolweather.coolweather.service.AutoUpdateService;
import com.coolweather.coolweather.util.HttpUtil;
import com.coolweather.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private Button bindService;

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefreshLayout;
    private Button navButton;
    public DrawerLayout drawerLayout;
    public AutoUpdateService autoUpdateService;


    private final ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
             autoUpdateService=((AutoUpdateService.UpdateBinder)service).getService();
             autoUpdateService.setOnUpdateListener(new AutoUpdateService.onUpdateListener() {
                 @Override
                 public void onChanged(String response) {
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Weather weather = Utility.handleWeatherResponse(response);
                             showWeatherInfo(weather);
                             Log.d("TAG", "run: =================================");
                         }
                     });

                 }
             });
//              autoUpdateService.updateWeather();
//             updateBinder= (AutoUpdateService.UpdateBinder) service;
//             String response=updateBinder.putresponseText();
//            SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
//            String responseText = pres.getString("weather", null);
//            Weather weather = Utility.handleWeatherResponse(responseText);
//            showWeatherInfo(weather);
            Log.d("TAG", "onServiceConnected: ==========================");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    class UpdateWeatherReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences pres = PreferenceManager.getDefaultSharedPreferences(context);
            String responseText = pres.getString("weather", null);
            Weather weather = Utility.handleWeatherResponse(responseText);
            showWeatherInfo(weather);
            Log.d("TAG", "onReceive: ====================");
        }
    }

    @Override
    protected void onDestroy() {

        Log.d("WeatherActivity", "onDestroy: " );
        super.onDestroy();
    }

    @Override


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
           if (Build.VERSION.SDK_INT>=21){
               View decorView=getWindow().getDecorView();
               decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
               getWindow().setStatusBarColor(Color.TRANSPARENT);
           }
        setContentView(R.layout.activity_weather);
           Intent intent=new Intent(WeatherActivity.this,AutoUpdateService.class);
           startService(intent);
        Intent bindintent=new Intent(WeatherActivity.this, AutoUpdateService.class);
        bindService(bindintent,connection,BIND_AUTO_CREATE);

           bindService=findViewById(R.id.bindservice);
           bindService.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   autoUpdateService.onChange();
//                   listenUpdate();
//                   Intent bindintent=new Intent(WeatherActivity.this, AutoUpdateService.class);
//                   bindService(bindintent,connection,BIND_AUTO_CREATE);

               }
           });
        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        bingPicImg=findViewById(R.id.bing_pic_img);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh);
        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);
        swipeRefreshLayout.setColorSchemeResources(R.color.design_default_color_primary);

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("UPDATE_WEATHER");
        BroadcastReceiver updateWeatherReceiver=new UpdateWeatherReceiver();
        registerReceiver(updateWeatherReceiver,intentFilter);


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString =pre.getString("weather",null);
        final String weatherId;
        if (weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences pre= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherId=pre.getString("weather_new_id",null);
                requestWeather(weatherId);
            }
        });

        String bingPic=pre.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

//            SharedPreferences pres= PreferenceManager.getDefaultSharedPreferences(this);
//            String responseText= pres.getString("weather",null);
//            Weather weather=Utility.handleWeatherResponse(responseText);
//            showWeatherInfo(weather);

    }
    public void listenUpdate(){

                SharedPreferences pres= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String responseText= pres.getString("weather",null);
                Weather weather=Utility.handleWeatherResponse(responseText);
                showWeatherInfo(weather);
                }
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=" +
                "b15a8044b0f1405fb6ea57595f402fbc";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                  final String responseText=response.body().string();

                Log.d("TAG", responseText+"---------------------------");


                  final Weather weather=Utility.handleWeatherResponse(responseText);
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          if (weather!=null && "ok".equals(weather.status)){
                              SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                              editor.putString("weather",responseText);
                              editor.apply();
                              showWeatherInfo(weather);

                          }else {
                              Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                          }
                          swipeRefreshLayout.setRefreshing(false);
                      }
                  });
            }
        });
        loadBingPic();
    }
    public void showWeatherInfo(Weather weather){
        Log.e("WeatherActivity", "111: " );
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split("")[1];
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);


            String degree = weather.now.temperature + "c";
            //String weatherInfo = weather.now.more.info;
            degreeText.setText(degree);
            //weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();

        for(Forecast forecast : weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        Log.d("TAG", "showWeatherInfo: ============");
        if(weather.suggestion!=null){
            String comfort="舒适度: "+weather.suggestion.comfort.info;
            String carWash = "洗车指数: " + weather.suggestion.carWash.info;
            String sport = "运动建议: " + weather.suggestion.sPort.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
        }
        weatherLayout.setVisibility(View.VISIBLE);
//        Intent intent=new Intent(WeatherActivity.this, AutoUpdateService.class);
//        startService(intent);

        Log.d("TAG", "Start Service====================== ");

    }
    private void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                  final String bingPic=response.body().string();
                  SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                  editor.apply();
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                      }
                  });
            }
        });
    }


}


