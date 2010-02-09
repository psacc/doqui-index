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
 
package it.doqui.index.ecmengine.dto.backoffice;

import it.doqui.index.ecmengine.dto.EcmEngineDto;

/**
 * DTO che rappresenta un utente dell'ECMENGINE dal punto di vista del backoffice.
 * 
 * <p>Questo DTO fornisce tutte le informazioni necessarie ai
 * servizi di backoffice per creare una nuova &quot;person&quot; sul repository
 * associandovi le informazioni di autenticazione. Non tutti gli attributi sono
 * obbligatori.</p>
 * 
 * <p>Il linea con il tipo {@code cm:person} di Alfresco sono obbligatori i seguenti
 * attributi:</p>
 * <ul>
 * 	<li>{@code name}: il nome dell'utente.</li>
 * 	<li>{@code surname}: il cognome dell'utente.</li>
 * 	<li>{@code username}: lo username <strong>univoco</strong> con cui l'utente &egrave;
 * identificato sul repository.</li>
 * </ul>
 * <p>Tutti gli altri attributi previsti da questo DTO sono facoltativi. Se non viene impostata
 * una {@code password} le informazioni di autenticazione non saranno create.</p>
 * 
 * @author Doqui
 */
public class User extends EcmEngineDto {

	private static final long serialVersionUID = -432639194766241726L;

	private String nome;
	private String cognome;
	private String nomeUtente;
	private String idOrganizzazione;
	private String password;
	private String homeFolderPath;


	/**
	 * Costruttore predefinito.
	 */
	public User() {
		super();
	}
	
	/**
	 * Inizializza un nuovo utente con i soli attributi obbligatori.
	 *
	 * @param nome Il nome dell'utente.
	 * @param cognome Il cognome dell'utente.
	 * @param nomeUtente Lo username <strong>univoco</strong> che identifica l'utente
	 * sul repository.
	 */
	public User(String nome, String cognome, String nomeUtente) {
		this.nome = nome;
		this.cognome = cognome;
		this.nomeUtente = nomeUtente;
	}

	/**
	 * Restituisce l'ID dell'organizzazione a cui appartiene l'utente (se impostato).
	 * 
	 * @return Restituisce l'ID dell'organizzazione, oppure {@code null} se non &egrave;
	 * impostato.
	 */
	public String getOrganizationId() {
		return idOrganizzazione;
	}

	/**
	 * Imposta l'ID dell'organizzazione a cui appartiene l'utente.
	 * 
	 * @param orgId L'ID dell'organizzazione.
	 */
	public void setOrganizationId(String orgId) {
		this.idOrganizzazione = orgId;
	}

	/**
	 * Restituisce la password dell'utente (se impostata).
	 * 
	 * @return Restituisce la password dell'utente, oppure {@code null} se non &egrave; impostata.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Imposta la password dell'utente.
	 * 
	 * @param password La password dell'utente.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Restituisce il nome dell'utente.
	 * 
	 * @return Il nome dell'utente.
	 */
	public String getName() {
		return nome;
	}

	/**
	 * Imposta il nome dell'utente.
	 * 
	 * @param name Il nome dell'utente.
	 */
	public void setName(String name) {
		this.nome = name;
	}
	
	/**
	 * Restituisce il cognome dell'utente.
	 * 
	 * @return Il cognome dell'utente.
	 */
	public String getSurname() {
		return cognome;
	}

	/**
	 * Imposta il cognome dell'utente.
	 * 
	 * @param surname Il cognome dell'utente.
	 */
	public void setSurname(String surname) {
		this.cognome = surname;
	}
	
	/**
	 * Restituisce lo username dell'utente.
	 * 
	 * @return Lo username dell'utente.
	 */
	public String getUsername() {
		return nomeUtente;
	}
	
	/**
	 * Imposta lo username dell'utente.
	 * 
	 * @param username Lo username dell'utente.
	 */
	public void setUsername(String username) {
		this.nomeUtente = username;
	}

	/**
	 * Restituisce il path della user home dell'utente.
	 * 
	 * @return il path della user home dell'utente.
	 */
	public String getHomeFolderPath() {
		return homeFolderPath;
	}

	/**
	 * Imposta il path della user home dell'utente.
	 * 
	 * @param homeFolderPath il path della user home dell'utente.
	 */
	public void setHomeFolderPath(String homeFolderPath) {
		this.homeFolderPath = homeFolderPath;
	}

}
