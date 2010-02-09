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

import org.alfresco.repo.tenant.Tenant;

public class MultiTServiceImpl extends org.alfresco.repo.tenant.MultiTServiceImpl implements TenantService {

	@Override
    protected void register(org.alfresco.repo.tenant.MultiTAdminServiceImpl tenantAdminService) {
        super.register(tenantAdminService);
    }

	@Override
    protected void putTenant(String tenantDomain, Tenant tenant) {
		super.putTenant(tenantDomain, tenant);
    }

	@Override
    protected String getName(String name, String tenantDomain) {
    	return super.getName(name, tenantDomain);
    }

    public it.doqui.index.ecmengine.business.personalization.multirepository.Tenant getTenant(String tenantDomain){
    	Tenant t = super.getTenant(tenantDomain);
        it.doqui.index.ecmengine.business.personalization.multirepository.Tenant tRet = null;
        if( t!=null ) {
        	if (t instanceof it.doqui.index.ecmengine.business.personalization.multirepository.Tenant) {
				tRet = (it.doqui.index.ecmengine.business.personalization.multirepository.Tenant) t;
			} else { // MB: potrebbe anche non servire, dato che il getTenant del MultiRServiceImp torna sempre un tenant Doqui
				tRet = new it.doqui.index.ecmengine.business.personalization.multirepository.Tenant( t.getTenantDomain(), t.isEnabled(), t.getRootContentStoreDir() );
			}
        }
        return tRet;
    }

}
