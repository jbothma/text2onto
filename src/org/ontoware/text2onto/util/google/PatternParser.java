package org.ontoware.text2onto.util.google;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author    gl
 * @created   11. Februar 2005
 */
public class PatternParser extends DefaultHandler
{
	private boolean insidePattern, insideArgument, insideArgumentAttribute;
	private boolean insideGlue;
	private String currentArgumentAttribute;
	private String currentArgument;
	private String currentChars;
	private SearchPattern currentPattern;

	private ArrayList m_patterns;
	private File m_patternFile;

	
	public PatternParser( File file ) {
		m_patternFile = file;
	}

	public void startDocument() {
		insidePattern = insideArgument = insideArgumentAttribute = insideGlue = false;
		m_patterns = new ArrayList();
	}

	public void startElement( String uri, String localName, String qName, Attributes attributes ) {
		if( qName.equals( "patterns" ) ){
			return;
		}
		if( qName.equals( "pattern" ) )
		{
			String type = attributes.getValue( "type" );
			currentPattern = new SearchPattern( type );
			m_patterns.add( currentPattern );
			insidePattern = true;
			return;
		}
		if( qName.equals( "glue" ) )
		{
			insideGlue = true;
			return;
		}
		// must be argument or argument attribute now
		if( insideArgument )
		{
			currentArgumentAttribute = qName;
			insideArgumentAttribute = true;
		}
		else if( insidePattern )
		{
			currentPattern.addArgument( qName );
			currentArgument = qName;
			insideArgument = true;
		}
	}

	public void endElement( String uri, String localName, String qName ) {
		if( insideArgumentAttribute )
		{
			assert qName.equals(currentArgumentAttribute);
			currentPattern.setArgumentAttribute( currentArgument, currentArgumentAttribute, currentChars );
			insideArgumentAttribute = false;
			return;
		}
		if( insideArgument )
		{
			assert qName.equals(currentArgument);
			insideArgument = false;
			return;
		}
		if( insideGlue )
		{
			currentPattern.addGlue( currentChars );
			insideGlue = false;
			return;
		}
		if( insidePattern )
		{
			insidePattern = false;
		}
	}

	public void characters( char[] ch, int start, int length ) {
		currentChars = new String( ch, start, length );
	}

	public ArrayList getPatterns() {
		return m_patterns;
	}

	public ArrayList getPatternsByType( String type ) {
		ArrayList list = new ArrayList();
		Iterator i = m_patterns.iterator();
		while( i.hasNext() )
		{
			SearchPattern p = (SearchPattern)i.next();
			if( p.getType().equals( type ) ){
				list.add( p );
			}
		}
		return list;
	}

	public void doReadPatterns() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try	{
			SAXParser parser = factory.newSAXParser();
			parser.parse( m_patternFile, this );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}
}

