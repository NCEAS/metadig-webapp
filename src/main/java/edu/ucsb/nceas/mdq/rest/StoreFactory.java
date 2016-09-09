package edu.ucsb.nceas.mdq.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ucsb.nceas.mdqengine.MDQStore;
import edu.ucsb.nceas.mdqengine.model.Check;
import edu.ucsb.nceas.mdqengine.store.InMemoryStore;

public class StoreFactory {
	
	private static Log log = LogFactory.getLog(StoreFactory.class);

	private static MDQStore store = null;
	
	public static MDQStore getStore() {
		if (store == null) {
			log.debug("Creating new MDQ store");
			// for now, just use in-memory version
			store = new InMemoryStore();
			
			// add some dumy checks
//			Check check = new Check();
//			check.setId("testingCheck.1.1");
//			store.createCheck(check);
//			check = new Check();
//			check.setId("testingCheck.2.1");
//			store.createCheck(check);
		}
		return store;
	}
}
