package com.softalks;

import static com.softalks.Patterns.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class Input {

	public Input(InputStream stream, int memory) {
		this.memory = memory;
		reader = new PushbackReader(new InputStreamReader(stream), memory);
	}

	public Input(InputStream stream) {
		this(stream, MEMORY);
	}
	
	private Input(byte[] bytes) {
		this(bytes, bytes.length > MEMORY? MEMORY : bytes.length);
	}

	private Input(byte[] bytes, int memory) {
		this(new ByteArrayInputStream(bytes), memory);
	}

	public Input(String string) {
		this(string.getBytes());
	}
	
	Input(String string, String encoding) throws UnsupportedEncodingException {
		this(string.getBytes(encoding));
	}

	private int level = -1;
	
	private String tabs(int level) {
		StringBuilder tabs = new StringBuilder();
		for (int i = 0; i < level; i++) {
			tabs.append("   ");
		}
		return (level < 10? "0" + level : level) + tabs.toString();
	}
	
	private String next() {
		logging = false;
		try {
			Rule characters = occurrences(0, 50, Character.class);
			String string = this.parse($string.$(and(characters).delimited(false)));
			string = string.replace("\r", "\\r");
			string = string.replace("\n", "\\n");
			string = string.replace("\t", "\\t");
			return string;
		} catch (Failure e) {
			return "?";
		} finally {
			logging = true;
		}
	}
	
	private boolean logging = true;
	
	private Level logLevel = Level.FINE;
	
	public <T> T parse(Pattern<T> parser) throws Failure {
		String id = parser.getClass().getName();
		Logger logger = Logger.getLogger(id);
		if (logger.isLoggable(logLevel) && logging == true) {
			logger.log(logLevel, tabs(++level) + " parsing " + parser + " on \"" + this.next() + "\""/* + " (" + id + ")"*/);
		}
		try {
			T result;
			Boolean local = parser.delimited();
			if (local != null && local != delimited) {
				boolean global = delimited;
				delimited = local;
				try {
					result = parser.parse(this);
				} finally {
					delimited = global;	
				}
			} else {
				 result = parser.parse(this);
			}
			if (logger.isLoggable(logLevel) && logging == true) {
				logger.log(logLevel, tabs(level--) + " success " + parser/* + " (" + result + ")"*/);
			}
			return result;
		} catch (Failure failure) {
			if (logger.isLoggable(logLevel) && logging == true) {
				logger.log(logLevel, tabs(level--) + " failure " + parser);
			}
			throw failure;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T parse(Class<T> type) throws Failure {
		return (T)this.parse((Object)type);
//		return read((Pattern<T>)Patterns.get((Object)type));
	}
	
	public Object parse(Object pattern) throws Failure {
		return parse(Patterns.get(pattern));
	}
	
	private final int memory;
	
	private static final int MEMORY = 512;
	
	private List<Character> readed = null;
	
	public class Mark {

		private final int beginning;
		
		Mark() {
			if (readed == null) {
				readed = new ArrayList<>();
			}
			beginning = readed.size();
		}
		
		public void reset() {
			char[] array = new char[readed.size() - beginning];
			for (int i = 0; i < array.length; i++) {
				array[i] = readed.remove(beginning);
			}
			try {
				reader.unread(array);
			} catch (IOException e) {
				String memory = String.valueOf(Input.this.memory);
				if (Input.this.memory == MEMORY) {
					memory += " (the default)";
				}
				throw new RuntimeException("The memory size of this " + Input.class.getName() + " is " + memory + ". You need " + array.length, e);
			}
			position -= array.length;
		}
		@Override
		protected void finalize() throws Throwable {
			if (readed.size() == 0) {
				readed = null;
			}
		}
	}
	
	protected Character read() {
		try {
			int integer = reader.read();
			Character character = (integer==-1)? null: (char)integer;
			if (character != null) {
				position += 1;
				if (readed != null) {
					readed.add(character);	
				}
			}
			return character;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean delimited = true;
	
	protected PushbackReader reader;
	
	private int position = 0;
	
	public int position() {
		return position;
	}
	
	private Map<Class<?>, Object> context = new HashMap<>();
	
	@SuppressWarnings("serial")
	public List<Object> delimiters = new ArrayList<Object>() {{
		add(oneOrMore(' '));
	}};

	public Pattern<?> spaces = new Pattern<String>() {
		@Override
		public String parse(Input input) throws Failure {
			return input.parse(string(oneOrMore(choice(' ','\n','\t','\r'))));
		}
		@Override
		public String toString() {
			return "Spaces";
		};
	};

	public static enum Delimiters {
		EXCLUDED, INCLUDED;
	}
	
	@SuppressWarnings("serial")
	private List<Object> options = new ArrayList<Object>() {{
		add(Delimiters.EXCLUDED); //TODO Move as default value to the constructors
	}};
	
	public boolean option(Object object) {
		if (options.contains(object)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void set(Object object) {
		context.put(object.getClass(), object);
	}
	
	public void set(Class<?> superClass, Object object) {
		try {
			object.getClass().asSubclass(superClass);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("'object' (" + object.getClass().getCanonicalName() + ") is not an instance of 'type' (" + superClass.getCanonicalName() + ")");
		}
		set(object);
		context.put(superClass, object);
	}
	
	public <T> T get(Class<T> type) {
		@SuppressWarnings("unchecked") T result = (T)context.get(type);
		return result;
	}
	
	public static class _Input {

		@Test
		public void main() throws Failure {
			Input input = new Input("123");
			Mark one = input.new Mark();
			assertEquals((Character)'1', input.read());
			assertEquals((Character)'2', input.read());
			Mark three = input.new Mark();
			assertEquals((Character)'3', input.read());
			assertEquals(3, input.position());
			three.reset();
			assertEquals((Character)'3', input.read());
			assertEquals(3, input.position());
			one.reset();
			assertEquals((Character)'1', input.read());
			assertEquals(1, input.position());
			one.reset();
			assertEquals((Character)'1', input.read());
			assertEquals(1, input.position());
		}		
	}

	public <T> T find(Class<T> type) throws Failure {
		return (T)parse(pattern(type).next());
	}
	
	public <T> T find(Pattern<T> pattern) throws Failure {
		return parse(pattern.next());
	}
}