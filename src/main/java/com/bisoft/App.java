package com.bisoft;

import com.bisoft.interfaces.INeoQuery;
import com.bisoft.interfaces.IOpenedConnection;
import com.bisoft.model.*;
import com.bisoft.navi.common.exceptions.DBConnectionException;
import com.bisoft.navi.common.exceptions.LoadConnectionParameterException;
import com.bisoft.navi.common.exceptions.TargetConnectionException;
import com.bisoft.navi.common.model.CSVFormat;
import com.bisoft.navi.common.resources.MapResource;
import com.bisoft.navi.common.resources.XMLResource;
import org.neo4j.driver.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App
{
    static final String[] fields = new String[] {"uid", "name"};

    public static void main( String[] args )
    {
        try {
            Map<String, String> sourceResource = new MapResource("source.properties").loadedResource();
            File folder = args.length > 0 && args[0] != null && args[0].length() > 0 ? new File(args[0])
                    : new File(Paths.get(System.getProperty("user.home"), sourceResource.get("location")).toUri());

            IOpenedConnection dbConnection = new DBConnection(new MapResource("db.properties").loadedResource()).openedConnection();
            Map<String, String> cql = new XMLResource(com.bisoft.navi.App.class.getClassLoader().getResourceAsStream("cql_collection.xml")).loadedResource();
            
            Map<String, INeoQuery> map = Map.of(
              "obj_", new NeoQueryObject(cql),
              "relation_", new NeoQueryShip(cql),
              "fact_", new NeoQueryFact(cql),
              "dic_", new NeoQueryDic(cql)
            );
    
            new ObjectStructure(
              new FileSource(folder, new CSVFormat(sourceResource.get("column.delimiter"))),
              new DBTarget(
                dbConnection
                , cql
              ).clearedTarget()
            ).save();
        } catch (LoadConnectionParameterException | DBConnectionException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadFromCSV() {
        String folder = Paths.get(System.getProperty("user.home"), "neo4j\\import\\pitc").toString(); //"C:\\Users\\Chebakov.AA\\neo4j\\import\\pitc";
        Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "admin" ) );
    
        Supplier<Stream<String>> stream = () -> Arrays
          .stream((new File(folder)).listFiles())
          .map(v -> v.toPath().getFileName().toString().replace(".csv", ""));
        
        String[] models = stream.get().filter(v -> v.contains("obj_"))
          .toArray(String[]::new);
        String[] relations = stream.get().filter(v -> v.contains("relation_"))
          .toArray(String[]::new);
        String[] events = stream.get().filter(v -> v.contains("timeevent_"))
          .toArray(String[]::new);
        String[] dims = stream.get().filter(v -> v.contains("dim_"))
          .toArray(String[]::new);

        emptyNeo(driver);
        //loadTableData(driver, models);
        //loadRelationData(driver, relations);
        loadEventData(driver, folder, events);
        //loadDimData(driver, folder, dims);
        driver.close();
    }
    

    private static void emptyNeo(Driver driver) {
        try ( org.neo4j.driver.Session session = driver.session() )
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
    }
    
    private static void loadEventData2(Driver driver, String folder, String[] events) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(final String entity: events) {
                System.out.print( entity );
                String[] params = getHeader(folder, entity);
                List<String> objects = Arrays.stream(params)
                  .filter(v -> v.contains("id_"))
                  .map(v -> v.split("_")[1])
                  .collect(Collectors.toList());
                List<String> values = Arrays.stream(params)
                  .filter(v -> v.contains("v_"))
                  .map(v -> v.split("_")[1])
                  .collect(Collectors.toList());
                String s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row MATCH ", entity);
                s += objects.stream().map(v -> String.format("(%1$s:%1$s {uid: row.id_%1$s}) ", v)).collect(Collectors.joining(", "));
                String strValues = values.stream().map(v -> String.format("%1$s: coalesce(row.v_%1$s, 'н/д')", v)).collect(Collectors.joining(","));
                s += String.format(" MERGE (%1$s:%1$s {%2$s, otype: 'item'}) ", entity, strValues);
                s += objects.stream().map( v -> String.format("MERGE (%1$s)<-[:HEPPENED_ON:CONTAINED_INTO {uid: '%2$s-' + %1$s.uid, oname: '%2$s', otype: 'folder'}]-(%2$s)", v, entity)).collect(Collectors.joining(" "));
                final String query = s;
                session.writeTransaction(tx -> {
                    Result result = tx.run(query);
                    return result.toString();
                });

                System.out.println( " pass" );
            }
        }
    }

    private static void loadEventData(Driver driver, String folder, String[] events) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(final String entity: events) {
                System.out.print( entity );
                String[] params = getHeader(folder, entity);

                List<String> values = Arrays.stream(params)
                  .filter(v -> v.contains("v_"))
                  .map(v -> v.split("_")[1])
                  .collect(Collectors.toList());
    
                String label = entity.split("_")[1];
                String s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row ", entity);
                s += String.format(" MERGE (o:%1$s {uid: row.uid, oname: '%1$s', otype: 'item'}) return count(o)", label);
                final String query = s;
                s = String.format("call apoc.periodic.commit('%s',{limit:10000})", s);
                String s1 = "CALL apoc.load.csv(\"file:///pitc/timeevent_mer.csv\", {sep:\";\", arraySep:\",\", header:true}) YIELD map as row return row";
                s = String.format(" MERGE (o:%1$s {uid: row.uid, oname: \"%1$s\", otype: \"item\"}) return count(o)", label);
                s = String.format("CALL apoc.periodic.iterate('%s','%s',{batchSize:10000, iterateList:true});", s1, s);
                
//                session.writeTransaction(tx -> {
//                    Result result = tx.run(
//                      String.format("DROP INDEX %1$s_pk_uid_unique IF EXISTS", label)
//                    );
//                    return result.toString();
//                });
                session.run(s);
//                session.writeTransaction(tx -> {
//                    Result result = tx.run(query);
//                    return result.toString();
//                });
//                session.writeTransaction(tx -> {
//                    Result result = tx.run(
//                      String.format("create constraint %1$s_pk_uid_unique IF NOT EXISTS on (n:%1$s) assert n.uid is unique", label)
//                    );
//                    return result.toString();
//                });
    
                System.out.println( " pass" );
            }
        }
    }
    
    private static void loadTimeEventData(Driver driver, String folder, String[] events) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(final String entity: events) { // пробегаемся по файлам
                System.out.print( entity );
                
                String[] headers = getHeader(folder, entity);
                
                List<String> objects = Arrays.stream(headers)
                  .filter(v -> v.contains("id_"))
                  .map(v -> v.split("_")[1])
                  .collect(Collectors.toList());
                List<String> times = Arrays.stream(headers)
                  .filter(v -> v.contains("tm_"))
                  .map(v -> v.split("_")[1])
                  .collect(Collectors.toList());
                List<String> params = Arrays.stream(headers)
                  .filter(v -> v.contains("v_"))
                  .map(v -> v.split("_")[1])
                  .collect(Collectors.toList());
//                List<String> values = Arrays.stream(headers)
//                  .filter(v -> v.contains("ve_"))
//                  .map(v -> v.split("_")[1])
//                  .collect(Collectors.toList());
                
                String s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row MATCH ", entity);
                s += objects.stream().map(v -> String.format("(%1$s:%1$s {uid: row.id_%1$s}) ", v)).collect(Collectors.joining(", "));
                s += ", " + times.stream().map(v -> String.format("(%1$s:%1$s {uid: row.tm_%1$s}) ", v)).collect(Collectors.joining(", "));

                // String strValues = params.stream().map(v -> String.format("%1$s: coalesce(row.v_%1$s, 'н/д')", v)).collect(Collectors.joining(","));
                // s += String.format(" MERGE (%1$s:%1$s {%2$s, otype: 'item'}) ", entity, strValues);
    
                String strObject = objects
                  .stream()
                  .map( v -> String.format("%1$s: %1$s.uid", v)).collect(Collectors.joining(","));
                
                s += objects
                  .stream()
                  .map( v -> String.format(" MERGE (%1$s)<-[:HEPPENED_ON {%3$s, otype: 'folder'}]-(year) "
                    , v, entity, strObject))
                  .collect(Collectors.joining(" "));
                //s += String.format("MERGE (year)<-[:HEPPENED_ON {%2$s, otype: 'folder'}]-(month) ", entity, strObject);
//                s += String.format("MERGE (year)<-[:HEPPENED_ON {%2$s, otype: 'folder'}]-(month) " +
//                  "MERGE (month)<-[:HEPPENED_ON {%2$s, otype: 'folder'}]-(%1$s) ", entity, strObject);
                final String query = s;
                //session.run(s);
                session.writeTransaction(tx -> {
                    Result result = tx.run(query);
                    return result.toString();
                });
                
                System.out.println( " pass" );
            }
        }
    }
    
    private static void loadDimData(Driver driver, String folder, String[] dims) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(final String entity: dims) {
                String[] params = getHeader(folder, entity);
                switch (entity) {
                    case "dim_i18n":
                        loadI18n(session, entity, params);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid entity: " + entity);
                }
                
                System.out.println( entity );
            }
        }
    }
    
    private static void loadI18n(Session session, String entity, String[] fields) {
        String greeting = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
                String s = "";
                String val = "";
                s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row " +
                  "WHERE row.%s IS NOT NULL MERGE (o:%1$s {uid:'%2$s', word: row.%2$s})", entity, fields[0]);
                for(int i=1;i<fields.length;i++){
                    s += String.format(" MERGE (o)-[:TRANSLATED_TO]->(%3$s:%1$s {uid:'%3$s', word: row.%3$s})", entity, fields[0], fields[i]);
                }
                Result result = tx.run(s);
                return result.toString();
            }
        } );
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

    
    private static void loadRelationData(Driver driver, String[] entities) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(String entity: entities) {
                System.out.print( entity );
                String fromE = entity.split("_")[1];
                String toE = entity.split("_")[2];
                String fn = entity;
                String s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row ", fn);
                s += String.format(" MATCH (e:%1$s {%3$s: row.from_id}), (o:%2$s {%3$s: row.to_id}) " +
                    "CREATE (e)-[r:CONTAINED_INTO {uid: '%1$s-' + o.uid, oname: '%1$s', otype: 'folder'}]->(o)"
                  , fromE, toE, fields[0]);
    
                final String query = s;
                String greeting = session.writeTransaction(tx -> {
                    Result result = tx.run(query);
                    return result.toString();
                });

                System.out.println( " pass" );
            }
        }
    }
    
    private static String[] getHeader(String folder, String entity) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Paths.get(folder, entity + ".csv").toString()));
            String header = reader.readLine();
            reader.close();
            return header.split(";");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
