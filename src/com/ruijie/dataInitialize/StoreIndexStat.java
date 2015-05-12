package com.ruijie.dataInitialize;

/**
 * Created by OA on 2015/4/17.
 */
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ruijie.pojo.StoreIndex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.text.SimpleDateFormat;

public class StoreIndexStat {
    private static Logger logger = LogManager.getLogger(StoreIndexStat.class);
    private JdbcTemplate jdbcTemplate;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processDataForPredict(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        String date7before = simpleDateFormat.format(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -23);
        Date startDate = cal.getTime();
        //获取数据
        logger.info("get raw date");
        List<StoreIndex> storeIndexList = getRawData(startDate, endDate);

        //将数据原始的数据进行处理，弄成map数据结构：
        //{building|floor|store:{date:指标值数组}}
        Map<String,Map<String, float[]>> allStoreData = new HashMap<String, Map<String, float[]>>();
        for(StoreIndex storeIndex : storeIndexList){
            String location = storeIndex.getBuildingID() + "|" + storeIndex.getFloorID() + "|" + storeIndex.getStoreID();
            String dateTime = storeIndex.getDate();
            float[] indexArray = new float[5];
            indexArray[0] = storeIndex.getAllNum();
            indexArray[1] = storeIndex.getOldNum();
            indexArray[2] = storeIndex.getEnteringRate();
            indexArray[3] = storeIndex.getStayRate();
            indexArray[4] = storeIndex.getAvgStayTime();
            if(allStoreData.containsKey(location)){
                allStoreData.get(location).put(dateTime, indexArray);
            }else{
                Map<String, float[]> mapTmp = new HashMap<String, float[]>();
                mapTmp.put(dateTime, indexArray);
                allStoreData.put(location, mapTmp);
            }
        }

        //再次处理对数据进行处理，对各个key下的各指标各日期的数据格式化成list，保存到map中
        //map的数据格式为：{building|floor|store:{"7allNum":ArrayList, "7oldNum":ArrayList, ... ...}
        Map<String, Map<String, ArrayList<Float>>> allStoreIndex = new HashMap<String, Map<String, ArrayList<Float>>>();
        for(Map.Entry<String, Map<String, float[]>> majorEntry: allStoreData.entrySet()){
            String key = majorEntry.getKey();
            if(!allStoreIndex.containsKey(key)){
                Map<String, ArrayList<Float>> mapIndex = new HashMap<String, ArrayList<Float>>(){
                    {
                        put("7allNum", new ArrayList<Float>());
                        put("7oldNum", new ArrayList<Float>());
                        put("7enteringRate", new ArrayList<Float>());
                        put("7stayRate", new ArrayList<Float>());
                        put("7avgStayTime", new ArrayList<Float>());
                        put("30allNum", new ArrayList<Float>());
                        put("30oldNum", new ArrayList<Float>());
                        put("30enteringRate", new ArrayList<Float>());
                        put("30stayRate", new ArrayList<Float>());
                        put("30avgStayTime", new ArrayList<Float>());
                    }
                };
                allStoreIndex.put(key, mapIndex);
                //allStoreIndex.put()
            }

            for(Map.Entry<String, float[]> secondEntry: majorEntry.getValue().entrySet()){
                Map<String, ArrayList<Float>> mapTmp = allStoreIndex.get(key);
                String dateTime = secondEntry.getKey();
                if(dateTime.compareTo(date7before) >=0){
                    mapTmp.get("7allNum").add(secondEntry.getValue()[0]);
                    mapTmp.get("7oldNum").add(secondEntry.getValue()[1]);
                    mapTmp.get("7enteringRate").add(secondEntry.getValue()[2]);
                    mapTmp.get("7stayRate").add(secondEntry.getValue()[3]);
                    mapTmp.get("7avgStayTime").add(secondEntry.getValue()[4]);

                    mapTmp.get("30allNum").add(secondEntry.getValue()[0]);
                    mapTmp.get("30oldNum").add(secondEntry.getValue()[1]);
                    mapTmp.get("30enteringRate").add(secondEntry.getValue()[2]);
                    mapTmp.get("30stayRate").add(secondEntry.getValue()[3]);
                    mapTmp.get("30avgStayTime").add(secondEntry.getValue()[4]);
                }else{
                    mapTmp.get("30allNum").add(secondEntry.getValue()[0]);
                    mapTmp.get("30oldNum").add(secondEntry.getValue()[1]);
                    mapTmp.get("30enteringRate").add(secondEntry.getValue()[2]);
                    mapTmp.get("30stayRate").add(secondEntry.getValue()[3]);
                    mapTmp.get("30avgStayTime").add(secondEntry.getValue()[4]);
                }
            }

        }

        //再次处理数据，算出各个key下的各个指标的均值
        //map的结构为：{building|floor|store:{"7allNum":Float, "7oldNum":Float, ... ...}};
        final Map<String, Map<String, Float>> resultValue = new HashMap<String, Map<String, Float>>();
        final List<String> keyList = new ArrayList<String>();
        for(Map.Entry<String, Map<String, ArrayList<Float>>> majorEntry: allStoreIndex.entrySet()){
            String key = majorEntry.getKey();
            keyList.add(key);
            if(!resultValue.containsKey(key)){
                Map<String, Float> mapTmp = new HashMap<String, Float>(){
                    {
                        put("7allNum", new Float(0.0));
                        put("7oldNum", new Float(0.0));
                        put("7EnteringRate", new Float(0.0));
                        put("7stayRate", new Float(0.0));
                        put("7avgStayTime", new Float(0.0));
                        put("30allNum", new Float(0.0));
                        put("30oldNum", new Float(0.0));
                        put("30EnteringRate", new Float(0.0));
                        put("30stayRate", new Float(0.0));
                        put("30avgStayTime", new Float(0.0));
                    }
                };
                resultValue.put(key, mapTmp);
            }
            for(Map.Entry<String, ArrayList<Float>> secondEntry: majorEntry.getValue().entrySet()){
                ArrayList<Float> valueList = secondEntry.getValue();
                float[] floatArray = new float[valueList.size()];
                int i = 0;
                for (Float f : valueList) {
                    floatArray[i++] = (f != null ? f : 0.0f);
                }
                resultValue.get(key).put(secondEntry.getKey(), getArrayAvg(floatArray));
            }
        }

        //插入数据库
        logger.info("insert data");
        final String dateStr = simpleDateFormat.format(endDate);
        String sql = " replace into t_common_stat_recent(date, building_id, floor_id, store_id, recent30all_num, recent30old_num, " +
                " recent30entering_rate, recent30stay_rate, recent30avg_stay_time, recent7all_num, recent7old_num, recent7entering_rate, " +
                " recent7stay_rate, recent7avg_stay_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                String key = keyList.get(i);
                String[] keyArray = key.split("\\|");
                Map<String, Float> mapTmp = resultValue.get(key);

                int buildingID = Integer.parseInt(keyArray[0]);
                int floorID = Integer.parseInt(keyArray[1]);
                int storeID = Integer.parseInt(keyArray[2]);

                preparedStatement.setString(1, dateStr);
                preparedStatement.setInt(2, buildingID);
                preparedStatement.setInt(3, floorID);
                preparedStatement.setInt(4, storeID);
                preparedStatement.setFloat(5, mapTmp.get("30allNum"));
                preparedStatement.setFloat(6, mapTmp.get("30oldNum"));
                preparedStatement.setFloat(7, mapTmp.get("30enteringRate"));
                preparedStatement.setFloat(8, mapTmp.get("30stayRate"));
                preparedStatement.setFloat(9, mapTmp.get("30avgStayTime"));

                preparedStatement.setFloat(10, mapTmp.get("7allNum"));
                preparedStatement.setFloat(11, mapTmp.get("7oldNum"));
                preparedStatement.setFloat(12, mapTmp.get("7enteringRate"));
                preparedStatement.setFloat(13, mapTmp.get("7stayRate"));
                preparedStatement.setFloat(14, mapTmp.get("7avgStayTime"));
            }

            @Override
            public int getBatchSize() {
                return keyList.size();
            }
        });

        //当date为当日的时候，需要补全后十五天的的数据，用于预测。
        //用date进行判断，是为了避免重跑数据时候，错误替换数据。
        Date dateNow = new Date();
        String dateArg = simpleDateFormat.format(date);
        String dateNowStr = simpleDateFormat.format(dateNow);
        if(dateArg.equals(dateNowStr)){
            cal.setTime(dateNow);
            for(int i=0; i<15; i++){
                String sqlUpdate = "replace into t_common_stat_recent " +
                        " SELECT (date + INTERVAL ? day) as date, building_id, floor_id, store_id, recent30all_num, recent30avg_stay_time, " +
                        " recent30entering_rate, recent30old_num, recent30stay_rate, recent7all_num, " +
                        " recent7avg_stay_time, recent7entering_rate, recent7old_num, recent7stay_rate " +
                        " from t_common_stat_recent WHERE date=?";
                jdbcTemplate.update(sqlUpdate, new Object[]{i+1, dateStr});
            }
        }
    }

    public List<StoreIndex> getRawData(Date startDate, Date endDate){
        logger.info("get Raw Data from t_common_stat_for_predict, start....");
        String startTime = simpleDateFormat.format(startDate);
        String endTime = simpleDateFormat.format(endDate);
        //客流量小于10的统计记录无效(由于涉及到店铺，并且算平均值时，只获取中间的1/3进行计算，该条件暂不考虑)
        //building_id=0为无效记录，需要过滤
        String sql = "SELECT  building_id, floor_id, store_id, DATE_FORMAT(date, '%Y-%m-%d') as date, " +
                " all_num, avg_stay_time,entering_rate, old_num, stay_rate" +
                " from t_common_stat_for_predict " +
                " where hour=24 and building_id!=0 and date>=? and date<=?" +
                " ORDER BY building_id, floor_id, store_id, date desc ";

        List<StoreIndex> storeIndexeList = jdbcTemplate.query(sql, new Object[]{startTime, endTime}, new RowMapper<StoreIndex>() {
            @Override
            public StoreIndex mapRow(ResultSet resultSet, int i) throws SQLException {
                StoreIndex storeIndex = new StoreIndex();
                storeIndex.setBuildingID(resultSet.getInt("building_id"));
                storeIndex.setFloorID(resultSet.getInt("floor_id"));
                storeIndex.setStoreID(resultSet.getInt("store_id"));
                storeIndex.setDate(resultSet.getString("date"));
                storeIndex.setAllNum(resultSet.getFloat("all_num"));
                storeIndex.setAvgStayTime(resultSet.getFloat("avg_stay_time"));
                storeIndex.setEnteringRate(resultSet.getFloat("entering_rate"));
                storeIndex.setOldNum(resultSet.getFloat("old_num"));
                storeIndex.setStayRate(resultSet.getFloat("stay_rate"));
                return storeIndex;
            }
        });
        logger.info("get Raw Data from t_common_stat_for_predict, success");
        return storeIndexeList;
    }

 	public static float getArrayAvg(float[] valueList){
		//计算数组的平均值。只截取排序后中间的1/3值来计算平均值
		if(valueList.length==0)return 0;
		else if (valueList.length==1)return valueList[0];
		else if(valueList.length==2)return (valueList[0] + valueList[1]) / 2f;
		float[] tmpList = valueList.clone();
		Tools.quick_sort(tmpList);
		float avg = 0;
		int len = valueList.length;
		int l = len/3,r = len-l;
		for(int i = l;i < r;i++){
			avg += tmpList[i];
		}
		avg /= (float)(r-l);
		return avg;
	}

}
