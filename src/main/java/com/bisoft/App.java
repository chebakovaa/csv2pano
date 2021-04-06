package com.bisoft;

import org.neo4j.driver.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App
{
    static final String[] fields = new String[] {"uid", "name"};

    public static void main( String[] args )
    {
        System.out.println( "Start load!" );
        loadFromCSV();
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
        String[] events = stream.get().filter(v -> v.contains("event_"))
          .toArray(String[]::new);
        String[] dims = stream.get().filter(v -> v.contains("dim_"))
          .toArray(String[]::new);

        emptyNeo(driver);
        loadTableData(driver, models);
        loadRelationData(driver, relations);
        loadEventData(driver, folder, events);
        loadDimData(driver, folder, dims);
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
    
    private static void loadEventData(Driver driver, String folder, String[] events) {
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
                s += String.format(" MERGE (%1$s:%1$s {%2$s}) ", entity, strValues);
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
                s = String.format("USING PERIODIC COMMIT 1000 LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row WHERE row.%s IS NOT NULL", file, fields[0]);
                String pars = Arrays.stream(fields).map(v -> String.format("%1$s: row.%1$s", v)).collect(Collectors.joining(","));
                s += String.format(" MERGE (o:%1$s {%2$s, oname: '%1$s', otype: 'item'}); ", entity, pars);
                session.run(s);
                
//                String greeting = session.writeTransaction(tx -> {
//                    Result result = tx.run(s);
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
                String s = String.format("USING PERIODIC COMMIT 1000 LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row FIELDTERMINATOR ';' WITH row ", fn);
                s += String.format(" MATCH (e:%1$s {%3$s: row.from_id}), (o:%2$s {%3$s: row.to_id}) " +
                    "CREATE (e)-[r:CONTAINED_INTO {uid: '%1$s-' + o.uid, oname: '%1$s', otype: 'folder'}]->(o)"
                  , fromE, toE, fields[0]);
                session.run(s);


//                String greeting = session.writeTransaction( new TransactionWork<String>()
//                {
//                    @Override
//                    public String execute( Transaction tx )
//                    {
//                        Result result = tx.run(s);
//                        return result.toString();
//                    }
//                } );
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
