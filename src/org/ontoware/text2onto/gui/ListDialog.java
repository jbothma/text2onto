package org.ontoware.text2onto.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ListDialog extends JDialog {

	private JList m_list;

	private JButton m_buttonOk;

	private JButton m_buttonCancel;

	private List m_items;
	
	private HashMap m_hmName2Item;
	 

	public ListDialog( JFrame owner, List items, List names ){
		super( owner, "Algorithms", true );
		setSize( 300, 300 );
		Dimension dim = getToolkit().getScreenSize();
		setLocation( (int)( dim.getWidth() / 2 ) - 150, (int)( dim.getHeight() / 2 ) - 150 ); 
		m_hmName2Item = new HashMap();
		for( int i=0; i<items.size(); i++ )
		{
			Object item = items.get(i);
			Object name = names.get(i);
			m_hmName2Item.put( name, item );
		}
		
		m_buttonOk = new JButton( "Ok" );
		m_buttonOk.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				m_items = getItems();
				dispose();
			}
		});
		m_buttonCancel = new JButton( "Cancel" );
		m_buttonCancel.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){ 
				m_items = new ArrayList();
				dispose();
			}
		}); 
		Container cp = getContentPane();
		cp.setLayout( new BorderLayout() );
		 	
		JPanel listPanel = new JPanel();
		listPanel.setLayout( new BorderLayout() );
		listPanel.setBorder( new EmptyBorder( 10, 10, 0, 10 ) );
		m_list = new JList( names.toArray() );
		JScrollPane sp = new JScrollPane( m_list );
		listPanel.add( BorderLayout.CENTER, sp );
		cp.add( BorderLayout.CENTER, listPanel );
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		buttonPanel.add( m_buttonOk );
		buttonPanel.add( m_buttonCancel );
		cp.add( BorderLayout.SOUTH, buttonPanel );
	}
	
	public List getSelectedItems(){
		return m_items;
	}
 
	private List getItems(){
		ArrayList items = new ArrayList();
		Object[] names = m_list.getSelectedValues();
		for( int i=0; i<names.length; i++ ){
			items.add( m_hmName2Item.get( names[i] ) );
		}
		return items;
	}
}