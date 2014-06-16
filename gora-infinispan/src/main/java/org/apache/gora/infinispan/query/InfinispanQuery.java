package org.apache.gora.infinispan.query;

import org.apache.gora.infinispan.store.InfinispanStore;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.impl.QueryBase;
import org.apache.gora.store.DataStore;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;

import java.util.List;


public class InfinispanQuery<K, T extends PersistentBase> extends QueryBase<K, T> {

    private QueryFactory qf;
    private org.infinispan.query.dsl.Query q;

	public InfinispanQuery(DataStore<K, T> dataStore) {
		super(dataStore);
        qf = Search.getQueryFactory(((InfinispanStore)this.dataStore).getClient().getCache());
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
