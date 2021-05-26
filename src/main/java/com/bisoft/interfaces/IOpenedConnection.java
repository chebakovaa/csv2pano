package com.bisoft.interfaces;

import org.neo4j.driver.Session;

public interface IOpenedConnection {
	Session session();
}
