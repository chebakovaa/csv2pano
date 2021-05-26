package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IDBConnection;
import com.bisoft.interfaces.IOpenedConnection;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

public class DBTarget implements ITarget{
	
	private final IOpenedConnection dbConnection;
	
	public DBTarget(IOpenedConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	@Override
	public IClearedTarget clearedTarget() {
		try ( org.neo4j.driver.Session session = dbConnection.session() )
		{
			String greeting = session.writeTransaction( new TransactionWork<String>()
			{
				@Override
				public String execute( Transaction tx )
				{
					Result result = tx.run("MATCH()-[n]-() delete n;");
					return result.toString();
				}
			} );
			System.out.println( greeting );
			greeting = session.writeTransaction( new TransactionWork<String>()
			{
				@Override
				public String execute( Transaction tx )
				{
					Result result = tx.run("MATCH(n) DELETE n;");
					return result.toString();
				}
			} );
		}
		return new ClearedTarget(dbConnection);
	}
}
