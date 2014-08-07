package com.softalks;

public class Constant<T> implements Semantic {
	
	private final T object;

	public Constant(T object) {
		this.object = object;
	}
	
	@Override
	public Pattern<T> $(final Object rule) {
		return new Pattern<T>() {
			@Override
			public T parse(Input input) throws Failure {
				try {
					input.parse(rule);
					return Constant.this.object;
				} catch (Failure e) {
					throw e;
				}
			}
			@Override
			public String toString() {
				return Constant.this.object.toString();
			}
		};
	}	
}