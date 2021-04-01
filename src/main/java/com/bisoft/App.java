package com.bisoft;

import org.neo4j.driver.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
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
    
        String[] models = Arrays
          .stream((new File(folder)).listFiles()).map(v -> v.toPath().getFileName().toString().replace(".csv", "")).filter(v -> !v.contains("relation"))
          .toArray(String[]::new);
        String[] relations = Arrays
          .stream((new File(folder)).listFiles()).map(v -> v.toPath().getFileName().toString().replace(".csv", "")).filter(v -> v.contains("relation"))
          .toArray(String[]::new);

        emptyNeo(driver);
        loadTableData(driver, models);
        loadRelationData(driver, relations);
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
    
    public static void loadTableData(Driver driver, String[] entities)
    {
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
                        pars = pars.replace(String.format("row.%s", fields[0])
                          , String.format("toInteger(row.%s)", fields[0]));
                        s += String.format(" MERGE (o:%s {%s}); ", entity, pars);
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
                        s += String.format(" MATCH (e:%1$s {%3$s: toInteger(row.from_id)}), (o:%2$s {%3$s: toInteger(row.to_id)}) CREATE (e)-[r:CONTAINED_INTO]->(o)"
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
