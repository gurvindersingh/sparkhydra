package com.lordjoe.algorithms;

import java.io.Serializable;
import java.util.*;

/**
 * Map containing a list of items
 *
 * @param <K>  key type
 * @param <V>  value type
 */
public class MapOfLists<K,V> implements Serializable {
    private Map<K, List<V>> items = new HashMap<K, List<V>>();

    public MapOfLists() {
    }


    public int size() {
        return items.size();
    }

   public List<V> get(K key)  {
       return items.get(key);
   }

    public void putItem(K key,V value)  {
        if(items.containsKey(key))   {
            items.get(key).add(value);
        }
        else {
            List<V>  added = new ArrayList<V>();
            added.add(value);
            items.put(key,added);
        }
    }


}
