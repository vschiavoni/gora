package org.apache.gora.infinispan.query;

import org.apache.gora.filter.MapFieldValueFilter;
import org.apache.gora.filter.SingleFieldValueFilter;
import org.apache.gora.infinispan.store.InfinispanClient;
import org.apache.gora.infinispan.store.InfinispanStore;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.impl.QueryBase;
import org.apache.gora.store.DataStore;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class InfinispanQuery<K, T extends PersistentBase> extends QueryBase<K, T> {

    public static final Logger LOG = LoggerFactory.getLogger(InfinispanQuery.class);

    private QueryFactory qf;
    private org.infinispan.query.dsl.Query q;

	public InfinispanQuery(DataStore<K, T> dataStore) {		
		super(dataStore);
		if (dataStore==null){
			throw new IllegalArgumentException("Illegal datastore, value is null");
		}
		InfinispanStore<K,T> infinispanStore = (InfinispanStore<K,T>)dataStore;
		if (infinispanStore.isInitialized()!=true){
			throw new IllegalStateException("Cannot execute query for not-initialized datastore");
		}
        InfinispanClient<K,T> client = infinispanStore.getClient();       
		RemoteCache<K,T> remoteCache = client.getCache();
		qf = Search.getQueryFactory(remoteCache);
	}

    // FIXME how to handle a specific key, or a key range ?
    public void build(){

        if(q!=null)
            throw new IllegalAccessError("Already built");

        if (filter instanceof  MapFieldValueFilter){
            MapFieldValueFilter mfilter = (MapFieldValueFilter) filter;
            Object loperand = mfilter.getOperands().get(0); // FIXME following the implementation of HBase.
            switch (mfilter.getFilterOp()) {
                case EQUALS:
                    qf.having(mfilter.getFieldName()).eq(loperand);
                    break;
                case NOT_EQUALS:
                    qf.not().having(mfilter.getFieldName()).eq(loperand);
                    break;
                case LESS:
                    qf.having(mfilter.getFieldName()).lt(loperand);
                    break;
                case LESS_OR_EQUAL:
                    qf.having(mfilter.getFieldName()).lte(loperand);
                    break;
                case GREATER:
                    qf.having(mfilter.getFieldName()).gt(loperand);
                    break;
                case GREATER_OR_EQUAL:
                    qf.having(mfilter.getFieldName()).gte(loperand);
                    break;
                default:
                    LOG.error("FilterOp not supported..");
                    break;
            }

        } else if (filter instanceof  SingleFieldValueFilter){

        } else{
            LOG.error("Filter not supported.");
        }

        q = qf.from(dataStore.getPersistentClass()).build();
    }

    public List<T> list(){
        if(q==null)
            throw new IllegalAccessError("Build before list.");
        return q.list();
    }

}
