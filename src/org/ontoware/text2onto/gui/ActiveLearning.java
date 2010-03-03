package org.ontoware.text2onto.gui;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.Dimension;
import java.awt.Container;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;

import org.ontoware.text2onto.pom.POM; 
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMInstance;
import org.ontoware.text2onto.pom.POMInstanceOfRelation;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.corpus.AbstractDocument;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ActiveLearning extends JDialog {

	private JComboBox m_cbConcept;

	private JTextField m_tfLower;
	
	private JTextField m_tfUpper;
	
	private JButton m_buttonApply;
	
	 
	private DefaultListModel m_modelPos;
	
	private DefaultListModel m_modelNeg;
	
	private DefaultListModel m_modelUnknown;	
	
	
	private JPopupMenu m_popup;
	
	private POMInstance m_selectedInstance;
	
	private JList m_selectedList;
	

	private JButton m_buttonOk;
	
	private JButton m_buttonCancel;
	
	
	private Border m_border;
	
	
	private POM m_pom;
	
	private Corpus m_corpus;
	
	
	private Comparator m_comparator;
	
	
	private double m_dLower = 0.2;
	
	private double m_dUpper = 0.8;
	
	
	private POMConcept m_concept;
	
	private ArrayList<POMListener> m_listeners;
	
	private JFrame m_owner;
		
	 
	public ActiveLearning( JFrame owner, Corpus corpus, POM pom ){
		super( owner, "Active Learning", true );
		m_owner = owner;
		m_pom = pom;
		m_corpus = corpus;
		m_listeners = new ArrayList<POMListener>();
		
		m_comparator = ( new Comparator(){
			public int compare( Object o1, Object o2 ){
				return o1.toString().compareTo( o2.toString() );
			}
         public boolean equals( Object obj ){
         	return false;
         } 
		});				
		setSize( 700, 600 );
		Dimension dim = getToolkit().getScreenSize();
		setLocation( (int)( ( dim.getWidth() - this.getWidth() ) / 2 ), 
						(int)( ( dim.getHeight() - this.getHeight() ) / 1.5 ) );
				
		Container cp = getContentPane();
		cp.setLayout( new BorderLayout() ); 
		
		m_border = BorderFactory.createCompoundBorder( new EmptyBorder( 10, 10, 10, 10 ), 
			BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
	
		// input panel
				
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout( new BorderLayout() );
		inputPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) ); 
		
		// concept panel
	
		JPanel conceptPanel = new JPanel();
		conceptPanel.setLayout( new GridLayout( 1, 1 ) );
		conceptPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		
		m_cbConcept = new JComboBox();
		m_cbConcept.addItemListener( new ItemListener(){
			public void itemStateChanged( ItemEvent e ){
				if( e.getStateChange() == ItemEvent.SELECTED ){
					m_concept = (POMConcept)e.getItem(); 
				}
			}
		});
		conceptPanel.add( m_cbConcept );
		
		List concepts = m_pom.getObjects( POMConcept.class );
		Collections.sort( concepts, m_comparator );
		for( Object concept: concepts )
		{
			// TODO: only concepts with instances
			m_cbConcept.addItem( (POMConcept)concept );
		}
		
		// apply panel
		
		JPanel applyPanel = new JPanel();
		applyPanel.setLayout( new GridLayout( 1, 3 ) );
		applyPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
				
		m_tfLower = new JTextField( String.valueOf( m_dLower ) );
		m_tfUpper = new JTextField( String.valueOf( m_dUpper ) );
		m_buttonApply = new JButton( "Apply" ); 
		m_buttonApply.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e )
			{
				m_dLower = Double.parseDouble( m_tfLower.getText().trim() );
				m_dUpper = Double.parseDouble( m_tfUpper.getText().trim() );
				doApply();
			}
		});
		applyPanel.add( m_tfLower );
		applyPanel.add( m_tfUpper );
		applyPanel.add( m_buttonApply );
		
		inputPanel.add( BorderLayout.NORTH, conceptPanel );
		inputPanel.add( BorderLayout.SOUTH, applyPanel );
		
		cp.add( BorderLayout.NORTH, inputPanel );
		
		// list panel
		
		m_modelPos = new DefaultListModel();
		m_modelNeg = new DefaultListModel();
		m_modelUnknown = new DefaultListModel();

		JList listPos = new JList( m_modelPos );
		JList listNeg = new JList( m_modelNeg );
		JList listUnknown = new JList( m_modelUnknown );
		
		listPos.addMouseListener( new PopupListener() );
		listNeg.addMouseListener( new PopupListener() );
		listUnknown.addMouseListener( new PopupListener() );
		
		listPos.setBackground( java.awt.Color.WHITE );
		listNeg.setBackground( java.awt.Color.WHITE );
		listUnknown.setBackground( java.awt.Color.WHITE );
		
		listPos.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		listNeg.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		listUnknown.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		
		JPanel panelPos = new JPanel();
		panelPos.setLayout( new BorderLayout() );
		panelPos.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "positive" ), m_border ) ); 
		JScrollPane scrollPos = new JScrollPane( listPos );
		panelPos.add( BorderLayout.CENTER, scrollPos );
		
		JPanel panelNeg = new JPanel();
		panelNeg.setLayout( new BorderLayout() );
		panelNeg.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "negative" ), m_border ) ); 
		JScrollPane scrollNeg = new JScrollPane ( listNeg );
		panelNeg.add( BorderLayout.CENTER, scrollNeg );
		
		JPanel panelUnknown = new JPanel();
		panelUnknown.setLayout( new BorderLayout() );
		panelUnknown.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "unknown" ), m_border ) ); 
		JScrollPane scrollUnknown = new JScrollPane( listUnknown );
		panelUnknown.add( BorderLayout.CENTER, scrollUnknown );	
		 
		JPanel listPanel = new JPanel();
		listPanel.setLayout( new GridLayout( 3, 1 ) );
		listPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );	 
		listPanel.add( panelPos );
		listPanel.add( panelNeg );
		listPanel.add( panelUnknown ); 
	 
		cp.add( BorderLayout.CENTER, listPanel );
		
		// button panel
		
		m_buttonOk = new JButton( "Ok" );
		m_buttonOk.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){ 
				dispose();
				HashMap<String,Double> hmLabel2Prob = new HashMap<String,Double>();
				
				// debug
				hmLabel2Prob.put( "instance-1", 0.1 );
				hmLabel2Prob.put( "instance-2", 0.2 );
				hmLabel2Prob.put( "instance-3", 0.3 );
				
				ActiveLearningResults alr = new ActiveLearningResults( m_owner, getResults( hmLabel2Prob ) );
				alr.show();
				List<POMInstanceOfRelation> results = alr.getResults();
				if( results != null ){
					notifyListeners( POMListener.ADD, results );
				}
			}
		}); 
		m_buttonCancel = new JButton( "Cancel" );
		m_buttonCancel.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				dispose();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		buttonPanel.add( m_buttonCancel ); 
		buttonPanel.add( m_buttonOk );
		
		cp.add( BorderLayout.SOUTH, buttonPanel );
		
		m_popup = createPopupMenu();
	}
	
	private JPopupMenu createPopupMenu(){
		JPopupMenu menu = new JPopupMenu();
		
		JMenuItem itemPos = new JMenuItem( "positive" );
		itemPos.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doInstance( new Boolean( true ) );
			}
		});	 
		JMenuItem itemNeg = new JMenuItem( "negative" );
		itemNeg.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doInstance( new Boolean( false ) );
			}
		}); 		
		JMenuItem itemUnknown = new JMenuItem( "Unknown" );
		itemUnknown.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doInstance( null );
			}
		}); 
		menu.add( itemPos );
		menu.add( itemNeg );
		menu.add( itemUnknown ); 
		return menu;
	}
	
	private void doInstance( Boolean bClass ){
		if( m_selectedInstance == null ){
			return;
		}
		if( bClass == null ){
			m_modelUnknown.insertElementAt( m_selectedInstance, 0 );	
		}
		else if( bClass.booleanValue() == true ){
			m_modelPos.insertElementAt( m_selectedInstance, 0 );
		}
		else {
			m_modelNeg.insertElementAt( m_selectedInstance, 0 );
		}
		((DefaultListModel)m_selectedList.getModel()).removeElement( m_selectedInstance );
	}
		
	private void doApply(){
		if( m_concept == null ){
			return;
		}
		m_modelPos.clear();
		m_modelNeg.clear();
		m_modelUnknown.clear();
		
		ArrayList<POMInstance> pos = new ArrayList<POMInstance>();
		ArrayList<POMInstance> neg = new ArrayList<POMInstance>();
		ArrayList<POMInstance> unknown = new ArrayList<POMInstance>();
				
		List<POMAbstractRelation> relations = m_pom.getRelationsWithRange( POMInstanceOfRelation.class, m_concept );
		for( POMAbstractRelation relation: relations )
		{
			POMInstance instance = (POMInstance)relation.getDomain();
			double dProb = relation.getProbability();
			if( dProb < m_dLower && !pos.contains( instance ) ){
				neg.add( instance );
			}
			else if( dProb > m_dUpper && !neg.contains( instance ) ){
				pos.add( instance );
			}
			else if( !unknown.contains( instance ) ){
				unknown.add( instance );
			}
		}	
		List<POMInstance> instances = m_pom.getObjects( POMInstance.class );
		for( POMInstance instance: instances ){
			if( !pos.contains( instance ) 
				&& !neg.contains( instance ) 
				&& !unknown.contains( instance ) )
			{
				unknown.add( instance );
			}
		}	
		Collections.sort( pos, m_comparator );
		Collections.sort( neg, m_comparator );
		Collections.sort( unknown, m_comparator );
		
		int i=0;
		for( POMInstance instance: pos ){
			m_modelPos.add( i++, instance );
		}
		i=0;
		for( POMInstance instance: neg ){
			m_modelNeg.add( i++, instance );
		}
		i=0;
		for( POMInstance instance: unknown ){
			m_modelUnknown.add( i++, instance );
		}
	}
	
	private class PopupListener extends MouseAdapter
	{ 
		public void mousePressed( MouseEvent e ) {
			maybeShowPopup( e );
		}
		public void mouseReleased( MouseEvent e ) {
			maybeShowPopup( e );
		}
		private void maybeShowPopup( MouseEvent e ) {
			if( e.isPopupTrigger() )
			{
				m_selectedList = (JList)e.getComponent(); 
				m_selectedInstance = (POMInstance)m_selectedList.getSelectedValue();
				m_popup.show( e.getComponent(), e.getX(), e.getY() );
			}
		}
	}
	
	public POMConcept getConcept(){
		return m_concept;
	}
	
	public List<AbstractDocument> getDocuments(){
		return m_corpus.getDocuments();
	}
	
	private List<POMInstance> getInstances( DefaultListModel model ){
		ArrayList<POMInstance> instances = new ArrayList<POMInstance>();
		Enumeration elements = model.elements();
		while( elements.hasMoreElements() ){
			instances.add( (POMInstance)elements.nextElement() );
		}
		return instances;
	}
	
	public List<POMInstance> getPositive(){
		return getInstances( m_modelPos );
	}
	
	public List<POMInstance> getNegative(){
		return getInstances( m_modelNeg );
	}
	
	public List<POMInstance> getUnknown(){
		return getInstances( m_modelUnknown );
	}
	
	private List<POMInstanceOfRelation> getResults( HashMap<String,Double> hmLabel2Prob ){
		ArrayList<POMInstanceOfRelation> relations = new ArrayList<POMInstanceOfRelation>();
		for( String sLabel: hmLabel2Prob.keySet() )
		{
			POMInstance instance = m_pom.newInstance( sLabel );
			POMInstanceOfRelation relation = m_pom.newInstanceOfRelation( instance, m_concept );
			Double dProb = hmLabel2Prob.get( sLabel );
			relation.setProbability( dProb );
			relations.add( relation );
		}
		return relations;
	}
	
	public void addListener( POMListener listener ){
		m_listeners.add( listener );
	}
	
	private void notifyListeners( int iMessage, List<? extends POMObject> objects ){
		for( POMListener listener: m_listeners ){
			listener.pomChanged( iMessage, objects );
		}
	}
}