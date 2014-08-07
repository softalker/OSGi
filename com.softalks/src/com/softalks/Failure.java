package com.softalks;

@SuppressWarnings("serial")
public class Failure extends Exception {

	public Failure(NumberFormatException e) {
		super(e);
	}

	public Failure() {
	}
}
