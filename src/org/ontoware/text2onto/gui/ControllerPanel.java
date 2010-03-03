package org.ontoware.text2onto.gui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.net.URL;
import java.io.File;

import org.ontoware.text2onto.algorithm.AbstractAlgorithm;
import org.ontoware.text2onto.algorithm.AbstractCombiner;
import org.ontoware.text2onto.algorithm.ComplexAlgorithm;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ControllerPanel extends JPanel {

	private DynamicTree m_tree;

	private DefaultMutableTreeNode m_node;

	private JMenuItem m_itemAdd;

	private JMenuItem m_itemRemove;

	private JMenuItem m_itemCombiner;

	private JMenuItem m_itemConfig;

	private JPopupMenu m_popup;

	/* available algorithms */
	private HashMap m_hmNode2Algorithms;

	/* available combiners */
	private List m_combiners;

	/* available auxiliary algorithms */
	private List m_auxiliaries;

	private String[] m_sComplex = { "Concept", "Instance", "Similarity",	
		"SubclassOf", "InstanceOf", "Relation", "SubtopicOf" }; //, "Disjoint" };

	private String[] m_sPackage = { "concept", "instance", "similarity", "taxonomic.subclassOf", 
		"taxonomic.instanceOf",	"relation.general", "relation.subtopicOf" }; //, "axiom" };

	private HashMap<DefaultMutableTreeNode, Class> m_hmNode2Class;

	private ArrayList m_alListeners;

	private JFrame m_frame;

	private ControllerPanel m_controllerPanel;

	public ControllerPanel( JFrame frame ) {
		m_frame = frame;
		m_controllerPanel = this;
		m_alListeners = new ArrayList();
		m_hmNode2Algorithms = new HashMap();
		m_combiners = getClasses( "combiner" );
		m_auxiliaries = getClasses( "auxiliary" );
		m_hmNode2Class = new HashMap<DefaultMutableTreeNode, Class>();

		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 10, 10, 10, 10 ) );

		m_tree = new DynamicTree( "Algorithms", DynamicTree.ICONS_CONTROLLER, true );
		for( int i = 0; i < m_sComplex.length; i++ ) {
			Object node = m_tree.addObject( m_sComplex[i] );
			m_hmNode2Algorithms.put( node, getClasses( m_sPackage[i] ) );
		}
		add( BorderLayout.CENTER, m_tree );

		m_popup = new JPopupMenu();
		m_itemRemove = new JMenuItem( "Remove" );
		m_itemRemove.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doRemove();
			}
		} );
		m_itemAdd = new JMenuItem( "Add..." );
		m_itemAdd.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doAdd();
			}
		} );
		m_itemCombiner = new JMenuItem( "Combiner..." );
		m_itemCombiner.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doCombiner();
			}
		} );
		m_itemConfig = new JMenuItem( "Configure..." );
		m_itemConfig.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doConfig();
			}
		} );
		m_popup.add( m_itemAdd );
		// m_popup.add( m_itemRemove );
		m_popup.add( m_itemCombiner );
		m_popup.add( m_itemConfig );
		m_tree.addMouseListener( new PopupListener() );
	}

	public void addListener( ControllerListener listener ) {
		m_alListeners.add( listener );
	}

	public void reset() {
		m_tree.clear();
		for( int i = 0; i < m_sComplex.length; i++ ) {
			Object node = m_tree.addObject( m_sComplex[i] );
			m_hmNode2Algorithms.put( node, getClasses( m_sPackage[i] ) );
		}
	}

	public List getComplexNames() {
		ArrayList<String> names = new ArrayList<String>();
		for( int i = 0; i < m_sComplex.length; i++ ) {
			names.add( m_sComplex[i] );
		}
		return names;
	}

	private void doAdd() {
		List algorithms = (List)m_hmNode2Algorithms.get( m_node );
		List names = getClassNames( algorithms );
		ListDialog ld = new ListDialog( m_frame, algorithms, names );
		ld.show();
		List items = ld.getSelectedItems();
		for( Object item : items ) {
			Class algorithmClass = (Class)item;
			DefaultMutableTreeNode child = m_tree.addObject( m_node, algorithmClass.getSimpleName() );
			m_hmNode2Class.put( child, algorithmClass );
			notifyListeners( ControllerListener.ADD, m_node.toString(), algorithmClass, null );
		}
	}

	private void doCombiner() {
		List names = getClassNames( m_combiners );
		ListDialog ld = new ListDialog( m_frame, m_combiners, names );
		ld.show();
		List combiners = ld.getSelectedItems();
		if( combiners.size() > 0 ) {
			Class combinerClass = (Class)combiners.get( 0 );
			notifyListeners( ControllerListener.COMBINER, m_node.toString(), null, combinerClass );
		}
	}

	private void doRemove() {
		// TODO
	}

	private void doConfig() {
		List names = getClassNames( m_auxiliaries );
		ListDialog ld = new ListDialog( m_frame, m_auxiliaries, names );
		ld.show();
		List items = ld.getSelectedItems();
		for( Object item : items ) {
			Class auxiliaryClass = (Class)item;
			DefaultMutableTreeNode child = m_tree.addObject( m_node, auxiliaryClass.getSimpleName() );
			m_hmNode2Class.put( child, auxiliaryClass );
			Class algorithmClass = (Class)m_hmNode2Class.get( m_node );
			notifyListeners( ControllerListener.AUXILIARY, m_node.getParent().toString(), algorithmClass, auxiliaryClass );
		}
	}

	public void update( List<AbstractAlgorithm> algos ) {
		reset();
		DefaultMutableTreeNode rootNode = m_tree.getRoot();
		Enumeration enumer = rootNode.children();
		int iChildCount = 0;
		while( enumer.hasMoreElements() ) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)enumer.nextElement();
			ComplexAlgorithm complexAlgo = (ComplexAlgorithm)algos.get( iChildCount );

			// add algorithms

			List<AbstractAlgorithm> lAlgos = complexAlgo.getAlgorithms();
			for( AbstractAlgorithm algo : lAlgos ) {
				DefaultMutableTreeNode child = m_tree.addObject( childNode, algo.getClass().getSimpleName() );
			}

			// show the combiner only if some algorithms were selected
			if( lAlgos.size() > 0 ) {
				AbstractCombiner combiner = complexAlgo.getCombiner();
				// add combiner
				DefaultMutableTreeNode child = m_tree.addObject( childNode, combiner.getClass().getSimpleName() );
			}
			iChildCount++;
		}
	}

	private List getClassNames( List classes ) {
		ArrayList names = new ArrayList();
		Iterator iter = classes.iterator();
		while( iter.hasNext() ) {
			Class c = (Class)iter.next();
			names.add( c.getSimpleName() );
		}
		return names;
	}

	private class PopupListener extends MouseAdapter {
		public void mousePressed( MouseEvent e ) {
			maybeShowPopup( e );
		}

		public void mouseReleased( MouseEvent e ) {
			maybeShowPopup( e );
		}

		private void maybeShowPopup( MouseEvent e ) {
			if( e.isPopupTrigger() ) {
				m_node = m_tree.getSelectedNode();
				if( m_node.getLevel() == 1 ) {
					m_itemRemove.setEnabled( false );
					m_itemAdd.setEnabled( m_node != null );
					m_itemCombiner.setEnabled( m_node != null );
					m_itemConfig.setEnabled( false );
					m_popup.show( e.getComponent(), e.getX(), e.getY() );
				}
				else if( m_node.getLevel() == 2 ) {
					m_itemRemove.setEnabled( m_node != null );
					m_itemAdd.setEnabled( false );
					m_itemCombiner.setEnabled( false );
					m_itemConfig.setEnabled( m_node != null );
					m_popup.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
		}
	}

	private void notifyListeners( int iMessage, String sComplex, Class algorithmClass, Class configClass ) {
		Iterator iter = m_alListeners.iterator();
		while( iter.hasNext() ) {
			( (ControllerListener)iter.next() ).controllerChanged( iMessage, sComplex, algorithmClass, configClass );
		}
	}

	private List getClasses( String sPackageName ) {
		ArrayList alClasses = new ArrayList();
		String sPackage = new String( sPackageName );
		sPackage = sPackage.replace( '.', '/' );
		URL url = AbstractAlgorithm.class.getResource( sPackage );
		if( url == null ) {
			return alClasses;
		}
		File directory = new File( url.getFile() );
		if( directory.exists() ) {
			File[] files = directory.listFiles();
			for( int i = 0; i < files.length; i++ ) {
				String sFile = files[i].toString();
				String sFileName = files[i].getName().toString();
				if( files[i].isDirectory() ) {
					String sSubPackage = sPackageName + "." + sFileName;
					alClasses.addAll( getClasses( sSubPackage ) );
				}
				else if( sFile.endsWith( ".class" ) ) {
					String sClass = sFileName.substring( 0, sFileName.length() - 6 );
					try {
						Class c = Class.forName( "org.ontoware.text2onto.algorithm." + sPackageName + "." + sClass );
						if( !sClass.startsWith( "Abstract" ) && !sClass.contains( "$" )
						/* && subclassOf( c, AbstractAlgorithm.class ) */ ) {
							alClasses.add( c );
						}
					}
					catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
		}
		return alClasses;
	}

	private boolean subclassOf( Class c1, Class c2 ) {
		Class superClass = c1.getSuperclass();
		if( superClass == null ) {
			return false;
		}
		else if( superClass.equals( c2 ) ) {
			return true;
		}
		return subclassOf( superClass, c2 );
	}
}
