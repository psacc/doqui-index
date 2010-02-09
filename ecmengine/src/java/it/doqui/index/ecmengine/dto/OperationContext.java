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
 
package it.doqui.index.ecmengine.dto;

/**
 * DTO contenente le informazioni di contesto da utilizzare per
 * l'esecuzione delle chiamate ai servizi ECMENGINE.
 * 
 * <p>Le informazioni contenute in questo DTO sono le seguenti:<p>
 * <ul>
 *  <li><i>Username:</i> username dell'utente 
 *  applicativo da utilizzare nell'autenticazione sul repository ECMENGINE 
 *  <strong>(obbligatorio)</strong>.</li>
 *  <li><i>Password:</i> password dell'utente 
 *  applicativo da utilizzare nell'autenticazione sul repository ECMENGINE 
 *  <strong>(obbligatorio)</strong>.</li>
 *  <li><i>Nome fisico:</i> nome reale dell'utente 
 *  per conto del quale viene invocato il servizio sull'ECMENGINE.</li>
 *  <li><i>Fruitore:</i> nome dell'applicativo fruitore che invoca il
 *  servizio sull'ECMENGINE <strong>(obbligatorio)</strong>.</li>
 *  <li><i>Repository:</i> repository fisico
 *  su cui &egrave; definito l'utente applicativo sull'ECMENGINE.</li>
 * </ul>
 * 
 * @author Doqui
 *
 */
public class OperationContext extends EcmEngineDto {

	private static final long serialVersionUID = -6025097836655290993L;

	private String username;
	private String password;
	private String nomeFisico;
	private String fruitore;
	private String repository;
	
	/**
	 * Costruttore predefinito.
	 */
	public OperationContext() {
		super();
	}

	/**
	 * Restituisce lo username dell'utente applicativo per l'autenticazione sull'ECMENGINE.
	 * 
	 * @return Lo username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Imposta lo username dell'utente applicativo per l'autenticazione sull'ECMENGINE.
	 * 
	 * @param username Lo username per l'autenticazione sull'ECMENGINE.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Restituisce la password dell'utente applicativo per l'autenticazione sull'ECMENGINE.
	 * 
	 * @return La password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Imposta la password dell'utente applicativo per l'autenticazione sull'ECMENGINE.
	 * 
	 * @param password La password per l'autenticazione sull'ECMENGINE.
	 */
	public void setPassword(String password) {	
		this.password = password;
	}

	/**
	 * Restituisce il nome fisico dell'utente che sta eseguendo l'operazione sull'ECMENGINE.
	 * 
	 * @return Il nome fisico, oppure {@code null} se non &egrave; stato impostato alcun nome fisico.
	 */
	public String getNomeFisico() {
		return nomeFisico;
	}

	/**
	 * Imposta il nome fisico dell'utente che sta eseguendo l'operazione sull'ECMENGINE.
	 * 
	 * <p>Questo metodo accetta in input il valore {@code null} per eliminare il nome
	 * eventualmente impostato precedentemente.</p>
	 * 
	 * @param nomeFisico Il nome fisico.
	 */
	public void setNomeFisico(String nomeFisico) {
		this.nomeFisico = nomeFisico;
	}

	/**
	 * Restituisce il nome del fruitore che sta invocando il servizio sull'ECMENGINE.
	 * 
	 * @return Il nome del fruitore.
	 */
	public String getFruitore() {
		return fruitore;
	}

	/**
	 * Imposta il nome del fruitore che sta invocando il servizio sull'ECMENGINE.
	 *
	 * @param fruitore Il nome del fruitore.
	 */
	public void setFruitore(String fruitore) {
		this.fruitore = fruitore;
	}

	/**
	 * Restituisce il nome del repository fisico su cui operare.
	 * 
	 * @return Il nome del repository.
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * Imposta il nome del repository fisico su cui operare.
	 *
	 * @param repository Il nome del repository.
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
}
