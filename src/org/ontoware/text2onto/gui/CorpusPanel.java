package org.ontoware.text2onto.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.filechooser.FileFilter; 

import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.corpus.Corpus;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class CorpusPanel extends JPanel {

	private DynamicTree m_tree;

	private Object m_node;

	private JMenuItem m_itemAdd;

	private JMenuItem m_itemRemove;

	private JMenuItem m_itemShow;

	private JPopupMenu m_popup;

	private ArrayList m_alListeners;

	private JFrame m_frame;

	private CorpusPanel m_corpusPanel;

	public CorpusPanel( JFrame frame ) {
		m_frame = frame;
		m_corpusPanel = this;
		m_alListeners = new ArrayList();
		setLayout( new BorderLayout() );
		setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
		m_tree = new DynamicTree( "Corpus", DynamicTree.ICONS_CORPUS, true );
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
		m_itemShow = new JMenuItem( "Show..." );
		m_itemShow.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doShow();
			}
		} );
		m_popup.add( m_itemAdd );
		m_popup.add( m_itemRemove );
		m_popup.add( m_itemShow );
		m_tree.addMouseListener( new PopupListener() );
	}

	public void addListener( CorpusListener listener ) {
		m_alListeners.add( listener );
	}

	public void reset() {
		m_tree.clear();
	}

	public void update( Corpus m_corpus ) {
		java.util.List docs = m_corpus.getDocuments();
		for( int i = 0; i < docs.size(); i++ ) {
			AbstractDocument doc = (AbstractDocument)docs.get( i );
			m_tree.addObject( m_tree.getRoot(), doc.getURI().toString() );
		}
	}

	private void doAdd() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chooser.setMultiSelectionEnabled( true );
		FileFilter filter = new FileFilter() {
			public boolean accept( File file ) {
				return ( file.isDirectory() || file.toString().endsWith( ".txt" ) || file.toString().endsWith( ".pdf" )
						|| file.toString().endsWith( ".htm" ) || file.toString().endsWith( ".html" ) );
			}

			public String getDescription() {
				return "Input Files (*.txt, *.pdf, *.htm, *.html)";
			}
		};
		chooser.setFileFilter( filter );
		int iReturn = chooser.showOpenDialog( m_frame );
		if( iReturn == JFileChooser.APPROVE_OPTION ) {
			File[] files = chooser.getSelectedFiles();
			for( int i = 0; i < files.length; i++ ) {
				String sFile = files[i].toString();
				m_tree.addObject( m_tree.getRoot(), sFile );
				notifyListeners( CorpusListener.ADD, sFile );
			}
		}
	}

	private void doRemove() {
		if( m_node == null || m_node.equals( m_tree.getRoot() ) ) {
			return;
		}
		String sFile = (String)m_tree.getSelectedObject();
		m_tree.removeCurrentNode();
		notifyListeners( CorpusListener.REMOVE, sFile );
	}

	private void doShow() {
		if( m_node == null || m_node.equals( m_tree.getRoot() ) ) {
			return;
		}
		String sFile = (String)m_tree.getSelectedObject();
		DocumentDialog dd = new DocumentDialog( m_frame, sFile );
		dd.show();
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
				if( m_node != null ) {
					boolean bRoot = m_node.equals( m_tree.getRoot() );
					m_itemAdd.setEnabled( bRoot );
					m_itemRemove.setEnabled( !bRoot );
					m_itemShow.setEnabled( !bRoot );
					m_popup.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
		}
	}

	private void notifyListeners( int iMessage, String sFile ) {
		Iterator iter = m_alListeners.iterator();
		while( iter.hasNext() ) {
			( (CorpusListener)iter.next() ).corpusChanged( iMessage, sFile );
		}
	}
}