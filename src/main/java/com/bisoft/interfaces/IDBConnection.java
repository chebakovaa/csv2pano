package com.bisoft.interfaces;

import com.bisoft.exeptions.DBConnectionException;

public interface IDBConnection {
    IOpenedConnection openedConnection() throws DBConnectionException;
}
