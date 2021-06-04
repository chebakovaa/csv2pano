package com.bisoft.interfaces;

import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Session;

import java.util.Map;

public interface ILoadedQuery {
	
	void run(Session session, IModelObject obj, Map<String, String> cql);
}
