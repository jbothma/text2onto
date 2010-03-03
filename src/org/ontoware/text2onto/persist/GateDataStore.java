package org.ontoware.text2onto.persist;

import java.util.List;

import gate.Corpus;
import gate.DataStore;

public class GateDataStore {

	protected DataStore m_dataStore;

	protected Corpus m_persistCorpus;

	protected String m_sUrl;
	
	public final static int SERIAL = 0;

	public final static int ORACLE = 1;

	public final static int POSTGRES = 2;
	
	// Marwane Modification
	public final static int NOTHING = 3;

	public void setUrl( String sUrl ) {
		m_sUrl = sUrl;
	}
}