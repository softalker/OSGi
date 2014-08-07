package com.softalks;

import static com.softalks.Patterns.*;

public abstract class Pattern<T> implements Rule {

	String name = null;
	
	public Pattern(String name) {
		this.name = name;
	}
	
	public Pattern() {
		this.name = "Unknown";
	}

	public abstract T parse(Input source) throws Failure;

	public T parse(String string) throws Failure {
		return parse(new Input(string));
	}

	public void generate(T object, Output output) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public Pattern<T> next() {
		return new Pattern<T>() {
			@Override
			public T parse(Input source) throws Failure {
				source.parse(zeroOrMore(sequence(not(Pattern.this), Character.class)));
				return source.parse(Pattern.this);
			}
		};
	}
	
	private Boolean delimited = null;
	
	public Boolean delimited() {
		return delimited;
	}

	public Pattern<T> delimited(Boolean value) {
		this.delimited = value;
		return this;
	}
	
	public String toString() {
		if (this.name != null) {
			return this.name;
		}
		try {
			return this.getClass().getMethod("parse", Input.class).getReturnType().getSimpleName();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}