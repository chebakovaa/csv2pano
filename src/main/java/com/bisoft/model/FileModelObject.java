package com.bisoft.model;

import com.bisoft.navi.common.exceptions.GetTitleObjectException;
import com.bisoft.navi.common.interfaces.IModelObject;
import java.util.ArrayList;
import java.util.List;

public class FileModelObject implements IModelObject {
	
	private final String name;
	private final List<String> title;
	
	public FileModelObject(String name, List<String> title) {
		this.name = name;
		this.title = title;
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public List<String> title() throws GetTitleObjectException {
		return title;
	}
	
	@Override
	public Iterable<List<String>> body() {
		return new ArrayList<>();
	}
}
