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

package it.doqui.index.ecmengine.business.personalization.importer;

import it.doqui.index.ecmengine.business.foundation.job.JobSvc;

import it.doqui.index.ecmengine.dto.backoffice.DataArchive;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import it.doqui.index.ecmengine.dto.OperationContext;
import org.alfresco.service.cmr.repository.NodeRef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Classe che esegue l'importazione di contenuti nel repository a partire da
 * archivi compressi.
 *
 * <p>
 * La procedeura di importazione prevede i seguenti passi:
 * <ol>
 * <li>creazione di una directory temporanea di lavoro</li>
 * <li>decompressione dell'archivio nella directory di lavoro</li>
 * <li>creazione della struttura presente nell'archivio sul repository</li>
 * <li>eliminazione della directory di lavoro</li>
 * </ol>
 * </p>
 *
 * <p>
 * Per la creazione dei contenuti sul repository vengono utilizzati i tipi
 * predefiniti:
 * <ul>
 * <li>{@code cm:content} per i file</li>
 * <li>{@code cm:folder} per le directory</li>
 * </ul>
 * con la possibilit&agrave; di fornire eventuali tipi specifici tramite il DTO
 * {@code DataArchive}.
 * </p>
 *
 * <p>
 * I formati supportati sono:
 * <ul>
 * <li>zip</li>
 * <li>tar</li>
 * <li>tar.gz</li>
 * </ul>
 * </p>
 *
 * @author Doqui
 *
 * @see it.doqui.index.ecmengine.dto.backoffice.DataArchive
 *
 */
public class ArchiveImporter implements EcmEngineConstants {

	private JobSvc jobService;

	private static Log logger = LogFactory.getLog(ECMENGINE_ROOT_LOG_CATEGORY);

	/**
	 * Costruttore predefinito.
	 *
	 * @param jobService L'istanza di {@code JobSvc} del job service.
	 */
	public ArchiveImporter(JobSvc jobService) {
		logger.debug("[ArchiveImporter::constructor] BEGIN");
		this.jobService         = jobService;
		logger.debug("[ArchiveImporter::constructor] END");
	}

	/**
	 * Esegue l'importazione dell'archivio compresso contenuto nel DTO
	 * {@code DataArchive} sotto il nodo specificato.
	 *
	 * @param archive Il {@code DataArchive} di cui importare i contenuti.
	 * @param node Il nodo sotto il quale importare i contenuti.
	 * @param context Il nodo context di riferimento
	 *
	 * @throws EcmEngineException
	 */
	public void importArchive(DataArchive archive, NodeRef node, OperationContext context ) throws EcmEngineException {
		logger.debug("[ArchiveImporter::importArchive] BEGIN");

		try {
	        jobService.createJob( ArchiveImporterJob.createBatchJob( archive,
                                                                     node,
                                                                     context
                                                                   ) );
		} catch(Exception e) {
			logger.error("[ArchiveImporter::importArchive] ERROR", e);
			throw new EcmEngineException("Error importing archive", e);
		} finally {
			logger.debug("[ArchiveImporter::importArchive] END");
		}
	}

}
