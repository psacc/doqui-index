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

package it.doqui.index.ecmengine.business.foundation.mimetype;

import org.alfresco.service.namespace.QName;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.mimetype.MimetypeBusinessInterface;
//import it.doqui.index.ecmengine.business.mimetype.dto.Mimetype;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.exception.repository.MimetypeRuntimeException;

public class MimetypeSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -8337347895671012405L;

	public Mimetype[] getMimetype(Mimetype mimetype) throws MimetypeRuntimeException {
		logger.debug("[MimetypeSvcBean::getMimetype] BEGIN");
        Mimetype[] mt = null;
		try {
			mt = getMimetypeService().getMimetype(mimetype);
		} catch (Exception e) {
			handleMimetypeServiceException("getMimetype", e);
		} finally {
			logger.debug("[MimetypeSvcBean::getMimetype] END");
		}
        return mt;
	}

	private MimetypeBusinessInterface getMimetypeService() {
		return (MimetypeBusinessInterface)serviceRegistry.getService(QName.createQName(ECMENGINE_MIMETYPE_SERVICE_BEAN));
	}

	private void handleMimetypeServiceException(String methodName, Throwable e) throws MimetypeRuntimeException {
		logger.warn("[MimetypeSvcBean::handleMimetypeServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new MimetypeRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new MimetypeRuntimeException(FoundationErrorCodes.GENERIC_MIMETYPE_SERVICE_ERROR);
		}
	}

}
