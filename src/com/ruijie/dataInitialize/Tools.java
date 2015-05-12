package com.ruijie.dataInitialize;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public abstract class Tools {

	public static final int NUM_CUSTOMER = 0;
	public static final int DURATION = 1;
	public static final int ENTER_RATE = 2;
	public static final int OLD_NUM = 3;
	public static final int STAY_RATE = 4;

		public static void STDOUT(String str){
		String systemTimeStr = 
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println("[" + systemTimeStr + "]" + str );
	}
	
	public static void ERROUT(String str){
		String systemTimeStr = 
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.err.println("[" + systemTimeStr + "]" + str );
	}
	
	//------------------------快速排序----------------------------  
	static void quick_sort(float[] s, int l, int r){
		if (l < r){
			int i = l, j = r;
			float x = s[l];
			while (i < j){
				while(i < j && s[j] >= x) // 从右向左找第一个小于x的数
					j--;
				if(i < j)
					s[i++] = s[j];
				while(i < j && s[i] < x) // 从左向右找第一个大于等于x的数
					i++;
				if(i < j)
					s[j--] = s[i];
			}
			s[i] = x;
			quick_sort(s, l, i - 1); // 递归调用 
			quick_sort(s, i + 1, r);
		}
	}
	static void quick_sort(float[] s){
		quick_sort(s,0,s.length-1);
	}
	
	/*static void quick_sort_index(int[] s, int l, int r,int[] index){
		if (l < r){
			int i = l, j = r, x = s[l],x_index = index[l];
			while (i < j){
				while(i < j && s[j] >= x) // 从右向左找第一个小于x的数
					j--;
				if(i < j){
					s[i] = s[j];
					index[i] = index[j];
					i++;
				}
				while(i < j && s[i] < x) // 从左向右找第一个大于等于x的数
					i++;
				if(i < j){
					s[j] = s[i];
					index[j] = index[i];
					j--;
				}
			}
			s[i] = x;
			index[i] = x_index;
			quick_sort_index(s, l, i - 1,index); // 递归调用 
			quick_sort_index(s, i + 1, r,index);
		}
	}*/
	
	static void quick_sort_index(int[] s, int l, int r,int[] index){
		if (l < r){
			int i = l, j = r, x = s[index[l]],x_index = index[l];
			while (i < j){
				while(i < j && s[index[j]] >= x) // 从右向左找第一个小于x的数
					j--;
				if(i < j){
					index[i] = index[j];
					i++;
				}
				while(i < j && s[index[i]] < x) // 从左向右找第一个大于等于x的数
					i++;
				if(i < j){
					index[j] = index[i];
					j--;
				}
			}
			index[i] = x_index;
			quick_sort_index(s, l, i - 1,index); // 递归调用 
			quick_sort_index(s, i + 1, r,index);
		}
	}
	
	public static double[][] normalize(double[][] array){
		double[] max = new double[array[0].length];
		double[] min = new double[array[0].length];
		for(int i = 0;i < max.length;i++){
			max[i] = -Double.MAX_VALUE;
			min[i] = Double.MAX_VALUE;
		}
		for(int i = 0;i < array.length;i++){
			for(int k = 0;k < array[i].length;k++){
				if(array[i][k] > max[k])max[k] = array[i][k];
				if(array[i][k] < min[k])min[k] = array[i][k];
			}
		}
		double[][] norArr = new double[array.length][];
		for(int i = 0;i < norArr.length;i++){
			norArr[i] = new double[array[i].length];
			for(int k = 0;k < norArr[i].length;k++){
				norArr[i][k] = (array[i][k] - min[k]) / (max[k] - min[k]);
			}
		}
		return norArr;
	}
	
	public static void printMatrix(double[][] mat){
		for(int i = 0;i < mat.length;i++){
			for(int k = 0;k < mat[i].length;k++){
				System.out.print(mat[i][k] + "\t");
			}
			System.out.println();
		}
	}
	
	public static double[][] calculateAllCosineSimi(double[][] items){
		double[][] simi = new double[items.length][items.length];
		for(int i = 0;i < items.length;i++){
			double[] item_i = items[i];
			for(int k = 0;k < items.length;k++){
				if(k == i){
					simi[i][k] = 1;
				}
				double[] item_k = items[k];
				double i_k = 0;
				double mod_i = 0;
				double mod_k = 0;
				for(int idx = 0;idx < item_i.length;idx++){
					i_k += item_i[idx] * item_k[idx];
					mod_i += item_i[idx] * item_i[idx];
					mod_k += item_k[idx] * item_k[idx];
				}
				mod_i = Math.sqrt(mod_i);
				mod_k = Math.sqrt(mod_k);
				double mod_i_k = mod_i * mod_k;
				simi[i][k] = mod_i_k==0 ? 1 : (i_k / mod_i_k);
			}
		}
		return simi;
	}
	
	public static void main(String[] args) throws IOException {
		
		double[][] test = new double[5][2];
		System.out.println(test.length);
		System.out.println(test[0].length);
		int idx = 1;
		for(int i = 0;i < test.length;i++){
			int c = i+1;
			for(int k = 0;k < test[i].length;k++){
				test[i][k] = idx;
				idx += c;
			}
		}
		printMatrix(test);
		double[][] norTest = normalize(test);
		printMatrix(norTest);
		printMatrix(test);

		System.out.println("function quick_sort, TEST:");
		float[] f = new float[10];
		Random ranf = new Random();
		for(int i=0; i < f.length; i++){
			f[i] = ranf.nextFloat();
		}
		System.out.println();
		for(int i=0; i<f.length; i++){
			System.out.print(f[i] + ",");
		}
		quick_sort(f, 2, 6);
		System.out.println();
		for(int i=0; i<f.length; i++){
			System.out.print(f[i] + ",");
		}

		System.out.println();
		System.out.println("function quick_sort_index, TEST:");
		int[] s = new int[10];
		int[] index = new int[10];
		Random ran = new Random();
		for (int i = 0;i < s.length;i++){
			s[i] = ran.nextInt(100);
			index[i] = i;
		}
		for(int i = 0;i < s.length;i++){
			System.out.print(s[i] + ",");
		}

		int[] cloneS = s.clone();
		System.out.println();
		quick_sort_index(s, 0, s.length - 1, index);
		//quick_sort_index(s,0,s.length-1,index);
		for(int i = 0;i < s.length;i++){
			System.out.print(s[i] + ",");
		}

		System.out.println();
		for(int i = 0;i < cloneS.length;i++){
			System.out.print(cloneS[i] + ",");
		}

		System.out.println();
		for(int i = 0;i < cloneS.length;i++){
			System.out.print(cloneS[index[i]] + ",");
		}
		System.out.println();
	}
	
}
