package org.ontoware.text2onto.util;

import java.util.Comparator;

import org.ontoware.text2onto.pom.POMObject;


public class ProbabilityComparator implements Comparator {

	public int compare( Object obj1, Object obj2 ){
		double d1 = ((POMObject)obj1).getProbability();
		double d2 = ((POMObject)obj2).getProbability();
		if( d1 < d2 ){
			return 1;
		}
		else if( d1 > d2 ){
			return -1;
		}
		else {
			return 0;
		}
	}
	
	public boolean equals( Object obj1, Object obj2 ){
		double d1 = ((POMObject)obj1).getProbability();
		double d2 = ((POMObject)obj2).getProbability();
		if( d1 == d2 ){
			return true;
		}
		else {
			return false;
		}
	}
}
