package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IOpenedConnection;

public class ClearedTarget implements IClearedTarget {
	private final IOpenedConnection dbConnection;
	
	public ClearedTarget(IOpenedConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
}
