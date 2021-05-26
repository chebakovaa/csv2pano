package com.bisoft.interfaces;

import com.bisoft.exeptions.DBConnectionException;
import org.neo4j.driver.Session;

public interface IDBConnection {
    IOpenedConnection openedConnection() throws DBConnectionException;
	
}
