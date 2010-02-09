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

package it.doqui.index.ecmengine.business.job.tenant;

/**
 * Interface che raggruppa le costanti utilizzate per la gestione del job di
 * creazione dei tenant.
 *
 * @author DoQui
 * @see it.doqui.index.ecmengine.business.job.dto.BatchJob
 * @see it.doqui.index.ecmengine.business.job.dto.BatchJobParam
 *
 */
public interface TenantAdminJobConstants {

	/**
	 * Identificatore del parametro 'domain'.
	 */
	String PARAM_DOMAIN = "domain";

	/**
	 * Identificatore del parametro 'admin-password'.
	 */
	String PARAM_ADMIN_PASSWORD = "admin-password";

	/**
	 * Identificatore del parametro 'content-root-location'.
	 */
	String PARAM_CONTENT_ROOT_LOCATION = "content-root-location";

	/**
	 * Identificatore del parametro 'content-store-type'.
	 */
	String PARAM_CONTENT_STORE_TYPE	    = "content-store-type";

	/**
	 * Identificatore del parametro 'content-store-protocol'.
	 */
	String PARAM_CONTENT_STORE_PROTOCOL = "content-store-protocol";

	/**
	 * Identificatore del parametro 'content-store-resource'.
	 */
	String PARAM_CONTENT_STORE_RESOURCE = "content-store-resource";

}
