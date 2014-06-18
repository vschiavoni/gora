package org.apache.gora.infinispan.store;

import java.io.IOException;
import java.io.InputStream;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.RawProtobufMarshaller;
import org.infinispan.protostream.SerializationContext;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public  class ProtobufMarshallerFactory<K>{
	
	public static <M> BaseMarshaller<M> newMarshaller(final Class<M> clazz) {
		
		BaseMarshaller<M> marshaller = new RawProtobufMarshaller<M>() {

			@Override
			public Class<? extends M> getJavaClass() {
				return clazz;
			}

			@Override
			public String getTypeName() {
				return clazz.getName();
			}

			@Override
			public M readFrom(SerializationContext ctx, CodedInputStream in)
					throws IOException {				
				return null;
			}

			@Override
			public void writeTo(SerializationContext ctx,
					CodedOutputStream out, M t) throws IOException {
				// TODO Auto-generated method stub
				
			}
		};
		
		return marshaller;		
	}
	
	public InputStream newProtobuf(Class<K> schemaClass) {	
		Schema<K> schema = RuntimeSchema.getSchema(schemaClass);			
		return null;		
	}
	
}