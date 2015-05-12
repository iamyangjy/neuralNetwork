package com.ruijie.ncl;

import java.io.Serializable;

public class AVG extends CombinationScheme implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public AVG(){
		type = "avg";
	}
	
	public float[] combineOutputs(float[][] ensembleOutputs){
		float[] combinedOutput = new float[ensembleOutputs[0].length];
		
		for(int node = 0; node < combinedOutput.length; node++){
			for(int network = 0; network < ensembleOutputs.length; network++){
				combinedOutput[node] += ensembleOutputs[network][node];
			}
			combinedOutput[node] = combinedOutput[node]/(ensembleOutputs.length*1f);
		}		
		return combinedOutput;
	}
	
}
