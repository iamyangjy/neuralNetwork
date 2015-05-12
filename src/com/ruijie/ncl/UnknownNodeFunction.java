package com.ruijie.ncl;

public class UnknownNodeFunction extends Throwable{
	
	private static final long serialVersionUID = 1L;

	int attemptedFunction;
	
	public UnknownNodeFunction(int function){
		super();
		attemptedFunction = function;
	}
	
	public String toString(){
		return "The node type ("+attemptedFunction+") supplied is unknown. Please see node class for list of supported node types.";
	}
}
