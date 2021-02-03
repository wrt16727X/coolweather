package com.coolweather.coolweather.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

public class MyApplication extends Application {
   private static Context context;
   public void onCreat(){

   }
   public static Context getContext(){
       return  context;
   }
}
