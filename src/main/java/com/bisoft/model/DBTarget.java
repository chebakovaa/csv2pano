package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IOpenedConnection;
import com.bisoft.interfaces.ITarget;

import java.util.Map;

public class DBTarget implements ITarget {
	
	private final IOpenedConnection dbConnection;
	private final Map<String, String> resource;
	
	public DBTarget(IOpenedConnection dbConnection, Map<String, String> resource) {
		this.dbConnection = dbConnection;
		this.resource = resource;
	}
	
	@Override
	public IClearedTarget clearedTarget() {
		try ( org.neo4j.driver.Session session = dbConnection.session() )
		{
			session.run(resource.get("clear_all_ships"));
			session.run(resource.get("clear_all_objs"));
		}
		return new ClearedTarget(dbConnection, resource);
	}
}
