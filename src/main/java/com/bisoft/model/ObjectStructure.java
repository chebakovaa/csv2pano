package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IObjectStructure;
import com.bisoft.navi.common.exceptions.LoadStructureSourceException;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.interfaces.IStructureSource;

import java.util.Iterator;

import static com.bisoft.navi.common.interfaces.IStructureSource.ElementType.OBJ;

public class ObjectStructure implements IObjectStructure {
	private final IStructureSource source;
	private final IClearedTarget target;
	
	public ObjectStructure(IStructureSource source, IClearedTarget target) {
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void save() throws LoadStructureSourceException {
		for (Iterator<IModelObject> it = source.objectCollection(OBJ); it.hasNext(); ) {
			target.saveObject(it.next());
		}
	}
}
