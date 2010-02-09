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

package it.doqui.index.ecmengine.client.engine;

import it.doqui.index.ecmengine.business.publishing.management.EcmEngineManagementBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.massive.EcmEngineMassiveBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.search.EcmEngineSearchBusinessInterface;
import it.doqui.index.ecmengine.business.publishing.security.EcmEngineSecurityBusinessInterface;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.Path;
import it.doqui.index.ecmengine.dto.engine.NodeArchiveParams;
import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.dto.engine.management.Association;
import it.doqui.index.ecmengine.dto.engine.management.Category;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatInfo;
import it.doqui.index.ecmengine.dto.engine.management.FileFormatVersion;
import it.doqui.index.ecmengine.dto.engine.management.FileInfo;
import it.doqui.index.ecmengine.dto.engine.management.Rendition;
import it.doqui.index.ecmengine.dto.engine.management.RenditionDocument;
import it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer;
import it.doqui.index.ecmengine.dto.engine.management.Rule;
import it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.dto.engine.management.Version;
import it.doqui.index.ecmengine.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultContentData;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.dto.engine.search.TopCategory;
import it.doqui.index.ecmengine.dto.engine.security.Document;
import it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent;
import it.doqui.index.ecmengine.dto.engine.security.VerifyReport;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CheckInCheckOutException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.CopyException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.MoveException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.RenditionException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.TransformException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UnsupportedTransformationException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.WorkflowException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.exception.publishing.engine.search.TooManyResultsException;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;

public abstract class AbstractEcmEngineDelegateImpl implements EcmEngineDelegate {

	protected Log log = null;

    private EcmEngineManagementBusinessInterface ecmEngineManagementInterface = null;
    private EcmEngineSearchBusinessInterface     ecmEngineSearchInterface     = null;
    private EcmEngineSecurityBusinessInterface   ecmEngineSecurityInterface   = null;
    private EcmEngineMassiveBusinessInterface    ecmEngineMassiveInterface    = null;

    protected AbstractEcmEngineDelegateImpl(Log inLog) {
		inLog.debug("["+getClass().getSimpleName()+"::constructor] BEGIN");
		this.log = inLog;
		try {
        	initializeManagement();
        	initializeSearch();
        	initializeSecurity();
        	initializeMassive();
		} catch (Throwable ex) {
			log.error("["+getClass().getSimpleName()+"::constructor] eccezione", ex);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::constructor] END");
		}
    }

    protected abstract EcmEngineManagementBusinessInterface createManagementService() throws Throwable;
    protected abstract EcmEngineSearchBusinessInterface     createSearchService() throws Throwable;
    protected abstract EcmEngineSecurityBusinessInterface   createSecurityService() throws Throwable;
    protected abstract EcmEngineMassiveBusinessInterface    createMassiveService() throws Throwable;

    private void initializeManagement() throws Throwable {
		log.debug("["+getClass().getSimpleName()+"::initializeManagement] BEGIN");
		try {
			this.ecmEngineManagementInterface = createManagementService();
		} catch(Throwable t) {
			log.error("["+getClass().getSimpleName()+"::initializeManagement] eccezione", t);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::initializeManagement] END");
		}
	}

    private void initializeSearch() throws Throwable {
		log.debug("["+getClass().getSimpleName()+"::initializeSearch] BEGIN");
		try {
			this.ecmEngineSearchInterface = createSearchService();
		} catch(Throwable t) {
			log.error("["+getClass().getSimpleName()+"::initializeSearch] eccezione", t);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::initializeSearch] END");
		}
	}

    private void initializeSecurity() throws Throwable {
		log.debug("["+getClass().getSimpleName()+"::initializeSecurity] BEGIN");
		try {
			this.ecmEngineSecurityInterface = createSecurityService();
		//} catch(javax.naming.NameNotFoundException nnfe) {
			//log.error("["+getClass().getSimpleName()+"::initializeSecurity] eccezione NameNotFoundException su SecurityBean");
		} catch(Throwable t) {
			log.error("["+getClass().getSimpleName()+"::initializeSecurity] eccezione", t);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::initializeSecurity] END");
		}
	}

    private void initializeMassive() throws Throwable {
		log.debug("["+getClass().getSimpleName()+"::initializeMassive] BEGIN");
		try {
			this.ecmEngineMassiveInterface = createMassiveService();
		} catch(Throwable t) {
			log.error("["+getClass().getSimpleName()+"::initializeMassive] eccezione", t);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::initializeMassive] END");
		}
	}

    /*
     * SERVIZI DI GESTIONE CONTENUTI
     */

	public Node checkOutContent(Node node, OperationContext context)
		throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, PermissionDeniedException,
		EcmEngineTransactionException, RemoteException, InvalidCredentialsException {

		this.log.debug("["+getClass().getSimpleName()+"::checkOutContent] BEGIN");
		Node outNodo = null;
		try {
			outNodo = this.ecmEngineManagementInterface.checkOutContent(node, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::checkOutContent] END");
		}
    	return outNodo;
	}

	public Node checkInContent(Node workingCopy, OperationContext context)
		throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, PermissionDeniedException,
		EcmEngineTransactionException, RemoteException, InvalidCredentialsException {

		this.log.debug("["+getClass().getSimpleName()+"::checkInContent] BEGIN");
		Node outNodo = null;
		try {
			outNodo = this.ecmEngineManagementInterface.checkInContent(workingCopy, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::checkInContent] END");
		}

    	return outNodo;
	}

    /*
     * SERVIZI DI RICERCA CONTENUTI
     */

	public ResultAssociation[] getAssociations(Node node,String assocType, int maxResults, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::getAssociations] BEGIN");
		ResultAssociation [] results = null;
    	try {
    		results = this.ecmEngineSearchInterface.getAssociations(node, assocType, maxResults, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::getAssociations] END");
    	}
    	return results;
	}

	public String nodeExists(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, NoDataExtractedException, RemoteException, InvalidCredentialsException,
	EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::nodeExists] BEGIN");
    	String result = null;
    	try {
    		result = this.ecmEngineSearchInterface.nodeExists(xpath, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::nodeExists] END");
    	}
    	return result;
	}

	public SearchResponse luceneSearch(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::luceneSearch] BEGIN");
		SearchResponse response = null;
		try {
			response = this.ecmEngineSearchInterface.luceneSearch(lucene, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::luceneSearch] END");
		}
		return response;
	}

	public SearchResponse xpathSearch(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::xpathSearch] BEGIN");
		SearchResponse response = null;
    	try {
    		response = this.ecmEngineSearchInterface.xpathSearch(xpath, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::xpathSearch] END");
    	}
    	return response;
	}

	public SearchResponse genericGlobalSearch(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::genericGlobalSearch] BEGIN");
		SearchResponse response = null;

    	try {
    		response = this.ecmEngineSearchInterface.genericGlobalSearch(params, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::genericGlobalSearch] END");
    	}
		return response;
	}

	/*
	 * SERVIZI DI CONSULTAZIONE AUDIT
	 */

	public OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca,
			OperationContext context) throws InvalidParameterException,
			InvalidCredentialsException, PermissionDeniedException,
			SearchException, NoDataExtractedException,
			EcmEngineTransactionException, RemoteException {
		final String baseLog = "["+getClass().getSimpleName()+"::ricercaAudit]";
		this.log.debug(baseLog + " BEGIN");
		OperazioneAudit[] listaAudit = null;
		try {
			listaAudit = this.ecmEngineSearchInterface.ricercaAudit(parametriRicerca, context);
    	} finally {
    		this.log.debug(baseLog +" END");
		}

    	return listaAudit;
	}

	/* Servizi Audit Trail */

	public void logTrail(AuditInfo auditTrail, OperationContext context)
	throws InvalidParameterException, AuditTrailException, EcmEngineTransactionException, RemoteException, InvalidCredentialsException {
		this.log.debug("["+getClass().getSimpleName()+"::logTrail] BEGIN");
		try {
			ecmEngineManagementInterface.logTrail(auditTrail, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::logTrail] END");
		}
	}

	public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca, OperationContext context)
	throws InvalidParameterException, AuditTrailException, RemoteException, PermissionDeniedException, InvalidCredentialsException, EcmEngineTransactionException {
		AuditInfo[] result = null;
		this.log.debug("["+getClass().getSimpleName()+"::ricercaAuditTrail] BEGIN");
		try {
			result = ecmEngineManagementInterface.ricercaAuditTrail(parametriRicerca, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::ricercaAuditTrail] END");
		}
		return result;
	}

	/* NUOVI SERVIZI */

	public Node createContent(Node parent, Content content, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
			EcmEngineTransactionException, PermissionDeniedException {
		Node result = null;

		this.log.debug("["+getClass().getSimpleName()+"::createContent] BEGIN");
		try {
			result = ecmEngineManagementInterface.createContent(parent, content, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::createContent] END");
		}

		return result;
	}

    public void deleteContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, DeleteException ,InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, RemoteException {

    	log.debug("["+getClass().getSimpleName()+"::deleteContent] BEGIN");
		try {
			ecmEngineManagementInterface.deleteContent(node, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::deleteContent] END");
		}
    }

	public byte [] retrieveContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		byte [] data = null;

		this.log.debug("["+getClass().getSimpleName()+"::retrieveContentData] BEGIN");
		try {
			data = ecmEngineManagementInterface.retrieveContentData(node, content, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::retrieveContentData] END");
		}
		return data;
	}

	public void linkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		this.log.debug("["+getClass().getSimpleName()+"::linkContent] BEGIN");
		try {
			ecmEngineManagementInterface.linkContent(source, destination, association, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::linkContent] END");
		}
	}

	public void unLinkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		this.log.debug("["+getClass().getSimpleName()+"::unLinkContent] BEGIN");
		try {
			ecmEngineManagementInterface.unLinkContent(source, destination, association, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::unLinkContent] END");
		}
	}

	public void updateMetadata(Node node, Content newContent, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException {
		this.log.debug("["+getClass().getSimpleName()+"::updateMetadata] BEGIN");
		try {
			ecmEngineManagementInterface.updateMetadata(node, newContent, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::updateMetadata] END");
		}
	}

	public void moveAggregation(Node source, Node destinationParent,OperationContext context)
	throws InvalidParameterException, MoveException, NoSuchNodeException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::moveAggregation] BEGIN");
		try {
			ecmEngineManagementInterface.moveAggregation(source,destinationParent,context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::moveAggregation] END");
		}
	}

	public Version[] getAllVersions(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, RemoteException,
	PermissionDeniedException, EcmEngineTransactionException {
		Version[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getAllVersions] BEGIN");
		try {
			result = ecmEngineManagementInterface.getAllVersions(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getAllVersions] END");
		}

		return result;
	}

	public Version getVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, EcmEngineException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		Version result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getVersion] BEGIN");
		try {
			result = ecmEngineManagementInterface.getVersion(node, versionLabel, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getVersion] END");
		}

		return result;
	}

	public byte[] retrieveVersionContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {
		byte [] data = null;

		this.log.debug("["+getClass().getSimpleName()+"::retrieveVersionContentData] BEGIN");
		try {
			data = ecmEngineManagementInterface.retrieveVersionContentData(node, content, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::retrieveVersionContentData] END");
		}
		return data;
	}

	public void revertVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, EcmEngineException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		this.log.debug("["+getClass().getSimpleName()+"::revertVersion] BEGIN");
		try {
			ecmEngineManagementInterface.revertVersion(node, versionLabel, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::revertVersion] END");
		}
	}

	public void updateContentData(Node node, Content content, OperationContext context) throws InvalidParameterException, UpdateException,
	EcmEngineTransactionException, NoSuchNodeException, RemoteException, InvalidCredentialsException, PermissionDeniedException {

		this.log.debug("["+getClass().getSimpleName()+"::updateContentData] BEGIN");
		try {
			ecmEngineManagementInterface.updateContentData(node, content, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::updateContentData] END");
		}
	}

	public Node cancelCheckOutContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, RemoteException, PermissionDeniedException,
	InvalidCredentialsException, EcmEngineTransactionException {

		this.log.debug("["+getClass().getSimpleName()+"::cancelCheckOutContent] BEGIN");
		Node outNodo = null;
		try {
			outNodo = this.ecmEngineManagementInterface.cancelCheckOutContent(node, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::cancelCheckOutContent] END");
		}

    	return outNodo;
	}

	public Node getWorkingCopy(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, EcmEngineException, RemoteException,
	InvalidCredentialsException, EcmEngineTransactionException {

		this.log.debug("["+getClass().getSimpleName()+"::getWorkingCopy] BEGIN");
		Node workingCopy = null;
		try {
			workingCopy = this.ecmEngineManagementInterface.getWorkingCopy(node, context);
    	} finally {
    		this.log.debug("["+getClass().getSimpleName()+"::getWorkingCopy] END");
		}

    	return workingCopy;
	}

	public Node getUid(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, SearchException, InvalidCredentialsException,
	PermissionDeniedException, RemoteException, EcmEngineTransactionException {

		log.debug("["+getClass().getSimpleName()+"::getUid] BEGIN");

		try {
			return this.ecmEngineSearchInterface.getUid(xpath, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::getUid] END");
		}
	}

	public int getTotalResults(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException,
	InvalidCredentialsException, RemoteException, EcmEngineTransactionException{

		log.debug("["+getClass().getSimpleName()+"::getTotalResults] BEGIN");

		try {
			return this.ecmEngineSearchInterface.getTotalResults(xpath, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::getTotalResults] END");
		}
	}

	public int getTotalResultsLucene(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, SearchException,
	InvalidCredentialsException, RemoteException, EcmEngineTransactionException{
		log.debug("["+getClass().getSimpleName()+"::getTotalResultsLucene] BEGIN");

		try {
			return this.ecmEngineSearchInterface.getTotalResultsLucene(lucene, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::getTotalResultsLucene] END");
		}
	}

	public EncryptionInfo checkEncryption(Node node, OperationContext context)
    throws InvalidParameterException, EcmEngineException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, RemoteException, EcmEngineTransactionException {
		log.debug("["+getClass().getSimpleName()+"::checkEncryption] BEGIN");

		try {
			return this.ecmEngineManagementInterface.checkEncryption(node, context);
		} finally {
			log.debug("["+getClass().getSimpleName()+"::checkEncryption] END");
		}
	}

    public boolean testResources() throws EcmEngineException, RemoteException {
    	this.log.debug("["+getClass().getSimpleName()+"::testResources] BEGIN");

        try {
            return this.ecmEngineManagementInterface.testResources();
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::testResources] END");
        }
     }

	public void addSimpleWorkflowRule(Node node, SimpleWorkflow workflow, Rule rule, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException, PermissionDeniedException,
	InvalidCredentialsException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::addSimpleWorkflowRule] BEGIN");
		try {
			ecmEngineManagementInterface.addSimpleWorkflowRule(node, workflow, rule, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::addSimpleWorkflowRule] END");
		}
	}

	public void approveContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException, PermissionDeniedException,
	InvalidCredentialsException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::approveContent] BEGIN");
		try {
			ecmEngineManagementInterface.approveContent(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::approveContent] END");
		}
	}

	public ResultContent getContentMetadata(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, RemoteException,
	InvalidCredentialsException, EcmEngineTransactionException, PermissionDeniedException {
    	this.log.debug("["+getClass().getSimpleName()+"::getContentMetadata] BEGIN");
        try {
            return this.ecmEngineManagementInterface.getContentMetadata(node, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getContentMetadata] END");
        }
	}

	public ResultContent getVersionMetadata(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, RemoteException,
	InvalidCredentialsException, EcmEngineTransactionException, PermissionDeniedException {
    	this.log.debug("["+getClass().getSimpleName()+"::getVersionMetadata] BEGIN");
        try {
            return this.ecmEngineManagementInterface.getVersionMetadata(node, context);
        } finally {
        	this.log.debug("["+getClass().getSimpleName()+"::getVersionMetadata] END");
        }
	}

	public void rejectContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException, PermissionDeniedException,
	InvalidCredentialsException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::rejectContent] BEGIN");
		try {
			ecmEngineManagementInterface.rejectContent(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::rejectContent] END");
		}
	}

	public void startSimpleWorkflow(Node node, SimpleWorkflow workflow, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, WorkflowException, RemoteException, PermissionDeniedException,
	InvalidCredentialsException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::startSimpleWorkflow] BEGIN");
		try {
			ecmEngineManagementInterface.startSimpleWorkflow(node, workflow, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::startSimpleWorkflow] END");
		}
	}

	public byte[] transformContent(Node node, String mimeType, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, UnsupportedTransformationException, RemoteException,
	PermissionDeniedException, TransformException, InvalidCredentialsException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::transformContent] BEGIN");
		try {
			return ecmEngineManagementInterface.transformContent(node, mimeType, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::transformContent] END");
		}
	}

	public Path[] getPaths(Node node, OperationContext context) throws InvalidParameterException, NoSuchNodeException,
	PermissionDeniedException, SearchException, RemoteException, InvalidCredentialsException {
		this.log.debug("["+getClass().getSimpleName()+"::getPaths] BEGIN");

		try {
			return ecmEngineSearchInterface.getPaths(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getPaths] END");
		}
	}

	public SearchResponse listDeletedNodes(NodeArchiveParams params, OperationContext context) throws InvalidParameterException,
	InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException, RemoteException {
		this.log.debug("["+getClass().getSimpleName()+"::listDeletedNodes] BEGIN");

		try {
			return ecmEngineSearchInterface.listDeletedNodes(params, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::listDeletedNodes] END");
		}
	}

	public void purgeAllContents(OperationContext context)
	throws InvalidParameterException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::purgeAllContents] BEGIN");

		try {
			ecmEngineManagementInterface.purgeAllContents(context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::purgeAllContents] END");
		}
	}

	public void purgeContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException,
	PermissionDeniedException, RemoteException, EcmEngineTransactionException {
		this.log.debug("["+getClass().getSimpleName()+"::purgeContent] BEGIN");

		try {
			ecmEngineManagementInterface.purgeContent(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::purgeContent] END");
		}
	}

	public Node[] restoreAllContents(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, PermissionDeniedException,
	RemoteException, EcmEngineTransactionException, EcmEngineException {
		this.log.debug("["+getClass().getSimpleName()+"::restoreAllContents] BEGIN");

		try {
			return ecmEngineManagementInterface.restoreAllContents(context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::restoreAllContents] END");
		}
	}

	public Node restoreContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException,
	PermissionDeniedException, RemoteException, EcmEngineTransactionException, EcmEngineException {
		this.log.debug("["+getClass().getSimpleName()+"::restoreContent] BEGIN");

		try {
			return ecmEngineManagementInterface.restoreContent(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::restoreContent] END");
		}
	}

	public NodeResponse luceneSearchNoMetadata(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("[" + getClass().getSimpleName() + "::luceneSearchNoMetadata] BEGIN");
		NodeResponse response = null;
		try {
			response = this.ecmEngineSearchInterface.luceneSearchNoMetadata(lucene, context);
		} finally {
			this.log.debug("[" + getClass().getSimpleName() + "::luceneSearchNoMetadata] END");
		}
		return response;
	}

	public NodeResponse xpathSearchNoMetadata(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("[" + getClass().getSimpleName() + "::xpathSearchNoMetadata] BEGIN");
		NodeResponse response = null;
    	try {
    		response = this.ecmEngineSearchInterface.xpathSearchNoMetadata(xpath, context);
    	} finally {
    		this.log.debug("[" + getClass().getSimpleName() + "::xpathSearchNoMetadata] END");
    	}
    	return response;
	}

	public NodeResponse genericGlobalSearchNoMetadata(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("[" + getClass().getSimpleName() + "::genericGlobalSearchNoMetadata] BEGIN");
		NodeResponse response = null;
    	try {
    		response = this.ecmEngineSearchInterface.genericGlobalSearchNoMetadata(params, context);
    	} finally {
    		this.log.debug("[" + getClass().getSimpleName() + "::genericGlobalSearchNoMetadata] END");
    	}
		return response;
	}

	public NodeResponse listDeletedNodesNoMetadata(NodeArchiveParams params, OperationContext context) throws InvalidParameterException,
	InvalidCredentialsException, NoSuchNodeException,PermissionDeniedException, SearchException, RemoteException {
		this.log.debug("[" + getClass().getSimpleName() + "::listDeletedNodesNoMetadata] BEGIN");

		try {
			return ecmEngineSearchInterface.listDeletedNodesNoMetadata(params, context);
		} finally {
			this.log.debug("[" + getClass().getSimpleName() + "::listDeletedNodesNoMetadata] END");
		}
	}

	public NodeResponse selectNodes(Node node, SearchParamsAggregate parameterAggregate, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, TooManyResultsException, SearchException, RemoteException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException {
		this.log.debug("[" + getClass().getSimpleName() + "::selectNodes] BEGIN");

		try {
			return ecmEngineSearchInterface.selectNodes(node, parameterAggregate, context);
		} finally {
			this.log.debug("[" + getClass().getSimpleName() + "::selectNodes] END");
		}
	}

 	public Mimetype[] getMimetype(Mimetype mimetype)
	throws InvalidParameterException, RemoteException {
		this.log.debug("["+getClass().getSimpleName()+"::getMimetype] BEGIN");
		try {
			return ecmEngineManagementInterface.getMimetype(mimetype);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getMimetype] END");
		}
	}

	public FileFormatInfo[] getFileFormatInfo(Node node, Content content, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException,
	RemoteException, EcmEngineException {
		this.log.debug("["+getClass().getSimpleName()+"::getFileFormatInfo] BEGIN");
		FileFormatInfo[] fileFormatInfo = null;
		try {
			fileFormatInfo = ecmEngineManagementInterface.getFileFormatInfo(node, content, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getFileFormatInfo] END");
		}
		return fileFormatInfo;
	}

	public FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException,
	RemoteException, EcmEngineException {
		this.log.debug("["+getClass().getSimpleName()+"::getFileFormatInfo] BEGIN");
		FileFormatInfo[] fileFormatInfo = null;
		try {
			fileFormatInfo = ecmEngineManagementInterface.getFileFormatInfo(fileInfo, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getFileFormatInfo] END");
		}
		return fileFormatInfo;
	}

	public FileFormatVersion getFileFormatVersion(OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException,
	RemoteException, EcmEngineException {
		this.log.debug("["+getClass().getSimpleName()+"::getFileFormatInfo] BEGIN");
		FileFormatVersion fileFormatVersion = null;
		try {
			fileFormatVersion = ecmEngineManagementInterface.getFileFormatVersion(context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getFileFormatVersion] END");
		}
		return fileFormatVersion;
	}

	public VerifyReport verifyDocument(EnvelopedContent envelopedContent, OperationContext context) throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::verifyDocument] BEGIN");
		VerifyReport result=null;
		try {
			result = ecmEngineSecurityInterface.verifyDocument(envelopedContent, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::verifyDocument] END");
		}
		return result;
	}

	public VerifyReport verifyDocument(Node node, OperationContext context) throws InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::verifyDocument] BEGIN");
		VerifyReport result=null;
		try {
			result = ecmEngineSecurityInterface.verifyDocument(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::verifyDocument] END");
		}
		return result;
	}

	public Document extractDocumentFromEnvelope(EnvelopedContent envelopedContent, OperationContext context)throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::extractDocumentFromEnvelope] BEGIN");
		Document result=null;
		try {
			result = ecmEngineSecurityInterface.extractDocumentFromEnvelope(envelopedContent, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::extractDocumentFromEnvelope] END");
		}
		return result;
	}

	public Document extractDocumentFromEnvelope(Node node, OperationContext context) throws InsertException,InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::extractDocumentFromEnvelope] BEGIN");
		Document result=null;
		try {
			result = ecmEngineSecurityInterface.extractDocumentFromEnvelope(node, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::extractDocumentFromEnvelope] END");
		}
		return result;
	}

	public Node createContentFromTemporaney(Node parentNode, Content content,OperationContext context, Node tempNode)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::createContentFromTemporaney] BEGIN");
		Node result=null;
		try {
			result = ecmEngineSecurityInterface.createContentFromTemporaney(parentNode, content, context, tempNode);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::createContentFromTemporaney] END");
		}
		return result;
	}

	public Node copyNode(Node source, Node parent, OperationContext context) throws InvalidParameterException, InsertException, CopyException,NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException,RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::copyNode] BEGIN");
		Node result=null;
		try {
			result = ecmEngineManagementInterface.copyNode(source, parent, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::copyNode] END");
		}
		return result;
	}

	public void moveNode(Node source, Node parent, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException,RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::moveNode] BEGIN");
		try {
			ecmEngineManagementInterface.moveNode(source, parent, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::moveNode] END");
		}
	}

	public Node createCategory(Node categoryParent, Category category, OperationContext context) throws InvalidParameterException,
    InsertException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
    EcmEngineTransactionException {

		Node result = null;

		this.log.debug("["+getClass().getSimpleName()+"::createCategory] BEGIN");
		try {
			result = ecmEngineManagementInterface.createCategory(categoryParent, category, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::createCategory] END");
		}
		return result;
	}


	public Node createRootCategory(Category rootCategory, OperationContext context) throws InvalidParameterException,
    InsertException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
    EcmEngineTransactionException {

		Node result = null;

		this.log.debug("["+getClass().getSimpleName()+"::createRootCategory] BEGIN");
		try {
			result = ecmEngineManagementInterface.createRootCategory(rootCategory, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::createRootCategory] END");
		}
		return result;
	}

	public void deleteCategory(Node categoryNode, OperationContext context) throws InvalidParameterException,
    DeleteException,RemoteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
    EcmEngineTransactionException {

		this.log.debug("["+getClass().getSimpleName()+"::deleteCategory] BEGIN");
		try {
			  ecmEngineManagementInterface.deleteCategory(categoryNode, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::deleteCategory] END");
		}
	}

	public ResultAssociation[] getCategories(Category category, String depth, OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException
	{

		ResultAssociation[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getCategories] BEGIN");
		try {

			result = ecmEngineSearchInterface.getCategories(category, depth, context);

		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getCategories] END");
		}
		return result;

	}

	public ResultAssociation[] getCategoryChildren(Node categoryNode, String mode, String depth, OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException
	{
		ResultAssociation[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getCategoryChildren] BEGIN");
		try {

			result = ecmEngineSearchInterface.getCategoryChildren(categoryNode, mode, depth, context);

		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getCategoryChildren] END");
		}
		return result;
	}

	public ResultAspect[] getClassificationAspects(OperationContext context) throws InvalidParameterException,
	SearchException, RemoteException, InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException
	{
		ResultAspect[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getClassificationAspects] BEGIN");
		try {

			result = ecmEngineSearchInterface.getClassificationAspects(context);

		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getClassificationAspects] END");
		}
		return result;
	}

	public ResultAssociation[] getClassifications(OperationContext context)  throws InvalidParameterException,
	NoSuchNodeException, SearchException, RemoteException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException {

		ResultAssociation[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getClassifications] BEGIN");
		try {

			result = ecmEngineSearchInterface.getClassifications(context);

		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getClassifications] END");
		}
		return result;
	}

	public ResultAssociation[] getRootCategories(Category category, OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException,
	InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException
	{

		ResultAssociation[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getRootCategories] BEGIN");
		try {

			result = ecmEngineSearchInterface.getRootCategories(category, context);

		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getRootCategories] END");
		}
		return result;
	}

	public TopCategory[] getTopCategories(Category category, int count, OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, RemoteException,
	InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException
	{

		TopCategory[] result = null;

		this.log.debug("["+getClass().getSimpleName()+"::getTopCategories] BEGIN");
		try {

			result = ecmEngineSearchInterface.getTopCategories(category, count, context);

		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::getTopCategories] END");
		}
		return result;
	}

	public Node[] massiveCreateContent(Node[] parents, Content[] contents, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException{
		Node[] result=null;
		this.log.debug("["+getClass().getSimpleName()+"::massiveCreateContent] BEGIN");
		try {
			result = ecmEngineMassiveInterface.massiveCreateContent(parents, contents, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::massiveCreateContent] END");
		}
		return result;
	}

	public void massiveDeleteContent(Node[] nodes,OperationContext context)throws InvalidParameterException, NoSuchNodeException, DeleteException ,InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, RemoteException {
		this.log.debug("["+getClass().getSimpleName()+"::massiveDeleteContent] BEGIN");
		try {
			ecmEngineMassiveInterface.massiveDeleteContent(nodes,context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::massiveDeleteContent] END");
		}
	}

	public void massiveUpdateMetadata(Node[] nodes, Content[] newContents, OperationContext context) throws InvalidParameterException, UpdateException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException{
		this.log.debug("["+getClass().getSimpleName()+"::massiveUpdateMetadata] BEGIN");
		try {
			ecmEngineMassiveInterface.massiveUpdateMetadata(nodes, newContents, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::massiveUpdateMetadata] END");
		}
	}

    public ResultContentData[] massiveRetrieveContentData(Node[] nodes, Content[] contents, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException {
        ResultContentData[] result = null;
		this.log.debug("["+getClass().getSimpleName()+"::massiveRetrieveContentData] BEGIN");
		try {
			result = ecmEngineMassiveInterface.massiveRetrieveContentData(nodes, contents, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::massiveRetrieveContentData] END");
		}
        return result;
	}

	public ResultContent[] massiveGetContentMetadata(Node[] nodes, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException {
        ResultContent[] result = null;
		this.log.debug("["+getClass().getSimpleName()+"::massiveGetContentMetadata] BEGIN");
		try {
			result = ecmEngineMassiveInterface.massiveGetContentMetadata(nodes, context);
		} finally {
			this.log.debug("["+getClass().getSimpleName()+"::massiveGetContentMetadata] END");
		}
        return result;
	}

	public Node addRenditionTransformer(Node nodoXml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		Node result = null;
		this.log.debug("["+getClass().getSimpleName()+"::addRenditionTransformer] BEGIN");
		try{
			result = ecmEngineManagementInterface.addRenditionTransformer(nodoXml, renditionTransformer, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::addRenditionTransformer] END");
		}
		return result;
	}

	public Node setRendition(Node nodoTransformer, RenditionDocument renditionDocument, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException {
		Node result = null;
		this.log.debug("["+getClass().getSimpleName()+"::setRendition] BEGIN");
		try{
			result = ecmEngineManagementInterface.setRendition(nodoTransformer, renditionDocument, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::setRendition] END");
		}
		return result;
	}

	public RenditionTransformer getRenditionTransformer(Node nodoTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		RenditionTransformer result = null;
		this.log.debug("["+getClass().getSimpleName()+"::getRenditionTransformer] BEGIN");
		try{
			result = ecmEngineManagementInterface.getRenditionTransformer(nodoTransformer, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::getRenditionTransformer] END");
		}
		return result;
	}

	public RenditionDocument getRendition(Node nodoTransformer, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		RenditionDocument result = null;
		this.log.debug("["+getClass().getSimpleName()+"::getRendition] BEGIN");
		try{
			result = ecmEngineManagementInterface.getRendition(nodoTransformer, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::getRendition] END");
		}
		return result;
	}

	public void deleteRenditionTransformer(Node xml, Node renditionTransformer, OperationContext context)throws InvalidParameterException, DeleteException, NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		this.log.debug("["+getClass().getSimpleName()+"::deleteRenditionTransformer] BEGIN");
		try{
			ecmEngineManagementInterface.deleteRenditionTransformer(xml, renditionTransformer, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::deleteRenditionTransformer] END");
		}
	}

	public RenditionTransformer[] getRenditionTransformers(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		RenditionTransformer[] result = null;
		this.log.debug("["+getClass().getSimpleName()+"::getRenditionTransformers] BEGIN");
		try{
			result = ecmEngineManagementInterface.getRenditionTransformers(xml, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::getRenditionTransformers] END");
		}
		return result;
	}

	public RenditionDocument[] getRenditions(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	RemoteException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException{
		RenditionDocument[] result = null;
		this.log.debug("["+getClass().getSimpleName()+"::getRenditions] BEGIN");
		try{
			result = ecmEngineManagementInterface.getRenditions(xml, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::getRenditions] END");
		}
		return result;
	}

	public RenditionDocument generateRendition(Content xml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException,RemoteException {
		RenditionDocument result = null;
		this.log.debug("["+getClass().getSimpleName()+"::generateRendition] BEGIN");
		try{
			result = ecmEngineManagementInterface.generateRendition(xml, renditionTransformer, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::generateRendition] END");
		}
		return result;
	}

	public Path getAbsolutePath(Node node, OperationContext context)throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException,	RemoteException{
		Path result = null;
		this.log.debug("["+getClass().getSimpleName()+"::generateRendition] BEGIN");
		try{
			result = ecmEngineSearchInterface.getAbsolutePath(node, context);
		}finally{
			this.log.debug("["+getClass().getSimpleName()+"::generateRendition] END");
		}
		return result;
	}

}
