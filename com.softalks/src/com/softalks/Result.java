package com.softalks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Result<T> implements Semantic {
	
	protected void start(Input input) {
		
	}
	
	protected abstract T parsed(Input input);
	
	@Override
	public Pattern<T> $(final Object rule) {
		return new Pattern<T>() {
			@Override
			public T parse(Input input) throws Failure {
				Result.this.start(input);
				try {
					input.parse(rule);
					return Result.this.parsed(input);
				} catch (Failure e) {
					throw e;
				}
			}
			@Override
			public String toString() {
				Type type = ((ParameterizedType)Result.this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
				return ((Class<?>)type).getSimpleName();
			}
		};
	}	
}