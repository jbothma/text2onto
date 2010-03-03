package org.ontoware.text2onto.util.google;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.*;
import java.util.*;
import java.util.Properties;

/**
 * @author    gl
 * @created   11. Februar 2005
 */
public class ResultCache
{
	private final String LAST_ID = "PANKOW_LAST_ID";
	private Properties index;
	private Properties size;
	private File cacheDir = new File( System.getProperty( "user.home" ) + "/.pankow/resultcache" );
	private File indexFile = new File( cacheDir + "/index" );
	private File sizeFile = new File( cacheDir + "/sizes" );

	private String getNewId() {
		int id = Integer.parseInt( index.getProperty( LAST_ID ) );
		id++;
		index.setProperty( LAST_ID, String.valueOf( id ) );
		return String.valueOf( id );
	}

	public ResultCache() {
		System.out.println( cacheDir );
		index = new Properties();
		size = new Properties();
		if( !cacheDir.exists() )
		{
			cacheDir.mkdirs();
			index.setProperty( LAST_ID, String.valueOf( 0 ) );
			return;
		}
		if( !indexFile.exists() )
		{
			index.setProperty( LAST_ID, String.valueOf( 0 ) );
			return;
		}
		if( !sizeFile.exists() ){
			return;
		}
		try	{
			index.load( new FileInputStream( indexFile ) );
			size.load( new FileInputStream( sizeFile ) );
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}

	public boolean resultCached( String search, int count ) {
		if( index.containsKey( search ) )
		{
			if( Integer.parseInt( size.getProperty( search ) ) >= count ){
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	public void add( String search, String[] results ) {
		String id;
		if( index.containsKey( search ) ){
			id = index.getProperty( search );
		}
		else {
			id = getNewId();
			index.setProperty( search, id );
		}
		File file = new File( cacheDir + "/" + id );
		try	{
			PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( file ) ) );
			int i;
			for( i = 0; i < results.length; i++ )
			{
				if( results[i] != null ){
					out.println( results[i] );
				}
			}
			out.close();
			size.setProperty( search, String.valueOf( i ) );
			index.store( new FileOutputStream( indexFile ), null );
			size.store( new FileOutputStream( sizeFile ), null );
		}
		catch( IOException e ){
			// TODO remove index entry or otherwise the index will be incoherent
			e.printStackTrace();
		}
	}

	public String[] get( String search, int count ) {
		String line;
		ArrayList res = new ArrayList();
		String[] results;
		String id = index.getProperty( search );
		int j = 0;
		if( !resultCached( search, count ) ){
			return null;
		}
		try	{
			BufferedReader in = new BufferedReader( new FileReader( cacheDir + "/" + id ) );
			while( ( line = in.readLine() ) != null && j < count ){
				res.add( line );
				j++;
			}
			in.close();
			results = new String[res.size()];
			Iterator i = res.iterator();
			j = 0;
			while( i.hasNext() )
			{
				results[j] = (String)i.next();
				j++;
			}
			System.out.println( "read cached results - string: " + search + ", count: " + j );
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return null;
		}
		return results;
	}
}

