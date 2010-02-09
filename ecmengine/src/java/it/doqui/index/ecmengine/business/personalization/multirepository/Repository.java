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

package it.doqui.index.ecmengine.business.personalization.multirepository;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;

public class Repository {

	private String id;
	private DataSource dataSource;
	private String contentRootLocation;
	private String indexRootLocation;
	private String indexBackupLocation;
	private String cacheConfigLocation;
	private String indexRecoveryMode;

    private List<ContentStoreDefinition> contentstores;
	public Repository() {
        contentstores = new ArrayList<ContentStoreDefinition>();
    }

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContentRootLocation() {
		return contentRootLocation;
	}

	public void setContentRootLocation(String contentStoreLocation) {
		this.contentRootLocation = contentStoreLocation;
	}

	public String getIndexRootLocation() {
		return indexRootLocation;
	}

	public void setIndexRootLocation(String indexLocation) {
		this.indexRootLocation = indexLocation;
	}

	public String getIndexBackupLocation() {
		return indexBackupLocation;
	}

	public void setIndexBackupLocation(String indexBackupLocation) {
		this.indexBackupLocation = indexBackupLocation;
	}

	public String getCacheConfigLocation() {
		return cacheConfigLocation;
	}

	public void setCacheConfigLocation(String cacheConfigLocation) {
		this.cacheConfigLocation = cacheConfigLocation;
	}

	public String getIndexRecoveryMode() {
		return indexRecoveryMode;
	}

	public void setIndexRecoveryMode(String indexRecoveryMode) {
		this.indexRecoveryMode = indexRecoveryMode;
	}

	/**
	 * Restituisce la lista dei contentstore conosciuti.
	 *
	 * @return La lista dei contentstore.
	 */
	public List<ContentStoreDefinition> getContentStores() {
		return contentstores;
	}

	/**
	 * Imposta la lista dei contentstore conosciuti.
	 *
	 * @param contentstores La lista dei contentstore da impostare.
	 */
	public void setContentStores(List<ContentStoreDefinition> contentstores) {
		this.contentstores = contentstores;
	}

}
