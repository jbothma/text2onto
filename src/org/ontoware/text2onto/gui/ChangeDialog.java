package org.ontoware.text2onto.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

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
public class ChangeDialog extends JDialog {

	private DynamicTree m_tree;

	private JButton m_buttonOk; 
	
	private JTextArea m_explanationArea;
	 
	private HashMap<POMChange,List<AbstractExplanation>> m_hmChange2Explanations;	 
	 

	public ChangeDialog( JFrame owner, List<POMChange> changes, HashMap<POMChange,List<AbstractExplanation>> hmChange2Explanations ){
		super( owner, "Changes", true );
		setSize( 700, 600 );
		Dimension dim = getToolkit().getScreenSize();
		setLocation( (int)( ( dim.getWidth() - this.getWidth() ) / 2 ), 
						(int)( ( dim.getHeight() - this.getHeight() ) / 2 ) );
		m_hmChange2Explanations = hmChange2Explanations; 
		m_tree = new DynamicTree( "Changes", DynamicTree.ICONS_CORPUS, false );  
		for( int i=0; i<changes.size(); i++ )
		{
			Change change = (Change)changes.get(i);
			List<Change> causes = change.getCauses();
			addNodes( m_tree.addObject( change ), causes ); 
		}
		m_tree.addSelectionListener( new TreeSelectionListener(){
			public void valueChanged( TreeSelectionEvent e )
			{
				m_tree.selectionChanged( e );
				doExplanation();
			}
		}); 
		
		m_buttonOk = new JButton( "Ok" );
		m_buttonOk.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){ 
				dispose();
			}
		}); 
		Container cp = getContentPane();
		cp.setLayout( new BorderLayout() );
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new GridLayout(2,1) );
		 	
		JPanel treePanel = new JPanel();
		treePanel.setLayout( new BorderLayout() );
		treePanel.setBorder( new EmptyBorder( 10, 10, 0, 10 ) );	 
		treePanel.add( BorderLayout.CENTER, m_tree );
		mainPanel.add( treePanel );
		
		JPanel exPanel = new JPanel();
		exPanel.setLayout( new BorderLayout() );
		exPanel.setBorder( new EmptyBorder( 10, 10, 0, 10 ) );
		m_explanationArea = new JTextArea( "" );
		m_explanationArea.setEditable( false ); 
		m_explanationArea.setLineWrap( true );
		m_explanationArea.setWrapStyleWord( false ); 
		exPanel.add( BorderLayout.CENTER, new JScrollPane( m_explanationArea ) );
		mainPanel.add( exPanel );
		
		cp.add( BorderLayout.CENTER, mainPanel );
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		buttonPanel.add( m_buttonOk ); 
		cp.add( BorderLayout.SOUTH, buttonPanel );
	}
	
	private void doExplanation(){
		Object object = m_tree.getSelectedObject();
		if( object == null || !( object instanceof Change ) ){
			return;
		}
		Change change = (Change)object; 
		String sText = change.toString(); 
		List<AbstractExplanation> explanations = m_hmChange2Explanations.get( change ); 
		if( explanations != null )
		{
			for( AbstractExplanation explanation: explanations ){
				if( explanation != null ){
					sText += "\n\n"+ change.getSource().getClass().getSimpleName() +": "+ explanation.getText();
				}
			}
		}
		m_explanationArea.setText( sText );
	}
	
	private void addNodes( Object parent, List children ){ 
		List childNodes = new ArrayList();
		Iterator iter = children.iterator();
		while( iter.hasNext() )
		{
			Change child = (Change)iter.next(); 
			Object childNode = m_tree.addObject( parent, child );
			childNodes.add( childNode );
			addNodes( childNode, child.getCauses() );
		} 
	}
}