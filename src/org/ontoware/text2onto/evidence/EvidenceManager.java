package org.ontoware.text2onto.evidence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractAlgorithm;

/**
 * @author Günter Ladwig
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class EvidenceManager implements Serializable {

	private static int m_storeIds = 0;

	private class StoreInstance {
		private AbstractEvidenceStore m_owner;
		private EvidenceWrapper m_store;
		private int m_id;

		public StoreInstance( AbstractEvidenceStore owner, EvidenceWrapper store, int id ) {
			m_owner = owner;
			m_id = id;
			m_store = store;
		}

		public int getId() {
			return m_id;
		}

		public EvidenceWrapper getStoreWrapper() {
			return m_store;
		}

		public AbstractEvidenceStore getOwner() {
			return m_owner;
		}
	}
 
	private List<EvidenceWrapper> m_globalStores;

	private HashMap<AbstractAlgorithm,HashMap<Class,List<EvidenceWrapper>>> m_localStores;

	public EvidenceManager() {
		m_localStores = new HashMap<AbstractAlgorithm,HashMap<Class,List<EvidenceWrapper>>>();
		m_globalStores = new ArrayList<EvidenceWrapper>();
	}

	/** *********************************************************************************** */

	private AbstractEvidenceStore getNwrapperStore( Class cStoreClass ) {
		AbstractEvidenceStore newStore;
		try {
			newStore = (AbstractEvidenceStore) cStoreClass.newInstance();
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
		return newStore;
	}

	public EvidenceWrapper getGlobalStoreWrapper( Class cStoreClass ) {
		for ( EvidenceWrapper wrapper : m_globalStores ) {
			AbstractEvidenceStore store = wrapper.getChangeable();
			if ( store.getClass().equals( cStoreClass ) ) {
				return wrapper;
			}
		}
		AbstractEvidenceStore newStore = getNwrapperStore( cStoreClass );
		EvidenceWrapper wrapper = new EvidenceWrapper( newStore, null, 0 );
		if ( newStore != null ) {
			m_globalStores.add( wrapper );
		}
		return wrapper;
	}

	private HashMap<Class,List<EvidenceWrapper>> getLocalStores( AbstractAlgorithm owner ) {
		HashMap<Class,List<EvidenceWrapper>> algoStores = m_localStores.get( owner );

		if ( algoStores == null ) {
			algoStores = new HashMap<Class,List<EvidenceWrapper>>();
			m_localStores.put( owner, algoStores );
		}
		
		return algoStores;
	}
	
	private List<EvidenceWrapper> getLocalStoreWrappers( AbstractAlgorithm owner, Class cStoreClass ) {
		HashMap<Class,List<EvidenceWrapper>> algoStores = getLocalStores( owner );

		List<EvidenceWrapper> storeWrappers = algoStores.get( cStoreClass );

		if ( storeWrappers == null ) {
			storeWrappers = new ArrayList<EvidenceWrapper>();
			algoStores.put( cStoreClass, storeWrappers );
		}

		return storeWrappers;
	}

	public EvidenceWrapper getNwrapperLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass ) {
		List<EvidenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		System.out.println( "EvidenceManager.getLocalStore: creating new "+ cStoreClass.getSimpleName() +" for "+ owner.getClass().getSimpleName() );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractEvidenceStore newStore = getNwrapperStore( cStoreClass );
		EvidenceWrapper wrapper = new EvidenceWrapper( newStore, owner, ++m_storeIds );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( wrapper );
		}
		return wrapper;
	}
	
	public EvidenceWrapper getNwrapperLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass, String name ) {
		EvidenceWrapper ew = getNwrapperLocalStoreWrapper( owner, cStoreClass );
		ew.setName( name );
		return ew;
	}
	
	public EvidenceWrapper getLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass, int id ) {
		List<EvidenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		for ( EvidenceWrapper wrapper : storeWrappers ) {
			if ( wrapper.getId() == id ) {
				return wrapper;
			}
		}
		System.out.println( "EvidenceManager.getLocalStore: creating new "+ cStoreClass.getSimpleName() 
			+" ("+ id +") for "+ owner.getClass().getSimpleName() );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractEvidenceStore newStore = getNwrapperStore( cStoreClass );
		EvidenceWrapper wrapper = new EvidenceWrapper( newStore, owner, id );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( wrapper );
		}
		return wrapper;
	}

	public EvidenceWrapper getLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass, String name ) {
		List<EvidenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		for ( EvidenceWrapper ew : storeWrappers ) {
			if ( ew.getName() != null && ew.getName().equals(name) ) {
				return ew;
			}
		}
		System.out.println( "EvidenceManager.getLocalStore: no store for this object known, creating" );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractEvidenceStore newStore = getNwrapperStore( cStoreClass );
		EvidenceWrapper ew = new EvidenceWrapper( newStore, owner, ++m_storeIds, name );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( ew );
		}
		return ew;
	}
	
	/**
	 * If a store of type cStoreClass does not exist for the specified
	 * owner, it is created, added to the list and returned.
	 * If there is more than one store of a certain type for the
	 * specified owner, always the first one is returned. To get a specific
	 * store, use getLocalStoreWrapper(AbstractAlgorithm, Class, int).
	 * @see org.ontoware.text2onto.evidence.EvidenceManager#getLocalStoreWrapper(AbstractAlgorithm owner, Class cStoreClass, int id)
	 * @see org.ontoware.text2onto.evidence.EvidenceManager#getNwrapperLocalStoreWrapper(AbstractAlgorithm owner, Class cStoreClass)
	 * @param owner - The owner of the store
	 * @param cStoreClass - The type of the store
	 * @return
	 */
	public EvidenceWrapper getLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass ) {
		List<EvidenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		if ( storeWrappers.size() >= 1 )
			return storeWrappers.get( 0 );

		System.out.println( "EvidenceManager.getLocalStore: creating new "+ cStoreClass.getSimpleName() +" for "+ owner.getClass().getSimpleName() );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractEvidenceStore newStore = getNwrapperStore( cStoreClass );
		EvidenceWrapper wrapper = new EvidenceWrapper( newStore, owner, ++m_storeIds );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( wrapper );
		}
		return wrapper;
	}

	public List<EvidenceWrapper> getStoreWrappersByType( Class cStoreClass ) {
		List<EvidenceWrapper> wrappers = new ArrayList<EvidenceWrapper>();
		for ( HashMap<Class,List<EvidenceWrapper>> algoStores: m_localStores.values() )
		{
			for ( List<EvidenceWrapper> wrapperList: algoStores.values() ){
				wrappers.addAll( wrapperList );
			}
		}

		return wrappers;
	}

	public List<EvidenceWrapper> getStoreWrappersByOwner( Object owner ) {
		List<EvidenceWrapper> wrappers = new ArrayList<EvidenceWrapper>();
		for ( List<EvidenceWrapper> wrapperList: m_localStores.get( owner ).values() ){
			wrappers.addAll( wrapperList );
		}
		return wrappers;
	}

	public List<EvidenceWrapper> getAllStoreWrappers() {
		List<EvidenceWrapper> wrappers = new ArrayList<EvidenceWrapper>();
		wrappers.addAll( getLocalStores() );
		wrappers.addAll( getGlobalStores() );
		return wrappers;
	}

	public EvidenceWrapper getWrapperByStore( AbstractEvidenceStore store ) {
		for ( EvidenceWrapper wrapper : getLocalStores() ) {
			if ( wrapper.getChangeable() == store )
				return wrapper;
		}

		for ( EvidenceWrapper wrapper : m_globalStores ) {
			if ( wrapper.getChangeable() == store )
				return wrapper;
		}
		return null;
	}

	public List<EvidenceWrapper> getLocalStores() {
		List<EvidenceWrapper> wrappers = new ArrayList<EvidenceWrapper>();
		for ( HashMap<Class,List<EvidenceWrapper>> algoStores : m_localStores.values() ) {
			for ( List<EvidenceWrapper> storeWrappers : algoStores.values() ) {
				for ( EvidenceWrapper wrapper : storeWrappers ) {
					wrappers.add( wrapper );
				}
			}
		}
		return wrappers;
	}

	public List<EvidenceWrapper> getGlobalStores() {
		return m_globalStores;
	}
}
