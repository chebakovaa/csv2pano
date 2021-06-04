package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.INeoQuery;
import com.bisoft.interfaces.IObjectStructure;
import com.bisoft.navi.common.exceptions.LoadStructureSourceException;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.interfaces.IStructureSource;

import java.util.Iterator;
import java.util.Map;

import static com.bisoft.navi.common.interfaces.IObjectStructure.ElementType.*;


public class ObjectStructure implements IObjectStructure {
	private final IStructureSource source;
	private final IClearedTarget target;
	private final Map<String, INeoQuery> map;
	
	
	public ObjectStructure(IStructureSource source, IClearedTarget target, Map<String, INeoQuery> map) {
		this.source = source;
		this.target = target;
		this.map = map;
	}
	
	@Override
	public void save() {
		map.forEach((key,  value) -> {
			try {
							for (Iterator<IModelObject> it = source.objectCollection(key); it.hasNext(); ) {
								target.save(it.next(), value);
							}
			} catch (LoadStructureSourceException e) {
				e.printStackTrace();
			}}
		);
	}
}
