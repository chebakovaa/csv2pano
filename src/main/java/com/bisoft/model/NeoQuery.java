package com.bisoft.model;

import com.bisoft.interfaces.ILoadedQuery;
import com.bisoft.interfaces.INeoQuery;
import com.bisoft.navi.common.interfaces.IModelObject;
import org.neo4j.driver.Session;

import java.util.Map;

public class NeoQuery implements INeoQuery {
    
    private final Map<String, String> cql;
    private final ILoadedQuery query;
    
    public NeoQuery(ILoadedQuery query, final Map<String, String> cql) {
        this.cql = cql;
        this.query = query;
    }
    
    @Override
    public void execute(IModelObject obj, Session session) {
        query.run(session, obj, cql);
    }
    
}
