package ca.barelabs.bareconnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiMap<K,V> {
	
	private Map<K, List<V>> mMap = new HashMap<K, List<V>>();
	private int mTotalItems;

	
	public Set<K> keySet() {
		return mMap.keySet();
	}
	
	public List<V> get(K key) {
		return mMap.containsKey(key) ? mMap.get(key) : new ArrayList<V>();
	}
	
	public boolean containsKey(Object key) {
		return mMap.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		for (Entry<K, List<V>> entry : mMap.entrySet()) {
			if (entry.getValue().contains(value)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsEntry(Object key, Object value) {
		if (mMap.containsKey(key)) {
			List<V> values = mMap.get(key);
			return values.contains(value);
		}
		return false;
	}
	
	public boolean put(K key, V value) {
		List<V> values = mMap.get(key);
		if (values == null) {
			values = new ArrayList<V>();
			mMap.put(key, values);
		}
		mTotalItems++;
		return values.add(value);
	}
	
	public boolean putAll(K key, Collection<? extends V> valuesToAdd) {
		if (valuesToAdd == null) {
			return false;
		}
		List<V> values = mMap.get(key);
		if (values == null) {
			values = new ArrayList<V>();
			mMap.put(key, values);
		}
		mTotalItems += valuesToAdd.size();
		return values.addAll(valuesToAdd);
	}
	
	public boolean remove(Object key, Object value) {
		if (mMap.containsKey(key) && mMap.get(key).remove(value)) {
			mTotalItems--;
			return true;
		}
		return false;
	}
	
	public List<V> removeAll(Object key) {
		if (mMap.containsKey(key)) {
			List<V> values = mMap.remove(key);
			mTotalItems -= values.size();
			return values;
		}
		return new ArrayList<V>();
	}
	
	public List<V> replaceValues(K key, Collection<? extends V> valuesToAdd) {
		List<V> values = mMap.containsKey(key) ? mMap.remove(key) : new ArrayList<V>();
		mTotalItems -= values.size();
		putAll(key, valuesToAdd);
		return values;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public int size() {
		return mTotalItems;
	}

	public void clear() {
		mMap.clear();
		mTotalItems = 0;
	}
}
