package com.ruijie.ncl;

import java.io.Serializable;
import java.util.Vector;

public class Layer extends Vector<Node> implements Serializable,Cloneable{

	private static final long serialVersionUID = 1L;
	
	public Layer(int size){
		super(size);
	}
	
	public void linearConnection(int noOfConnections){
		for(int i = 0;i < this.capacity();i++){
			this.add(i,new LinearNode(noOfConnections));
		}
	}
	
	public void logisticConnection(int noOfConnections){
		for(int i = 0;i < this.capacity();i++){		
			this.add(i,new LogisticNode(noOfConnections));
		}
	}
	
	public void initial(float rangeW,float rangeB){
		for(int i = 0;i < this.size();i++){
			this.get(i).initial(rangeW,rangeB);
		}
	}
	
	public void setWeights(float[][] w){
		if(w.length != this.size()){
			System.err.println("setWeights Error! The length isn't matched!");
			System.exit(1);
		}
		for(int i = 0;i < this.size();i++){
			setWeights(i,w[i]);
		}
	}
	
	public void setWeights(int node,float[] w){
		this.get(node).setWeights(w);
	}
	
	public void setWeight(int node,int index,float w){
		this.get(node).setWeight(index, w);
	}
	
	public float[][] getWeights(){
		float[][] weights = new float[this.size()][];
		for(int i = 0;i < weights.length;i++){
			weights[i] = this.getNode(i).getWeights();
		}
		return weights;
	}
	
	public float[] getBias(){
		float[] bias = new float[this.size()];
		for(int i = 0;i < bias.length;i++){
			bias[i] = this.getNode(i).getBias();
		}
		return bias;
	}
	
	public void setBias(float[] b){
		if(b.length != this.size()){
			System.err.println("setBias Error! The length isn't matched!");
			System.exit(1);
		}
		for(int i = 0;i < this.size();i++){
			setBias(i,b[i]);
		}
	}
	
	public void setBias(int node,float b){
		this.get(node).setBias(b);
	}
	
	public Node getNode(int i){
		return this.get(i);
	}
	
	public void addNode(Node node){
		this.ensureCapacity(this.size()+1);
		this.add(node);
	}
	
	public void deleteNode(int i){
		this.remove(i);
	}
	
	public float[] getOutputs(float[] inputs){
		float[] outputs = new float[this.size()];
		for(int i = 0;i < outputs.length;i++){
			outputs[i] = this.getNode(i).getOutput(inputs);
		}
		return outputs;
	}
	
	public void update(Parameters para,float[] layerInputs,float[] sensitivity){
		for(int node = 0;node < this.size();node++){
			this.getNode(node).update(para, layerInputs, sensitivity[node]);
		}
	}
	
	public void evaluateDelta(Parameters para,float[] layerInputs,float[] sensitivity){
		for(int node = 0;node < this.size();node++){
			this.getNode(node).evaluateDelta(para.getAlpha(), layerInputs, sensitivity[node]);
		}
	}
	public void updateBatch(){
		for(int node = 0;node < this.size();node++){
			this.getNode(node).updataBatch();
		}
	}
	
	public Object clone(){
		Layer newLayer = (Layer)super.clone();
		for(int i = 0;i < this.size();i++){
			newLayer.set(i, (Node)this.getNode(i).clone());
		}
		return newLayer;
	}
	
}
