package com.bisoft.model;

import com.bisoft.interfaces.ILoadedQuery;
import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Session;

import java.util.Map;

public class StructureShip implements ILoadedQuery {
	@Override
	public void run(Session session, IModelObject obj, Map<String, String> cql) {
		String[] names = obj.name().split("_");
		String s0 = String.format(cql.get("relation-load"), obj.name());
		String s1 = String.format(cql.get("relation-create"), names[1], names[2]);
		String s = String.format(cql.get("apoc-iterate"), s0, s1);
		session.run(s);
	}
}
