package org.ontoware.text2onto.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.List; 
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class DocumentDialog extends JDialog {
 
	private JButton m_buttonClose; 
	
	private JTextArea m_textArea;
	  

	public DocumentDialog( JFrame owner, String sFile ){
		super( owner, sFile, true );
		setSize( 700, 600 );
		Dimension dim = getToolkit().getScreenSize();
		setLocation( (int)( ( dim.getWidth() - this.getWidth() ) / 2 ), 
						(int)( ( dim.getHeight() - this.getHeight() ) / 2 ) );
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout( new BorderLayout() );
		textPanel.setBorder( new EmptyBorder( 10, 10, 0, 10 ) ); 
		String sText = null;
		try {
			sText = getText( sFile );
		}
		catch( Exception e ){
			sText = e.toString();
		}
		m_textArea = new JTextArea( sText );
		m_textArea.setEditable( false ); 
		m_textArea.setLineWrap( true );
		m_textArea.setWrapStyleWord( false ); 
		textPanel.add( BorderLayout.CENTER, new JScrollPane( m_textArea ) );
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		 				
		m_buttonClose = new JButton( "Ok" );
		m_buttonClose.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){ 
				dispose();
			}
		}); 
		buttonPanel.add( m_buttonClose );
		
		Container cp = getContentPane();
		cp.setLayout( new BorderLayout() );
		cp.add( BorderLayout.CENTER, textPanel );
		cp.add( BorderLayout.SOUTH, buttonPanel );
	}
	
	private String getText( String sFile ) throws Exception {
		StringBuffer sb = new StringBuffer();
		File file = new File( sFile );
		BufferedReader reader = new BufferedReader( new FileReader( file ) );
		String sLine = null;
		while( ( sLine = reader.readLine() ) != null ){
			sb.append( sLine +"\n" );
		}
		return sb.toString();
	}
}