package com.ruijie.pojo;

/**
 * Created by OA on 2015/5/5.
 */
//近30日和近7日的指标值
public class RecentIndexValue {
    private String date;
    private float recent30Value;
    private float recent7Value;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getRecent30Value() {
        return recent30Value;
    }

    public void setRecent30Value(float recent30Value) {
        this.recent30Value = recent30Value;
    }

    public float getRecent7Value() {
        return recent7Value;
    }

    public void setRecent7Value(float recent7Value) {
        this.recent7Value = recent7Value;
    }
}
