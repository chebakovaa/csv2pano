package com.bisoft.model;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.interfaces.IOpenedConnection;
import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClearedTarget implements IClearedTarget {
	private final IOpenedConnection dbConnection;
	private final Map<String, String> resource;
	
	public ClearedTarget(IOpenedConnection dbConnection, Map<String, String> resource) {
			this.resource = resource;
			this.dbConnection = dbConnection;
	}
	
	@Override
	public void saveObject(IModelObject obj) {
	
	
	}
	
	
	public static void loadTableData(Driver driver, String[] entities) {
		try ( org.neo4j.driver.Session session = driver.session() )
		{
			for(final String file: entities) {
				String entity = file.replace("obj_", "");
				System.out.print( entity );
				String s = "";
				s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row WHERE row.%s IS NOT NULL", file, fields[0]);
				String pars = Arrays.stream(fields).map(v -> String.format("%1$s: row.%1$s", v)).collect(Collectors.joining(","));
				s += String.format(" MERGE (o:%1$s {%2$s, oname: '%1$s', otype: 'item'}) return count(o); ", entity, pars);
				s = String.format("CALL apoc.import.csv([{fileName: 'file:///pitc/obj_%1$s.csv', labels:['%1$s']}], [], {delimiter: ';', arrayDelimiter: '|', stringIds: true})", entity);
				final String query = s;


//                session.writeTransaction(tx -> {
//                    Result result = tx.run(
//                      String.format("DROP INDEX %1$s_pk_uid_unique IF EXISTS", entity)
//                    );
//                    return result.toString();
//                });
				
				String greeting = session.writeTransaction(tx -> {
					Result result = tx.run(query);
					return result.toString();
				});

//                session.writeTransaction(tx -> {
//                    Result result = tx.run(
//                      String.format("create constraint %1$s_pk_uid_unique IF NOT EXISTS on (n:%1$s) assert n.uid is unique", entity)
//                    );
//                    return result.toString();
//                });
				
				System.out.println( " pass" );
			}
		}
	}
	
	
	
}
