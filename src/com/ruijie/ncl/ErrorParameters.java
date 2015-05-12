package com.ruijie.ncl;

public class ErrorParameters {
	
	public float estimate;
	public float target;
	public float ensAverage;
	public float lambda;
	
	public ErrorParameters(float est, float tar, float avg,float lam){
		estimate = est;
		target = tar;
		ensAverage = avg;
		lambda = lam;
	}
}
