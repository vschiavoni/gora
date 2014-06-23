package org.apache.gora.infinispan.store;

import java.io.IOException;
import java.io.InputStream;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.RawProtobufMarshaller;
import org.infinispan.protostream.SerializationContext;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public class ProtobufMarshallerFactory<K> {

	public static <M> BaseMarshaller<M> newMarshaller(final Class<M> clazz) {

		BaseMarshaller<M> marshaller = new RawProtobufMarshaller<M>() {

			private Schema<M> theSchema = RuntimeSchema.getSchema(clazz);

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
				
				M theMessage = theSchema.newMessage();
				
//				byte[] buffer = in.readRawBytes(LinkedBuffer.DEFAULT_BUFFER_SIZE);
//
//				ProtobufIOUtil.mergeFrom(buffer, theMessage, theSchema);
				return theMessage;
			}

			@Override
			public void writeTo(SerializationContext ctx,
					CodedOutputStream out, M t) throws IOException {
							
//				LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
//				try {
//					byte[] protostuff = ProtobufIOUtil.toByteArray(t,this.theSchema, buffer);
//					out.writeRawBytes(protostuff);
//				} finally {
//					buffer.clear();
//				}
			}
		};

		return marshaller;
	}

//    private static <M> byte[] newProtobinDecriptor(final Class<M> clazz) {
//
//        Schema<M> theSchema = RuntimeSchema.getSchema(clazz);
//
//        for(int i=0;;i++){
//            String fieldName = theSchema.getFieldName(i);
//            if(fieldName!=null)
//
//
//        }
//
//
//
//    }


}