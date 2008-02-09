package org.mailster.util;

/*
 * Copyright (C) 2004, Intalio Inc.
 *
 * The program(s) herein may be used and/or copied only with the
 * written permission of Intalio Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 * $Id$
 * 
 * Created on Dec 14, 2004 by kvisco
 *
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A very simple extended HashMap, which maintains order via an ArrayList.
 * 
 * This class provides similar, though not identical, functionality as 
 * the JDK's LinkedHashMap, but works under JDK 1.2 and JDK 1.3.
 * 
 * This class is not synchronized, if more than one thread accesses an
 * instance of this class and at least one thread modifies the map, 
 * the OrderedHashMap instance must be synchronized via a call to
 * Collections.synchronizedMap method.
 * 
 * The #entrySet() and #keySet() methods return unmodifiable sets.
 * 
 * The #values() method returns an unmodifiable collection.
 * 
 * @author <a href="mailto:kvisco-at-intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 * 
 * This file has been modified to use JDK 1.5 generics.
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class OrderedHashMap<K, V> 
	extends HashMap<K, V> 
{
	private static final long serialVersionUID = -6150936168245585480L;
	/**
     * Ordered list of contained values
     */
    private ArrayList<V> _orderedValues = null;
    
    /**
     * Creates a new OrderedHashMap
     */
    public OrderedHashMap() 
    {
        super();
        _orderedValues = new ArrayList<V>();
    }
    
    /**
     * Creates a new OrderedHashMap with the given initial capacity
     * 
     * @param initialCapacity
     */
    public OrderedHashMap(int initialCapacity) 
    {
        super(initialCapacity);
        _orderedValues = new ArrayList<V>(initialCapacity);
    }
    
    /**
     * Creates a new OrderedHashMap with the same entries
     * as the given map.
     * 
     * @param m the Map to initialize this Map with
     */
    public OrderedHashMap(Map<K, V> m) 
    {
        this(m.size());
        putAll(m);
    }
    
    /**
     * @see java.util.Map#clear()
     */
    public void clear() 
    {
        super.clear();
        _orderedValues.clear();
    }
    
    /**
     * Returns the Map.Entry set for this Map. Note that the 
     * returned Set is an unmodifiable Set
     * 
     * @see java.util.Map#entrySet()
     */
    public Set<Map.Entry<K,V>> entrySet() 
    {
        return Collections.unmodifiableSet(super.entrySet());
    } 

    /**
     * Returns the key set for this Map. Note that the returned 
     * set is an unmodifiable Set
     * 
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() 
    {
        return Collections.unmodifiableSet(super.keySet());
    } 
    
    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) 
    {
        //-- remove any current value references from
        //-- the ordered list for the given key
        V obj = super.get(key);
        if (obj != null)
            _orderedValues.remove(obj);
        
    	obj = super.put(key, value);
        _orderedValues.add(value);
        return obj;
    }
    
    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> m) 
    {
        Set<? extends Map.Entry<? extends K, ? extends V>> entries = m.entrySet();
        if (!entries.isEmpty()) 
        {
        	Iterator<? extends Map.Entry<? extends K, ? extends V>> iterator = entries.iterator();
            while (iterator.hasNext()) 
            {
            	Map.Entry<? extends K, ? extends V> entry = iterator.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) 
    {
        V obj = super.remove(key);
        _orderedValues.remove(obj);
        return obj;
    }

    /**
     * Returns the set of values for this Map. Note that
     * the returned Collection is unmodifiable.
     * 
     * @see java.util.Map#values()
     */
    public Collection<V> values() 
    {
        return Collections.unmodifiableList(_orderedValues);
    }
}