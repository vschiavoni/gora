/**
 a * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.gora.infinispan.store;

import org.apache.avro.Schema;
import org.apache.gora.infinispan.query.InfinispanQuery;
import org.apache.gora.infinispan.query.InfinispanResult;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.query.PartitionQuery;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;
import org.apache.gora.store.impl.DataStoreBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * {@link org.apache.gora.infinispan.store.InfinispanStore} is the primary class
 * responsible for directing Gora CRUD operations into Infinispan. We (delegate) rely
 * heavily on {@link org.apache.gora.infinispan.store.InfinispanClient} for many operations
 * such as initialization, creating and deleting schemas (Infinispan caches), etc.
 */
public class InfinispanStore<K, T extends PersistentBase> extends DataStoreBase<K, T> {

    /**
     * Logging implementation
     */
    public static final Logger LOG = LoggerFactory.getLogger(InfinispanStore.class);

    private InfinispanClient<K, T> infinispanClient = new InfinispanClient<K, T>();
    private String primaryFieldName;
    private int primaryFieldPos;
    private boolean initialized = false;
    /**
     * The values are cache entries pending to be stored.
     * <p/>
     * We want to iterate over the keys in insertion order.
     * We don't want to lock the entire collection before iterating over the keys,
     * since in the meantime other threads are adding entries to the map.
     */
    private Map<K, T> buffer = Collections.synchronizedMap(new LinkedHashMap<K, T>());

    /**
     * The default constructor for InfinispanStore
     */
    public InfinispanStore() throws Exception {
    }

    @Override
    public void initialize(Class<K> keyClass, Class<T> persistentClass, Properties properties) {

        try {

            super.initialize(keyClass, persistentClass, properties);

            LOG.info("InfinispanStore initializing with key class: "
                    + keyClass.getCanonicalName() + " and persistent class:"
                    + persistentClass.getCanonicalName());

            T t = persistentClass.newInstance();
            for (Schema.Field f : t.getSchema().getFields()){
                if (f.aliases().contains("primary")) {
                    if (primaryFieldName != null){
                        primaryFieldName = f.name();
                        primaryFieldPos = f.pos();
                    }else{
                        throw new IllegalArgumentException("Invalid schema due to multiple primary keys.");
                    }
                }
            }

            LOG.warn("Cannot infer primary key from schema.");

            this.infinispanClient.initialize(keyClass, persistentClass, properties);

            initialized = true;

        } catch (Exception e) {
            LOG.error(e.getMessage());
            LOG.error(e.getStackTrace().toString());
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        LOG.debug("close");
        flush();
    }

    @Override
    public void createSchema() {
        LOG.debug("creating Infinispan keyspace");
        this.infinispanClient.checkKeyspace();
    }

    @Override
    public boolean delete(K key) {
        this.infinispanClient.deleteByKey(key);
        return true;
    }

    @Override
    public long deleteByQuery(Query<K, T> query) {
        InfinispanQuery<K, T> q = (InfinispanQuery) query;
        q.build();
        for( T t : q.list()){
            infinispanClient.deleteByKey((K) t.get(primaryFieldPos));
        }
        return q.getResultSize();
    }

    @Override
    public void deleteSchema() {
        LOG.debug("delete schema");
        this.infinispanClient.dropKeyspace();
    }

    /**
     * When executing Gora Queries in Infinispan .. TODO
     */
    @Override
    public Result<K, T> execute(Query<K, T> query) {
        return new InfinispanResult<K, T>(this, (InfinispanQuery<K, T>) query);
    }


    /**
     * Flush the buffer which is a synchronized {@link java.util.LinkedHashMap}
     * storing fields pending to be stored by
     * {@link org.apache.gora.infinispan.store.InfinispanStore#put(Object, PersistentBase)}
     * operations. Invoking this method therefore writes the buffered entries into Infinispan.
     *
     * @see org.apache.gora.store.DataStore#flush()
     */
    @Override
    public void flush() {

        Set<K> keys = this.buffer.keySet();

        // this duplicates memory footprint
        @SuppressWarnings("unchecked")
        K[] keyArray = (K[]) keys.toArray();

        // iterating over the key set directly would throw
        //ConcurrentModificationException with java.util.HashMap and subclasses
        for (K key : keyArray) {
            T value = this.buffer.get(key);
            if (value == null) {
                LOG.info("Value to update is null for key: " + key);
                continue;
            }
//      Schema schema = value.getSchema(); 
//      for (Field field: schema.getFields()) {
//        if (value.isDirty(field.pos())) {
//          addOrUpdateField(key, field, field.schema(), value.get(field.pos()));
//        }
//      }
        }

        // remove flushed rows from the buffer as all
        // added or updated fields should now have been written.
        for (K key : keyArray) {
            this.buffer.remove(key);
        }
    }

    @Override
    public T get(K key, String[] fields) {
        InfinispanQuery query = new InfinispanQuery(this);
        query.setKey(key);
        query.project(fields);
        query.build();
        List<T> l = query.list();
        if (l.isEmpty())
            return null;
        return l.get(0);
    }

    @Override
    public List<PartitionQuery<K, T>> getPartitions(Query<K, T> query)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * In Infinispan, Schemas are referred to as caches.
     *
     * @return Cache
     */
    @Override
    public String getSchemaName() {
        return this.infinispanClient.getKeyspaceName();
    }

    @Override
    public Query<K, T> newQuery() {
        Query<K, T> query = new InfinispanQuery<K, T>(this);
        query.setFields(getFieldsToQuery(null));
        return query;
    }

    @Override
    public void put(K key, T value) {
        this.infinispanClient.putInCache(key, value);
    }

    /**
     * Simple method to check if a an Infinispan Keyspace exists.
     *
     * @return true if a Keyspace exists.
     */
    @Override
    public boolean schemaExists() {
        LOG.info("schema exists");
        return infinispanClient.keyspaceExists();
    }

    public InfinispanClient<K, T> getClient() {
        return infinispanClient;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getPrimaryFieldName() {
        return primaryFieldName;
    }

    public void setPrimaryFieldName(String name){
        primaryFieldName = name;
    }

    public int getPrimaryFieldPos(){
        return primaryFieldPos;
    }

    public void setPrimaryFieldPos(int p){
        primaryFieldPos = p;
    }
}
