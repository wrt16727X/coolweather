package com.coolweather.coolweather.db;


import org.litepal.exceptions.DataSupportException;

public class Province extends DataSupport {
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    private String provinceName;
    private int provinceCode;
    public int getId() {
        return id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }



}
