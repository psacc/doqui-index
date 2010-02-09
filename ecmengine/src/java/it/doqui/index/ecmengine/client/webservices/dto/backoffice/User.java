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

/**
 * DTO che rappresenta un utente dell'ECMENGINE dal punto di vista del
 * backoffice.
 * 
 * <p>
 * Questo DTO fornisce tutte le informazioni necessarie ai servizi di backoffice
 * per creare una nuova &quot;person&quot; sul repository associandovi le
 * informazioni di autenticazione. Non tutti gli attributi sono obbligatori.
 * </p>
 * 
 * <p>
 * Il linea con il tipo {@code cm:person} di Alfresco sono obbligatori i
 * seguenti attributi:
 * </p>
 * <ul>
 * <li>{@code name}: il nome dell'utente.</li>
 * <li>{@code surname}: il cognome dell'utente.</li>
 * <li>{@code username}: lo username <strong>univoco</strong> con cui
 * l'utente &egrave; identificato sul repository.</li>
 * </ul>
 * <p>
 * Tutti gli altri attributi previsti da questo DTO sono facoltativi. Se non
 * viene impostata una {@code password} le informazioni di autenticazione non
 * saranno create.
 * </p>
 * 
 * @author Doqui
 */
public class User {

	private String name;
	private String surname;
	private String username;
	private String organizationId;
	private String password;
	private String homeFolderPath;

	public String getSurname() {
		return surname;
	}

	public void setSurname(String cognome) {
		this.surname = cognome;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String idOrganizzazione) {
		this.organizationId = idOrganizzazione;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String nomeUtente) {
		this.username = nomeUtente;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHomeFolderPath() {
		return homeFolderPath;
	}

	public void setHomeFolderPath(String homeFolderPath) {
		this.homeFolderPath = homeFolderPath;
	}
}
