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

package it.doqui.index.ecmengine.business.job.fileformat;

import static it.doqui.index.ecmengine.util.EcmEngineConstants.ECMENGINE_REPO_ADMIN_SERVICE_BEAN;
import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.fileformat.business.service.FileFormatServiceBusinessInterface;
import it.doqui.index.fileformat.business.service.FileFormatServiceImpl;



import org.alfresco.service.cmr.admin.RepoAdminService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class FileFormatUpdateSignatureFileJob implements Job, EcmEngineConstants{

	private static Log logger = LogFactory.getLog(ECMENGINE_JOB_MODEL_LOG_CATEGORY);
	private StopWatch stopwatch;
	private static boolean running=false;

	private FileFormatServiceBusinessInterface fileFormatService;

	public void execute(JobExecutionContext ctx) throws JobExecutionException {		
		logger.debug("[FileFormatUpdateSignatureFileJob::execute] BEGIN");
		if (!running) {
			synchronized (this) {
				if (!running) {
					running = true;
				} else {
					logger.debug("[FileFormatUpdateSignatureFileJob::execute] job already running");
					logger.debug("[FileFormatUpdateSignatureFileJob::execute] END");
					return;
				}
			}
		} else {
			logger.debug("[FileFormatUpdateSignatureFileJob::execute] job already running");
			logger.debug("[FileFormatUpdateSignatureFileJob::execute] END");
			return;
		}
		try {
			fileFormatService    = (FileFormatServiceBusinessInterface)ctx.getJobDetail().getJobDataMap().get(ECMENGINE_FILE_FORMAT_SERVICE_BEAN);
			fileFormatService.updateSignatureFile();
		} catch (Exception e) {			
			throw new JobExecutionException("Exception : " + e.getMessage(), e);
		} finally {
			running=false;
			logger.debug("[FileFormatUpdateSignatureFileJob::execute] END");        	
		}		
	}

	/** Azzera e avvia la misurazione dei tempi da parte dello stopwatch. */	
	protected void start() {
		stopwatch = new StopWatch(ECMENGINE_STOPWATCH_LOG_CATEGORY);
		stopwatch.start();
	}

	/** Arresta la misurazione dei tempi da parte dello stopwatch. */
	protected void stop() {
		stopwatch.stop();
	}

	/**
	 * Registra sul logger dello stowpatch il tempo misurato al momento della chiamata.
	 * 
	 * @param className Il nome della classe chiamante.
	 * @param methodName Il nome del metodo chiamante.
	 * @param ctx Il contesto in cui il metodo &egrave; stato chiamato.
	 * @param message Un messaggio da registrare nel log assieme al tempo.
	 */
	protected void dumpElapsed(String className, String methodName, String ctx, String message) {
		stopwatch.dumpElapsed(className, methodName, ctx, message);
	}
}
