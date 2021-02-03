package com.coolweather.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    public More more;
    public class More{
        @SerializedName("txt")
          public String info;
    }
}
