package org.ontoware.text2onto.debug;

import java.util.*;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.explanation.*;
import org.ontoware.text2onto.change.*;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class PatternAlgorithm extends AbstractSimpleAlgorithm {

	private String m_sAnnType = null;


	public PatternAlgorithm(){}

	public PatternAlgorithm( String sAnnType ){
		m_sAnnType = sAnnType;
	}
	
	public void initialize(){
		// TODO
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception { 
		List documents = m_corpus.getDocuments();
		Iterator iter = documents.iterator();
		while( iter.hasNext() )
		{
			AbstractDocument doc = (AbstractDocument)iter.next();
			System.out.println( "PatternAlgorithm: "+ doc.getURI() ); 
			List anns = m_analyser.getDebug( doc, m_sAnnType ); 
			Iterator annIter = anns.iterator();
			while( annIter.hasNext() )
			{
				String sAnn = (String)annIter.next();
				System.out.println( sAnn );
			}
		}
		return new ArrayList(); 
	}
	 
	protected AbstractExplanation getExplanation( POMChange change ) throws Exception {
		return null;
	}
	
	protected List<ChangeRequest> getPOMChanges() throws Exception {
		return new ArrayList<ChangeRequest>();
	}
	
	protected List<ChangeRequest> getReferenceChanges() throws Exception {
		return new ArrayList<ChangeRequest>();
	}
}
