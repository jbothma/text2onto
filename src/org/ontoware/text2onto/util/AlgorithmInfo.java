package org.ontoware.text2onto.util;

/**
 * @author simon
 */
public class AlgorithmInfo {

	private String sName;

	private String sClass;

	public AlgorithmInfo() {}

	/**
	 * @return Returns the sClass.
	 */
	public String getClassPath() {
		return sClass;
	}

	/**
	 * @param class1 The sClass to set.
	 */
	public void setClassPath( String class1 ) {
		sClass = class1;
	}

	/**
	 * @return Returns the sName.
	 */
	public String getName() {
		return sName;
	}

	/**
	 * @param name The sName to set.
	 */
	public void setName( String name ) {
		sName = name;
	}
}