package de.litona.ipInformation;

import java.util.HashMap;

final class CacheMap<K, E> {

	private final long expireTime;
	private final HashMap<K, CacheEntry<E>> cache = new HashMap<>();

	CacheMap(long expireTime) {
		this.expireTime = expireTime;
	}

	E get(K key) {
		if(cache.containsKey(key)) {
			CacheEntry<E> ce = cache.get(key);
			if(System.currentTimeMillis() < ce.created + expireTime)
				return ce.entry;
			else
				cache.remove(key);
		}
		return null;
	}

	void put(K key, E entry) {
		if(!cache.containsKey(key))
			cache.put(key, new CacheEntry<>(entry));
	}

	private final static class CacheEntry<Entry> {

		private final Entry entry;
		private final long created;

		private CacheEntry(Entry entry) {
			this.entry = entry;
			this.created = System.currentTimeMillis();
		}
	}
}