package org.apache.gora.infinispan.query;

import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.Query;
import org.apache.gora.query.impl.ResultBase;
import org.apache.gora.store.DataStore;

import java.io.IOException;
import java.util.List;

/*
* TODO implements a lazy retrieval of the reuslts (if possible over HotRod).
 */
public class InfinispanResult<K, T extends PersistentBase> extends ResultBase<K, T>  {

    private List<T> list;
    private int current;

	public InfinispanResult(DataStore<K, T> dataStore, Query<K, T> query, InfinispanQuery<K, T> infinispanQuery) {
        super(dataStore, query);
        infinispanQuery.build();
        list = infinispanQuery.list();
        current = 0;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
        return (float)1;
	}

	@Override
	protected boolean nextInner() throws IOException {
        if(current==list.size()-1)
            return false;
        persistent = list.get(current);
        current++;
        return true;
    }

}
