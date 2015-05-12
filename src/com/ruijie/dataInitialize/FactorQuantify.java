package com.ruijie.dataInitialize;

/**
 * Created by OA on 2015/4/15.
 */
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FactorQuantify {

 	public static final int ALLNUM = 0;
	public static final int DURATION = 1;
	public static final int ENTERRATE = 2;
	public static final int OLDNUM = 3;
	public static final int STAYRATE = 4;

    private static Logger logger = LogManager.getLogger(FactorQuantify.class);

    private String weatherFile;
    private String holidayFile;
    private String predictFile;

    public String getWeatherFile() {
        return weatherFile;
    }

    public void setWeatherFile(String weatherFile) {
        this.weatherFile = weatherFile;
    }

    public String getHolidayFile() {
        return holidayFile;
    }

    public void setHolidayFile(String holidayFile) {
        this.holidayFile = holidayFile;
    }

    public String getPredictFile() {
        return predictFile;
    }

    public void setPredictFile(String predictFile) {
        this.predictFile = predictFile;
    }

    private final int numFeaInHoliday = 5;//今天是否休息,明天是否休息,节假日类型,法定假日第几天,该法定假日总天数
    //private Map<String, int[]> holidayQuantify;
    //private Map<String, Float> weatherQuantify;

    public Map<String, int[]> holidayQuantify;
    public Map<String, Float> weatherQuantify;
    public Map<String, String> predictQuantify;

    public void init(){
        //属性对象集合
        Properties prop = new Properties();
        try{
            //配置文件
            FileInputStream fileInputStream = new FileInputStream(holidayFile);
            //字符串转为utf-8
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            //加载到properties对象中
            prop.load(inputStreamReader);
            fileInputStream.close();
            //获取节假日的map映射
            holidayQuantify = new HashMap<String, int[]>();
            Iterator holidayIter = prop.entrySet().iterator();
            while(holidayIter.hasNext()){
                Map.Entry entry = (Map.Entry) holidayIter.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                String[] valList = val.split(",");
                if(valList.length != 5){
                    logger.error("holiday configFile format error");
                    throw new Exception("holiday configFile format error");
                }
                int[] holidayVal = new int[numFeaInHoliday];
                for(int i=0; i<valList.length; i++){
                    holidayVal[i] = Integer.valueOf(valList[i]);
                }
                holidayQuantify.put(key, holidayVal);
            }

            //天气的map
            fileInputStream = new FileInputStream(weatherFile);
            //字符床转化为utf-8
            inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            //加载到properties对象中
            prop.clear();
            prop.load(inputStreamReader);
            fileInputStream.close();
            //获取天气的Map
            weatherQuantify = new HashMap<String, Float>();
            Iterator weatherIter = prop.entrySet().iterator();
            while (weatherIter.hasNext()){
                Map.Entry entry = (Map.Entry) weatherIter.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                weatherQuantify.put(key, Float.parseFloat(val));
            }

            //客流预测参数的的map
            fileInputStream = new FileInputStream(predictFile);
            //转化为utf-8
            inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            //加载到properties
            prop.clear();
            prop.load(inputStreamReader);
            fileInputStream.close();
            predictQuantify = new HashMap<String, String>();
            Iterator predictIter = prop.entrySet().iterator();
            while (predictIter.hasNext()){
                Map.Entry entry = (Map.Entry) predictIter.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                predictQuantify.put(key, value);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int[] getHolidayQuantify(String dateStr){
        if(holidayQuantify.containsKey(dateStr)){
            return holidayQuantify.get(dateStr);
        }else{
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            try{
                date = dateFormat.parse(dateStr);
            }catch (ParseException e){
                logger.info("date format error:" + dateStr);
                e.printStackTrace();
            }
            cal.setTime(date);
            int weekday = cal.get(Calendar.DAY_OF_WEEK);
            if(cal.getFirstDayOfWeek() == Calendar.SUNDAY){
                weekday -= 1;
                if(weekday == 0){
                    weekday = 7;
                }
            }
            int[] valList = new int[numFeaInHoliday];
            switch (weekday) {
                case 5:
                    valList[1] = 1;
                    break;
                case 6:
                    valList[0] = 1;
                    valList[1] = 1;
                    break;
                case 7:
                    valList[0] = 1;
                    break;
                default:
                    break;
            }

            return valList;
        }
    }

    public Float getWeatherQuantify(String weather){
        if(weatherQuantify.containsKey(weather)){
            return weatherQuantify.get(weather);
        }else{
            logger.error("key " + weather + " not Found");
            return 0.0f;
        }
    }

    //-1表示预测所有的building；
    public int getPredictBuilding() {
        String buildingStr = predictQuantify.get("buildingID");
        return Integer.parseInt(buildingStr);
    }

    //0表示不预测店铺数据。
    public int getPredictStore(){
        String storeStr = predictQuantify.get("everyStore");
        return Integer.parseInt(storeStr);
    }

    public static void main(String[] args){
        //属性集合对象
        Properties prop = new Properties();
        try{
            //配置文件
            //节假日配置信息
            FileInputStream fileInputStream = new FileInputStream("E:\\IDEAProject" +
                    "\\Project\\customerPredict\\conf\\holiday.properties");
            //加载到properties对象中
            prop.load(fileInputStream);
            fileInputStream.close();

            //获取属性值
            String t = prop.getProperty("date");
            System.out.println(t);
            //获取属性值，有默认值
            String tt = prop.getProperty("2014-09-07", "123");
            System.out.println(tt);
            Iterator iter = prop.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry) iter.next();
                //String key = (String) entry.getKey();
                //String val = (String) entry.getValue();
                Object key = entry.getKey();
                Object val = entry.getValue();
                System.out.println("key:" +key + "value" + val);
            }
            Set<String> stringSet = prop.stringPropertyNames();
            for(String s :stringSet){
                System.out.println(s);
            }

            //天气配置信息
            Properties propWeather = new Properties();
            fileInputStream = new FileInputStream("E:\\IDEAProject" +
                    "\\Project\\customerPredict\\conf\\weather.properties");
            //有中文，需要转为utf-8
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            propWeather.load(inputStreamReader);
            fileInputStream.close();
            Iterator iterWeather = propWeather.entrySet().iterator();
            while (iterWeather.hasNext()){
                Map.Entry entry = (Map.Entry) iterWeather.next();
                //String key = (String) entry.getKey();
                //String val = (String) entry.getValue();
                Object key = entry.getKey();
                Object val = entry.getValue();
                System.out.println("key:" + key + "value:" + val);
            }

            //rbis配置信息
            Properties propRbis = new Properties();
            fileInputStream = new FileInputStream("E:\\IDEAProject" +
                    "\\Project\\customerPredict\\conf\\predict.properties");
            propRbis.load(fileInputStream);
            fileInputStream.close();
            Iterator iterRbis = propRbis.entrySet().iterator();
            while (iterRbis.hasNext()){
                Map.Entry entry = (Map.Entry) iterRbis.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                System.out.println("key:" + key + "value: " + val);
            }



        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
