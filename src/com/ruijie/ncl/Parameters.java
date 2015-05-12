package com.ruijie.ncl;

public class Parameters {

	//学习速率
	private float alpha;
//	private float coefficient;
	private float[] extraAlpha;
	//负相关参数，范围[0,1]，一般不用改
	private float lambda;
	//神经网络个数
	private int noOfNet;
	//迭代次数
	private int epochs;
	//隐层节点数
	private int noOfHidNodes;

	private int hidFunc;
	private int outFunc;
	private float momentum;
	
	private float phy;//the parameter in DyS
	
	private float rankThreshold;
	
	private float temperature;
	private float decreaseFactor;
	private int decTime;
	
	private int stripSize;
	
	
	private float[] ensAvgOutputs;
	
	private float ratioForAL;
	private float epsilonForAL;
	
	private String m_predictType = "";
	
	public Parameters(){
//		coefficient = 1f;
		extraAlpha = new float[0];
		momentum = 0;
		
		stripSize  =1;
		rankThreshold = 0.5f;
		
		temperature = 1f;
		decreaseFactor = 0.9f;
		decTime = 100;
		
		phy = 0.5f;
		
		ratioForAL = 0;
		epsilonForAL = 0;
	}
	
	public void setAllParameters(float al,float lam,int numNet,int epo,int numHid){
		alpha = al;
		lambda = lam;
		noOfNet = numNet;
		epochs = epo;
		noOfHidNodes = numHid;
	}
	
	public float getAlpha(){
		return alpha;
	}
	public void setAlpha(float al){
		alpha = al;
	}
	
//	public float getCoefficient(){
//		return coefficient;
//	}
	
	public float getExtraAlpha(int index){
		return extraAlpha[index];
	}
	
	public float[] getExtraAlpha(){
		return extraAlpha;
	}
	
	public void initExtraAlpha(int numOutNodes){
		extraAlpha = new float[numOutNodes];
	}
	
	public void resetExtraAlpha(){
		if(extraAlpha.length == 0){
			System.err.println("Please initialize the extra alpha first!");
			System.exit(0);
		}
		for(int i = 0;i < extraAlpha.length;i++){
			extraAlpha[i] = 1f;
		}
	}
	
	public void setExtraAlpha(float[] extraA){
		if(extraA.length != extraAlpha.length){
			System.err.println("The length of extra alpha isn't matched!");
			System.exit(0);
		}
		for(int i = 0;i < extraA.length;i++){
			extraAlpha[i] = extraA[i];
		}
	}
	
//	public void setCoefficient(float coe){
//		coefficient = coe;
//	}
	
	public float getLambda(){
		return lambda;
	}
	public void setLambda(float lam){
		lambda = lam;
	}
	
	public int getNoOfNet(){
		return noOfNet;
	}
	public void setNoOfNet(int numNet){
		noOfNet = numNet;
	}
	
	public int getEpochs(){
		return epochs;
	}
	public void setEpochs(int epo){
		epochs = epo;
	}
	
	public int getNoOfHidNodes(){
		return noOfHidNodes;
	}
	public void setNoOfHidNodes(int numHid){
		noOfHidNodes = numHid;
	}
	
	public float[] getEnsAvgOutputs(){
		return ensAvgOutputs;
	}
	
	public void setEnsAvgOutputs(float[] avg){
		ensAvgOutputs = new float[avg.length];
		for(int i = 0;i < avg.length;i++)
			ensAvgOutputs[i] = avg[i];
	}
	
	public void setHidFunc(int func){
		hidFunc = func;
	}
	
	public int getHidFunc(){
		return hidFunc;
	}
	
	public void setOutFunc(int func){
		outFunc = func;
	}
	
	public int getOutFunc(){
		return outFunc;
	}
	
	public void setMomentum(float m){
		momentum = m;
	}
	
	public float getMomentum(){
		return momentum;
	}
	
	public int getStripSize(){
		return stripSize;
	}
	public void setStripSize(int size){
		stripSize = size;
	}
	
	public float getRankThreshold(){
		return rankThreshold;
	}
	public void setRankThreshold(float rt){
		rankThreshold = rt;
	}
	
	public float getTemperature(){
		return temperature;
	}
	public void setTemperature(float tempe){
		temperature = tempe;
	}
	
	public float getFactor(){
		return decreaseFactor;
	}
	public void setFactor(float factor){
		decreaseFactor = factor;
	}
	
	public int getDecTime(){
		return decTime;
	}
	public void setDecTime(int time){
		decTime = time;
	}
	
	public float getPhy(){
		return phy;
	}
	public void setPhy(float fai){
		phy = fai;
	}
	
	public float getRatioForAL(){
		return ratioForAL;
	}
	public void setRatioForAL(float r){
		ratioForAL = r;
	}
	
	public float getEpsilonForAL(){
		return epsilonForAL;
	}
	public void setEpsilonForAL(float epsilon){
		epsilonForAL = epsilon;
	}
	
	public void setPredictType(String type){
		m_predictType = type;
	}
	public String getPredictType(){
		return m_predictType;
	}
}
