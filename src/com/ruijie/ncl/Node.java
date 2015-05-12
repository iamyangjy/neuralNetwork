package com.ruijie.ncl;

import java.io.Serializable;
import java.util.Random;

public abstract class Node implements Cloneable,Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected float[] prDeltaW;
	
	protected float[] weights;
	protected float[] deltaWeights;

	//当前神经网络的偏差值
	protected float bias;
	//本次迭代对偏差值的修改
	protected float deltaBias;

	public String NodeFunction;
	
	public static final int LINEAR = 0;
	public static final int LOGISTIC = 1;
	public static final int TANSIG = 2;
	
	
	/*
	//public static Node createNode(String s){
	//	StringTokenizer st = new StringTokenizer(s,);
	//	String function = st.nextToken();
	//	if(function.equals("linear")){
	//		return new LinearNode
	//	}
	//}
	 */

	public abstract float getOutput(float[] inputs);
	
	public abstract float getDerivative(float output);
	
	public float calculateInput(float[] inputs){
		return Matrix.sum(Matrix.dotProduct(weights, inputs)) + bias;
	}
	
	public int getNoOfConnections(){
		return weights.length;
	}
	
	public void initial(float rangeW,float rangeB){
		Random ran = new Random();
		for(int i = 0;i < weights.length;i++){
			weights[i] = 2f * (ran.nextFloat() - 0.5f) * rangeW;
		}
		bias = 2f * (ran.nextFloat() - 0.5f) * rangeB;
	}
	
	public void resetDelta(){
		for(int i = 0;i < deltaWeights.length;i++){
			deltaWeights[i] = 0;
		}
		deltaBias = 0;
	}
	
	public void setWeights(float[] w){
		if(weights.length != w.length){
			System.err.println("Set weights Error!The length isn't matched!");
			System.exit(1);
		}
		for(int i = 0;i < weights.length;i++){
			setWeight(i,w[i]);
		}
	}
	
	public void setWeight(int i,float w){
		weights[i] = w;
	}
	
	public float[] getWeights(){
		return weights;
	}
	
	public float getWeight(int i){
		return weights[i];
	}
	
	public void setBias(float b){
		bias = b;
	}
	
	public float getBias(){
		return bias;
	}
	
	public void update(Parameters para,float[] layerInputs,float sensitivity){
		float alpha = para.getAlpha();
		float momentum = para.getMomentum();
		for(int i = 0;i < weights.length;i++){
			prDeltaW[i] = (-1f) * alpha * sensitivity * layerInputs[i] + 
					momentum * prDeltaW[i];
			weights[i] += prDeltaW[i];
		}
		bias += (-1f) * alpha * sensitivity;
	}
	
	public void evaluateDelta(float alpha,float[] layerInputs,float sensitivity){
		for(int i = 0;i < deltaWeights.length;i++){
			deltaWeights[i] += (-1f) * alpha * sensitivity * layerInputs[i];
		}
		deltaBias += (-1f) * alpha * sensitivity;
	}
	public void updataBatch(){
		for(int i = 0;i < weights.length;i++){
			weights[i] += deltaWeights[i];
		}
		bias += deltaBias;
		resetDelta();
	}
	
	public Object clone(){
		Node newNode = null;
		try{
			newNode = (Node)super.clone();
			newNode.weights = new float[weights.length];
			newNode.setWeights((float[])this.weights);
			newNode.setBias(this.getBias());
		}catch(CloneNotSupportedException e){
			System.err.println("Node can't clone.");
			System.exit(0);
		}
		return newNode;
	}

}
