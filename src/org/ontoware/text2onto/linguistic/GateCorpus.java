package org.ontoware.text2onto.linguistic;

import java.io.Serializable;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.AnnotationSet;
import gate.creole.ExecutionException;
import gate.creole.SerialAnalyserController;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;
/* 
import org.ontoware.text2onto.persist.GateDBDataStore;
import org.ontoware.text2onto.persist.GateDataStore;
import org.ontoware.text2onto.persist.GateSerialDataStore;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.corpus.TextDocument;
*/
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.persist.GateDataStore;
import org.ontoware.text2onto.persist.GateSerialDataStore;
import org.ontoware.text2onto.persist.GateDBDataStore;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class GateCorpus implements Serializable {

	private transient GateDataStore m_dataStore;

	private transient gate.Corpus m_corpus;

	private HashMap m_hmDoc2GateDoc;

	private HashMap m_hmGateDoc2Doc;

	private String m_sEncoding = "UTF-8";
	
	private ArrayList m_lNewAddedDocs;
 
 
	protected GateCorpus( GateDataStore dataStore ) throws ResourceInstantiationException {
		m_dataStore = dataStore;
		m_corpus = Factory.newCorpus( "GateCorpus" );
		m_hmDoc2GateDoc = new HashMap();
	}

	protected void setEncoding( String sEncoding ) {
		m_sEncoding = sEncoding;
	}

	protected void addDocument( AbstractDocument doc ) throws ResourceInstantiationException, MalformedURLException {
		System.out.println( "GateCorpus.addDocument: "+ doc );
		Document gateDoc = createGateDocument( doc );
		m_corpus.add( gateDoc );
		m_hmDoc2GateDoc.put( doc, gateDoc );
	}

	private Document createGateDocument( AbstractDocument doc ) throws ResourceInstantiationException, MalformedURLException {
		URI uri = doc.getURI();
		Document gateDoc = null; 
		String content = doc.getContent(); 
		if( uri != null ) {
			gateDoc = Factory.newDocument( uri.toURL() );
		}
		else {
			gateDoc = Factory.newDocument( content );
		}
		((gate.corpora.DocumentImpl)gateDoc).setEncoding( m_sEncoding );
		return gateDoc;
	}
 
	protected void apply( SerialAnalyserController application, List transducers, List changes ) throws Exception {
		System.out.print( "GateCorpus: preprocessing documents ( " );
		m_lNewAddedDocs = new ArrayList();
		Iterator iter = changes.iterator();
		while( iter.hasNext() ) {
			Change change = (Change)iter.next();
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			if( iChange == Change.Type.ADD ) // || change instanceof Modify )
			{
				m_lNewAddedDocs.add( doc );
				System.out.print( doc + " " );
			}
			else if( iChange == Change.Type.REMOVE ) {
				m_corpus.remove( getGateDocument( doc ) );
				m_hmGateDoc2Doc.remove( getGateDocument( doc ) );
				m_hmDoc2GateDoc.remove( doc );
			}
		}		
		System.out.println( ")..." );
		for( int i = 0; i < m_lNewAddedDocs.size(); i++ ) {
			AbstractDocument doc = (AbstractDocument)m_lNewAddedDocs.get( i );
			processDocument( application, transducers, doc );
		}
	}

	private void processDocument( SerialAnalyserController application, List transducers, AbstractDocument doc )
			throws Exception {
		Document gateDoc = getGateDocument( doc );
		if( gateDoc == null ) {
			gateDoc = createGateDocument( doc );
		}
		Corpus tempCorpus = Factory.newCorpus( "tempCorpus" );
		tempCorpus.add( gateDoc );
		application.setCorpus( tempCorpus );
		application.execute();

		Iterator iter = transducers.iterator();
		while( iter.hasNext() ) {
			Transducer trans = (Transducer)iter.next();
			trans.setDocument( gateDoc );
			trans.execute();
		}
		m_corpus.add( gateDoc );
		m_hmDoc2GateDoc.put( doc, gateDoc );
	}

	private List getGateDocuments() {
		ArrayList al = new ArrayList();
		Iterator iter = m_hmDoc2GateDoc.values().iterator();
		while( iter.hasNext() ) {
			al.add( iter.next() );
		}
		return al;
	}

	private Document getGateDocument( AbstractDocument doc ) {
		Iterator iter = m_hmDoc2GateDoc.keySet().iterator();
		while( iter.hasNext() ) 
		{
			AbstractDocument d = (AbstractDocument)iter.next();
			if( d.equals( doc ) ) {
				return (Document)m_hmDoc2GateDoc.get( d );
			}
		}
		return null;
	}

	protected AnnotationSet getAnnotations( AbstractDocument doc ) {
		Document gateDoc = getGateDocument( doc );
		if( gateDoc != null ) {
			return gateDoc.getAnnotations();
		}
		return null;
	}

	protected AnnotationSet getAnnotations( AbstractDocument doc, Long lStart, Long lEnd ) {
		Document gateDoc = getGateDocument( doc );
		if( gateDoc != null ) {
			return gateDoc.getAnnotations().get( lStart, lEnd );
		}
		return null;
	}

	protected String getDocumentContent( Long lStart, Long lEnd, AbstractDocument doc ) throws InvalidOffsetException {
		Document gateDoc = getGateDocument( doc );
		return gateDoc.getContent().getContent( lStart, lEnd ).toString();
	}

	private Corpus getCorpus() {
		return m_corpus;
	}

	protected void save() throws Exception {
		if( m_dataStore instanceof GateSerialDataStore ) {
			( (GateSerialDataStore)m_dataStore ).syncSerialDataStore( m_corpus );
		}
		else {
			( (GateDBDataStore)m_dataStore ).syncDBDataStore( m_lNewAddedDocs );
		}
	}
	
	public String toString() {
		String s = "GateCorpus: ";
		Iterator iter = getGateDocuments().iterator();
		while( iter.hasNext() ) 
		{
			Document doc = (Document)iter.next();
			boolean bAnn = doc.getAnnotations().size() > 0;
			s += doc.getSourceUrl() + " (" + bAnn + ") ";
		}
		return s;
	}
}