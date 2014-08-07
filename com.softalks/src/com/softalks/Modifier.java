package com.softalks;

public abstract class Modifier<T> extends Transformation<T, T> implements Semantic {
	
	@Override
	protected T success(Input input, T result) throws Failure {
		return result;
	}
	
}