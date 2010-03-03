package org.ontoware.text2onto.algorithm.instance;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.algorithm.AbstractEntityExtraction; 


public abstract class AbstractInstanceExtraction extends AbstractEntityExtraction { 

	protected HashMap<POMEntity,List<DocumentReference>> getEntity2References( AbstractDocument doc ){
		HashMap<POMEntity,List<DocumentReference>> hmEntity2References = new HashMap<POMEntity,List<DocumentReference>>();
		List allReferences = m_analyser.getDocumentReferences( doc, LinguisticAnalyser.INSTANCE );
		Iterator pIter = allReferences.iterator();
		while( pIter.hasNext() )
		{
			DocumentReference entityReference = (DocumentReference)pIter.next();
			String sEntity = getEntity( entityReference );
			if( sEntity == null ){
				continue;
			}
			POMEntity entity = m_pom.newInstance( sEntity );
			List<DocumentReference> references = hmEntity2References.get( entity );
			if( references == null )
			{
				references = new ArrayList<DocumentReference>();
				hmEntity2References.put( entity, references );
			}
			references.add( entityReference );
		}
		return hmEntity2References;
	}
}