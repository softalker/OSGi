package com.softalks;

import static com.softalks.Patterns.*;

import java.util.List;

import com.softalks.Input.Delimiters;

public abstract class Delimitable extends Pattern<List<?>> {

	
	protected static class Delimiter {

		boolean exclusive = false;
		
		boolean mandatory = false;
		
		Pattern<?> pattern = null;

		private final boolean finalizer;
		
		public Delimiter(boolean finalizer) {
			this.finalizer = finalizer;
		}

		void pattern(Object... pattern) {
			List<Pattern<?>> patterns = patterns(pattern);
			if (patterns.size() == 1) {
				this.pattern = patterns.get(0);
			} else {
				this.pattern = choice(patterns);
			}
		}
		
		public void read(Input input, List<Object> output) throws Failure {
			Object value;
			if (!input.delimited) {
				return;
			}
			try {
				if (pattern == null) {
					value = input.parse(input.spaces);
				} else {
					Pattern<?> spaces = optional(input.spaces);
					Pattern<?> both = finalizer? sequence(spaces, pattern) : sequence(spaces, pattern, spaces);
					both.delimited(false);
					if (mandatory) {
						if (exclusive) {
							value = input.parse(pattern);
						} else {
							value = input.parse(both);
						}
					} else {
						value = input.parse(choice(both, input.spaces));	
					}
				}
				if (input.option(Delimiters.INCLUDED)) {
					output.add(value);
				}	
			} catch (Failure exception) {
				if (mandatory) {
					throw exception;	
				}
			}
		}		
	}
	
	protected Delimiter separation	= new Delimiter(false);
	protected Delimiter finish		= new Delimiter(true);
		
	protected void delimite(Input input) {
		if (separation.pattern != null) {
			input.delimiters.add(separation.pattern);
		}
		if (finish.pattern != null) {
			input.delimiters.add(finish.pattern);
		}
	}

	protected void undelimite(Input input) {
		if (separation.pattern != null) {
			input.delimiters.remove(separation.pattern);
		}
		if (finish.pattern != null) {
			input.delimiters.remove(finish.pattern);		
		}
	}

	public Delimitable separableBy(Object... pattern) {
		separation.pattern(pattern);
		return this;
	}

	protected Delimitable separatedBy(Object... pattern) {
		separation.pattern(pattern);
		separation.mandatory = true;
		return this;
	}
	
	protected Delimitable separatedOnlyBy(Object... pattern) {
		separation.pattern(pattern);
		separation.mandatory = true;
		separation.exclusive = true;
		return this;
	}
	
	public Delimitable finishableBy(Object pattern) {
		finish.pattern(pattern);
		return this;
	}

	public Delimitable finishedBy(Object... pattern) {
		finish.pattern(pattern);
		finish.mandatory = true;
		return this;
	}
	
	protected Delimitable finishedOnlyBy(Object pattern) {
		finish.pattern(pattern);
		finish.mandatory = true;
		finish.exclusive = true;
		return this;
	}
}