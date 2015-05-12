package com.ruijie.main;

import com.ruijie.dataInitialize.FactorQuantify;
import com.ruijie.dataInitialize.Tools;
import com.ruijie.ncl.*;
import com.ruijie.pojo.DateFeature;
import com.ruijie.pojo.PredictOutIndex;
import com.ruijie.pojo.RecentIndexValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RunBPBAK {

	private static Logger logger = LogManager.getLogger(RunBPBAK.class);
	private JdbcTemplate jdbcTemplate;
	private FactorQuantify factor;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setFactor(FactorQuantify factor) {
		this.factor = factor;
	}

	/**
	 * @param args
	 * @throws java.io.IOException
	 */

	public static void main(String[] args) throws IOException{
		//ConfigHandler config = new ConfigHandler();
		//config.init("rbis_predict.cfg");
		//config.init("E:\\IdeaProjects\\project\\rbis_dm\\conf\\rbis_predict.cfg");
		//run(config,"2014-03-11",15);
	}
	public void run(String fromDateStr,int numDays) throws IOException {
		// TODO 
		factor.init();
		int preditBuilding = factor.getPredictBuilding();
		int predictStore = factor.getPredictStore();

		//获取building, floor, store 信息的sql
		String sqlLocation = "";
		//获取各个天的天气和节假日信息的sql
		String sqlDateFeature = "";
		if(preditBuilding == -1){
			logger.info("Predict for all buildings...");
			sqlDateFeature = "select * from " +
					" (SELECT building_id as buildingID, region_id from t_buildings_info) t0 " +
					" left OUTER JOIN " +
					" (SELECT RegionID as regionID, DATE_FORMAT(date, '%Y-%m-%d') as date, weather_1, weather_2, temp_h, temp_l, aqi, " +
					" wind, today, tomorrow, festival, day_in_holiday, num_days  from t_date_features) t1" +
					" on t1.regionID=t0.region_id";
			//判断是否预测storeid
			if(predictStore==0){
				sqlLocation = "select distinct building_id,floor_id,store_id from t_common_stat_for_predict " +
						" where building_id>0 and hour=24 and store_id=-1";
			}else{
				sqlLocation = "select distinct building_id,floor_id,store_id from t_common_stat_for_predict " +
						" where building_id>0 and hour=24";
			}

		} else{
			logger.info("Predict for buildings: " + preditBuilding + "...");
			sqlDateFeature = " SELECT RegionID, DATE_FORMAT(date, '%Y-%m-%d') as date, " + preditBuilding + " as building_id, " +
					"weather_1, weather_2, temp_h, temp_l, aqi, wind, today, tomorrow, festival, day_in_holiday, num_days " +
					"from t_date_features t " +
					"where RegionID in (select RegionID from t_buildings_info where building_id=" + preditBuilding + ")";

			//判断是否预测storeid
			if(predictStore==0){
				sqlLocation = "select distinct building_id,floor_id,store_id from t_common_stat_for_predict " +
						" where building_id=" + preditBuilding +" and hour=24 and store_id=-1";
			}else{
				sqlLocation = "select distinct building_id,floor_id,store_id from t_common_stat_for_predict " +
						" where building_id=" + preditBuilding + " and hour=24";
			}
		}

		//获取dateFeature数据
		List<DateFeature> dateFeatureList = jdbcTemplate.query(sqlDateFeature, new Object[]{}, new RowMapper<DateFeature>() {
			@Override
			public DateFeature mapRow(ResultSet resultSet, int i) throws SQLException {
				DateFeature dateFeature = new DateFeature();
				dateFeature.setRegionID(resultSet.getInt("regionID"));
				dateFeature.setBuildingID(resultSet.getInt("buildingID"));
				dateFeature.setDate(resultSet.getString("date"));
				dateFeature.setWeatherBegin(resultSet.getFloat("weather_1"));
				dateFeature.setWeatherEnd(resultSet.getFloat("weather_2"));
				dateFeature.setTempHigh(resultSet.getInt("temp_h"));
				dateFeature.setTempLow(resultSet.getInt("temp_l"));
				dateFeature.setAqi(resultSet.getInt("aqi"));
				dateFeature.setWind(resultSet.getFloat("wind"));
				dateFeature.setToday(resultSet.getInt("today"));
				dateFeature.setTomorrow(resultSet.getInt("tomorrow"));
				dateFeature.setFestival(resultSet.getInt("festival"));
				dateFeature.setDayInHoliday(resultSet.getInt("day_in_holiday"));
				dateFeature.setSumDays(resultSet.getInt("num_days"));
				return dateFeature;
			}
		});
		//用于存储region中各个data所对应的dataFeature值
		//暂时不用aqi数据
		HashMap<Integer,HashMap<String,float[]> > buildingDate2allFeature
				= new HashMap<Integer,HashMap<String,float[]> >();
		HashMap<String,float[]> date2allFeature = null;
		for(DateFeature d: dateFeatureList){
			int buildingTmp = d.getBuildingID();

			String date = d.getDate();
			float[] features = new float[12];
			features[0] = d.getWeatherBegin();
			features[1] = d.getWeatherEnd();
			features[2] = d.getTempHigh();
			features[3] = d.getTempLow();
			features[4] = d.getWind();
			features[5] = d.getToday();
			features[6] = d.getTomorrow();
			features[7] = d.getFestival();
			features[8] = d.getDayInHoliday();
			features[9] = d.getSumDays();

			if(buildingDate2allFeature.containsKey(buildingTmp)){
				date2allFeature = buildingDate2allFeature.get(buildingTmp);
				date2allFeature.put(date, features);
			}else{
				date2allFeature = new HashMap<String,float[]>();
				date2allFeature.put(date, features);
			}
			buildingDate2allFeature.put(buildingTmp, date2allFeature);
		}
		//获取需要预测的building，floor，store
		List<int[]> locationList = jdbcTemplate.query(sqlLocation, new Object[]{}, new RowMapper<int[]>() {
			@Override
			public int[] mapRow(ResultSet resultSet, int i) throws SQLException {
				int[] location = new int[3];
				location[0] = resultSet.getInt("building_id");
				location[1] = resultSet.getInt("floor_id");
				location[2] = resultSet.getInt("store_id");
				return location;

			}
		});

/*		//遍历各个building, floor, store进行预测
		for(int[] location:locationList) {
			int buildingID = location[0];
			int floorID = location[1];
			int storeID = location[2];

			HashMap<String, float[]> mapDate2allFeature = buildingDate2allFeature.get(buildingID);

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：小时");
			trainAndPredict(FactorQuantify.ALLNUM, buildingID, floorID,
					storeID, true, mapDate2allFeature, fromDateStr, numDays);

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：天");
			//字典传入函数,值会变化,进行重新赋值
			mapDate2allFeature = buildingDate2allFeature.get(buildingID);
			trainAndPredict(FactorQuantify.ALLNUM, buildingID, floorID,
					storeID, false, mapDate2allFeature, fromDateStr, numDays);

		}*/

		//并行运行：日客流量、时段客流量、日驻留时长、时段驻留时长、日进店率、时段进店率、老顾客数、驻留率
		RunParallel rp_allNum_allday = new RunParallel();
		RunParallel rp_allNum_hourly = new RunParallel();
		RunParallel rp_duration_allday = new RunParallel();
		//RunParallel rp_duration_hourly = new RunParallel();
		RunParallel rp_enterRate_allday = new RunParallel();
		//RunParallel rp_enterRate_hourly = new RunParallel();
		RunParallel rp_oldNum_allday = new RunParallel();
		RunParallel rp_stayRate_allday = new RunParallel();

		//遍历各个building, floor, store进行预测
		for(int[] location:locationList){
			int buildingID = location[0];
			int floorID = location[1];
			int storeID = location[2];

			HashMap<String,float[]> mapDate2allFeature = buildingDate2allFeature.get(buildingID);

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：天,预测指标:all_num");
			rp_allNum_allday.init(FactorQuantify.ALLNUM, buildingID, floorID,
					storeID, false, mapDate2allFeature, fromDateStr, numDays);

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：小时,预测指标:all_num");
			//字典传入函数,值会变化,进行重新赋值
			mapDate2allFeature = buildingDate2allFeature.get(buildingID);
			rp_allNum_hourly.init(FactorQuantify.ALLNUM, buildingID, floorID,
					storeID, true,mapDate2allFeature, fromDateStr, numDays);
			Thread thread_allNum_allday = new Thread(rp_allNum_allday);
			thread_allNum_allday.start();
			Thread thread_allNum_hourly = new Thread(rp_allNum_hourly);
			thread_allNum_hourly.start();

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：天,预测指标:old_num");
			mapDate2allFeature = buildingDate2allFeature.get(buildingID);
			rp_oldNum_allday.init(FactorQuantify.OLDNUM, buildingID, floorID,
					storeID, false,mapDate2allFeature, fromDateStr, numDays);
			Thread thread_oldNum_allday = new Thread(rp_oldNum_allday);
			thread_oldNum_allday.start();

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：天,预测指标:stay_rate");
			mapDate2allFeature = buildingDate2allFeature.get(buildingID);
			rp_stayRate_allday.init(FactorQuantify.STAYRATE, buildingID, floorID,
					storeID, false, mapDate2allFeature, fromDateStr, numDays);
			Thread thread_stayRate_allday = new Thread(rp_stayRate_allday);
			thread_stayRate_allday.start();

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：天,预测指标:duration");
			mapDate2allFeature = buildingDate2allFeature.get(buildingID);
			rp_duration_allday.init(FactorQuantify.DURATION, buildingID, floorID,
						storeID, false,mapDate2allFeature, fromDateStr, numDays);
			//rp_duration_hourly.init(FactorQuantify.DURATION, buildingID, floorID,
			//			storeID, true,mapDate2allFeature, fromDateStr, numDays);

			logger.info("location:" + buildingID + "|" + floorID + "|" + storeID + ",预测类别：天,预测指标:enter_rate");
			mapDate2allFeature = buildingDate2allFeature.get(buildingID);
			rp_enterRate_allday.init(FactorQuantify.ENTERRATE, buildingID, floorID,
						storeID, false,mapDate2allFeature, fromDateStr, numDays);

			//rp_enterRate_hourly.init(FactorQuantify.ENTERRATE, buildingID, floorID,
			//			storeID, true,mapDate2allFeature, fromDateStr, numDays);

			Thread thread_duration_allday = new Thread(rp_duration_allday);
			thread_duration_allday.start();
			//Thread thread_duration_hourly = new Thread(rp_duration_hourly);
			//thread_duration_hourly.start();
			Thread thread_enterRate_allday = new Thread(rp_enterRate_allday);
			thread_enterRate_allday.start();
			//Thread thread_enterRate_hourly = new Thread(rp_enterRate_hourly);
			//thread_enterRate_hourly.start();

			try {
				thread_duration_allday.join();
    			//thread_duration_hourly.join();
    			thread_enterRate_allday.join();
    			//thread_enterRate_hourly.join();
    			thread_oldNum_allday.join();
    			thread_stayRate_allday.join();
    			thread_allNum_allday.join();
				thread_allNum_hourly.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void trainAndPredict(int predictType,int buildingID, int floorID,int storeID, boolean hourly,
			HashMap<String,float[]> date2allFeatures, String fromDateStr,int numDays) throws IOException{

		//预测的地点，如哪个商城，哪个楼层，哪个店铺
		String index = "";
		String recent30index = "";
		String recent7index = "";
		String paraStr = "";
		factor.init();
		Map<String, String> predictArg = factor.predictQuantify;
		switch(predictType){
			case FactorQuantify.ALLNUM:
				index = "all_num";
				recent30index = "recent30all_num";
				recent7index = "recent7all_num";
				paraStr = hourly==false?predictArg.get("AllNumDayPara"):predictArg.get("AllNumHourPara");
				break;
			case FactorQuantify.DURATION:
				index = "avg_stay_time";
				recent30index = "recent30avg_stay_time";
				recent7index = "recent7avg_stay_time";
				paraStr = hourly==false?predictArg.get("DurationDayPara"):predictArg.get("DurationHourPara");
				break;
			case FactorQuantify.ENTERRATE:
				index = "entering_rate";
				recent30index = "recent30entering_rate";
				recent7index = "recent7entering_rate";
				paraStr = hourly==false?predictArg.get("EnterRateDayPara"):predictArg.get("EnterRateHourPara");
				break;
			case FactorQuantify.OLDNUM:
				index = "old_num";
				recent30index = "recent30old_num";
				recent7index = "recent7old_num";
				paraStr = hourly==false?predictArg.get("OldNumDayPara"):predictArg.get("OldNumHourPara");
				break;
			case FactorQuantify.STAYRATE:
				index = "stay_rate";
				recent30index = "recent30stay_rate";
				recent7index = "recent7stay_rate";
				paraStr = hourly==false?predictArg.get("StayRateDayPara"):predictArg.get("StayRateHourPara");
				break;
			default:
				index = "all_num";
				recent30index = "recent30all_num";
				recent7index = "recent7all_num";
				paraStr = hourly==false?predictArg.get("AllNumDayPara"):predictArg.get("AllNumHourPara");
				break;
		}

		//获取近30和近7日的指标，插入到date2allFeature
		logger.info("获取近30和近7日的指标，插入到date2allFeature");
		String indexSql = "select DATE_FORMAT(date, '%Y-%m-%d') as date," + recent30index + " as recent30Value," + recent7index
					+ " as recent7Value from t_common_stat_recent where building_id=? and floor_id=? and store_id=?";
		List<RecentIndexValue> recentIndexValueList = jdbcTemplate.query(indexSql, new Object[]{buildingID, floorID, storeID}, new RowMapper<RecentIndexValue>() {
			@Override
			public RecentIndexValue mapRow(ResultSet resultSet, int i) throws SQLException {
				RecentIndexValue recentIndexValue = new RecentIndexValue();
				recentIndexValue.setDate(resultSet.getString("date"));
				recentIndexValue.setRecent30Value(resultSet.getFloat("recent30Value"));
				recentIndexValue.setRecent7Value(resultSet.getFloat("recent7Value"));
				return recentIndexValue;
			}
		});
/*		for(Map.Entry<String, float[]> m: date2allFeatures.entrySet()){
			logger.info("key:" + m.getKey());
			float[] val = m.getValue();
			logger.info("length:" + val.length);
			String v="";
			for(float f:val){
				v+=f + ",";
			}
			logger.info("val:" + v);

		}*/
		for(RecentIndexValue r: recentIndexValueList){
			String date = r.getDate();
			if(date2allFeatures.containsKey(date)){
				float[] features = date2allFeatures.get(date);
				features[10] = r.getRecent30Value();
				features[11] = r.getRecent7Value();
				date2allFeatures.put(date, features);
			}
		}

		//获取各个天的指标值
		//商场日客流量小于一定值时，认为整体数据无效。设为10个
		logger.info("获取各个指标实际统计值,插入到date2outputs");
		String sqlIndexValue = "";
		if(hourly){
			sqlIndexValue = " SELECT DATE_FORMAT(date, '%Y-%m-%d') as date, hour, " + index + " as indexValue from t_common_stat_for_predict " +
					" where hour<24 and building_id=? and floor_id=? and store_id=? and date in " +
					" (SELECT date from t_common_stat_for_predict where hour=24 and building_id=? and floor_id=-1 and all_num>10)";
		}else{
			sqlIndexValue = " SELECT DATE_FORMAT(date, '%Y-%m-%d') as date, hour, " + index + " as indexValue from t_common_stat_for_predict " +
					" where hour=24 and building_id=? and floor_id=? and store_id=? and date in " +
					" (SELECT date from t_common_stat_for_predict where hour=24 and building_id=? and floor_id=-1 and all_num>10)";
		}
		List<PredictOutIndex> predictOutIndexList = jdbcTemplate.query(sqlIndexValue, new Object[]{buildingID, floorID, storeID, buildingID},
				new RowMapper<PredictOutIndex>() {
					@Override
					public PredictOutIndex mapRow(ResultSet resultSet, int i) throws SQLException {
						PredictOutIndex predictOutIndex = new PredictOutIndex();
						predictOutIndex.setDate(resultSet.getString("date"));
						predictOutIndex.setHour(resultSet.getInt("hour"));
						predictOutIndex.setIndex(resultSet.getFloat("indexValue"));
						return predictOutIndex;
					}
				});

		//用于保存每天实际指标值, 数据结构为{date: float[]}
		//当为小时时，float[24]保存每个小时值，当为天时,float[1]保存每天数据
		logger.info("用于保存每天实际指标值, 数据结构为{date: float[]}");
		HashMap<String,float[]> date2outputs = new HashMap<String,float[]>();
		float[] outputs = null;
		for(PredictOutIndex p: predictOutIndexList){
			String date = p.getDate();
			int hour = p.getHour();
			float i = p.getIndex();
			if(date2outputs.containsKey(date) && hour<24){
				outputs = date2outputs.get(date);
				outputs[hour] = i;
			}else{
				if(hour==24){
					outputs = new float[1];
					outputs[0] = i;
				}else{
					outputs = new float[24];
					outputs[hour] = i;
				}
			}
			date2outputs.put(date, outputs);
		}

		//当天数少于7天时，不进行预测。
		if(date2outputs.size()<7){
			logger.info("数据条目少于7天，不进行预测");
			logger.error("Not enough training data: " + " indexType=" + index + " floor_id=" + floorID
					+ " store_id=" + storeID + " hourly=" + hourly);
			return;
		}

		logger.info("神经网络训练及预测过程");
		//===以下是训练及预测过程
		//将从DB获取到的数据转化成训练用的标准格式
		Dataset traindata = new Dataset();
		traindata.initFromMap(date2allFeatures, date2outputs);
		int numNet = 5;//神经网络个数
		float lambda = 0.9f;//负相关参数，范围[0,1]，一般不用改
		float alpha = 0.01f;//学习速率
		int numHid = 15;//隐层节点数
		int epoch = 5000;//迭代次数
		//以上是默认参数，若在配置文件里设置了参数，则在以下代码按照配置文件设置。
		String[] paraList = paraStr.split(",");
		if(paraList.length == 4){
			numNet = Integer.valueOf(paraList[0]);
			alpha = Float.valueOf(paraList[1]);
			numHid = Integer.valueOf(paraList[2]);
			epoch = Integer.valueOf(paraList[3]);
		}

		//按一定规则将输出值做个变换，使得范围在[0,1]，因为模型的输出范围是[0,1]，后续对预测值的输出要做反变换。
		float scale = traindata.reScaleOutputs();
		traindata.normalize();//归一化数据

		Parameters para = new Parameters();
		//设定参数
		para.setAllParameters(alpha,lambda,numNet,epoch,numHid);
		para.setStripSize(epoch<10?5:(epoch/5));//设定迭代过程中，中间结果的输出频率
		//设定多个神经网络的集成方式，这里设定为平均，不需要修改。
		CombinationScheme combineScheme = CombinationScheme.createCombinationScheme("avg");
		Ensemble ens = new Ensemble(para.getNoOfNet(),combineScheme);//得到一个神经网络集成对象
		//以下三个是初始化隐层权值、输出层权值、偏置值的范围，不需要修改
		float rangeHidW = 0.01f;//
		float rangeOutW = 0.01f;//
		float rangeB = 0.01f;
			
		//设定问题类型，在输入日志中使用
		String showTypeInLog = index + (hourly?" - everyhour":" - allday");
		para.setPredictType(showTypeInLog);
			
		try{
			//创建神经网络集成
			ens.createEnsemble(traindata.getNoOfInputs(), para.getNoOfHidNodes(),
					traindata.getNoOfClasses(), Node.LOGISTIC, Node.LOGISTIC);
		}catch(UnknownNodeFunction UNF){
			Tools.ERROUT("[" + showTypeInLog + "]Unknow Node Function!");
			System.exit(1);
		}
		ens.initialEnsemble(rangeHidW, rangeOutW, rangeB);//初始化模型
		ens.learnBatchMode(traindata.clone(), para);//训练模型
		//ens.learn(traindata.clone(), para);

		//获取归一化的相关参数，用于对测试数据也进行统一规则的归一化过程
		float[] max4normalize = traindata.getMax();
		float[] min4normalize = traindata.getMin();

		Calendar cal = Calendar.getInstance();
		if(!fromDateStr.equals("")){
			SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
			try{
				Date date= myFormatter.parse(fromDateStr);
				cal.setTime(date);
			}catch(ParseException e){
				logger.error("[WARNING][" + showTypeInLog + "]Parse date error! Default: predict from today.");
			}
		}

		//获取预测数据，并插入数据表中
		logger.info("获取预测数据，并插入数据表中");
		String tmpDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		logger.info("[" + showTypeInLog + "]Predict from " + tmpDateStr + ": " + numDays + "days.");
		for(int kk = 0;kk < numDays; kk++){
			tmpDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
			if(date2allFeatures.containsKey(tmpDateStr)){
				float[] input = Dataset.normalize
						(date2allFeatures.get(tmpDateStr),max4normalize,min4normalize);
				float[] output = ens.getOutputs(input);//从待预测的数据的输入来获取模型的输出
				if(output.length == 24){
					for(int ii = 0;ii < output.length;ii++){
						//从模型直接得到的输出是处理过的，范围是[0,1]，应该转换成正常的值(比如客流量都是比较大的数值)
						float predictValue = output[ii] * scale;
						//写到DB的结果表里
						String updateSql =
								"insert into t_predict_result(date,hour,building_id,floor_id,store_id," + index
								+ ")values(\"" + tmpDateStr +"\"," + ii + "," + buildingID
								+ "," + floorID + "," + storeID + "," + predictValue + ") "
								+ "on duplicate key update " + index + "=" + predictValue;

						jdbcTemplate.update(updateSql);
					}
				}else if(output.length == 1){
					float predictValue = output[0] * scale;
					String updateSql =
							"insert into t_predict_result(date,hour,building_id,floor_id,store_id," + index
							+ ")values(\"" + tmpDateStr +"\",24," + buildingID
							+ "," + floorID + "," + storeID + "," + predictValue + ") "
							+ "on duplicate key update " + index + "=" + predictValue;
					jdbcTemplate.update(updateSql);
				}else{
					logger.error("[WARNING][" + showTypeInLog + "]Output length illegal!");
				}
			}
				cal.add(Calendar.DATE, 1);
		}

		cal.add(Calendar.DATE, -1);
		tmpDateStr = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		logger.info("[" + showTypeInLog + "]End date: " + tmpDateStr);
	}
	
	public class RunParallel implements Runnable{

		//private ConfigHandler m_config;
		private int m_predictType;
		private int m_building_id;
		private int m_floor_id;
		private int m_store_id;
		private boolean m_hourly;
		private HashMap<String,float[]> m_date2allFeatures;
		private String m_fromDateStr;
		private int m_numDays;
		
		public RunParallel(){
		}
		
		public void init(
				int predictType, int building_id, int floor_id,int store_id,boolean hourly,
				HashMap<String,float[]> date2allFeatures,
				String fromDateStr,int numDays){
			m_predictType = predictType;
			m_building_id = building_id;
			m_floor_id = floor_id;
			m_store_id = store_id;
			m_hourly = hourly;
			m_date2allFeatures = date2allFeatures;
			m_fromDateStr = fromDateStr;
			m_numDays = numDays;
		}
		
		@Override
		public void run() {
			try {
				//训练模型，并对未知日期做出预测
				trainAndPredict(
					m_predictType,m_building_id, m_floor_id,m_store_id,m_hourly,
					m_date2allFeatures,m_fromDateStr,m_numDays);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
