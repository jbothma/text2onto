 package org.ontoware.text2onto.linguistic;

import gate.Document; 
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ResourceInstantiationException;
import gate.creole.ExecutionException;
import gate.jape.Batch;
import gate.jape.JapeException; 
	
import java.io.File;
import java.net.URL;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class Transducer extends AbstractLanguageAnalyser {
 
	private URL m_grammarURL;
 
	protected Batch m_batch;
 
	private String m_encoding;
 
	private String m_inputASName;
 
	private String m_outputASName;

	private Document m_document;

	private boolean m_interrupted;
		 

	public void setDocument( Document document ) {
		m_document = document;
	}
 
	public Document getDocument() {
		return m_document;
	}  
 
	public void setGrammarURL( URL grammarURL ){
		m_grammarURL = grammarURL;
	}
 
	public URL getGrammarURL() {
		return m_grammarURL;
	}
 
	public void setEncoding( String encoding ) {
		m_encoding = encoding;
	}
 
	public String getEncoding() {
		return m_encoding;
	}
 
	public void setInputASName( String inputASName ) {
		m_inputASName = inputASName;
	}
 
	public String getInputASName() {
		return m_inputASName;
	}
 
	public void setOutputASName( String outputASName ) {
		m_outputASName = outputASName;
	}
 
	public String getOutputASName() {
		return m_outputASName;
	}

 
	public Resource init() throws ResourceInstantiationException {
		if( m_grammarURL != null && m_encoding != null )
		{       
			try { 
				m_batch = new Batch( m_grammarURL, m_encoding );
			} 
			catch( JapeException je ){
				throw new ResourceInstantiationException( je );
			}
		} else {
			throw new ResourceInstantiationException( "missing parameters" );
		}  
		return this;
	}
 
	public void execute() throws ExecutionException {
		m_interrupted = false;
		if( m_document == null ){
			throw new ExecutionException( "no document" );
		}
		if( m_inputASName != null && m_inputASName.equals( "" ) ){
			m_inputASName = null;
		}
		if( m_outputASName != null && m_outputASName.equals( "" ) ){
			m_outputASName = null;
		}
		try {
			m_batch.transduce( m_document,
				m_inputASName == null ?
					m_document.getAnnotations() :
					m_document.getAnnotations( m_inputASName ),
				m_outputASName == null ?
					m_document.getAnnotations() :
					m_document.getAnnotations( m_outputASName ) ); 
		} 
		catch( JapeException je ){
			throw new ExecutionException( je );
		}
	}
 
	public synchronized void interrupt(){
		m_interrupted = true;
		m_batch.interrupt();
	}
}
