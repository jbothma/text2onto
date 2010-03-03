package org.ontoware.text2onto.gui;

/*
 * This code is based on an example provided by Richard Stanford,
 * a tutorial reader.
 */
import java.awt.*;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.net.URL;

import org.ontoware.text2onto.util.Settings;


public class DynamicTree extends JPanel
{
	protected DefaultMutableTreeNode m_rootNode;
	
	protected DefaultTreeModel m_treeModel;
	
	protected JTree m_tree;
	
	protected DefaultMutableTreeNode m_node;
	
	private Toolkit m_toolkit = Toolkit.getDefaultToolkit();
	
	public final static String[] ICONS_CORPUS = { "ClosedFolder.ico", "OpenFolder.ico", "Document.ico" };
	
	public final static String[] ICONS_CONTROLLER = { "RunDocument.ico", "RunDocument.ico", "Gear.ico" };
	
	private boolean m_bShow = false;
	

	public DynamicTree( String sRoot, String[] sIcons, boolean bShow ) {
		super( new GridLayout( 1, 0 ) );
		m_bShow = bShow;
		m_rootNode = new DefaultMutableTreeNode( sRoot );
		m_treeModel = new DefaultTreeModel( m_rootNode );
		m_treeModel.addTreeModelListener( new MyTreeModelListener() );

		m_tree = new JTree( m_treeModel );
		m_tree.setEditable( true );
		m_tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		m_tree.setShowsRootHandles( true );
		m_tree.addTreeSelectionListener( new TreeSelectionListener(){
			public void valueChanged( TreeSelectionEvent e ){
				selectionChanged( e ); 
			}
		});		
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		Icon openIcon = Text2Onto.createImageIcon( sIcons[1] );
		Icon closedIcon = Text2Onto.createImageIcon( sIcons[0] );
		Icon leafIcon = Text2Onto.createImageIcon( sIcons[2] );
		renderer.setOpenIcon( openIcon );
		renderer.setClosedIcon( closedIcon );
		renderer.setLeafIcon( leafIcon );
		m_tree.setCellRenderer( renderer );

		JScrollPane scrollPane = new JScrollPane( m_tree );
		add( scrollPane );
	}
	
	public void selectionChanged( TreeSelectionEvent e ){
		TreePath path = e.getPath();
		if( e.isAddedPath() ){
			m_node = (DefaultMutableTreeNode)( path.getLastPathComponent() );
		} else {
			m_node = null;
		} 
	}
	
	public void addMouseListener( MouseListener listener ){
		m_tree.addMouseListener( listener );
	}
	
	public void addSelectionListener( TreeSelectionListener listener ){
		m_tree.addTreeSelectionListener( listener );
	}
	
	public DefaultMutableTreeNode getRoot(){
		return m_rootNode;
	}
	
	public DefaultMutableTreeNode getSelectedNode(){
		return m_node;
	}
	
	public Object getSelectedObject(){
		DefaultMutableTreeNode node = getSelectedNode();
		if( node != null ){
			return node.getUserObject();
		}
		return null;
	}
 
	/** Remove all nodes except the root node. */
	public void clear() {
		m_rootNode.removeAllChildren();
		m_treeModel.reload();
	}

	/** Remove the currently selected node. */
	public void removeCurrentNode() {
		TreePath currentSelection = m_tree.getSelectionPath();
		if( currentSelection != null )
		{
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)( currentSelection.getLastPathComponent() );
			MutableTreeNode parent = (MutableTreeNode)( currentNode.getParent() );
			if( parent != null )
			{
				m_treeModel.removeNodeFromParent( currentNode );
				return;
			}
		}
		// Either there was no selection, or the root was selected.
		m_toolkit.beep();
	}

	/**
	 * Add child to the currently selected node.
	 *
	 * @param child  The feature to be added to the Object attribute
	 * @return       Description of the Return Value
	 */
	public DefaultMutableTreeNode addObject( Object child ) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = m_tree.getSelectionPath();
		if( parentPath == null ){
			parentNode = m_rootNode;
		}
		else {
			parentNode = (DefaultMutableTreeNode)( parentPath.getLastPathComponent() );
		}
		return addObject( parentNode, child, m_bShow );
	}

	public DefaultMutableTreeNode addObject( DefaultMutableTreeNode parent, Object child ) {
		return addObject( parent, child, m_bShow );
	}
	
	public DefaultMutableTreeNode addObject( Object parent, Object child ){
		return addObject( (DefaultMutableTreeNode)parent, child );
	}

	public DefaultMutableTreeNode addObject( DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible ) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode( child );
		childNode.setUserObject( child );
		if( parent == null ){
			parent = m_rootNode;
		}
		m_treeModel.insertNodeInto( childNode, parent, parent.getChildCount() );
		// Make sure the user can see the lovely new node.
		if( shouldBeVisible ){
			m_tree.scrollPathToVisible( new TreePath( childNode.getPath() ) );
		}
		return childNode;
	}
	 
	private class MyTreeModelListener implements TreeModelListener
	{
		public void treeNodesChanged( TreeModelEvent e ) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode)( e.getTreePath().getLastPathComponent() );
			/*
			 * If the event lists children, then the changed
			 * node is the child of the node we've already
			 * gotten.  Otherwise, the changed node and the
			 * specified node are the same.
			 */
			try	{
				int index = e.getChildIndices()[0];
				node = (DefaultMutableTreeNode)( node.getChildAt( index ) );
			}
			catch( NullPointerException exc )
			{}
			System.out.println( "The user has finished editing the node." );
			System.out.println( "New value: " + node.getUserObject() );
		}

		public void treeNodesInserted( TreeModelEvent e ) {
		}

		public void treeNodesRemoved( TreeModelEvent e ) {
		}

		public void treeStructureChanged( TreeModelEvent e ) {
		}
	}
}

