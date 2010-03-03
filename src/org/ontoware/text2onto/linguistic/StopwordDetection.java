package org.ontoware.text2onto.linguistic;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class StopwordDetection extends AbstractLanguageAnalyser implements Serializable {


	private HashSet m_stopwords;

	//default value is false
	private Boolean m_bTestUpperCase = new Boolean(false);

	private URL m_stopwordsURL;

	public void setTestUpperCase( Boolean bTestUpperCase ) {
		this.m_bTestUpperCase = bTestUpperCase;
	}

	public Boolean getTestUpperCase() {
		return this.m_bTestUpperCase;
	}

	public URL getStopwordsURL() {
		return this.m_stopwordsURL;
	}

	public void setStopwordsURL( URL stopwordsURL ) {
		this.m_stopwordsURL = stopwordsURL;
	}

	public Resource init() throws ResourceInstantiationException {
		m_stopwords = new HashSet();
		BufferedReader reader = null;
		try {
			File file = new File( m_stopwordsURL.getPath() );
			reader = new BufferedReader( new FileReader( file ) );
			String sLine = null;
			while ( ( sLine = reader.readLine() ) != null ) {
				m_stopwords.add( sLine.trim() );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		// finally {
		// 	reader.close();
		// }  
		return this;
	}

	public void execute() throws ExecutionException {
		AnnotationSet annSet = getDocument().getAnnotations().get( TOKEN_ANNOTATION_TYPE );
		Iterator iter = annSet.iterator();
		while ( iter.hasNext() ) {
			Annotation ann = (Annotation) iter.next();
			String sToken = (String) ann.getFeatures().get( "string" );
			ann.getFeatures().put( "stopword", new Boolean( isStopword( sToken ) ) );
		}
	}

	private boolean isStopword( String sWord ) {
		boolean bReturn = false;
		bReturn = m_stopwords.contains( sWord );
		if ( !bReturn && m_bTestUpperCase.booleanValue() ) {
			bReturn = m_stopwords.contains( sWord.toLowerCase() );
		}
		return bReturn;
	}
}