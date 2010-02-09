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

package it.doqui.index.ecmengine.business.job;

import it.doqui.index.ecmengine.business.job.dto.BatchJob;

/**
 * Interface che descrive le operazioni di business della gestione della coda
 * dei job.
 *
 * @author DoQui
 *
 */
public interface JobBusinessInterface {

	/**
	 * Metodo che inserisce un job nella coda di esecuzione.
	 *
	 * @param job
	 *            L'istanza di {@code BatchJob} contenente i dati del job da
	 *            inserire nella coda di esecuzione.
	 *
	 * @throws Exception
	 */
	public void createJob(BatchJob job) throws Exception;

	/**
	 * Metodo che estrae un job dalla coda di esecuzione.
	 *
	 * @param jobRef
	 *            Stringa che identifica l'esecutore che ha rischiesto
	 *            l'accodamento del job.
	 * @return L'istanza di {@code BatchJob} contenente i dati del job da
	 *         gestire.
	 * @throws Exception
	 */
	public BatchJob getNextJob(String jobRef) throws Exception;

	/**
	 * Aggiorna i dati di un job nella coda di esecuzione.
	 *
	 * @param job
	 *            L'istanza di {@code BatchJob} contenente le informazioni da
	 *            aggiornare.
	 * @throws Exception
	 */
	public void updateJob(BatchJob job) throws Exception;

	/**
	 * Metodo che verifica lo stato RUNNING di un job
	 *
	 * @param jobRef
	 *            Stringa che identifica l'esecutore che ha rischiesto
	 *            l'accodamento del job.
	 *
     * @return true se il job e' in stato running
     *
	 * @throws Exception
	 */
	public boolean isExecuting(String jobRef) throws Exception;

	/**
	 * Metodo che restituisce tutti i job associati a un certo executor
	 *
	 * @param jobRef
	 *            Stringa che identifica l'esecutore che ha rischiesto
	 *            l'accodamento del job.
	 *
     * @return null, oppure un vettore di elementi
     *
	 * @throws Exception
	 */
	public BatchJob[] getJobsByExecutor(String jobRef) throws Exception;

	/**
	 * Metodo che restituisce tutti i job associati a un certo executor e status
	 *
	 * @param jobRef
	 *            Stringa che identifica l'esecutore che ha rischiesto
	 *            l'accodamento del job.
     *
	 * @param status
	 *            Stringa che identifica lo stato del job da prendere
	 *
     * @return null, oppure un vettore di elementi
     *
	 * @throws Exception
	 */
	public BatchJob[] getJobsByStatus(String jobRef, String status) throws Exception;

}
