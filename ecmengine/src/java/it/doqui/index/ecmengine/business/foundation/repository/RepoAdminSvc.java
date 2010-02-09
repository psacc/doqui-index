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

import java.io.InputStream;
import java.util.List;

import it.doqui.index.ecmengine.exception.repository.RepoAdminRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Interfaccia pubblica del servizio di gestione dei content model dinamici
 * esportata come componente EJB 2.1.
 * 
 * <p>L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link RepoAdminSvcBean}.</p>
 * 
 * <p>Tutti i metodi esportati dal bean di gestione dei tenant rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link it.doqui.index.ecmengine.exception.repository.RepoAdminRuntimeException}.
 * </p>
 * 
 * @author Doqui
 * 
 * @see TenantAdminSvcBean
 * @see it.doqui.index.ecmengine.exception.repository.RepoAdminRuntimeException
 */
public interface RepoAdminSvc extends EJBLocalObject {

	public QName activateModel(String modelFileName) throws RepoAdminRuntimeException;

	public QName deactivateModel(String modelFileName) throws RepoAdminRuntimeException;

	public void deployModel(InputStream modelStream, String modelFileName) throws RepoAdminRuntimeException;

	public void undeployModel(String modelFileName) throws RepoAdminRuntimeException;

	public List<RepoModelDefinition> getModels() throws RepoAdminRuntimeException;

}
