package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IOpenedConnection;
import com.bisoft.interfaces.ITarget;
import com.bisoft.navi.common.exceptions.LoadResourceException;
import org.neo4j.driver.Session;

import java.util.Map;

public class DBTarget implements ITarget {
	
	private final IOpenedConnection dbConnection;
	private final Map<String, String> resource;
	
	public DBTarget(IOpenedConnection dbConnection, Map<String, String> resource) {
		this.dbConnection = dbConnection;
		this.resource = resource;
	}
	
	@Override
	public IClearedTarget clearedTarget() throws LoadResourceException {
		if(!resource.containsKey("clear-all-ships") || !resource.containsKey("clear-all-objs")){
				throw new LoadResourceException(String.format("Loading clear query from %s fail ", resource.toString()), new Exception());
		}
		try ( Session session = dbConnection.session() )
		{

				session.run(resource.get("clear-all-ships"));
				session.run(resource.get("clear-all-objs"));
		}
		return new ClearedTarget(dbConnection, resource);
	}
}
