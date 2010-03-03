package org.ontoware.text2onto.reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractAlgorithm;

/**
 * @author Günter Ladwig
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ReferenceManager implements Serializable {

	private static int m_storeIds = 0;

	private class StoreInstance {
	
		private AbstractReferenceStore m_owner;
		private ReferenceWrapper m_store;
		private int m_id;

		public StoreInstance( AbstractReferenceStore owner, ReferenceWrapper store, int id ) {
			m_owner = owner;
			m_id = id;
			m_store = store;
		}

		public int getId() {
			return m_id;
		}

		public ReferenceWrapper getStoreWrapper() {
			return m_store;
		}

		public AbstractReferenceStore getOwner() {
			return m_owner;
		}
	}
 
	private List<ReferenceWrapper> m_globalStores;

	private HashMap<AbstractAlgorithm,HashMap<Class,List<ReferenceWrapper>>> m_localStores;

	public ReferenceManager() {
		m_localStores = new HashMap<AbstractAlgorithm,HashMap<Class,List<ReferenceWrapper>>>();
		m_globalStores = new ArrayList<ReferenceWrapper>();
	}

	/** *********************************************************************************** */

	private AbstractReferenceStore getNwrapperStore( Class cStoreClass ) {
		AbstractReferenceStore newStore;
		try {
			newStore = (AbstractReferenceStore) cStoreClass.newInstance();
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
		return newStore;
	}

	public ReferenceWrapper getGlobalStoreWrapper( Class cStoreClass ) {
		for ( ReferenceWrapper wrapper : m_globalStores ) {
			AbstractReferenceStore store = wrapper.getChangeable();
			if ( store.getClass().equals( cStoreClass ) ) {
				return wrapper;
			}
		}
		AbstractReferenceStore newStore = getNwrapperStore( cStoreClass );
		ReferenceWrapper wrapper = new ReferenceWrapper( newStore, null, 0 );
		if ( newStore != null ) {
			m_globalStores.add( wrapper );
		}
		return wrapper;
	}

	private HashMap<Class,List<ReferenceWrapper>> getLocalStores( AbstractAlgorithm owner ) {
		HashMap<Class,List<ReferenceWrapper>> algoStores = m_localStores.get( owner );

		if ( algoStores == null ) {
			algoStores = new HashMap<Class,List<ReferenceWrapper>>();
			m_localStores.put( owner, algoStores );
		}
		
		return algoStores;
	}
	
	private List<ReferenceWrapper> getLocalStoreWrappers( AbstractAlgorithm owner, Class cStoreClass ) {
		HashMap<Class,List<ReferenceWrapper>> algoStores = getLocalStores( owner );

		List<ReferenceWrapper> storeWrappers = algoStores.get( cStoreClass );

		if ( storeWrappers == null ) {
			storeWrappers = new ArrayList<ReferenceWrapper>();
			algoStores.put( cStoreClass, storeWrappers );
		}

		return storeWrappers;
	}

	public ReferenceWrapper getNwrapperLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass ) {
		List<ReferenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		System.out.println( "ReferenceManager.getLocalStore: creating new "+ cStoreClass.getSimpleName() +" for "+ owner.getClass().getSimpleName() );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractReferenceStore newStore = getNwrapperStore( cStoreClass );
		ReferenceWrapper wrapper = new ReferenceWrapper( newStore, owner, ++m_storeIds );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( wrapper );
		}
		return wrapper;
	}
	
	public ReferenceWrapper getLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass, int id ) {
		List<ReferenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		for ( ReferenceWrapper wrapper : storeWrappers ) {
			if ( wrapper.getId() == id ) {
				return wrapper;
			}
		}
		System.out.println( "ReferenceManager.getLocalStore: creating new "+ cStoreClass.getSimpleName() 
			+" ("+ id +") for "+ owner.getClass().getSimpleName() );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractReferenceStore newStore = getNwrapperStore( cStoreClass );
		ReferenceWrapper wrapper = new ReferenceWrapper( newStore, owner, id );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( wrapper );
		}
		return wrapper;
	}

	/**
	 * If a store of type cStoreClass does not exist for the specified
	 * owner, it is created, added to the list and returned.
	 * If there is more than one store of a certain type for the
	 * specified owner, always the first one is returned. To get a specific
	 * store, use getLocalStoreWrapper(AbstractAlgorithm, Class, int).
	 * @see org.ontoware.text2onto.reference.ReferenceManager#getLocalStoreWrapper(AbstractAlgorithm owner, Class cStoreClass, int id)
	 * @see org.ontoware.text2onto.reference.ReferenceManager#getNwrapperLocalStoreWrapper(AbstractAlgorithm owner, Class cStoreClass)
	 * @param owner - The owner of the store
	 * @param cStoreClass - The type of the store
	 * @return
	 */
	public ReferenceWrapper getLocalStoreWrapper( AbstractAlgorithm owner, Class cStoreClass ) {
		List<ReferenceWrapper> storeWrappers = getLocalStoreWrappers( owner, cStoreClass );

		if ( storeWrappers.size() >= 1 )
			return storeWrappers.get( 0 );

		System.out.println( "ReferenceManager.getLocalStore: creating new "+ cStoreClass.getSimpleName() +" for "+ owner.getClass().getSimpleName() );
		// The calling object has no store of this particular type
		// associated with it. So we have to create a new one using
		// Java Reflection.
		AbstractReferenceStore newStore = getNwrapperStore( cStoreClass );
		ReferenceWrapper wrapper = new ReferenceWrapper( newStore, owner, ++m_storeIds );
		// And add it to the list of stores.
		if ( newStore != null ) {
			storeWrappers.add( wrapper );
		}
		return wrapper;
	}

	public List<ReferenceWrapper> getStoreWrappersByType( Class cStoreClass ) {
		List<ReferenceWrapper> wrappers = new ArrayList<ReferenceWrapper>();
		for ( HashMap<Class,List<ReferenceWrapper>> algoStores: m_localStores.values() )
		{
			for ( List<ReferenceWrapper> wrapperList: algoStores.values() ){
				wrappers.addAll( wrapperList );
			}
		}

		return wrappers;
	}

	public List<ReferenceWrapper> getStoreWrappersByOwner( Object owner ) {
		List<ReferenceWrapper> wrappers = new ArrayList<ReferenceWrapper>();
		
		for ( List<ReferenceWrapper> wrapperList: m_localStores.get( owner ).values() )
			wrappers.addAll( wrapperList );
		
		return wrappers;
	}

	public List<ReferenceWrapper> getAllStoreWrappers() {
		List<ReferenceWrapper> wrappers = new ArrayList<ReferenceWrapper>();
		wrappers.addAll( getLocalStores() );
		wrappers.addAll( getGlobalStores() );
		return wrappers;
	}

	public ReferenceWrapper getWrapperByStore( AbstractReferenceStore store ) {
		for ( ReferenceWrapper wrapper : getLocalStores() ) {
			if ( wrapper.getChangeable() == store )
				return wrapper;
		}

		for ( ReferenceWrapper wrapper : m_globalStores ) {
			if ( wrapper.getChangeable() == store )
				return wrapper;
		}

		return null;
	}

	public List<ReferenceWrapper> getLocalStores() {
		List<ReferenceWrapper> wrappers = new ArrayList<ReferenceWrapper>();
		for ( HashMap<Class,List<ReferenceWrapper>> algoStores : m_localStores.values() ) {
			for ( List<ReferenceWrapper> storeWrappers : algoStores.values() ) {
				for ( ReferenceWrapper wrapper : storeWrappers ) {
					wrappers.add( wrapper );
				}
			}
		}

		return wrappers;
	}

	public List<ReferenceWrapper> getGlobalStores() {
		return m_globalStores;
	}
}
