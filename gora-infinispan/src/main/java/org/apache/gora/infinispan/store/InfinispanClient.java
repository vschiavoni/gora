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

import com.google.protobuf.Descriptors;
import org.apache.gora.persistency.impl.PersistentBase;
import org.infinispan.arquillian.utils.MBeanServerConnectionProvider;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.commons.util.Util;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.remote.ProtobufMetadataManager;
import org.infinispan.query.remote.client.MarshallerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;

// FIXME to be run wtih -Dcom.sun.management.jmxremote.port=10000 -Dcom.sun.management.jmxremote.authenticate=false

public class InfinispanClient<K, T extends PersistentBase> {
	public static final Logger LOG = LoggerFactory
			.getLogger(InfinispanClient.class);

    private static final String PROTOBUF_DESCRIPTOR_RESOURCE =  "/bank.protobin";
    private static String host = "127.0.0.1";
    private static int jmxPort = 9999;
    private  static int hotRodPort = 15233;

    public static String JMX_DOMAIN = "infinispan*";
	
	private Class<K> keyClass;
	private Class<T> persistentClass;
	private RemoteCacheManager cacheManager;

	private RemoteCache<K, T> cache; // TODO use as types the keyClass clazz

	public void initialize(Class<K> keyClass, Class<T> persistentClass,
			Properties properties) throws Exception {

		LOG.info("Initializing InfinispanClient");

        // initialize cache maanager and cache
        this.keyClass = keyClass;
        this.persistentClass = persistentClass;
        ConfigurationBuilder clientBuilder = new ConfigurationBuilder();
        clientBuilder.addServer().host(host).port(hotRodPort).marshaller(new ProtoStreamMarshaller());
        cacheManager = new RemoteCacheManager(clientBuilder.build(), true);
        cache = cacheManager.getCache();

        // register proto file
        // this.registerRemoteProtofile(host,jmxPort);
        this.registerLocalProtofile();

        //initialize client-side serialization context
        MarshallerRegistration.registerMarshallers(
                ProtoStreamMarshaller.getSerializationContext(cacheManager));

        registerMarshaller(persistentClass);
		registerMarshaller(keyClass);

		cache = this.cacheManager.getCache(getKeyspaceName());

		// add keyspace to cluster
		checkKeyspace();

	}
	
	 private static ObjectName createObjectName(String name) {
	      try {
	         return new ObjectName(name);
		} catch (MalformedObjectNameException e) {
	         throw new RuntimeException(e);
	      }
	   }


	/**
	 * Check if keyspace already exists. In the case of Infinispan, check if a
	 * cache with the same name already exists.
	 */
	public boolean keyspaceExists() {
		if (cacheManager.getCache(getKeyspaceName()) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Check if keyspace already exists. If not, create it.
	 */
	public void checkKeyspace() {
		RemoteCache<Object, Object> remoteCache = cacheManager
				.getCache(getKeyspaceName());
		if (remoteCache == null) {
			// TODO there is no way via hot-rod to create a remote cache..what
			// do we do here ?
			// Pierre suggests to go via JMX
		}
	}

	/**
	 * Drop keyspace.
	 */
	public void dropKeyspace() {

		// via hot-rod we cannot delete caches, what do we do ? JMX again ?

	}

	public void deleteByKey(K key) {
		throw new UnsupportedOperationException(
				"To be implemented yet");
	}

	public void putInCache(K key, T val) {
		this.cache.put(key, val);
	}

	/**
	 * Obtain Schema/Keyspace name
	 * 
	 * @return Keyspace
	 */
	public String getKeyspaceName() {
		return this.keyClass.getName();
	}

	public RemoteCacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(RemoteCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public RemoteCache<K, T> getCache() {
		return this.cache;
	}

    //
    // HELPERS
    //

    private  <M> void registerMarshaller(Class<M> marshalee) {

        SerializationContext srcCtx = ProtoStreamMarshaller
                .getSerializationContext(this.cacheManager);

        try {
            srcCtx.registerProtofile(PROTOBUF_DESCRIPTOR_RESOURCE);
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        } catch (Descriptors.DescriptorValidationException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }

        srcCtx.registerMarshaller(
                ProtobufMarshallerFactory.newMarshaller(marshalee));

        LOG.info("Registered Marshaller for class " + marshalee.getName());

    }
    private Object invokeOperation(MBeanServerConnectionProvider provider, String mbean, String operationName, Object[] params,
                                   String[] signature) throws Exception {
        return provider.getConnection().invoke(new ObjectName(mbean), operationName, params, signature);
    }


    private byte[] readClasspathResource(String resourcePath) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        try {
            return Util.readStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private void registerRemoteProtofile(String jmxHost, int jmxPort) throws Exception {
        JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:remoting-jmx://" + jmxHost + ":" + jmxPort));
        MBeanServerConnection jmxConnection = jmxConnector.getMBeanServerConnection();

        ObjectName protobufMetadataManagerObjName = new ObjectName("jboss.infinispan:type=RemoteQuery,name="
                + ObjectName.quote("DefaultCacheManager") + ",component=ProtobufMetadataManager");

        //initialize client-side serialization context via JMX
        byte[] descriptor = readClasspathResource(PROTOBUF_DESCRIPTOR_RESOURCE);
        jmxConnection.invoke(protobufMetadataManagerObjName, "registerProtofile", new Object[]{descriptor}, new String[]{byte[].class.getName()});
        jmxConnector.close();
    }

    private void registerLocalProtofile() throws Exception {
        MBeanServer mBeanServer =ManagementFactory.getPlatformMBeanServer();
        LOG.info("MBeans managed by mBeanServer: "+ mBeanServer.getMBeanCount());

        for (String s: mBeanServer.getDomains()){
            if (s.contains("infinispan-")) { //ugly hack, but it works
                JMX_DOMAIN=s;
                break;
            }
        }
        QueryExp queryExpRemoteQueryMBean = new ObjectName(JMX_DOMAIN + ":type=RemoteQuery,name="
                + ObjectName.quote("DefaultCacheManager")
                + ",component=" + ProtobufMetadataManager.OBJECT_NAME);

        LOG.info("Recovering Mbean with name: "+queryExpRemoteQueryMBean.toString());

        ObjectInstance targetBean = null;

        for (ObjectInstance mbean : mBeanServer.queryMBeans(null, queryExpRemoteQueryMBean)) {
            LOG.info("MBean matching query:"+mbean.getClassName());
            targetBean = mbean;
        }

        //initialize server-side serialization context via JMX
        byte[] descriptor = readClasspathResource(PROTOBUF_DESCRIPTOR_RESOURCE);

        // FIXME fails with a status WAITING (name of the mbean is not appropriate ?)
        mBeanServer.invoke( targetBean.getObjectName(), "registerProtofile", new Object[]{descriptor}, new String[]{byte[].class.getName()});

    }

}
