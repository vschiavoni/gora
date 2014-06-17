package org.apache.gora.infinispan.store;

import java.io.InputStream;

import org.infinispan.protostream.BaseMarshaller;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public  class ProtobufMarshallerFactory<K>{
	
	public  BaseMarshaller<K> newMarshaller(Class<K> schemaClass) {
		Schema<K> schema = RuntimeSchema.getSchema(schemaClass);	
		return null;		
	}
	
	public InputStream newProtobuff(Class<K> schemaClass) {	
		Schema<K> schema = RuntimeSchema.getSchema(schemaClass);			
		return null;		
	}
	
}