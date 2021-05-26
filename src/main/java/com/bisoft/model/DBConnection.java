package com.bisoft.model;

import com.bisoft.exeptions.DBConnectionException;
import com.bisoft.interfaces.IDBConnection;
import com.bisoft.interfaces.IOpenedConnection;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.sql.Connection;
import java.util.Map;

import static com.bisoft.helpers.SqlHelper.getConnection;

public class DBConnection implements IDBConnection {
    private final Map<String, String> resource;

    public DBConnection(Map<String, String> resource) {
        this.resource = resource;
    }
    
    @Override
    public IOpenedConnection openedConnection() throws DBConnectionException {
        Driver driver = GraphDatabase.driver( resource.get("neo.url"), AuthTokens.basic( resource.get("neo.username"), resource.get("noe.password") ) );

        if (driver == null) {
            throw new DBConnectionException("DB connection fail");
        }
        return new OpenedConnection(driver);
    }
    
}
