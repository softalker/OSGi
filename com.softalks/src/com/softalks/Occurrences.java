package com.softalks;

import static com.softalks.Patterns.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import com.softalks.Input.Mark;

public class Occurrences extends Delimitable {

	private final Rule expression;

	private final int minimum;

	private final int maximum;

	public Occurrences(Integer minimum, Integer maximum, Object object) {
		expression = get(object);
		this.minimum = minimum == null? 0 : minimum;
		this.maximum = maximum == null? Integer.MAX_VALUE : maximum;
	}
	
	@Override
	public List<?>parse(Input input) throws Failure {
		delimite(input);
		try {
			List<Object> output = new ArrayList<>();
			Mark mark = null;
			while (output.size() != maximum) {
				mark = input.new Mark();
				try {
					if (output.size() != 0) {
						separation.read(input, output);
					}
					output.add(input.parse(expression));
				} catch (Failure exception) {
					mark.reset();
					break;
				}
			}
			if (output.size() < minimum) {
				throw new Failure();
			} else {
				finish.read(input, output);
				return output;	
			}
		} finally {
			undelimite(input);
		}
	}
	@Override
	public String toString() {
		return "(" + expression + ")" + (minimum == 0? '*':'+');
	}
	
	public static class _Occurrences {
//		@Test
		public void basic() throws Failure {
			Occurrences occurrences = occurrences(0, null, '9');
			Input input = new Input("9999and more");
			List<?> result = input.parse(occurrences);
			assertEquals(4, result.size());
			assertEquals('9', result.get(0));
			assertEquals('9', result.get(1));
			assertEquals('9', result.get(2));
			assertEquals('9', result.get(3));
			input.parse('a');
			result = input.parse(occurrences);
			assertEquals(0, result.size());
			input.parse('n');
		}
		
		static interface Clause {
			
		}
		static class Redefines implements Clause {
			
		}
		
		static class DataDescriptionEntry {
			Integer level;
			String name;
			List<Clause> clauses = new ArrayList<>();
		}
		
		static class DataDivisionSection {
			List<DataItem> dataItems;
		}
		
		static abstract class DataItem {
			
		}
		
		static class ElementaryDataItem extends DataItem {
			
		}
		
//		@Test
//		public void main() throws Failure {
//			Rule level = $level.$(choice(range(1,49), 77));
//			Rule name = optional(sequence(not(Clause.class), $name.$(String.class)));
//			Rule clauses = zeroOrMore($clause.$(Clause.class)).singleton().leading(Redefines.class).separableBy(';',',').finishedBy('.');
//			Rule dataDescriptionEntry = sequence(level, name, clauses);
//			Rule dataDivisionSection = $section.$(zeroOrMore($dataDescriptionEntry.$(dataDescriptionEntry)));
//			
//			Input sourceCode = new Input("1.");
//			read(dataDivisionSection, "hola");
//			DataDivisionSection result = sourceCode.push(DataDivisionSection.class);
//			assertEquals(1, result.dataItems.size());
//		}
	}

	public Occurrences leading(Class<?> leader) {
		return this;
	}
	
	public Occurrences singleton() {
		return this;
	}
	
//	private static final Action $section = null;
//	private static final Action $clause = null;
//	private static final Action $dataDescriptionEntry = null;
//	private static final Action $level = null;
//	private static final Action $name = null;
}