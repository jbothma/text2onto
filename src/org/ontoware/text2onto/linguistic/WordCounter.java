package org.ontoware.text2onto.linguistic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ontoware.text2onto.corpus.AbstractDocument;

import gate.Document;
import gate.Factory;

public class WordCounter {
	private ArrayList<Document> m_ArrayDocuments;
	
	public WordCounter() {
		m_ArrayDocuments = new ArrayList();
	}	
	
	public void addDocument ( AbstractDocument abDocument ) throws LinguisticException {
		URL urlDocumentURL;
		try {
			urlDocumentURL = abDocument.getURI().toURL();
			Document dDocument = Factory.newDocument( urlDocumentURL );
			m_ArrayDocuments.add(dDocument);
		}
		catch( Exception e ){
			throw new LinguisticException( "failed to add documents in wordcounter.addDocument(AbstractDocument abDocument)", e );
		}
	}
	
	public void delDocument ( ) {
		
	}
	
	public int countOcurrences ( String sWord ) {
		int iNumOcurences = 0;
		String sPattern = "";
		String[] asWord = sWord.split( " " );
				
		for ( int iIndex = 0; iIndex < asWord.length; iIndex++ ) {
			if ( asWord[iIndex].length() >= 9 ) {
				sPattern = sPattern + asWord[iIndex].substring( 0, asWord[iIndex].length()-5 )+ "[a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]?[a-zA-Zραινσϊ][a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]? ";
			}
			else if ( asWord[iIndex].length() >= 7 ) {
				sPattern = sPattern + asWord[iIndex].substring( 0, asWord[iIndex].length()-3 )+ "[a-zA-Zραινσϊ][?a-zA-Zραινσϊ][?a-zA-Zραινσϊ][?a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]? ";
			}
			else if ( asWord[iIndex].length() >= 5 ) {
				sPattern = sPattern + asWord[iIndex].substring( 0, asWord[iIndex].length()-2 )+ "[a-zA-Zραινσϊ]?[a-zA-Zραινσϊ][?a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]? ";
			}
			else {
				sPattern = sPattern + asWord[iIndex].substring( 0, asWord[iIndex].length() )+ "[a-zA-Zραινσϊ]?[a-zA-Zραινσϊ]? ";	
			} 
		}
		sPattern = sPattern.substring( 0, sPattern.lastIndexOf(" ") );
		Pattern pPattern = Pattern.compile( sPattern.toLowerCase() );
		for ( Document document: m_ArrayDocuments ) {
			Matcher mMatcher = pPattern.matcher( document.getContent().toString().toLowerCase() );
			while (mMatcher.find()) {
	            // Get the matching string
	            iNumOcurences++;
	        }
		}
		return iNumOcurences;
	}
	
	private String delAccents( String sWord ) {
		sWord = "αινσϊρ";
		String a = "a";
		String e = "e";
		String i = "i";
		String o = "o";
		String u = "u";
		byte[] abWord = sWord.getBytes();
		byte[] abWorda = a.getBytes();
		byte[] abWorde = e.getBytes();
		byte[] abWordi = i.getBytes();
		byte[] abWordo = o.getBytes();
		byte[] abWordu = u.getBytes();
		
		for ( int ix = 0; ix < abWord.length; ix++ ) {
			byte b = abWord[ix];
			switch (b) {
				case -31:
					b = 97;
					break;
				case -23:
					b = 101;
					break;
				case -19:
					b = 105;
					break;
				case -13:
					b = 111;
					break;
				case -6:
					b = 117;
					break;
			}				
			abWord[ix] = b;
		}
		String sReturn = new String( abWord );
		return sReturn;		
	}
}
