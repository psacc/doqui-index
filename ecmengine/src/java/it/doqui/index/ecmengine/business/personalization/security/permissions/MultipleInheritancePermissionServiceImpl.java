package it.doqui.index.ecmengine.business.personalization.security.permissions;

import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionServiceImpl;
import org.alfresco.repo.security.permissions.impl.RequiredPermission;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MultipleInheritancePermissionServiceImpl extends PermissionServiceImpl {

	private static Log log = LogFactory.getLog(EcmEngineConstants.ECMENGINE_ROOT_LOG_CATEGORY + ".business.personalization.security");
    
    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
    {
        // If the node ref is null there is no sensible test to do - and there
        // must be no permissions
        // - so we allow it
        if (nodeRef == null)
        {
            return AccessStatus.ALLOWED;
        }

        nodeRef = tenantService.getName(nodeRef);

        // If the permission is null we deny
        if (perm == null)
        {
            return AccessStatus.DENIED;
        }

        // Allow permissions for nodes that do not exist
        if (!nodeService.exists(nodeRef))
        {
            return AccessStatus.ALLOWED;
        }

        // Get the current authentications
        // Use the smart authentication cache to improve permissions performance
        Authentication auth = authenticationComponent.getCurrentAuthentication();
        Set<String> authorisations = getAuthorisations(auth, nodeRef);

        Serializable key = generateKey(authorisations, nodeRef, perm, CacheType.HAS_PERMISSION);
        AccessStatus status = accessCache.get(key);
        if (status != null)
        {
            return status;
        }

        // If the node does not support the given permission there is no point
        // doing the test
        Set<PermissionReference> available = modelDAO.getAllPermissions(nodeRef);
        available.add(getAllPermissionReference());
        available.add(OLD_ALL_PERMISSIONS_REFERENCE);

        if (!(available.contains(perm)))
        {
            accessCache.put(key, AccessStatus.DENIED);
            return AccessStatus.DENIED;
        }

        if (authenticationComponent.getCurrentUserName().equals(authenticationComponent.getSystemUserName()))
        {
            return AccessStatus.ALLOWED;
        }

        //
        // TODO: Dynamic permissions via evaluators
        //

        /*
         * Does the current authentication have the supplied permission on the given node.
         */

        QName typeQname = nodeService.getType(nodeRef);
        Set<QName> aspectQNames = nodeService.getAspects(nodeRef);

        if (perm.equals(OLD_ALL_PERMISSIONS_REFERENCE))
        {
            perm = getAllPermissionReference();
        }
        MultipleInheritancePermissionServiceImpl.NodeTest nt = 
        	new MultipleInheritancePermissionServiceImpl.NodeTest(perm, typeQname, aspectQNames);
        boolean result = nt.evaluate(authorisations, nodeRef);
        if (log.isDebugEnabled())
        {
            log.debug("Permission <" + perm + "> is " 
            		+ (result ? "allowed" : "denied") 
            		+ " for " + authenticationComponent.getCurrentUserName() 
            		+ " on node " + nodeService.getPath(nodeRef));
        }

        status = result ? AccessStatus.ALLOWED : AccessStatus.DENIED;
        accessCache.put(key, status);
        return status;
    }
    
    private class NodeTest
    {
        /*
         * The required permission.
         */
        PermissionReference required;

        /*
         * Granters of the permission
         */
        Set<PermissionReference> granters;

        /*
         * The additional permissions required at the node level.
         */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /*
         * The additional permissions required on the parent.
         */
        Set<PermissionReference> parentRequirements = new HashSet<PermissionReference>();

        /*
         * The permissions required on all children.
         */
        Set<PermissionReference> childrenRequirements = new HashSet<PermissionReference>();

        /*
         * The type name of the node.
         */
        QName typeQName;

        /*
         * The aspects set on the node.
         */
        Set<QName> aspectQNames;

        /*
         * Constructor just gets the additional requirements
         */
        NodeTest(PermissionReference required, QName typeQName, Set<QName> aspectQNames)
        {
            this.required = required;
            this.typeQName = typeQName;
            this.aspectQNames = aspectQNames;

            // Set the required node permissions
            if (required.equals(getPermissionReference(ALL_PERMISSIONS)))
            {
                nodeRequirements = modelDAO.getRequiredPermissions(getPermissionReference(PermissionService.FULL_CONTROL), typeQName, aspectQNames, RequiredPermission.On.NODE);
            }
            else
            {
                nodeRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.NODE);
            }

            parentRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.PARENT);

            childrenRequirements = modelDAO.getRequiredPermissions(required, typeQName, aspectQNames, RequiredPermission.On.CHILDREN);

            // Find all the permissions that grant the allowed permission
            // All permissions are treated specially.
            granters = new LinkedHashSet<PermissionReference>(128, 1.0f);
            granters.addAll(modelDAO.getGrantingPermissions(required));
            granters.add(getAllPermissionReference());
            granters.add(OLD_ALL_PERMISSIONS_REFERENCE);
        }

        /**
         * External hook point
         * 
         * @param authorisations
         * @param nodeRef
         * @return
         */
        boolean evaluate(Set<String> authorisations, NodeRef nodeRef)
        {
        	log.debug("[NodeTest::evaluate] BEGIN");
        	
        	try {
        		Set<org.alfresco.util.Pair<String, PermissionReference>> denied = new HashSet<org.alfresco.util.Pair<String, PermissionReference>>();
        		return evaluate(authorisations, nodeRef, denied, null);
        	} finally {
        		log.debug("[NodeTest::evaluate] END");
        	}
        }

        /**
         * Internal hook point for recursion
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @param recursiveIn
         * @return
         */
        private boolean evaluate(Set<String> authorisations, NodeRef nodeRef, Set<org.alfresco.util.Pair<String, PermissionReference>> denied, MutableBoolean recursiveIn)
        {
        	log.debug("[NodeTest::evaluate] BEGIN");
        	
        	try {
        		log.debug("[NodeTest::evaluate] Controllo ACL sul nodo: " + nodeRef);
	            // Do we defer our required test to a parent (yes if not null)
	            MutableBoolean recursiveOut = null;
	
	            Set<org.alfresco.util.Pair<String, PermissionReference>> locallyDenied = new HashSet<org.alfresco.util.Pair<String, PermissionReference>>();
	            locallyDenied.addAll(denied);
	            locallyDenied.addAll(getDenied(nodeRef));
	
	            // Start out true and "and" all other results
	            boolean success = true;
	
	            // Check the required permissions but not for sets they rely on
	            // their underlying permissions
	            if (modelDAO.checkPermission(required)) {
	                if (parentRequirements.contains(required)) {
	                    if (checkGlobalPermissions(authorisations) 
	                    		|| checkRequired(authorisations, nodeRef, locallyDenied)) {
	                        // No need to do the recursive test as it has been found
	                        recursiveOut = null;
	                        if (recursiveIn != null) {
	                            recursiveIn.setValue(true);
	                        }
	                    } else {
	                        // Much cheaper to do this as we go then check all the
	                        // stack values for each parent
	                        recursiveOut = new MutableBoolean(false);
	                    }
	                } else {
	                    // We have to do the test as no parent will help us out
	                	success &= hasSinglePermission(authorisations, nodeRef);
	                }
	                
	                if (!success) {
	                    return false;
	                }
	            }
	
	            // Check the other permissions required on the node
	            for (PermissionReference pr : nodeRequirements)
	            {
	                // Build a new test
	                NodeTest nt = new NodeTest(pr, typeQName, aspectQNames);
	                success &= nt.evaluate(authorisations, nodeRef, locallyDenied, null);
	                if (!success) {
	                    return false;
	                }
	            }
	
	            // Check the permission required of the parent
	
	            if (success) {
	            	log.debug("[NodeTest::evaluate] Controllo ACL nodo: GRANTED - Inizio check ricorsivo");
	            	
//					ChildAssociationRef car = nodeService.getPrimaryParent(nodeRef);
	            	
                    NodePermissionEntry nodePermissions = permissionsDaoComponent.getPermissions(nodeRef);
                    if ((nodePermissions == null) || (nodePermissions.inheritPermissions())) {

                    	log.debug("[NodeTest::evaluate] nodePermissions = " + nodePermissions);
                    	log.debug("[NodeTest::evaluate] nodePermissions.inheritPermissions = " + nodePermissions.inheritPermissions());
	            	
                    	List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
	            	
                    	for (ChildAssociationRef car : parents) {	// Ciclo su tutti i padri (DoQui)
                    		if (car.getParentRef() != null) {
                    			log.debug("[NodeTest::evaluate] Controllo ACL sul nodo: " + car.getParentRef());
		                	
//		                    NodePermissionEntry nodePermissions = permissionsDaoComponent.getPermissions(car.getChildRef());
//		                    if ((nodePermissions == null) || (nodePermissions.inheritPermissions())) {
//		
//		                    	log.debug("[NodeTest::evaluate] nodePermissions = " + nodePermissions);
//		                    	log.debug("[NodeTest::evaluate] nodePermissions.inheritPermissions = " + nodePermissions.inheritPermissions());
		                    	
                    			locallyDenied.addAll(getDenied(car.getParentRef()));
		                        for (PermissionReference pr : parentRequirements) {
		                            if (pr.equals(required)) {
		                                // Recursive permission
		                                success &= this.evaluate(authorisations, car.getParentRef(), locallyDenied, recursiveOut);
		                                log.debug("[NodeTest::evaluate] Controllo ACL nodo: " + success + " [recursiveOut = " + recursiveOut.getValue() + "]");
		                                if ((recursiveOut != null) && recursiveOut.getValue()) {
		                                    if (recursiveIn != null) {
		                                        recursiveIn.setValue(true);
		                                    }
		                                }
		                            } else {
		                            	NodeTest nt = new NodeTest(pr, typeQName, aspectQNames);
		                                success &= nt.evaluate(authorisations, car.getParentRef(), locallyDenied, null);
		                                log.debug("[NodeTest::evaluate] Controllo ACL nodo: " + success);
		                            }        
		                            if (!success) {
		                            	log.debug("[NodeTest::evaluate] Controllo ACL nodo: DENIED - " +
		                            			"Break loop interno per permesso negato su " + car.getParentRef());
		                                break;
		                            }
		                        }
		                    }
                    		
    	                    if (success) {
    	                    	log.debug("[NodeTest::evaluate] Controllo ACL nodo: GRANTED - " +
    	                    			"Break loop esterno per permesso consentito su " + car.getParentRef());
    	                        break;
    	                    }
	                	}
	                }
	            	
	                if (!success) {
	                	log.debug("[NodeTest::evaluate] Controllo ACL nodo: DENIED");
	                    return false;
	                }
	            }
	
	            if ((recursiveOut != null) && (!recursiveOut.getValue())) {
	                // The required authentication was not resolved in recursion
	                return false;
	            }
	
	            // Check permissions required of children
	            if (!childrenRequirements.isEmpty()) {
	                List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(nodeRef);
	                for (PermissionReference pr : childrenRequirements) {
	                    for (ChildAssociationRef child : childAssocRefs) {
	                        success &= (hasPermission(child.getChildRef(), pr) == AccessStatus.ALLOWED);
	                        if (!success) {
	                            return false;
	                        }
	                    }
	                }
	            }
	
	            return success;
            
        	} finally {
        		log.debug("[NodeTest::evaluate] END");
        	}
        }

        public boolean hasSinglePermission(Set<String> authorisations, NodeRef nodeRef)
        {
        	nodeRef = tenantService.getName(nodeRef);
        	
            Serializable key = generateKey(authorisations, nodeRef, this.required, CacheType.SINGLE_PERMISSION_GLOBAL);

            AccessStatus status = accessCache.get(key);
            if (status != null)
            {
                return status == AccessStatus.ALLOWED;
            }

            // Check global permission

            if (checkGlobalPermissions(authorisations))
            {
                accessCache.put(key, AccessStatus.ALLOWED);
                return true;
            }

            Set<org.alfresco.util.Pair<String, PermissionReference>> denied = new HashSet<org.alfresco.util.Pair<String, PermissionReference>>();

            return hasSinglePermission(authorisations, nodeRef, denied);

        }

        public boolean hasSinglePermission(Set<String> authorisations, NodeRef nodeRef, Set<org.alfresco.util.Pair<String, PermissionReference>> denied)
        {
        	nodeRef = tenantService.getName(nodeRef);
        	
            // Add any denied permission to the denied list - these can not
            // then
            // be used to given authentication.
            // A -> B -> C
            // If B denies all permissions to any - allowing all permissions
            // to
            // andy at node A has no effect

            denied.addAll(getDenied(nodeRef));

            // Cache non denied
            Serializable key = null;
            if (denied.size() == 0)
            {
                key = generateKey(authorisations, nodeRef, this.required, CacheType.SINGLE_PERMISSION);
            }
            if (key != null)
            {
                AccessStatus status = accessCache.get(key);
                if (status != null)
                {
                    return status == AccessStatus.ALLOWED;
                }
            }

            // If the current node allows the permission we are done
            // The test includes any parent or ancestor requirements
            if (checkRequired(authorisations, nodeRef, denied))
            {
                if (key != null)
                {
                    accessCache.put(key, AccessStatus.ALLOWED);
                }
                return true;
            }

            // Se l'accesso fosse consentito dalle ACL del nodo a questo punto ci sarebbe gia` stato un "return true".
        	boolean allowed = false;
            
            NodePermissionEntry nodePermissions = permissionsDaoComponent.getPermissions(nodeRef);
            if ((nodePermissions == null) || (nodePermissions.inheritPermissions()))
            {
//                ChildAssociationRef car = nodeService.getPrimaryParent(nodeRef);
            	List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);

            	for (ChildAssociationRef car : parentAssocs) {
	                // Build the next element of the evaluation chain
	                if (car.getParentRef() != null)
	                {
	                	if (hasSinglePermission(authorisations, car.getParentRef(), denied))
	                    {
	                        allowed = true;
	                    } // ELSE - Il valore precedente deve rimanere invariato... basta che l'accesso sia consentito su un ramo
	                } // ELSE - Il valore precedente deve rimanere invariato... basta che l'accesso sia consentito su un ramo
            	}
            } // ELSE - Il valore precedente deve rimanere invariato... il nodo non eredita nulla dal padre
            
            // Il controllo e` completato... salvo il risultato in cache
        	if (key != null) 
        	{
        		accessCache.put(key, (allowed) ? AccessStatus.ALLOWED : AccessStatus.DENIED);
        	}
        	return allowed;
        }

        /**
         * Check if we have a global permission
         * 
         * @param authorisations
         * @return
         */
        private boolean checkGlobalPermissions(Set<String> authorisations)
        {
            for (PermissionEntry pe : modelDAO.getGlobalPermissionEntries())
            {
                if (isGranted(pe, authorisations, null))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Get the list of permissions denied for this node.
         * 
         * @param nodeRef
         * @return
         */
        private Set<org.alfresco.util.Pair<String, PermissionReference>> getDenied(NodeRef nodeRef)
        {
            Set<org.alfresco.util.Pair<String, PermissionReference>> deniedSet = new HashSet<org.alfresco.util.Pair<String, PermissionReference>>();

            // Loop over all denied permissions
            NodePermissionEntry nodeEntry = permissionsDaoComponent.getPermissions(nodeRef);
            if (nodeEntry != null)
            {
                for (PermissionEntry pe : nodeEntry.getPermissionEntries())
                {
                    if (pe.isDenied())
                    {
                        // All the sets that grant this permission must be
                        // denied
                        // Note that granters includes the orginal permission
                        Set<PermissionReference> granters = modelDAO.getGrantingPermissions(pe.getPermissionReference());
                        for (PermissionReference granter : granters)
                        {
                            deniedSet.add(new org.alfresco.util.Pair<String, PermissionReference>(pe.getAuthority(), granter));
                        }

                        // All the things granted by this permission must be
                        // denied
                        Set<PermissionReference> grantees = modelDAO.getGranteePermissions(pe.getPermissionReference());
                        for (PermissionReference grantee : grantees)
                        {
                            deniedSet.add(new org.alfresco.util.Pair<String, PermissionReference>(pe.getAuthority(), grantee));
                        }

                        // All permission excludes all permissions available for
                        // the node.
                        if (pe.getPermissionReference().equals(getAllPermissionReference()) || pe.getPermissionReference().equals(OLD_ALL_PERMISSIONS_REFERENCE))
                        {
                            for (PermissionReference deny : modelDAO.getAllPermissions(nodeRef))
                            {
                                deniedSet.add(new org.alfresco.util.Pair<String, PermissionReference>(pe.getAuthority(), deny));
                            }
                        }
                    }
                }
            }
            return deniedSet;
        }

        /**
         * Check that a given authentication is available on a node
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @return
         */
        private boolean checkRequired(Set<String> authorisations, NodeRef nodeRef, Set<org.alfresco.util.Pair<String, PermissionReference>> denied)
        {
            NodePermissionEntry nodeEntry = permissionsDaoComponent.getPermissions(nodeRef);

            // No permissions set - short cut to deny
            if (nodeEntry == null)
            {
                return false;
            }

            // Check if each permission allows - the first wins.
            // We could have other voting style mechanisms here
            for (PermissionEntry pe : nodeEntry.getPermissionEntries())
            {
                if (isGranted(pe, authorisations, denied))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is a permission granted
         * 
         * @param pe -
         *            the permissions entry to consider
         * @param granters -
         *            the set of granters
         * @param authorisations -
         *            the set of authorities
         * @param denied -
         *            the set of denied permissions/authority pais
         * @return
         */
        private boolean isGranted(PermissionEntry pe, Set<String> authorisations, 
        		Set<org.alfresco.util.Pair<String, PermissionReference>> denied)
        {
            // If the permission entry denies then we just deny
            if (pe.isDenied())
            {
                return false;
            }

            // The permission is allowed but we deny it as it is in the denied
            // set

            if (denied != null)
            {
            	org.alfresco.util.Pair<String, PermissionReference> specific = 
            			new org.alfresco.util.Pair<String, PermissionReference>(pe.getAuthority(), required);
                if (denied.contains(specific))
                {
                    return false;
                }
            }

            // any deny denies

            if (false)
            {
                if (denied != null)
                {
                    for (String auth : authorisations)
                    {
                    	org.alfresco.util.Pair<String, PermissionReference> specific = 
                        		new org.alfresco.util.Pair<String, PermissionReference>(auth, required);
                        if (denied.contains(specific))
                        {
                            return false;
                        }
                        for (PermissionReference perm : granters)
                        {
                            specific = new org.alfresco.util.Pair<String, PermissionReference>(auth, perm);
                            if (denied.contains(specific))
                            {
                                return false;
                            }
                        }
                    }
                }
            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (authorisations.contains(pe.getAuthority()) && granters.contains(pe.getPermissionReference()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }
    }
}
