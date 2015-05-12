package com.ruijie.pojo;

/**
 * Created by OA on 2015/4/14.
 */
public class DateFeature {
    private String date;
    private int regionID;
    private int buildingID;
    //天气特征
    private float weatherBegin;//当天的初始天气
    private float weatherEnd; //当天的结束天气，比如多云转晴，weatherBegin为多云,目前值用权重来表示
    private int tempHigh;//当天最高气温
    private int tempLow;//当天最低气温
    private int aqi;//空气质量
    private float wind;//风力
    //日期特征，是否为休息日或者节假日
    private int today;//当天是否为休息日
    private int tomorrow;//明天是否为休息日
    private int festival;//当日是否为节假日，什么节假日
    private int dayInHoliday;//当日在节假日中的第几天
    private int sumDays;//节假日总共多少天

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRegionID() {
        return regionID;
    }

    public void setRegionID(int regionID) {
        this.regionID = regionID;
    }

    public int getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(int buildingID) {
        this.buildingID = buildingID;
    }

    public float getWeatherBegin() {
        return weatherBegin;
    }

    public void setWeatherBegin(float weatherBegin) {
        this.weatherBegin = weatherBegin;
    }

    public float getWeatherEnd() {
        return weatherEnd;
    }

    public void setWeatherEnd(float weatherEnd) {
        this.weatherEnd = weatherEnd;
    }

    public int getTempHigh() {
        return tempHigh;
    }

    public void setTempHigh(int tempHigh) {
        this.tempHigh = tempHigh;
    }

    public int getTempLow() {
        return tempLow;
    }

    public void setTempLow(int tempLow) {
        this.tempLow = tempLow;
    }

    public int getAqi() {
        return aqi;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public float getWind() {
        return wind;
    }

    public void setWind(float wind) {
        this.wind = wind;
    }

    public int getToday() {
        return today;
    }

    public void setToday(int today) {
        this.today = today;
    }

    public int getTomorrow() {
        return tomorrow;
    }

    public void setTomorrow(int tomorrow) {
        this.tomorrow = tomorrow;
    }

    public int getFestival() {
        return festival;
    }

    public void setFestival(int festival) {
        this.festival = festival;
    }

    public int getDayInHoliday() {
        return dayInHoliday;
    }

    public void setDayInHoliday(int dayInHoliday) {
        this.dayInHoliday = dayInHoliday;
    }

    public int getSumDays() {
        return sumDays;
    }

    public void setSumDays(int sumDays) {
        this.sumDays = sumDays;
    }
}
