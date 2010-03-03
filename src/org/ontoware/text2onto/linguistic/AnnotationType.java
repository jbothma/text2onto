package org.ontoware.text2onto.linguistic;


public interface AnnotationType {

	// ontological annotation types

	public final static String CONCEPT = "Concept";

	public final static String INSTANCE = "Instance";

	public final static String SUBCLASS_OF = "SubclassOfRelation";

	public final static String INSTANCE_OF = "InstanceOfRelation";

	public final static String PART_OF = "PartOfRelation";
	
	public final static String DISJOINT_CLASSES = "DisjointClasses";

	public final static String DOMAIN = "Domain";

	public final static String RANGE = "Range";

	// linguistic annotation types

	public final static String NOUN_PHRASE = "NounPhrase";

	public final static String PROPER_NOUN_PHRASE = "ProperNounPhrase";
	
	public final static String CONTEXT_VERB_PHRASE = "ContextTransitiveVerbPhrase";

	public final static String VERB_PHRASE_TRANSITIVE = "TransitiveVerbPhrase";

	public final static String VERB_PHRASE_TRANSITIVE_PP = "TransitivePPVerbPhrase";

	public final static String VERB_PHRASE_INTRANSITIVE_PP = "IntransitivePPVerbPhrase";

	public final static String HEAD = "Head";

	public final static String VERB = "Verb";

	public final static String SUBJECT = "Subject";

	public final static String OBJECT = "Object";

	public final static String POBJECT = "PObject";

	public final static String PREPOSITION = "Preposition";
	
	public final static String PREPOSITIONAL_PHRASE = "PrepositionalPhrase";
	
	//public final static String SUBORDINATE_PHRASE = "SubordinatePhrase";
	
	public final static String ADJECTIVE = "Adjective";
	
	public final static String PROPER_NOUN = "ProperNoun";
}

