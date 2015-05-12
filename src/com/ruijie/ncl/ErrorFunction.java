package com.ruijie.ncl;

public abstract class ErrorFunction {
	
	/*public static float getError(ErrorParameters errPara){
		float error = getEmpiricalError(errPara) + errPara.lambda * getPenalty(errPara);
		return error;
	}*/
	
	public static float getEmpiricalError(ErrorParameters errPara){
		float error = 0.5f * (float)Math.pow((errPara.estimate - errPara.target),2.0);
		return error;
	}
	
	public static float getPenalty(ErrorParameters errPara){
		float penalty = (float) (-1 * errPara.lambda * Math.pow(errPara.estimate - errPara.ensAverage, 2.0));
		return penalty;
	}
	
	public static float getNCLError(ErrorParameters errPara){
		return getEmpiricalError(errPara) + getPenalty(errPara);
	}
	
	//(a[i] - d) - lambda * (a[i] - a)
	public static float getDifferential(ErrorParameters errPara){
		return (errPara.estimate - errPara.target) - errPara.lambda * (errPara.estimate - errPara.ensAverage);
	}

}
