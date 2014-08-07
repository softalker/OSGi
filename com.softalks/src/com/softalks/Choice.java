package com.softalks;

import static com.softalks.Patterns.*;

import java.util.List;

import org.junit.Test;

import com.softalks.Input.Mark;

public class Choice extends Pattern<Object> {

	private final List<Pattern<?>> patterns;
	
	public Choice(Object... objects) {
		patterns = patterns(objects);
	}
	
	public Choice finishedBy(Object... pattern) {
		for (Pattern<?> option : patterns) {
			((Delimitable)option).finishedBy(pattern);
		}
		return this;
	}
	
	@Override
	public Object parse(Input input) throws Failure {
		Mark mark = input.new Mark();
		for (Pattern<?> option : patterns) {
			try {
				return input.parse(option);	
			} catch (Failure e) {
				mark.reset();
			}
		}
		throw new Failure();
	}
	
	@Override
	public String toString() {
		return "(" + expression(" / ", patterns) + ")";
	}

	public static class _Choice {		
	
		@Test
		public void success() throws Failure {
			Rule level = choice(range(1,49), 77);
			readOld(level,"77");
		}
		
	}
}