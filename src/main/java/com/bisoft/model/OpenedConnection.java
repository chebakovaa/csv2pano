package com.bisoft.model;

import com.bisoft.interfaces.IOpenedConnection;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

public class OpenedConnection implements IOpenedConnection {
	private final Driver driver;
	
	public OpenedConnection(Driver driver) {
		this.driver = driver;
	}
	
	@Override
	public Session session() {
		return driver.session();
	}
}
