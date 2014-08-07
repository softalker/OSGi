package com.softalks;

import static com.softalks.Patterns.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.softalks.Input.Mark;

public class And extends Pattern<Object> {

	private final Pattern<?> expression;
	
	public And(Object expression) {
		this.expression = get(expression);
	}
	
	@Override
	public Object parse(Input input) throws Failure {
		Mark mark = input.new Mark();
		try {
			return input.parse(expression);
		} finally {
			mark.reset();
		}
	}
	
	@Override
	public String toString() {
		return "&(" + expression + ")";
	}
	
	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class _And {
		
		@Test
		public void a1_shouldBeAbleToMatchARuleWithoutConsumingAnyInput() throws Failure {
			Input input = new Input("778");
			input.parse('7');
			input.parse('7');
			input.parse(and('8'));
			input.parse('8');
		}
		
		@Test
		public void a2_shouldBeAbleToResetWhenTheInnerRuleResetToo() throws Failure {
			Input input = new Input("778");
			input.parse('7');
			input.parse('7');
			try {
				input.parse(and(choice('9','8')));
			} catch (Failure e) {
				input.parse('8');	
			}
		}

	}
}