/* Index ECM Engine - A system for managing the capture (when created 
 * or received), classification (cataloguing), storage, retrieval, 
 * revision, sharing, reuse and disposition of documents.
 *
 * Copyright (C) 2008 Regione Piemonte
 * Copyright (C) 2008 Provincia di Torino
 * Copyright (C) 2008 Comune di Torino
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
 
package it.doqui.index.ecmengine.business.personalization.splitting.util;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LRUCache<K extends Serializable, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = -3811728082796063327L;
	private static final int DEFAULT_MAX_SIZE = 500;

	private static Log logger = LogFactory.getLog(SplittingNodeServiceConstants.ECMENGINE_SPLITTING_LOG_CATEGORY);
	
	private int maxSize;
	
	public LRUCache(int maxSize) {
		super((int) (maxSize / 0.75F), 0.75F, true);
		this.maxSize = maxSize;
		logger.debug("[LRUCache::constructor] IS: " + (int) (maxSize / 0.75F) + 
				" - LF: 0.75F - MS: " + maxSize);
	}
	
	public LRUCache() {
		super((int) (DEFAULT_MAX_SIZE / 0.75F), 0.75F, true);
		this.maxSize = DEFAULT_MAX_SIZE;
		logger.debug("[LRUCache::constructor] IS: " + (int) (DEFAULT_MAX_SIZE / 0.75F) + 
				" - LF: 0.75F - MS: " + DEFAULT_MAX_SIZE);
	}

	public V get(K key) {
		final long start = System.currentTimeMillis();
		V value = super.get(key);
		final long stop = System.currentTimeMillis();
		logger.debug("[LRUCache::get] GET: " + key + " -> " + value + " [" + (stop - start) + " ms]");
		
		return value;
	}
	
	public V put(K key, V value) {
		final long start = System.currentTimeMillis();
		V v = super.put(key, value);
		final long stop = System.currentTimeMillis();
		logger.debug("[LRUCache::put] PUT: " + key + " -> " + value + " [" + (stop - start) + " ms]");
		
		return v;
	}
	
	public void clear() {
		final int size = super.size();
		final long start = System.currentTimeMillis();
		super.clear();
		final long stop = System.currentTimeMillis();
		logger.debug("[LRUCache::clear] CLEAR: " + size + " entries [" + (stop - start) + " ms]");
	}
	
	public V remove(K key) {
		final long start = System.currentTimeMillis();
		V value = super.remove(key);
		final long stop = System.currentTimeMillis();
		logger.debug("[LRUCache::remove] REM: " + key + " -> " + value + " [" + (stop - start) + " ms]");
		
		return value;
	}
	
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > this.maxSize;
    }
}
