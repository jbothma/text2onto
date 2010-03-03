package org.ontoware.text2onto.util.google;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;
import com.google.soap.search.GoogleSearchResultElement;

/**
 * @author    gl
 * @created   11. Februar 2005
 */
public class SearchThread extends Thread
{
	private GoogleSearch gs;
	private String[] results = null;
	private String key;
	private boolean snippets = true;
	private int startIndex = 0, resultCount;
	private int runNr = 0;
	private long startTime;

	public void setGoogleSearch( GoogleSearch gs, String key, int startIndex, int resultCount ) {
		this.gs = gs;
		this.key = key;
		this.startIndex = startIndex;
		this.resultCount = resultCount;
		startTime = System.currentTimeMillis();
	}

	public GoogleSearch getGoogleSearch() {
		return gs;
	}

	public String[] getResults() {
		return results;
	}

	public void setResults( String[] results ) {
		this.results = results;
	}

	public void setGetSnippets( boolean snippets ) {
		this.snippets = snippets;
	}

	public long getDuration() {
		return System.currentTimeMillis() - startTime;
	}

	public void run() {
		GoogleSearchResult gsr;
		startTime = System.currentTimeMillis();
		try {
			gsr = gs.doSearch();
		}
		catch( GoogleSearchFault e ){
			e.printStackTrace();
			return;
		}
		GoogleSearchResultElement[] gsre = gsr.getResultElements();
		results = new String[gsre.length];
//		System.out.println("SearchThread finished: (" + startIndex + "-" + (startIndex + resultCount) + ") gsre length: " + gsre.length);
		for( int i = 0; i < gsre.length; i++ )
		{
			results[i] = gsre[i].getSnippet();
			if( snippets )
			{
				results[i] = gsre[i].getSnippet();
			}
			else {
				GoogleSearch cache = new GoogleSearch();
				cache.setKey( key );
				try	{
					results[i] = new String( gs.doGetCachedPage( gsre[i].getURL() ) );
				}
				catch( GoogleSearchFault e )
				{
					results[i] = null;
					e.printStackTrace();
				}
			}
		}
	}

	public static void main( String[] args ) {
		long start = System.currentTimeMillis();
		int THREAD_COUNT = 10;
		int RESULT_COUNT = 100;
		if( args.length == 2 )
		{
			THREAD_COUNT = Integer.valueOf( args[0] ).intValue();
			RESULT_COUNT = Integer.valueOf( args[1] ).intValue();
		}
		String[] results = new String[RESULT_COUNT];
		int resultIndex = 0;
		GoogleSearchResult gsr;
		String key = "wIwufPRQFHI3OG2G3owuUzaJAJjKQtyV";
		SearchThread[] threads = new SearchThread[THREAD_COUNT];
		String search = "java sdk jaf";
		int startIndex = 0;
		//int resultsPerThread = (THREAD_COUNT * 10 > RESULT_COUNT) ? RESULT_COUNT / THREAD_COUNT : 10;
		int resultsPerThread = 10;
		while( true )
		{
			boolean finished = true;
			for( int i = 0; i < THREAD_COUNT; i++ )
			{
				if( threads[i] != null && threads[i].isAlive() )
				{
					finished = false;
				}
				else if( startIndex <= RESULT_COUNT - resultsPerThread )
				{
					if( threads[i] != null && threads[i].getResults() != null )
					{
						String[] res = threads[i].getResults();

						for( int j = 0; j < res.length; j++ )
						{
							System.out.println( "g: " + res[j] );
							results[resultIndex] = res[j];
							resultIndex++;
						}
						threads[i].setResults( null );
					}
					threads[i] = new SearchThread();
					GoogleSearch gs = new GoogleSearch();
					gs.setKey( key );
					gs.setQueryString( search );
					gs.setMaxResults( resultsPerThread );
					gs.setStartResult( startIndex );
					System.out.println( "thread " + i + " with result nrs " + startIndex + " - " + ( startIndex + resultsPerThread ) );

					threads[i].setGoogleSearch( gs, key, startIndex, resultsPerThread );
					threads[i].start();
					startIndex += resultsPerThread;
					finished = false;
				}
				else if( threads[i] != null )
				{
					if( threads[i].getResults() != null )
					{
						String[] res = threads[i].getResults();
						for( int j = 0; j < res.length; j++ )
						{
							if( res[j] == null )
							{
								System.out.println( "thread " + i + " result " + j + " empty" );
							}
							results[resultIndex] = res[j];
							resultIndex++;
						}
						System.out.println( "results from thread nr " + i + " saved" );
						threads[i].setResults( null );
					}
				}
			}
			if( finished ){
				break;
			}
		}
		long end = System.currentTimeMillis();
		for( int i = 0; i < results.length; i++ )
		{
			//if (results[i] == null)
			System.out.println( i + ": " + results[i] );
		}
		System.out.println( "time: " + ( end - start ) );
		System.out.println( "results per thread: " + resultsPerThread );
	}

	public int getResultCount() {
		return resultCount;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getRunNr() {
		return runNr;
	}

	public void setRunNr( int runNr ) {
		this.runNr = runNr;
	}
}

