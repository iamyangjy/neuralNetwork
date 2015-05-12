package com.ruijie.dataInitialize;

/**
 * Created by OA on 2015/4/14.
 */
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.ruijie.pojo.DateFeature;

public class DateInfo {

	private static Logger logger = LogManager.getLogger(DateInfo.class);
    private JdbcTemplate jdbcTemplate;
	private FactorQuantify factorQuantify;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setFactorQuantify(FactorQuantify factorQuantify) {
		this.factorQuantify = factorQuantify;
	}

	public void processData(String buildingList, String beginDate, String endDate){

		if(buildingList.equals("")){
			logger.info("Processing date data of all buildings...");
		}else{
			logger.info("Processing date data of building ids: " + buildingList + "...");
		}

		//final DataQuantify dq = new DataQuantify();
		logger.info("weather, holiday quantify initilize");
		factorQuantify.init();

		String getWeatherSql = "select * from t_weather where date>=? and date<=?";
		if(!buildingList.equals("")){
			getWeatherSql += " and regionID in (select region_id from t_buildings_info "
						+ "where building_id in (" + buildingList + "))";
		}

		logger.info("get dateInfo ");
		final List<DateFeature> dateFeatureList = jdbcTemplate.query(getWeatherSql, new Object[]{beginDate, endDate}, new RowMapper<DateFeature>() {
			@Override
			public DateFeature mapRow(ResultSet resultSet, int i) throws SQLException {
				DateFeature dateFeature = new DateFeature();
				dateFeature.setDate(resultSet.getString("date"));
				dateFeature.setRegionID(resultSet.getInt("RegionID"));
				dateFeature.setWeatherBegin(factorQuantify.getWeatherQuantify(resultSet.getString("weather_1")));
				dateFeature.setWeatherEnd(factorQuantify.getWeatherQuantify(resultSet.getString("weather_2")));
				dateFeature.setTempHigh(resultSet.getInt("temp_h"));
				dateFeature.setTempLow(resultSet.getInt("temp_l"));
				dateFeature.setAqi(resultSet.getInt("aqi"));
				dateFeature.setWind(resultSet.getFloat("wind"));
				int[] dateValue = factorQuantify.getHolidayQuantify(resultSet.getString("date"));
				dateFeature.setToday(dateValue[0]);
				dateFeature.setTomorrow(dateValue[1]);
				dateFeature.setFestival(dateValue[2]);
				dateFeature.setDayInHoliday(dateValue[3]);
				dateFeature.setSumDays(dateValue[4]);
				return dateFeature;
			}
		});

		logger.info("insert into dateInfo");
		String updateSql = "replace into t_date_features values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
				DateFeature dateFeature = dateFeatureList.get(i);
				preparedStatement.setString(1, dateFeature.getDate());
				preparedStatement.setInt(2, dateFeature.getRegionID());
				preparedStatement.setFloat(3, dateFeature.getWeatherBegin());
				preparedStatement.setFloat(4, dateFeature.getWeatherEnd());
				preparedStatement.setInt(5, dateFeature.getTempHigh());
				preparedStatement.setInt(6, dateFeature.getTempLow());
				preparedStatement.setInt(7, dateFeature.getAqi());
				preparedStatement.setFloat(8, dateFeature.getWind());
				preparedStatement.setInt(9, dateFeature.getToday());
				preparedStatement.setInt(10, dateFeature.getTomorrow());
				preparedStatement.setInt(11, dateFeature.getFestival());
				preparedStatement.setInt(12, dateFeature.getDayInHoliday());
				preparedStatement.setInt(13, dateFeature.getSumDays());
			}

			@Override
			public int getBatchSize() {
				return dateFeatureList.size();
			}
		});
	}

	//获取某个时间段内的节假日的信息
	public Map<String, int[]> getFestivalInfo(String beingDate, String endDate){
		//用字典来存储，
		//int[]有5个元素，分别表示:
        //1、今天是否休息日，明天是否休息日，今天是什么节日，今天是法定节假日第几天，今天所处法定假日一共几天。
        //(量化标准：0为否，1为是；
        // 节日: 0(无)，2(除夕)，4(春节)，6(元旦)，8(国庆)，10(中秋)，12(情人节)，14(端午), 15(其他小长假))。

		//判断是否为公历节假日
		//判断是否是农历节假日
		//判断是否为周末

		return null;
	}


}
