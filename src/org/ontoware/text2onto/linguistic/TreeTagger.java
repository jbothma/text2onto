package org.ontoware.text2onto.linguistic;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

import java.io.*;
import java.net.URL; 
import java.util.Iterator;

import org.ontoware.text2onto.util.Settings;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class TreeTagger extends AbstractLanguageAnalyser implements Serializable {
 
	private String m_sTaggerScript;
	private File m_temp;
	private String[] m_tagger;
	private AnnotationSet m_asAnnotationSet;
	long m_lNextOffset;
	int m_iIndexTagger;

 
	public String getTaggerScript() {
		return m_sTaggerScript;
	}

	public void setTaggerScript( String sTaggerScript ) {
		m_sTaggerScript = sTaggerScript;
	}
	  
	public Resource init() throws ResourceInstantiationException {
		return this;
	}

	public void execute() throws ExecutionException { 
		m_asAnnotationSet = getDocument().getAnnotations().get( TOKEN_ANNOTATION_TYPE );
		AnnotationSet asAnnotation = null;	
		Annotation aAnnotation = null;
		m_lNextOffset = 0;
		m_iIndexTagger = 0;
		Iterator iteratorAnnotation;
		m_temp = new File( "temporal.txt" ); 
		System.out.println( "\nTreeTagger.execute()--> Start function over " + getDocument().getName() );
		try {
			FileWriter fwWriter = new FileWriter( m_temp );
			fwWriter.write( getDocument().getContent().toString() );
			fwWriter.close(); 
			procesFile();
			asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );
			while( asAnnotation != null && m_iIndexTagger < m_tagger.length ) {
				iteratorAnnotation = asAnnotation.iterator();
				aAnnotation = (Annotation)iteratorAnnotation.next();
				tagAssignation ( aAnnotation );
				asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );			
			}
			m_temp.delete();
			System.out.println( "\nTreeTagger.execute()-->End function over " + getDocument().getName() );
		} 
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
			
	private void tagAssignation ( Annotation annotation ) throws Exception {
		int iLengthTagWord, iLengthAnnotationWord;
		String sTagWord, sAnnotationWord;
		String[] asTagWord;
		AnnotationSet asAnnotation;
		Iterator iteratorAnnotation;
		Annotation aAnnotationAux;
		
		if ( getWord( m_iIndexTagger ).compareTo( (String)annotation.getFeatures().get( "string" ) ) == 0 ) {
			annotation.getFeatures().put( "pos", getTag( m_iIndexTagger ) );
			m_lNextOffset = annotation.getEndNode().getOffset();
			m_iIndexTagger++;
		}
		else {
			iLengthTagWord = getWord( m_iIndexTagger ).length();
			iLengthAnnotationWord = ((String) annotation.getFeatures().get( "string" )).length();
			if ( iLengthTagWord <= iLengthAnnotationWord ) {
				m_iIndexTagger++;
				if ( ((String)annotation.getFeatures().get( "kind" )).compareTo( "punctuation" ) == 0 ) {
					m_lNextOffset = annotation.getEndNode().getOffset();
				}
			}
			else if ( iLengthTagWord > iLengthAnnotationWord ) {
				sTagWord = getWord( m_iIndexTagger );
				sAnnotationWord = (String) annotation.getFeatures().get( "string" );
				asTagWord = sTagWord.split( " " );
				if ( asTagWord.length == 1 ){
					annotation.getFeatures().put( "pos", getTag( m_iIndexTagger ));
					m_lNextOffset = annotation.getEndNode().getOffset();
					while ( sTagWord.compareTo( sAnnotationWord ) != 0 && sAnnotationWord.length() <= sTagWord.length()) {
						asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );
						iteratorAnnotation = asAnnotation.iterator();
						aAnnotationAux = (Annotation)iteratorAnnotation.next();
						sAnnotationWord = sAnnotationWord + (String)aAnnotationAux.getFeatures().get( "string" );
						if ( sAnnotationWord.length() > sTagWord.length() ||
								sAnnotationWord.length() == sTagWord.length() && sTagWord.compareTo( sAnnotationWord ) != 0 ) {
							continue;
						}
						m_lNextOffset = aAnnotationAux.getEndNode().getOffset();
						aAnnotationAux.getFeatures().put( "pos", getTag( m_iIndexTagger ));		
					}
					m_iIndexTagger++;
				}
				else {
					annotation.getFeatures().put( "pos", getTag( m_iIndexTagger ));
					m_lNextOffset = annotation.getEndNode().getOffset();
					while ( sTagWord.compareTo( sAnnotationWord ) != 0 ) {
						asAnnotation = m_asAnnotationSet.get( (long)m_lNextOffset );
						iteratorAnnotation = asAnnotation.iterator();
						aAnnotationAux = (Annotation)iteratorAnnotation.next();
						m_lNextOffset = aAnnotationAux.getEndNode().getOffset();
						sAnnotationWord = sAnnotationWord + " " + aAnnotationAux.getFeatures().get( "string" );
						aAnnotationAux.getFeatures().put( "pos", getTag( m_iIndexTagger ));					
					}
					m_iIndexTagger++;
				}
			}
		}
	}

	private void procesFile () throws Exception {
		//System.out.println( "\nTreeTagger.procesFile() : " + m_sTaggerScript + " " + m_temp.getAbsolutePath() );
		Process process = Runtime.getRuntime().exec( m_sTaggerScript + " " + m_temp.getAbsolutePath() );  
		InputStream in = process.getInputStream();
		int numLine=0;
		int c;
		StringBuffer sb = new StringBuffer();
		while( ( c = in.read() ) != -1 ) {
			if ((char)c == '\n' ) {
				numLine++;
			}
			sb.append( (char)c );
		}
		in.close();		
		m_tagger=sb.toString().split( "\n" );	
	}
	
	private String getTag(int line) {
		String sLine = m_tagger[line];
		String[] vsLine = sLine.split( "\u0009" );
		return vsLine[1];
	}
	
	private String getWord(int line) {
	    String sLine = m_tagger[line];
		String[] vsLine = sLine.split( "\u0009" );
		return vsLine[0];
	}
}