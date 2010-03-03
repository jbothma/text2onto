/**
 * 
 */
package org.ontoware.text2onto.linguistic;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

import org.ontoware.text2onto.util.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Sergi
 *
 */
public class Lemmatizer extends AbstractLanguageAnalyser implements Serializable {
 
	private String m_sLemmatizerScript;
	private File m_temp;
	private String[] m_lemmatizer;
	private AnnotationSet m_asAnnotationSet;
	long m_lNextOffset;
	int m_iIndexLemma;

 
	public String getLemma( String word ) throws Exception {
		if ( m_sLemmatizerScript == null ) {
			setLemmatizerScript( Settings.get( Settings.TAGGER_DIR ) + "tag-spanish.bat" );
		}
		m_temp = new File ( "temporal.txt" );
		FileWriter fwWriter = new FileWriter( m_temp );
		fwWriter.write( word );
		fwWriter.close();
		procesFile();
		String sLemma = getLemma( 0 );
		return sLemma;		
	}
	
	public String getLemmatizerScript() {
		return m_sLemmatizerScript;
	}

	public void setLemmatizerScript( String sLemmatizerScript ) {
		m_sLemmatizerScript = sLemmatizerScript;
	}
	  
	public Resource init() throws ResourceInstantiationException {
		return this;
	}

	public void execute() throws ExecutionException{
		m_asAnnotationSet = getDocument().getAnnotations().get( TOKEN_ANNOTATION_TYPE );
		AnnotationSet asAnnotation= null;	
		Annotation aAnnotation = null;
		FileWriter fwWriter;
		m_lNextOffset = 0;
		m_iIndexLemma = 0;
		Iterator iteratorAnnotation;
		m_temp=new File( "temporal.txt" );
		System.out.println( "\nLemmatizer.execute()--> Start function over " + getDocument().getName() );
		try {
			fwWriter = new FileWriter( m_temp );
			fwWriter.write( getDocument().getContent().toString() );
			fwWriter.close(); 
			procesFile();
			asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );
			while( asAnnotation != null && m_iIndexLemma < m_lemmatizer.length ) {
				iteratorAnnotation = asAnnotation.iterator();
				aAnnotation = (Annotation)iteratorAnnotation.next();
				LemmaAssignation ( aAnnotation );
				asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );			
			}
			m_temp.delete();
			System.out.println( "\nLemmatizer.execute()-->Finish function over " + getDocument().getName() );
			} 
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
			
	private void LemmaAssignation ( Annotation annotation ) throws Exception {
		int iLengthLemmaWord, iLengthAnnotationWord;
		String sLemmaWord, sAnnotationWord;
		String[] asLemmaWord;
		AnnotationSet asAnnotation;
		Iterator iteratorAnnotation;
		Annotation aAnnotationAux;
		if ( getWord( m_iIndexLemma ).compareTo( (String)annotation.getFeatures().get( "string" ) ) == 0 ) {
			annotation.getFeatures().put( "lemma", getLemma(m_iIndexLemma) );
			m_lNextOffset = annotation.getEndNode().getOffset();
			m_iIndexLemma++;
		}
		else {
			iLengthLemmaWord = getWord( m_iIndexLemma ).length();
			iLengthAnnotationWord = ((String) annotation.getFeatures().get( "string" )).length();
			if ( iLengthLemmaWord <= iLengthAnnotationWord ) {
				m_iIndexLemma++;
				if ( ((String)annotation.getFeatures().get( "kind" )).compareTo( "punctuation" ) == 0 ) {
					m_lNextOffset = annotation.getEndNode().getOffset();
				}
			}
			else if ( iLengthLemmaWord > iLengthAnnotationWord ) {
				sLemmaWord = getWord( m_iIndexLemma );
				sAnnotationWord = (String) annotation.getFeatures().get( "string" );
				asLemmaWord= sLemmaWord.split( " " );
				if ( asLemmaWord.length == 1 ){
					annotation.getFeatures().put( "lemma", getLemma( m_iIndexLemma ));
					m_lNextOffset = annotation.getEndNode().getOffset();
					while ( sLemmaWord.compareTo( sAnnotationWord ) != 0 && sAnnotationWord.length() <= sLemmaWord.length()) {
						asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );
						iteratorAnnotation = asAnnotation.iterator();
						aAnnotationAux = (Annotation)iteratorAnnotation.next();
						sAnnotationWord = sAnnotationWord + (String)aAnnotationAux.getFeatures().get( "string" );
						if ( sAnnotationWord.length() > sLemmaWord.length() ||
								sAnnotationWord.length() == sLemmaWord.length() && sLemmaWord.compareTo( sAnnotationWord ) != 0 ) {
							continue;
						}
						m_lNextOffset = aAnnotationAux.getEndNode().getOffset();
						aAnnotationAux.getFeatures().put( "lemma", getLemma( m_iIndexLemma ));		
					}
					m_iIndexLemma++;
				}
				else {
					annotation.getFeatures().put( "lemma", getLemma( m_iIndexLemma ));
					m_lNextOffset = annotation.getEndNode().getOffset();
					while ( sLemmaWord.compareTo( sAnnotationWord ) != 0 ) {
						asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );
						iteratorAnnotation = asAnnotation.iterator();
						aAnnotationAux = (Annotation)iteratorAnnotation.next();
						m_lNextOffset = aAnnotationAux.getEndNode().getOffset();
						sAnnotationWord = sAnnotationWord + " " + aAnnotationAux.getFeatures().get( "string" );
						aAnnotationAux.getFeatures().put( "lemma", getLemma( m_iIndexLemma ));					
					}
					m_iIndexLemma++;
				}
			}
		}
	}

	private void procesFile () throws Exception {
		//System.out.println( "\nLemmatizer.procesFile()-->" + m_sLemmatizerScript+ " runpath " + m_temp.getAbsolutePath() );
		Process process = Runtime.getRuntime().exec( m_sLemmatizerScript +" "+m_temp.getAbsolutePath() );  
		InputStream in = process.getInputStream();
		int numLine = 0;
		int c;
		StringBuffer sb = new StringBuffer();
		while( ( c = in.read() ) != -1 ){
			if ((char)c == '\n') {
				numLine++;
			}
			sb.append( (char)c );
		}
		in.close();		
		m_lemmatizer = sb.toString().split( "\n" );	
	}
	
	private String getTag(int line) {
		String sLine = m_lemmatizer[line];
		String[] asLine = sLine.split( "\u0009" );
		return asLine[1];
	}
	
	private String getWord(int line) {
		String sLine = m_lemmatizer[line];
		String[] asLine = sLine.split( "\u0009" );
		return asLine[0];
	}
	
	private String getLemma(int line) {
		String sLine = m_lemmatizer[line];
		String[] asLine = sLine.split( "\u0009" );
		String sLemma = asLine[2].substring( 0, asLine[2].length() - 1 );
		return sLemma;
	}
	
	public ArrayList<String[]> getWordsLemmas( String word ) throws Exception {
		ArrayList alResult = new ArrayList();
		if ( m_sLemmatizerScript == null ) {
			setLemmatizerScript( Settings.get( Settings.TAGGER_DIR ) + "tag-spanish.bat" );
		}
		m_temp = new File ( "temporal.txt" );
		FileWriter fwWriter = new FileWriter( m_temp );
		fwWriter.write( word );
		fwWriter.close();
		procesFile();
		for ( int i = 0; i < m_lemmatizer.length; i++ ) {
			String[] asWordLemma = new String[2];
			asWordLemma[0] = getWord( i );
			asWordLemma[1] = getLemma( i );
			alResult.add(asWordLemma);
		}
		return alResult;		
	}
}
