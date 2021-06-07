package com.bisoft.interfaces;

import org.neo4j.driver.Session;

public interface IOpenedConnection extends AutoCloseable {
	Session session();
	
	@Override
	void close() throws Exception;
}
