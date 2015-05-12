package com.ruijie.ncl;

import com.ruijie.dataInitialize.Tools;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class NCL implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void learn(Ensemble ens, Dataset data, Parameters para) {
			//all NNs will be updated 
			
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		int stripSize = para.getStripSize();
		para.initExtraAlpha(data.getNoOfClasses());
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
		float avgMSE = ens.getAvgMSE(data,para);
		float errRate = ens.getErrorRate(data);			
		String printOut = "Init " + "MSE: " + df.format(avgMSE) + "\terrorRate: " + df.format(errRate);
		System.out.println(printOut);
		
		//data.disorder();
		for(int e = 1;e <= para.getEpochs();e++){
			data.disorder();
			for(int d = 0;d < data.getNoOfSamples();d++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				para.resetExtraAlpha();
				
				AVG avg = new AVG();
				/*
				 * parallel
				 */
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
			}
			if(e % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				errRate = ens.getErrorRate(data);				
				
				printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\terrorRate: " + df.format(errRate);
				System.out.println(printOut);	
				//if(errRate < 0.01f)return;
			}	
		}
	}
	
	public static void learnSortByMargin(Ensemble ens, Dataset data, Parameters para,
				float[] auc, float[] mse, 	float[] testAUC, Dataset testdata) {
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
		
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = ens.calculateAUC(testdata);
		
		
		for(int e = 1;e <= para.getEpochs();e++){
			int[] sequenceIndex = data.getSeqSortByMargin(ens);
			for(int d = data.getNoOfSamples() / 2;d < data.getNoOfSamples();d++){
				int index = sequenceIndex[d];
				float[] inputs = data.getInputs(index);
				float[] targets = data.getTargets(index);
				
				para.resetExtraAlpha();
				
				AVG avg = new AVG();
				/*
				 * parallel
				 */
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
			}
			if(e % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
			
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);

				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
				
			}	
			
		}
	}
	
	public static void learnSBP(Ensemble ens, Dataset data, Parameters para,
				float[] auc, float[] mse,float[] testAUC, Dataset testdata) {
	
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	//	float errRate = ens.getErrorRate(data);			
	//	String printOut = "Init " + "\terrorRate: " + df.format(errRate);
	//	System.out.println(printOut);
		
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		
		
		//data.disorder();
		float avgMSE = ens.getAvgMSE(data, para);
		
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = ens.calculateAUC(testdata);
		
		
		for(int e = 1;e <= para.getEpochs();e++){
			data.disorder(0);
			for(int d = 0;d < data.getNoOfSamples();d++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				para.resetExtraAlpha();
				
				AVG avg = new AVG();
				
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				
				
				
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
			}
			if(e % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
				
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
				
			}	
			
		}
		
	}
	
	/*public static void learnDys(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse,float[] testAUC, Dataset testdata) {
		
		 // using an epoch is a sample used to train 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		AVG avg = new AVG();
		
		//parallel 
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		int[] count = new int[ens.size()];
		boolean[] noNeedToTrain = new boolean[ens.size()];
		for(int e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data.disorder(0);
			}
			
			float[] inputs = data.getInputs(d);
			float[] targets = data.getTargets(d);
			
			int comN = 0;
			for(int n = 0;n < ens.size();n++){
				if(noNeedToTrain[n] == true){
					comN++;
					continue;
				}
				int prediction = ens.getNetwork(n).getPrediction(inputs);
				if(	targets[prediction]==1){
					if(count[n]++ < data.getNoOfSamples()){
						continue;
					}
					else {
						noNeedToTrain[n] = true;
					}
				}
				count[n] = 0;
				ens.getNetwork(n).learn(para, inputs, targets);
				//para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
			}
			if(comN == ens.size()){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
		
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
				break;
			}
			if((e+1) % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
				
				
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}
			e++;
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
		}	
	}*/
	
	public static void learnDys(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse, 	float[] testAUC, Dataset testdata) {
		
		//using an epoch is a sample used to train
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize();// * data.getNoOfSamples();
		int epoch = para.getEpochs();// * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		/*boolean[] TL = data.checkTomekLink();
		boolean[] isNoise = data.checkNoise(3);
		int[] numEachC = data.getNumEachClass();*/
		float[] ratio = data.getRatioEachClass();
		//int majClass = Matrix.maxLocation(numEachC);
		Random ran = new Random();
		
		for(int count = 0, e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				Matrix.disorder(index, 0);
				//e++;
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			int prediction = Matrix.maxLocation(outputs);
			int label = Matrix.maxLocation(targets);
			float sumOutputs = Matrix.sum(outputs);
			float trP = 0;
			if(targets[prediction] == 0){
				trP = 1f;
			}else if(outputs[label] == 1f){
				trP = 0;
			}else{
				trP = (1f/outputs[label] + 1f/ratio[label] + sumOutputs - outputs[label]) 
						/ (data.getNoOfClasses() + 1f / Matrix.minValue(ratio) + 1f - 1f/data.getNoOfSamples());
			}
			if(
					ran.nextFloat() > trP
					//outputs[prediction] > 1 - (float)numEachC[prediction]/data.getNoOfSamples()
					//&&targets[prediction]==1 
//					|| TL[index[d]]// && prediction == majClass
//					|| isNoise[index[d]]
			){
				if(count++ < data.getNoOfSamples()){
					needToTrain = false;
				}
				else {
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
			}
			
			
			if((d+1)%data.getNoOfSamples()==0){
				e++;
				if((e) % stripSize == 0){
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					//e++;
				}
			}
			
		}	
	}
	
	public static void learnSBP_sampleEpoch(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] gmean, /*float[] mse,*/float[] testAUC, float[] testGmean, Dataset testdata) {

		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize();
		int epoch = para.getEpochs();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC)
							+ "\t gmean: " + df.format(initGmean)
							+ "\t testGmean: " + df.format(initTestGmean)
							);
		
		//float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		//mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = ens.calculateAUC(testdata);
		gmean[aucIndex] = initGmean;
		testGmean[aucIndex] = initTestGmean;
		
		for(int e = 1;e <= epoch;){
			data.disorder(0);
			for(int d = 0;d < data.getNoOfSamples() && e <= epoch;d++,e++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				para.resetExtraAlpha();
				
				AVG avg = new AVG();
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				if(e % stripSize == 0){
					//avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
					
					String printOut = "Epoch " + Integer.toString(e) 
							//+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\rAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\t gmean: " + df.format(gmean[aucIndex])
							+ "\t testGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
				}
			}
		}
	}
	
	public static void learnDys_sampleEpoch_withoutLabel(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] gmean, float[] testAUC,float[] testGmean, int[] trTimeEachClass, Dataset testdata) {
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		//float phy = para.getPhy();
		//float threshold = 1f / data.getNoOfClasses();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC_norOutputs(data);
		float initTestAUC = ens.calculateAUC_norOutputs(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateGmean(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC)
							+ "\t gmean: " + df.format(initGmean)
							+ "\t testGmean: " + df.format(initTestGmean)
							);
		
		/*float avgMSE = ens.getAvgMSE(data, para);*/
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		gmean[aucIndex] = initGmean;
		testAUC[aucIndex] = initTestAUC;
		testGmean[aucIndex] = initTestGmean;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		float[] ratio = data.getRatioEachClass();
		float minRatio = Matrix.minValue(ratio);
		float maxRatio = Matrix.maxValue(ratio);
		float[] rho = new float[ratio.length];
		for(int i = 0;i < rho.length;i++){
			rho[i] = maxRatio/ratio[i] - 1;
		}
		Random ran = new Random();
		
		/*int[] tempCount = new int[2];
		float temp = 0;*/
		Dataset tempData = data.clone();
		//int avgNumEachClass = Matrix.maxValue(tempData.getNumEachClass());
		
		for(int loop = 0, count = 0, e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data = tempData.clone();
				data.duplicateData(rho);
				index = new int[data.getNoOfSamples()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}
				Matrix.disorder(index);
				loop++;
				if(loop > 3){
					for(int i = 0;i < rho.length;i++){
						rho[i] = rho[i]/(float)Math.log(loop);
					}
				}
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			//float[] norOutputs = Matrix.norToPredefinedSum(outputs, 1f);
			int label = Matrix.maxLocation(targets);
			int maxLoc = Matrix.maxLocation(outputs);
			float[] tmpOut = outputs.clone()/*norOutputs.clone()*/;
			//tmpOut[label] = 0;
			tmpOut[maxLoc] = 0;
			float secondLargestOut = Matrix.maxValue(tmpOut);
			float trP = (1f - (outputs[maxLoc] - secondLargestOut))*ratio[label]/minRatio;
			
			if(
					ran.nextFloat() > trP
			){
				if(count++ < data.getNoOfSamples()){
					needToTrain = false;
				}
				else {
					//avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				trTimeEachClass[label]++;
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
				e++;
				if((e) % stripSize == 0){
//					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
				}
			}
		}
		//temp = temp/tempCount[1];
		//System.out.println("p = 1: " + tempCount[0] + "\nothers: " + tempCount[1] + "\ny_i - r_i: " + temp);
	}
	
	public static void learnDys_sampleEpoch_useMargin(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] gmean, float[] testAUC,float[] testGmean, int[] trTimeEachClass, Dataset testdata) {
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		//float phy = para.getPhy();
		//float threshold = 1f / data.getNoOfClasses();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC_norOutputs(data);
		float initTestAUC = ens.calculateAUC_norOutputs(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateGmean(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC)
							+ "\t gmean: " + df.format(initGmean)
							+ "\t testGmean: " + df.format(initTestGmean)
							);
		
		/*float avgMSE = ens.getAvgMSE(data, para);*/
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		gmean[aucIndex] = initGmean;
		testAUC[aucIndex] = initTestAUC;
		testGmean[aucIndex] = initTestGmean;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		float[] ratio = data.getRatioEachClass();
		float minRatio = Matrix.minValue(ratio);
		float maxRatio = Matrix.maxValue(ratio);
		float[] rho = new float[ratio.length];
		float[] initRho = new float[ratio.length];
		for(int i = 0;i < rho.length;i++){
			rho[i] = maxRatio/ratio[i] - 1;
			initRho[i] = rho[i];
		}
		Random ran = new Random();
		
		/*int[] tempCount = new int[2];
		float temp = 0;*/
		Dataset tempData = data.clone();
		//int avgNumEachClass = Matrix.maxValue(tempData.getNumEachClass());
		
		for(int loop = 0, count = 0, e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data = tempData.clone();
				data.duplicateData(rho);
				index = new int[data.getNoOfSamples()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}
				Matrix.disorder(index);
				loop++;
				if(loop > 10){
					for(int i = 0;i < rho.length;i++){
						rho[i] = initRho[i]/(float)Math.log10(loop);
					}
				}
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			float[] norOutputs = Matrix.norToPredefinedSum(outputs, 1f);
			int label = Matrix.maxLocation(targets);
			float[] tmpOut = norOutputs.clone();
			tmpOut[label] = 0;
			float secondLargestOut = Matrix.maxValue(tmpOut);
			float trP = 0;
			if(secondLargestOut >= norOutputs[label]){
				//tempCount[0]++;
				trP = 1f;
			}else{
				//tempCount[1]++;
				//temp += Math.abs(norOutputs[label] - ratio[label]);
				//trP = 1f - phy * norOutputs[label] - (1 - phy) * ratio[label];
				trP = (1f - (norOutputs[label] - secondLargestOut))*ratio[label]/minRatio;
			}
			if(
					ran.nextFloat() > trP
			){
				if(count++ < data.getNoOfSamples()){
					needToTrain = false;
				}
				else {
					//avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				trTimeEachClass[label]++;
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
				e++;
				if((e) % stripSize == 0){
//					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
				}
			}
		}
		//temp = temp/tempCount[1];
		//System.out.println("p = 1: " + tempCount[0] + "\nothers: " + tempCount[1] + "\ny_i - r_i: " + temp);
	}
	
	public static void learnDys_sampleEpoch_useMargin_noNorOut(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] gmean, float[] testAUC,float[] testGmean, int[] trTimeEachClass, Dataset testdata) {
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();

		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateGmean(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC)
							+ "\t gmean: " + df.format(initGmean)
							+ "\t testGmean: " + df.format(initTestGmean)
							);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		gmean[aucIndex] = initGmean;
		testAUC[aucIndex] = initTestAUC;
		testGmean[aucIndex] = initTestGmean;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		float[] ratio = data.getRatioEachClass();
		float minRatio = Matrix.minValue(ratio);
		float maxRatio = Matrix.maxValue(ratio);
		float[] rho = new float[ratio.length];
		float[] initRho = new float[ratio.length];
		for(int i = 0;i < rho.length;i++){
			rho[i] = maxRatio/ratio[i] - 1;
			initRho[i] = rho[i];
		}
		Random ran = new Random();

		Dataset tempData = data.clone();
		for(int loop = 0, count = 0, e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data = tempData.clone();
				data.duplicateData(rho);
				index = new int[data.getNoOfSamples()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}
				Matrix.disorder(index);
				loop++;
				if(loop > 10){
					for(int i = 0;i < rho.length;i++){
						rho[i] = initRho[i]/(float)Math.log10(loop);
					}
				}
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			int label = Matrix.maxLocation(targets);
			float[] tmpOut = outputs.clone();
			tmpOut[label] = 0;
			float secondLargestOut = Matrix.maxValue(tmpOut);
			float trP = 0;
			if(secondLargestOut >= outputs[label]){
				trP = 1f;
			}else{
				trP = (1f - (outputs[label] - secondLargestOut))*ratio[label]/minRatio;
			}
			if(
					ran.nextFloat() > trP
			){
				if(count++ < data.getNoOfSamples()){
					needToTrain = false;
				}
				else {
					auc[++aucIndex] = ens.calculateAUC(data);
					testAUC[aucIndex] = ens.calculateAUC(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				trTimeEachClass[label]++;
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
				e++;
				if((e) % stripSize == 0){
					auc[++aucIndex] = ens.calculateAUC(data);
					testAUC[aucIndex] = ens.calculateAUC(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
				}
			}
		}
	}
	
	public static void learnDys_sampleEpoch(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] gmean, float[] testAUC,float[] testGmean, int[] trTimeEachClass, Dataset testdata) {
		
		//using an epoch is a sample used to train
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		float phy = para.getPhy();
		float threshold = 1f / data.getNoOfClasses();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC_norOutputs(data);
		float initTestAUC = ens.calculateAUC_norOutputs(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateGmean(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC)
							+ "\t gmean: " + df.format(initGmean)
							+ "\t testGmean: " + df.format(initTestGmean)
							);
		
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		gmean[aucIndex] = initGmean;
		testAUC[aucIndex] = initTestAUC;
		testGmean[aucIndex] = initTestGmean;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		float[] ratio = data.getRatioEachClass();
		//float minRatio = Matrix.minValue(ratio);
		Random ran = new Random();
		
		Dataset tempData = data.clone();
		int avgNumEachClass = Matrix.maxValue(tempData.getNumEachClass());
		for(int count = 0, e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data = tempData.clone();
				data.balanceData(avgNumEachClass);
				index = new int[data.getNoOfSamples()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}
				Matrix.disorder(index);
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			float[] norOutputs = Matrix.norToPredefinedSum(outputs, 1f);
			int label = Matrix.maxLocation(targets);
			float[] tmpOut = norOutputs.clone();
			tmpOut[label] = 0;
			float secondLargestOut = Matrix.maxValue(tmpOut);
			float trP = 0;
			if(secondLargestOut > threshold){
				//tempCount[0]++;
				trP = 1f;
			}else{
				//tempCount[1]++;
				//temp += Math.abs(norOutputs[label] - ratio[label]);
				trP = 1f - phy * norOutputs[label] - (1 - phy) * ratio[label];
				//trP = (1f - (norOutputs[label] - secondLargestOut))*minRatio/ratio[label];
			}
			if(
					ran.nextFloat() > trP
			){
				if(count++ < data.getNoOfSamples()){
					needToTrain = false;
				}
				else {
					//avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				trTimeEachClass[label]++;
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
				e++;
				if((e) % stripSize == 0){
//					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					//mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
					gmean[aucIndex] = ens.calculateGmean(data);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
				}
			}
		}
		//temp = temp/tempCount[1];
		//System.out.println("p = 1: " + tempCount[0] + "\nothers: " + tempCount[1] + "\ny_i - r_i: " + temp);
	}
	
	public static void learnAL_sampleEpoch(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] gmean, float[] testAUC, float[] testGmean, int[] trTimeEachClass, Dataset testdata) {
		
		//using an epoch is a sample used to train
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateGmean(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC)
							+ "\t gmean: " + df.format(initGmean)
							+ "\t testGmean: " + df.format(initTestGmean)
							);
		
		/*float avgMSE = ens.getAvgMSE(data, para);*/
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		gmean[aucIndex] = initGmean;
		testAUC[aucIndex] = initTestAUC;
		testGmean[aucIndex] = initTestGmean;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		//float[] ratio = data.getRatioEachClass();
		//float minRatio = Matrix.minValue(ratio);
		//Random ran = new Random();
		
		/*int[] tempCount = new int[2];
		float temp = 0;*/
		/*Dataset tempData = data.clone();
		int avgNumEachClass = Matrix.maxValue(tempData.getNumEachClass());*/
		ArrayList<Integer>[] arr = data.stratifiedSelect(para.getRatioForAL());
		ArrayList<Integer> selectedIndex = arr[0];
		ArrayList<Integer> remainedIndex = arr[1];
		int countEpoch = 0;
		for(int e = 0, d = 0;e < epoch;d = (d+1)%selectedIndex.size()/*data.getNoOfSamples()*/){
			if(d == 0){
				countEpoch++;
				if(countEpoch%(para.getEpochs()/10)==0){
					/*
					 * Query for new training samples
					 */
					for(int i = 0;i < remainedIndex.size();i++){
						float[] inputs = data.getInputs(remainedIndex.get(i));
						float[] output = ens.getOutputs(inputs);
						if(Matrix.maxValue(output)-Matrix.secondMaxValue(output) < para.getEpsilonForAL()){
							selectedIndex.ensureCapacity(selectedIndex.size()+1);
							selectedIndex.add(remainedIndex.get(i));
							remainedIndex.remove(i);
							i--;
						}
					}
				}
				index = new int[selectedIndex.size()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}
				Matrix.disorder(index);
				System.out.println(countEpoch + " selected:" + selectedIndex.size() + " remained:" + remainedIndex.size());
			}
			float[] inputs = data.getInputs(selectedIndex.get(index[d]));
			float[] targets = data.getTargets(selectedIndex.get(index[d]));
			//float[] outputs = ens.getOutputs(inputs);
			//float[] norOutputs = Matrix.norToPredefinedSum(outputs, 1f);
			int label = Matrix.maxLocation(targets);
			
			trTimeEachClass[label]++;
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learn(para, inputs, targets);
			}
			//count = 0;
			e++;
			if((e) % stripSize == 0){
//				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				//mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
				gmean[aucIndex] = ens.calculateGmean(data);
				testGmean[aucIndex] = ens.calculateGmean(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex])
						+ "\tgmean: " + df.format(gmean[aucIndex])
						+ "\ttestGmean: " + df.format(testGmean[aucIndex])
						;
				System.out.println(printOut);
			}
		}
		//temp = temp/tempCount[1];
		//System.out.println("p = 1: " + tempCount[0] + "\nothers: " + tempCount[1] + "\ny_i - r_i: " + temp);
	}
	
	public static void learnDys_CountTrainTime(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] testAUC, int[] trTimeEachSample, Dataset testdata,float rate) {
		
		//using an epoch is a sample used to train
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		float phy = para.getPhy();
		float threshold = 1f / data.getNoOfClasses();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC_norOutputs(data);
		float initTestAUC = ens.calculateAUC_norOutputs(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		testAUC[aucIndex] = initTestAUC;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		
		float[] ratio = data.getRatioEachClass();
		//float minRatio = Matrix.minValue(ratio);
		Random ran = new Random();
		
		/*int[] tempCount = new int[2];
		float temp = 0;*/
		//Dataset tempData = data.clone();
		int avgNumEachClass = Matrix.maxValue(data.getNumEachClass());
		int numSamples_new = avgNumEachClass * data.getNoOfClasses();
		int[] index = new int[numSamples_new];
		/*for(int i = 0;i < index.length;i++){
			index[i] = i;
		}*/
		for(int count = 0, e = 0, d = 0;e < epoch;d = (d+1)%numSamples_new){
			if(d == 0){
				/*data = tempData.clone();
				data.balanceData(Matrix.maxValue(tempData.getNumEachClass()));
				index = new int[data.getNoOfSamples()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}*/
				index = data.balanceData_onlyGetIndex(avgNumEachClass);
				Matrix.disorder(index, 0);
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			float[] norOutputs = Matrix.norToPredefinedSum(outputs, 1f);
			int label = Matrix.maxLocation(targets);
			float[] tmpOut = norOutputs.clone();
			tmpOut[label] = 0;
			float secondLargestOut = Matrix.maxValue(tmpOut);
			float trP = 0;
			if(secondLargestOut > threshold){
				//tempCount[0]++;
				trP = 1f;
			}else{
				//tempCount[1]++;
				//temp += Math.abs(norOutputs[label] - ratio[label]);
				trP = 1f - phy * norOutputs[label] - (1 - phy) * ratio[label];
				//trP = (1f - (norOutputs[label] - secondLargestOut))*minRatio/ratio[label];
			}
			if(
					ran.nextFloat() > trP
			){
				if(count++ < index.length){
					/*
					 * the length of index is the number of samples of oversampled data set
					 */
					needToTrain = false;
				}
				else {
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				if(e > epoch*rate)trTimeEachSample[index[d]]++;
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
				e++;
				if((e) % stripSize == 0){
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC_norOutputs(data);
					testAUC[aucIndex] = ens.calculateAUC_norOutputs(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
				}
			}
		}
		//temp = temp/tempCount[1];
		//System.out.println("p = 1: " + tempCount[0] + "\nothers: " + tempCount[1] + "\ny_i - r_i: " + temp);
	}
	
	public static void learnDysNoNor(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse, 	float[] testAUC, int[] trTimeEachClass, Dataset testdata) {
		
		//an epoch is a sample used to train
		//the output is not normalized
		 
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		float phy = para.getPhy();
		float threshold = 1f / data.getNoOfClasses();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		AVG avg = new AVG();
		
		// parallel
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		
		int[] index = new int[data.getNoOfSamples()];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		float[] ratio = data.getRatioEachClass();
		//float minRatio = Matrix.minValue(ratio);
		Random ran = new Random();
		
		/*int[] tempCount = new int[2];
		float temp = 0;*/
		Dataset tempData = data.clone();
		int avgNumEachClass = Matrix.maxValue(tempData.getNumEachClass());
		for(int count = 0, e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data = tempData.clone();
				data.balanceData(avgNumEachClass);
				index = new int[data.getNoOfSamples()];
				for(int i = 0;i < index.length;i++){
					index[i] = i;
				}
				Matrix.disorder(index, 0);
			}
			boolean needToTrain = true;
			float[] inputs = data.getInputs(index[d]);
			float[] targets = data.getTargets(index[d]);
			float[] outputs = ens.getOutputs(inputs);
			int label = Matrix.maxLocation(targets);
			float[] tmpOut = outputs.clone();
			tmpOut[label] = 0;
			float secondLargestOut = Matrix.maxValue(tmpOut);
			float trP = 0;
			if(secondLargestOut > threshold){
				//tempCount[0]++;
				trP = 1f;
			}else{
				//tempCount[1]++;
				//temp += Math.abs(norOutputs[label] - ratio[label]);
				trP = 1f - phy * outputs[label] - (1 - phy) * ratio[label];
				//trP = (1f - (norOutputs[label] - secondLargestOut))*minRatio/ratio[label];
			}
			if(
					ran.nextFloat() > trP
			){
				if(count++ < data.getNoOfSamples()){
					needToTrain = false;
				}
				else {
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					break;
				}
			}
			
			if(needToTrain){
				trTimeEachClass[label]++;
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				count = 0;
				e++;
				if((e) % stripSize == 0){
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
				
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
				}
			}
		}
		//temp = temp/tempCount[1];
		//System.out.println("p = 1: " + tempCount[0] + "\nothers: " + tempCount[1] + "\ny_i - r_i: " + temp);
	}
	
	
	public static void learnDys_selByP(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse,  
			float[] testAUC, Dataset testdata) {
		/*
		 * using an epoch is a sample used to train
		 */
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		
		AVG avg = new AVG();
		/*
		 * parallel
		 */
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		para.resetExtraAlpha();
		Random ran = new Random();
		for(int  e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			if(d == 0){
				data.disorder(0);
			}
			
			float[] inputs = data.getInputs(d);
			float[] targets = data.getTargets(d);
			float[] outputs = ens.getOutputs(inputs);
			float[] normalizedOut = Matrix.norToPredefinedSum(outputs, 1f);
			float p = 1f - normalizedOut[Matrix.maxLocation(targets)];
			
			if(
					ran.nextFloat() > p
			){
		//		if(count++ < data.getNoOfSamples()){
					continue;
		/*		}
				else {
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
			
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					break;
				}*/
			}
			//count = 0;
			
			
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learn(para, inputs, targets);
			}
			
			if((e+1) % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
				
				
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}
			e++;
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
		}	
	}
	
	
	public static void learn_rankBased_Dys(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse,  
			float[] testAUC, Dataset testdata) {
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		
		AVG avg = new AVG();
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		
		para.resetExtraAlpha();
		
		int[] numEachCla = data.getNumEachClass();
		int NP = numEachCla[0];
		int NN = numEachCla[1];
		
		data.disorder(0);
		float[][] outputs = ens.getOutputsOfAllSamples(data.getInputs());
		float[] score = new float[outputs.length];
		for(int i = 0;i < outputs.length;i++){
			score[i] = outputs[i][0] - outputs[i][1];
		}
		int[] index = Matrix.QuickSort(score);//每次有更新权值之后就更新score以及index
		
		
		//float avgRate = 0;
		for(int  e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			
			float[] inputs = data.getInputs(d);
			float[][] targets = data.getTargets();
			
			float rate = 0;
			if(targets[d][0] == 1){
				//positive
				for(int i = index.length - 1;i >= 0;i--){
					int tempIndex = index[i];
					if(tempIndex == d){
						break;
					}
					if(targets[tempIndex][0] == 0){
						rate++;
					}
				}
				rate = rate / (float)NN;
			}else {
				//negative
				for(int i = 0;i < index.length;i++){
					int tempIndex = index[i];
					if(tempIndex == d){
						break;
					}
					if(targets[tempIndex][0] == 1){
						rate++;
					}
				}
				rate = rate / (float)NP;
			}
			//System.out.println(d + " " + rate);
			//avgRate += rate;
			if(rate <= para.getRankThreshold() && targets[d][ens.getPrediction(inputs)] == 1){
				if((d + 1)%data.getNoOfSamples() == 0){
					data.disorder(0);
					
					//System.out.println(avgRate / data.getNoOfSamples());
					//avgRate = 0;
					
					outputs = ens.getOutputsOfAllSamples(data.getInputs());
					for(int i = 0;i < outputs.length;i++){
						score[i] = outputs[i][0] - outputs[i][1];
					}
					index = Matrix.QuickSort(score);
				}
				continue;
			}
			
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learn(para, inputs, targets[d]);
			}
			
			if((d + 1)%data.getNoOfSamples() == 0){
				data.disorder(0);
				//System.out.println(avgRate / data.getNoOfSamples());
				//avgRate = 0;
			}
			outputs = ens.getOutputsOfAllSamples(data.getInputs());
			for(int i = 0;i < outputs.length;i++){
				score[i] = outputs[i][0] - outputs[i][1];
			}
			index = Matrix.QuickSort(score);//每次有更新权值之后就更新score以及index
			
			if((e+1) % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}
			e++;
			
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
		}	
	}
	
	
	public static Ensemble learn_rankBased_Dys_SA(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse, float[] testAUC, Dataset testdata) {
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		
		AVG avg = new AVG();
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		
		para.resetExtraAlpha();
		
		int[] numEachCla = data.getNumEachClass();
		int NP = numEachCla[0];
		int NN = numEachCla[1];
		
		data.disorder(0);
		float[][] outputs = ens.getOutputsOfAllSamples(data.getInputs());
		float[] score = new float[outputs.length];
		for(int i = 0;i < outputs.length;i++){
			score[i] = outputs[i][0] - outputs[i][1];
		}
		int[] index = Matrix.QuickSort(score);//每次有更新权值之后就更新score以及index
		
		Ensemble ensPre = (Ensemble)ens.clone();
		float aucPre = initAuc;
		float T = para.getTemperature();
		float factor = para.getFactor();
		int decStrip = epoch / 100;
		
		Random ran = new Random();
		
		//float avgRate = 0;
		for(int  e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			
			float[] inputs = data.getInputs(d);
			float[][] targets = data.getTargets();
			
			
			//=============================  Begin  ======================================
			//判断该样本是否用于训练
			float rate = 0;
			if(targets[d][0] == 1){
				//positive
				for(int i = index.length - 1;i >= 0;i--){
					int tempIndex = index[i];
					if(tempIndex == d){
						break;
					}
					if(targets[tempIndex][0] == 0){
						rate++;
					}
				}
				rate = rate / (float)NN;
			}else {
				//negative
				for(int i = 0;i < index.length;i++){
					int tempIndex = index[i];
					if(tempIndex == d){
						break;
					}
					if(targets[tempIndex][0] == 1){
						rate++;
					}
				}
				rate = rate / (float)NP;
			}
			if(rate <= para.getRankThreshold() && targets[d][ens.getPrediction(inputs)] == 1){
				if((d + 1)%data.getNoOfSamples() == 0){
					data.disorder(0);
					
					//System.out.println(avgRate / data.getNoOfSamples());
					//avgRate = 0;
					
					outputs = ens.getOutputsOfAllSamples(data.getInputs());
					for(int i = 0;i < outputs.length;i++){
						score[i] = outputs[i][0] - outputs[i][1];
					}
					index = Matrix.QuickSort(score);
				}
				continue;
			}
			//=============================  End  ==============================================
			
			//用该样本更新NN
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learn(para, inputs, targets[d]);
			}
			
			//==============================  Begin  ===============================================
			//Simulated Annealing
			float[] label = new float[targets.length];
			for(int i = 0;i < label.length;i++){
				label[i] = targets[i][0];
			}
			float aucNew = Ensemble.calculateAUC(score, index, label);
			if(aucNew < aucPre){
				float p = (float)Math.exp((aucNew - aucPre) / T);
				//System.out.println(p);
				if(ran.nextFloat() > p){
					ens = (Ensemble)ensPre.clone();
					aucNew = aucPre;
				}
			}
			//================================  End  ============================================
			ensPre = (Ensemble)ens.clone();
			aucPre = aucNew;
			
			if((d + 1)%data.getNoOfSamples() == 0){
				data.disorder(0);
				//System.out.println(avgRate / data.getNoOfSamples());
				//avgRate = 0;
			}
			outputs = ens.getOutputsOfAllSamples(data.getInputs());
			for(int i = 0;i < outputs.length;i++){
				score[i] = outputs[i][0] - outputs[i][1];
			}
			index = Matrix.QuickSort(score);//每次有更新权值之后就更新score以及index
			
			if((e+1) % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = aucNew;//ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}
			//====================================================
			if((e + 1) % decStrip == 0){
				
				T = T * factor;
				//System.out.println("T" + T);
			}
			//====================================================
			e++;
			
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
		}
		return ens;
		//System.out.println("testAUC: " + df.format(ens.calculateAUC(testdata)));
	}


	public static Ensemble learn_Dys_SA(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse, float[] testAUC, Dataset testdata) {
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		
		AVG avg = new AVG();
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		
		para.resetExtraAlpha();
		
		data.disorder(0);
		
		
		Ensemble ensPre = (Ensemble)ens.clone();
		
		Ensemble bestEns = (Ensemble)ens.clone();
		float bestAuc = initAuc;
		
		boolean accepted = false;
		float aucPre = initAuc;
		float T = para.getTemperature();
		float factor = para.getFactor();
		int decStrip = epoch / para.getDecTime();
		
		Random ran = new Random();
		
		//float avgRate = 0;
		int count = 0;
		for(int  e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			
			float[] inputs = data.getInputs(d);
			float[][] targets = data.getTargets();
			
			
			//======================= 判断该样本是否用于训练 ===============================
			if(targets[d][ens.getPrediction(inputs)] == 1){
				if((d + 1)%data.getNoOfSamples() == 0){
					data.disorder(0);
				}
				if(count++ < data.getNoOfSamples()){
					
					continue;
				}
				else {
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
			
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					break;
				}
			}
			count = 0;
			//=============================  End  ==============================================
			
			//用该样本更新NN
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learn(para, inputs, targets[d]);
			}
			
			//======================= Simulated Annealing ======================================
			/*float[][] tempOutputs = ens.getOutputsOfAllSamples(data.getInputs());
			float[] tempScore = new float[tempOutputs.length];
			for(int i = 0;i < tempScore.length;i++){
				tempScore[i] = tempOutputs[i][0] - tempOutputs[i][1];
			}
			int[] tempIndex = Matrix.QuickSort(tempScore);
			float[] label = new float[targets.length];
			for(int i = 0;i < label.length;i++){
				label[i] = targets[i][0];
			}
			float aucNew = Ensemble.calculateAUC(tempScore, tempIndex, label);*/
			float aucNew = ens.calculateAUC(data);
			if(aucNew < aucPre){
				float p = (float)Math.exp((aucNew - aucPre) / T);
				if(ran.nextFloat() > p){
					ens = (Ensemble)ensPre.clone();
					aucNew = aucPre;
					accepted = false;
				}else{
					accepted = true;
				}
			}else{
				accepted = true;
			}
			//================================  End  ============================================
			if(accepted){
				ensPre = (Ensemble)ens.clone();
				aucPre = aucNew;
				if(aucNew > bestAuc){
					bestAuc = aucNew;
					bestEns = (Ensemble)ens.clone();
				}
			}
			if((d + 1)%data.getNoOfSamples() == 0){
				data.disorder(0);
			}
			if((e+1) % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = aucNew;//ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}
			//====================================================
			if((e + 1) % decStrip == 0){
				
				T = T * factor;
				//System.out.println("T" + T);
			}
			//====================================================
			e++;
			
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
		}
		return bestEns;
		//System.out.println("testAUC: " + df.format(ens.calculateAUC(testdata)));
	}
	
	
	public static Ensemble learn_Dys_SA_sample(Ensemble ens, Dataset data, Parameters para,
			float[] auc, float[] mse, float[] testAUC, Dataset testdata) {
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize() * data.getNoOfSamples();
		int epoch = para.getEpochs() * data.getNoOfSamples();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		Ensemble bestEns = (Ensemble)ens.clone();
		float bestAuc = initAuc;
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = initTestAUC;
		
		
		AVG avg = new AVG();
		para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs(0))));
		
		para.resetExtraAlpha();
		
		int[] numEachCla = data.getNumEachClass();
		int[] sample_size = new int[numEachCla.length];
		
		
		//================= calculate sample_size ===================
		float bias = 0.05f, biasP = 0.01f;
		for(int i = 0;i < numEachCla.length;i++){
			if(numEachCla[i] > 100){
				float C = ((float)Math.log(2 / biasP)) / (2 * numEachCla[i] * bias * bias);
				sample_size[i] = (int)(C * numEachCla[i] / (1 + C));
			}else{
				sample_size[i] = numEachCla[i];
			}
		}
		//=========================== end =================
		
		data.disorder(0);
		//=========== Sampling ================
		Dataset cutData = data.clone();
		cutData.balanceData(sample_size);
		//System.out.println(Matrix.sum(cutData.getNumEachClass()));
		
		//============= end ===================
	/*	float[][] outputs = ens.getOutputsOfAllSamples(cutData.getInputs());
		float[] score = new float[outputs.length];
		for(int i = 0;i < outputs.length;i++){
			score[i] = outputs[i][0] - outputs[i][1];
		}
		int[] index = Matrix.QuickSort(score);//每次有更新权值之后就更新score以及index
		float[] label = new float[cutData.getNoOfSamples()];
		float[][] cutTargets = cutData.getTargets();
		for(int i = 0;i < label.length;i++){
			label[i] = cutTargets[i][0];
		}
		float aucPre = Ensemble.calculateAUC(score, index, label);*/
		
		float aucPre = ens.calculateAUC(cutData);
		Ensemble ensPre = (Ensemble)ens.clone();
		
		float T = para.getTemperature();
		float factor = para.getFactor();
		int decStrip = epoch / para.getDecTime();
		
		Random ran = new Random();
		
		boolean accepted = false;
		//float avgRate = 0;
		int count = 0;
		for(int  e = 0, d = 0;e < epoch;d = (d+1)%data.getNoOfSamples()){
			
			float[] inputs = data.getInputs(d);
			float[][] targets = data.getTargets();
			
			
			//======================= 判断该样本是否用于训练 ================================
			if(targets[d][ens.getPrediction(inputs)] == 1){
				if((d + 1)%data.getNoOfSamples() == 0){
					data.disorder(0);
				}
				if(count++ < data.getNoOfSamples()){
					
					continue;
				}
				else {
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
			
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
					break;
				}
			}
			count = 0;
			//=============================  End  ==============================================
			
			//用该样本更新NN
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learn(para, inputs, targets[d]);
			}
			
			//========================  Simulated Annealing  ===================================
			/*float[][] tempOutputs = ens.getOutputsOfAllSamples(cutData.getInputs());
			float[] tempScore = new float[tempOutputs.length];
			for(int i = 0;i < tempOutputs.length;i++){
				tempScore[i] = tempOutputs[i][0] = tempOutputs[i][1];
			}
			int[] tempIndex = Matrix.QuickSort(tempScore);
			float aucNew = Ensemble.calculateAUC(tempScore, tempIndex, label);*/
			float aucNew = ens.calculateAUC(cutData);
			if(aucNew < aucPre){
				float p = (float)Math.exp((aucNew - aucPre) / T);
				if(ran.nextFloat() > p){
					ens = (Ensemble)ensPre.clone();
					aucNew = aucPre;
					accepted = false;
				}else{
					accepted = true;
				}
			}else{
				accepted = true;
			}
			//================================  End  ============================================
			if(accepted){
				ensPre = (Ensemble)ens.clone();
				aucPre = aucNew;
				if(aucNew > bestAuc){
					bestAuc = aucNew;
					bestEns = (Ensemble)ens.clone();
				}
				//======== sampling ============
				cutData = data.clone();
				cutData.balanceData(sample_size);
				//========== end ===============
			}
			
			if((d + 1)%data.getNoOfSamples() == 0){
				data.disorder(0);	
			}
			
			/*
			outputs = ens.getOutputsOfAllSamples(cutData.getInputs());
			for(int i = 0;i < outputs.length;i++){
				score[i] = outputs[i][0] - outputs[i][1];
			}
			index = Matrix.QuickSort(score);//每次有更新权值之后就更新score以及index
			*/
			if((e+1) % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = aucNew;//ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}
			//====================================================
			if((e + 1) % decStrip == 0){
				T = T * factor;
			}
			//====================================================
			e++;
			
			para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(data.getInputs((d+1)%data.getNoOfSamples()))));
		}
		return bestEns;
		//System.out.println("testAUC: " + df.format(ens.calculateAUC(testdata)));
	}
	
	
	public static void learnALR(Ensemble ens, Dataset data, Parameters para,float[][] costMat,
			float[] auc, float[] mse, 	float[] testAUC, Dataset testdata) {
		
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		int stripSize = para.getStripSize();
		para.initExtraAlpha(data.getNoOfClasses());
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
		
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = ens.calculateAUC(testdata);
		
		int[] numEachClass = data.getNumEachClass();
		/*float[][] costMat = new float[data.getNoOfClasses()][data.getNoOfClasses()];
		for(int i = 0;i < costMat.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(i == k)costMat[i][k] = 0;
				else{
					costMat[i][k] = 1f/(numEachClass[i] * 1f);
				}
			}
		}*/
		
		float[] priorPro = new float[data.getNoOfClasses()];
		float[] costVector = new float[data.getNoOfClasses()];
		for(int i = 0;i < priorPro.length;i++){
			priorPro[i] = numEachClass[i] / (data.getNoOfSamples() * 1f);
		}
		for(int i = 0;i < costVector.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(k != i){
					costVector[i] += priorPro[k] * costMat[i][k];
				}
			}
			costVector[i] = costVector[i] / (1 - priorPro[i]);
		}
		
		float maxCost = Matrix.maxValue(costVector);
		for(int e = 1;e <= para.getEpochs();e++){
			data.disorder(0);
			for(int d = 0;d < data.getNoOfSamples();d++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				int classLabel = Matrix.maxLocation(targets);
				
				float[] extraA = new float[data.getNoOfClasses()];
				for(int i = 0;i < extraA.length;i++){
					extraA[i] = costVector[classLabel] / maxCost;
				}
				para.setExtraAlpha(extraA);
				
				
				AVG avg = new AVG();
				/*
				 * parallel
				 */
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
			}
			if(e % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}	
		}
	}
	
	public static void learnALR_sampleEpoch(Ensemble ens, Dataset data, Parameters para,float[][] costMat,
			float[] auc, float[] mse,float[] testAUC, Dataset testdata) {

		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize();
		int epoch = para.getEpochs();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = ens.calculateAUC(testdata);
		
		int[] numEachClass = data.getNumEachClass();
		/*float[][] costMat = new float[data.getNoOfClasses()][data.getNoOfClasses()];
		for(int i = 0;i < costMat.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(i == k)costMat[i][k] = 0;
				else{
					costMat[i][k] = 1f/(numEachClass[i] * 1f);
				}
			}
		}*/
		float[] priorPro = new float[data.getNoOfClasses()];
		float[] costVector = new float[data.getNoOfClasses()];
		for(int i = 0;i < priorPro.length;i++){
			priorPro[i] = numEachClass[i] / (data.getNoOfSamples() * 1f);
		}
		for(int i = 0;i < costVector.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(k != i){
					costVector[i] += priorPro[k] * costMat[i][k];
				}
			}
			costVector[i] = costVector[i] / (1 - priorPro[i]);
		}
		float maxCost = Matrix.maxValue(costVector);
		
		for(int e = 1;e <= epoch;){
			data.disorder(0);
			for(int d = 0;d < data.getNoOfSamples() && e <= epoch;d++,e++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				//para.resetExtraAlpha();
				int classLabel = Matrix.maxLocation(targets);
				float[] extraA = new float[data.getNoOfClasses()];
				for(int i = 0;i < extraA.length;i++){
					extraA[i] = costVector[classLabel] / maxCost;
				}
				para.setExtraAlpha(extraA);
				
				AVG avg = new AVG();
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				if(e % stripSize == 0){
					avgMSE = ens.getAvgMSE(data,para);
					auc[++aucIndex] = ens.calculateAUC(data);
					mse[aucIndex] = avgMSE;
					testAUC[aucIndex] = ens.calculateAUC(testdata);
					
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
							+ "\tAUC: " + df.format(auc[aucIndex])
							+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
					System.out.println(printOut);
				}
			}
		}
	}
	
	
	public static void learnMMC(Ensemble ens, Dataset data, Parameters para, float[][] costMat,
			float[] auc, float[] mse, 	float[] testAUC, Dataset testdata) {
		
		
		String stringOfParameters = "MMC\nalpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		int stripSize = para.getStripSize();
		para.initExtraAlpha(data.getNoOfClasses());
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
		
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\t testAUC: " + df.format(initTestAUC));
		
		float avgMSE = ens.getAvgMSE(data, para);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		mse[aucIndex] = ens.getAvgMSE(data, para);
		testAUC[aucIndex] = ens.calculateAUC(testdata);
		
		/*float[][] costMat = new float[data.getNoOfClasses()][data.getNoOfClasses()];
		int[] numEachClass = data.getNumEachClass();
		float[] priorPro = new float[data.getNoOfClasses()];
		float[] costVector = new float[data.getNoOfClasses()];
		for(int i = 0;i < costMat.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(i == k)costMat[i][k] = 0;
				else{
					costMat[i][k] = 1f/(numEachClass[i] * 1f);
				}
			}
		}*/
		float[] priorPro = new float[data.getNoOfClasses()];
		float[] costVector = new float[data.getNoOfClasses()];
		int[] numEachClass = data.getNumEachClass();
		for(int i = 0;i < priorPro.length;i++){
			priorPro[i] = numEachClass[i] / (data.getNoOfSamples() * 1f);
		}
		for(int i = 0;i < costVector.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(k != i){
					costVector[i] += priorPro[k] * costMat[i][k];
				}
			}
			costVector[i] = costVector[i] / (1 - priorPro[i]);
		}
		float[][] factorK = new float[data.getNoOfClasses()][data.getNoOfClasses()];
		for(int i = 0;i < factorK.length;i++){
			for(int k = 0;k < factorK[i].length;k++){
				if(i == k){
					factorK[i][k] = costVector[i];
				}else{
					factorK[i][k] = costMat[i][k];
				}
			}
		}
		//float maxK = Matrix.maxValue(factorK);
		float[][] squareFactorK = new float[factorK.length][factorK[0].length];
		for(int i = 0;i < squareFactorK.length;i++){
			for(int k = 0;k < squareFactorK.length;k++){
				squareFactorK[i][k] = factorK[i][k] * factorK[i][k];
			}
		}
		float normalizedFactor = Matrix.maxValue(squareFactorK);
		for(int e = 1;e <= para.getEpochs();e++){
			data.disorder(0);
			for(int d = 0;d < data.getNoOfSamples();d++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				int classLabel = Matrix.maxLocation(targets);
				
				float[] extraA = new float[data.getNoOfClasses()];
				for(int i = 0;i < extraA.length;i++){
					extraA[i] = squareFactorK[classLabel][i] / normalizedFactor;
				}
				
				//Matrix.print(extraA);
				para.setExtraAlpha(extraA);
				
				
				AVG avg = new AVG();
				/*
				 * parallel
				 */
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
			}
			if(e % stripSize == 0){
				avgMSE = ens.getAvgMSE(data,para);
				auc[++aucIndex] = ens.calculateAUC(data);
				mse[aucIndex] = avgMSE;
				testAUC[aucIndex] = ens.calculateAUC(testdata);
			
				String printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\tAUC: " + df.format(auc[aucIndex])
						+ "\tTestAUC: " + df.format(testAUC[aucIndex]);
				System.out.println(printOut);
			}	
		}
	}
	
	public static void learnMMC_sampleEpoch(Ensemble ens, Dataset data, Parameters para,float[][] costMat,
			float[] auc, float[] gmean,float[] testAUC, float[] testGmean, Dataset testdata) {

		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
								para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		int stripSize = para.getStripSize();
		int epoch = para.getEpochs();
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
	
		float initAuc = ens.calculateAUC(data);
		float initTestAUC = ens.calculateAUC(testdata);
		float initGmean = ens.calculateGmean(data);
		float initTestGmean = ens.calculateGmean(testdata);
		System.out.println("Init AUC: " + df.format(initAuc)
							+ "\ttestAUC: " + df.format(initTestAUC)
							+ "\tgmean: " + df.format(initGmean)
							+ "\ttestGmean:" + df.format(initTestGmean)
							);
		
		int aucIndex = 0;
		auc[aucIndex] = initAuc;
		gmean[aucIndex] = initGmean;
		testAUC[aucIndex] = initTestAUC;
		testGmean[aucIndex] = initTestGmean;
		
		float[] priorPro = new float[data.getNoOfClasses()];
		float[] costVector = new float[data.getNoOfClasses()];
		int[] numEachClass = data.getNumEachClass();
		for(int i = 0;i < priorPro.length;i++){
			priorPro[i] = numEachClass[i] / (data.getNoOfSamples() * 1f);
		}
		for(int i = 0;i < costVector.length;i++){
			for(int k = 0;k < costMat[i].length;k++){
				if(k != i){
					costVector[i] += priorPro[k] * costMat[i][k];
				}
			}
			costVector[i] = costVector[i] / (1 - priorPro[i]);
		}
		float[][] factorK = new float[data.getNoOfClasses()][data.getNoOfClasses()];
		for(int i = 0;i < factorK.length;i++){
			for(int k = 0;k < factorK[i].length;k++){
				if(i == k){
					factorK[i][k] = costVector[i];
				}else{
					factorK[i][k] = costMat[i][k];
				}
			}
		}
		//float maxK = Matrix.maxValue(factorK);
		float[][] squareFactorK = new float[factorK.length][factorK[0].length];
		for(int i = 0;i < squareFactorK.length;i++){
			for(int k = 0;k < squareFactorK.length;k++){
				squareFactorK[i][k] = factorK[i][k] * factorK[i][k];
			}
		}
		float normalizedFactor = Matrix.maxValue(squareFactorK);
		
		for(int e = 1;e <= epoch;){
			data.disorder(0);
			for(int d = 0;d < data.getNoOfSamples() && e <= epoch;d++,e++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				//para.resetExtraAlpha();
				int classLabel = Matrix.maxLocation(targets);
				float[] extraA = new float[data.getNoOfClasses()];
				for(int i = 0;i < extraA.length;i++){
					extraA[i] = squareFactorK[classLabel][i] / normalizedFactor;
				}
				//Matrix.print(extraA);
				para.setExtraAlpha(extraA);
				
				AVG avg = new AVG();
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
				if(e % stripSize == 0){
					auc[++aucIndex] = ens.calculateAUC(data);
					gmean[aucIndex] = ens.calculateGmean(data);
					testAUC[aucIndex] = ens.calculateAUC(testdata);
					testGmean[aucIndex] = ens.calculateGmean(testdata);
					
					String printOut = "Epoch " + Integer.toString(e) 
							+ "\r\nAUC: " + df.format(auc[aucIndex])
							+ "\ttestAUC: " + df.format(testAUC[aucIndex])
							+ "\tgmean: " + df.format(gmean[aucIndex])
							+ "\ttestGmean: " + df.format(testGmean[aucIndex])
							;
					System.out.println(printOut);
				}
			}
		}
	}
	
	
	public static void learn(Ensemble ens, Dataset data, Parameters para,int firstEpoch,int stripSize) {
		//all NNs will be updated 
		// using wrong classified data each epoch
		
		String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
							+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + 
							para.getNoOfHidNodes() + "\r\nepochs: " + para.getEpochs() + "\r\n";
		
		//float alpha = para.getAlpha();
		para.initExtraAlpha(data.getNoOfClasses());
		
		System.out.println(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
		float errRate = ens.getErrorRate(data);			
		String printOut = "Init " + "\terrorRate: " + df.format(errRate);
		System.out.println(printOut);
		
		data.disorder();
		Dataset traindata = data.clone();
		for(int e = 1;e <= para.getEpochs();e++){
			//data.disorder();
			//Dataset traindata = data.getWrongClassifiedData(ens);
			
			for(int d = 0;d < traindata.getNoOfSamples();d++){
				float[] inputs = traindata.getInputs(d);
				float[] targets = traindata.getTargets(d);
				
				para.resetExtraAlpha();
				
				AVG avg = new AVG();
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).learn(para, inputs, targets);
				}
			}
			if(e % 10 == 0){
				float avgMSE = ens.getAvgMSE(data,para);
				errRate = ens.getErrorRate(data);				
				
				float[] E = ens.getSeparateErrorRate(data);
				float EP = E[0];
				float EN = E[1];
				printOut = "Epoch " + Integer.toString(e) 
						+ "\r\nMSE: " + df.format(avgMSE) 
						+ "\terrorRate: " + df.format(errRate)
						+ "\tEP: " + df.format(EP)
						+ "\tEN: " + df.format(EN);
				System.out.println(printOut);
			}
			if(e >= firstEpoch){
				if(e % stripSize == 0){
					traindata = data.getLowConfidenceData(ens, 0.2f);
					traindata = data.balanceData(traindata);
					if(traindata.getNoOfSamples() < 5)
						break;
					//para.setAlpha(alpha * traindata.getNoOfSamples()/(float)data.getNoOfSamples());
				}
			}
		}
		
	}
	
	public static void learnBatchMode(Ensemble ens, Dataset data, Parameters para){
		//all NNs will be updated 
		
		String stringOfParameters = "[" + para.getPredictType() + "]\n"
							+ "alpha: " + para.getAlpha() + "\nlambda: " + para.getLambda()
							+ "\nnumOfNets: " + para.getNoOfNet() + "\nnumOfHidNodes: " + 
							para.getNoOfHidNodes() + "\nepochs: " + para.getEpochs() + "\n";
		
		para.initExtraAlpha(data.getNoOfClasses());
		Tools.STDOUT(stringOfParameters);
		DecimalFormat df = new DecimalFormat("0.00000");
		//float errRate = ens.getErrorRate(data);			
		//String printOut = "Init " + "\terrorRate: " + df.format(errRate);
		//System.out.println(printOut);
		float initMse = ens.getAvgMSE(data,para);
		String printOut = "[" + para.getPredictType() + "]Init " + "\tMSE: " + df.format(initMse);
		Tools.STDOUT(printOut);			
		
		float prevMse = initMse;
		int noImprove = 0;
		data.disorder();
		//迭代次数
		for(int e = 1;e <= para.getEpochs();e++){
			//data.disorder();
			for(int d = 0;d < data.getNoOfSamples();d++){
				float[] inputs = data.getInputs(d);
				float[] targets = data.getTargets(d);
				
				para.resetExtraAlpha();
				
				AVG avg = new AVG();
				para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int n = 0;n < ens.size();n++){
					ens.getNetwork(n).evaluateDelta(para, inputs, targets);
				}
			}
			for(int n = 0;n < ens.size();n++){
				ens.getNetwork(n).learnBatch();
			}
			
			/*if(e%100==0){
				float avgMSE = ens.getAvgMSE(data,para);
				float decline = (prevMse-avgMSE)/prevMse;
				if(decline < 0.0001){
					noImprove++;
				}else{
					noImprove = 0;
				}
				if(noImprove > 5){
					printOut = "Stop at Epoch " + Integer.toString(e) 
							+ "\r\nMSE: " + df.format(avgMSE) 
					//		+ "\terrorRate: " + df.format(errRate)
							;
					System.out.println(printOut);
					break;
				}
				prevMse = avgMSE;
			}*/
			if(e % para.getStripSize() == 0){
				float avgMSE = ens.getAvgMSE(data,para);
				//errRate = ens.getErrorRate(data);				
				
				printOut = "[" + para.getPredictType() + "]Epoch " + Integer.toString(e) 
						+ "\tMSE: " + df.format(avgMSE) 
				//		+ "\terrorRate: " + df.format(errRate)
						;
				Tools.STDOUT(printOut);				
			}	
		}
	}
	

	/*public static void learnUnbalanceData(Ensemble ens, Dataset data, Parameters para,String outFileName, int weak) {

		try{
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			

			String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + para.getNoOfHidNodes()
								+ "\r\nepochs: " + para.getEpochs() + "\r\n";
			//String printOut = stringOfParameters;
			para.initExtraAlpha(data.getNoOfClasses());
			
			System.out.println(stringOfParameters);
			br.write(stringOfParameters);
			
			data.disorder();
			for(int e = 1;e <= para.getEpochs();e++){
				//data.disorder();
				for(int d = 0;d < data.getNoOfSamples();d++){
					float[] inputs = data.getInputs(d);
					float[] targets = data.getTargets(d);
					
					//if it is a weak sample,set extraAlpha as coefficient,else reset it as 1f
					if(targets[0] == weak)
						para.setExtraAlpha();
					else
						para.resetExtraAlpha();
					
					AVG avg = new AVG();
					para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
					for(int n = 0;n < ens.size();n++){
						ens.getNetwork(n).learn(para, inputs, targets);
					}
				}
				if(e % 5 == 0){
					float AvgMSE = ens.getAvgMSE(data,para);
					float errRate = ens.getErrorRate(data);
					String stringOfAvgMSE = Float.toString(AvgMSE);
					String stringOfErrRate = Float.toString(errRate);
					if(stringOfAvgMSE.length() > 7)
						stringOfAvgMSE = stringOfAvgMSE.substring(0, 7);
					if(stringOfErrRate.length() > 7)
						stringOfErrRate = stringOfErrRate.substring(0, 7);
					String printOut = "Epoch " + Integer.toString(e) + "\r\nMSE: " + stringOfAvgMSE + " \terrorRate: " + stringOfErrRate;
					System.out.println(printOut);
				
					br.write(printOut + "\r\n");
				}
			}
			br.close();
		}
		catch(IOException IOe) {
		    IOe.printStackTrace();
		    System.exit(1);
		}
	}
*/
	/*public Ensemble learnEarlyStop(Ensemble ens, Dataset trainData, Dataset validationData, float glAlpha, float minProgress, int stripSize, Parameters param,String outFileName) {
		param.setNoOfNet(ens.size());//.setEnsembleSize(ens.size());
		
		float trainError = ens.getErrorRate(trainData);
		float validationError = ens.getErrorRate(validationData);
		float minValidationError = validationError;
		Ensemble bestValidationEns = (Ensemble) ens.clone();
		float generalizationLoss = -1F; //100 * (validationError/minValidationError - 1);
		float trainingProgress = -1F;
		float stripTrainErrorSum = 0;
		float minStripTrainError = trainError;
		boolean minTrainingProgressAttained = false;
		
		int maxEpochs = param.getEpochs();
		
		int epoch = 0;
		
		while(epoch < maxEpochs && generalizationLoss <= glAlpha) {
			trainData.disorder();//.shuffleData();

			//int[] correPerPattern = this.getNumberCorrectForEachPattern(data);
			for(int d = 0; d < trainData.getNoOfSamples(); d++){
				float[] inputs = trainData.getInputs(d);//.getInputExample(d);
				float[] targets = trainData.getTargets(d);//.getTargetExample(d);
				AVG avg = new AVG();
				param.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
				for(int net = 0; net < ens.size(); net++){
					ens.getNetwork(net).learn(param,inputs,targets);
				}
			}
			
			trainError = ens.getErrorRate(trainData);//.test(trainData).getErrorRate();
			validationError = ens.getErrorRate(validationData);//.test(validationData).getErrorRate();
			
			// if it is the first epoch of the strip
			// reset the min strip train error and error sum at each new strip
			// and reset the min validation error
			if (epoch % stripSize == 0) {
				minStripTrainError = trainError;
				stripTrainErrorSum = trainError;
				
				// update the min validation error only if the GL5 already started to
				// be calculated
				//if (generalizationLoss != -1F) {
				// Always do that, for it is possible that the generalizationLoss never attain
				// the minimum value <---Minku12/01/07
					minValidationError = validationError;
					bestValidationEns = (Ensemble) ens.clone();
				//}
			}
			else // update min strip train error and error sum
			{
				if (minStripTrainError > trainError) {
					minStripTrainError = trainError;
				}
				
				stripTrainErrorSum += trainError;
				
				//	After we start calculating the generalizationLoss, update the minValidationError
				// at every epoch inside the strip
				//if ( (generalizationLoss != -1F) &&
				//	 (minValidationError >= validationError) )
				//{
				//Always do that, for it is possible that the generalizationLoss never attain
				// the minimum value <---Minku12/01/07
				if (minValidationError >= validationError)
				{
					minValidationError = validationError;
					bestValidationEns = (Ensemble) ens.clone();
				}
			}
			
			
			
			// if it is the last epoch of the strip, calculate the training progress
			if ((epoch+1) % stripSize == 0) {
				
				if (minStripTrainError != 0)
					trainingProgress = 1000 * (stripTrainErrorSum/(stripSize * minStripTrainError) - 1);
				else trainingProgress = 0;
				
				if (trainingProgress <= minProgress)
					minTrainingProgressAttained = true;
				
				// calculate the generalization loss only if the training progress already
				// attained the minimim pre defined value
				if (minTrainingProgressAttained)
				{
					// if it's the first time I'm calculating the generalizationLoss
					if (generalizationLoss == -1F)
					{
						minValidationError = validationError;
						bestValidationEns = (Ensemble) ens.clone();
					}
					
					// update min validation error and the best validation error ens until now
					// only when we are in the last epoch of the strip
					/*else if (minValidationError >= validationError) {
						minValidationError = validationError;
						bestValidationEns = (Ensemble) ens.clone();
					}*/
				
	/*				generalizationLoss = 100 * (validationError/minValidationError - 1);
				}

				
				//System.out.println("Training progress: " + trainingProgress);
				//System.out.println("Generalization loss: " + generalizationLoss);
			}
			
			System.out.println(epoch + ", " + trainError + ", " + validationError + ", " + minStripTrainError + ", " + minValidationError + ", " + trainingProgress + ", " + generalizationLoss);
			epoch++;
			
		}

		System.out.println("Number of training epochs: " + epoch);
		return bestValidationEns;

	}*/

	public static Ensemble learnEarlyStop(Ensemble ens, Dataset trainData, Dataset valiData, float glAlpha, float minProgress, int stripSize,  Parameters para, String outFileName) {
		
		try{
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			
			String stringOfParameters = "alpha: " + para.getAlpha() + "\r\nlambda: " + para.getLambda()
								+ "\r\nnumOfNets: " + para.getNoOfNet() + "\r\nnumOfHidNodes: " + para.getNoOfHidNodes()
								+ "\r\nepochs: " + para.getEpochs() + "\r\nglAlpha: " + glAlpha + "\r\nminProgress: "
								+ minProgress + "\r\nstripSize: " + stripSize + "\r\n";			
			para.initExtraAlpha(trainData.getNoOfClasses());
			System.out.println(stringOfParameters);
			br.write(stringOfParameters);
			
			float GL = -1f;
			float trainingProgress = -1f;
			float sumOfTrainErrRate = 0f;
			float trainErrRate = ens.getErrorRate(trainData);
			float minTrainErrRate = trainErrRate;
			float valiErrRate = ens.getErrorRate(valiData);
			float minValiErrRate = valiErrRate;
			boolean minTrainingProgressAttained = false;
			Ensemble bestValiEns = (Ensemble) ens.clone();
			
			int epochs = para.getEpochs();
			para.resetExtraAlpha();
			//trainData.disorder();
			int e = 0;
			for(e = 0;e < epochs && GL <= glAlpha;e++){
				trainData.disorder();
				for(int d = 0;d < trainData.getNoOfSamples();d++){
					float[] inputs = trainData.getInputs(d);
					float[] targets = trainData.getTargets(d);	
					AVG avg = new AVG();
					para.setEnsAvgOutputs(avg.combineOutputs(ens.getIndividualOutputs(inputs)));
					for(int n = 0;n < ens.size();n++){
						ens.getNetwork(n).learn(para, inputs, targets);
					}
				}				
				trainErrRate = ens.getErrorRate(trainData);
				valiErrRate = ens.getErrorRate(valiData);				
				if(e % stripSize == 0){	//first epoch of the strip
					
					minTrainErrRate = trainErrRate;
					sumOfTrainErrRate = trainErrRate;
					
					//The GL considers only the validation error inside the strip size
					minValiErrRate = valiErrRate;
					bestValiEns = (Ensemble) ens.clone();				
				}
				else{
					if(minTrainErrRate > trainErrRate){
						minTrainErrRate = trainErrRate;
					}
					sumOfTrainErrRate += trainErrRate;
					
					if(minValiErrRate >= valiErrRate){
						minValiErrRate = valiErrRate;
						bestValiEns = (Ensemble) ens.clone();
					}
				}
				
				if((e + 1) % stripSize == 0){	//last epoch of the strip
					if(minTrainErrRate != 0){
						trainingProgress = 1000 * (sumOfTrainErrRate / (stripSize * minTrainErrRate) - 1f);
					}
					else{
						trainingProgress = 0;
					}
					if(trainingProgress <= minProgress){
						minTrainingProgressAttained = true;
					}
					if(minTrainingProgressAttained){
						if(GL == -1f){
							minValiErrRate = valiErrRate;
							bestValiEns = (Ensemble) ens.clone();
						}
						
						//minValiErrRate = valiErrRate;
						//bestValiEns = (Ensemble) ens.clone();
						
						GL = 100 * (valiErrRate / minValiErrRate - 1f);
					}
					
//					float AvgMSE = ens.getAvgMSE(trainData,para);
//					String stringOfAvgMSE = Float.toString(AvgMSE).substring(0, 7);
					String stringOfTrainErrRate = Float.toString(trainErrRate);
					String stringOfValiErrRate = Float.toString(valiErrRate);
					if(stringOfTrainErrRate.length() > 7)
						stringOfTrainErrRate = stringOfTrainErrRate.substring(0, 7);
					if(stringOfValiErrRate.length() > 7)
						stringOfValiErrRate = stringOfValiErrRate.substring(0, 7);
					String printOut = "Epoch " + Integer.toString(e+1)  //+ "\r\nMSE: " + stringOfAvgMSE 
										+ "\ttrainErrorRate: " + stringOfTrainErrRate 
										+ "\tvalidationErrorRate: " + stringOfValiErrRate;
					System.out.println(printOut);				
					br.write(printOut + "\r\n");
				}				
			}
			System.out.println("Number of Epochs: " + e);
			br.write("\r\n\r\nNumber of Epochs: " + e);
			br.close();
			return bestValiEns;
		}
		catch(IOException IOe) {
		    IOe.printStackTrace();
		    System.exit(1);
		}
		return null;
	}
	

}
