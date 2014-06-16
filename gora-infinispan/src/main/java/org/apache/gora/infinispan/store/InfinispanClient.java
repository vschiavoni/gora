/**
 * Licensed to the Apache Software Foundation (ASF) under one
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

import org.apache.gora.persistency.impl.PersistentBase;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfinispanClient<K, T extends PersistentBase> {

    public static final Logger LOG = LoggerFactory.getLogger(InfinispanClient.class);

    private RemoteCache cache;
    private RemoteCacheManager manager;

    private Class<K> keyClass;
    private Class<T> persistentClass;

    public void initialize(Class<K> keyClass, Class<T> persistentClass) throws Exception {
        this.keyClass = keyClass;
        this.persistentClass = persistentClass;
        // add keyspace to cluster
        checkKeyspace();

    }

    /**
     * Check if keyspace already exists. In the case of Infinispan, check if a cache with the same name already exists.
     */
    public boolean keyspaceExists() {
        throw new UnsupportedOperationException("todo");
    }

    /**
     * Check if keyspace already exists. If not, create it.
     */
    public void checkKeyspace() {
        throw new UnsupportedOperationException("todo");
    }

    /**
     * Drop keyspace.
     */
    public void dropKeyspace() {
        throw new UnsupportedOperationException("todo");
    }

    public void deleteByKey(K key){
        throw new UnsupportedOperationException("ask the cachemanager to delete the cache with the given name/key?");
    }

    /**
     * Obtain Schema/Keyspace name
     * @return Keyspace
     */
    public String getKeyspaceName() {
        throw new UnsupportedOperationException("map this to some ispn data structure");
    }

    public RemoteCache getCache(){
        return this.cache;
    }
}
