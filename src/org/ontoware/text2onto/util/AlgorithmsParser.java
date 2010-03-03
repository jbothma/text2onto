package org.ontoware.text2onto.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author sparn@ontoprise.de
 */
public class AlgorithmsParser extends DefaultHandler {

	String sName = "";

	String sClass = "";

	private String currentArgumentAttribute;

	private boolean insideName, insideClass, insideAlgorithm, insideRelation;

	private String currentArgument;

	private String currentChars;

	private List m_lAlgorithms;

	private HashMap m_hmRelationTypes;

	private File m_patternFile;

	private AlgorithmInfo m_currentAlgo;

	public AlgorithmsParser( File file ) {
		m_patternFile = file;
	}

	public void startDocument() {
		m_lAlgorithms = new ArrayList();
		m_hmRelationTypes = new HashMap();
		insideAlgorithm = insideName = insideClass = insideRelation = false;
	}

	public void startElement( String uri, String localName, String qName, Attributes attributes ) {

		if ( qName.equals( "algorithms" ) || qName.equals( "relation_types" ) || qName.equals( "properties" ) )
		{
			return;
		}

		if ( qName.equals( "algorithm" ) )
		{
			m_currentAlgo = new AlgorithmInfo();
			insideAlgorithm = true;
			return;
		}

		if ( qName.equals( "name" ) )
		{
			insideName = true;
		}

		if ( qName.equals( "class" ) )
		{
			insideClass = true;
		}

		if ( qName.equals( "relation" ) )
		{
			insideRelation = true;
		}
	}

	public void characters( char[] ch, int start, int length ) {
		currentChars = new String( ch, start, length );
	}

	public void endElement( String uri, String localName, String qName ) {

		if ( insideName )
		{
			sName = currentChars;
			insideName = false;
		}
		if ( insideClass )
		{
			sClass = currentChars;
		}
		if ( insideAlgorithm && insideClass )
		{
			m_currentAlgo.setName( sName );
			m_currentAlgo.setClassPath( sClass );
			m_lAlgorithms.add( m_currentAlgo );
			insideAlgorithm = false;
			insideClass = false;
			sName = null;
			sClass = null;

		} else if ( insideRelation && insideClass )
		{
			m_hmRelationTypes.put( sName, sClass );
			insideRelation = false;
			insideClass = false;
			sName = null;
			sClass = null;
		}
	}

	public List getAvailableAlgorithms() {
		return m_lAlgorithms;
	}

	public HashMap getRelationTypes() {
		return m_hmRelationTypes;
	}

	public void parseXML() {
		SAXParserFactory factory = SAXParserFactory.newInstance();

		try
		{
			SAXParser parser = factory.newSAXParser();
			parser.parse( m_patternFile, this );
		} catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) {
		String sAlgorithmsXML = Settings.get( Settings.ALGORITHMS_XML );
		AlgorithmsParser parser = new AlgorithmsParser( new File( args[0] ) );
		//XMLParser parser = new XMLParser( new File( sAlgorithmsXML ) );
		parser.parseXML();
	}
}