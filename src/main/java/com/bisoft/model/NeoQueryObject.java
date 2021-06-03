package com.bisoft.model;

import com.bisoft.interfaces.INeoQuery;
import com.bisoft.navi.common.interfaces.IModelObject;
import com.bisoft.navi.common.resources.XMLResource;
import org.neo4j.driver.Session;

import java.util.Map;

public class NeoQueryObject implements INeoQuery {
	
	private final Map<String, String> querySource;
	
	public NeoQueryObject(Map<String, String> cql) {
		querySource = cql;
	}
	
	@Override
	public void execute(IModelObject obj, Session session) {
		session.run(String.format(querySource.get("import_obj"), obj.name()));
	}
}
