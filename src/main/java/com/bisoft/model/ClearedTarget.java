package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IOpenedConnection;
import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClearedTarget implements IClearedTarget {
	private final IOpenedConnection dbConnection;
	private final Map<String, String> resource;
	
	public ClearedTarget(IOpenedConnection dbConnection, Map<String, String> resource) {
			this.resource = resource;
			this.dbConnection = dbConnection;
	}
	
	@Override
	public void saveObject(IModelObject obj) {
		System.out.print( obj.name() );
		try ( Session session = dbConnection.session() )
		{
			session.run(String.format(resource.get("import_obj"), obj.name()));
		}
	}
	
}
