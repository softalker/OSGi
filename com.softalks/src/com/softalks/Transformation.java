package com.softalks;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class Transformation<SYNTACTIC, SEMANTIC> implements Semantic {
	
	protected void start(Input input) {
	}
	
	protected abstract SEMANTIC success(Input input, SYNTACTIC result) throws Failure;
	
	protected SEMANTIC failure(Input input, Failure e) throws Failure {
		throw e;
	}

	protected void stop(Input input) {
	}
	
	public Pattern<SEMANTIC> $(final Object expression) {
		return new Pattern<SEMANTIC>() {
			@SuppressWarnings("unchecked")
			@Override
			public SEMANTIC parse(Input input) throws Failure {
				Transformation<SYNTACTIC,SEMANTIC> action = Transformation.this;
				action.start(input);
				try {
					Object result = input.parse(expression);
					return action.success(input, (SYNTACTIC)result);
				} catch (Failure e) {
					return action.failure(input, e);
				} finally {
					action.stop(input);
				}
			}
			@Override
			public String toString() {
				Type type = ((ParameterizedType)Transformation.this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
				return ((Class<?>)type).getSimpleName();
			}

		};
	}
}