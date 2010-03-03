package org.ontoware.text2onto.util.google;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author    Günter Ladwig
 * @created   11. Februar 2005
 */
public class SearchPattern
{
	private class Glue 
	{		
		private String m_glue;

		public Glue( String glue ) {
			m_glue = glue;
		}

		public String getGlue() {
			return m_glue;
		}

		public void setGlue( String glue ) {
			m_glue = glue;
		}
	}

	private class Argument
	{
		private String m_name;
		private HashMap m_attributes;

		public Argument( String name ) {
			m_name = name;
			m_attributes = new HashMap();
		}

		public String getName() {
			return m_name;
		}

		public void setName( String m_name ) {
			this.m_name = m_name;
		}

		public void setAttribute( String name, String val ) {
			m_attributes.put( name, val );
		}

		public String getAttribute( String name ) {
			return (String)m_attributes.get( name );
		}
	}

	private String m_type;
	private ArrayList m_elements;
	private HashMap m_argumentsIndex;

	public SearchPattern() {
		m_elements = new ArrayList();
		m_argumentsIndex = new HashMap();
	}

	public SearchPattern( String type ) {
		this();
		m_type = type;
	}

	public String getType() {
		return m_type;
	}

	public void setType( String m_type ) {
		this.m_type = m_type;
	}

	public void addGlue( String glue ) {
		m_elements.add( new Glue( glue ) );
		System.out.println( "added glue: " + glue );
	}

	public void addArgument( String name ) {
		Argument arg = new Argument( name );
		m_elements.add( arg );
		m_argumentsIndex.put( name, arg );
		System.out.println( "added argument " + name );
	}

	public void setArgumentAttribute( String argName, String attribute, String val ) {
		if( m_argumentsIndex.containsKey( argName ) )
		{
			Argument arg = (Argument)m_argumentsIndex.get( argName );
			arg.setAttribute( attribute, val );
			System.out.println( "added argument attribute: (" + argName + ") " + attribute + ":" + val );
		}
	}

	public String getPattern( HashMap args ) throws IllegalArgumentException {
		if( args.size() < m_argumentsIndex.size() )
		{
			throw new IllegalArgumentException( "Not enough arguments" );
		}
		String s = "";
		String addSpace = "";
		Iterator e = m_elements.iterator();
		while( e.hasNext() )
		{
			Object o = e.next();
			if( o instanceof Glue )
			{
				Glue g = (Glue)o;
				s += addSpace + g.getGlue();
				addSpace = " ";
			}
			if( o instanceof Argument )
			{
				Argument arg = (Argument)o;
				if( args.containsKey( arg.getName() ) )
				{
					// TODO use attributes (make plural etc.)
					s += addSpace + args.get( arg.getName() );
				}
				addSpace = " ";
			}
		}
		return s;
	}
}

