package org.ontoware.text2onto.debug;

import java.net.URI;
import java.io.File;

import org.ontoware.text2onto.pom.POM;
import org.ontoware.text2onto.pom.POMFactory;
import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.corpus.CorpusFactory;
import org.ontoware.text2onto.corpus.DocumentFactory;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.algorithm.AbstractAlgorithm;
import org.ontoware.text2onto.algorithm.AlgorithmController;
import org.ontoware.text2onto.algorithm.ComplexAlgorithm;
import org.ontoware.text2onto.algorithm.concept.*; 
import org.ontoware.text2onto.algorithm.auxiliary.context.*; 
import org.ontoware.text2onto.algorithm.instance.*;
import org.ontoware.text2onto.algorithm.similarity.ContextSimilarityExtraction;
import org.ontoware.text2onto.algorithm.taxonomic.subclassOf.*;
import org.ontoware.text2onto.algorithm.taxonomic.instanceOf.*;
import org.ontoware.text2onto.algorithm.relation.general.*;
import org.ontoware.text2onto.algorithm.relation.subtopicOf.*; 
import org.ontoware.text2onto.algorithm.combiner.*;
import org.ontoware.text2onto.algorithm.normalizer.*;
import org.ontoware.text2onto.ontology.*;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;


public class TestSpanish {

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
		TestSpanish test = new TestSpanish( sCorpus, sOntology );
	}

	public TestSpanish( String sCorpusDir, String sOntology ) {
		try {
			Corpus corpus = CorpusFactory.newCorpus( sCorpusDir );
			System.out.println( "\n"+ corpus.toString() );

			POM pom = POMFactory.newPOM();

			AlgorithmController ac = new AlgorithmController( corpus, pom );
			// ac.setNormalizer( new Zero2OneNormalizer() );
			 
			ac.addAlgorithm( new TFIDFConceptExtraction() );
			ac.addAlgorithm( new TFIDFInstanceExtraction() );
			
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
			
			ac.addAlgorithm( new PatternInstanceClassification() );
			 
			// concept classification
			
			ComplexAlgorithm conceptClassification = new ComplexAlgorithm();
			conceptClassification.setCombiner( new AverageCombiner() );
			ac.addAlgorithm( conceptClassification );
			
			ac.addAlgorithmTo( conceptClassification, new PatternConceptClassification() );
			// ac.addAlgorithmTo( conceptClassification, new SpanishVerticalRelationsConceptClassification() ); 
			ac.addAlgorithmTo( conceptClassification, new SpanishWordNetConceptClassification() );
			
			// relation extraction
			
			ac.addAlgorithm( new SubcatRelationExtraction() );			
			  
			// subtopic-of
			
			// ComplexAlgorithm subtopicOfExtraction = new ComplexAlgorithm();
			// subtopicOfExtraction.setCombiner( new AverageCombiner() );
			// ac.addAlgorithm( subtopicOfExtraction );
			
			// ac.addAlgorithmTo( subtopicOfExtraction, new SubtopicOfRelationExtraction() );
			// ac.addAlgorithmTo( subtopicOfExtraction, new SubtopicOfRelationConversion() ); 
			
			// ac.addAlgorithm( new SubtopicOfRelationExtraction() );
			// ac.addAlgorithm( new SubtopicOfRelationConversion() );
			
			System.out.println( "\nAlgorithmController: "+ ac );
			
			ac.execute();
 
			// POMTable pomtable = new POMTable( pom );
			
			System.out.println( "\nPOM:\n"+ pom +"\n" );
			
			System.out.println( "\nPOM (details):\n"+ pom.toStringDetails() +"\n" );		
				 
			write( ac, sOntology +".rdfs" );
			// write( ac, sOntology +".kaon" );
			write( ac, sOntology +".owl" );
			// write( ac, sOntology +".ser" );
		} 
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	private void write( AlgorithmController ac, String sOntology ) throws Exception {
		System.err.println( "\nTestSpanish.write: "+ sOntology +"..." );
		POM pom = ac.getPOM();
		OntologyWriter writer = null; 
		if( sOntology.endsWith( ".rdf" ) || sOntology.endsWith( ".rdfs" ) ){
			writer = new RDFSWriter( pom ); 
		}
		else if( sOntology.endsWith( ".kaon" ) ){ 
			writer = new KAONWriter( pom ); 
		} 
		else if( sOntology.endsWith( ".owl" ) ){
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
}
