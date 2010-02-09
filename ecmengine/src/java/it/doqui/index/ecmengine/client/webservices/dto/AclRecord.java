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

package it.doqui.index.ecmengine.client.webservices.dto;

/**
 * DTO che rappresenta un record di Access Control List.
 *
 * <p>Un record di ACL &egrave; permette di associare un permesso ad una generica
 * authority (utente, gruppo, ruolo), specificando se l'accesso definito da tale permesso
 * sia consentito o negato. Un record di ACL pu&ograve; essere, inoltre, associato
 * ad un nodo (se la ACL &egrave; specifica di un nodo) e pu&ograve; avere scope limitato
 * ad un particolare aspect o tipo di contenuto.</p>
 *
 * @author Doqui
 */
public class AclRecord {



	private String authority;
	private String permission;
	private boolean accessAllowed;








	/**
	 * Restituisce il nome dell'authority associata al record di ACL.
	 *
	 * @return Il nome dell'authority.
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * Imposta il nome dell'authority associata al record di ACL.
	 *
	 * @param authority Il nome dell'authority (diverso da {@code null} e lungo
	 * almeno un carattere).
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}

	/**
	 * Restituisce il nome del permesso specificato dal record ACL.
	 *
	 * @return Il nome del permesso.
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * Imposta il nome del permesso associato al record di ACL.
	 *
	 * @param permission Il nome del permesso (diverso da {@code null} e lungo
	 * almeno un carattere).
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * Verifica se l'accesso definito dal permesso associato al record ACL &egrave;
	 * consentito o negato.
	 *
	 * @return Il valore {@code true} se l'accesso &egrave; consentito, {@code false} altrimenti.
	 */
	public boolean isAccessAllowed() {
		return accessAllowed;
	}

	/**
	 * Imposta il valore del flag che indica se l'accesso definito dal permesso
	 * associato al record ACL &egrave; consentito o negato.
	 *
	 * @param allowed {@code true} se l'accesso deve essere consentito, {@code false} se
	 * deve essere negato.
	 */
	public void setAccessAllowed(boolean allowed) {
		this.accessAllowed = allowed;
	}
}
