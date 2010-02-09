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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.cache.SimpleCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExpiringCache<K extends Serializable, V> implements SimpleCache<K, V> {

	private static long DEFAULT_TIMEOUT = 1000L * 120L;			// 120 secondi
	private static long DEFAULT_EXPIRE_INTERVAL = 1000L * 30L;	// 30 secondi

	private static Log logger = LogFactory.getLog(SplittingNodeServiceConstants.ECMENGINE_SPLITTING_LOG_CATEGORY);
	private Map<K, ExpiringValue<V>> cacheMap;
	private long timeout;
	private long expireInterval;
	private long lastExpire;

	public ExpiringCache(long timeout, long expireInterval) {
		this(timeout);
		this.expireInterval = (expireInterval > 0L) ? expireInterval : DEFAULT_EXPIRE_INTERVAL;
	}

	public ExpiringCache(long timeout) {
		this();
		this.timeout = (timeout > 0L) ? timeout : DEFAULT_TIMEOUT;
	}

	public ExpiringCache() {
		this.timeout = DEFAULT_TIMEOUT;
		this.expireInterval = DEFAULT_EXPIRE_INTERVAL;

		cacheMap = new HashMap<K, ExpiringValue<V>>(600);	// About 450 entries...
	}

	public void clear() {
		cacheMap.clear();
	}

	public boolean contains(K key) {
		if (cacheMap.containsKey(key)) {
			return (get(key) != null);
		}
		return false;
	}

	public V get(K key) {
	    if (logger.isDebugEnabled()) {
    		logger.debug("[ExpiringCache::get] SIZE: " + cacheMap.size());
        }

		ExpiringValue<V> expiringValue = cacheMap.get(key);

		if (expiringValue == null) {
			return null;
		} else {
			V value = expiringValue.get();

    	    if (logger.isDebugEnabled()) {
	    		logger.debug("[ExpiringCache::get] GET: " + key + " -> " + value);
            }
			return value;
		}
	}

	public Collection<K> getKeys() {
		return cacheMap.keySet();
	}

	public void put(K key, V value) {
		if (value == null) {
			logger.warn("[ExpiringCache::put] Attempt to add null value ignored. Key: " + key);
			return;
		}
		ExpiringValue<V> expiringValue = new ExpiringValue<V>(timeout);

		expiringValue.put(value);
    	if (logger.isDebugEnabled()) {
		    logger.debug("[ExpiringCache::put] PUT: " + key + " -> " + value);
        }
		cacheMap.put(key, expiringValue);
		cleanup();	// Cleanup cache after every put
    	if (logger.isDebugEnabled()) {
    		logger.debug("[ExpiringCache::put] SIZE: " + cacheMap.size());
        }
	}

	public void remove(K key) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("[ExpiringCache::remove] REM: " + key);
        }
		cacheMap.remove(key);
    	if (logger.isDebugEnabled()) {
    		logger.debug("[ExpiringCache::remove] SIZE: " + cacheMap.size());
        }
	}

	private void cleanup() {
		if (lastExpire + expireInterval > System.currentTimeMillis()) {
			return;
		}

		List<K> toRemove = new ArrayList<K>();

		for (Map.Entry<K, ExpiringValue<V>> entry : cacheMap.entrySet()) {
			if (entry.getValue().isExpired()) {
				toRemove.add(entry.getKey());
			}
		}

		for (K key : toRemove) {
			cacheMap.remove(key);
		}

		lastExpire = System.currentTimeMillis();
    	if (logger.isDebugEnabled()) {
    		logger.debug("[ExpiringCache::cleanup] EXP: " + toRemove.size());
        }
		return;
	}

	private class ExpiringValue<T> {
	    private long timeout;
	    private long snapshot = 0;
	    private T value;

	    public ExpiringValue(long timeout) {
	        this.timeout = timeout;
	    }

	    public void put(T value) {
	        this.value = value;
	        this.snapshot = System.currentTimeMillis();
	    }

	    public T get() {
	        this.snapshot = System.currentTimeMillis(); // renew
	        return this.value;
	    }

	    public boolean isExpired() {
	    	return (snapshot + timeout < System.currentTimeMillis());
	    }

	    /**
	     * Clear the cache value
	     */
	    public void clear() {
	        this.value = null;
	    }
	}
}
