package com.bisoft.model;

import com.bisoft.navi.common.exceptions.GetTitleObjectException;
import com.bisoft.navi.common.interfaces.IModelObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FileModelObject implements IModelObject {
	
	private final String name;
	private final Iterable<String> title;
	
	public FileModelObject(String name, Iterable<String> title) {
		this.name = name;
		this.title = title;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public List<String> title() throws GetTitleObjectException {
		return StreamSupport
				.stream(title.spliterator(), false)
				.collect(Collectors.toList());
	}
	
	@Override
	public Iterable<List<String>> body() {
		return new ArrayList<>();
	}
}
