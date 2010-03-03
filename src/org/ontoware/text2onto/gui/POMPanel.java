package org.ontoware.text2onto.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.util.ProbabilityComparator;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class POMPanel extends JPanel {

	private JTabbedPane m_tabs;
	
	private JTable[] m_tables;
	
	private JPopupMenu m_popup;
	
	private POMObject m_object;
	
	private final static String[] m_sTitles = { "Concept", "Instance", "Similiarity", 
		"SubclassOf", "InstanceOf", "Relation", "SubtopicOf" }; // , "Disjoint" };
	
	private final static int CONCEPTS = 0;		
	private final static int INSTANCES = 1;
	private final static int SIMILARITY = 2;	
	private final static int SUBCLASS_OF = 3;
	private final static int INSTANCE_OF = 4;
	private final static int RELATIONS = 5;
	private final static int SUBTOPIC_OF = 6;
	private final static int DISJOINT = 7; 
			 
	private POM m_pom;
	
	private Text2Onto m_text2onto;
	
	private JFrame m_frame;
		 

	public POMPanel( JFrame frame, Text2Onto text2onto, POM pom ){
		m_pom = pom;
		m_frame = frame;
		m_text2onto = text2onto;
		setLayout( new BorderLayout() );
		m_tabs = new JTabbedPane();
		m_tables = new JTable[m_sTitles.length];
		for( int i=0; i<m_tables.length; i++ )
		{ 
			AbstractDataModel normalModel = createDataModel(i); 
			// TableSorter sortedModel = new TableSorter( normalModel ); 
			// m_tables[i] = new POMTable( sortedModel ); 
			m_tables[i] = new POMTable( normalModel );
			((POMTable)m_tables[i]).setDataModel( normalModel );
			// sortedModel.addMouseListenerToHeaderInTable( m_tables[i] );
			m_tables[i].setDragEnabled( false );
			m_tables[i].setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			m_tables[i].setColumnSelectionAllowed( false ); 
			m_tables[i].addMouseListener( new PopupListener() ); 
			m_tables[i].setDefaultRenderer( Object.class, new ColorTableCellRenderer(i) );
 
			JPanel panel = new JPanel();
			panel.setLayout( new BorderLayout() );
			panel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
			JScrollPane scrollPane = new JScrollPane( m_tables[i] );
			panel.add( BorderLayout.CENTER, scrollPane ); 
			m_tabs.add( m_sTitles[i], panel ); 
		}
		add( BorderLayout.CENTER, m_tabs );
		m_popup = createPopupMenu();
	}
	
	private JPopupMenu createPopupMenu(){
		JPopupMenu menu = new JPopupMenu();
		
		JMenuItem itemChanges = new JMenuItem( "Changes" );
		itemChanges.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doChanges();
			}
		});		
		JMenu menuFeedback = new JMenu( "Feedback..." );
		menuFeedback.setEnabled( true );
		
		JMenuItem itemTrue = new JMenuItem( "True" );
		itemTrue.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doFeedback( new Boolean( true ) );
			}
		});
		menuFeedback.add( itemTrue );
		
		JMenuItem itemFalse = new JMenuItem( "False" );
		itemFalse.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doFeedback( new Boolean( false ) );
			}
		});
		menuFeedback.add( itemFalse );
		
		JMenuItem itemUnknown = new JMenuItem( "Don't know" );
		itemUnknown.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				doFeedback( null );
			}
		});
		menuFeedback.add( itemUnknown );
		
		menu.add( itemChanges );
		menu.add( menuFeedback ); 
		return menu;
	}
	
	private void doChanges(){
		if( m_object == null ){
			return;
		}
		List changes = m_pom.getChanges( m_object );
		HashMap<POMChange,List<AbstractExplanation>> hmChange2Explanations = m_text2onto.getExplanations( changes );
		ChangeDialog cd = new ChangeDialog( m_frame, changes, hmChange2Explanations );
		cd.show();
	}
	
	private void doFeedback( Boolean bCorrect ){
		if( m_object == null ){
			return;
		}
		m_object.setUserEvidence( bCorrect );
		update();
	}
	
	public void reset( POM pom ){
		m_pom = pom;
		update();
	}
	
	public void update(){
		for( int i=0; i<m_tables.length; i++ )
		{
			// TableSorter model = (TableSorter)m_tables[i].getModel();
			// AbstractDataModel model = (AbstractDataModel)m_tables[i].getModel();
			// model.tableChanged( new TableModelEvent( model ) );
			m_tables[i].revalidate();
			m_tables[i].repaint();
		}
	}

	private AbstractDataModel createDataModel( int iType ){
		switch( iType ){
			case CONCEPTS: return new DataModelConcepts();
			case INSTANCES: return new DataModelInstances();
			case SUBCLASS_OF: return new DataModelSubclassOf();
			case INSTANCE_OF: return new DataModelInstanceOf();
			case SUBTOPIC_OF: return new DataModelSubtopicOf();
			case RELATIONS: return new DataModelRelations();
			case SIMILARITY: return new DataModelSimilarity();
		}
		return null;
	}
	
	private abstract class AbstractDataModel extends DefaultTableModel {
		public abstract Object getValueAt( int iRow );
	}
	
	private class DataModelConcepts extends AbstractDataModel {
		private String[] sColumns = { "Label", "Relevance" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( CONCEPTS ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( CONCEPTS ); 
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMConcept object = (POMConcept)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getLabel();
			} else {
				return new Double( object.getProbability() );
			}
		}
	}
	
	private class DataModelInstances extends AbstractDataModel {
		private String[] sColumns = { "Label", "Relevance" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( INSTANCES ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( INSTANCES );
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMInstance object = (POMInstance)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getLabel();
			} else {
				return new Double( object.getProbability() );
			}
		}
	}
	
	private class DataModelSubclassOf extends AbstractDataModel {
		private String[] sColumns = { "Domain", "Range", "Confidence" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( SUBCLASS_OF ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( SUBCLASS_OF );
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMSubclassOfRelation object = (POMSubclassOfRelation)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getDomain().getLabel();
			} else if( iCol == 1 ){
				return object.getRange().getLabel();
			} else {
				return new Double( object.getProbability() ); 
			}
		}
	}
	
	private class DataModelInstanceOf extends AbstractDataModel {
		private String[] sColumns = { "Domain", "Range", "Confidence" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( INSTANCE_OF ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( INSTANCE_OF );
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMInstanceOfRelation object = (POMInstanceOfRelation)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getDomain().getLabel();
			} else if( iCol == 1 ){
				return object.getRange().getLabel();
			} else {
				return new Double( object.getProbability() );
			}
		}
	}
	
	private class DataModelSubtopicOf extends AbstractDataModel {
		private String[] sColumns = { "Domain", "Range", "Confidence" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( SUBTOPIC_OF ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( SUBTOPIC_OF );
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMSubtopicOfRelation object = (POMSubtopicOfRelation)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getDomain().getLabel();
			} else if( iCol == 1 ){
				return object.getRange().getLabel();
			} else {
				return new Double( object.getProbability() ); 
			}
		}
	}
	
	private class DataModelRelations extends AbstractDataModel {
		private String[] sColumns = { "Label", "Domain", "Range", "Confidence" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( RELATIONS ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( RELATIONS );
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMRelation object = (POMRelation)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getLabel();
			} else if( iCol == 1 ){
				return object.getDomain().getLabel();
			} else if( iCol == 2 ){
				return object.getRange().getLabel();
			} else {
				return new Double( object.getProbability() );
			}
		}
	}
	
	private class DataModelSimilarity extends AbstractDataModel {
		private String[] sColumns = { "Domain", "Range", "Confidence" };
		public int getColumnCount(){ 
			return sColumns.length;
		}
		public String getColumnName( int iColumn ){
			return sColumns[iColumn];
		}
		public int getRowCount(){ 
			return getObjects( SIMILARITY ).size(); 
		}
		public Object getValueAt( int iRow ){
			List objects = getObjects( SIMILARITY );
			return objects.get( iRow );
		}
		public Object getValueAt( int iRow, int iCol ){ 
			POMSimilarityRelation object = (POMSimilarityRelation)getValueAt( iRow );
			if( iCol == 0 ){
				return object.getDomain().getLabel();
			} else if( iCol == 1 ){
				return object.getRange().getLabel();
			} else {
				return new Double( object.getProbability() );
			}
		}
	}
	
	private class POMTable extends JTable { 
		private AbstractDataModel m_model;
		public POMTable( AbstractTableModel model ){
			super( model );
		}
		public void setDataModel( AbstractDataModel model ){
			m_model = model;
		}
		public AbstractDataModel getDataModel(){
			return m_model;
		}
		public boolean isCellEditable( int iRow, int iCol ){ 
			return false;
		}
	}
	 
	private List getObjects( int iType ){ 
		List objects = null;
		switch( iType ){
			case CONCEPTS: objects = m_pom.getObjects( POMConcept.class ); break;
			case INSTANCES: objects = m_pom.getObjects( POMInstance.class ); break;
			case SUBCLASS_OF: objects = m_pom.getObjects( POMSubclassOfRelation.class ); break;
			case INSTANCE_OF: objects = m_pom.getObjects( POMInstanceOfRelation.class ); break;
			case SUBTOPIC_OF: objects = m_pom.getObjects( POMSubtopicOfRelation.class ); break;
			case RELATIONS: objects = m_pom.getObjects( POMRelation.class ); break;
			case SIMILARITY: objects = m_pom.getObjects( POMSimilarityRelation.class ); break;
		}
		if( objects != null ){
			Collections.sort( objects, new ProbabilityComparator() );
		}
		return objects;
	}
	
	private List getColors( List<POMObject> objects ){
		List<Color> colors = new ArrayList<Color>();
		for( POMObject object: objects )
		{
			if( object.getUserEvidence() != null ){
				if( object.getUserEvidence().booleanValue() == false ){
					colors.add( Color.RED );
				}
				else {
					colors.add( Color.GREEN );
				}
			}
			else {
				colors.add( null );
			}
		}
		return colors;
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
				int i = m_tabs.getSelectedIndex();
				int iRow = m_tables[i].getSelectedRow(); 
				if( iRow != -1 ){
					AbstractDataModel model = ((POMTable)m_tables[i]).getDataModel();
					m_object = (POMObject)model.getValueAt( iRow );
					m_popup.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
		}
	}
	
	private class ColorTableCellRenderer extends DefaultTableCellRenderer {  
		private int m_iType = -1;
		public ColorTableCellRenderer( int iType ){
			m_iType = iType;
		}		
		public Component getTableCellRendererComponent( JTable table, Object value, boolean bSelected, boolean bFocus, int iRow, int iColumn ){
			Component c = super.getTableCellRendererComponent( table, value, bSelected, bFocus, iRow, iColumn );
			Color color = (Color)getColors( getObjects( m_iType ) ).get( iRow );
			if( color != null ){
				setBackground( color );
			}  
			else {
				if( bSelected ){
					setBackground( Color.yellow );
				}
				else {
					setBackground( Color.white );
				}
			}
			setForeground( Color.black );
			setOpaque( true ); 
			return this; 
		}
	}
}