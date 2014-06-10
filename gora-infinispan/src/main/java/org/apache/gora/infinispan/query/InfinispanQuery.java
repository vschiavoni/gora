package org.apache.gora.infinispan.query;

import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.Query;
import org.apache.gora.query.impl.QueryBase;
import org.apache.gora.store.DataStore;

public class InfinispanQuery<K, T extends PersistentBase> extends
		QueryBase<K, T> {

	public InfinispanQuery(DataStore<K, T> dataStore) {
		super(dataStore);
	}

	private Query<K, T> query;

	public InfinispanQuery() {
		this(null);
	}

	public Query<K, T> getQuery() {
		return query;
	}

	public void setQuery(Query<K, T> query) {
		this.query = query;
	}

}
