package com.coolweather.coolweather.util;

import android.text.TextUtils;

import com.coolweather.coolweather.db.City;
import com.coolweather.coolweather.db.County;
import com.coolweather.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    public static boolean handleProvinceresponse(String response) {
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province =new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allcitys=new JSONArray(response);
                for(int i=0;i<allcitys.length();i++){
                    JSONObject provinceObject=allcitys.getJSONObject(i);
                    City city =new City();
                    city.setCityName(provinceObject.getString("name"));
                    city.setCityCode(provinceObject.getInt("weather_id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounty=new JSONArray(response);
                for(int i=0;i<allCounty.length();i++){
                    JSONObject provinceObject=allCounty.getJSONObject(i);
                    County county =new County();
                    county.setCountryName(provinceObject.getString("name"));
                    county.setWeatherId(provinceObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
