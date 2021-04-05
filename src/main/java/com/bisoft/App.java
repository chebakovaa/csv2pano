package com.bisoft;

import org.neo4j.driver.*;
import org.apache.commons.lang3.ArrayUtils;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
        String folder = "C:\\Users\\Chebakov.AA\\neo4j\\import\\pitc";
        Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "admin" ) );
    
        Supplier<Stream<String>> stream = () -> Arrays
          .stream((new File(folder)).listFiles())
          .map(v -> v.toPath().getFileName().toString().replace(".csv", ""));
        
        String[] models = stream.get().filter(v -> v.contains("obj_"))
          .toArray(String[]::new);
        String[] relations = stream.get().filter(v -> v.contains("relation_"))
          .toArray(String[]::new);
        String[] dims = stream.get().filter(v -> v.contains("dim_"))
          .toArray(String[]::new);

        emptyNeo(driver);
        loadTableData(driver, models);
        loadRelationData(driver, relations);
        loadDimData(driver, dims);
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
    
    private static void loadDimData(Driver driver, String[] dims) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(final String entity: dims) {
                switch (entity) {
                    case "dim_i18n":
                        loadI18n(session, entity);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid entity: " + entity);
                }
                
                System.out.println( entity );
            }
        }
    }
    
    private static void loadI18n(Session session, String entity) {
        String greeting = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
                String s = "";
                String val = "";
                s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row WITH row WHERE row.%s IS NOT NULL", entity, "mnem");
                s += String.format(" MERGE (o:%1$s {%2$s, oname: '%1$s', otype: 'item'}); ", entity, pars);
                Result result = tx.run(s);
                return result.toString();
            }
        } );
    }
    
    public static void loadTableData(Driver driver, String[] entities) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(final String entity: entities) {
                String greeting = session.writeTransaction( new TransactionWork<String>()
                {
                    @Override
                    public String execute( Transaction tx )
                    {
                        String s = "";
                        String val = "";
                        s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row WITH row WHERE row.%s IS NOT NULL", entity, fields[0]);
                        String pars = Arrays.stream(fields).map(v -> String.format("%1$s: row.%1$s", v)).collect(Collectors.joining(","));
//                        pars = pars.replace(String.format("row.%s", fields[0])
//                          , String.format("row.%s", fields[0]));
                        s += String.format(" MERGE (o:%1$s {%2$s, oname: '%1$s', otype: 'item'}); ", entity, pars);
                        Result result = tx.run(s);
                        return result.toString();
                    }
                } );
                System.out.println( entity );
            }
        }
    }
    
    private static void loadRelationData(Driver driver, String[] entities) {
        try ( org.neo4j.driver.Session session = driver.session() )
        {
            for(String entity: entities) {
                String greeting = session.writeTransaction( new TransactionWork<String>()
                {
                    @Override
                    public String execute( Transaction tx )
                    {
                        String s = "";
                        String fromE = entity.split("_")[1];
                        String toE = entity.split("_")[2];
                        String fn = entity;
                        s = String.format("LOAD CSV WITH HEADERS FROM 'file:///pitc/%s.csv' AS row", fn);
                        s += String.format(" MATCH (e:%1$s {%3$s: row.from_id}), (o:%2$s {%3$s: row.to_id}) " +
                            "CREATE (e)-[r:CONTAINED_INTO {uid: '%1$s-' + o.uid, oname: '%1$s', otype: 'folder'}]->(o)"
                          , fromE, toE, fields[0]);
                        Result result = tx.run(s);
                        return result.toString();
                    }
                } );
                System.out.println( entity );
            }
        }
    }
    
}
