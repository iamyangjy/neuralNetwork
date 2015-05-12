package com.ruijie.ncl;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


public class Dataset implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;
	
	private float[][] inputs;
	private float[][] targets;
	//输入数据的个数
	private int noOfSamples;
	//特侦数据的维度个数
	private int noOfInputs;
	//实际值的维度数
	private int noOfClasses;
	
	private float m_scale=1f;
	
	private float[] m_max=null;
	private float[] m_min=null;
	private boolean normalized = false;
	
	
	public Dataset(){
		
	}
	
	public Dataset(float scale){
		m_scale = scale;
	}
	
	public Dataset(String filename,int numIn) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<float[]> inputsRead = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsRead = new ArrayList<float[]>(0);
		String line = reader.readLine();
		noOfInputs = numIn;
		noOfSamples = 0;
		while(line != null){
			StringTokenizer tokenizer = new StringTokenizer( line," \t," );
			float[] input = new float[noOfInputs];
			for(int j = 0;j < input.length;j++){
				input[j] = Float.valueOf(tokenizer.nextToken());
			}
			float[] target = new float[tokenizer.countTokens()];
			for(int j = 0;j < target.length;j++){
				target[j] = Float.valueOf(tokenizer.nextToken());
			}
			noOfSamples++;
			inputsRead.ensureCapacity(noOfSamples);
			inputsRead.add(input);
			targetsRead.ensureCapacity(noOfSamples);
			targetsRead.add(target);
			line = reader.readLine();
		}			
		reader.close();
		inputs = new float[noOfSamples][noOfInputs];
		noOfClasses = targetsRead.get(0).length;
		targets = new float[noOfSamples][noOfClasses];
		//dataWeights = new float[noOfSamples];
		for(int i = 0;i < noOfSamples;i++){
			inputs[i] = inputsRead.get(i).clone();
			targets[i] = targetsRead.get(i).clone();
		}
	}
	
	public Dataset(String filename){
		BufferedReader reader = null;
		try{
			// Open the file
			reader = new BufferedReader(new FileReader(new File(filename)));
		}
		catch(FileNotFoundException ex){
			System.err.println("File '"+filename+"' is not found.");
			System.exit(1);
		}
		try{
			//read the first line which contains information about the data
			String line = reader.readLine();
			//Break that line up into chunks separated by commas
			StringTokenizer tokenizer = new StringTokenizer(line, ", \t");
			//first token is number of samples
			noOfSamples = Integer.valueOf(tokenizer.nextToken()).intValue();
			//second token is number of inputs values    
			noOfInputs = Integer.valueOf(tokenizer.nextToken()).intValue();
			//third token is number of classes
			noOfClasses = Integer.valueOf(tokenizer.nextToken()).intValue();
			//construct arrays to hold values
			inputs = new float[noOfSamples][];
			targets = new float[noOfSamples][];

			//read in line at a time, tokenize and add to inputs and targets
			for(int i = 0; i < noOfSamples; i++){
		    		line = reader.readLine();
		    		tokenizer = new StringTokenizer( line, "," );
		    		inputs[i] = new float[noOfInputs];
		    		for(int j = 0; j < noOfInputs; j++){
	        			inputs[i][j] = Float.valueOf(tokenizer.nextToken()).floatValue();
		    		}
		    		targets[i] = new float[noOfClasses];
		    		for(int j = 0; j < noOfClasses; j++){
	        			targets[i][j] = Float.valueOf(tokenizer.nextToken()).floatValue();
		    		}
			}
			reader.close();
		}
		catch(IOException ex){
			System.err.print("There is some problems when reading from the file,please check it.");
			System.exit(1);
		}
		
	}
	
	public Dataset(int sam,int in,int cla){
		noOfSamples = sam;
		noOfInputs = in;
		noOfClasses = cla;
		inputs = new float[noOfSamples][noOfInputs];
		targets = new float[noOfSamples][noOfClasses];
	}
	
	public Dataset(float[] series, int numIn, int deltaT){
		noOfSamples = series.length - numIn*deltaT;
		noOfInputs = numIn;
		noOfClasses = 1;
		inputs = new float[noOfSamples][numIn];
		targets = new float[noOfSamples][noOfClasses];
		int index = 0;
		for(int i = (numIn-1) * deltaT;i < series.length - deltaT;i++){
			for(int k = 0,m = i;k < inputs[index].length;k++,m -= deltaT){
				inputs[index][k] = series[m];
			}
			targets[index][0] = series[i + deltaT];
			index++;
		}
	}
	
	public void initFromMap(HashMap<String,float[]> mapInputs,HashMap<String,float[]> mapTargets){
		ArrayList<float[]> inputsRead = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsRead = new ArrayList<float[]>(0);
		List<Map.Entry<String, float[]>> sortedInputs = 
				new ArrayList<Map.Entry<String, float[]>>(mapInputs.entrySet());
		Collections.sort(sortedInputs, new Comparator<Map.Entry<String, float[]>>() {   
			public int compare(Map.Entry<String, float[]> o1, Map.Entry<String, float[]> o2) {
				return (o1.getKey()).toString().compareTo(o2.getKey());
		    }
		});
		noOfSamples = 0;
		for(int i = 0;i < sortedInputs.size();i++){
			Map.Entry<String, float[]> kv = sortedInputs.get(i);
			String date = kv.getKey();
			if(!mapTargets.containsKey(date)){
				//System.err.println("[WARNING]Date " + date + " in inputs do not match that in outputs!");
				continue;
			}
			//System.out.println(date);
			float[] input = kv.getValue();
			float[] target = mapTargets.get(date);
			noOfSamples++;
			inputsRead.ensureCapacity(noOfSamples);
			inputsRead.add(input);
			targetsRead.ensureCapacity(noOfSamples);
			targetsRead.add(target);
		}
		noOfInputs = inputsRead.get(0).length;
		noOfClasses = targetsRead.get(0).length;
		inputs = new float[noOfSamples][noOfInputs];
		targets = new float[noOfSamples][noOfClasses];
		//dataWeights = new float[noOfSamples];
		for(int i = 0;i < noOfSamples;i++){
			inputs[i] = inputsRead.get(i).clone();
			targets[i] = targetsRead.get(i).clone();
		}
	}
	
	
	public void readFromFile(String filename,int numClass) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<float[]> inputsRead = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsRead = new ArrayList<float[]>(0);
		String line = reader.readLine();
		if(line == null){
			System.err.println("The file is illegal!");
			System.exit(0);
		}
		StringTokenizer tokenizer = new StringTokenizer( line," \t," );
		noOfClasses = numClass;
		noOfSamples = 0;
		noOfInputs = tokenizer.countTokens() - noOfClasses;
		
		while(true){
			float[] input = new float[noOfInputs];
			for(int j = 0;j < input.length;j++){
				input[j] = Float.valueOf(tokenizer.nextToken());
			}
			float[] target = new float[noOfClasses];
			for(int j = 0;j < target.length;j++){
				target[j] = Float.valueOf(tokenizer.nextToken());
			}
			noOfSamples++;
			inputsRead.ensureCapacity(noOfSamples);
			inputsRead.add(input);
			targetsRead.ensureCapacity(noOfSamples);
			targetsRead.add(target);
			line = reader.readLine();
			if(line == null){
				break;
			}
			tokenizer = new StringTokenizer( line," \t," );
		}			
		reader.close();
		inputs = new float[noOfSamples][noOfInputs];
		noOfClasses = targetsRead.get(0).length;
		targets = new float[noOfSamples][noOfClasses];
		//dataWeights = new float[noOfSamples];
		for(int i = 0;i < noOfSamples;i++){
			inputs[i] = inputsRead.get(i).clone();
			targets[i] = targetsRead.get(i).clone();
		}
	}
	
	
	public void readFromFile(String filename,int classColumn, float[] labels) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<float[]> inputsRead = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsRead = new ArrayList<float[]>(0);
		String line = reader.readLine();
		if(line == null){
			System.err.println("The file is illegal!");
			System.exit(0);
		}
		StringTokenizer tokenizer = new StringTokenizer( line," \t," );
		noOfInputs = tokenizer.countTokens() - 1;
		noOfSamples = 0;
		if(classColumn < 0){
			classColumn = noOfInputs;
		}
		while(true){
			float[] input = new float[noOfInputs];
			float[] target = new float[labels.length];
			
			for(int inputIndex = 0,j = 0;j < noOfInputs + 1;j++){
				if(j == classColumn){
					/*
					 * this column is the class
					 */
					float cla = Float.valueOf(tokenizer.nextToken());
					int k = 0;
					for(;k < labels.length;k++){
						if(cla == labels[k]){
							target[k] = 1f;
							break;
						}
					}
					if(k == labels.length){
						System.err.println("The label is wrong!");
						System.exit(0);
					}
				}else{
					input[inputIndex++] = Float.valueOf(tokenizer.nextToken());
				}
			}
			noOfSamples++;
			inputsRead.ensureCapacity(noOfSamples);
			inputsRead.add(input);
			targetsRead.ensureCapacity(noOfSamples);
			targetsRead.add(target);
			line = reader.readLine();
			if(line == null)break;
			tokenizer = new StringTokenizer( line," \t," );
		}			
		reader.close();
		inputs = new float[noOfSamples][noOfInputs];
		noOfClasses = targetsRead.get(0).length;
		targets = new float[noOfSamples][noOfClasses];
		//dataWeights = new float[noOfSamples];
		for(int i = 0;i < noOfSamples;i++){
			inputs[i] = inputsRead.get(i).clone();
			targets[i] = targetsRead.get(i).clone();
		}
	}
	
	public void readFromFile(String filename,float[] labels){
		BufferedReader reader = null;
		
		// Open the file
		try {
			reader = new BufferedReader(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			System.err.println("File '"+ filename+ "' is not found.");
			System.exit(1);
		}
		
		try{
			for(int i = 0;i < inputs.length;i++){
				String line = reader.readLine();
				StringTokenizer tokenizer = new StringTokenizer( line," ,\t");
				for(int j = 0;j < inputs[i].length;j++){
					inputs[i][j] = Float.valueOf(tokenizer.nextToken());
				}
				float lab = Float.valueOf(tokenizer.nextToken());
				boolean t = false;
				for(int k = 0;k < targets[0].length;k++){
					if(lab == labels[k]){
						targets[i][k] = 1;
						t = true;
					}
				}
				if(!t){
					System.err.println("The labels is wrong!");
					System.exit(1);
				}
			}			
			reader.close();
		}
		catch(IOException ex){
			System.err.print("There is some problems when reading from the file,please check it.");
			System.exit(1);
		}
	}
	
	public void readFromFile(String inputFilename,String labelFilename, int[] labels){
		readInputs(inputFilename);
		readTargets(labelFilename,labels);
	}
	
	public void readInputs(String inputFilename){
		BufferedReader readerInput = null;		
		// Open the file
		try {
			readerInput = new BufferedReader(new FileReader(new File(inputFilename)));
		} catch (FileNotFoundException e) {
			System.err.println("File '"+inputFilename+"' is not found.");
			System.exit(1);
		}		
		try{
			for(int i = 0;i < inputs.length;i++){
				String line = readerInput.readLine();
				StringTokenizer tokenizer = new StringTokenizer( line );
				for(int j = 0;j < inputs[i].length;j++){
					inputs[i][j] = Float.valueOf(tokenizer.nextToken());
				}
			}
			readerInput.close();
		}
		catch(IOException ex){
			System.err.print("There is some problems when reading from the file,please check it.");
			System.exit(1);
		}
	}
	
	public void readTargets(String targetFilename,int[] labels){
		//labels = {0,1,...}
		BufferedReader readerTarget = null;		
		// Open the file
		try {
			readerTarget = new BufferedReader(new FileReader(new File(targetFilename)));
		} catch (FileNotFoundException e) {
			System.err.println("File '"+targetFilename+"' is not found.");
			System.exit(1);
		}		
		try{
			for(int i = 0;i < targets.length;i++){
				String line = readerTarget.readLine();
				StringTokenizer ST = new StringTokenizer(line);
				targets[i][Matrix.findIndex(Integer.valueOf(ST.nextToken()), labels)] = 1;
			}
			readerTarget.close();
		}
		catch(IOException ex){
			System.err.print("There is some problems when reading from the file,please check it.");
			System.exit(1);
		}
	}
	
	public float reScaleOutputs(){
		float scale = 1f;
		for(int i = 0;i < targets.length;i++){
			for(int k = 0;k < targets[i].length;k++){
				if(targets[i][k] > scale){
					scale = targets[i][k];
				}
			}
		}
		scale = scale * 1.5f;
		for(int i = 0;i < targets.length;i++){
			for(int k = 0;k < targets[i].length;k++){
				targets[i][k] = targets[i][k] / scale;
			}
		}
		return scale;
	}
	
	public void reScaleOutputs(float scale){
		for(int i = 0;i < targets.length;i++){
			for(int k = 0;k < targets[i].length;k++){
				targets[i][k] = targets[i][k] / scale;
			}
		}
	}
	
	public boolean isNormalized(){
		return normalized;
	}
	
	public float[] getMax(){
		return m_max;
	}
	public float[] getMin(){
		return m_min;
	}
	
	public void normalize(){
		/*int[] maxLoc = Matrix.maxLocationOfEachRow(inputs);
		int[] minLoc = Matrix.minLocationOfEachRow(inputs);
		for(int i = 0;i < inputs[0].length;i++){
			float max = inputs[maxLoc[i]][i];
			float min = inputs[minLoc[i]][i];
			for(int j = 0;j < inputs.length;j++){
				inputs[j][i] = (inputs[j][i] - min) / (max - min);
			}			
		}*/
		//float[] max = getMaxOfInputs();
		//float[] min = getMinOfInputs();
		if(!normalized){
			m_max = getMaxOfInputs();
			m_min = getMinOfInputs();
		}
		normalize(m_max,m_min);
		normalized = true;
	}
	
	public void normalize(Dataset data){
		/*int[] maxLoc = data.getMaxLocOfInputs();
		int[] minLoc = data.getMinLocOfInputs();
		for(int i = 0;i < inputs[0].length;i++){
			float max = data.getInputs()[maxLoc[i]][i];
			float min = data.getInputs()[minLoc[i]][i];
			for(int j = 0;j < inputs.length;j++){
				inputs[j][i] = (inputs[j][i] - min) / (max - min);
			}			
		}*/
		//float[] max = data.getMaxOfInputs();
		//float[] min = data.getMinOfInputs();
		if(!data.isNormalized()){
			m_max = data.getMaxOfInputs();
			m_min = data.getMinOfInputs();
		}else{
			m_max = data.getMax();
			m_min = data.getMin();
		}
		normalize(m_max,m_min);
		normalized = true;
	}
	
	public void normalize(float[] max,float[] min){
		for(int i = 0;i < inputs[0].length;i++){
			if(max[i] != min[i]){
				for(int j = 0;j < inputs.length;j++){
					inputs[j][i] = (inputs[j][i] - min[i]) / (max[i] - min[i]);
				}
			}else{
				for(int j = 0;j < inputs.length;j++){
					inputs[j][i] = inputs[j][i] - min[i]==0?0:1;
				}
			}
		}
	}
	
	public static float[] normalize(float[] oriInput,float[] max,float[] min){
		float[] norInput = oriInput.clone();
		for(int i = 0;i < norInput.length;i++){
			if(max[i] != min[i]){
				norInput[i] = (norInput[i] - min[i]) / (max[i] - min[i]);
			}else{
				norInput[i] = norInput[i] - min[i]==0?0:1;
			}
		}
		return norInput;
	}
	
	public void mapToBinaryClass(int[] positiveLabel){
		float[][] newTargets = new float[noOfSamples][2];
		for(int i = 0;i < noOfSamples;i++){
			if(Matrix.findIndex(Matrix.maxLocation(targets[i]), positiveLabel) >= 0){
				newTargets[i][0] = 1;
			}else{
				newTargets[i][1] = 1;
			}
		}
		targets = newTargets;
	}
	
	public float[] getMaxOfInputs(){
		int[] maxLoc = getMaxLocOfInputs();
		float[] max = new float[inputs[0].length];
		for(int i = 0;i < inputs[0].length;i++){
			max[i] = getInputs()[maxLoc[i]][i];
		}
		return max;
	}
	
	public float[] getMinOfInputs(){
		int[] minLoc = getMinLocOfInputs();
		float[] min = new float[inputs[0].length];
		for(int i = 0;i < inputs[0].length;i++){
			min[i] = getInputs()[minLoc[i]][i];
		}
		return min;
	}
	
	public int[] getMaxLocOfInputs(){
		return Matrix.maxLocationOfEachRow(inputs);
	}
	
	public int[] getMinLocOfInputs(){
		return Matrix.minLocationOfEachRow(inputs);
	}
	
	public int[] getSeqSortByMargin(Ensemble ens){
		float[] marg = new float[noOfSamples];
		for(int i = 0;i < marg.length;i++){
			float[] out = ens.getOutputs(inputs[i]);
			float[] tar = targets[i];
			for(int k = 0;k < out.length;k++){
				marg[i] += (out[k] - tar[k]) * (out[k] - tar[k]);
			}
		}
		return Matrix.QuickSort(marg);
	}
	
	public float[][] getDistanceMat_Euclid(){
		float[][] distanceMat = new float[noOfSamples][noOfSamples];
		for(int i = 0;i < distanceMat.length;i++){
			for(int k = 0;k < distanceMat.length;k++){
				if(k < i){
					distanceMat[i][k] = distanceMat[k][i];
				}else if(k > i){
					float[] x_i = this.getInputs(i);
					float[] x_k = this.getInputs(k);
					for(int n = 0;n < x_i.length;n++){
						distanceMat[i][k] += (x_i[n] - x_k[n]) * (x_i[n] - x_k[n]);
					}
					distanceMat[i][k] = (float)Math.sqrt(distanceMat[i][k]);
				}
			}
		}
		for(int i = 0;i < distanceMat.length;i++){
			distanceMat[i][i] = Matrix.maxValue(distanceMat[i]) + 1;
		}
		return distanceMat;
	}
	
	public boolean[] checkTomekLink(){
		boolean[] isTomekLink = new boolean[noOfSamples];
		float[][] distanceMat = this.getDistanceMat_Euclid();
		for(int i = 0;i < this.noOfSamples;i++){
			int label = Matrix.maxLocation(this.getTargets(i));
			int closestIndex = Matrix.minLocation(distanceMat[i]);
			int labelOfClosest = Matrix.maxLocation(this.getTargets(closestIndex));
			if(label != labelOfClosest){
				int closestIndex_j = Matrix.minLocation(distanceMat[closestIndex]);
				if(closestIndex_j == i){
					isTomekLink[i] = true;
					isTomekLink[closestIndex] = true;
				}
			}
		}
		return isTomekLink;
	}
	
	public boolean[] checkNoise(int K){
		boolean[] isNoise = new boolean[noOfSamples];
		float[][] distanceMat = this.getDistanceMat_Euclid();
		int[] numEachC = this.getNumEachClass();
		//int majClass = Matrix.maxLocation(numEachC);
		for(int i = 0;i < this.noOfSamples;i++){
			int label = Matrix.maxLocation(this.getTargets(i));
			float[] distanceTo_i = distanceMat[i].clone();
			int[] index = Matrix.QuickSort(distanceTo_i);
			int sameLabel = 0;
			for(int t = 0;t < K;t++){
				if(Matrix.maxLocation(this.getTargets(index[t])) == label){
					sameLabel++;
				}
			}
			/*if(label == majClass){
				if(sameLabel < K/2){
					isNoise[i] = true;
				}
			}else{*/
				if((float)sameLabel / K < (float)numEachC[label] / this.noOfSamples){
					isNoise[i] = true;
				}
			//}
		}
		return isNoise;
	}
	
	public int[] disorder(){
		Random ran = new Random();
		int[] index = new int[inputs.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < inputs.length;i++){
			int temp = ran.nextInt(inputs.length);
			float[] tempInputs = inputs[temp];
			float[] tempTargets = targets[temp];
			int tempIndex = index[temp];
			inputs[temp] = inputs[i];
			targets[temp] = targets[i];
			index[temp] = index[i];
			inputs[i] = tempInputs;
			targets[i] = tempTargets;
			index[i] = tempIndex;
		}
		return index;
	}
	
	public int[] disorder(int seed){
		Random ran = new Random(seed);
		int[] index = new int[inputs.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < inputs.length;i++){
			int temp = ran.nextInt(inputs.length);
			float[] tempInputs = inputs[temp];
			float[] tempTargets = targets[temp];
			int tempIndex = index[temp];
			inputs[temp] = inputs[i];
			targets[temp] = targets[i];
			index[temp] = index[i];
			inputs[i] = tempInputs;
			targets[i] = tempTargets;
			index[i] = tempIndex;
		}
		return index;
	}
	
	
	public void disorder(int seed,int[] index){
		Random ran = new Random(seed);
		for(int i = 0;i < inputs.length;i++){
			int temp = ran.nextInt(inputs.length);
			float[] tempInputs = inputs[temp];
			float[] tempTargets = targets[temp];
			int tempIndex = index[temp];
			inputs[temp] = inputs[i];
			targets[temp] = targets[i];
			index[temp] = index[i];
			inputs[i] = tempInputs;
			targets[i] = tempTargets;
			index[i] = tempIndex;
		}
	}
	
	public int[] sortByClass(){
		// sort the patterns by class lables, return the sorted index of the orignal dataset
		int[] sortedIndex = new int[this.getNoOfSamples()];
		int[] num = getNumEachClass();
		int[] sumNumBefore = new int[num.length];
		for(int i = 1;i < sumNumBefore.length;i++){
			sumNumBefore[i] = sumNumBefore[i-1] + num[i-1];
		}
		float[][][] in = new float[num.length][][];
		float[][][] tar = new float[num.length][][];
		for(int i = 0;i < num.length;i++){
			in[i] = new float[num[i]][];
			tar[i]  = new float[num[i]][];
		}
		int[] index = new int[num.length];
		//int si = 0;
		for(int i = 0;i < this.noOfSamples;i++){
			int label = Matrix.maxLocation(this.targets[i]);
			in[label][index[label]] = this.inputs[i].clone();
			tar[label][index[label]] = this.targets[i].clone();
			sortedIndex[num[label] - 1 - index[label] + sumNumBefore[label]] = i;
			index[label]++;
		}
		int k = 0;
		for(int i = 0;i < num.length;i++){
			while(--num[i] >= 0){
				this.setInputs(k, in[i][num[i]]);
				this.setTargets(k, tar[i][num[i]]);
				k++;
			}
		}
		return sortedIndex;
	}

	public Dataset getValiData(int num){
		Dataset valiData = new Dataset(num,this.noOfInputs,this.noOfClasses);
		//this.disorder();
		valiData.setInputs(this.getSomeInputs(0,num-1));
		valiData.setTargets(this.getSomeTargets(0,num-1));
		float[][] in = this.getSomeInputs(num, this.noOfSamples - 1);
		float[][] tar = this.getSomeTargets(num, this.noOfSamples - 1);
		this.noOfSamples -= num;
		this.inputs = new float[this.noOfSamples][this.noOfInputs];
		this.targets = new float[this.noOfSamples][this.noOfClasses];
		this.setInputs(in);
		this.setTargets(tar);
		
		return valiData;
	}
	
	public void setTargets(float[][] tar){
		for(int i = 0;i < noOfSamples;i++){
			setTargets(i,tar[i]);
		}
	}
	public void setTargets(int index,float[] tar){
		for(int i = 0;i < tar.length;i++)
			setTargets(index,i,tar[i]);
	}
	public void setTargets(int i,int j,float tar){
		targets[i][j] = tar;
	}
	public void setInputs(float[][] in){
		for(int i = 0;i < noOfSamples;i++){
			setInputs(i,in[i]);
		}
	}
	public void setInputs(int index,float[] in){
		for(int i = 0;i < in.length;i++)
			setInputs(index,i,in[i]);
	}
	public void setInputs(int i,int j,float in){
		inputs[i][j] = in;
	}
	
	
	public float[][] getInputs(){
		return inputs;
	}
	
	public float[][] getSomeInputs(int beginIndex,int endIndex){
		float[][] ret = new float[endIndex - beginIndex + 1][];
		for(int i = 0;i < ret.length;i++){
			ret[i] = getInputs(i + beginIndex);
		}
		return ret;
	}
	
	public float[] getInputs(int index){
		return inputs[index];
	}
	
	public float[][] getTargets(){
		return targets;
	}
	
	public float[][] getSomeTargets(int beginIndex,int endIndex){
		float[][] ret = new float[endIndex - beginIndex + 1][];
		for(int i = 0;i < ret.length;i++){
			ret[i] = getTargets(i + beginIndex);
		}
		return ret;
	}
	
	public float[] getTargets(int index){
		return targets[index];
	}
	
	public int getNoOfSamples(){
		return noOfSamples;
	}
	
	public int getNoOfInputs(){
		return noOfInputs;
	}
	
	public int getNoOfClasses(){
		return noOfClasses;
	}
	
	public void setNoOfClasses(int num){
		if(num < noOfClasses){
			noOfClasses = num;
			float[][] tempTar = new float[noOfSamples][num];
			for(int i = 0;i < tempTar.length;i++){
				for(int k = 0;k < tempTar[i].length;k++){
					tempTar[i][k] = targets[i][k];
				}
			}
			targets = new float[noOfSamples][num];
			setTargets(tempTar);
		}else if(num > noOfClasses){
			noOfClasses = num;
			float[][] tempTar = new float[noOfSamples][num];
			for(int i = 0;i < tempTar.length;i++){
				for(int k = 0;k < targets[i].length;k++){
					tempTar[i][k] = targets[i][k];
				}
			}
			targets = new float[noOfSamples][num];
			setTargets(tempTar);
		}
	}
	
	public ArrayList[] stratifiedSelect(float ratio/*, ArrayList<Integer> selectedIndex, ArrayList<Integer> remainedIndex*/){
		/*
		 * Randomly select n samples from the data set with the ratio of
		 * each class in the selected set the same to S and return the selected set
		 * n=ratio*numOfSamples.
		 * the orininal remainedIndex={0,1,...,noOfSamples-1}
		 */
		//int[] index = new int[(int)ratio * noOfSamples];
		int[] numEachC = getNumEachClass();
		int[] selectedNumEachC = new int[numEachC.length];
		ArrayList<Integer> selectedIndex = new ArrayList<Integer>(0);
		ArrayList<Integer> remainedIndex = new ArrayList<Integer>(0);
		for(int i = 0;i < numEachC.length;i++){
			selectedNumEachC[i] = (int) (numEachC[i] * ratio);
		}
		/*int[] index = new int[Matrix.sum(selectedNumEachC)];
		remainedIndex = new int[noOfSamples - index.length];*/
		//sortByClass();
		for(int i = 0;i < noOfSamples;i++){
			float[] output = getTargets(i);
			int label = Matrix.maxLocation(output);
			if(selectedNumEachC[label] > 0){
				selectedIndex.ensureCapacity(selectedIndex.size() + 1);
				selectedIndex.add(i);
				selectedNumEachC[label]--;
			}else{
				remainedIndex.ensureCapacity(remainedIndex.size() + 1);
				remainedIndex.add(i);
			}
		}
		ArrayList[] arr = new ArrayList[2];
		arr[0] = selectedIndex;
		arr[1] = remainedIndex;
		return arr;
	}
	
	public Dataset[] partForCroVali_Stratified(int numFold,int seed,int foldIndex){
		this.disorder(seed);
		this.sortByClass();
		
		ArrayList<float[]> inputsVali = new ArrayList<float[]>(0);
		ArrayList<float[]> inputsTrain = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsVali = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsTrain = new ArrayList<float[]>(0);
		for(int d = 0;d < noOfSamples;d++){
			if(d % numFold == foldIndex){
				inputsVali.ensureCapacity(inputsVali.size()+1);
				inputsVali.add(this.inputs[d]);
				targetsVali.ensureCapacity(targetsVali.size()+1);
				targetsVali.add(this.targets[d]);
			}else{
				inputsTrain.ensureCapacity(inputsTrain.size()+1);
				inputsTrain.add(this.inputs[d]);
				targetsTrain.ensureCapacity(targetsTrain.size()+1);
				targetsTrain.add(this.targets[d]);
			}
		}
		Dataset valiData = new Dataset(inputsVali.size(),noOfInputs,noOfClasses);
		for(int k = 0;k < valiData.noOfSamples;k++){
			valiData.setInputs(k, inputsVali.get(k));
			valiData.setTargets(k, targetsVali.get(k));
		}
		Dataset trainData = new Dataset(inputsTrain.size(),noOfInputs,noOfClasses);
		for(int k = 0;k < trainData.noOfSamples;k++){
				trainData.setInputs(k, inputsTrain.get(k));
			trainData.setTargets(k, targetsTrain.get(k));
		}
		Dataset[] data = {trainData,valiData};
		return data;
	}
	
	public void formCrossValidationSet(String pathToSave, int num,int seed){
		this.disorder(seed);
		this.sortByClass();
		for(int i = 0;i < num;i++){
			ArrayList<float[]> inputsVali = new ArrayList<float[]>(0);
			ArrayList<float[]> inputsTrain = new ArrayList<float[]>(0);
			ArrayList<float[]> targetsVali = new ArrayList<float[]>(0);
			ArrayList<float[]> targetsTrain = new ArrayList<float[]>(0);
			for(int d = 0;d < noOfSamples;d++){
				if(d % num == i){
					inputsVali.ensureCapacity(inputsVali.size()+1);
					inputsVali.add(this.inputs[d]);
					targetsVali.ensureCapacity(targetsVali.size()+1);
					targetsVali.add(this.targets[d]);
				}else{
					inputsTrain.ensureCapacity(inputsTrain.size()+1);
					inputsTrain.add(this.inputs[d]);
					targetsTrain.ensureCapacity(targetsTrain.size()+1);
					targetsTrain.add(this.targets[d]);
				}
			}
			Dataset valiData = new Dataset(inputsVali.size(),noOfInputs,noOfClasses);
			for(int k = 0;k < valiData.noOfSamples;k++){
				valiData.setInputs(k, inputsVali.get(k));
				valiData.setTargets(k, targetsVali.get(k));
			}
			Dataset trainData = new Dataset(inputsTrain.size(),noOfInputs,noOfClasses);
			for(int k = 0;k < trainData.noOfSamples;k++){
				trainData.setInputs(k, inputsTrain.get(k));
				trainData.setTargets(k, targetsTrain.get(k));
			}
			valiData.saveDataset(pathToSave + "vali" + i);
			trainData.saveDataset(pathToSave + "train" + i);
		}
	}
	
	public Dataset clone(){
		Dataset data = null;
		try {
			data = (Dataset)super.clone();
			data.inputs = new float[this.noOfSamples][this.noOfInputs];
			data.targets = new float[this.noOfSamples][this.noOfClasses];
			data.setInputs(this.getInputs());
			data.setTargets(this.getTargets());
		} catch (CloneNotSupportedException e) {

			e.printStackTrace();
			System.err.println("Dataset can't clone.");
		}
		return data;
	}
	
	public void saveDataset(String filename){
		try{
			FileOutputStream fileOutStr = new FileOutputStream(filename);
			ObjectOutputStream ObjOutStr = new ObjectOutputStream(fileOutStr);
			ObjOutStr.writeObject(this);
			ObjOutStr.close();
		}
		catch(IOException IOe){
			System.err.println("Some error occurs when save Dataset!");
			System.exit(1);
		}
	}
	
	public void writeData(String filename){
		BufferedWriter br = null;
		DecimalFormat df = new DecimalFormat("0.000000");
		try{
			br = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(filename)));
			for(int i = 0;i < this.noOfSamples;i++){
				for(int k = 0;k < inputs[i].length;k++){
					br.write(df.format(inputs[i][k]) + " ");
				}
				for(int k = 0;k < targets[i].length;k++){
					br.write(df.format(targets[i][k]) + " ");
				}
				br.write("\r\n");
			}
			
			br.close();
		}
		catch(IOException IOe) {
			IOe.printStackTrace();
			System.exit(1);
		}
	}
	
	public void writeData(String filename,DecimalFormat df){
		BufferedWriter br = null;
		//DecimalFormat df = new DecimalFormat("0.000000");
		try{
			br = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(filename)));
			for(int i = 0;i < this.noOfSamples;i++){
				for(int k = 0;k < inputs[i].length;k++){
					br.write(df.format(inputs[i][k]) + " ");
				}
				for(int k = 0;k < targets[i].length;k++){
					br.write(df.format(targets[i][k]) + " ");
				}
				br.write("\r\n");
			}
			
			br.close();
		}
		catch(IOException IOe) {
			IOe.printStackTrace();
			System.exit(1);
		}
	}
	
	public static Dataset loadDataset(String filename) throws ClassNotFoundException{
		File myFile = new File(filename);
		Dataset data = null;
		try{
			FileInputStream fileInStr = new FileInputStream(myFile);
			ObjectInputStream ObjInStr = new ObjectInputStream(fileInStr);
			data = (Dataset)ObjInStr.readObject();
			ObjInStr.close();
		}
		catch(IOException IOe){
			System.err.println("Load Dataset err.");
			System.exit(1);
		}
		return data;
	}
	
	public static Dataset uniteData(Dataset dataset1,Dataset dataset2){
		
		int sam = dataset1.getNoOfSamples() + dataset2.getNoOfSamples();
		int in = dataset1.getNoOfInputs();
		int cla = dataset1.getNoOfClasses();
		Dataset unitDataset = new Dataset(sam,in,cla);
		for(int i = 0;i < dataset1.getNoOfSamples();i++){
			unitDataset.setTargets(i, dataset1.getTargets(i));
			unitDataset.setInputs(i, dataset1.getInputs(i));
		}
		for(int i = dataset1.getNoOfSamples();i < sam;i++){
			unitDataset.setInputs(i, dataset2.getInputs(i - dataset1.getNoOfSamples()));
			unitDataset.setTargets(i, dataset2.getTargets(i - dataset1.getNoOfSamples()));
		}
		return unitDataset;
	}
	
	public static void missingValue(String origFilename, int[] valueType, 
			DecimalFormat df, String newFilename, String missingNote) throws IOException{
		/*
		 * valueType is the type of each feature, 0 for binary or nominal, 1 for continuous 
		 */
		
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(origFilename)));
		String[] original = new String[valueType.length];//every element is the data of a feature, with a " " between every data
		for(int i = 0;i < original.length;i++){
			original[i] = "";
		}
		String line = reader.readLine();
		StringTokenizer tempTokenizer = new StringTokenizer( line," \t," );
		
		if(tempTokenizer.countTokens() != valueType.length){
			System.err.println("The length of valueType doesn't match the number of attributes!");
			System.exit(0);
		}
		boolean[] hasMissingValue = new boolean[valueType.length];
		
		System.out.println("Reading the file...");
		/*
		 * After the follow while loop, the data of feature i are saved in original[i], including missNote
		 */
		int numOfline = 0;
		while(line != null){
			numOfline++;
			//System.out.println("" + numOfline);
			StringTokenizer tokenizer = new StringTokenizer( line," \t," );
			
			for(int count = 0;count < original.length;count++){
				String currentValue = tokenizer.nextToken();
				original[count] += currentValue + " ";
				if(currentValue.equals(missingNote)){
					hasMissingValue[count] = true;
				}
			}
			line = reader.readLine();
		}
		reader.close();
		
		System.out.println("Done!");
		
		
		float[][] finalData = new float[numOfline][valueType.length];
		for(int fea = 0;fea < original.length;fea++){
			System.out.print("Processing attribute " + (fea+1) + "...");
			if(hasMissingValue[fea]){
				StringTokenizer tokenizer = new StringTokenizer(original[fea]," " + missingNote);
				float[] values = new float[tokenizer.countTokens()];
				for(int k = 0;k < values.length;k++){
					values[k] = Float.valueOf(tokenizer.nextToken());
				}
				float replaceValue = 0;
				if(valueType[fea] == 0){
					replaceValue = Matrix.majority(values);
				}else if(valueType[fea] == 1){
					replaceValue = Matrix.mean(values);
				}else{
					System.err.println("Please set the right valueType!");
					System.exit(0);
				}
				original[fea] = original[fea].replace(missingNote, df.format(replaceValue));
			}
			StringTokenizer tokenizer = new StringTokenizer(original[fea]);
			for(int k = 0;k < numOfline;k++){
				finalData[k][fea] = Float.valueOf(tokenizer.nextToken());
			}
			System.out.println("Done!");
		}
		Matrix.writeMatrix(finalData, newFilename,df);
		
	}
	
	public static void missingValue(String origFilename, int[] valueType, 
			DecimalFormat df, String newFilename) throws IOException{
		/*
		 * valueType is the type of each feature, 0 for binary or nominal, 1 for continuous 
		 */
		
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(origFilename)));
		String[] original = new String[valueType.length];//every element is the data of a feature, with a " " between every data
		for(int i = 0;i < original.length;i++){
			original[i] = "";
		}
		String line = reader.readLine();
		StringTokenizer tempTokenizer = new StringTokenizer( line," \t," );
		
		if(tempTokenizer.countTokens() != valueType.length){
			System.err.println("The length of valueType doesn't match the number of attributes!");
			System.exit(0);
		}
		boolean[] hasMissingValue = new boolean[valueType.length];
		
		System.out.println("Reading the file...");
		/*
		 * After the follow while loop, the data of feature i are saved in original[i], including "?"
		 */
		int numOfline = 0;
		while(line != null){
			numOfline++;
			//System.out.println("" + numOfline);
			StringTokenizer tokenizer = new StringTokenizer( line," \t," );
			
			for(int count = 0;count < original.length;count++){
				String currentValue = tokenizer.nextToken();
				original[count] += currentValue + " ";
				if(currentValue.equals("?")){
					hasMissingValue[count] = true;
				}
			}
			line = reader.readLine();
		}
		reader.close();
		
		System.out.println("Done!");
		
		
		float[][] finalData = new float[numOfline][valueType.length];
		for(int fea = 0;fea < original.length;fea++){
			System.out.print("Processing attribute " + (fea+1) + "...");
			if(hasMissingValue[fea]){
				StringTokenizer tokenizer = new StringTokenizer(original[fea]," ?");
				float[] values = new float[tokenizer.countTokens()];
				for(int k = 0;k < values.length;k++){
					values[k] = Float.valueOf(tokenizer.nextToken());
				}
				float replaceValue = 0;
				if(valueType[fea] == 0){
					replaceValue = Matrix.majority(values);
				}else if(valueType[fea] == 1){
					replaceValue = Matrix.mean(values);
				}else{
					System.err.println("Please set the right valueType!");
					System.exit(0);
				}
				original[fea] = original[fea].replace("?", df.format(replaceValue));
			}
			StringTokenizer tokenizer = new StringTokenizer(original[fea]);
			for(int k = 0;k < numOfline;k++){
				finalData[k][fea] = Float.valueOf(tokenizer.nextToken());
			}
			System.out.println("Done!");
		}
		Matrix.writeMatrix(finalData, newFilename,df);
		
	}
	
	public Dataset getLowConfidenceData(Ensemble ens,float b){
		ArrayList<float[]> inputsGetted = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsGetted = new ArrayList<float[]>(0);
		for(int i = 0;i < noOfSamples;i++){
			float[] in = this.getInputs(i);
			float[] tar = this.getTargets(i);
			int pred = ens.getPrediction(in);
			if(tar[pred] == 0){
				//wrong classified
				inputsGetted.ensureCapacity(inputsGetted.size()+1);
				inputsGetted.add(in);
				targetsGetted.ensureCapacity(targetsGetted.size()+1);
				targetsGetted.add(tar);
			}else if(1f-ens.getOutputs(in)[pred] > b){
				inputsGetted.ensureCapacity(inputsGetted.size()+1);
				inputsGetted.add(in);
				targetsGetted.ensureCapacity(targetsGetted.size()+1);
				targetsGetted.add(tar);
			}
		}
		Dataset data = new Dataset(inputsGetted.size(),noOfInputs,noOfClasses);
		for(int i = 0;i < data.noOfSamples;i++){
			data.setInputs(i,inputsGetted.get(i));
			data.setTargets(i, targetsGetted.get(i));
		}
		return data;
	}

	public Dataset getWrongClassifiedData(Ensemble ens){
		ArrayList<float[]> inputsGetted = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsGetted = new ArrayList<float[]>(0);
		for(int i = 0;i < noOfSamples;i++){
			float[] in = this.getInputs(i);
			float[] tar = this.getTargets(i);
			int pred = ens.getPrediction(in);
			if(tar[pred] == 0){
				//wrong classified
				inputsGetted.ensureCapacity(inputsGetted.size()+1);
				inputsGetted.add(in);
				targetsGetted.ensureCapacity(targetsGetted.size()+1);
				targetsGetted.add(tar);
			}
		}
		Dataset data = new Dataset(inputsGetted.size(),noOfInputs,noOfClasses);
		for(int i = 0;i < data.noOfSamples;i++){
			data.setInputs(i,inputsGetted.get(i));
			data.setTargets(i, targetsGetted.get(i));
		}
		return data;
	}
	
	public Dataset balanceData(Dataset data){
		//make the dataset "data" balanced using this dataset
		/*
		 * select some data from 'this', and add them to 'data'
		 */
		int[] numEachClass = data.getNumEachClass();
		int maxNum = Matrix.maxValue(numEachClass);
		ArrayList<float[]> inputsGetted = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsGetted = new ArrayList<float[]>(0);
		int numAdd = 0;
		int minNum = Matrix.minValue(numEachClass);
		int t = (int)Math.ceil(1d * maxNum/minNum)-1;
		this.disorder();
		while(t-- > 0){
			for(int i = 0;i < noOfSamples;i++){
				int classLable = Matrix.maxLocation(targets[i]);
				if(numEachClass[classLable] < maxNum){
					inputsGetted.ensureCapacity(inputsGetted.size()+1);
					inputsGetted.add(inputs[i]);
					targetsGetted.ensureCapacity(targetsGetted.size()+1);
					targetsGetted.add(targets[i]);
					numEachClass[classLable]++;
					numAdd++;
				}
			}
		}
		Dataset balancedData = new Dataset(data.noOfSamples+numAdd,data.noOfInputs,data.noOfClasses);
		for(int i = 0;i < data.noOfSamples;i++){
			balancedData.inputs[i] = data.inputs[i];
			balancedData.targets[i] = data.targets[i];
		}
		for(int i = data.noOfSamples;i < data.noOfSamples+inputsGetted.size();i++){
			balancedData.inputs[i] = inputsGetted.get(i - data.noOfSamples);
			balancedData.targets[i] = targetsGetted.get(i - data.noOfSamples);
		}
		return balancedData;
	}
	
	public int[] getNumEachClass(){
		int[] numEachClass = new int[noOfClasses];
		for(int i = 0;i < targets.length;i++){
			numEachClass[Matrix.maxLocation(targets[i])]++;
		}
		return numEachClass;
	}
	
	public float[] getRatioEachClass(){
		int[] numEachClass = this.getNumEachClass();
		float[] ratio = new float[numEachClass.length];
		for(int i = 0;i < ratio.length;i++){
			ratio[i] = (float)numEachClass[i] / this.getNoOfSamples();
		}
		return ratio;
	}
	
	public void duplicateData(float[] rho){
		//duplicate the minority class by rho
		int[] newNumEachClass = new int[this.getNoOfClasses()];
		int[] numEachClass = this.getNumEachClass();
		for(int i = 0;i < newNumEachClass.length;i++){
			newNumEachClass[i] = (int) ((rho[i] + 1f) * numEachClass[i]);
		}
		
		int newNum = Matrix.sum(newNumEachClass);
		float[][] newInputs = new float[newNum][noOfInputs];
		float[][] newTargets = new float[newNum][noOfClasses];
		this.disorder();
		this.sortByClass();
		int count = 0;
		int index = 0;
		while(count < newNum){
			int label = Matrix.maxLocation(getTargets(index));
			if(numEachClass[label] <= newNumEachClass[label]){
				int tempCount = 0;
				while(tempCount++ < newNumEachClass[label]){
					if(index >= noOfSamples || Matrix.maxLocation(getTargets(index)) != label){
						index -= numEachClass[label];
					}
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}else{
				int tempCount = 0;
				while(tempCount++ < newNumEachClass[label]){
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}
			while(index < noOfSamples && Matrix.maxLocation(getTargets(index)) == label){
				index++;
			}
		}
		noOfSamples = newNum;
		this.inputs = new float[noOfSamples][noOfInputs];
		this.targets = new float[noOfSamples][noOfClasses];
		this.setInputs(newInputs);
		this.setTargets(newTargets);
	}
	
	public void balanceData(int[] newNumEachClass){
		/*
		 * make this dataset balance
		 */
		int[] numEachClass = this.getNumEachClass();
		int newNum = Matrix.sum(newNumEachClass);
		float[][] newInputs = new float[newNum][noOfInputs];
		float[][] newTargets = new float[newNum][noOfClasses];
		this.disorder();
		this.sortByClass();
		int count = 0;
		int index = 0;
		while(count < newNum){
			int label = Matrix.maxLocation(getTargets(index));
			if(numEachClass[label] <= newNumEachClass[label]){
				int tempCount = 0;
				while(tempCount++ < newNumEachClass[label]){
					if(index >= noOfSamples || Matrix.maxLocation(getTargets(index)) != label){
						index -= numEachClass[label];
					}
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}else{
				int tempCount = 0;
				while(tempCount++ < newNumEachClass[label]){
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}
			while(index < noOfSamples && Matrix.maxLocation(getTargets(index)) == label){
				index++;
			}
		}
		noOfSamples = newNum;
		this.inputs = new float[noOfSamples][noOfInputs];
		this.targets = new float[noOfSamples][noOfClasses];
		this.setInputs(newInputs);
		this.setTargets(newTargets);
	}
	
	public void balanceData(int avgNumEachClass){
		/*
		 * make this dataset balance
		 */
		int[] numEachClass = this.getNumEachClass();
		int newNum = avgNumEachClass * noOfClasses;
		float[][] newInputs = new float[newNum][noOfInputs];
		float[][] newTargets = new float[newNum][noOfClasses];
		this.sortByClass();
		int count = 0;
		int index = 0;
		while(count < newNum){
			int label = Matrix.maxLocation(getTargets(index));
			if(numEachClass[label] <= avgNumEachClass){
				int tempCount = 0;
				while(tempCount++ < avgNumEachClass){
					if(index >= noOfSamples || Matrix.maxLocation(getTargets(index)) != label){
						index -= numEachClass[label];
					}
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}else{
				int tempCount = 0;
				while(tempCount++ < avgNumEachClass){
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}
			while(index < noOfSamples && Matrix.maxLocation(getTargets(index)) == label){
				index++;
			}
		}
		noOfSamples = newNum;
		this.inputs = new float[noOfSamples][noOfInputs];
		this.targets = new float[noOfSamples][noOfClasses];
		this.setInputs(newInputs);
		this.setTargets(newTargets);
	}
	
	public int[] balanceData_onlyGetIndex(int avgNumEachClass){
		/*
		 * make this dataset balance, return the index of the original data set
		 * while the original data set is not changed
		 */
		int[] numEachClass = this.getNumEachClass();
		Dataset tempData = this.clone();
		int newNum = avgNumEachClass * noOfClasses;
		int[] balancedIndex = new int[newNum];
		/*float[][] newInputs = new float[newNum][noOfInputs];
		float[][] newTargets = new float[newNum][noOfClasses];*/
		int[] sortedIndex = tempData.sortByClass();
		int count = 0;
		int index = 0;
		while(count < newNum){
			int label = Matrix.maxLocation(tempData.getTargets(index));
			if(numEachClass[label] <= avgNumEachClass){
				int tempCount = 0;
				while(tempCount++ < avgNumEachClass){
					if(index >= tempData.getNoOfSamples() || Matrix.maxLocation(tempData.getTargets(index)) != label){
						index -= numEachClass[label];
					}
					balancedIndex[count++] = sortedIndex[index++];
					/*newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;*/
					//index++;
				}
			}else{
				int tempCount = 0;
				while(tempCount++ < avgNumEachClass){
					/*newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();*/
					balancedIndex[count++] = sortedIndex[index++];
					//index++;
				}
			}
			while(index < tempData.getNoOfSamples() && Matrix.maxLocation(tempData.getTargets(index)) == label){
				index++;
			}
		}
		return balancedIndex;
		/*noOfSamples = newNum;
		this.inputs = new float[noOfSamples][noOfInputs];
		this.targets = new float[noOfSamples][noOfClasses];*/
		/*this.setInputs(newInputs);
		this.setTargets(newTargets);*/
	}
	
	public void balanceData(){
		/*
		 * make this dataset balance, the number of instances is not changed
		 */
		int[] numEachClass = this.getNumEachClass();
		int avgNumEachClass = noOfSamples / noOfClasses;
		int newNum = avgNumEachClass * noOfClasses;
		float[][] newInputs = new float[newNum][noOfInputs];
		float[][] newTargets = new float[newNum][noOfClasses];
		this.sortByClass();
		int count = 0;
		int index = 0;
		while(count < newNum){
			int label = Matrix.maxLocation(getTargets(index));
			if(numEachClass[label] <= avgNumEachClass){
				int tempCount = 0;
				while(tempCount++ < avgNumEachClass){
					if(index >= noOfSamples || Matrix.maxLocation(getTargets(index)) != label){
						index -= numEachClass[label];
					}
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}else{
				int tempCount = 0;
				while(tempCount++ < avgNumEachClass){
					newInputs[count] = getInputs(index).clone();
					newTargets[count] = getTargets(index).clone();
					count++;
					index++;
				}
			}
			while(index < noOfSamples && Matrix.maxLocation(getTargets(index)) == label){
				index++;
			}
		}
		noOfSamples = newNum;
		this.inputs = new float[noOfSamples][noOfInputs];
		this.targets = new float[noOfSamples][noOfClasses];
		this.setInputs(newInputs);
		this.setTargets(newTargets);
	}
	
	/*public void randomUndersampling(float rate){
		
	}*/
	
	public Dataset sampleDataByNCLEF(Ensemble ens,Parameters para){
		/*
		 * resample the dataset by NCL Error Function
		 */
		float[] weights = new float[noOfSamples];
		int numNet = para.getNoOfNet();
		for(int d = 0;d < getNoOfSamples();d++){
			float[][] outputs = ens.getIndividualOutputs(getInputs(d));
			
			AVG avg = new AVG();
			para.setEnsAvgOutputs(avg.combineOutputs(outputs));
			float[] targets = getTargets(d);
			for(int i = 0;i < numNet;i++){
				float[] estimate = outputs[i];
				for(int node = 0;node < ens.getNetwork(0).getOutputLayer().size();node++){
					ErrorParameters errPara = new ErrorParameters(estimate[node],targets[node],para.getEnsAvgOutputs()[node],para.getLambda());
					weights[d] += ErrorFunction.getEmpiricalError(errPara);
				}
			}
		}
		
		/*float min = Math.abs(Matrix.minValue(weights));
		for(int i = 0;i < weights.length;i++){
			weights[i] += min;
		}*/
		float sum = Matrix.sum(weights);
		for(int i = 0;i < noOfSamples;i++){
			weights[i] = weights[i]/sum;
		}
		return resample(noOfSamples,weights);

	}
	
	public Dataset resample(int num,float[] dataWeights){
		/*
		 * Resample the dataset by the distribution using roulette
		 */
		ArrayList<float[]> inputsGetted = new ArrayList<float[]>(0);
		ArrayList<float[]> targetsGetted = new ArrayList<float[]>(0);
		
		float[] sumWeights = new float[dataWeights.length];
		float sum = 0;
		for(int i = 0;i < noOfSamples;i++){
			sum += dataWeights[i];
			sumWeights[i] = sum;
		}
		Random ran = new Random();
		for(int i = 0;i < num;i++){
			float ranFloat = ran.nextFloat();
			for(int k = 0;k < noOfSamples;k++){
				if(ranFloat < sumWeights[k]){
					inputsGetted.ensureCapacity(inputsGetted.size() + 1);
					inputsGetted.add(inputs[k]);
					targetsGetted.ensureCapacity(targetsGetted.size() + 1);
					targetsGetted.add(targets[k]);
					break;
				}
			}
		}
		Dataset data = new Dataset(inputsGetted.size(),noOfInputs,noOfClasses);
		for(int i = 0;i < data.noOfSamples;i++){
			data.setInputs(i, inputsGetted.get(i));
			data.setTargets(i, targetsGetted.get(i));
		}
		return data;
	}
	
	public void selectFea(int[] index){
		noOfInputs = index.length;
		float[][] newInputs = new float[noOfSamples][noOfInputs];
		for(int i = 0, p = 0; i < index.length; i++, p++){
			for(int k = 0;k < noOfSamples;k++){
				newInputs[k][p] = this.getInputs(k)[index[i]-1];
			}
		}
		this.inputs = newInputs;
	}
	
	
	public Dataset sampling_cutMajClass(int sample_size){
		int[] numEachClass = this.getNumEachClass();
		Dataset data = null;
		int[] index = Matrix.getRandomIndex(this.getNoOfSamples());
		if(numEachClass[0] < numEachClass[1]){
			int numSamples = numEachClass[0] + sample_size;
			data = new Dataset(numSamples,noOfInputs,noOfClasses);
			int newIndex = 0;
			for(int i = 0,count = 0;i < index.length;i++){
				float[] input = this.getInputs(index[i]);
				float[] target = this.getTargets(index[i]);
				if(target[0] == 1){
					data.setInputs(newIndex, input);
					data.setTargets(newIndex, target);
					newIndex++;
				}else{
					if(count < sample_size){
						data.setInputs(newIndex,input);
						data.setTargets(newIndex, target);
						newIndex++;
						count++;
					}
				}
			}
		}else{
			int numSamples = numEachClass[1] + sample_size;
			data = new Dataset(numSamples,noOfInputs,noOfClasses);
			int newIndex = 0;
			for(int i = 0,count = 0;i < index.length;i++){
				float[] input = this.getInputs(index[i]);
				float[] target = this.getTargets(index[i]);
				if(target[1] == 1){
					data.setInputs(newIndex, input);
					data.setTargets(newIndex, target);
					newIndex++;
				}else{
					if(count < sample_size){
						data.setInputs(newIndex,input);
						data.setTargets(newIndex, target);
						newIndex++;
						count++;
					}
				}
			}
		}
		return data;
	}
	
	
	public float[] scMatInClass(int classIndex){
		int[] numEachClass = this.getNumEachClass();
		int num = numEachClass[classIndex];
		float[] scMatTrace = new float[num];
		float[][] tempInputs = new float[num][this.noOfInputs];
		for(int i = 0, k = 0;i < this.noOfSamples;i++){
			if(targets[i][classIndex] == 1){
				tempInputs[k++] = this.getInputs(i);
			}
		}
		float[][] transposeMat = Matrix.transpose(tempInputs);
		for(int i = 0;i < transposeMat.length;i++){
			float[] X = transposeMat[i];
			float mean_X = Matrix.mean(X);
			float[] X_minus_mean = Matrix.minus(X, mean_X);
			//float[][] X_minus_mean = Matrix.transpose(X_minus_mean_T);
			float[] tempMat = Matrix.dotProduct(X_minus_mean, X_minus_mean);
			scMatTrace = Matrix.plus(tempMat, scMatTrace);
		}
		//float p = (float)num / this.noOfSamples;
		//scMatTrace = Matrix.product(scMatTrace, p);
		return scMatTrace;
	}
	
	public float scMatBetweenClasses(){

		
		int[] numEachClass = this.getNumEachClass();
		this.sortByClass();
		float[][][] sepInputs = new float[this.noOfClasses][][];
		for(int i = 0,count = 0;i < sepInputs.length;i++){
			sepInputs[i] = new float[numEachClass[i]][];
			for(int k = 0;k < numEachClass[i];k++){
				sepInputs[i][k] = this.getInputs(count++);
			}
		}
		float[][][] sepInputsTrans = new float[sepInputs.length][][];
		for(int i = 0;i < sepInputsTrans.length;i++){
			sepInputsTrans[i] = Matrix.transpose(sepInputs[i]);
		}
		float meanTrace = 0;
		for(int i = 0;i < sepInputsTrans.length;i++){
			 
			for(int k = 0;k < sepInputsTrans.length;k++){
				if(k == i){
					continue;
				}
				
				float[][] mat_i = sepInputsTrans[i];
				float[][] mat_k = sepInputsTrans[k];
				float tempTrace = 0;
				for(int fea = 0;fea < mat_i.length;fea++){
					float[] X = mat_i[fea];
					float mean_X = Matrix.mean(mat_k[fea]);
					float[] X_minus_mean = Matrix.minus(X, mean_X);
					float[] tempMat = Matrix.dotProduct(X_minus_mean, X_minus_mean);
					tempTrace += Matrix.mean(tempMat);
				}
				meanTrace += tempTrace;
			}
			//float p = (float)numEachClass[i] / this.noOfSamples;
			//meanTrace *= p; 
			//meanTrace /= numEachClass[i];
		}
		meanTrace /= sepInputsTrans.length * (sepInputsTrans.length - 1);
		
		return meanTrace;
		
	}
	
	public float avgAucOfEachFea(){
		float auc = 0;
		
		float[] targets = new float[this.noOfSamples];
		for(int i = 0;i < targets.length;i++){
			targets[i] = this.getTargets(i)[0];
		}
		for(int i = 0;i < this.noOfInputs;i++){
			float[] score = new float[this.noOfSamples];
			for(int k = 0;k < score.length;k++){
				score[k] = this.getInputs(k)[i];
			}
			float tempAuc = calculateAUC(score,targets);
			if(tempAuc < 0.5){
				tempAuc = 1 - tempAuc;
			}
			auc += tempAuc;
		}
		auc /= this.getNoOfInputs();
		return auc;
	}
	public static float calculateAUC(float[] score,float[] targets){
		int[] sortedIndex = Matrix.QuickSort(score);
		return calculateAUC(score,sortedIndex,targets);
	}
	public static float calculateAUC(float[] score,int[] index,float[] targets){
		float auc = 0;
		float[] rank = new float[index.length];
		for(int i = 0;i < rank.length;){
			int tempCount = 0,tempIndex = i;
			float sumRank = 0;
			for(;tempIndex < index.length && score[tempIndex] == score[i];
						tempIndex++,tempCount++){
				sumRank += (float)tempIndex;
			}
			for(int k = i;k < tempIndex;k++){
				rank[k] = sumRank / (float)tempCount + 1;
			}
			i = tempIndex;
		}
		float SP = 0;
		for(int i = 0;i < index.length;i++){
			if(targets[index[i]] == 1){
				SP += rank[i];
			}
		}
		float totalNum = targets.length;
		float P = Matrix.sum(targets);
		float N = totalNum - P;
		auc = (SP - P * (P + 1f) / 2f) / (P * N);
		return auc;
	}
	
}
