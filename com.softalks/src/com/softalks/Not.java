package com.softalks;

import com.softalks.Input.Mark;

public class Not extends Pattern<Object> {

	private final Rule expression;
	
	Not(Object expression) {
		this.expression = Patterns.get(expression);
	}
	
	@Override
	public Object parse(Input input) throws Failure {
		Mark mark = input.new Mark();
		try {
			input.parse(expression);
		} catch (Failure e) {
			return null;
		} finally {
			mark.reset();
		}
		throw new Failure();
	}
	
	@Override
	public String toString() {
		return "!(" + expression + ")";
	}

}
