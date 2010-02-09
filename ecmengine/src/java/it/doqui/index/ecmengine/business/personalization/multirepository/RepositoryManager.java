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

import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton per la gestione della lista dei repository conosciuti dall'ECMENGINE.
 *
 * <p>Questa classe mantiene in una variabile {@code ThreadLocal} l'identificativo
 * del repository correntemente selezionato. Tale repository &egrave; quello che sar&agrave;
 * utilizzato come target per tutte le operazioni dell'ECMENGINE fino a quando non verr&agrave;
 * sostituito mediante una nuova chiamata al metodo {@link #setCurrentRepository(String)}.</p>
 *
 * @see #setCurrentRepository(String)
 * @see #getCurrentRepository()
 *
 * @author Doqui
 */
public class RepositoryManager {

	// NOTA (Bono)
	// Questa classe e` un bean che viene utilizzato anche come singleton.
	// La motivazione e` dovuta al fatto che la stessa istanza deve essere
	// condivisa da un application context di spring e da una classe
	// istanziata con Class.newInstance() senza argomenti.
	// L'aggiunta quindi della variabile statica 'theInstance' serve come
	// workaround per rendere il bean accessibile alla classe istanziata
	// con Class.newInstance().
	/*
	 * Aggiunto il parametro factory-method="getInstance" nella dichiarazione del
	 * bean Spring. In questo modo il bean e` effettivamente un singleton istanziato
	 * con un metodo static. - FF
	 */
	private static RepositoryManager theInstance;

	private static Log logger = LogFactory.getLog(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_LOG_CATEGORY);

	private Map<String, Repository> repositoryMap;

	private Repository defaultRepository;

	private static ThreadLocal<String> repositoryHolder = new ThreadLocal<String>();

	/**
	 * Restituisce l'unica istanza di {@code RepositoryManager}.
	 *
	 * @return L'unica istanza di {@code RepositoryManager}.
	 */
	public static RepositoryManager getInstance() {
		if (theInstance == null) {
			theInstance = new RepositoryManager();
		}
		return theInstance;
	}

	/**
	 * Costruisce una nuova istanza di RepositoryManager.
	 */
	private RepositoryManager() {
		repositoryMap = new LinkedHashMap<String, Repository>(4);
		RepositoryManager.theInstance = this;
	}

	/**
	 * Imposta il repository corrente.
	 *
	 * <p>Se viene specificato {@code null} come identificativo di repository
	 * questo metodo seleziona il repository predefinito. Se l'identificativo
	 * specificato non corrisponde ad alcun repository noto viene sollevata una
	 * {@code IllegalArgumentException}.</p>
	 *
	 * @param repositoryId L'identificativo del repository da selezionare.
	 *
	 * @see #getDefaultRepository()
	 */
	public static void setCurrentRepository(String repositoryId) {
		if (repositoryId == null) {
            if(logger.isDebugEnabled()) {
	    		logger.debug("[RepositoryManager::setCurrentRepository] " +
					"Got null repository... setting default: " + getInstance().defaultRepository.getId());
            }
			repositoryHolder.set(getInstance().defaultRepository.getId());
		} else if (getInstance().repositoryMap.containsKey(repositoryId)) {
			repositoryHolder.set(repositoryId);
		} else {
			logger.warn("[RepositoryManager::setCurrentRepository] Unknown repository: " + repositoryId);
			throw new IllegalArgumentException("Unknown repository: " + repositoryId);
		}
	}

	/**
	 * Restituisce il repository corrente, oppure il repository di default se
	 * non &egrave; stato impostato un repository corrente.
	 *
	 * @return Il nome del repository corrente.
	 */
	public static String getCurrentRepository() {
		String repo = repositoryHolder.get();

		if (repo == null) {
            if (logger.isDebugEnabled()){
    			logger.debug("[RepositoryManager::getCurrentRepository] Thread '" + Thread.currentThread() +"' -- Current repository not set. Returning default...");
	    	}
		}

		return (repo != null) ? repo : getInstance().defaultRepository.getId();
	}

	/**
	 * Restituisce l'istanza di {@link Repository} configurata come predefinita.
	 *
	 * @return L'istanza di {@link Repository} predefinita.
	 */
	public Repository getDefaultRepository() {
		return this.defaultRepository;
	}

	/**
	 * Imposta l'istanza di {@link Repository} predefinita.
	 *
	 * @param defaultRepository L'istanza di {@link Repository}.
	 */
	public void setDefaultRepository(Repository defaultRepository) {
		this.defaultRepository = defaultRepository;
	}

	/**
	 * Restituisce la lista dei repository conosciuti.
	 *
	 * @return La lista dei repository.
	 */
	public List<Repository> getRepositories() {
		return new ArrayList<Repository>(repositoryMap.values());
	}

	/**
	 * Imposta la lista dei repository conosciuti.
	 *
	 * @param repositories La lista dei repository da impostare.
	 */
	public void setRepositories(List<Repository> repositories) {
        // MB: 16:54:44 giovedi' 11 settembre 2008
        // aggiunto l'azzeramento di repositoryMap, per evitare che
        // il setter non agisca da setter ma da adder
		repositoryMap = new LinkedHashMap<String, Repository>(4);

		for (Repository repository : repositories) {
			this.repositoryMap.put(repository.getId(), repository);
		}
	}

	/**
	 * Restituisce l'istanza di {@link Repository} corrispondente all'identificativo
	 * specificato.
	 *
	 * @param repositoryId L'identificativo.
	 *
	 * @return L'istanza di {@link Repository}.
	 */
	public Repository getRepository(String repositoryId) {
		return this.repositoryMap.get(repositoryId);
	}
}
