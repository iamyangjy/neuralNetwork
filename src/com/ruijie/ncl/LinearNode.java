package com.ruijie.ncl;

import java.util.StringTokenizer;

public class LinearNode extends Node{
	
	private static final long serialVersionUID = 1L;
	
	public LinearNode(int noOfConnection){
		NodeFunction = "linear";
		//神经网络当前的连接权值
		weights = new float[noOfConnection];
		//本次迭代对权值的修改值
		deltaWeights = new float[noOfConnection];
	    //上次迭代对权值的修改值
		prDeltaW = new float[noOfConnection];
	}
	
	public LinearNode(String w,String b){
		NodeFunction = "linear";
		StringTokenizer s = new StringTokenizer(w," ");
		weights = new float[s.countTokens()];
		deltaWeights = new float[s.countTokens()];
		for(int i = 0;i < weights.length;i++){
			weights[i] = new Float(s.nextToken()).floatValue();
		}
		bias  = new Float(b).floatValue();
		prDeltaW = new float[weights.length];
	}

	//导数值
	public float getDerivative(float output){
		return 1;
	}

	//神经元响应函数(需要满足可微)
	public float getOutput(float[] inputs){
		return super.calculateInput(inputs);
	}


}
