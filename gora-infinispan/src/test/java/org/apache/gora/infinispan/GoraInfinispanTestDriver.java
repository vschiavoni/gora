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

/**
 * @author valerio schiavoni
 *
 */

package org.apache.gora.infinispan;

import org.apache.gora.GoraTestDriver;
import org.apache.gora.infinispan.store.InfinispanStore;

import java.io.File;


// Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for third party tests using gora-infinispan backend. 
 * @see GoraTestDriver for test specifics.
 * This driver is the base for all test cases that require an embedded Infinispan
 * server. 
 * It starts (setUp) and stops (tearDown) embedded Infinispan server.
 * 

 */

public class GoraInfinispanTestDriver extends GoraTestDriver {
  private static Logger log = LoggerFactory.getLogger(GoraInfinispanTestDriver.class);
  

  public GoraInfinispanTestDriver() {
    super(InfinispanStore.class);
  }
	
  /**
   * Starts embedded Cassandra server.
   *
   * @throws Exception
   * 	if an error occurs
   */
  @Override
  public void setUpClass() throws Exception {
    super.setUpClass();
    log.info("Starting embedded Infinispan Server...");   
      // cleanup
      tearDownClass();
    
  }

  /**
   * Stops embedded Cassandra server.
   *
   * @throws Exception
   * 	if an error occurs
   */
  @Override
  public void tearDownClass() throws Exception {
    super.tearDownClass();
    
  }  


 
}
