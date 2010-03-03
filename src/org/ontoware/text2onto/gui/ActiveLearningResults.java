
import javax.swing.JFrame;
import javax.swing.JButton; 
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JDialog; 
import javax.swing.JLabel;
import javax.swing.JScrollPane; 
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
import java.util.Arrays;

import org.ontoware.text2onto.pom.POMInstanceOfRelation;
import org.ontoware.text2onto.util.ProbabilityComparator;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ActiveLearningResults extends JDialog {

	private Border m_border;
	
	private JList m_list;
	 
	private List<POMInstanceOfRelation> m_results;
	
	private JButton m_buttonOk;
	
	private JButton m_buttonCancel;
	
	 
	public ActiveLearningResults( JFrame owner, List<POMInstanceOfRelation> relations ){
		super( owner, "Feedback", true );			
		setSize( 500, 500 );
		Dimension dim = getToolkit().getScreenSize();
		setLocation( (int)( ( dim.getWidth() - this.getWidth() ) / 2 ), 
						(int)( ( dim.getHeight() - this.getHeight() ) / 2 ) );
				
		Container cp = getContentPane();
		cp.setLayout( new BorderLayout() ); 
		
		m_border = BorderFactory.createCompoundBorder( new EmptyBorder( 10, 10, 10, 10 ), 
			BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
		
		//  results panel
		
		Collections.sort( relations, new ProbabilityComparator() );
		m_list = new JList();
		m_list.setListData( relations.toArray() );
		JScrollPane scrollList = new JScrollPane( m_list );
		scrollList.setBorder( m_border );
		cp.add( BorderLayout.CENTER, scrollList );
				
		// button panel
		
		m_buttonOk = new JButton( "Ok" );
		m_buttonOk.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){  
				m_results = getResults( m_list.getSelectedValues() );
				dispose(); 
			}
		}); 
		m_buttonCancel = new JButton( "Cancel" );
		m_buttonCancel.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				m_results = null;
				dispose();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		buttonPanel.add( m_buttonCancel ); 
		buttonPanel.add( m_buttonOk );
		
		cp.add( BorderLayout.SOUTH, buttonPanel );
	}
	
	public List<POMInstanceOfRelation> getResults(){
		return m_results; 
	}
	
	private List<POMInstanceOfRelation> getResults( Object[] objects ){
		List<POMInstanceOfRelation> results = new ArrayList<POMInstanceOfRelation>();
		if( objects == null ){
			return results;
		}
		for( int i=0; i<objects.length; i++ ){
			results.add( (POMInstanceOfRelation)objects[i] );
		}
		return results;
	} 
}
