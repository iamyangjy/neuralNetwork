package com.ruijie.ncl;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

public abstract class Matrix {
	
	public static int maxLocation(float[] array){
		int loc = 0;
		float max = array[0];
		for(int i = 1;i < array.length;i++){
			if(max < array[i]){
				max = array[i];
				loc = i;
			}
		}
		return loc;
	}
	
	public static int maxLocation(int[] array){
		int loc = 0;
		int max = array[0];
		for(int i = 1;i < array.length;i++){
			if(max < array[i]){
				max = array[i];
				loc = i;
			}
		}
		return loc;
	}
	
	public static float maxValue(float[] array){
		return array[maxLocation(array)];
	}
	
	public static float secondMaxValue(float[] array){
		float[] temp = array.clone();
		temp[maxLocation(array)]=temp[minLocation(array)]-1f;
		return maxValue(temp);
	}
	
	public static int maxValue(int[] array){
		return array[maxLocation(array)];
	}
	
	public static int[] maxLocation(float[][] array){
		int[] loc = new int[2];
		loc[0] = 0;
		loc[1] = 0;
		float max = array[0][0];
		for(int i = 0;i < array.length;i++){
			for(int j = 0;j < array[i].length;j++){
				if(max < array[i][j]){
					max = array[i][j];
					loc[0] = i;
					loc[1] = j;
				}
			}
		}
		return loc;
	}
	
	public static float maxValue(float[][] array){
		int[] location = maxLocation(array);
		return array[location[0]][location[1]];
	}
	
	
	public static int minLocation(float[] array){
		int loc = 0;
		float min = array[0];
		for(int i = 1;i < array.length;i++){
			if(min > array[i]){
				min = array[i];
				loc = i;
			}
		}
		return loc;
	}
	
	public static int minValue(int[] array){
		return array[minLocation(array)];
	}
	
	public static int minLocation(int[] array){
		int loc = 0;
		int min = array[0];
		for(int i = 1;i < array.length;i++){
			if(min > array[i]){
				min = array[i];
				loc = i;
			}
		}
		return loc;
	}
	
	public static float minValue(float[] array){
		return array[minLocation(array)];
	}
	
	public static int[] maxLocationOfEachRow(float[][] array){
		int[] ans = new int[array[0].length];
		for(int i = 0;i < ans.length;i++){
			ans[i] = 0;
			float max = array[0][i];
			for(int j = 0;j < array.length;j++){
				if(max < array[j][i]){
					max = array[j][i];
					ans[i] = j;
				}
			}
		}
		return ans;
	}
	
	public static int[] minLocationOfEachRow(float[][] array){
		int[] ans = new int[array[0].length];
		for(int i = 0;i < ans.length;i++){
			ans[i] = 0;
			float min = array[0][i];
			for(int j = 0;j < array.length;j++){
				if(min > array[j][i]){
					min = array[j][i];
					ans[i] = j;
				}
			}
		}
		return ans;
	}
	
	public static float[] plus(float[] array,float c){
		float[] arrayR = new float[array.length];
		for(int i = 0;i < array.length;i++)
			arrayR[i] = array[i] + c;
		return arrayR;
	}
	
	public static float[] minus(float[] array,float c){
		float[] arrayR = new float[array.length];
		for(int i = 0;i < array.length;i++)
			arrayR[i] = array[i] - c;
		return arrayR;
	}
	
	public static float[][] plus(float[][] array,float c){
		float[][] arrayR = new float[array.length][array[0].length];
		for(int i = 0;i < array.length;i++)
			arrayR[i] = plus(array[i],c);
		return arrayR;
	}
	
	public static float[][] minus(float[][] array,float c){
		float[][] arrayR = new float[array.length][array[0].length];
		for(int i = 0;i < array.length;i++)
			arrayR[i] = minus(array[i],c);
		return arrayR;
	}
	
	public static float[] plus(float[] array1,float[] array2){
		if(array1.length != array2.length){
			System.err.println("Array Plus:Arrays are not matched!\n");
			System.exit(1);
		}
		float[] arrayPlus = new float[array1.length];
		for(int i = 0;i < array1.length;i++){
			arrayPlus[i] = array1[i] + array2[i];
		}
		return arrayPlus;
	}
	
	public static float[] minus(float[] array1,float[] array2){
		if(array1.length != array2.length){
			System.err.println("Array Minus:Arrays are not matched!\n");
			System.exit(1);
		}
		float[] arrayMinus = new float[array1.length];
		for(int i = 0;i < array1.length;i++){
			arrayMinus[i] = array1[i] - array2[i];
		}
		return arrayMinus;
	}
	
	public static float[][] plus(float[][] array1,float[][] array2){
		if(array1.length != array2.length || array1[0].length != array2[0].length){
			System.err.println("Matrix Plus: Arrays are not matched!\n");
			System.exit(1);
		}
		float[][] arrayPlus = new float[array1.length][array1[0].length];
		for(int i = 0;i < array1.length;i++){
			for(int j = 0;j < array1[0].length;j++)
				arrayPlus[i][j] = array1[i][j] + array2[i][j];
		}
		return arrayPlus;
	}
	
	public static float[][] minus(float[][] array1,float[][] array2){
		if(array1.length != array2.length || array1[0].length != array2[0].length){
			System.err.println("Matris Minus: Arrays are not matched!\n");
			System.exit(1);
		}
		float[][] arrayR = new float[array1.length][array1[0].length];
		for(int i = 0;i < array1.length;i++){
			for(int j = 0;j < array1[0].length;j++)
				arrayR[i][j] = array1[i][j] - array2[i][j];
		}
		return arrayR;
	}
	
	public static void print(float[][] array){
		for(int i = 0;i < array.length;i++){
			for(int j = 0;j < array[0].length;j++){
				System.out.print(array[i][j] + "\t");
			}
			System.out.print("\n");
		}
	}
	
	public static void print(float[] array){
		for(int i = 0;i < array.length;i++){
			System.out.print(array[i] + "\t");
		}
		System.out.print("\n");
	}
	public static void print(int[] array){
		for(int i = 0;i < array.length;i++){
			System.out.print(array[i] + "\t");
		}
		System.out.print("\n");
	}

	public static float[] dotProduct(float[] array1,float[] array2){
		if(array1.length != array2.length){
			System.err.println("DotProduct: Arrays are not matched!\n");
			System.exit(1);
		}
		float[] arrayR = new float[array1.length];
		for(int i = 0;i < array1.length;i++){
			arrayR[i] = array1[i] * array2[i];
		}
		return arrayR;
	}

	public static float[][] product(float[][] array1,float[][] array2){
		if(array1[0].length != array2.length){
			System.err.println("Matrix Product: Arrays are not matched!\n");
			System.exit(1);
		}
		float[][] arrayR = new float[array1.length][array2[0].length];
		for(int i = 0;i < array1.length;i++){
			for(int j = 0;j < array2[0].length;j++){
				arrayR[i][j] = 0;
				for(int k = 0;k < array1[0].length;k++){
					arrayR[i][j] += array1[i][k] * array2[k][j];
				}
			}
		}
		return arrayR;
	}
	
	public static float[] product(float[] array,float c){
		for(int i = 0;i < array.length;i++)
			array[i] *= c;
		return array;
	}

	public static float[][] product(float[][] array,float c){
		for(int i = 0;i < array.length;i++)
			for(int j = 0;j < array[0].length;j++)
				array[i][j] *= c;
		return array;
	}
	
	public static float sum(float[] array){
		float ans = 0;
		for(int i = 0;i < array.length;i++)
			ans += array[i];
		return ans;
	}
	
	public static int sum(int[] array){
		int ans = 0;
		for(int i = 0;i < array.length;i++)
			ans += array[i];
		return ans;
	}
	
	public static float mean(float[] array){
		return sum(array)/(array.length * 1f);
	}
	public static float mean(int[] array){
		return sum(array)/(array.length * 1f);
	}
	
	public static float mean(float[][] matrix){
		int num = matrix.length * matrix[0].length;
		if(num == 0)return 0;
		return sum(matrix) / num * 1f;
	}
	
	public static float majority(float[] array){
		
		/*
		 * votes: each array is has 2 float values, one for data ,one for votes 
		 */
		ArrayList<float[]> votes = new ArrayList<float[]>();
		
		float[] temp = new float[2];
		temp[0] = array[0];
		temp[1] = 1f;
		votes.ensureCapacity(votes.size() + 1);
		votes.add(temp);
		
		float maxValue = temp[0];
		int maxIndex = 0;
		for(int i = 1;i < array.length;i++){
			int n = 0;
			for(;n < votes.size();n++){
				/*
				 * search in the list
				 */
				if(array[i] == votes.get(n)[0])break;
			}
			if(n == votes.size()){
				float[] newValue = new float[2];
				newValue[0] = array[i];
				newValue[1] = 1f;
				votes.ensureCapacity(votes.size() + 1);
				votes.add(newValue);
			}else{
				votes.get(n)[1] += 1f;
				if(votes.get(n)[1] > maxValue){
					maxValue = votes.get(n)[1];
					maxIndex = n;
				}
			}
		}
		return votes.get(maxIndex)[0];
	}
	
	public static int majority(int[] array){
		
		/*
		 * votes: each array is has 2 float values, one for data ,one for votes 
		 */
		ArrayList<int[]> votes = new ArrayList<int[]>();
		
		int[] temp = new int[2];
		temp[0] = array[0];
		temp[1] = 1;
		votes.ensureCapacity(votes.size() + 1);
		votes.add(temp);
		
		int maxValue = temp[0];
		int maxIndex = 0;
		for(int i = 1;i < array.length;i++){
			int n = 0;
			for(;n < votes.size();n++){
				/*
				 * search in the list
				 */
				if(array[i] == votes.get(n)[0])break;
			}
			if(n == votes.size()){
				int[] newValue = new int[2];
				newValue[0] = array[i];
				newValue[1] = 1;
				votes.ensureCapacity(votes.size() + 1);
				votes.add(newValue);
			}else{
				votes.get(n)[1]++;
				if(votes.get(n)[1] > maxValue){
					maxValue = votes.get(n)[1];
					maxIndex = n;
				}
			}
		}
		return votes.get(maxIndex)[0];
	}
	
	public static int midValue(int[] array){
		int len = array.length;
		int[] tempArray = array.clone();
		Matrix.QuickSort(tempArray);
		int mid = 0;
		if(len % 2 == 1){
			mid = tempArray[len / 2];
		}else{
			mid = (tempArray[len / 2] + tempArray[len / 2 - 1]) / 2;
		}
		return mid;
	}
	
	public static double std(float[] array){
		float[] temp = dotProduct(minus(array,mean(array)),minus(array,mean(array)));
		return Math.sqrt(sum(temp)/(array.length - 1));
		
	}
	
	public static double std(float[][] matrix){
		int num = matrix.length * matrix[0].length;
		float[] array = new float[num];
		int index = 0;
		for(int i = 0;i < matrix.length;i++){
			for(int k = 0;k < matrix[0].length;k++){
				array[index++] = matrix[i][k];
			}
		}
		return std(array);
	}
	
	public static float[][] transpose(float[][] array){
		float[][] arrayTrans = new float[array[0].length][array.length];
		for(int i = 0;i < arrayTrans.length;i++){
			for(int j = 0;j < arrayTrans[i].length;j++){
				arrayTrans[i][j] = array[j][i];
			}
		}
		return arrayTrans;
	}
	
	public static float[] meanOfEachRow(float[][] array){
		float[][] arrayTrans = transpose(array);
		float[] temp = new float[array[0].length];
		for(int i = 0;i < temp.length;i++){
			temp[i] = mean(arrayTrans[i]);
		}
		return temp;
	}
	
	public static float[] stdOfEachRow(float[][] array){
		float[][] arrayTrans = transpose(array);
		float[] temp = new float[array[0].length];
		for(int i = 0;i < temp.length;i++){
			temp[i] = (float)std(arrayTrans[i]);
		}
		return temp;
	}
	
	public static float sum(float[][] array){
		float s = 0;
		for(int i = 0;i < array.length;i++){
			for(int j = 0;j < array[i].length;j++){
				s += array[i][j];
			}
		}
		return s;
	}
	
	public static int findIndex(int tar,int[] array){
		for(int i = 0;i < array.length;i++){
			if(tar == array[i])
				return i;
		}
		return -1;
	}
	
	public static int findIndex(float tar,float[] array){
		for(int i = 0;i < array.length;i++){
			if(tar == array[i])
				return i;
		}
		return -1;
	}
	
	public static int[] findXY(String filename){
		//get the number of lines (x) and volumes (y) of a matrix in the file
		BufferedReader reader = null;
		int[] XY = new int[2];
		try{
			// Open the file
			reader = new BufferedReader(new FileReader(new File(filename)));
		}
		catch(FileNotFoundException ex){
			System.err.println("File '"+filename+"' is not found.");
			System.exit(1);
		}
		try{
	
			String line = reader.readLine();
			StringTokenizer tokenizer = new StringTokenizer( line );
			XY[1] = tokenizer.countTokens();
			
			//read in line at a time, tokenize and add to inputs and targets
			while(line != null){
		    		
		    		XY[0]++;
		    		line = reader.readLine();
			}
			reader.close();
		}
		catch(IOException ex){
			System.err.print("There is some problems when reading from the file,please check it.");
			System.exit(1);
		}
		return XY;
	}
	
	public static int[] sortIndexUp(float[] array){
		int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < array.length;i++){
			for(int j = array.length - 1;j > i ;j--){
				if(array[j] < array[j-1]){
					float temp = array[j];
					array[j] = array[j-1];
					array[j-1] = temp;
					int tempIndex = index[j];
					index[j] = index[j-1];
					index[j-1] = tempIndex;
				}
			}
		}
		return index;
	}
	
	public static int[] sortIndexUp(int[] array){
		int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < array.length;i++){
			for(int j = array.length - 1;j > i ;j--){
				if(array[j] < array[j-1]){
					int temp = array[j];
					array[j] = array[j-1];
					array[j-1] = temp;
					int tempIndex = index[j];
					index[j] = index[j-1];
					index[j-1] = tempIndex;
				}
			}
		}
		return index;
	}
	
	
	private static int Partition(float[] array,int low,int high,int[] index){
		float temp = array[low];
		int tempIndex = index[low];
		while(low < high){
			while(low < high && array[high] >= temp)high--;
			array[low] = array[high];
			index[low] = index[high];
			while(low < high && array[low] <= temp)low++;
			array[high] = array[low];
			index[high] = index[low];
		}
		array[low] = temp;
		index[low] = tempIndex;
		return low;
	}
	private static void QSort(float[] array,int low,int high,int[] index){
		if(low < high){
			int loc = Partition(array,low,high,index);
			QSort(array,low,loc-1,index);
			QSort(array,loc + 1,high,index);
		}
	}
	public static int[] QuickSort(float[] array){
		/*int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}*/
		int[] index = disorder(array);
		QSort(array,0,array.length-1,index);
		return index;
	}
	
	
	private static int Partition(int[] array,int low,int high,int[] index){
		int temp = array[low];
		int tempIndex = index[low];
		while(low < high){
			while(low < high && array[high] >= temp)high--;
			array[low] = array[high];
			index[low] = index[high];
			while(low < high && array[low] <= temp)low++;
			array[high] = array[low];
			index[high] = index[low];
		}
		array[low] = temp;
		index[low] = tempIndex;
		return low;
	}
	private static void QSort(int[] array,int low,int high,int[] index){
		if(low < high){
			int loc = Partition(array,low,high,index);
			QSort(array,low,loc-1,index);
			QSort(array,loc + 1,high,index);
		}
	}
	public static int[] QuickSort(int[] array){
		/*int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}*/
		int[] index = disorder(array);
		QSort(array,0,array.length-1,index);
		return index;
	}
	
	public static float EuclideanDistance(float[][] matrixOne,float[][] matrixTwo){
		float[][] minusMat = minus(matrixOne, matrixTwo);
		return secondNormalForm(minusMat);
	}
	public static float secondNormalForm(float[][] matrix){
		float sum = 0;
		for(int i = 0;i < matrix.length;i++){
			for(int k = 0;k < matrix[i].length;k++){
				sum += matrix[i][k] * matrix[i][k];
			}
		}
		return (float)Math.sqrt(sum);
	}
	
	public static int[] randomArray(int range, int num){
		int[] array = new int[num];
		Random ran = new Random();
		for(int i = 0;i < num;){
			int k;
			int temp = ran.nextInt(range);
			for(k = 0;k < i;k++){
				if(temp == array[k])
					break;
			}
			if(k == i){
				array[i++] = temp;
			}
		}
		return array;
	}
	
	public static float[] randomArray(int num){
		float[] array = new float[num];
		Random ran = new Random();
		for(int i = 0;i < num;){
			int k;
			float temp = ran.nextFloat();
			for(k = 0;k < i;k++){
				if(temp == array[k])
					break;
			}
			if(k == i){
				array[i++] = temp;
			}
		}
		return array;
	}
	
	public static float[] subArray(float[] array, int beginIndex, int endIndex){
		if(endIndex >= array.length || beginIndex < 0){
			System.err.println("The index is illegal when getting subArray!");
			System.exit(1);
		}
		float[] sub = new float[endIndex - beginIndex + 1];
		for(int i = 0, k = beginIndex;k <= endIndex;i++,k++){
			sub[i] = array[k];
		}
		return sub;
	}
	
	public static float[] normalizeArray(float[] array, float[] interval){
		float max = Matrix.maxValue(array);
		float min = Matrix.minValue(array);
		for(int i = 0;i < array.length;i++){
			array[i] = (array[i] - min) * (interval[1] - interval[0]) / (max - min) + interval[0];
		}
		return array.clone();
	}
	
	public static float[] norToPredefinedSum(float[] array, float predSum){
		float[] normalizedArray = new float[array.length];
		float sumOfArray = sum(array);
		if(sumOfArray == 0f){
			sumOfArray = 0.1f * array.length;
			for(int i = 0;i < array.length;i++){
				normalizedArray[i] = (array[i] + 0.1f) / sumOfArray * predSum;
			}
		}else{
			for(int i = 0;i < normalizedArray.length;i++){
				normalizedArray[i] = array[i] / sumOfArray * predSum;
			}
		}
		return normalizedArray;
	}
	
	public static float[][] norEachLineToPredefinedSum(float[][] matrix, float predSum){
		float[][] normalizedMat = new float[matrix.length][matrix[0].length];
		for(int i = 0;i < matrix.length;i++){
			float sumOfLine = sum(matrix[i]);
			if(sumOfLine == 0f){
				sumOfLine = 0.1f * matrix[i].length;
				for(int k = 0;k < matrix[i].length;k++){
					normalizedMat[i][k] = (matrix[i][k] + 0.1f) / sumOfLine * predSum;
				}
			}else{
				for(int k = 0;k < matrix[i].length;k++){
					normalizedMat[i][k] = matrix[i][k] / sumOfLine * predSum;
				}
			}
		}
		return normalizedMat;
	}
	
	public static void writeMatrix(float[][] matrix,String filename,DecimalFormat df) 
					throws IOException{
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(int i = 0;i < matrix.length;i++){
			for(int k = 0;k < matrix[i].length;k++){
				br.write(df.format(matrix[i][k]) + "\t");
			}
			br.write("\r\n");
		}
		br.close();
	}
	
	public static void writeMatrix(String[][] matrix,String filename) 
			throws IOException{
		
		BufferedWriter br = new BufferedWriter(
			new OutputStreamWriter(
					new FileOutputStream(filename)));
		for(int i = 0;i < matrix.length;i++){
			for(int k = 0;k < matrix[i].length;k++){
				br.write(matrix[i][k] + "\t");
			}
			br.write("\r\n");
		}
		br.close();
	}
	
	
	public static void writeArray(float[] array,String filename,DecimalFormat df) 
				throws IOException{
		BufferedWriter br = null;
		try {
		br = new BufferedWriter(
				new OutputStreamWriter(
					new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(int i = 0;i < array.length;i++){
			br.write(df.format(array[i]) + "\t");
		}
		br.close();
	}
	
	public static void writeArray(int[] array,String filename) 
	throws IOException{

		BufferedWriter br = null;
		try {
			br = new BufferedWriter(
					new OutputStreamWriter(	
							new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(int i = 0;i < array.length;i++){
			br.write(array[i] + "\t");
		}
		br.close();
	}
	
	public static void writeMatrix(int[][] matrix,String filename) 
	throws IOException{
		
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(
			new OutputStreamWriter(
					new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(int i = 0;i < matrix.length;i++){
			for(int k = 0;k < matrix[i].length;k++){
				br.write(matrix[i][k] + "\t");
			}
			br.write("\r\n");
		}
		br.close();
	}
	
	public static float[][] loadMatrix(String filename,String token) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<float[]> inputsRead = new ArrayList<float[]>(0);
		String line = reader.readLine();
		int count = 0;
		while(line != null){
			StringTokenizer tokenizer = new StringTokenizer( line,token );
			float[] temp = new float[tokenizer.countTokens()];
			for(int i = 0;i < temp.length;i++){
				temp [i]= Float.valueOf(tokenizer.nextToken());
			}
			count++;
			inputsRead.ensureCapacity(count);
			inputsRead.add(temp);
			line = reader.readLine();
		}			
		reader.close();
		float[][] mat = new float[count][];
		
		for(int i = 0;i < mat.length;i++){
			mat[i] = inputsRead.get(i).clone();
		}
		return mat;
	}
	
	public static int[][] loadIntMatrix(String filename,String token) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<int[]> inputsRead = new ArrayList<int[]>(0);
		String line = reader.readLine();
		int count = 0;
		while(line != null){
			StringTokenizer tokenizer = new StringTokenizer( line,token );
			int[] temp = new int[tokenizer.countTokens()];
			for(int i = 0;i < temp.length;i++){
				temp [i]= Integer.valueOf(tokenizer.nextToken());
			}
			count++;
			inputsRead.ensureCapacity(count);
			inputsRead.add(temp);
			line = reader.readLine();
		}			
		reader.close();
		int[][] mat = new int[count][];
		
		for(int i = 0;i < mat.length;i++){
			mat[i] = inputsRead.get(i).clone();
		}
		return mat;
	}
	
	public static ArrayList<int[]> loadArrays(String filename,String token) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		ArrayList<int[]> inputsRead = new ArrayList<int[]>(0);
		String line = reader.readLine();
		int count = 0;
		while(line != null){
			StringTokenizer tokenizer = new StringTokenizer( line,token );
			int[] temp = new int[tokenizer.countTokens()];
			for(int i = 0;i < temp.length;i++){
				temp [i]= Integer.valueOf(tokenizer.nextToken());
			}
			count++;
			inputsRead.ensureCapacity(count);
			inputsRead.add(temp);
			line = reader.readLine();
		}			
		reader.close();
		return inputsRead;
	}
	
	public static float[] loadArray(String filename,String token) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		String line = reader.readLine();
		StringTokenizer tokenizer = new StringTokenizer( line,token );
		float[] temp = new float[tokenizer.countTokens()];
		for(int i = 0;i < temp.length;i++){
			temp[i] =  Float.valueOf(tokenizer.nextToken());
		}
			
		reader.close();
		return temp;
	}
	
	public static int[] loadIntegerArray(String filename,String token) throws IOException{
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(new File(filename)));
		String line = reader.readLine();
		StringTokenizer tokenizer = new StringTokenizer( line,token );
		int[] temp = new int[tokenizer.countTokens()];
		for(int i = 0;i < temp.length;i++){
			temp[i] =  Integer.valueOf(tokenizer.nextToken());
		}
			
		reader.close();
		return temp;
	}
	
	public static float[][] deleteColumn(float[][] matrix, int[] index){
		float[][] newMat = new float[matrix.length][matrix[0].length - index.length];
		for(int i = 0;i < matrix.length;i++){
			int count = 0;
			for(int k = 0;k < matrix[i].length;k++){
				if(Matrix.findIndex(k, index) >= 0)continue;
				newMat[i][count++] = matrix[i][k];
			}
		}
		return newMat;
	}
	
	
	public static int[] disorder(float[] array){
		Random ran = new Random();
		int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < index.length;i++){
			int temp = ran.nextInt(index.length);
			int tempIndex = index[temp];
			float tempValue = array[temp];
			index[temp] = index[i];
			array[temp] = array[i];
			index[i] = tempIndex;
			array[i] = tempValue;
		}
		return index;
	}
	
	public static int[] disorder(float[] array,int seed){
		Random ran = new Random(seed);
		int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < index.length;i++){
			int temp = ran.nextInt(index.length);
			int tempIndex = index[temp];
			float tempValue = array[temp];
			index[temp] = index[i];
			array[temp] = array[i];
			index[i] = tempIndex;
			array[i] = tempValue;
		}
		return index;
	}
	
	public static int[] disorder(int[] array){
		Random ran = new Random();
		int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < index.length;i++){
			int temp = ran.nextInt(index.length);
			int tempIndex = index[temp];
			int tempValue = array[temp];
			index[temp] = index[i];
			array[temp] = array[i];
			index[i] = tempIndex;
			array[i] = tempValue;
		}
		return index;
	}
	
	public static int[] disorder(int[] array,int seed){
		Random ran = new Random(seed);
		int[] index = new int[array.length];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < index.length;i++){
			int temp = ran.nextInt(index.length);
			int tempIndex = index[temp];
			int tempValue = array[temp];
			index[temp] = index[i];
			array[temp] = array[i];
			index[i] = tempIndex;
			array[i] = tempValue;
		}
		return index;
	}
	
	
	public static int[] getRandomIndex(int num){
		Random ran = new Random();
		int[] index = new int[num];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < index.length;i++){
			int temp = ran.nextInt(index.length);
			int tempIndex = index[temp];
			index[temp] = index[i];
			index[i] = tempIndex;
		}
		return index;
	}
	
	public static int[] getRandomIndex(int num,int seed){
		Random ran = new Random(seed);
		int[] index = new int[num];
		for(int i = 0;i < index.length;i++){
			index[i] = i;
		}
		for(int i = 0;i < index.length;i++){
			int temp = ran.nextInt(index.length);
			int tempIndex = index[temp];
			index[temp] = index[i];
			index[i] = tempIndex;
		}
		return index;
	}
	
	public static int findNumOfDiff(int[] array){
		int[] arr = array.clone();
		Matrix.QuickSort(arr);
		int num = 1;
		for(int i = 1;i < arr.length;i++){
			if(arr[i] != arr[i-1]){
				num++;
			}
		}
		return num;
	}
	
	public static float calculateAUC_NorOutputs(float[][] matrix){
		float[][] outputs = new float[matrix.length][matrix[0].length - 1];
		int[] labels = new int[matrix.length];
		for(int i = 0;i < labels.length;i++){
			for(int k = 0;k < matrix[i].length - 1;k++){
				outputs[i][k] = matrix[i][k];
			}
			labels[i] = (int)matrix[i][matrix[i].length - 1];
		}
		int numClass = findNumOfDiff(labels);
		
		float[][] targets = new float[matrix.length][numClass];
		for(int i = 0;i < targets.length;i++){
			targets[i][labels[i]] = 1f;
		}
		float[][] norOutputs = Matrix.norEachLineToPredefinedSum(outputs, 1f);
		return calculateAUC(norOutputs,targets);
	}
	
	public static float calculateAUC(float[][] matrix){
		float[][] outputs = new float[matrix.length][matrix[0].length - 1];
		int[] labels = new int[matrix.length];
		for(int i = 0;i < labels.length;i++){
			for(int k = 0;k < matrix[i].length - 1;k++){
				outputs[i][k] = matrix[i][k];
			}
			labels[i] = (int)matrix[i][matrix[i].length - 1];
		}
		int numClass = findNumOfDiff(labels);
		
		float[][] targets = new float[matrix.length][numClass];
		for(int i = 0;i < targets.length;i++){
			targets[i][labels[i]] = 1f;
		}
		
		/*Matrix.print(labels);
		Matrix.print(outputs);
		Matrix.print(targets);*/
		
		return calculateAUC(outputs,targets);
	}
	
	public static float calculateAUC(float[][] outputs,float[][] targets){	
		//float[][] norOutputs = Matrix.norEachLineToPredefinedSum(outputs, 1f);
		int numClass = targets[0].length;
		float auc = 0;
		for(int i = 0;i < numClass;i++){
			for(int k = i + 1;k < numClass;k++){
				int count = 0;
				for(int d = 0;d < targets.length;d++){
					if(targets[d][i] == 1 || targets[d][k] == 1)count++;
				}
				float[] score_i = new float[count];
				float[] score_k = new float[count];
				float[] targetsLabel_i = new float[count];
				float[] targetsLabel_k = new float[count];
				int index = 0;
				for(int d = 0;d < targets.length;d++){
					if(targets[d][i] == 1 || targets[d][k] == 1){
						score_i[index] = outputs[d][i];
						score_k[index] = outputs[d][k];
						targetsLabel_i[index] = targets[d][i];
						targetsLabel_k[index] = targets[d][k];
						index++;
					}
				}
				
				
				/*if(i == 0 && k == 2){
					System.out.println();
					System.out.println();
					Matrix.print(score_i);
					//System.out.println();
					Matrix.print(targetsLabel_i);
					System.out.println();
					System.out.println();
				}*/
				
				
				auc += calculateAUC(score_i,targetsLabel_i) + calculateAUC(score_k,targetsLabel_k);
				
				
				
				//System.out.print(calculateAUC(score_i,targetsLabel_i) + " " + calculateAUC(score_k,targetsLabel_k) + " ");
			}
			//System.out.println();
		}
		auc = auc / (1f * numClass * (numClass - 1));
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
