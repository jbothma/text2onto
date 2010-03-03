package org.ontoware.text2onto.util.google;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;

/**
 * @author    gl
 * @created   11. Februar 2005
 */
public class GoogleQuery {

	private final int THREAD_COUNT = 10;

	private final int SEARCH_TIMEOUT = 30000;

	private boolean m_useCache = false;

	private ResultCache m_cache = null;

	private String m_key;

	private PatternParser m_patterns;

	public GoogleQuery() {
		m_patterns = new PatternParser( new File( "C:/Programme/Entwicklung/eclipse3.0.0/workspace/t2o_src_temp/org/ontoware/text2onto/google/patterns.xml" ) );
		m_patterns.doReadPatterns();
	}

	public void setKey( String key ) {
		m_key = key;
	}

	public int getAbsoluteHits( String s ) {
		s = "\"" + s + "\"";
		GoogleSearchResult gsr;
		GoogleSearch gs = new GoogleSearch();
		gs.setKey( m_key );
		gs.setQueryString( s );
		try
		{
			gsr = gs.doSearch();
		} catch ( GoogleSearchFault e )
		{
			e.printStackTrace();
			return 0;
		}
		return gsr.getEstimatedTotalResultsCount();
	}

	public double getRelativeHits( String s1, String s2 ) {
		String[] results = null;
		GoogleSearch gs;
		GoogleSearchResult gsr;
		int combinedResults = 0;
		int termResults = 0;
		s1 = "\"" + s1 + "\"";
		s2 = "\"" + s2 + "\"";
		try
		{
			String query = s1;
			gs = new GoogleSearch();
			gs.setKey( m_key );
			gs.setQueryString( query );
			gsr = gs.doSearch();

			termResults = gsr.getEstimatedTotalResultsCount();
			//System.out.println("termResults: " + termResults + " (query: " + query + ") " + gsr.getEstimateIsExact());

			query = s1 + " " + s2;
			gs = new GoogleSearch();
			gs.setKey( m_key );
			gs.setQueryString( query );
			gsr = gs.doSearch();

			combinedResults = gsr.getEstimatedTotalResultsCount();
			//System.out.println("combinedResults: " + combinedResults + "(query: " + query + ") " + gsr.getEstimateIsExact());
		} catch ( GoogleSearchFault e )
		{
			e.printStackTrace();
		}
		if ( termResults == 0 )
		{
			return 0.0;
		} else
		{
			return (double) combinedResults / (double) termResults;
		}
	}

	public double getRelativeHits( String type, HashMap args, String inst ) {
		int combinedResults = getHits( type, args );
		int termResults = getAbsoluteHits( (String) args.get( inst ) );
		if ( termResults == 0 )
		{
			return 0.0;
		} else
		{
			return (double) combinedResults / (double) termResults;
		}
	}

	public int getHits( String type, HashMap args ) {
		int hits = 0;
		ArrayList patterns = m_patterns.getPatternsByType( type );
		Iterator i = patterns.iterator();
		while ( i.hasNext() )
		{
			SearchPattern p = (SearchPattern) i.next();
			String pattern = p.getPattern( args );
			System.out.println( pattern );
			hits += getAbsoluteHits( pattern );
		}
		return hits;
	}

	public static void main( String[] args ) throws Exception {
		GoogleQuery g = new GoogleQuery();
		g.setKey( args[0] );
		HashMap h = new HashMap();

		h.put( "domain", "BMW" );
		h.put( "range", "cars" );

		System.out.println( g.getPages( "SubclassOfRelation", h, 10, true ) );
		//		System.out.println(g.getRelativeHits("SubclassOfRelation", h, "domain"));
		//		System.out.println(gq.getRelativeHits("dog psychologist", "day time"));
		//		System.out.println(gq.getAbsoluteHits("dog psychologist"));
		//		System.out.println(gq.getRelativeHits("dog hair cutter", "money"));
		//		System.out.println(gq.getRelativeHits("dog AND hair AND cutter", " AND cash"));
		//		System.out.println(gq.getRelativeHits("hair dog cutter", "cash"));
	}

	public void setUseCache( boolean useCache ) {
		m_useCache = useCache;
	}

	public String[] getPages( String type, HashMap args, int count, boolean snippets ) {
		ArrayList patterns = m_patterns.getPatternsByType( type );
		String[] results = new String[patterns.size() * count];
		int pos = 0;
		Iterator i = patterns.iterator();
		while ( i.hasNext() )
		{
			SearchPattern p = (SearchPattern) i.next();
			String pattern = p.getPattern( args );
			String[] res = getPages( pattern, count, snippets );
			System.arraycopy( res, 0, results, pos, res.length );
			pos += res.length;
		}
		return results;
	}

	public String[] getPages( String search, int count, boolean snippets ) {
		long start = System.currentTimeMillis();
		SearchThread[] threads = new SearchThread[THREAD_COUNT];
		GoogleSearch gs;
		GoogleSearchResult gsr;
		String[] results;
		search = "\"" + search + "\"";
		System.out.print( "s:" + search );
		if ( m_useCache )
		{
			if ( m_cache == null )
			{
				m_cache = new ResultCache();
			}
			// Check if the cache has results for the query
			if ( snippets && m_cache.resultCached( search, count ) )
			{
				results = m_cache.get( search, count );
				return results;
			}
		}
		try
		{
			gs = new GoogleSearch();
			gs.setKey( m_key );
			gs.setQueryString( search );
			gsr = gs.doSearch();
		} catch ( GoogleSearchFault e )
		{
			e.printStackTrace();
			return null;
		}
		int estimate = gsr.getEstimatedTotalResultsCount();
		count = ( count > estimate ) ? estimate : count;
		System.out.print( " cae:" + count );
		if ( count == 0 )
		{
			return new String[0];
		}
		if ( m_useCache )
		{
			// Now check if the cache has results for the
			// estimated count (which may be lower than the requested count
			// or zero)
			if ( snippets && m_cache.resultCached( search, count ) )
			{
				results = m_cache.get( search, count );
				return results;
			}
		}
		int resultsPerThread = 10;
		int resultIndex = 0;
		int startIndex = 0;
		//		int resultsPerThread = (THREAD_COUNT * 10 > RESULT_COUNT) ? RESULT_COUNT / THREAD_COUNT : 10;
		results = new String[count];
		while ( true )
		{
			boolean finished = true;
			for ( int i = 0; i < THREAD_COUNT; i++ )
			{
				if ( threads[i] != null && threads[i].isAlive() )
				{
					finished = false;
					if ( threads[i].getDuration() > SEARCH_TIMEOUT )
					{
						if ( threads[i].getRunNr() >= 4 )
						{
							//							System.out.println("Thread " + i + " (" + threads[i].getStartIndex() + "-"
							//								+  (threads[i].getStartIndex() + threads[i].getResultCount())
							//								+ ") restarted too often, killing");
							threads[i].interrupt();
							threads[i] = null;
						} else
						{
							GoogleSearch newgs;
							GoogleSearch oldgs = threads[i].getGoogleSearch();

							newgs = new GoogleSearch();

							int resultCount = threads[i].getResultCount();
							int idx = threads[i].getStartIndex();
							int runnr = threads[i].getRunNr() + 1;

							//							System.out.println("[" + System.currentTimeMillis() + "] Restarting thread " + i + " ("
							//								+ idx + "-" + (idx + resultCount) + ")");

							threads[i].interrupt();

							newgs.setKey( m_key );
							newgs.setQueryString( search );
							newgs.setMaxResults( resultCount );
							newgs.setStartResult( idx );

							threads[i] = new SearchThread();
							threads[i].setGoogleSearch( newgs, m_key, idx, resultCount );
							threads[i].setRunNr( runnr );
							threads[i].start();
						}
					}
				} else if ( startIndex <= count - resultsPerThread )
				{
					if ( threads[i] != null && threads[i].getResults() != null )
					{
						String[] res = threads[i].getResults();
						for ( int j = 0; j < res.length; j++ )
						{
							System.out.println( "g: " + res[j] );
							results[resultIndex] = res[j];
							resultIndex++;
						}
						threads[i].setResults( null );
					}
					threads[i] = new SearchThread();
					gs = new GoogleSearch();
					gs.setKey( m_key );
					gs.setQueryString( search );
					gs.setMaxResults( resultsPerThread );
					gs.setStartResult( startIndex );

					//					System.out.println("thread " + i + " with result nrs " + startIndex + " - " + (startIndex + resultsPerThread));

					threads[i].setGoogleSearch( gs, m_key, startIndex, resultsPerThread );
					threads[i].setRunNr( 1 );
					threads[i].start();

					startIndex += resultsPerThread;
					finished = false;
				} else if ( threads[i] != null )
				{
					if ( threads[i].getResults() != null )
					{
						String[] res = threads[i].getResults();
						for ( int j = 0; j < res.length; j++ )
						{
							//							if (res[j] == null)
							//								System.out.println("thread " + i + " result " + j + " empty");
							results[resultIndex] = res[j];
							resultIndex++;
						}
						//						System.out.println("results from thread nr " + i + " saved");
						threads[i].setResults( null );
					}
				}
			}
			if ( finished )
			{
				break;
			}
		}
		long end = System.currentTimeMillis();
		int nullResults = 0;
		//		for (int i = 0; i < results.length; i++)
		//		{
		//			if (results[i] == null)
		//			{
		//				System.out.print(i + " ");
		//				nullResults++;
		//			}
		//		}
		//		System.out.print("\n");

		//		System.out.println("search string: " + search + ", count: " + count + ", time: " + (end - start));
		System.out.println( " t:" + ( end - start ) + " nr:" + nullResults );
		//		System.out.println("results per thread: " + resultsPerThread);

		if ( m_useCache && snippets )
		{
			m_cache.add( search, results );
		}
		return results;
	}
}

