package org.ontoware.text2onto.util;

import java.io.Serializable;


public class MyDouble implements Serializable {

	private double m_dValue = 0;


	public MyDouble( double dValue ){
		m_dValue = dValue;
	}

	public void increase(){
		m_dValue++;
	}

	public void increase( double dValue ){
		m_dValue += dValue;
	}

	public void decrease(){
		m_dValue--;
	}

	public void setValue( double dValue ){
		m_dValue = dValue;
	}

	public double getValue(){
		return m_dValue;
	}

	public String toString(){
		return String.valueOf( m_dValue );
	}
}