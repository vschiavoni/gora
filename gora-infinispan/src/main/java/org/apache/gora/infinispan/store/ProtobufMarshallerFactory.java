package org.apache.gora.infinispan.store;

import java.io.InputStream;

import org.infinispan.protostream.BaseMarshaller;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public  class ProtobufMarshallerFactory<K>{
	
	public  BaseMarshaller<K> newMarshaller(final Class<K> clazz) {
		
		BaseMarshaller<K> marshaller = new BaseMarshaller<K>() {
			
			@Override
			public Class<? extends K> getJavaClass() {
				return clazz;
			}

			@Override
			public String getTypeName() {
				//System.out.println("typename for marshaller:" + clazz.getCanonicalName());
				return clazz.getCanonicalName();
			}
		};
		
		return marshaller;		
	}
	
	public InputStream newProtobuf(Class<K> schemaClass) {	
		Schema<K> schema = RuntimeSchema.getSchema(schemaClass);			
		return null;		
	}
	
}