package org.ontoware.text2onto.debug;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.*;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.util.ProbabilityComparator;


public class POMTable extends JFrame {
 
	public POMTable( POM pom ){
		this( "POM", pom );
	} 
 
	public POMTable( String sTitle, POM pom ){
		super( sTitle );   
		setBounds( 100, 100, 800, 800 );
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
		setLayout( new GridLayout( 2, 2 ) ); 
		
		// concepts
		
		List concepts = pom.getObjects( POMConcept.class ); 		
		JScrollPane spConcepts = createTableConcepts( concepts );
		JPanel jpConcepts = new JPanel();
		jpConcepts.setLayout( new BorderLayout() );
		jpConcepts.add( "North", new JLabel( "Concepts" ) );
		jpConcepts.add( "Center", spConcepts );
		
		// instances
		
		List instances = pom.getObjects( POMInstance.class ); 
		JScrollPane spInstances = createTableInstances( instances );
		JPanel jpInstances = new JPanel();
		jpInstances.setLayout( new BorderLayout() ); 
		jpInstances.add( "North", new JLabel( "Instances" ) );
		jpInstances.add( "Center", spInstances );
		
		// subclassOf
		
		List subclassOf = pom.getObjects( POMSubclassOfRelation.class );  
		JScrollPane spSubclassOf = createTableSubclassOf( subclassOf );
		JPanel jpSubclassOf = new JPanel();
		jpSubclassOf.setLayout( new BorderLayout() );
		jpSubclassOf.add( "North", new JLabel( "SubclassOf" ) );
		jpSubclassOf.add( "Center", spSubclassOf );
		 
		// instanceOf
		 
		List instanceOf = pom.getObjects( POMInstanceOfRelation.class ); 
		JScrollPane spInstanceOf = createTableInstanceOf( instanceOf );
		JPanel jpInstanceOf = new JPanel();
		jpInstanceOf.setLayout( new BorderLayout() );
		jpInstanceOf.add( "North", new JLabel( "InstanceOf" ) );
		jpInstanceOf.add( "Center", spInstanceOf );
		
		// relation
		
		List relation = pom.getObjects( POMRelation.class ); 
		JScrollPane spRelation = createTableRelation( relation );
		JPanel jpRelation = new JPanel();
		jpRelation.setLayout( new BorderLayout() );
		jpRelation.add( "North", new JLabel( "Relations" ) );
		jpRelation.add( "Center", spRelation );
		
		// similarity
		
		List similarity = pom.getObjects( POMSimilarityRelation.class ); 
		JScrollPane spSimilarity = createTableSimilarity( similarity );
		JPanel jpSimilarity = new JPanel();
		jpSimilarity.setLayout( new BorderLayout() );
		jpSimilarity.add( "North", new JLabel( "Similarities" ) );
		jpSimilarity.add( "Center", spSimilarity );
		
		// disjointClasses
		
		List disjoint = pom.getObjects( POMDisjointClasses.class ); 
		JScrollPane spDisjoint = createTableDisjoint( disjoint );
		JPanel jpDisjoint = new JPanel();
		jpDisjoint.setLayout( new BorderLayout() );
		jpDisjoint.add( "North", new JLabel( "DisjointClasses" ) );
		jpDisjoint.add( "Center", spDisjoint );
		
		// subtopicOf
		
		List subtopicOf = pom.getObjects( POMSubtopicOfRelation.class );  
		JScrollPane spSubtopicOf = createTableSubtopicOf( subtopicOf );
		JPanel jpSubtopicOf = new JPanel();
		jpSubtopicOf.setLayout( new BorderLayout() );
		jpSubtopicOf.add( "North", new JLabel( "SubtopicOf" ) );
		jpSubtopicOf.add( "Center", spSubtopicOf );
		
		if( concepts.size() > 0 ) add( jpConcepts );
		if( instances.size() > 0 ) add( jpInstances ); 
		if( subclassOf.size() > 0 ) add( jpSubclassOf );
		if( instanceOf.size() > 0 ) add( jpInstanceOf );
		if( relation.size() > 0 ) add( jpRelation );
		if( subtopicOf.size() > 0 ) add( jpSubtopicOf );
		if( similarity.size() > 0 ) add( jpSimilarity );
		if( disjoint.size() > 0 ) add( jpDisjoint );
		
		setVisible( true );
	}
	
	private JScrollPane createTableConcepts( List concepts ){
		Collections.sort( concepts, new ProbabilityComparator() );
		String sColConcepts[] = { "Label", "Relevance" };
		Object[][] data = new Object[concepts.size()][sColConcepts.length];
		boolean[] bEvidence = new boolean[concepts.size()];
		for( int i=0; i<concepts.size(); i++ )
		{
			POMConcept concept = (POMConcept)concepts.get(i);
			data[i][0] = concept.getLabel();  
			data[i][1] = String.valueOf( concept.getProbability() );
			bEvidence[i] = ( concept.getUserEvidence() != null );			
		}
		JTable jtConcepts = new JTable( data, sColConcepts ); 
		jtConcepts.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtConcepts );
	}
	
	private JScrollPane createTableInstances( List instances ){
		Collections.sort( instances, new ProbabilityComparator() );
		String sColInstances[] = { "Label", "Relevance" };
		Object[][] data = new Object[instances.size()][sColInstances.length];
		boolean[] bEvidence = new boolean[instances.size()];
		for( int i=0; i<instances.size(); i++ )
		{
			POMInstance instance = (POMInstance)instances.get(i);
			data[i][0] = instance.getLabel(); 
			data[i][1] = String.valueOf( instance.getProbability() );
			bEvidence[i] = ( instance.getUserEvidence() != null );
		}
		JTable jtInstances = new JTable( data, sColInstances );
		jtInstances.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtInstances );
	}
	
	private JScrollPane createTableSubclassOf( List subclassOf ){
		Collections.sort( subclassOf, new ProbabilityComparator() );
		String[] sColSubclassOf = { "Domain", "Range", "Confidence" };
		Object[][] data = new Object[subclassOf.size()][sColSubclassOf.length];
		boolean[] bEvidence = new boolean[subclassOf.size()];
		for( int i=0; i<subclassOf.size(); i++ )
		{
			POMTaxonomicRelation rel = (POMTaxonomicRelation)subclassOf.get(i); 
			data[i][0] = rel.getDomain().getLabel();
			data[i][1] = rel.getRange().getLabel();
			data[i][2] = String.valueOf( rel.getProbability() );
			bEvidence[i] = ( rel.getUserEvidence() != null );
		}
		JTable jtSubclassOf = new JTable( data, sColSubclassOf );
		jtSubclassOf.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtSubclassOf );
	}
	
	private JScrollPane createTableInstanceOf( List instanceOf ){
		Collections.sort( instanceOf, new ProbabilityComparator() );
		String[] sColInstanceOf = { "Domain", "Range", "Confidence" };
		Object[][] data = new Object[instanceOf.size()][sColInstanceOf.length];
		boolean[] bEvidence = new boolean[instanceOf.size()];
		for( int i=0; i<instanceOf.size(); i++ )
		{
			POMTaxonomicRelation rel = (POMTaxonomicRelation)instanceOf.get(i); 
			data[i][0] = rel.getDomain().getLabel();
			data[i][1] = rel.getRange().getLabel();
			data[i][2] = String.valueOf( rel.getProbability() );
			bEvidence[i] = ( rel.getUserEvidence() != null );
		}
		JTable jtInstanceOf = new JTable( data, sColInstanceOf );
		jtInstanceOf.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtInstanceOf );
	}
	
	private JScrollPane createTableRelation( List relation ){
		Collections.sort( relation, new ProbabilityComparator() );
		String[] sColRelation = { "Relation", "Domain", "Range", "Confidence" };
		Object[][] data = new Object[relation.size()][sColRelation.length];
		boolean[] bEvidence = new boolean[relation.size()];
		for( int i=0; i<relation.size(); i++ )
		{
			POMRelation rel = (POMRelation)relation.get(i); 
			data[i][0] = rel.getLabel();
			data[i][1] = rel.getDomain().getLabel();
			data[i][2] = rel.getRange().getLabel();
			data[i][3] = String.valueOf( rel.getProbability() );
			bEvidence[i] = ( rel.getUserEvidence() != null );
		}
		JTable jtRelation = new JTable( data, sColRelation );
		jtRelation.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtRelation );
	}
	
	private JScrollPane createTableSimilarity( List similarity ){
		Collections.sort( similarity, new ProbabilityComparator() );
		String[] sColSimilarity = { "Domain", "Range", "Similarity" };
		Object[][] data = new Object[similarity.size()][sColSimilarity.length];
		boolean[] bEvidence = new boolean[similarity.size()];
		for( int i=0; i<similarity.size(); i++ )
		{
			POMSimilarityRelation rel = (POMSimilarityRelation)similarity.get(i);
			data[i][0] = rel.getDomain().getLabel();
			data[i][1] = rel.getRange().getLabel();
			data[i][2] = String.valueOf( rel.getProbability() );
			bEvidence[i] = ( rel.getUserEvidence() != null );
		}
		JTable jtSimilarity = new JTable( data, sColSimilarity );
		jtSimilarity.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtSimilarity );
	}
	
	private JScrollPane createTableDisjoint( List disjoint ){
		Collections.sort( disjoint, new ProbabilityComparator() );
		String[] sColDisjoint = { "Classes", "Confidence" };
		Object[][] data = new Object[disjoint.size()][sColDisjoint.length];
		boolean[] bEvidence = new boolean[disjoint.size()];
		for( int i=0; i<disjoint.size(); i++ )
		{
			POMDisjointClasses dc = (POMDisjointClasses)disjoint.get(i);
			data[i][0] = new ArrayList( dc.getConcepts() ).toString(); 
			data[i][1] = String.valueOf( dc.getProbability() );
			bEvidence[i] = ( dc.getUserEvidence() != null );
		}
		JTable jtDisjoint = new JTable( data, sColDisjoint );
		jtDisjoint.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtDisjoint );
	}
	
	private JScrollPane createTableSubtopicOf( List subtopicOf ){
		Collections.sort( subtopicOf, new ProbabilityComparator() );
		String[] sColSubtopicOf = { "Domain", "Range", "Confidence" };
		Object[][] data = new Object[subtopicOf.size()][sColSubtopicOf.length];
		boolean[] bEvidence = new boolean[subtopicOf.size()];
		for( int i=0; i<subtopicOf.size(); i++ )
		{
			POMSubtopicOfRelation rel = (POMSubtopicOfRelation)subtopicOf.get(i); 
			data[i][0] = rel.getDomain().getLabel();
			data[i][1] = rel.getRange().getLabel();
			data[i][2] = String.valueOf( rel.getProbability() );
			bEvidence[i] = ( rel.getUserEvidence() != null );
		}
		JTable jtSubtopicOf = new JTable( data, sColSubtopicOf );
		jtSubtopicOf.setDefaultRenderer( Object.class, new ColorTableCellRenderer( bEvidence ) );
		return new JScrollPane( jtSubtopicOf );
	}
	
	private class ColorTableCellRenderer extends DefaultTableCellRenderer {
		private boolean[] m_bColor;
		
		public ColorTableCellRenderer( boolean[] bColor ){
			m_bColor = bColor;
		}
		public Component getTableCellRendererComponent( JTable table, Object value, boolean bSelected, boolean bFocus, int iRow, int iColumn ){
			Component c = super.getTableCellRendererComponent( table, value, bSelected, bFocus, iRow, iColumn );
			if( m_bColor[iRow] ){
				setBackground( Color.YELLOW );
			} else {
				setBackground( Color.WHITE );
			}
			return this; 
		}
	}
}