//package com.pramari.emulator.server.comm;

import java.util.HashMap;

//import com.pramari.emulator.server.reader.Reader;
//import com.pramari.emulator.server.reader.ReaderManager;

import junit.framework.TestCase;

public class CommManagerTest extends TestCase {

	CommManager commmanager = null;
	HashMap propertyList = null;
	
//	public CommManager createInstance() throws Exception {
//	    return new CommManager();
//	}
//
//	public CommManagerTest(String arg0) {
//		super(arg0);
//	}
//
//	protected void setUp() throws Exception {
//		commmanager = createInstance();
//		super.setUp();
//	}
//
//	protected void tearDown() throws Exception {
//		commmanager = null;
//		super.tearDown();
//	}
//
//	public void testGetStatus() {
//		Communication test = commmanager.createComm();
//		propertyList = test.getStatus();
//		assertEquals("COM", propertyList.get("type"));
//		commmanager.deleteComm((String)propertyList.get("ID"));
//	}
//	
//	public void testCreateReader() {
//		Communication test = commmanager.createComm(commtype);
//		propertyList = test.getStatus();
//		assertNotNull(propertyList.get("ID"));
//		commmanager.deleteComm((String)propertyList.get("ID"));
//	}
	
	public void testDeleteReader() {
		Communication test = commmanager.createComm(commtype);
		assertNotNull(test.getStatus().get("ID"));
		commmanager.deleteComm((String)test.getStatus().get("ID"));
		assertNull();
	}
	
//	public void testReadersinList() {
//		Communication test = commmanager.createComm(commtype);
//		test = commmanager.createComm("COM");
//		assertEquals(2, CommManager.listComms());
//	}
}
