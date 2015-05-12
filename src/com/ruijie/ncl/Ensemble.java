package com.ruijie.ncl;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Vector;

public class Ensemble extends Vector<Network> implements Serializable,Cloneable{
	
	private static final long serialVersionUID = 1L;
	
	protected CombinationScheme combiner;
	
	
	
	public Ensemble(int noOfnet,CombinationScheme com){
		super(noOfnet);
		combiner = com;
	}
	
	public CombinationScheme getCombinationScheme(){
		return combiner;
	}
	
	public void setCombinationScheme(CombinationScheme com){
		combiner = com;
	}
	
	
	public void createEnsemble(int in,int hid,int out,int hidFunc,int outFunc) throws UnknownNodeFunction{
		for(int i = 0;i < this.capacity();i++){
			Network net = new Network(in,hid,out);
			net.connect(hidFunc,outFunc);
			this.add(i,net);
		}
	}

	public void initialEnsemble(float rangeHidW,float rangeOutW,float rangeB){
		for(int i = 0;i < this.size();i++){
			this.get(i).initialWeights(rangeHidW,rangeOutW, rangeB);
		}
	}
	
	public Network getNetwork(int i){
		return this.get(i);
	}
	
	public void addNetwork(Network net){
		this.ensureCapacity(this.size() + 1);
		this.add(net);
	}
	
	public void addEnsemble(Ensemble ens){
		//this.ensureCapacity(this.size() + ens.size());
		for(int i = 0;i < ens.size();i++){
			addNetwork(ens.getNetwork(i));
		}
	}
	
	public Ensemble getEnsemble(int beginIndex,int endIndex){
		Ensemble ens = new Ensemble(0, combiner);
		for(int i = beginIndex;i < endIndex;i++){
			ens.addNetwork((Network)this.getNetwork(i).clone());
		}
		return ens;
	}
	
	public Ensemble getEnsemble(int[] selected){
		Ensemble ens = new Ensemble(0, combiner);
		for(int i = 0;i < selected.length;i++){
			if(selected[i] == 1){
				ens.addNetwork((Network)this.getNetwork(i).clone());
			}
		}
		return ens;
	}
	
	public void replace(int[] selected, Ensemble ens){
		int k = 0;
		for(int i = 0;i < selected.length;i++){
			if(selected[i] == 1){
				/*this.deleteNetwork(i);
				this.add(i, (Network)ens.getNetwork(k++).clone());*/
				this.set(i, (Network)ens.getNetwork(k++).clone());
			}
		}
	}
	
	public void deleteNetwork(int index){
		this.remove(index);
	}
	
	public float[][] getIndividualOutputs(float[] inputs){
		float[][] outputs = new float[this.size()][];
		for(int i = 0;i < this.size();i++){
			outputs[i] = this.getNetwork(i).getOutputs(inputs);
		}
		return outputs;
	}
	
	public float[] getOutputs(float[] inputs){
		float[][] outputs = getIndividualOutputs(inputs);
		return combiner.combineOutputs(outputs);
	}
	
	public float[][] getOutputsOfAllSamples(float[][] inputs){
		float[][] outputsOfAllSamples = new float[inputs.length][];
		for(int i = 0;i < inputs.length;i++){
			outputsOfAllSamples[i] = getOutputs(inputs[i]);
		}
		return outputsOfAllSamples;
	}
	
	public float[][][] getOutputsOfAllNetworks(float[][] inputs){
		float[][][] allOutputs = new float[this.size()][][];
		for(int i = 0;i < allOutputs.length;i++){
			allOutputs[i] = this.getNetwork(i).getOutputs(inputs); //N*nc
		}
		return allOutputs;
	}
	
	public float[][][] getIndividualOutputsOfAllSamples(float[][] inputs){
		float[][][] allIndiOutputs = new float[inputs.length][][];
		for(int i = 0;i < inputs.length;i++){
			allIndiOutputs[i] = getIndividualOutputs(inputs[i]).clone();
		}
		return allIndiOutputs;
	}
	
	public float calculateAUC_norOutputs(Dataset data){
		
		float[][] tmpOutputs = getOutputsOfAllSamples(data.getInputs());
		float[][] outputs = Matrix.norEachLineToPredefinedSum(tmpOutputs, 1f);
		float[][] targets = data.getTargets();
		
		//float[][] norOutputs = Matrix.norEachLineToPredefinedSum(outputs, 1f);
		
		int numClass = data.getNoOfClasses();
		float auc = 0;
		for(int i = 0;i < numClass;i++){
			for(int k = i + 1;k < numClass;k++){
				int count = 0;
				for(int d = 0;d < targets.length;d++){
					if(targets[d][i] == 1 || targets[d][k] == 1)count++;
				}
				float[] score_i = new float[count];
				float[] score_k = new float[count];
				float[] targetsLabel_i = new float[count];
				float[] targetsLabel_k = new float[count];
				int index = 0;
				for(int d = 0;d < targets.length;d++){
					if(targets[d][i] == 1 || targets[d][k] == 1){
						score_i[index] = outputs[d][i];
						score_k[index] = outputs[d][k];
						targetsLabel_i[index] = targets[d][i];
						targetsLabel_k[index] = targets[d][k];
						index++;
					}
				}
				auc += calculateAUC(score_i,targetsLabel_i) + calculateAUC(score_k,targetsLabel_k);
			}
		}
		auc = auc / (1f * numClass * (numClass - 1));
		return auc;
	}
	
	public float calculateAUC(Dataset data){
		
		float[][] outputs = getOutputsOfAllSamples(data.getInputs());
		float[][] targets = data.getTargets();
		
		//float[][] norOutputs = Matrix.norEachLineToPredefinedSum(outputs, 1f);
		
		int numClass = data.getNoOfClasses();
		float auc = 0;
		for(int i = 0;i < numClass;i++){
			for(int k = i + 1;k < numClass;k++){
				int count = 0;
				for(int d = 0;d < targets.length;d++){
					if(targets[d][i] == 1 || targets[d][k] == 1)count++;
				}
				float[] score_i = new float[count];
				float[] score_k = new float[count];
				float[] targetsLabel_i = new float[count];
				float[] targetsLabel_k = new float[count];
				int index = 0;
				for(int d = 0;d < targets.length;d++){
					if(targets[d][i] == 1 || targets[d][k] == 1){
						score_i[index] = outputs[d][i];
						score_k[index] = outputs[d][k];
						targetsLabel_i[index] = targets[d][i];
						targetsLabel_k[index] = targets[d][k];
						index++;
					}
				}
				auc += calculateAUC(score_i,targetsLabel_i) + calculateAUC(score_k,targetsLabel_k);
			}
		}
		auc = auc / (1f * numClass * (numClass - 1));
		return auc;
	}
	
	public static float calculateAUC(float[] score,float[] targets){
		//float auc = 0;
		int[] sortedIndex = Matrix.QuickSort(score);
		return calculateAUC(score,sortedIndex,targets);
		/*float[] rank = new float[score.length];
		for(int i = 0;i < rank.length;i++){
			int tempCount = 0,tempIndex = i;
			float sumRank = 0;
			for(;tempIndex < score.length && score[tempIndex] == score[i];
						tempIndex++,tempCount++){
				sumRank += (float)tempIndex;
			}
			for(int k = i;k < tempIndex;k++){
				rank[k] = sumRank / (float)tempCount + 1;
			}
		}
		float SP = 0;
		for(int i = 0;i < sortedIndex.length;i++){
			if(targets[sortedIndex[i]] == 1){
				SP += rank[i];
			}
		}
		float totalNum = targets.length;
		float P = Matrix.sum(targets);
		float N = totalNum - P;
		auc = (SP - P * (P + 1f) / 2f) / (P * N);
		return auc;*/
	}
	
	
	public static float calculateAUC(float[] score,int[] index,float[] targets){
		float auc = 0;
		float[] rank = new float[index.length];
		for(int i = 0;i < rank.length;){
			int tempCount = 0,tempIndex = i;
			float sumRank = 0;
			for(;tempIndex < index.length && score[tempIndex] == score[i];
						tempIndex++,tempCount++){
				sumRank += (float)tempIndex;
			}
			for(int k = i;k < tempIndex;k++){
				rank[k] = sumRank / (float)tempCount + 1;
			}
			i = tempIndex;
		}
		float SP = 0;
		for(int i = 0;i < index.length;i++){
			if(targets[index[i]] == 1){
				SP += rank[i];
			}
		}
		float totalNum = targets.length;
		float P = Matrix.sum(targets);
		float N = totalNum - P;
		auc = (SP - P * (P + 1f) / 2f) / (P * N);
		return auc;
	}
	
	/*private float calculateAUC(float[] score,float[] targets){
		
		 * targets are 0 or 1
		 
		float auc = 0;
		int[] sortedIndex = Matrix.sortIndexUp(score);
		float totalNum = targets.length;
		float P = Matrix.sum(targets);
		float N = totalNum - P;
		
		if(P == 0 || N == 0)return 0;
		
		float preTPR = 0, preFPR = 0;
		float TP = 0, FP = 0;
		for(int i = sortedIndex.length-1;i >= 0;i--){
			TP += targets[sortedIndex[i]];
			FP += 1 - targets[sortedIndex[i]];
			float TPR = TP / P;
			float FPR = FP / N;
			auc += (TPR + preTPR) * (FPR - preFPR) / 2f;
			preTPR = TPR;
			preFPR = FPR;
		}
		
		return auc;
	}*/
	
	public float calculateGmean(Dataset data){
		float gmean = 1f;
		float[] accEachC = this.getAccEachC(data);
		for(int i = 0;i < accEachC.length;i++){
			gmean *= accEachC[i];
		}
		gmean = (float)Math.pow((double)gmean, 1d/(double)accEachC.length);
		return gmean;
	}
	
	public float[] getMSE(Dataset data,Parameters para){
		//float[][] inputs = data.getInputs();
		//float[][] targets = data.getTargets();
		float[] mse = new float[data.getNoOfClasses()];
		for(int d = 0;d < data.getNoOfSamples();d++){
			float[][] outputs = this.getIndividualOutputs(data.getInputs(d));
			
			AVG avg = new AVG();
			para.setEnsAvgOutputs(avg.combineOutputs(outputs));
			
			float[] targets = data.getTargets(d);
			float[] sumMseOfAllData = new float[mse.length];
			for(int i = 0;i < this.size();/*para.getNoOfNet();*/i++){
				float[] estimate = outputs[i];
				
				//AVG avg =new AVG();
				//float[] ensAvg = avg.combineOutputs(getInidividualOutputs(data.getInputs(d)));
				
				for(int node = 0;node < data.getNoOfClasses();node++){
					ErrorParameters errPara = new ErrorParameters(estimate[node],targets[node],para.getEnsAvgOutputs()[node],para.getLambda());
					sumMseOfAllData[node] += ErrorFunction.getNCLError(errPara);
				}
			}
			for(int node = 0;node < data.getNoOfClasses();node++){
				mse[node] += sumMseOfAllData[node] / this.size();/*para.getNoOfNet();*/
			}
		}
		for(int node = 0;node < mse.length;node++){
			mse[node] = mse[node] / (1f * data.getNoOfSamples());
		}
		return mse;
	}
	
	
	
	public float getAvgMSE(Dataset data,Parameters para){
		float[] mse = getMSE(data,para);
		return Matrix.mean(mse);
	}

	public static float getAvgMSE_Selective(int[] x,float[][][] singleOutputs,
										float[][] targetsOfAllData,Parameters para){
		/*
		 * singleOutputs[numData][numNet][numClass]
		 */
		float[] mse = new float[singleOutputs[0][0].length];
		int numNet = 0;
		for(int k = 0;k < x.length;k++){
			numNet += x[k];
		}
		for(int d = 0;d < singleOutputs.length;d++){
			float[][] outputsTemp = singleOutputs[d];
			float[][] outputs = new float[numNet][outputsTemp[0].length];
			for(int i = 0,j = 0;i < outputs.length;j++){
				if(x[j] == 1f){
					outputs[i] = outputsTemp[j];
					i++;
				}
			}
			
			AVG avg = new AVG();
			para.setEnsAvgOutputs(avg.combineOutputs(outputs));
			
			float[] targets = targetsOfAllData[d];
			float[] sumMseOfAllData = new float[mse.length];
			for(int i = 0;i < numNet;i++){
				float[] estimate = outputs[i];
				
				//AVG avg =new AVG();
				//float[] ensAvg = avg.combineOutputs(getInidividualOutputs(data.getInputs(d)));
				
				for(int node = 0;node < singleOutputs[0][0].length;node++){
					ErrorParameters errPara = new ErrorParameters(estimate[node],targets[node],para.getEnsAvgOutputs()[node],para.getLambda());
					sumMseOfAllData[node] += ErrorFunction.getNCLError(errPara);
				}
			}
			for(int node = 0;node < singleOutputs[0][0].length;node++){
				mse[node] += sumMseOfAllData[node] / numNet;
			}
		}
		for(int node = 0;node < mse.length;node++){
			mse[node] = mse[node] / (1f * singleOutputs.length);
		}
		
		return Matrix.mean(mse);
	}
	/*public float getAvgMSE_Selective(float[] x,Dataset data,Parameters para){
		Ensemble ens = (Ensemble) this.clone();
		for(int i = 0,k = 0;i < x.length;i++){
			if(x[i] == 1)
				k++;
			else{
				ens.deleteNetwork(k);
				para.setNoOfNet(para.getNoOfNet()-1);
			}
		}
		
		return ens.getAvgMSE(data, para);
	}*/
	
	public void learn(Dataset data,Parameters para){
		NCL.learn(this, data, para);
	}
	public void learnSortByMargin(Dataset data,Parameters para,float[] auc, float[] mse,
			float[] testAUC, Dataset testdata){
		NCL.learnSortByMargin(this, data, para,auc, mse, testAUC, testdata);
	}
	public void learnSBP(Dataset data,Parameters para,float[] auc, float[] mse, float[] testAUC, Dataset testdata){
		NCL.learnSBP(this, data, para,auc, mse, testAUC, testdata);
	}
	public void learnSBP_samEpoch(Dataset data,Parameters para,float[] auc, float[] gmean, float[] testAUC,float[] testGmean, Dataset testdata){
		NCL.learnSBP_sampleEpoch(this, data, para,auc,gmean, testAUC, testGmean, testdata);
	}
	public void learnDys(Dataset data, Parameters para,
			float[] auc, float[] mse, 
			float[] testAUC, Dataset testdata){
		NCL.learnDys(this, data, para, auc, mse,  testAUC,testdata);
	}
	public void learnDys_samEpoch(Dataset data, Parameters para,
			float[] auc, float[] gmean, 
			float[] testAUC, float[] testGmean, int[] trTimeEachClass, Dataset testdata){
		NCL.learnDys_sampleEpoch(this, data, para, auc, gmean,  testAUC, testGmean, trTimeEachClass,testdata);
	}
	public void learnDys_samEpoch_useMargin(Dataset data, Parameters para,
			float[] auc, float[] gmean, 
			float[] testAUC, float[] testGmean, int[] trTimeEachClass, Dataset testdata){
		NCL.learnDys_sampleEpoch_useMargin(this, data, para, auc, gmean,  testAUC, testGmean, trTimeEachClass,testdata);
	}
	public void learnDys_samEpoch_useMargin_noNorOut(Dataset data, Parameters para,
			float[] auc, float[] gmean, 
			float[] testAUC, float[] testGmean, int[] trTimeEachClass, Dataset testdata){
		NCL.learnDys_sampleEpoch_useMargin_noNorOut(this, data, para, auc, gmean,  testAUC, testGmean, trTimeEachClass,testdata);
	}
	public void learnDys_CountTrainTime(Dataset data, Parameters para,
			float[] auc, float[] testAUC, int[] trTimeEachSample, Dataset testdata,float rate){
		NCL.learnDys_CountTrainTime(this, data, para, auc, testAUC, trTimeEachSample, testdata,rate);
	}
	public void learnDysNoNor(Dataset data, Parameters para,
			float[] auc, float[] mse, 
			float[] testAUC,int[] trTimeEachClass, Dataset testdata){
		NCL.learnDysNoNor(this, data, para, auc, mse,  testAUC, trTimeEachClass,testdata);
	}
	
	public void learnDys_selByP( Dataset data, Parameters para,
			float[] auc, float[] mse,  
			float[] testAUC, Dataset testdata){
		NCL.learnDys_selByP(this, data, para, auc, mse, testAUC, testdata);
	}
	public void learnALR(Dataset data, Parameters para, float[][] costMat,
			float[] auc, float[] mse, 
			float[] testAUC, Dataset testdata){
		NCL.learnALR(this, data, para,costMat, auc, mse,  testAUC, testdata);
	}
	public void learnALR_samEpoch(Dataset data,Parameters para,float[][] costMat,
			float[] auc, float[] mse, float[] testAUC, Dataset testdata){
		NCL.learnALR_sampleEpoch(this, data, para,costMat,auc, mse, testAUC, testdata);
	}
	public void learnMMC(Dataset data, Parameters para, float[][] costMat,
			float[] auc, float[] mse, 
			float[] testAUC, Dataset testdata){
		NCL.learnMMC(this, data, para,costMat, auc, mse,  testAUC, testdata);
	}
	public void learnMMC_samEpoch(Dataset data,Parameters para,float[][] costMat,
			float[] auc, float[] gmean, float[] testAUC, float[] testGmean, Dataset testdata){
		NCL.learnMMC_sampleEpoch(this, data, para,costMat,auc, gmean, testAUC, testGmean, testdata);
	}
	public void learn(Dataset data,Parameters para,int firstEpoch,int stripSize){
		NCL.learn(this, data, para,firstEpoch,stripSize);
	}
	
	
	
	public void learnBatchMode( Dataset data, Parameters para) {
		//all NNs will be updated 
		NCL.learnBatchMode(this, data, para);
	}
	/*
	public void learnUnbalanceData(Dataset data,Parameters para,String outFileName,int weak){
		NCL.learnUnbalanceData(this, data, para, outFileName, weak);
	}*/
	
	
	public int getPrediction(float[] inputs){	
		//return the prediction class lable:0,1,2...
		return Matrix.maxLocation(this.getOutputs(inputs));
	}
	
	public float getErrorRate(Dataset data){
		float errRate = 0f;
		for(int i = 0;i < data.getNoOfSamples();i++){
			float[] inputs = data.getInputs(i);
			float[] targets = data.getTargets(i);
			int pred = getPrediction(inputs);
			if(pred >= targets.length){
				/*
				 * There new classes
				 */
				errRate += 1f;
			}else if(targets[pred] != 1){
				errRate += 1f;
			}
		}
		return errRate / (data.getNoOfSamples() * 1f);
	}
	
	public int[][] getDistributionForNN(Dataset data){
		int[][] distr = new int[this.size()][data.getNoOfSamples()];
		for(int i = 0;i < distr.length;i++){
			for(int k = 0;k < distr[i].length;k++){
				float[] inputs = data.getInputs(k);
				float[] targets = data.getTargets(k);
				int pre = this.getNetwork(i).getPrediction(inputs);
				if(targets[pre] == 1){
					distr[i][k] = 1;
				}
			}
		}
		return distr;
	}
	
	
	public int[] getFP_FN(Dataset data){
		
		int TP = 0, TN = 0, FP = 0, FN = 0;
		for(int i = 0;i < data.getNoOfSamples();i++){
			float[] inputs = data.getInputs(i);
			float[] targets = data.getTargets(i);
			int prediction = getPrediction(inputs);
			if(prediction == 0){
				if(targets[prediction] == 1)TP++;
				else FP++;
			}else{
				if(targets[prediction] == 1)TN++;
				else FN++;
			}
		}
		int[] ret = {TP,TN,FP,FN};
		return ret;
	}
	
	public float[] getSeparateErrorRate(Dataset data){
		/*
		 * FP rate && FN rate
		 */
		float[] separateRight = new float[data.getNoOfClasses()];
		float[] separateErrRate = new float[data.getNoOfClasses()];
		float[] noOfSeparateData = new float[data.getNoOfClasses()];
		for(int i = 0;i < data.getNoOfSamples();i++){
			float[] inputs = data.getInputs(i);
			float[] targets = data.getTargets(i);
			
			for(int c = 0;c < noOfSeparateData.length;c++){
				if(targets[c] == 1f)
					noOfSeparateData[c] += 1f;
			}
			int prediction = getPrediction(inputs);
			if(targets[prediction] == 1){
				separateRight[prediction] += 1f;
			}
		}
		for(int c = 0;c < separateErrRate.length;c++){
			separateErrRate[c] = 1f - separateRight[c] / noOfSeparateData[c];
		}
		return separateErrRate;
	}
	
	public float[] getAccEachC(Dataset data){
		/*
		 * FP rate && FN rate
		 */
		float[] accEachC = new float[data.getNoOfClasses()];
		float[] noOfEachC = new float[data.getNoOfClasses()];
		for(int i = 0;i < data.getNoOfSamples();i++){
			float[] inputs = data.getInputs(i);
			float[] targets = data.getTargets(i);
			for(int c = 0;c < noOfEachC.length;c++){
				if(targets[c] == 1f){
					noOfEachC[c] += 1f;
					break;
				}
			}
			int prediction = getPrediction(inputs);
			if(targets[prediction] == 1){
				accEachC[prediction] += 1f;
			}
		}
		for(int c = 0;c < accEachC.length;c++){
			accEachC[c] = accEachC[c] / noOfEachC[c];
		}
		return accEachC;
	}
	
	
	public float accuracyOfSelective(float[] x,float[][][] singleOutputs,
			float[][] targets){
		float correct = 0;
		int numNet = 0;
		for(int k = 0;k < x.length;k++){
			numNet += x[k];
		}
		for(int d = 0;d < singleOutputs.length;d++){
			float[][] outputsTemp = singleOutputs[d];
			float[][] outputs = new float[numNet][outputsTemp[0].length];
			for(int i = 0,j = 0;i < outputs.length;j++){
				if(x[j] == 1f){
					outputs[i] = outputsTemp[j];
					i++;
				}
			}
			int pre = Matrix.maxLocation(this.combiner.combineOutputs(outputs));
			if(targets[d][pre] == 1){
				correct += 1f;
			}
		}
		return correct / (singleOutputs.length * 1f);
	}
	
	public float[] getFitness(Dataset data){
		float[] fitness = new float[this.size()];
		int[] correctIndex = new int[this.size()];//the NN index that is correct to a pattern
		
		for(int d = 0;d < data.getNoOfSamples();d++){
			float[] input = data.getInputs(d);
			float[] target = data.getTargets(d);
			int numOfCorrectNN = 0;
			for(int n = 0;n < this.size();n++){
				Network net = this.getNetwork(n);
				float[] output = net.getOutputs(input);
				int predic = Matrix.maxLocation(output);
				if(target[predic] == 1){
					//the NN is correctly learned to this pattern
					correctIndex[numOfCorrectNN++] = n;
				}
			}
			for(int i = 0;i < numOfCorrectNN;i++){
				fitness[correctIndex[i]] += 1f/numOfCorrectNN;
			}
		}
		return fitness;
	}
	
	/*public float accuracyOfSelective(float[] x,Dataset data){
		Ensemble ens = (Ensemble) this.clone();
		for(int i = 0,k = 0;i < x.length;i++){
			if(x[i] == 1)
				k++;
			else
				ens.deleteNetwork(k);
		}
		return 1f - ens.getErrorRate(data);
	}
	
	public float getSumOfRightOutputs(Dataset data){
		//get the summary of the outputs of the data which is correctly classified
		float sum = 0;
		float[][] inputs = data.getInputs();
		float[][] targets = data.getTargets();
		for(int i = 0;i < inputs.length;i++){
			int pre = this.getPrediction(inputs[i]);
			if(targets[i][pre] == 1){
				sum += this.getOutputs(inputs[i])[pre];
			}
		}
		return sum;
	}
	
	public float getSumOfRightOutputs(float[] x ,Dataset data){
		Ensemble ens = (Ensemble) this.clone();
		for(int i = 0,k = 0;i < x.length;i++){
			if(x[i] == 1)
				k++;
			else
				ens.deleteNetwork(k);
		}
		return ens.getSumOfRightOutputs(data);
	}
	*/
	
	public float getSumOfRightOutputs(float[] x,
				float[][][] singleOutputs,float[][] targets){
		float sum = 0;
		int numNet = 0;
		for(int k = 0;k < x.length;k++){
			numNet += x[k];
		}
		for(int d = 0;d < singleOutputs.length;d++){
			float[][] outputsTemp = singleOutputs[d];
			float[][] outputs = new float[numNet][outputsTemp[0].length];
			for(int i = 0,j = 0;i < outputs.length;j++){
				if(x[j] == 1f){
					outputs[i] = outputsTemp[j];
					i++;
				}
			}
			int pre = Matrix.maxLocation(this.combiner.combineOutputs(outputs));
			if(targets[d][pre] == 1){
				sum += Matrix.meanOfEachRow(outputs)[pre];
			}
		}
		return sum;
	}
	
	public float[] getPred(float[] seriesIn, int num){
		float[] pred = new float[num]; 
		pred[0] = this.getOutputs(seriesIn)[0];
		for(int i = 1;i < pred.length;i++){
			for(int k = seriesIn.length - 1;k > 0;k--){
				seriesIn[k] = seriesIn[k - 1];
			}
			seriesIn[0] = pred[i-1];
			pred[i] = this.getOutputs(seriesIn)[0];
		}
		return pred;
	}
	
	public float getRMS(float[] series, int deltaT, int step){
		/*
		 * series is the series of the problem.
		 * numIn is the number of inputs.
		 * deltaT is the interval for get inputs: x(t),x(t - deltaT),x(t - 2*deltaT)...
		 * step is the prediction step, i.e., to predict x(t + (step-1)*deltaT).
		 */
		float e = 0;
		int numIn = this.getNetwork(0).getNumOfInputs();
		for(int i = numIn * deltaT;i < series.length - deltaT * step;i++){
			float[] seriesIn = new float[numIn];
			for(int k = 0,m = i;k < seriesIn.length;k++,m -= deltaT){
				seriesIn[k] = series[m];
			}
			float xPred = this.getPred(seriesIn, step)[step - 1];
			e += (xPred - series[i + deltaT*step]) * (xPred - series[i + deltaT*step]);
		}
		e = (float)Math.sqrt(e / (series.length - deltaT * (numIn + 1)));
		float sigma = 0;
		float mean = Matrix.mean(series);
		for(int i = 0;i < series.length;i++){
			sigma += (series[i] - mean) * (series[i] - mean);
		}
		sigma = (float)Math.sqrt(sigma / series.length);
		e /= sigma;
		return e;
	}
	
	public Object clone(){
		Ensemble newEnsemble = (Ensemble)super.clone();
		for(int i = 0;i < this.size();i++){
			newEnsemble.set(i, (Network)this.getNetwork(i).clone());
		}
		newEnsemble.setCombinationScheme(this.getCombinationScheme());
		return newEnsemble;
	}
	
	public void saveEnsemble(String filename) throws IOException{
		
			FileOutputStream fileOutStr = new FileOutputStream(filename);
			ObjectOutputStream ObjOutStr = new ObjectOutputStream(fileOutStr);
			ObjOutStr.writeObject(this);
			ObjOutStr.close();
		
	}
	
	public static Ensemble loadEnsemble(String filename) throws ClassNotFoundException{
		File myFile = new File(filename);
		Ensemble ens = null;
		try{
			FileInputStream fileInStr = new FileInputStream(myFile);
			ObjectInputStream ObjInStr = new ObjectInputStream(fileInStr);
			ens = (Ensemble)ObjInStr.readObject();
			ObjInStr.close();
		}
		catch(IOException IOe){
			System.err.println("Load ensemble err.");
			System.exit(1);
		}
		return ens;
	}
	
	public static Ensemble readWeights(String path, int numNets, CombinationScheme com,
			int in,int hid,int out,int hidFunc,int outFunc) 
					throws UnknownNodeFunction, IOException{
		
		Ensemble ens = new Ensemble(numNets,com);
		ens.createEnsemble(in, hid, out, hidFunc, outFunc);
		String token = "\t";
		for(int i = 0;i < ens.size();i++){
			String hidFile = path + "net_"+ (i + 1) +"__Inputs-Hids.txt";
			String outFile = path + "net_" + (i + 1) + "__Hids-Outputs.txt";
			float[][] hidWeights = Matrix.loadMatrix(hidFile, token);
			float[][] outputWeights = Matrix.loadMatrix(outFile, token);
			ens.get(i).setWeights(hidWeights, outputWeights);
		}
		return ens;
	}
	
	public void saveWeight(String path){
		/*
		 * EnsFilename：原来保存Ensemble的文件名
		 * outFilename：新的文件名的前缀
		 * 
		 * Ensemble中的第i个网络文件名如下
		 * （设输入的特征数为d，隐层结点数为h，输出结点数为o）
		 * 输入层和隐层结点连接矩阵 (h X d)：outFilename_net_i__Inputs-Hids.txt
		 * 隐层和输出结点连接矩阵 (o X h)：outFilename_net_i__Hids-Outputs.txt
		 * 
		 */
		
		DecimalFormat df = new DecimalFormat("0.00000"); 
		int numOfNets = this.size();
		for(int i = 0;i < numOfNets;i++){
			int numInputs = this.getNetwork(i).getNumOfInputs();
			Layer hidLayer = this.getNetwork(i).getHidLayer();
			Layer outLayer = this.getNetwork(i).getOutputLayer();
			int numHid = hidLayer.size();
			int numOut = outLayer.size();
			float[][] hidWeights = new float[numHid][numInputs];
			float[][] outWeights = new float[numOut][numHid];
			float[] hidBias = hidLayer.getBias();
			float[] outBias = outLayer.getBias();
			for(int j = 0;j < hidWeights.length;j++){
				 hidWeights[j] = hidLayer.getNode(j).getWeights();
			}
			for(int j = 0;j < outWeights.length;j++){
				outWeights[j] = outLayer.getNode(j).getWeights();
			}
			
			BufferedWriter brInput_Hid = null;
			BufferedWriter brHid_Output = null;
			BufferedWriter brHidBias = null;
			BufferedWriter brOutBias = null;
			String hidWeightsName = path + "net_" + (i + 1) + "__Inputs-Hids.txt";
			String outWeightsName = path + "net_" + (i + 1) + "__Hids-Outputs.txt";
			String hidBiasName = path + "net_" + (i + 1) + "__hidBias.txt";
			String outBiasName = path + "net_" + (i + 1) + "__outBias.txt";
			try {
				brInput_Hid = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hidWeightsName)));
				brHid_Output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outWeightsName)));
				brHidBias = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hidBiasName)));
				brOutBias = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outBiasName)));
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}
			try{
				for(int j = 0;j < hidWeights.length;j++){
					for(int k = 0;k < hidWeights[j].length;k++){
						brInput_Hid.write(df.format((hidWeights[j][k])) + "\t");
					}
					
					brInput_Hid.write("\r\n");
				}
				brInput_Hid.close();
				for(int j = 0;j < outWeights.length;j++){
					for(int k = 0;k < outWeights[j].length;k++){
						brHid_Output.write(df.format((outWeights[j][k])) + "\t");
					}
					
					brHid_Output.write("\r\n");
				}
				brHid_Output.close();
				for(int j = 0;j < hidBias.length;j++){
					brHidBias.write(df.format(hidBias[j]) + "\t");
				}
				brHidBias.close();
				for(int j = 0;j < outBias.length;j++){
					brOutBias.write(df.format(outBias[j]) + "\t");
				}
				brOutBias.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
