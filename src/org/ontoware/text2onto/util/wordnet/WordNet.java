package org.ontoware.text2onto.util.wordnet;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;
import org.ontoware.text2onto.util.Settings;

import com.sleepycat.je.DatabaseException;


public class WordNet {

	protected static WordNet m_wordnet;

	protected Dictionary m_wnDictionary;
	

	protected WordNet(){}

	public static WordNet getWordNet(){
		if( m_wordnet == null ){
			m_wordnet = new WordNet();
			m_wordnet.init();
		}
		return m_wordnet;
	}
	
	public void init() {
		try {
			JWNL.initialize( new FileInputStream( Settings.get( Settings.JWNL_PROPERTIES ) ) );
		}
		catch( Exception ex ){
			ex.printStackTrace();
			System.exit( -1 );
		}
	}

	public String getGloss( Synset synset ) {
		return synset.getGloss();
	}

	public String getGlosses( Synset[] syns ) {
		String sReturn = "";
		for( int i = 0; i < syns.length; i++ )
		{
			Synset synset = (Synset)syns[i];
			sReturn += synset.getGloss() + " ";
		}
		return sReturn;
	}

	public List<WordNetRelation> getHypernymRelations( Synset[] aSynset1, Synset[] aSynset2 ) throws JWNLException {
		List<WordNetRelation> relations = new ArrayList<WordNetRelation>();
		if( aSynset1 == null || aSynset2 == null ){
			return relations;
		}
		for( int i = 0; i < aSynset1.length; i++ ){
			for( int j = 0; j < aSynset2.length; j++ )
			{
				boolean bIsHypernym = isHypernymOf( aSynset1[i], aSynset2[j] );
				if( bIsHypernym ){
					relations.add( new WordNetRelation( aSynset1[i], aSynset2[j] ) );

				}
			}
		}
		return relations;
	}

	public List<WordNetRelation> getMeronymRelations( Synset[] aSynset1, Synset[] aSynset2 ) throws JWNLException {
		List<WordNetRelation> relations = new ArrayList<WordNetRelation>();
		if( aSynset1 == null || aSynset2 == null ){
			return relations;
		}
		for( int i = 0; i < aSynset1.length; i++ ){
			for( int j = 0; j < aSynset2.length; j++ )
			{
				boolean bIsMeronym = isMeronymOf( aSynset1[i], aSynset2[j] );
				if( bIsMeronym ){
					relations.add( new WordNetRelation( aSynset1[i], aSynset2[j] ) );
				}
			}
		}
		return relations;
	}

	public Synset[] getHypernymSynsets( Synset syn ) throws JWNLException {
		List<Synset> lSyns = new ArrayList<Synset>();
		PointerTargetNodeList nodelist = (PointerTargetNodeList)PointerUtils.getInstance().getHypernymTree( syn ).toList().get( 0 );
		for( int i = 0; i < nodelist.size(); i++ )
		{
			PointerTargetNode node = (PointerTargetNode)nodelist.get( i );
			lSyns.add( node.getSynset() );
		}
		return (Synset[])lSyns.toArray( new Synset[]{} );
	}

	public Synset[] getMeronymSynsets( Synset syn ) throws JWNLException {
		return (Synset[])PointerUtils.getInstance().getMeronyms( syn ).get( 0 );
	}

	public Synset[] getSynsets( String sNoun ) throws JWNLException, WordNetException {
		IndexWord indexword = Dictionary.getInstance().getIndexWord( POS.NOUN, sNoun );
		if( indexword != null )
		{
			Synset[] synset = indexword.getSenses();
			return synset;
		}
		return new Synset[0];
	}
 
	protected boolean comparePointerTargetNodeLists( Synset synset1, List nodelist2 ) {
		Iterator iterNodelist2 = nodelist2.iterator(); 
		while( iterNodelist2.hasNext() )
		{
			PointerTargetNodeList nl2 = (PointerTargetNodeList)iterNodelist2.next();
			Iterator iterNl2 = nl2.iterator();
			while( iterNl2.hasNext() )
			{
				PointerTargetNode n2 = (PointerTargetNode)iterNl2.next();
				Synset synset2 = n2.getSynset();
				if( synset2.equals( synset1 ) ){
					return true;
				}
			}
		}
		return false;
	} 

	protected boolean isAbstract( PointerTargetNode ptn ) {
		/*
		 * [Offset: 1740] [POS: noun] Words: entity 
		 * [Offset: 16236] [POS: noun] Words: object, physical_object 
		 * [Offset: 19244] [POS: noun] Words: artifact, artefact 
		 * [Offset: 2645] [POS: noun] Words: whole, whole_thing, unit 
		 * [Offset: 3068033] [POS: noun] Words: device
		 * [Offset: 3443493] [POS: noun] Words: instrumentality, instrumentation
		 */
		long[] lExceptions = new long[]{ 16236, 1740, 19244, 2645 };
		Arrays.sort( lExceptions );
		long lOffset = ptn.getSynset().getOffset();
		if( Arrays.binarySearch( lExceptions, lOffset ) < 0 ){
			return false;
		}
		return true;
	}
	
	public double isHypernymOf( String sWord1, String sWord2 ) throws WordNetException {
		double dHypernym = 0.0;
		if( sWord1 == null || sWord2 == null || sWord1.equals( sWord2 ) ){
			return dHypernym;
		}
		try {
			Synset[] synsets1 = getSynsets( sWord1 );
			//System.out.println("s");
			//System.out.println("\n Wordnet Synset:"+((Synset)synsets1[0]).toString());
			Synset[] synsets2 = getSynsets( sWord2 );
			//System.out.println("\n Wordnet Synset:"+((Synset)synsets2[0]).toString());
			int iHypernym = 0;
			for( int i=0; i<synsets1.length; i++ ){
				for( int j=0; j<synsets2.length; j++ )
				{
					if( isHypernymOf( synsets1[i], synsets2[j] ) ){
						iHypernym++;
					}
				}
			}
			dHypernym = Math.min( 1.0, (double)iHypernym / (double)synsets2.length );
		}
		catch( Exception e ){
			throw new WordNetException(e);
		}
		return dHypernym;
	}
	
	protected double isMeronymOf( String sWord1, String sWord2 ) throws WordNetException {
		double dMeronym = 0.0;
		if( sWord1 == null || sWord2 == null || sWord1.equals( sWord2 ) ){
			return dMeronym;
		}
		try {
			Synset[] synsets1 = getSynsets( sWord1 );
			Synset[] synsets2 = getSynsets( sWord2 );
			int iMeronym = 0;
			for( int i=0; i<synsets1.length; i++ ){
				for( int j=0; j<synsets2.length; j++ )
				{
					if( isMeronymOf( synsets1[i], synsets2[j] ) ){
						iMeronym++;
					}
				}
			}
			dMeronym = Math.min( 1.0, (double)iMeronym / (double)synsets2.length );
		}
		catch( Exception e ){
			throw new WordNetException(e);
		}
		return dMeronym;
	}

	protected boolean isHypernymOf( Synset synset1, Synset synset2 ) throws JWNLException {
		PointerTargetTree hypernyms2 = PointerUtils.getInstance().getHypernymTree( synset2 ); 
		List nodelist2 = hypernyms2.toList();
		return comparePointerTargetNodeLists( synset1, nodelist2 );
	}

	protected boolean isMeronymOf( Synset synset1, Synset synset2 ) throws JWNLException { 
		
		PointerTargetNodeList nodelist2 = PointerUtils.getInstance().getPartMeronyms( synset2 );
		return comparePointerTargetNodeLists( synset1, nodelist2 );
	}
}
