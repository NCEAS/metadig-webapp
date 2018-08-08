package edu.ucsb.nceas.mdq.rest;

import edu.ucsb.nceas.mdqengine.exception.MetadigException;
import edu.ucsb.nceas.mdqengine.exception.MetadigStoreException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ucsb.nceas.mdqengine.MDQStore;
import edu.ucsb.nceas.mdqengine.model.Check;
import edu.ucsb.nceas.mdqengine.store.DatabaseStore;
import edu.ucsb.nceas.mdqengine.store.InMemoryStore;

import java.sql.SQLException;

public class StoreFactory {
	
	private static Log log = LogFactory.getLog(StoreFactory.class);

	private static MDQStore store = null;

	public static MDQStore getStore(boolean persist) {
		if (store == null) {
			if (persist) {
				log.debug("Creating new MDQ persistent store");
				try {
					store = new DatabaseStore();
				} catch (SQLException e | ClassNotFoundException e) {
					e.printStackTrace();
					MetadigException me = new MetadigStoreException("Unable to create the database store.");
					me.initCause(e);
				}
			} else {
				log.debug("Creating new MDQ store");
				store = new InMemoryStore();
			}

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
