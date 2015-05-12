package com.ruijie.pojo;

/**
 * Created by OA on 2015/4/23.
 */
public class PredictTrainData {

    //地点
    private int buildingID;
    private int floorID;
    private int storeID;

    //时间特征
    private String date;
    private int hour;

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

    //近30日均值，近7日均值，实际值
    private float allNum;
    private float oldNum;
    private float avgStayTime;
    private float enteringRate;
    private float stayRate;

}
