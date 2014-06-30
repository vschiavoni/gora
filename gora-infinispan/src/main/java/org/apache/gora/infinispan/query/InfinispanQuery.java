package org.apache.gora.infinispan.query;

import org.apache.gora.filter.MapFieldValueFilter;
import org.apache.gora.filter.SingleFieldValueFilter;
import org.apache.gora.infinispan.store.InfinispanClient;
import org.apache.gora.infinispan.store.InfinispanStore;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.impl.QueryBase;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.FilterConditionContext;
import org.infinispan.query.dsl.QueryBuilder;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class InfinispanQuery<K, T extends PersistentBase> extends QueryBase<K, T> {

    public static final Logger LOG = LoggerFactory.getLogger(InfinispanQuery.class);

    private QueryFactory qf;
    private org.infinispan.query.dsl.Query q;
    private QueryBuilder qb;
    private String primaryFieldName;

	public InfinispanQuery(InfinispanStore<K, T> dataStore) {
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
        qb = qf.from(dataStore.getPersistentClass());
        primaryFieldName = dataStore.getPrimaryFieldName();
	}

    // FIXME how to handle a specific key, or a key range ?
    public void build(){

        FilterConditionContext context = null;

        if(q!=null)
            throw new IllegalAccessError("Already built");

        if (filter instanceof  MapFieldValueFilter){
            MapFieldValueFilter mfilter = (MapFieldValueFilter) filter;
            Object loperand = mfilter.getOperands().get(0); // FIXME following the implementation of HBase.
            switch (mfilter.getFilterOp()) {
                case EQUALS:
                    context = qb.having(mfilter.getFieldName()).eq(loperand);
                    break;
                case NOT_EQUALS:
                    context = qb.not().having(mfilter.getFieldName()).eq(loperand);
                    break;
                case LESS:
                    context = qb.having(mfilter.getFieldName()).lt(loperand);
                    break;
                case LESS_OR_EQUAL:
                    context = qb.having(mfilter.getFieldName()).lte(loperand);
                    break;
                case GREATER:
                    context = qb.having(mfilter.getFieldName()).gt(loperand);
                    break;
                case GREATER_OR_EQUAL:
                    context = qb.having(mfilter.getFieldName()).gte(loperand);
                    break;
                default:
                    LOG.error("FilterOp not supported..");
                    break;
            }

        } else if (filter instanceof  SingleFieldValueFilter){
            SingleFieldValueFilter sfilter = (SingleFieldValueFilter) filter;
            Object loperand = sfilter.getOperands().get(0);
            switch (sfilter.getFilterOp()) {
                case EQUALS:
                    context = qb.having(sfilter.getFieldName()).eq(loperand);
                    break;
                case NOT_EQUALS:
                    context = qb.not().having(sfilter.getFieldName()).eq(loperand);
                    break;
                case LESS:
                    context = qb.having(sfilter.getFieldName()).lt(loperand);
                    break;
                case LESS_OR_EQUAL:
                    context = qb.having(sfilter.getFieldName()).lte(loperand);
                    break;
                case GREATER:
                    context = qb.having(sfilter.getFieldName()).gt(loperand);
                    break;
                case GREATER_OR_EQUAL:
                    context = qb.having(sfilter.getFieldName()).gte(loperand);
                    break;
                default:
                    LOG.error("FilterOp not supported..");
                    break;
            }

        } else if (filter!=null) {
            LOG.error("Filter not supported.");
        }

        if (this.startKey==this.endKey && this.startKey != null ){
            (context == null ? qb : context.and()).having(primaryFieldName).eq(this.startKey);
        }else{
            if (this.startKey!=null)
                context = (context == null ? qb : context.and()).having(primaryFieldName).gte(this.startKey);
            if (this.endKey!=null)
                (context == null ? qb : context.and()).having(primaryFieldName).lte(this.endKey);
        }

        q = qb.build();
    }

    public void project(String[] fields){
        if(q!=null)
            throw new IllegalAccessError("Already built");
        qb.setProjection(fields);
    }

    public List<T> list(){
        if(q==null)
            throw new IllegalAccessError("Build before list.");
        return q.list();
    }

    public int getResultSize(){
        return q.getResultSize();
    }

}
