package com.ruijie.pojo;

/**
 * Created by OA on 2015/5/5.
 */
//神经网络预测，输入参数的指标值
public class PredictOutIndex {
    private String date;
    private int hour;
    private float index;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public float getIndex() {
        return index;
    }

    public void setIndex(float index) {
        this.index = index;
    }
}
