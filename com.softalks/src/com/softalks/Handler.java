package com.softalks;

public abstract class Handler<T> implements Semantic {
	
	protected void start(Input input) {
	}
	
	protected void parsed(Input input, T result) {
	}
	
	protected void failure(Input input, Failure failure) {
	}
	
	protected void stop(Input input) {
	}

	@Override
	public Pattern<?> $(final Object rule) {
		return new Pattern<Object>() {
			Pattern<?> parser = Patterns.get(rule);
			@SuppressWarnings("unchecked")
			@Override
			public Object parse(Input input) throws Failure {
				Handler<T> action = Handler.this;
				action.start(input);
				try {
					Object result = input.parse(parser);
					action.parsed(input, (T)result);
					return result;
				} catch (Failure e) {
					action.failure(input, e);
					throw e;
				} finally {
					action.stop(input);
				}
			}
			@Override
			public String toString() {
				return parser.toString();
			}
		};
	}
}