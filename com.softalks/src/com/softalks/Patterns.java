package com.softalks;

import static org.junit.Assert.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.softalks.Input.Mark;

public abstract class Patterns {

	static Object readOld(Object object, String source) throws Failure {
		return new Input(source).parse(object);
	}

	static <T> T readOld(Class<T> type, String source) throws Failure {
		return new Input(source).parse(type);
	}

	static <T> T readOld(Pattern<T> pattern, String source) throws Failure {
		return new Input(source).parse(pattern);
	}

	public static <T> Pattern<T> pattern(Class<T> type) {
		@SuppressWarnings("unchecked")
		Pattern<T> pattern = (Pattern<T>)get((Object)type);
		return pattern;
	}
	
	public static Pattern<String> pattern(String string) {
		@SuppressWarnings("unchecked")
		Pattern<String> pattern = (Pattern<String>)get((Object)string);
		return pattern;
	}

	public static Pattern<Character> pattern(Character character) {
		@SuppressWarnings("unchecked")
		Pattern<Character> pattern = (Pattern<Character>)get((Object)character);
		return pattern;
	}
	
	public static Pattern<Integer> pattern(Integer integer) {
		@SuppressWarnings("unchecked")
		Pattern<Integer> pattern = (Pattern<Integer>)get((Object)integer);
		return pattern;
	}
	
	static Pattern<?> get(Object object) {
		Pattern<?> pattern = null;
		if (object instanceof Pattern) {
			pattern = (Pattern<?>)object;
		} else if (object instanceof Class) {
			pattern = patterns.get(object);
			if (pattern == null) {
				Class<?> type = (Class<?>)object;
				try {
					pattern = (Pattern<?>)type.getField("pattern").get(null);
					patterns.put(type, pattern);
				} catch (NoSuchFieldException e) {
					throw new IllegalArgumentException("No pattern registered for the class " + type.getCanonicalName() +  " and it has not the attribute [public static Pattern<" + type.getSimpleName() + "> pattern]");
				} catch (IllegalAccessException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
		} else if (object instanceof Character || object instanceof String || object instanceof Integer) {
			pattern = terminals.get(object);
			if (pattern == null) {
				if (object instanceof Character) {
					pattern = newPattern((Character)object);
				} else if (object instanceof String) {
					pattern = newPattern((String)object);
				} else if (object instanceof Integer) {
					pattern = newPattern((Integer)object);
				}
				terminals.put(object, pattern);
			}
		}
		if (pattern == null) {
			throw new IllegalArgumentException("The object " + object + " from class " + object.getClass().getCanonicalName() + " is not parseable");
		}
		return pattern;
	}
	
	static class AbstractParser<T> extends Pattern<T> {

		List<Pattern<? extends T>> implementations = new ArrayList<>();
		
		public AbstractParser(Pattern<? extends T> pattern) {
			implementations.add(pattern);
		}
		
		void add(Pattern<? extends T> pattern) {
			implementations.add(pattern);
		}
		
		@Override
		public T parse(Input input) throws Failure {
			for (Pattern<? extends T> pattern : implementations) {
				Mark mark = input.new Mark();
				try {
					return input.parse(pattern);
				} catch (Failure e) {
					mark.reset();
				}
			}
			throw new Failure();
		}
	}
	
	/**
	 * Registers a pattern for a rule. It the rule is not registered, this method registers the rule too
	 * @param rule the class that will be able to be parsed
	 * @param pattern the object that is able to parse the rule
	 */
	@SuppressWarnings("unchecked")
	public static <T> void register(Class<T> rule, Pattern<? extends T> pattern) {
		Pattern<?> current = patterns.get(rule);
		if (rule.isInterface() || Modifier.isAbstract(rule.getModifiers())) {
			if (current != null) {
				
				AbstractParser.class.cast(current).add(pattern);
			} else {
				patterns.put(rule, new AbstractParser<T>(pattern));
			}
		} else {
			if (current == null) {
				throw new UnsupportedOperationException("Concrete rules extension is not implemented yet");
			} else {
				patterns.put(rule, pattern);
			}
		}
	}
	
	public static void unregister(Class<?> type) {
		patterns.remove(type); //TODO Control abstract types
	}

	public static final Transformation<Object,String> $string = new Transformation<Object,String>() {
		@Override
		protected String success(Input context, Object result) throws Failure {
			if (result == null) {
				return "";
			} else if (result instanceof String) {
				return (String)result;
			} else if (result instanceof List) {
				List<?> list = List.class.cast(result);
				StringBuilder builder = new StringBuilder();
				for (Object item : list) {
					builder.append(success(context, item));
				}
				return builder.toString();
			} else {
				return result.toString();
			}
		};
	};

	public static final Pattern<Object> delimiter = new Pattern<Object>() {
		@Override
		public Object parse(Input input) throws Failure {
			try {
				input.parse(and(Character.class));
			} catch (Failure e) {
				return null;
			}
			return input.parse(choice(input.delimiters));
		}
		@Override
		public String toString() {
			return "Delimiter";
		};
	};	
	
	private static final Pattern<Character> character = new Pattern<Character>() {
		@Override
		public Character parse(Input input) throws Failure {
			Character character = input.read();
			if (character == null) {
				throw new Failure();
			} else {
				return character;
			}
		}
	};
	
	private static final Pattern<String> string = new Pattern<String>() {
		
		private Rule character = sequence(not(delimiter), Patterns.character);
		private Rule string	   = string(zeroOrMore(character));
		
		@Override
		public String parse(Input input) throws Failure {
			if (input.delimited) {
				return (String)input.parse(string);
			} else {
				throw new IllegalStateException("It is not possible to parse a String if the input is not delimited");
			}
		}
	};
		
	private static final Pattern<Integer> integer = new Pattern<Integer>() {
		@Override
		public Integer parse(Input input) throws Failure {
			if (input.delimited) {
				return parse((String)input.parse(string));
			} else {
				StringBuilder characters = new StringBuilder();
				Integer integer = null;
				Mark mark = null;
				try {
					while (true) {
						mark = input.new Mark();
						characters.append(input.parse(character));
						integer = parse(characters.toString());
					}
				} catch (Failure failure) {
					if (integer == null) {
						throw failure;
					} else {
						mark.reset();
						return integer;
					}
				}
			}
		}	
		@Override
		public Integer parse(String string) throws Failure {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException e) {
				throw new Failure(e);
			}
		}	
	};

	@SuppressWarnings("serial")
	private static Map<Class<?>, Pattern<?>> patterns = new HashMap<Class<?>, Pattern<?>>() {{
		put(Character.class, character);
		put(   String.class, string);
		put(  Integer.class, integer);
	}};
	
	private static Map<Object, Pattern<?>> terminals = new WeakHashMap<>();
	
	private static Pattern<String> newPattern(final String string) {
		return new Pattern<String>() {
			@Override
			public String parse(Input input) throws Failure {
				for (char expected : string.toCharArray()) {
					input.parse(expected);
				}
				if (input.delimited) input.parse(and(delimiter));
				return string;
			}
			@Override
			public String toString() {
				return "\"" + string + "\"";
			};
		};
	}
	
	private static Pattern<Integer> newPattern(final Integer integer) {
		return new Pattern<Integer>() {
			@Override
			public Integer parse(Input input) throws Failure {
				input.parse(integer.toString());
				return integer;
			}
			@Override
			public String toString() {
				return integer.toString();
			};
		};
	}
	
	private static Pattern<Character> newPattern(final Character expected) {
		return new Pattern<Character>() {
			@Override
			public Character parse(Input input) throws Failure {
				Character actual = input.parse(character);
				if (expected.equals(actual)) {
					return expected;
				} else {
					throw new Failure();
				}
			}
			@Override
			public String toString() {
				return "'" + expected + "'";
			};
		};
	}

	/*
	 * This method is necessary for the toString method of the operators
	 * It also serves to allow using collections as the unique argument of a choice
	 * Actually it expands all the collections from the expression and this could be confusing
	 */
 	static List<Pattern<?>> patterns(Object... expression) {
		List<Pattern<?>> expressions = new ArrayList<>();
		for (Object object : expression) {
			if (object instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<Object> collection = Collection.class.cast(object);
				for (Object item : collection) {
					expressions.add(get(item));
				}
			} else {
				expressions.add(get(object));
			}
		}
		if (expressions.size() == 0) {
			throw new IllegalArgumentException("At least one expression were expected");
		}
		return expressions;
	}

 	//Shared code for the toString of sequences and choices
	static String expression(String separator, List<Pattern<?>> expressions) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Object object : expressions) {
			if (first) {
				first = false;
			} else {
				builder.append(separator);
			}
			builder.append(object);
		}
		return builder.toString();
	}
	
	public static Pattern<String> string(Rule rule) {
		final Pattern<?> pattern = (Pattern<?>)rule;
		return $string.$(pattern.delimited(false));
//		return new Parser<String>() {
//			private String transform(Object result) {
//				if (result == null) {
//					return "";
//				} else if (result instanceof String) {
//					return (String)result;
//				} else if (result instanceof List) {
//					List<?> list = List.class.cast(result);
//					StringBuilder builder = new StringBuilder();
//					for (Object item : list) {
//						builder.append(transform(item));
//					}
//					return builder.toString();
//				} else {
//					return result.toString();
//				}
//			}
//			private String read(Input input) throws Failure {
//				return transform(input.read(pattern));
//			}
//			@Override
//			public String parse(Input input) throws Failure {
//				if (input.delimited) {
//					try {
//						input.delimited = false;
//						String result = read(input);
//						return result;
//					} finally {
//						input.delimited = true;
//					}
//				} else {
//					return read(input);
//				}
//			}
//			@Override
//			public String toString() {
//				return "string(" + pattern + ")";
//			} 
//		};
	}
	
	public static Sequence sequence(final Object... expression) {
		return new Sequence(expression);
	}
	
	public static Choice choice(final Object... expression) {
		return new Choice(expression);
	}
	
	public static Occurrences occurrences(Integer minimum, Integer maximum, Object expression) {
		return new Occurrences(minimum, maximum, expression);
	}
	
	public static Occurrences occurs(Integer constant, Object expression) {
		return new Occurrences(constant, constant, expression);
	}
	
	public static Occurrences oneOrMore(Object object) {
		return occurrences(1, null, object);
	}

	public static Occurrences zeroOrMore(Object object) {
		return occurrences(0, null, object);
	}
	
	public static Optional optional(Object expression) {
		return new Optional(expression);
	}
	
	public static Pattern<Character> range(final char minimum, final char maximum) {
		return new Pattern<Character>() {
			@Override
			public Character parse(Input input) throws Failure {
				char actual = input.parse(Character.class);
				if (actual < minimum || actual > maximum) {
					throw new Failure();
				} else {
					return actual;
				}
			}
			@Override
			public String toString() {
				return "[" + minimum + "-" + maximum + "]";
			}
		};
	}
	
	public static Pattern<Integer> range(final int minimum, final int maximum) {
		return new Pattern<Integer>() {
			@Override
			public Integer parse(Input input) throws Failure {
				int actual = input.parse(Integer.class);
				if (actual < minimum || actual > maximum) {
					throw new Failure();
				} else {
					return actual;
				}
			}
			@Override
			public String toString() {
				return "[" + minimum + "-" + maximum + "]";
			}
		};
	}

	public static Rule not(final Object object) {
		return new Not(object);
	}

	public static And and(final Object object) {
		return new And(object);
	}	
	
	public static <T> Pattern<T> lookAhead(final Pattern<T> pattern) {
		return new Pattern<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T parse(Input source) throws Failure {
				return (T)source.parse(and(pattern));
			}
		};
	}	
	
	public static <T> Pattern<T> lookAhead(final Class<T> type) {
		return new Pattern<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T parse(Input source) throws Failure {
				return (T)source.parse(and(type));
			}
		};
	}	
	
	@FixMethodOrder(MethodSorters.NAME_ASCENDING)
	public static class $Parsers {
		
		@Test
		public void a1_character() throws Failure {
			Input input = new Input("abc");
			assertEquals((Character)'a', character.parse(input));
			assertEquals((Character)'b', character.parse(input));
			assertEquals((Character)'c', character.parse(input));
			try {
				input.parse(Character.class);
				fail("The input is finished but the pattern don't mind");
			} catch (Failure e) {}
			assertEquals((Character)'a', readOld(Character.class, "a"));
		}
		
		@Test
		public void b1_string() throws Failure {
			Input input = new Input("a");
			assertEquals("a", string.parse(input));
			assertEquals("b", readOld(String.class, "b"));
		}
		
		@Test 
		public void c1_integerAsParserInDelimitedMode() throws Failure {
			assertEquals((Integer)345, integer.parse("345"));
		}
		
		@Test 
		public void c1_andInUndelimitedMode() throws Failure {
			Input input = new Input("3)");
			input.delimited = false;
			Integer value = integer.parse(input);
			assertEquals((Integer)3, value);
			assertEquals(1, input.position());
		}
				
		@Test 
		public void c2_asClass() throws Failure {
			Integer integer = readOld(Integer.class, "345");
			assertEquals((Integer)345, integer);
		}
		
		@Test 
		public void c3_asClassWithInput() throws Failure {
			Input input = new Input("345");
			assertEquals((Integer)345, input.parse(Integer.class));
		}
		
	}
}