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

import it.doqui.index.ecmengine.exception.repository.ExportRuntimeException;
import it.doqui.index.ecmengine.exception.repository.ImportRuntimeException;
import it.doqui.index.ecmengine.exception.repository.TenantRuntimeException;

import java.util.List;

import javax.ejb.EJBLocalObject;

import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;

/**
 * Interfaccia pubblica del servizio di gestione dei tenant
 * esportata come componente EJB 2.1.
 *
 * <p>L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link TenantAdminSvcBean}.</p>
 *
 * <p>Tutti i metodi esportati dal bean di gestione dei tenant rimappano le
 * {@code RuntimeException} ricevute in
 * {@link it.doqui.index.ecmengine.exception.repository.TenantRuntimeException}.
 * </p>
 *
 * @author Doqui
 *
 * @see TenantAdminSvcBean
 * @see it.doqui.index.ecmengine.exception.repository.TenantRuntimeException
 */
public interface ImportSvc extends EJBLocalObject {

	public void importTenant(byte[] data,String tenant)throws ImportRuntimeException;

}
