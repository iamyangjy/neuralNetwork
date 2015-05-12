package com.ruijie.ncl;

public abstract class CombinationScheme {
	String type;
	public abstract float[] combineOutputs(float[][] ensembleOutputs);
	
	public String toString(){
		return type;
	}
	
	public static CombinationScheme createCombinationScheme(String s){
		//maj和wta这两个比较少见，一般还是用平均的

		//avg:平均值，比较常见
		if(s.equals("avg")){
			return new AVG();
		}

		//maj：majority，也是只在分类模型中使用，对各个神经网络的分类决策进行投票，选取得票最多的那个类别作为最终决策
		if(s.equals("maj")){
			return new MAJ();
		}

		//wta: winner-takes-all，也就是以输出最大的那个node为主，一般只在分类模型中使用。
		if(s.equals("wta")){
			return new WTA();
		}
		return null;
	}
}
