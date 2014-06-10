package org.apache.gora.infinispan.query;

import java.io.IOException;

import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.Query;
import org.apache.gora.query.impl.ResultBase;
import org.apache.gora.store.DataStore;

public class InfinispanResult<K, T extends PersistentBase> extends ResultBase<K, T>  {

	public InfinispanResult(DataStore<K, T> dataStore, Query<K, T> query) {
		super(dataStore, query);	
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		throw new UnsupportedOperationException("to implement");
	}

	@Override
	protected boolean nextInner() throws IOException {
		throw new UnsupportedOperationException("to implement");
	}

}
