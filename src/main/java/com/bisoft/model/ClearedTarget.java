package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.INeoQuery;
import com.bisoft.interfaces.IOpenedConnection;
import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Session;

import java.util.Map;

public class ClearedTarget implements IClearedTarget {
	private final IOpenedConnection dbConnection;
	private final Map<String, String> resource;
	
	public ClearedTarget(IOpenedConnection dbConnection, Map<String, String> resource) {
			this.resource = resource;
			this.dbConnection = dbConnection;
	}
	
	@Override
	public void save(IModelObject obj, INeoQuery nquery) {
		System.out.println( obj.name() );
		try ( Session session = dbConnection.session() )
		{
			nquery.execute(obj, session);
		}
	}
}
