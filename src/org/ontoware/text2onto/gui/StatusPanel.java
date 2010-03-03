package org.ontoware.text2onto.gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class StatusPanel extends JPanel {

	private JTextArea m_debug;
	
	private JTextArea m_error;
	
	private JTabbedPane m_tabs;
	
	
	public StatusPanel(){
		setLayout( new BorderLayout() ); 
		m_tabs = new JTabbedPane();
		
		JPanel jpDebug = new JPanel();
		jpDebug.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		jpDebug.setLayout( new BorderLayout() );
		m_debug = new JTextArea();
		m_debug.setLineWrap( true );
		m_debug.setWrapStyleWord( false );
		m_debug.setEditable( false );
		m_debug.setBackground( Color.WHITE );
		JScrollPane spDebug = new JScrollPane( m_debug );
		jpDebug.add( BorderLayout.CENTER, spDebug );	
		m_tabs.add( "Debug", jpDebug );		
		
		JPanel jpError = new JPanel();
		jpError.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		jpError.setLayout( new BorderLayout() );
		m_error = new JTextArea();
		m_error.setLineWrap( true );
		m_error.setWrapStyleWord( false );
		m_error.setEditable( false );
		m_error.setBackground( Color.WHITE );
		JScrollPane spError = new JScrollPane( m_error );
		jpError.add( BorderLayout.CENTER, spError );	
		m_tabs.add( "Errors", jpError );

		add( BorderLayout.CENTER, m_tabs );
	}

	public void printDebug( String sText ){
		m_debug.append( "\n"+ sText +"\n" );
	}
	
	public void printError( String sText ){
		m_error.append( "\n"+ sText +"\n" );
	}
	
	public void reset(){
		m_debug.setText( "" );
		m_error.setText( "" );
	}
}