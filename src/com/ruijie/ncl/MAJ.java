package com.ruijie.ncl;

import java.io.Serializable;

public class MAJ extends CombinationScheme implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public MAJ(){
		type = "maj";
	}
	
	public float[] combineOutputs(float[][] ensembleOutputs){
		float[] combinedOutput = new float[ensembleOutputs[0].length];
		int[] votes = new int[combinedOutput.length];
		for(int network = 0; network < ensembleOutputs.length; network++){
			int highestOutput = Matrix.maxLocation(ensembleOutputs[network]);
			votes[highestOutput]++;
		}
		int winner = Matrix.maxLocation(votes);
		combinedOutput[winner] = 1.0f;
		return combinedOutput;
	}

}
