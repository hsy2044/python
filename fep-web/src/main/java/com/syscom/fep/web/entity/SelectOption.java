package com.syscom.fep.web.entity;

public class SelectOption<T> {
	private String text;
	private T value;

	public SelectOption(String text, T value) {
		super();
		this.text = text;
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
