package com.softalks;


public abstract class Action implements Semantic {

	private final String name;
	
	public Action(String name) {
		this.name = name;
	}
	
	public Action() {
		this("Unknown action");
	}
	
	protected void start(Input input) {
	}
	
	protected void success(Input input) {
	}
	
	protected void failure(Input input) {
	}
	
	protected void stop(Input input) {
	}

	@Override
	public Pattern<?> $(Object rule) {
		final Pattern<?> parser = Patterns.get(rule);
		return new Pattern<Object>() {
			@Override
			public Object parse(Input input) throws Failure {
				Action action = Action.this;
				action.start(input);
				try {
					Object result = input.parse(parser);
					action.success(input);
					return result;
				} catch (Failure e) {
					action.failure(input);
					throw e;
				} finally {
					action.stop(input);
				}
			}
			@Override
			public String toString() {
				return Action.this.name;
			}
		};
	}
}