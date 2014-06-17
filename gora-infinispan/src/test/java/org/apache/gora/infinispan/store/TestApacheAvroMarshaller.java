package org.apache.gora.infinispan.store;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.gora.examples.generated.Employee;
import org.infinispan.client.hotrod.marshall.ApacheAvroMarshaller;
import org.infinispan.commons.CacheException;
import org.junit.Before;
import org.junit.Test;

public class TestApacheAvroMarshaller {

	private ApacheAvroMarshaller marsh;
	private Employee e1;
	
	@Before
	public void setUp() throws Exception {
		marsh = new ApacheAvroMarshaller();
		this.e1  = new Employee();
	}
	
	@Test
	public void testMarshallString() throws IOException, InterruptedException {
		assertNotNull(marsh.objectToByteBuffer("hello"));
	}

	@Test
	public void testMarshallEmployee() throws IOException, InterruptedException {
		try{
			assertNotNull(marsh.objectToByteBuffer(e1)) ;
		}
		catch(CacheException ce){
			String errorMessage = ce.getLocalizedMessage();
			System.out.println(errorMessage);
			assertEquals(errorMessage, "Unsupported type: class org.apache.gora.examples.generated.Employee");
		}
	}

}
