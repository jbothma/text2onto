package org.ontoware.text2onto.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import java.util.*;
import java.net.URI;
import java.net.URL;
import java.io.File;

import com.ctreber.aclib.image.ico.*;

import org.ontoware.text2onto.persist.PersistenceManager;
import org.ontoware.text2onto.persist.Session;
import org.ontoware.text2onto.pom.POM;
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.pom.POMFactory;
import org.ontoware.text2onto.pom.POMWrapper;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.corpus.CorpusFactory;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.corpus.DocumentFactory;
import org.ontoware.text2onto.algorithm.*;
import org.ontoware.text2onto.algorithm.normalizer.*;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.ontology.*;
import org.ontoware.text2onto.util.Settings;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class Text2Onto implements CorpusListener, ControllerListener, POMListener {

	private final static String m_sVersion = "1.2b";
	

	private JButton m_buttonExport;

	private JButton m_buttonImport;
	
	private JButton m_buttonActive;
	
	
	private AlgorithmController m_controller;

	private ControllerPanel m_controllerPanel;
	

	private Corpus m_corpus;

	private CorpusPanel m_corpusPanel;
	

	private JFrame m_frame;
	

	private HashMap m_hmClass2Algorithm;

	private HashMap m_hmName2Complex;

	private HashMap m_hmURI2Document;
	

	private JMenuItem m_itemExport;

	private JMenuItem m_itemImport;

	private JMenuItem m_itemLoad;

	private JMenuItem m_itemSave;
	

	private PersistenceManager m_persistenceManager;
	

	private POMPanel m_pomPanel;

	private POMWrapper m_pomWrapper;
	

	private JLabel m_statusLabel;

	private StatusPanel m_statusPanel;
	

	private Text2Onto m_text2onto;
	
	

	public Text2Onto( JFrame frame ) {
		m_frame = frame;
		m_text2onto = this;
		this.reset();
	}

	private static void createAndShowGUI() {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch( Exception e ) {
			// e.printStackTrace();
		}
		JFrame.setDefaultLookAndFeelDecorated( true );
		JFrame frame = new JFrame( "Text2Onto" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		Text2Onto t2o = new Text2Onto( frame );
		frame.setJMenuBar( t2o.createMenuBar() );
		frame.setContentPane( t2o.createContentPane() );
		// frame.setSize( Toolkit.getDefaultToolkit().getScreenSize() );
		// frame.setSize( new Dimension( 800, 600 ) );
		frame.setExtendedState( JFrame.MAXIMIZED_BOTH );
		frame.setVisible( true );
	}

	public static ImageIcon createImageIcon( String sImage ) {
		try {
			URL url = new URL( "file:" + Settings.get( Settings.ICONS ) + sImage );
			if( sImage.endsWith( ".gif" ) ) {
				return new ImageIcon( url );
			}
			else {
				ICOFile icoFile = new ICOFile( url );
				java.util.List entries = icoFile.getEntries();
				Iterator iter = entries.iterator();
				while( iter.hasNext() ) {
					ICOEntry entry = (ICOEntry)iter.next();
					final Image image = entry.getImageRGB();
					if( image != null ) {
						return new ImageIcon( image );
					}
				}
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main( String[] args ) {
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		} );
	}

	/*
	 * sComplex: complex algorithm, e.g. ConceptExtraction sAlgorithmClass: simple algorithm, e.g.
	 * PatternConceptClassification sConfigClass: combiner or auxiliary algorithm
	 */
	public void controllerChanged( int iMessage, String sComplex, Class algorithmClass, Class configClass ) {
		if( m_hmName2Complex == null || m_hmName2Complex.size() == 0 ) 
		{
			m_hmName2Complex = new HashMap<String, ComplexAlgorithm>();
			java.util.List<String> names = m_controllerPanel.getComplexNames();
			for( String sName : names ) 
			{
				ComplexAlgorithm complex = new ComplexAlgorithm( sName );
				m_hmName2Complex.put( sName, complex );
				m_controller.addAlgorithm( complex );
			}
		}
		ComplexAlgorithm complex = (ComplexAlgorithm)m_hmName2Complex.get( sComplex );
		if( iMessage == ControllerListener.COMBINER ) 
		{
			// System.out.println( "Text2Onto.controllerChanged COMBINER: "+ sComplex +", "+ configClass );
			AbstractCombiner combiner = null;
			try {
				combiner = (AbstractCombiner)configClass.newInstance();
			}
			catch( Exception e ) {
				m_statusPanel.printError( "Cannot instantiate combiner: " + configClass );
				return;
			}
			complex.setCombiner( combiner );
		}
		else {
			AbstractAlgorithm algorithm = null;
			try {
				algorithm = (AbstractAlgorithm)m_hmClass2Algorithm.get( algorithmClass );
			}
			catch( Exception e ) {
				m_statusPanel.printError( "Cannot find algorithm: " + algorithmClass );
				return;
			}
			if( iMessage == ControllerListener.ADD ) 
			{
				// System.out.println( "Text2Onto.controllerChanged ADD: "+ sComplex +", "+ algorithmClass );
				if( algorithm == null ) {
					try {
						algorithm = (AbstractAlgorithm)algorithmClass.newInstance();
					}
					catch( Exception e ) {
						m_statusPanel.printError( "Cannot instantiate algorithm: " + algorithmClass );
						return;
					}
					m_hmClass2Algorithm.put( algorithmClass, algorithm );
				}
				m_controller.addAlgorithmTo( complex, algorithm );
			}
			else if( iMessage == ControllerListener.REMOVE ) {
				// TODO
				System.out.println( "Text2Onto.controllerChanged REMOVE: " + sComplex + ", " + algorithmClass );
			}
			else if( iMessage == ControllerListener.AUXILIARY ) 
			{
				// System.out.println( "Text2Onto.controllerChanged AUXILIARY: "+ sComplex +", "+ algorithmClass +", "+
				// configClass );
				AbstractAuxiliaryAlgorithm auxiliary = null;
				try {
					auxiliary = (AbstractAuxiliaryAlgorithm)configClass.newInstance();
				}
				catch( Exception e ) {
					m_statusPanel.printError( "Cannot instantiate auxiliary algorithm: " + configClass );
					return;
				}
				m_controller.addAlgorithmTo( algorithm, auxiliary );
			}
		}
		m_statusPanel.printDebug( "ComplexAlgorithm: " + complex );
	}

	public void corpusChanged( int iMessage, String sFile ) {
		URI uri = null;
		AbstractDocument doc = null;
		try {
			uri = ( new File( sFile ) ).toURI();
			doc = (AbstractDocument)m_hmURI2Document.get( uri );
			if( doc == null ) {
				doc = DocumentFactory.newDocument( uri );
				m_hmURI2Document.put( uri, doc );
			}
		}
		catch( Exception e ) {
			m_statusPanel.printError( "Cannot create document: " + sFile );
			return;
		}
		if( iMessage == CorpusListener.ADD ) {
			m_corpus.addDocument( doc );
		}
		else if( iMessage == CorpusListener.REMOVE ) {
			m_corpus.removeDocument( doc );
		}
	}
	
	public void pomChanged( int iMessage, java.util.List<? extends POMObject> objects ){ 
		ArrayList<ChangeRequest> changes = new ArrayList<ChangeRequest>();
		if( iMessage == POMListener.ADD ){
			for( POMObject object: objects ){
				changes.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, object, object.getProbability(), new ArrayList() ) ) );
			}
		}
		else if( iMessage == POMListener.REMOVE ){
			for( POMObject object: objects ){
				changes.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, object, object.getProbability(), new ArrayList() ) ) );
			}
		}
		m_pomWrapper.processChangeRequests( changes );
		update();
	}

	public Container createContentPane() {
		JPanel contentPane = new JPanel( new BorderLayout() );
		contentPane.setOpaque( true );
		contentPane.add( createToolBar(), BorderLayout.NORTH );
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Border border = BorderFactory.createCompoundBorder( new EmptyBorder( 10, 10, 10, 10 ), new BevelBorder(
				BevelBorder.LOWERED ) );

		// controller
		m_controllerPanel = new ControllerPanel( m_frame );
		m_controllerPanel.addListener( this );
		m_controllerPanel.setPreferredSize( new Dimension( 500, 400 ) );
		m_controllerPanel.setBorder( border );

		// corpus
		m_corpusPanel = new CorpusPanel( m_frame );
		m_corpusPanel.addListener( this );
		m_corpusPanel.setPreferredSize( new Dimension( 500, 400 ) );
		m_corpusPanel.setBorder( border );

		// controller + corpus
		JSplitPane spConfig = new JSplitPane( JSplitPane.VERTICAL_SPLIT, m_controllerPanel, m_corpusPanel );
		spConfig.setDividerSize( 20 );

		// pom
		m_pomPanel = new POMPanel( m_frame, this, m_pomWrapper.getChangeable() );
		m_pomPanel.setPreferredSize( new Dimension( 600, 600 ) );

		// messages
		m_statusPanel = new StatusPanel();
		m_statusPanel.setPreferredSize( new Dimension( 600, 200 ) );

		// pom + messages
		JSplitPane spResults = new JSplitPane( JSplitPane.VERTICAL_SPLIT, m_pomPanel, m_statusPanel );
		spResults.setDividerSize( 20 );

		JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, spConfig, spResults );
		splitPane.setBorder( null );
		splitPane.setOneTouchExpandable( true );
		splitPane.setDividerLocation( 300 );
		splitPane.setDividerSize( 20 );
		contentPane.add( splitPane, BorderLayout.CENTER );

		JPanel statusBar = new JPanel();
		m_statusLabel = new JLabel( " " );
		statusBar.add( m_statusLabel );
		contentPane.add( statusBar, BorderLayout.SOUTH );

		return contentPane;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu( "File" );
		JMenuItem itemNew = new JMenuItem( "New" );
		itemNew.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doNew();
			}
		} );
		JMenuItem itemRun = new JMenuItem( "Run" );
		itemRun.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doRun();
			}
		} );
		m_itemSave = new JMenuItem( "Save..." );
		m_itemSave.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doSave();
			}
		} );

		m_itemLoad = new JMenuItem( "Load..." );
		m_itemLoad.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doLoad();
			}
		} );
		m_itemImport = new JMenuItem( "Import..." );
		m_itemImport.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doImport();
			}
		} );
		m_itemExport = new JMenuItem( "Export..." );
		// m_itemExport.setEnabled( false );
		m_itemExport.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doExport();
			}
		} );
		JMenuItem itemExit = new JMenuItem( "Exit" );
		itemExit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doExit();
			}
		} );
		menuFile.add( itemNew );
		menuFile.add( itemRun );
		menuFile.addSeparator();
		menuFile.add( m_itemSave );
		menuFile.add( m_itemLoad );
		menuFile.addSeparator();
		menuFile.add( m_itemImport );
		menuFile.add( m_itemExport );
		menuFile.addSeparator();
		menuFile.add( itemExit );
		menuBar.add( menuFile );

		JMenu menuHelp = new JMenu( "Help" );
		JMenuItem itemAbout = new JMenuItem( "About..." );
		itemAbout.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				m_text2onto.doAbout();
			}
		} );
		menuHelp.add( itemAbout );
		menuBar.add( menuHelp );

		return menuBar;
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		JButton buttonNew = new JButton( createImageIcon( "star.ico" ) );
		buttonNew.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doNew();
			}
		} );
		JButton buttonRun = new JButton( createImageIcon( "play.ico" ) );
		buttonRun.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doRun();
			}
		} );
		m_buttonImport = new JButton( createImageIcon( "import.ico" ) );
		m_buttonImport.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doImport();
			}
		} );
		m_buttonExport = new JButton( createImageIcon( "export.ico" ) );
		// m_buttonExport.setEnabled( false );
		m_buttonExport.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doExport();
			}
		} );
		JButton buttonHelp = new JButton( createImageIcon( "help.ico" ) );
		buttonHelp.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doAbout();
			}
		} );
		JButton buttonExit = new JButton( createImageIcon( "exit.ico" ) );
		buttonExit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				doExit();
			}
		} );
		toolbar.add( buttonNew );
		toolbar.add( buttonRun );
		toolbar.add( m_buttonImport );
		toolbar.add( m_buttonExport );
		toolbar.add( buttonHelp );
		toolbar.add( buttonExit );
		return toolbar;
	}

	private void doAbout() {
		String sText = "Text2Onto " + m_sVersion
				+ "\n\n(c)2004 Johanna Voelker, Philipp Cimiano\n{jvo,pci}@aifb.uni-karlsruhe.de";
		JOptionPane.showMessageDialog( m_frame, sText, "About", JOptionPane.INFORMATION_MESSAGE );
	}

	private void doExit() {
		System.exit( 0 );
	}

	private void doExport() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chooser.setMultiSelectionEnabled( false );
		MyFileFilter filterKaon = new MyFileFilter( "KAON OI-Models", "kaon" );
		MyFileFilter filterRdfs = new MyFileFilter( "RDFS Ontologies", "rdfs" );
		MyFileFilter filterOwl = new MyFileFilter( "OWL Ontologies", "owl" );
		chooser.addChoosableFileFilter( filterKaon );
		chooser.addChoosableFileFilter( filterRdfs );
		chooser.addChoosableFileFilter( filterOwl );
		int iReturn = chooser.showOpenDialog( m_frame );
		if( iReturn == JFileChooser.APPROVE_OPTION ) 
		{
			File file = chooser.getSelectedFile();
			MyFileFilter filter = (MyFileFilter)chooser.getFileFilter();
			String sExtension = filter.getExtension();
			URI uri = file.toURI();
			String sURI = uri.toString();
			OntologyWriter writer = null;
			try {
				if( !sURI.endsWith( "."+ sExtension ) ){
					uri = new URI( sURI +"."+ sExtension );
				}
				if( sExtension.equals( "kaon" ) ) {
					writer = new KAONWriter( m_pomWrapper.getChangeable() );
				}
				else if( sExtension.equals( "rdfs" ) ) {
					writer = new RDFSWriter( m_pomWrapper.getChangeable() );
				}
				else if( sExtension.equals( "owl" ) ){
					writer = new OWLWriter( m_pomWrapper.getChangeable() );
				}
				if( writer != null ) {
					writer.write( uri );
				}
			}
			catch( Exception e ) {
				m_statusPanel.printError( e.toString() );
			}
		}
	}

	private void doImport() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chooser.setMultiSelectionEnabled( false );
		MyFileFilter filterKaon = new MyFileFilter( "KAON OI-Models", "kaon" );
		MyFileFilter filterRdfs = new MyFileFilter( "RDFS Ontologies", "rdfs" );
		MyFileFilter filterOwl = new MyFileFilter( "OWL Ontologies", "owl" );
		chooser.addChoosableFileFilter( filterKaon );
		chooser.addChoosableFileFilter( filterRdfs );
		chooser.addChoosableFileFilter( filterOwl );
		int iReturn = chooser.showOpenDialog( m_frame );
		if( iReturn == JFileChooser.APPROVE_OPTION ) 
		{
			File file = chooser.getSelectedFile();
			URI uri = file.toURI();
			System.out.println( uri );
			try {
				POMWrapper pomWrapper = new POMWrapper( POMFactory.newPOM( uri ) );
				POMFactory.merge( m_pomWrapper, pomWrapper );
				update();
			}
			catch( Exception e ) {
				e.printStackTrace();
				m_statusPanel.printError( "Cannot create POM from ontology: " + uri );
				return;
			}
		}
	}

	protected void doLoad() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType( JFileChooser.OPEN_DIALOG );
		chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chooser.setMultiSelectionEnabled( false );
		FileFilter filter = new FileFilter() {
			public boolean accept( File file ) {
				String sFile = file.toString();
				return ( file.isDirectory() || sFile.endsWith( ".ser" ) );
			}

			public String getDescription() {
				return "session (*.ser)";
			}
		};
		chooser.setFileFilter( filter );
		int iReturn = chooser.showOpenDialog( m_frame );
		if( iReturn == JFileChooser.APPROVE_OPTION ) 
		{
			File file = chooser.getSelectedFile();
			URI uri = file.toURI();
			System.out.println( uri );
			String sURI = uri.toString();
			if( sURI.endsWith( ".ser" ) ) {
				Session session = (Session)PersistenceManager.deserialize( file );
				m_controller = session.getAlgorithmController();
				try {
					m_controller.initPreprocessor();
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
				// catch( AnalyserException e ) {
				// e.printStackTrace();
				// }
				m_corpus = session.getCorpus();
				m_pomWrapper = new POMWrapper( session.getPOM() );
				m_pomPanel.reset( m_pomWrapper.getChangeable() );
				m_persistenceManager = new PersistenceManager( session );

				updateControllerPanel();
				updatePOMPanel();
				updateCorpusPanel();
			}
		}
	}

	private void doNew() {
		this.reset();
		m_pomPanel.reset( m_pomWrapper.getChangeable() );
		m_corpusPanel.reset();
		m_controllerPanel.reset();
	}

	private void doRun() {
		try {
			m_statusPanel.printDebug( "Corpus: " + m_corpus );
			// m_statusPanel.printDebug( "Run: "+ m_hmName2Complex.values() );
			m_statusPanel.printDebug( "Controller: " + m_controller ); 
			m_controller.execute();
			update();
		}
		catch( Exception e ) {
			m_statusPanel.printError( e.toString() );
		}
	}

	protected void doSave() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType( JFileChooser.SAVE_DIALOG );
		chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chooser.setMultiSelectionEnabled( false );
		FileFilter filter = new FileFilter() {
			public boolean accept( File file ) {
				String sFile = file.toString();
				return ( file.isDirectory() || sFile.endsWith( ".ser" ) );
			}

			public String getDescription() {
				return "session (*.ser)";
			}
		};
		chooser.setFileFilter( filter );
		int iReturn = chooser.showOpenDialog( m_frame );
		if( iReturn == JFileChooser.APPROVE_OPTION ) {
			File file = chooser.getSelectedFile();
			URI uri = file.toURI();
			String sURI = uri.toString();
			try {
				if( sURI.endsWith( ".ser" ) ) {
					file.createNewFile();
					m_persistenceManager.serialize( file );
				}
			}
			catch( Exception e ) {
				m_statusPanel.printError( e.toString() );
			}
		}
	}

	public HashMap<POMChange, java.util.List<AbstractExplanation>> getExplanations( java.util.List<POMChange> changes ) {
		HashMap<POMChange, java.util.List<AbstractExplanation>> hmChange2Explanations = new HashMap<POMChange, java.util.List<AbstractExplanation>>();
		for( POMChange change : changes ) {
			hmChange2Explanations.put( change, getExplanations( change ) );
		}
		return hmChange2Explanations;
	}

	private java.util.List<AbstractExplanation> getExplanations( POMChange change ) {
		java.util.List<AbstractExplanation> explanations = new ArrayList();
		java.util.List<AbstractAlgorithm> algorithms = m_controller.getAlgorithms();
		for( AbstractAlgorithm algorithm : algorithms ) {
			if( algorithm instanceof AbstractSimpleAlgorithm ) {
				try {
					AbstractExplanation explanation = m_controller.getExplanation( (AbstractSimpleAlgorithm)algorithm,
							change );
					if( explanation != null ) {
						explanations.add( explanation );
					}
				}
				catch( Exception e ) {
					m_statusPanel.printError( e.toString() );
				}
			}
		}
		return explanations;
	}

	private void reset() {
		m_corpus = CorpusFactory.newCorpus();
		m_pomWrapper = new POMWrapper( POMFactory.newPOM() );
		try {
			m_controller = new AlgorithmController( m_corpus, m_pomWrapper.getChangeable() );
		}
		catch( Exception e ) {
			m_statusPanel.printError( e.toString() );
		}
		// m_controller.setNormalizer( new Zero2OneNormalizer() );
		Session session = new Session( this.m_controller, this.m_pomWrapper.getChangeable(), m_corpus );
		m_persistenceManager = new PersistenceManager( session );
		m_hmURI2Document = new HashMap();
		m_hmClass2Algorithm = new HashMap();
		m_hmName2Complex = new HashMap();
		boolean bPOM = false;
		if( m_itemImport != null ) {
			// m_itemImport.setEnabled( !bPOM );
			// m_buttonImport.setEnabled( !bPOM );
			// m_itemExport.setEnabled( bPOM );
			// m_buttonExport.setEnabled( bPOM );
		}
		m_frame.validate();
	}

	private void update() {
		m_pomPanel.update();
		m_statusPanel.printDebug( "POM:\n" + m_pomWrapper.getChangeable() );
		// boolean bPOM = ( m_pomWrapper.getChangeable() != null && !m_pomWrapper.getChangeable().isEmpty() );
		// m_itemImport.setEnabled( !bPOM );
		// m_buttonImport.setEnabled( !bPOM );
		// m_itemExport.setEnabled( bPOM );
		// m_buttonExport.setEnabled( bPOM );
		m_frame.validate();
	}

	private void updateControllerPanel() {
		m_controllerPanel.update( m_controller.getComplexAlgorithms() );
	}

	private void updateCorpusPanel() {
		m_corpusPanel.reset();
		m_corpusPanel.update( m_corpus );

	}

	private void updatePOMPanel() {
		m_pomPanel.update();
		m_statusPanel.printDebug( "POM:\n" + m_pomWrapper.getChangeable() );
		m_frame.validate();
	}
	
	private class MyFileFilter extends FileFilter {
		private String m_sExtension;
		private String m_sDescription;
		
		public MyFileFilter( String sDescription, String sExtension ){
			m_sExtension = sExtension;
			m_sDescription = sDescription;
		}
		public boolean accept( File file ) {
			String sFile = file.toString();
			return ( file.isDirectory() || sFile.endsWith( "."+ m_sExtension ) );
		}
		public String getDescription() {
			return m_sDescription +" (*."+ m_sExtension +")";
		}
		public String getExtension(){
			return m_sExtension;
		}		
	}
}
