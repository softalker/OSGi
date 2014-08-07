package com.softalks;

import static com.softalks.Patterns.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class Sequence extends Delimitable {
	
	private final List<Pattern<?>> patterns;	
	
	public Sequence(Object... pattern) {
		patterns = patterns(pattern);
	}
		
	@Override		
	public List<?> parse(Input input) throws Failure {
		List<Object> output = new ArrayList<>();
		try {
			delimite(input);
			for (int i = 0; i < patterns.size(); i++) {
				if (i != 0) {
					separation.read(input, output);	
				}
				output.add(input.parse(patterns.get(i)));
			}
			finish.read(input, output);
			return output;
		} finally {
			undelimite(input);
		}
	}
	
	@Override
	public String toString() {
		return "{" + expression(" ", patterns) + "}";
	}
	
	static interface Clause {
		
	}
	
	static class Picture implements Clause {
		public static final Pattern<Picture> parser = new Pattern<Picture>() {
			@Override
			public Picture parse(Input input) throws Failure {
				return null;
			}
		};
	}
	
	public static class _Sequence {
		@Test
		public void a1_basicSuccess() throws Failure {
			
			Logger.getLogger("com.softalks").setLevel(Level.INFO);
			Logger.getLogger("com.softalks.Parsers$3").setLevel(Level.OFF);
			Logger.getLogger("com.softalks").setLevel(Level.OFF);
			
			Pattern<List<?>> sequence;
			List<?> list;
			
			sequence = sequence('a','b').separableBy(',',';').finishedBy('.');
			list = readOld(sequence, "a , b.");
			assertEquals(2, list.size());
			assertEquals((Character)'a', list.get(0));
			assertEquals((Character)'b', list.get(1));
			
			sequence = sequence(Integer.class, 'a','b').separableBy(',',';').finishedBy('.');
			list = readOld(sequence, "1 a , b.");
			assertEquals(3, list.size());
			assertEquals((Integer)1, list.get(0));
			assertEquals((Character)'a', list.get(1));
			assertEquals((Character)'b', list.get(2));

			Pattern<String> name = $string.$(sequence(not(Integer.class), String.class));
			String output = readOld(name, "My-Name");
			assertEquals("My-Name", output);
			
			Pattern<List<?>> dataDescriptionEntry = sequence(choice(range(1, 49), 77), optional(name), optional("clauses")).finishedBy('.');
			
			list = Patterns.readOld(dataDescriptionEntry, "1 MY-NAME.");
			assertEquals(3, list.size());
			assertEquals((Integer)1, list.get(0));
			assertEquals("MY-NAME", list.get(1));
			assertNull(list.get(2));
		}
	}
}