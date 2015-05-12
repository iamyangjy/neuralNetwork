package com.ruijie.ncl;

import java.io.Serializable;

public class Network implements Serializable,Cloneable {
	
	private static final long serialVersionUID = 1L;
	
	protected int noOfInputs;
	protected Layer hidLayer;
	protected Layer outputLayer;
	
	public Network(int in,int hid,int out){
		noOfInputs = in;
		hidLayer = new Layer(hid);
		outputLayer = new Layer(out);		
	}
	
	public int getNumOfInputs(){
		return noOfInputs;
	}
	
	public Layer getHidLayer(){
		return hidLayer;
	}
	
	public Layer getOutputLayer(){
		return outputLayer;
	}
	
	public void connect(int hidNodeFunction,int outputNodeFunction) throws UnknownNodeFunction{
		this.connectHidLayer(hidNodeFunction);
		this.connectOutputLayer(outputNodeFunction);
	}
	
	private void connectHidLayer(int function) throws UnknownNodeFunction{
		switch(function){
		case Node.LINEAR:
			hidLayer.linearConnection(noOfInputs);
			break;
		case Node.LOGISTIC:
			hidLayer.logisticConnection(noOfInputs);
			break;
		default:
			throw new UnknownNodeFunction(function);	
		}
		
	}
	
	private void connectOutputLayer(int function) throws UnknownNodeFunction{
		switch(function){
		case Node.LINEAR:
			outputLayer.linearConnection(hidLayer.size());
			break;
		case Node.LOGISTIC:
			outputLayer.logisticConnection(hidLayer.size());
			break;
		default:
			throw new UnknownNodeFunction(function);	
		}
	}

	public void initialWeights(float rangeHidW,float rangeOutW,float rangeB){
		hidLayer.initial(rangeHidW,rangeB);
		outputLayer.initial(rangeOutW,rangeB);
	}
	
	public void setWeights(float[][] hidWeights,float[][] outputWeights){
		hidLayer.setWeights(hidWeights);
		outputLayer.setWeights(outputWeights);
	}
	
	public void setBias(float[] hidBias,float[] outputBias){
		hidLayer.setBias(hidBias);
		outputLayer.setBias(outputBias);
	}
	
	public float[][] getOutputs(float[][] inputs){
		float[][] outputs = new float[inputs.length][];
		for(int i = 0;i < outputs.length;i++){
			outputs[i] = getOutputs(inputs[i]);
		}
		return outputs;
	}
	
	public float[] getOutputs(float[] inputs){
		return outputLayer.getOutputs(getHidOutputs(inputs));
	}
	
	public int getPrediction(float[] inputs){
		float[] out = getOutputs(inputs);
		return Matrix.maxLocation(out);
	}
	
	public float[] getHidOutputs(float[] inputs){
		return hidLayer.getOutputs(inputs);
	}
	
	public float[] getHidSensitivity(float[] outputSensitivity,float[] inputs){
		float[] hidOutputs = getHidOutputs(inputs);
		float[] hidSensitivity = new float[hidLayer.size()];
		for(int hidNode = 0;hidNode < hidSensitivity.length;hidNode++){
			float sumOfOutputSensitivity = 0;
			for(int outNode = 0;outNode < outputSensitivity.length;outNode++){
				sumOfOutputSensitivity += outputLayer.get(outNode).getWeight(hidNode) * outputSensitivity[outNode];
			}
			hidSensitivity[hidNode] = sumOfOutputSensitivity * hidLayer.getNode(hidNode).getDerivative(hidOutputs[hidNode]);
		}
		return hidSensitivity;
	}
	
	public float[] getOutputSensitivity(Parameters para,float[] inputs,float[] targets){
		float[] sensitivity = new float[outputLayer.size()];
		float[] outputs = getOutputs(inputs);
		for(int outNode = 0;outNode < sensitivity.length;outNode++){
			ErrorParameters errPara = new ErrorParameters(outputs[outNode],targets[outNode],para.getEnsAvgOutputs()[outNode],para.getLambda());
			sensitivity[outNode] = para.getExtraAlpha(outNode) * ErrorFunction.getDifferential(errPara) * outputLayer.getNode(outNode).getDerivative(outputs[outNode]);
		}
		return sensitivity;		
	}
	
	public void learn(Parameters para,float[] inputs,float[] targets){
		float[] outSensitivity = getOutputSensitivity(para,inputs,targets);
		float[] hidSensitivity = getHidSensitivity(outSensitivity,inputs);
		float[] hidOutputs = getHidOutputs(inputs);
		hidLayer.update(para, inputs, hidSensitivity);
		outputLayer.update(para, hidOutputs,outSensitivity);
	}
	
	public void evaluateDelta(Parameters para,float[] inputs,float[] targets){
		float[] outSensitivity = getOutputSensitivity(para,inputs,targets);
		float[] hidSensitivity = getHidSensitivity(outSensitivity,inputs);
		float[] hidOutputs = getHidOutputs(inputs);
		hidLayer.evaluateDelta(para, inputs, hidSensitivity);
		outputLayer.evaluateDelta(para, hidOutputs,outSensitivity);
	}
	public void learnBatch(){
		hidLayer.updateBatch();
		outputLayer.updateBatch();
	}
	
	public Object clone(){
		Network newNetwork = null;
		try{
			newNetwork = (Network)super.clone();
			newNetwork.hidLayer = (Layer)this.hidLayer.clone();
			newNetwork.outputLayer = (Layer)this.outputLayer.clone();
		}catch(CloneNotSupportedException e){
			System.err.println("Network can't clone.");
		}
		return newNetwork;
	}
}
