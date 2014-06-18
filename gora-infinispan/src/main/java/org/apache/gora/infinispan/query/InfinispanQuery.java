package org.apache.gora.infinispan.query;

import org.apache.gora.infinispan.store.InfinispanClient;
import org.apache.gora.infinispan.store.InfinispanStore;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.impl.QueryBase;
import org.apache.gora.store.DataStore;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;

import java.util.List;


public class InfinispanQuery<K, T extends PersistentBase> extends QueryBase<K, T> {

    private QueryFactory qf;
    private org.infinispan.query.dsl.Query q;

	public InfinispanQuery(DataStore<K, T> dataStore) {		
		super(dataStore);
		if (dataStore==null){
			throw new IllegalArgumentException("Illegal null datastore");
		}
		InfinispanStore infinispanStore = (InfinispanStore)dataStore;
		if (infinispanStore.isInitialized()!=true){
			throw new IllegalStateException("Cannot execute query for not-initialized datastore");
		}
        InfinispanClient client = infinispanStore.getClient();       
		RemoteCache remoteCache = client.getCache();
		qf = Search.getQueryFactory(remoteCache);
	}

	public InfinispanQuery() {
		this(null);
	}

    public void build(){
        if(q!=null)
            throw new IllegalAccessError("Already built");
        q = qf.from(dataStore.getPersistentClass()).build();
    }

    public List list(){
        if(q==null)
            throw new IllegalAccessError("Build before list.");
        return q.list();
    }

}
