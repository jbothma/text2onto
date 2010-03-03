package org.ontoware.text2onto.persist;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.security.AccessController;
import gate.security.Group;
import gate.security.SecurityInfo;
import gate.security.Session;
import gate.security.User;

import java.net.URL;
import java.util.List;

import org.ontoware.text2onto.corpus.AbstractDocument;

public class GateDBDataStore extends GateDataStore {

	public final static int ORACLE = 1;

	public final static int POSTGRES = 2;
	
	// Marwane Modification
	public final static int NOTHING = 3;

	/* nachfolgender GATE_USER ist voreingetragen in der Tabelle t_user */
	private String GATE_USER = "ADMIN";

	private String GATE_GROUP = "ADMINS";

	private String GATE_PASSWORD = "sesame";

	private String POSTGRES_DATASTORE_URL = "jdbc:postgresql://localhost/database?user=postgres";

	private User m_usr;

	private Group m_grp;

	private AccessController m_ac;

	private Session m_usrSession;

	private SecurityInfo m_si;

	private Corpus m_tc;

	public GateDBDataStore( int iType ) throws Exception {
		if( iType == POSTGRES ) {
			m_dataStore = Factory.openDataStore( "gate.persist.PostgresDataStore", POSTGRES_DATASTORE_URL );
			connect( POSTGRES_DATASTORE_URL );
		}
		else if( iType == ORACLE ) {
		}
		// Marwane Modification
		else if (iType == NOTHING){
			
		}
		// Marwane Modification
//		m_dataStore.open();
//		m_tc = Factory.newCorpus( "testcorpus" );
//		m_persistCorpus = (Corpus)m_dataStore.adopt( m_tc, m_si );
	}

	public void connect( String sUrl ) throws Exception {

		m_ac = Factory.createAccessController( sUrl );
		m_ac.open();

		// login and get session
		m_usr = m_ac.findUser( GATE_USER );
		m_grp = m_ac.findGroup( GATE_GROUP );
		m_usrSession = m_ac.login( m_usr.getName(), GATE_PASSWORD, m_grp.getID() );

		// m_ac.setSessionTimeout( m_usrSession, 60 );

		// use this session for all consequent operations with the datastore
		m_dataStore.setSession( m_usrSession );
		m_si = new SecurityInfo( SecurityInfo.ACCESS_WR_GW, m_usr, m_grp );
	}

	/**
	 * saves corpus to datastore and updates persistCorpus
	 * 
	 * @throws Exception
	 */
	public void syncSerialDataStore( Corpus corpus ) throws Exception {
		// List<String> lNames = corpus.getDocumentNames();
		// for (String sName : lNames) {
		// addDocument(sName);
		// }
		// m_dataStore.sync(m_persistCorpus);
		// m_persistCorpus.sync();
	}

	private void addDocument( URL url ) throws Exception {
		Document transDoc = Factory.newDocument( url );
		Document persistDoc = (Document)this.m_dataStore.adopt( transDoc, m_si );
		Factory.deleteResource( transDoc );
		m_persistCorpus.add( persistDoc );
		Factory.deleteResource( persistDoc );
	}

	public void syncDBDataStore( List lDocs ) throws Exception {
		for( int i = 0; i < lDocs.size(); i++ ) {
			AbstractDocument doc = (AbstractDocument)lDocs.get( i );
			addDocument( doc.getURI().toURL() );
		}
		m_dataStore.sync( m_persistCorpus );
		m_persistCorpus.sync();
	}
}
