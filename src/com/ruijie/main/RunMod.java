package com.ruijie.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RunMod {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//加载配置文件
		//配置log4 2.0配置文件
        ConfigurationSource source;
        try{
            //File config=new File("E:\\IdeaProjects\\project\\alg_jar\\conf\\log4j2.xml");
            File config=new File("conf/log4j2.xml");
            source=new ConfigurationSource(new FileInputStream(config),config);
            Configurator.initialize(null, source);
        } catch (Exception e){
            e.printStackTrace();
        }
        //
        Logger logger = LogManager.getLogger(RunMod.class);

		logger.info("load spring configuration");
        //String[] path = {"E:\\IdeaProjects\\project\\alg_jar\\conf\\applicationContext.xml"};
        //ApplicationContext context = new FileSystemXmlApplicationContext(path);
        ApplicationContext context = new ClassPathXmlApplicationContext("conf/applicationContext.xml");
        logger.info("load spring configuration, success");
		//获取各个bean
		RunTask runTask = (RunTask) context.getBean("runTask");


		logger.info("Begin.");
		String usage = "Program arguments:\n0: Start for processing historic data from 2014-08-01\n"
				+ "0 date: Start for processing historic data from specify date\n"
				+ "1: Start for daily running\n"
				+ "1 date numDays: Predict specify dates and the number of days";


		if(args.length == 0 || args.length > 3){
			System.out.println(usage);
		}else{
			//获取当前时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String today = simpleDateFormat.format(new Date());
            String tableName = "t_predict_result";
    		//ConfigHandler config = new ConfigHandler();
    		//config.init("conf/rbis_predict.cfg");
			//运行前插入日志
			logger.info("test");
			runTask.updateLogTable(tableName, today, false);

			if(args[0].equals("0")){
				if(args.length == 1){
					String fromDateStr = "2014-08-01";
					logger.info("Process historic data from default date: " + fromDateStr);
					runTask.processHistoryData(fromDateStr);

					//运行结束插入日志
					runTask.updateLogTable(tableName, today, true);
				}else if (args.length == 2){
					String fromDateStr = args[1];
					logger.info("Process historic data from date: " + fromDateStr);
					runTask.processHistoryData(fromDateStr);

					//运行结束插入日志
					runTask.updateLogTable(tableName, today, true);
				}else{
					System.out.println(usage);
				}
				//runDaily();
			}else if(args[0].equals("1")){
				if(args.length == 1){
					runTask.runDaily();

					//运行结束插入日志
					runTask.updateLogTable(tableName, today, true);
				}else if(args.length == 3){
					String fromDateStr = args[1];
					int numDays = Integer.valueOf(args[2]);
					logger.info("Predict from date: " + fromDateStr + ", " + numDays + "days.");
					runTask.predictSpecifyDate(fromDateStr, numDays);

					//运行结束插入日志
					runTask.updateLogTable(tableName, today, true);
				}else{
					System.out.println(usage);
				}
			}else{
				System.out.println(usage);
			}
		}
		logger.info("End.");
	}

}
