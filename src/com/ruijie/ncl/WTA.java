package com.ruijie.ncl;

import java.io.Serializable;

public class WTA extends CombinationScheme implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public WTA(){
		type = "wta";
	}
	
	public float[] combineOutputs(float[][] ensembleOutputs){
		float[] combinedOutput = new float[ensembleOutputs[0].length];
		int[] highestOutput = Matrix.maxLocation(ensembleOutputs);
		combinedOutput = ensembleOutputs[highestOutput[0]];		
		return combinedOutput;
	}

}
