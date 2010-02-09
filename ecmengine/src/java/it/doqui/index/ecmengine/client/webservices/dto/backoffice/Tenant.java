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

package it.doqui.index.ecmengine.client.webservices.dto.backoffice;

public class Tenant {


	private String domain;
	private String rootContentStoreDir;
	private boolean enabled;
	private String adminPassword;

    // Vettore di coppie di oggetti ContentStore
    private ContentStoreDefinition[] contentStore;

    /**
     * Resituisce il nome del tenant.
     * @return Il nome del tenant.
     */
	public String getDomain() {
		return domain;
	}

	/**
	 * Imposta il nome del tenant.
	 * @param domain il nome del tenant.
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Restituisce il percorso in cui vengono memorizzati i contenuti fisici del tenant.
	 * @return Il percorso in cui vengono memorizzati i contenuti fisici del tenant.
	 */
	public String getRootContentStoreDir() {
		return rootContentStoreDir;
	}

	/**
	 * Imposta il percorso in cui vengono memorizzati i contenuti fisici del tenant.
	 * @param rootContentStoreDir Il percorso in cui vengono memorizzati i contenuti fisici del tenant.
	 */
	public void setRootContentStoreDir(String rootContentStoreDir) {
		this.rootContentStoreDir = rootContentStoreDir;
	}

	/**
	 * Restituisce la password dell'utente admin del tenant.
	 * @return La password dell'utente admin del tenant.
	 */
	public String getAdminPassword() {
		return adminPassword;
	}

	/**
	 * Imposta la password dell'utente admin del tenant
	 * @param adminPassword La password dell'utente admin del tenant.
	 */
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	/**
	 * Verifica se il tenant e' abilitato.
	 * @return true se il tenant e' abilitato, false altrimenti.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Imposta l'abilitazione del tenant.
	 * @param enabled L'abilitazione del tenant.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Restituisce l'elenco dei ContentStoreDefinition attivi su questo tenant
	 * @return Un vettore dei ContentStoreDefinition
	 */
	public ContentStoreDefinition[] getContentStores() {
		return contentStore;
	}

	/**
	 * Imposta l'elenco dei ContentStoreDefinition attivi su questo tenant
	 * @param contentStore L'elenco dei ContentStoreDefinition da usare su questo tenant
	 */
	public void setContentStores(ContentStoreDefinition[] contentStore) {
		this.contentStore = contentStore;
	}

}
