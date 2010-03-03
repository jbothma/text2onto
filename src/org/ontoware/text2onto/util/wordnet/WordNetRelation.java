package org.ontoware.text2onto.util.wordnet;

import net.didion.jwnl.data.Synset;


public class WordNetRelation {

	private Synset m_synset1;
	
	private Synset m_synset2;
	
	
	public WordNetRelation( Synset synset1, Synset synset2 ){
		m_synset1 = synset1;
		m_synset2 = synset2;
	}
}