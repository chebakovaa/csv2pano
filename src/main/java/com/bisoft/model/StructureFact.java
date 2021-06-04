package com.bisoft.model;

import com.bisoft.interfaces.ILoadedQuery;
import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Session;

import java.util.Map;

public class StructureFact implements ILoadedQuery {
	@Override
	public void run(Session session, IModelObject obj, Map<String, String> cql) {
	
	}
}
