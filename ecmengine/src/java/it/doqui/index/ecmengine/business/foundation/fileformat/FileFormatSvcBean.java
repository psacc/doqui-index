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
 
package it.doqui.index.ecmengine.business.foundation.fileformat;

import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.FileFormatRuntimeException;
import it.doqui.index.fileformat.business.service.FileFormatServiceBusinessInterface;
import it.doqui.index.fileformat.dto.FileFormatInfo;
import it.doqui.index.fileformat.dto.FileFormatVersion;
import it.doqui.index.fileformat.dto.FileInfo;

import java.io.File;
import java.io.InputStream;

import org.alfresco.service.namespace.QName;

public class FileFormatSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -8496226823212946953L;

	public FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo) throws FileFormatRuntimeException {
		logger.debug("[FileFormatSvcBean::getFileFormatInfo] BEGIN");
		FileFormatInfo[] result = null;
		try {
			result = getFileFormatService().getFileFormatInfo(fileInfo);
		} catch (Exception e) {
			handleFileFormatServiceException("getFileFormatInfo", e);
		} finally {
			logger.debug("[FileFormatSvcBean::getFileFormatInfo] END");
		}
		return result;
	}

	public FileFormatInfo[] getFileFormatInfo(File file) throws FileFormatRuntimeException {
		logger.debug("[FileFormatSvcBean::getFileFormatInfo] BEGIN");
		FileFormatInfo[] result = null;
		try {
			result = getFileFormatService().getFileFormatInfo(file);
		} catch (Exception e) {
			handleFileFormatServiceException("getFileFormatInfo", e);
		} finally {
			logger.debug("[FileFormatSvcBean::getFileFormatInfo] END");
		}
		return result;
	}
	
	public FileFormatInfo[] getFileFormatInfo(String filename, InputStream is) throws FileFormatRuntimeException {
		logger.debug("[FileFormatSvcBean::getFileFormatInfo] BEGIN");
		FileFormatInfo[] result = null;
		try {
			result = getFileFormatService().getFileFormatInfo(filename, is);
		} catch (Exception e) {
			handleFileFormatServiceException("getFileFormatInfo", e);
		} finally {
			logger.debug("[FileFormatSvcBean::getFileFormatInfo] END");
		}
		return result;
	}

	public FileFormatVersion getFileFormatVersion()throws FileFormatRuntimeException{
		logger.debug("[FileFormatSvcBean::getFileFormatVersion] BEGIN");
		FileFormatVersion result = null;
		try {
			result = getFileFormatService().getFileFormatVersion();
		} catch (Exception e) {
			handleFileFormatServiceException("getFileFormatVersion", e);
		} finally {
			logger.debug("[FileFormatSvcBean::getFileFormatVersion] END");
		}
		return result;		
	}
	
	public void updateSignatureFile() throws FileFormatRuntimeException {
		logger.debug("[FileFormatSvcBean::updateSignatureFile] BEGIN");
		try {
			//getFileFormatService().updateSignatureFile();
		} catch (Exception e) {
			handleFileFormatServiceException("updateSignatureFile", e);
		} finally {
			logger.debug("[FileFormatSvcBean::updateSignatureFile] END");
		}
	}

	private FileFormatServiceBusinessInterface getFileFormatService() {
		return (FileFormatServiceBusinessInterface)serviceRegistry.getService(QName.createQName(ECMENGINE_FILE_FORMAT_SERVICE_BEAN));
	}

	private void handleFileFormatServiceException(String methodName, Throwable e) throws FileFormatRuntimeException {
		logger.warn("[FileFormatSvcBean::handleFileFormatServiceException] Exception in method '" + methodName + "': " + e.getMessage(), e);

		if (e instanceof org.alfresco.repo.security.permissions.AccessDeniedException) {
			throw new FileFormatRuntimeException(FoundationErrorCodes.ACCESS_DENIED_ERROR);
		} else {
			throw new FileFormatRuntimeException(FoundationErrorCodes.GENERIC_FILE_FORMAT_SERVICE_ERROR);
		}
	}

}
