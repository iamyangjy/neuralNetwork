package com.ruijie.pojo;

/**
 * Created by OA on 2015/4/20.
 */
public class StoreIndex {
    private int buildingID;
    private int floorID;
    private int storeID;
    private String date;
    private float allNum;
    private float oldNum;
    private float avgStayTime;
    private float enteringRate;
    private float stayRate;

    public int getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(int buildingID) {
        this.buildingID = buildingID;
    }

    public int getFloorID() {
        return floorID;
    }

    public void setFloorID(int floorID) {
        this.floorID = floorID;
    }

    public int getStoreID() {
        return storeID;
    }

    public void setStoreID(int storeID) {
        this.storeID = storeID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getAllNum() {
        return allNum;
    }

    public void setAllNum(float allNum) {
        this.allNum = allNum;
    }

    public float getOldNum() {
        return oldNum;
    }

    public void setOldNum(float oldNum) {
        this.oldNum = oldNum;
    }

    public float getAvgStayTime() {
        return avgStayTime;
    }

    public void setAvgStayTime(float avgStayTime) {
        this.avgStayTime = avgStayTime;
    }

    public float getEnteringRate() {
        return enteringRate;
    }

    public void setEnteringRate(float enteringRate) {
        this.enteringRate = enteringRate;
    }

    public float getStayRate() {
        return stayRate;
    }

    public void setStayRate(float stayRate) {
        this.stayRate = stayRate;
    }
}
