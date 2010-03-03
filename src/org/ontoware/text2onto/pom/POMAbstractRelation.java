package org.ontoware.text2onto.pom;


public interface POMAbstractRelation extends POMAbstractObject {

	public POMEntity getDomain();
	
	public POMEntity getRange();

	public static class Type {
		public final static String UNKNOWN = "related-to";
		public final static String PART_OF = "part-of";
		public final static String SIMILAR_TO = "similar-to";
		public final static String SUBCLASS_OF = "subclass-of";
		public final static String INSTANCE_OF = "instance-of";
	}
}

