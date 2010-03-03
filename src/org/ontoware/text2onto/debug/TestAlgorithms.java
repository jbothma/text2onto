package org.ontoware.text2onto.debug;

import java.net.URI;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Collections;
import java.util.Set;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.corpus.*;
import org.ontoware.text2onto.algorithm.*;
import org.ontoware.text2onto.algorithm.concept.*; 
import org.ontoware.text2onto.algorithm.auxiliary.context.*; 
import org.ontoware.text2onto.algorithm.instance.*;
import org.ontoware.text2onto.algorithm.similarity.ContextSimilarityExtraction;
import org.ontoware.text2onto.algorithm.taxonomic.subclassOf.*;
import org.ontoware.text2onto.algorithm.taxonomic.instanceOf.*;
import org.ontoware.text2onto.algorithm.relation.general.*;
import org.ontoware.text2onto.algorithm.relation.subtopicOf.*;
import org.ontoware.text2onto.algorithm.axiom.*; 
import org.ontoware.text2onto.algorithm.combiner.*;
import org.ontoware.text2onto.algorithm.normalizer.*;
import org.ontoware.text2onto.ontology.*;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;
import org.ontoware.text2onto.util.ProbabilityComparator;
import org.ontoware.text2onto.change.*;


public class TestAlgorithms implements ProgressListener {

	public static void main( String args[] ) {
		String sCorpus = null;
		String sOntology = null;
		if ( args.length == 0 ) {
			System.exit( 0 );
		}
		if ( args.length > 0 ) {
			sCorpus = args[0];
		}
		if ( args.length > 1 ) {
			sOntology = args[1];
		}
		TestAlgorithms test = new TestAlgorithms( sCorpus, sOntology );
	}

	public TestAlgorithms( String sCorpusDir, String sOntology ) {
		try {
			Corpus corpus = CorpusFactory.newCorpus( sCorpusDir );
			System.out.println( "\n"+ corpus.toString() );

			POM pom = POMFactory.newPOM();

			AlgorithmController ac = new AlgorithmController( corpus, pom );
			// ac.setNormalizer( new Zero2OneNormalizer() );
			ac.addProgressListener( this );
			 
			// ac.addAlgorithm( new TFIDFConceptExtraction() );
			// ac.addAlgorithm( new TFIDFInstanceExtraction() );
			
			// similarity extraction
			
			// ContextSimilarityExtraction cse = new ContextSimilarityExtraction();
			// ac.addAlgorithmTo( cse, new ContextExtractionWithoutStopwords() );
			// ac.addAlgorithm( cse );
			
			// instance classification
			
			// ComplexAlgorithm instanceClassification = new ComplexAlgorithm();
			// instanceClassification.setCombiner( new AverageCombiner() );
			// ac.addAlgorithm( instanceClassification );
						
			// ac.addAlgorithmTo( instanceClassification, new ContextInstanceClassification() );
			// ac.addAlgorithmTo( instanceClassification, new PatternInstanceClassification() ); 
			
			// ac.addAlgorithm( new PatternInstanceClassification() );
			 
			// concept classification
			
			ComplexAlgorithm conceptClassification = new ComplexAlgorithm();
			conceptClassification.setCombiner( new AverageCombiner() );
			ac.addAlgorithm( conceptClassification );
			
			ac.addAlgorithmTo( conceptClassification, new PatternConceptClassification() );
			ac.addAlgorithmTo( conceptClassification, new VerticalRelationsConceptClassification() ); 
			ac.addAlgorithmTo( conceptClassification, new WordNetConceptClassification() );
			
			// relation extraction
			
			// ac.addAlgorithm( new SubcatRelationExtraction() );			
			  
			// subtopic-of
			
			// ComplexAlgorithm subtopicOfExtraction = new ComplexAlgorithm();
			// subtopicOfExtraction.setCombiner( new AverageCombiner() );
			// ac.addAlgorithm( subtopicOfExtraction );
			
			// ac.addAlgorithmTo( subtopicOfExtraction, new SubtopicOfRelationExtraction() );
			// ac.addAlgorithmTo( subtopicOfExtraction, new SubtopicOfRelationConversion() ); 
			
			// ac.addAlgorithm( new SubtopicOfRelationExtraction() );
			// ac.addAlgorithm( new SubtopicOfRelationConversion() );
			
			// disjointness
			
			// ac.addAlgorithm( new PatternDisjointClassesExtraction() );
			
			System.out.println( "\nAlgorithmController: "+ ac );
			
			ac.execute();
 
			// POMTable pomtable = new POMTable( pom );
			
			System.out.println( "\nPOM:\n"+ pom +"\n" );
			
			System.out.println( "\nPOM (details):\n"+ pom.toStringDetails() +"\n" );		
			
			StringBuffer sb = new StringBuffer();
			List objects = pom.getObjects( POMSubclassOfRelation.class );
			for( int i=0; i<objects.size(); i++ ){
				POMSubclassOfRelation rel = (POMSubclassOfRelation)objects.get(i);
				sb.append( rel.getDomain() );
				sb.append( ", "+ rel.getRange() );
				sb.append( ", "+ rel.getProbability() +"\n" );
			}
			write( sb.toString(), "/Volumes/Dev/temp/text2onto.txt" );
			
			// write( ac, sOntology +".rdfs" );
			// write( ac, sOntology +".kaon" );
			// write( ac, sOntology +".owl" );
			// write( ac, sOntology +".ser" );
			
			// writeSplit( ac, sOntology, 10 );
		} 
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private static File write( String s, String sFile ) throws Exception {
	      File file = new File( sFile );
	      if( !file.exists() ) {
	         file.createNewFile();
	      }
	      else {
	      	System.out.println( "TestAlgorithms.write: existing file! "+ sFile );
	      	return null;
	      }
	      FileWriter writer = null;
	      try {
	         writer = new FileWriter( file );
	         writer.write( s, 0, s.length() );
	      }
	      finally {
	         writer.close();
	      }
	      return file;
	   }
		
	private void write( AlgorithmController ac, String sOntology ) throws Exception {
		System.err.println( "\nTestAlgorithms.write: "+ sOntology +"..." );
		POM pom = ac.getPOM();
		OntologyWriter writer = null; 
		if( sOntology.endsWith( ".rdf" ) || sOntology.endsWith( ".rdfs" ) ){
			writer = new RDFSWriter( pom ); 
		}
		else if( sOntology.endsWith( ".kaon" ) ){ 
			writer = new KAONWriter( pom ); 
		} 
		else if( sOntology.endsWith( ".owl" ) ){
			// writer = new SimpleOWLWriter( pom );
			writer = new OWLWriter( pom );
		}
		else if( sOntology.endsWith( ".ser" ) ){
			File file = new File( new URI( sOntology ) );
			pom.save( file.toString() );
			writer = null;
		}
		if( writer != null ){
			writer.setReferenceManager( ac.getReferenceManager() );
			writer.setEvidenceManager( ac.getEvidenceManager() ); 
			writer.write( new URI( sOntology ) ); 
		}
	}
	
	private void writeSplit( AlgorithmController ac, String sOntology, int iSplits ) throws Exception {
		System.err.println( "\nTestAlgorithms.writeSplit: "+ sOntology +"..." );
		POM pom = ac.getPOM();
		List objects = pom.getObjects();
		Collections.sort( objects, new ProbabilityComparator() );
		int iSplitSize = (int)( objects.size() / iSplits );
		for( int i=1; i<iSplits; i++ )
		{
			POMWrapper part = new POMWrapper( POMFactory.newPOM() );
			for( int j=0; j<iSplitSize*i; j++ )
			{
				POMObject object = (POMObject)objects.get(j);
				ChangeRequest changeRequest = new ChangeRequest( new POMChange( Change.Type.ADD, 
					this, (POMAbstractObject) object.deepCopy(), (Change)null ) );
	         part.processChangeRequest( changeRequest );

			}
			writeSplit( ac, part.getChangeable(), sOntology, i );
		}
		writeSplit( ac, pom, sOntology, iSplits );
	}
	
	private void writeSplit( AlgorithmController ac, POM pom, String sOntology, int iId ) throws Exception {
		System.err.println( "\nTestAlgorithms.writeSplit: "+ sOntology +"("+ iId +")..." );
		System.out.println( "\nPOM #"+ iId +" ("+ pom.getObjects().size() +"):\n"+ pom.toStringDetails() +"\n" );
		OntologyWriter writer = new OWLWriter( pom );
		writer.setReferenceManager( ac.getReferenceManager() );
		writer.setEvidenceManager( ac.getEvidenceManager() );
		writer.write( new URI( sOntology + iId +".owl" ) ); 
	}
	
	public void progressChanged( String sAlgorithm, int iAlgorithm, int iAll ){
		System.out.println( "\nTestAlgorithms.progressChanged: "+ sAlgorithm +" ("+ iAlgorithm +"/"+ iAll +")" );
	}
}
