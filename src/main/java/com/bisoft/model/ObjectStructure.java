package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IFileSource;
import com.bisoft.interfaces.IObjectStructure;

public class ObjectStructure implements IObjectStructure {
	private final IStructureSource source;
	private final IClearedTarget target;
	
	public ObjectStructure(IFileSource source, IClearedTarget target) {
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void save() {
		source.
	}
}
