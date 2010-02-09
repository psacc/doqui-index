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

package it.doqui.index.ecmengine.business.personalization.security.acl;

import it.doqui.index.ecmengine.business.personalization.security.ReadersService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;

import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.lucene.LuceneResultSet;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterException;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ACLEntryAfterInvocationProvider extends org.alfresco.repo.security.permissions.impl.acegi.ACLEntryAfterInvocationProvider
{
    private static Log log = LogFactory.getLog(AclCheckDao.ECMENGINE_PERSONALIZATION_ACL_CHECK);

    private static final String AFTER_ACL_NODE = "AFTER_ACL_NODE";

    private ReadersService readersService;
    private AclCheckDao aclCheckDao;
    private ModelDAO modelDao;

    public ResultSet decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
            ResultSet returnedObject) throws AccessDeniedException {
        if (returnedObject == null) {
            return null;
        }

        boolean isCheckResultSetACL = false;

        Iterator<?> iter = config.getConfigAttributes();

        while (iter.hasNext()) {
        	ConfigAttribute attr = (ConfigAttribute) iter.next();

        	if (attr.getAttribute().startsWith(AFTER_ACL_NODE)) {
        		isCheckResultSetACL = true;
        	}
        }

        if (returnedObject instanceof LuceneResultSet && isCheckResultSetACL) {
	        if (log.isDebugEnabled()) {
        	    log.debug("[ACLEntryAfterInvocationProvider::decide] Delegating...");
            }
        	return decideLucene(authentication, object, config, (LuceneResultSet) returnedObject);
        } else {
        	return super.decide(authentication, object, config, returnedObject);
        }
    }

    public ResultSet decideLucene(Authentication authentication, Object object, ConfigAttributeDefinition config,
            LuceneResultSet resultSet) throws AccessDeniedException {

	    if (log.isDebugEnabled()) {
           log.debug("[ACLEntryAfterInvocationProvider::decideLucene] BEGIN");
           log.debug("[ACLEntryAfterInvocationProvider::decideLucene] resultset.length: "+resultSet.length());
           for(int i=0;i<resultSet.length();i++){
               log.debug("[ACLEntryAfterInvocationProvider::decideLucene] resultset["+i+"]: "+resultSet.getDocument(i));
           }
    	}

		try {
			final long prepareStart = System.currentTimeMillis();
			HashMap<Long, Integer> toCheck = new HashMap<Long, Integer>();
			Set<String> authorities = readersService.getAuthoritiesForCurrentUser();

    	    if (log.isDebugEnabled()) {
	    		log.debug("[ACLEntryAfterInvocationProvider::decideLucene] Authorities: " + authorities);
    	    }

			FilteringResultSet filteringResultSet = new FilteringResultSet(resultSet);
			ResultSetMetaData rsInfo = resultSet.getResultSetMetaData();

			Integer maxSize = null;
			if (rsInfo.getSearchParameters().getLimitBy() == LimitBy.FINAL_SIZE) {
				maxSize = new Integer(rsInfo.getSearchParameters().getLimit());
			}

			if (authorities == null) {
				// L'utente ha privilegi amministrativi
				if (maxSize != null
						&& maxSize.intValue() > 0
						&& resultSet.length() > maxSize.intValue()) {

					// Filtriamo in base alla dimensione massima impostata
					for (int i = 0; i < resultSet.length(); i++) {
						filteringResultSet.setIncluded(i, (i < maxSize.intValue()));
					}

					filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(
							LimitBy.FINAL_SIZE,
							PermissionEvaluationMode.EAGER,
							rsInfo.getSearchParameters()));

					return filteringResultSet;
				} else {

					for (int i = 0; i < resultSet.length(); i++) {
						filteringResultSet.setIncluded(i, true);
					}

					// La lunghezza del RS originale non supera la dimensione massima impostata
					filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(
							LimitBy.UNLIMITED,
							PermissionEvaluationMode.EAGER,
							rsInfo.getSearchParameters()));

					return filteringResultSet; // L'utente ha i privilegi amministrativi o di sistema.
				}
			}
	        if (log.isDebugEnabled()) {
			    log.debug("[ACLEntryAfterInvocationProvider::decideLucene] Esecuzione codice per utente non admin.");
            }

			PermissionReference required = null;
			boolean configContainsSupportedDefinitions = false;

	        Iterator<?> iter = config.getConfigAttributes();

	        while (iter.hasNext()) {
	        	ConfigAttribute attr = (ConfigAttribute) iter.next();

	        	if (supports(attr)) {
	        		configContainsSupportedDefinitions = true;
	        	}

	        	if (attr.getAttribute().startsWith(AFTER_ACL_NODE)) {
	                StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
	                if (st.countTokens() != 3)
	                {
	                    throw new ACLEntryVoterException("There must be three . separated tokens in each config attribute");
	                }
	                @SuppressWarnings("unused")
					String typeString = st.nextToken();
	                String qNameString = st.nextToken();
	                String permissionString = st.nextToken();

	                required = new SimplePermissionReference(QName.createQName(qNameString, getNamespacePrefixResolver()),
	                		permissionString);
	        	}
	        }

			// Insieme dei permission group che includono il permesso di Read
	        if (log.isDebugEnabled()) {
	            log.debug("[ACLEntryAfterInvocationProvider::decideLucene] required permission: "+required);
            }
	        Set<PermissionReference> permissions = new HashSet<PermissionReference>(10);
			permissions.addAll(modelDao.getGrantingPermissions(required));

			permissions.add(modelDao.getPermissionReference(null, PermissionService.ALL_PERMISSIONS));

			for (int i = 0; i < resultSet.length(); i++) {
	            if (log.isDebugEnabled()) {
	    			log.debug("[ACLEntryAfterInvocationProvider::decideLucene] i: "+i);
    				log.debug("[ACLEntryAfterInvocationProvider::decideLucene] document[i]: "+resultSet.getDocument(i));
                }

				String id = resultSet.getDocument(i).get("DBID");

	            if (log.isDebugEnabled()) {
				    log.debug("[ACLEntryAfterInvocationProvider::decideLucene] id: "+id);
                }

				toCheck.put(new Long(id), new Integer(i));
			}

			final List<Long> toCheckList = new ArrayList<Long>(toCheck.keySet());

	        if (log.isDebugEnabled()) {
			    final long prepareStop = System.currentTimeMillis();
    			log.debug("[ACLEntryAfterInvocationProvider::decideLucene] Prepare duration: " + (prepareStop - prepareStart) + " ms [toCheck: " + toCheck.size() + "]");
            }

			final long start = System.currentTimeMillis();
			List<Long> readables = aclCheckDao.checkHasPermissionsOnNodes(toCheckList, authorities, permissions);

	        if (log.isDebugEnabled()) {
	    		final long stop = System.currentTimeMillis();
		    	log.debug("[ACLEntryAfterInvocationProvider::decideLucene] Check duration: " + (stop - start) + " ms [readables: " + readables.size() + "]");
			}

			final long filteringStart = System.currentTimeMillis();

			for (Long readable : readables) {
				filteringResultSet.setIncluded(
						toCheck.get(readable).intValue(), true);
			}

			if (!configContainsSupportedDefinitions) {
				if (maxSize == null) {
					return resultSet;
				} else if (resultSet.length() > maxSize.intValue()) {
					for (int i = 0; i < maxSize.intValue(); i++) {
						filteringResultSet.setIncluded(i, true);
					}
					filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(
									LimitBy.FINAL_SIZE,
									PermissionEvaluationMode.EAGER,
									rsInfo.getSearchParameters()));
				} else {
					for (int i = 0; i < resultSet.length(); i++) {
						filteringResultSet.setIncluded(i, true);
					}
					filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(
									LimitBy.UNLIMITED,
									PermissionEvaluationMode.EAGER,
									rsInfo.getSearchParameters()));
				}
			} else {
				if (maxSize != null) {
        	        if (log.isDebugEnabled()) {
					    log.debug("[ACLEntryAfterInvocationProvider::decideLucene] Max RS size: " + maxSize.toString());
                    }

					int counter = 0;
					int included = 0;

					maxSize = (maxSize <= resultSet.length()) ? maxSize : resultSet.length();

					while (counter < maxSize) {
						if (filteringResultSet.getIncluded(counter)) {
							included++;
						}
						counter++;
					}
					while (counter < resultSet.length()) {
						filteringResultSet.setIncluded(counter, false);
						counter++;
					}
					filteringResultSet.setResultSetMetaData(
							new SimpleResultSetMetaData(LimitBy.FINAL_SIZE,
									PermissionEvaluationMode.EAGER, rsInfo.getSearchParameters()));
				}
			}
			// set the default, unlimited result set type
			filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(
							LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER,
							rsInfo.getSearchParameters()));

   	        if (log.isDebugEnabled()) {
    			final long filteringStop = System.currentTimeMillis();
	    		log.debug("[ACLEntryAfterInvocationProvider::decideLucene] Filtering duration: " + (filteringStop - filteringStart) + " ms");
		    }

			return filteringResultSet;
		} finally {
			log.debug("[ACLEntryAfterInvocationProvider::decideLucene] END");
		}
    }

	public void setReadersService(ReadersService readersService) {
		this.readersService = readersService;
	}

	public void setAclCheckDao(AclCheckDao aclCheckDao) {
		this.aclCheckDao = aclCheckDao;
	}

	public void setModelDao(ModelDAO modelDao) {
		this.modelDao = modelDao;
	}
}
