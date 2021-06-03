package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IObjectStructure;
import com.bisoft.navi.common.exceptions.LoadStructureSourceException;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.interfaces.IStructureSource;

import java.util.Iterator;
import java.util.Map;

import static com.bisoft.navi.common.interfaces.IStructureSource.ElementType.*;

public class ObjectStructure implements IObjectStructure {
	private final IStructureSource source;
	private final IClearedTarget target;

	Map<IStructureSource.ElementType, String> map = Map.of(
			OBJ, "obj_"
			, REL, "relation_"
			, FACT, "fact_"
			, DIC, "dic_"
			, ALL, "_"
	);

	public ObjectStructure(IStructureSource source, IClearedTarget target) {
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void save() {
		map.forEach((key,  value) -> {
		try {

						for (Iterator<IModelObject> it = source.objectCollection(OBJ); it.hasNext(); ) {
							IModelObject object = it.next();
							target.save(object);

						}

		} catch (LoadStructureSourceException e) {
			e.printStackTrace();
		}}
			);
	}
}
