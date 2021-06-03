package com.bisoft.interfaces;

import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Session;

public interface INeoQuery {
	void execute(IModelObject obj, Session session);
}
