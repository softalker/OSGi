package com.softalks;

public abstract class Constraint<T> implements Semantic {
	
	protected abstract void parsed(Input context, T result) throws Failure;
	
	protected void start(Input context) {
		
	}
	
	protected void stop(Input context) {
		
	}
	
	protected void failure(Input context) {
		
	}

	@Override
	public Pattern<?> $(final Object rule) {
		return new Pattern<Object>() {
			Pattern<?> parser = Patterns.get(rule);
			@SuppressWarnings("unchecked")
			@Override
			public Object parse(Input input) throws Failure {
				Constraint<T> semantic = Constraint.this;
				semantic.start(input);
				try {
					Object result = input.parse(parser);
					semantic.parsed(input, (T)result);
					return result;
				} catch (Failure e) {
					semantic.failure(input);
					throw e;
				} finally {
					semantic.stop(input);
				}
			}
			@Override
			public String toString() {
				return parser.toString();
			}
		};
	}
}