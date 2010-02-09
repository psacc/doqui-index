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

package it.doqui.index.ecmengine.business.personalization.multirepository.bootstrap;

import it.doqui.index.ecmengine.business.personalization.multirepository.ContentStoreDefinition;
import it.doqui.index.ecmengine.business.personalization.multirepository.Tenant;

import org.alfresco.repo.tenant.TenantAdminService;

import java.util.List;

/**
 * MultiT Tenant Admin Service interface.
 * <p>
 * This interface provides administrative methods to provision and administer tenants.
 *
 */
public interface MultiTTenantAdminService extends TenantAdminService
{
    public void createTenant(String tenantDomain, char[] adminRawPassword, String rootContentStoreDir, List<ContentStoreDefinition> stores);
    public Tenant getTenant(String tenantDomain);
    public List<Tenant> getAllTenantsDoqui();
}
