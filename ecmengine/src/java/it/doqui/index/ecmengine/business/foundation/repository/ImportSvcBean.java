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

package it.doqui.index.ecmengine.business.foundation.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.ImportRuntimeException;

public class ImportSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -9084583399572312015L;

	public void importTenant(byte[]data,String tenant) throws ImportRuntimeException{
		logger.debug("[ImportSvcBean::importTenant] BEGIN");
		ByteArrayInputStream bais=null;
		InputStreamReader isr=null;
		try {
			Location loc=new Location(new StoreRef("workspace","@"+tenant+"@SpacesStore"));
			bais=new ByteArrayInputStream(data);
			isr=new InputStreamReader(bais);
			getImportService().importView(isr, loc, null, null);
		}catch(Exception e){
			logger.error("[ImportSvcBean::importTenant] ERROR:\n"+e.getMessage());
			throw new ImportRuntimeException(FoundationErrorCodes.GENERIC_IMPORT_EXCEPTION);
		}finally{
			try{isr.close();bais.close();}catch(Exception e){}
			logger.debug("[ImportSvcBean::importTenant] END");
		}
	}


	private ImporterService getImportService() {
		return (ImporterService)serviceRegistry.getImporterService();
	}
	
}
