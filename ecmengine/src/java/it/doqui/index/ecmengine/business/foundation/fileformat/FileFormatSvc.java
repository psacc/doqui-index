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

import java.io.InputStream;
import java.io.File;

import it.doqui.index.fileformat.dto.FileFormatInfo;
import it.doqui.index.fileformat.dto.FileFormatVersion;
import it.doqui.index.fileformat.dto.FileInfo;
import it.doqui.index.ecmengine.exception.repository.FileFormatRuntimeException;

import javax.ejb.EJBLocalObject;

/**
 * Interfaccia pubblica del servizio di gestione dei job
 * esportata come componente EJB 2.1.
 *
 * <p>L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link JobSvcBean}.</p>
 *
 * <p>Tutti i metodi esportati dal bean di gestione dei job rimappano le
 * {@code RuntimeException} ricevute in
 * {@link it.doqui.index.ecmengine.exception.repository.JobRuntimeException}.
 * </p>
 *
 * @author DoQui
 *
 * @see JobSvcBean
 * @see it.doqui.index.ecmengine.exception.repository.JobRuntimeException
 */
public interface FileFormatSvc extends EJBLocalObject {
	/**
	 * Effettua il riconoscimento di un documento a partire da uno stream di dati.
	 * @param filename il nome del file
	 * @param is lo stream del contenuto del file
	 * @return Un array di riconoscimenti
	 * @throws FileFormatRuntimeException Se si verifica un errore durante il riconoscimento di un documento.
	 */
	public FileFormatInfo[] getFileFormatInfo(String filename, InputStream is) throws FileFormatRuntimeException;

	/**
	 * Effettua il riconoscimento di un documento a partire da un DTO che lo rappresenta.
	 * @param fileInfo il DTO che rappresenta il documento
	 * @return Un array di riconoscimenti.
	 * @throws FileFormatRuntimeException Se si verifica un errore durante il riconoscimento di un documento.
	 */
	public FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo) throws FileFormatRuntimeException;

	/**
	 * Effettua il riconoscimento di un documento a partire da un oggetto File.
	 * @param file L'oggetto File che rappresenta il documento.
	 * @return Un array di riconoscimenti.
	 * @throws FileFormatRuntimeException  Se si verifica un errore durante il riconoscimento di un documento.
	 */
	public FileFormatInfo[] getFileFormatInfo(File file) throws FileFormatRuntimeException;

	/**
	 * Restituisce la versione del signature file utilizzato da FileFormat.
	 * @return La versione del signature file.
	 * @throws FileFormatRuntimeException Se si verifica un errore durante l'esecuzione del metodo.
	 */
	public FileFormatVersion getFileFormatVersion() throws FileFormatRuntimeException;
}
