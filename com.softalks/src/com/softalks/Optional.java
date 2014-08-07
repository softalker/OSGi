package com.softalks;

import static com.softalks.Patterns.*;
import java.util.List;

public class Optional extends Pattern<Object> {

	final Pattern<?> expression;
	
	public Optional(Object object) {
		this.expression = get(object);
	}
	
	public Object parse(Input input) throws Failure {
		List<?> list = input.parse(occurrences(0, 1, expression));
		if (list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}
	
	@Override
	public String toString() {
		return "(" + expression + ")?";
	}
	
}
