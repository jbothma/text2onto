package org.ontoware.text2onto.util.google;

import java.util.HashMap;

import org.ontoware.text2onto.util.Settings;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;
import com.google.soap.search.GoogleSearchResultElement;


public class GoogleWrapper {

	public final static String SUCH_AS = "such as";
	public final static String IS_A = "is a";
	public final static String COMO = "como";
	public final static String ES_UN = "es un";
	public final static String ES_UNA = "es una";
	public final static String WIE = "wie";
	
    private String m_query	= null;
    private int    m_initialResults	= 0;
    private int	   m_maxResults	= 10;
    private GoogleSearch m_service = null;
    private GoogleSearchResult m_result = null;

    
	public GoogleWrapper() {
		m_service = new GoogleSearch();	
	}
	
	public void init() {
		m_service.setKey( "<no key>" );
		m_service.setStartResult( m_initialResults );
		m_service.setMaxResults( m_maxResults );
	}
	
	public void setQuery( String sType, String sWord1, String sWord2 ) {
		m_query = "\"" + sWord1 + " " + sType + " " + sWord2 + "\"" ;
		m_service.setQueryString( m_query );
	}
	
	public void setQuery( String sType, String sWord ) {
		m_query = "\"" + sType + " " + sWord + "\"" ;
		m_service.setQueryString( m_query );
	}
	
	public void addToQuery( String sType, String sWord1, String sWord2 ) {
		m_query = m_query + " " + "OR" + " " + "\"" + sWord1 + " " + sType + " " + sWord2 + "\"" ;
		m_service.setQueryString( m_query );
	} 
	
	public void run( ) throws Exception {
		m_result = m_service.doSearch();
	}
	
	public int getResults() {
		int iTotalResoults = m_result.getEstimatedTotalResultsCount();
		return iTotalResoults;
	}
	
	public String getQuery () {
		return m_query.substring(1,m_query.length()-1);		
	}
	
	public void setStartResult(int firstIndex) {
		m_service.setStartResult( firstIndex );		
	}

	public HashMap<String, Integer> addResults2Hm(HashMap<String,Integer> hmConcept2Occurrences) {
		GoogleSearchResultElement[] reResultElements = m_result.getResultElements();
		for ( GoogleSearchResultElement element: reResultElements ) {
			String sSnippet = element.getSnippet().replaceAll( "<br>", "").replaceAll("<b>","").replaceAll( "</br>", "").replaceAll("</b>","").replaceAll(",","");
			String[] sSnippetDivided = sSnippet.split( getQuery() );
			String sPrePattern = sSnippetDivided[0];
			String[] sPrePatternDivided = sPrePattern.split( " " );
			if (sPrePatternDivided.length > 3) {
				for ( int i = sPrePatternDivided.length-4; i < sPrePatternDivided.length; i++) {
					if ( hmConcept2Occurrences.containsKey( sPrePatternDivided[i]) ) {
						hmConcept2Occurrences.put( sPrePatternDivided[i], hmConcept2Occurrences.get( sPrePatternDivided[i])+ 1 );
					}
					else {
						hmConcept2Occurrences.put( sPrePatternDivided[i], 1 );
					}			
				}
			}
			else {
				for ( int i = 0; i < sPrePatternDivided.length; i++) {
					if ( hmConcept2Occurrences.containsKey( sPrePatternDivided[i]) ) {
						hmConcept2Occurrences.put( sPrePatternDivided[i], hmConcept2Occurrences.get( sPrePatternDivided[i])+ 1 );
					}
					else {
						hmConcept2Occurrences.put( sPrePatternDivided[i], 1 );
					}			
				}
			}			
		}
		return hmConcept2Occurrences;
		
	}
	
	
	
}
