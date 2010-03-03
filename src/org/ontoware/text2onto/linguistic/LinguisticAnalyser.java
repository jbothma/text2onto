package org.ontoware.text2onto.linguistic;

import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.creole.ANNIEConstants;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.ontoware.text2onto.corpus.*; 
import org.ontoware.text2onto.reference.document.DocumentReference;

//import org.ontoware.text2onto.linguistic.AnnotationType;
//import org.ontoware.text2onto.linguistic.FeatureType;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class LinguisticAnalyser implements Serializable, AnnotationType, FeatureType {

	private LinguisticPreprocessor m_preprocessor;


	public LinguisticAnalyser() throws AnalyserException {
		try {
			m_preprocessor = new LinguisticPreprocessor();
			m_preprocessor.init();
		} 
		catch ( Exception e ){
			throw new AnalyserException( "failed to initialize LinguisticPreprocessor", e );
		}
	}
 
	public void doPreprocessing( Corpus corpus ) throws AnalyserException {
		try {
			m_preprocessor.setCorpus( corpus );
			m_preprocessor.doPreprocessing();
		} 
		catch ( Exception e ){
			throw new AnalyserException( "exception while preprocessing corpus", e );
		}
	}
	 
	// AlgorithmController compatibility added methods:
	public void setCorpus( Corpus corpus ) throws AnalyserException { 
		try {
			m_preprocessor.setCorpus( corpus );
		} 
		catch ( Exception e ){
			throw new AnalyserException( "unable to create GATE corpus", e );
		}
	}
	
	public void doPreprocessing() throws AnalyserException {
		try {
			m_preprocessor.doPreprocessing();
		} 
		catch ( Exception e ){
			throw new AnalyserException( "exception while preprocessing corpus", e );
		}
	}
	
	public void initPreprocessor() throws AnalyserException {
		try {
			m_preprocessor.initTransducers();
			m_preprocessor.initApplication();
		}
		catch( Exception e ){
			throw new AnalyserException( "failed to init preprocessor", e );
		}
	}
	
	// org.ontoware.text2onto.algorithm.auxiliary.context.ContextExtractionWithFrame compatibility added methods:
	public List getTokenReference( AbstractDocument doc ) {
		List anns = getTokenAnnotations( createDocumentReference( doc ) );
		return createDocumentReferences( doc, anns );
	}

	public List getTokenReference( DocumentReference reference ) {
		List anns = getTokenAnnotations( reference );
		return createDocumentReferences( reference.getDocument(), anns );
	}
	
	/** **************************************************************************************************************** */

	// Token
	
	public List getTokenReferences( AbstractDocument doc ) {
		List anns = getTokenAnnotations( createDocumentReference( doc ) );
		return createDocumentReferences( doc, anns );
	}

	public List getTokenReferences( DocumentReference reference ) {
		List anns = getTokenAnnotations( reference );
		return createDocumentReferences( reference.getDocument(), anns );
	}

	public List getTokenStrings( DocumentReference reference ) { 
		return getTokenFeatureValues( "string", reference ); 
	}

	public List getTokenCategories( DocumentReference reference ) {
		return getTokenFeatureValues( "category", reference );
	}

	public List getTokenStems( DocumentReference reference ) {
		List stems = getTokenFeatureValues( "root", reference );
		if( stems.size() == 0 )
		{
			stems = getTokenFeatureValues( "stem", reference );
			if( stems.size() == 0 ){
				stems = getTokenStrings( reference );
			}
		} 
		return stems;
	}

	public boolean isStopword( DocumentReference reference ) {
		ArrayList alStopword = (ArrayList)getTokenFeatureValues( "stopword", reference );
		if ( alStopword.size() == 0 ){
			return false;
		}
		return ( (Boolean) alStopword.get( 0 ) ).booleanValue();
	}

	// Sentence

	public List getSentenceReferences( AbstractDocument doc ) {
		List anns = getSentenceAnnotations( createDocumentReference( doc ) );
		return createDocumentReferences( doc, anns );
	}

	public List getSentenceReferences( DocumentReference reference ) {
		List anns = getSentenceAnnotations( reference );
		return createDocumentReferences( reference.getDocument(), anns );
	}

	// Linguistic, Ontological

	public List getDocumentReferences( AbstractDocument doc, String sAnnType ) {
		AnnotationSet annSet = getAnnotationSet( doc, sAnnType );
		if ( annSet == null ){
			return new ArrayList();
		}
		return createDocumentReferences( doc, new ArrayList( annSet ) );
	}

	public List getDocumentReferences( AbstractDocument doc ) {
		AnnotationSet annSet = getAnnotationSet( doc );
		if ( annSet == null ){
			return new ArrayList();
		}
		return createDocumentReferences( doc, new ArrayList( annSet ) );
	}

	public List getDocumentReferences( DocumentReference reference, String sAnnType ) {
		AnnotationSet annSet = getAnnotationSet( reference, sAnnType );
		if ( annSet == null ){
			return new ArrayList();
		}
		return createDocumentReferences( reference.getDocument(), new ArrayList( annSet ) );
	}
	
	public List getDocumentReferences( AbstractDocument doc, String sAnnType, HashMap<String,Object> hmFeature2Value ) {
		AnnotationSet annSet = getAnnotationSet( doc, sAnnType );
		if( annSet == null ){
			return new ArrayList();
		}
		List<Annotation> results = new ArrayList<Annotation>();
		List<Annotation> annList = new ArrayList<Annotation>( annSet ); 
		Iterator iter = annList.iterator();
		while( iter.hasNext() )
		{
			Annotation ann = (Annotation)iter.next();
			for( String sFeature: hmFeature2Value.keySet() )
			{
				Object annValue = ann.getFeatures().get( sFeature );
				if( annValue != null 
					&& annValue.equals( hmFeature2Value.get( sFeature ) ) )
				{
					results.add( ann );
				}
			}
		}
		return createDocumentReferences( doc, results );
	}

	/** **************************************************************************************************************** */

	private AnnotationSet getAnnotationSet( AbstractDocument doc ) {
		return m_preprocessor.getAnnotations( doc );
	}

	private AnnotationSet getAnnotationSet( AbstractDocument doc, String sAnnType ) {
		AnnotationSet annSet = getAnnotationSet( doc );
		if ( annSet != null )
		{
			return annSet.get( sAnnType );
		}
		return null;
	}

	private AnnotationSet getAnnotationSet( DocumentReference reference ) {
		return getAnnotationSet( reference.getStartOffset(), reference.getEndOffset(), reference.getDocument() );
	}

	private AnnotationSet getAnnotationSet( DocumentReference reference, String sAnnType ) {
		return getAnnotationSet( reference.getStartOffset(), reference.getEndOffset(), reference.getDocument(), sAnnType );
	}

	private AnnotationSet getAnnotationSet( Long lStart, Long lEnd, AbstractDocument doc ) {
		return m_preprocessor.getAnnotations( doc, lStart, lEnd );
	}

	private AnnotationSet getAnnotationSet( Long lStart, Long lEnd, AbstractDocument doc, String sAnnType ) {
		AnnotationSet annSet = getAnnotationSet( doc, sAnnType );
		if ( annSet != null )
		{
			return annSet.get( lStart, lEnd );
		}
		return null;
	}
	
	/** **************************************************************************************************************** */
 	
 	public List getDebug( AbstractDocument doc ) throws Exception {
		return getDebug( doc, null );
	}
	
	public List getDebug( AbstractDocument doc, String sAnnType ) throws Exception {
		ArrayList al = new ArrayList();
		AnnotationSet annSet = null;
		if( sAnnType != null ){
			annSet = getAnnotationSet( doc, sAnnType );
		} else {
			annSet = getAnnotationSet( doc );
		}
		if( annSet != null )
		{		 
			ArrayList alAnn = new ArrayList( annSet );
			Collections.sort( alAnn, new OffsetComparator() );
			Iterator iter = alAnn.iterator();
			while( iter.hasNext() )
			{
				Annotation ann = (Annotation)iter.next();
				String sText = getText( ann, doc );
				String sAnn = "[ "+ sText +" ] = "+ ann;
				al.add( sAnn );
			}
		}
		return al;
	}
	
	/** **************************************************************************************************************** */

	private List createDocumentReferences( AbstractDocument doc, List anns ) {
		ArrayList alReference = new ArrayList();
		Collections.sort( anns, new OffsetComparator() );
		Iterator iter = anns.iterator();
		while ( iter.hasNext() )
		{
			alReference.add( createDocumentReference( doc, (Annotation) iter.next() ) );
		}
		return alReference;
	}

	private DocumentReference createDocumentReference( AbstractDocument doc ) {
		AnnotationSet annSet = getAnnotationSet( doc );
		Long lStart = annSet.firstNode().getOffset();
		Long lEnd = annSet.lastNode().getOffset(); 
		//return new DocumentReference( doc, lStart, lEnd ); 
		String sText = null;
		try {
			sText = getText( doc, lStart, lEnd );
		}
		catch( AnalyserException e ){
			System.err.println( "LinguisticAnalyser.createDocumentReference: "+ e );
		}
		return new DocumentReference( this, doc, lStart, lEnd, sText ); 
	}

	private DocumentReference createDocumentReference( AbstractDocument doc, Annotation ann ) {
		Long lStart = ann.getStartNode().getOffset();
		Long lEnd = ann.getEndNode().getOffset(); 
		//return new DocumentReference( doc, lStart, lEnd );
		String sText = null;
		try {
			sText = getText( doc, lStart, lEnd );
		}
		catch( AnalyserException e ){
			System.err.println( "LinguisticAnalyser.createDocumentReference: "+ e );
		}
		return new DocumentReference( this, doc, lStart, lEnd, sText ); 
	}
 
	public List removeStopwords( DocumentReference reference ) {
		ArrayList alTokens = new ArrayList();
		if ( !isStopword( reference ) ) 
		{
			// alTokenStems contains always one token
			ArrayList alTokenStems = (ArrayList) getTokenStems( reference );
			alTokens.addAll( alTokenStems );
		}
		return alTokens;
	}

	public List removeSpecialCharacters( DocumentReference reference ) {
		String sText = null;
		try {
			sText = getText( reference );
		}
		catch( AnalyserException e ){
			System.err.println( "LinguisticAnalyser.removeSpecialCharacters: "+ e );
		}
		return removeSpecialCharacters( sText );
	}

	public List removeSpecialCharacters( String[] words ){
		List lReturn = new ArrayList();
		for( int i = 0; i < words.length; i++ ){
			String sWord = words[i];
			lReturn.addAll( removeSpecialCharacters ( sWord ) );
		}
		return lReturn;
	}

	public List removeSpecialCharacters( String sWord ) {
		List lTokens = new ArrayList();
		sWord = sWord.replaceAll( "[^A-Za-z \t\n]", "" );
		StringTokenizer tokenizer = new StringTokenizer( sWord );
		while ( tokenizer.hasMoreTokens() ) 
		{
			String sToken = (String) tokenizer.nextToken();
			if ( !sToken.equals( "" ) ) {
				lTokens.add( sToken );
			}
		}
		return lTokens;
	}
	
	public String getObjectLabel( DocumentReference reference ){
		StringBuffer sbEntity = new StringBuffer();
		List stems = getTokenStems( reference );
		for( Iterator sIter = stems.iterator(); sIter.hasNext(); )
		{
			String sStem = (String)sIter.next();
			if( sStem.length() > 1 )
			{
				sbEntity.append( sStem );
				sbEntity.append( " " ); 
			}
		}
		String sTemp = sbEntity.toString().trim();
		StringBuffer sb = new StringBuffer();
		for( int i=0; i<sTemp.length(); i++ )
		{
			Character c = sTemp.charAt(i);
			if( Character.isLetter( c ) || Character.isWhitespace( c ) ){
				sb.append( c );
			}
		}
		String sEntity = sb.toString();
		if( sEntity.length() < 2 ){
			return null;
		}
		return sEntity;
	}
	
	/** **************************************************************************************************************** */

	protected List getTokenFeatureValues( String sFeature, DocumentReference reference ) {
		return getFeatureValues( sFeature, getTokenAnnotations( reference ) );
	}

	private List getTokenAnnotations( DocumentReference reference ) {
		ArrayList al = new ArrayList();
		AnnotationSet annSet = getAnnotationSet( reference.getDocument(), ANNIEConstants.TOKEN_ANNOTATION_TYPE );
		if ( annSet != null )
		{
			annSet = annSet.get( reference.getStartOffset(), reference.getEndOffset() );
			if ( annSet != null )
			{
				List annList = new ArrayList( annSet );
				Collections.sort( annList, new OffsetComparator() );
				Iterator iter = annList.iterator();
				while ( iter.hasNext() )
				{
					al.add( (Annotation) iter.next() );
				}
			}
		}
		return al;
	}

	private List getSentenceFeatureValues( String sFeature, DocumentReference reference ) {
		return getFeatureValues( sFeature, getSentenceAnnotations( reference ) );
	}

	private List getSentenceAnnotations( DocumentReference reference ) {
		ArrayList al = new ArrayList();
		AnnotationSet annSet = getAnnotationSet( reference.getDocument(), ANNIEConstants.SENTENCE_ANNOTATION_TYPE );
		if ( annSet != null )
		{
			annSet = annSet.get( reference.getStartOffset(), reference.getEndOffset() );
			if ( annSet != null )
			{
				List annList = new ArrayList( annSet );
				Collections.sort( annList, new OffsetComparator() );
				Iterator iter = annSet.iterator();
				while ( iter.hasNext() )
				{
					al.add( (Annotation) iter.next() );
				}
			}
		}
		return al;
	}

	private List getFeatureValues( String sFeature, List anns ) {
		ArrayList al = new ArrayList();
		Collections.sort( anns, new OffsetComparator() );
		Iterator iter = anns.iterator();
		while ( iter.hasNext() )
		{
			Annotation ann = (Annotation) iter.next();
			FeatureMap features = ann.getFeatures();
			if ( features.get( sFeature ) instanceof String )
			{
				String sValue = (String) features.get( sFeature );
				al.add( sValue );
			} 
			else if ( features.get( sFeature ) instanceof Boolean )
			{
				Boolean bValue = (Boolean) features.get( sFeature );
				al.add( bValue );
			}
		}
		return al;
	}

	/** **************************************************************************************************************** */

	public String getText( DocumentReference reference ) throws AnalyserException {
		AbstractDocument doc = reference.getDocument();
		return getText( doc, reference.getStartOffset(), reference.getEndOffset() );
	}

	public String getText( AbstractDocument doc, Long lStart, Long lEnd ) throws AnalyserException {
		String sText = null;
		try {
			sText = m_preprocessor.getDocumentContent( lStart, lEnd, doc );
		} 
		catch ( InvalidOffsetException e ){
			throw new AnalyserException( "Invalid annotation offsets", e );
		}
		return sText;
	}

	public String getStemmedText( DocumentReference reference ) throws AnalyserException {
		StringBuffer sb = new StringBuffer();
		List tokenStems = getTokenStems( reference );
		Iterator iter = tokenStems.iterator();
		while ( iter.hasNext() )
		{
			sb.append( (String) iter.next() );
			if ( iter.hasNext() ){
				sb.append( " " );
			}
		}
		return sb.toString();
	}

	private String getText( Annotation ann, AbstractDocument doc ) throws AnalyserException {
		return getText( createDocumentReference( doc, ann ) );
	}

	public String toString() {
		return "LinguisticAnalyser: [ " + m_preprocessor + " ]";
	}
}