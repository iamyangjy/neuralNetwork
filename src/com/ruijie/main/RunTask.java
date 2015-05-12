package com.ruijie.main;

/**
 * Created by OA on 2015/5/6.
 */

//import com.ruijie.dataInitialize.ConfigHandler;
import com.ruijie.dataInitialize.DateInfo;
import com.ruijie.dataInitialize.StoreIndexStat;
import com.ruijie.dataInitialize.FactorQuantify;
import com.ruijie.dataInitialize.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ruijie.pojo.StoreIndex;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.jdbc.core.JdbcTemplate;

public class RunTask {

	private static Logger logger = LogManager.getLogger(RunTask.class);
	private JdbcTemplate jdbcTemplate;
	private DateInfo dateInfo;
	private StoreIndexStat storeIndexStat;
	private RunBP runBP;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setDateInfo(DateInfo dateInfo) {
		this.dateInfo = dateInfo;
	}

	public void setStoreIndexStat(StoreIndexStat storeIndexStat) {
		this.storeIndexStat = storeIndexStat;
	}

	public void setRunBP(RunBP runBP) {
		this.runBP = runBP;
	}

	public void predictSpecifyDate(String fromDateStr,int numDays){
		/**
		 * 前提是所有历史数据都已处理
		 */
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String todayDateStr = myFormatter.format(cal.getTime());
		try{
			Date date= myFormatter.parse(fromDateStr);
			cal.setTime(date);
		}catch(ParseException e){
			logger.error("[ERROR]Parse date error in Run.predictSpecifyDate.");
			return;
		}
		cal.add(Calendar.DATE, numDays);
		Date toDate = cal.getTime();
		String toDateStr = myFormatter.format(cal.getTime());

		//从当天往后numDays天的日期属性处理
		//从当天往后numDays天的日期属性处理
		dateInfo.processData("", fromDateStr, toDateStr);
		//指定日期的近期统计值处理(若是当天，则处理包含从当天算起15天后)
		if(Calendar.getInstance().before(cal)){
			System.err.println("process today recent values.");
			//com.ruijie.dataInitialize.RecentValue.getRecentValues_oneday(config, todayDateStr);
			storeIndexStat.processDataForPredict(toDate);
			//com.ruijie.dataInitialize.StoreIndexStat.processDataForPredict(todayDateStr);
		}
		try {
			runBP.run(fromDateStr,numDays);
		} catch (IOException e) {
			System.err.println("[ERROR]Exception catched when training and predicting.");
			e.printStackTrace();
		}
	}

	public void runDaily(){

		/**
		 * 1、量化日期特性
		 * 2、处理近期统计值
		 * 3、训练模型并预测
		 */
		Calendar cal = Calendar.getInstance();
		String todayDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		cal.add(Calendar.DATE, -1);
		String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		cal.add(Calendar.DATE, 1);
		String fromDateStr = todayDateStr;
		cal.add(Calendar.DATE, 15);
		Date toDate = cal.getTime();
		String toDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		//调用存储过程，跑昨天的数据的统计
		//callCommonStatProcOneday(config,yesterdayStr);
		//从当天往后15天的日期属性处理
		dateInfo.processData("", fromDateStr, toDateStr);
		//当天的近期统计值处理(含15天后)
		storeIndexStat.processDataForPredict(toDate);
		//训练模型并预测
		try {
			runBP.run("",15);
		} catch (IOException e) {
			logger.info("[ERROR]Exception catched when training and predicting.");
			e.printStackTrace();
		}
	}

	public void processHistoryData(String fromDateStr){
		/**
		 * 1、量化日期特性
		 * 2、处理近期统计值
		 */
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		Date toDate = cal.getTime();
		String todayDateStr = simpleDateFormat.format(toDate);
		String toDateStr = todayDateStr;
		//解析起始日期
		Date fromDate =new Date();
		try{

			fromDate = simpleDateFormat.parse(fromDateStr);
		}catch (ParseException e){
			logger.error("parse fromDateStr error");
			e.printStackTrace();
		}

		//从指定日期到当天为止的所有有日期属性处理
		dateInfo.processData("", fromDateStr, toDateStr);
		//所有历史数据的近期统计值处理
		cal.setTime(fromDate);
		for(Date from = cal.getTime(); from.getTime()<=toDate.getTime(); ){
			logger.info("run date:" + simpleDateFormat.format(from));
			storeIndexStat.processDataForPredict(from);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			from = cal.getTime();
		}
	}

	public void updateLogTable(String tableName, String date, boolean isupdate) {
        logger.info("update t_pro_execute_log, start...");

        String sql = null;
        if (isupdate) {
            sql = " UPDATE t_pro_execute_log set run_state = 1, cost_time = TIME_TO_SEC(NOW()) - TIME_TO_SEC(start_time) " +
                    " WHERE pro_name =? and refer_time =?  ORDER BY id DESC LIMIT 1";
        } else {
            sql = "insert into t_pro_execute_log(pro_name, refer_time, start_time, cost_time, content, frequency, run_state) " +
                    " VALUES (?, ?, CURRENT_TIMESTAMP(), NULL, '客流预测', 'sometime', 0)";
        }

        jdbcTemplate.update(sql, new Object[]{tableName, date});
        logger.info("update t_pro_execute_log, end...");
    }

}
