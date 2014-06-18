package org.apache.gora.infinispan;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.gora.examples.generated.Employee;
import org.junit.Before;
import org.junit.Test;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufOutput;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class ProtostuffRuntimeTest {

	Employee e1;
	@Before
	public void setUp() throws Exception {
		this.e1 = new Employee();
		this.e1.setName("John");
		this.e1.setSalary(1000);
		this.e1.setSsn("ACME101");
	}

	@Test
	public void testSchemaEmptyEmployee() {
		Schema<Employee> schema = RuntimeSchema.getSchema(Employee.class);	
		assertNotNull(schema);
		System.out.println("Name: "+schema.messageName());
		System.out.println("FullName: "+schema.messageFullName());
		System.out.println(schema.newMessage());
	}
	
	@Test
	public void testSchemaEmployee() throws IOException {
		Schema<Employee> schema = RuntimeSchema.getSchema(Employee.class);	
		assertNotNull(schema);
		
		ProtobufOutput output = new ProtobufOutput(LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
		//new BufferedOutputStream( new FileOutputStream("output-file.proto"));
		schema.writeTo(output, e1);
		//LinkedBuffer.writeTo(new BufferedOutputStream( new FileOutputStream("output-file.proto")));
	}

}
