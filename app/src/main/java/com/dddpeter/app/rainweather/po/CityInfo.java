package com.dddpeter.app.rainweather.po;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

import java.io.Serializable;

import lombok.Data;

@Data
@Table(name="t_cityinfo")
public class CityInfo implements Serializable {
    public String getCityid() {
        return cityid;
    }

    public void setCityid(String cityid) {
        this.cityid = cityid;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Id
    private String cityid;
    private String city;
    public CityInfo(){};
    public CityInfo(String id,String name){
        this.city = name;
        this.cityid = id;
    }
}
