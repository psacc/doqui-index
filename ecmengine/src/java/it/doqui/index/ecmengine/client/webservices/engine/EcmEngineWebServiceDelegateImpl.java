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

package it.doqui.index.ecmengine.client.webservices.engine;

import it.doqui.index.ecmengine.client.engine.EcmEngineDelegate;
import it.doqui.index.ecmengine.client.engine.EcmEngineDirectDelegateImpl;
import it.doqui.index.ecmengine.client.webservices.AbstractWebServiceDelegateBase;
import it.doqui.index.ecmengine.client.webservices.dto.Node;
import it.doqui.index.ecmengine.client.webservices.dto.OperationContext;
import it.doqui.index.ecmengine.client.webservices.dto.Path;
import it.doqui.index.ecmengine.client.webservices.dto.engine.NodeArchiveParams;
import it.doqui.index.ecmengine.client.webservices.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.client.webservices.dto.engine.audit.AuditSearchParams;
import it.doqui.index.ecmengine.client.webservices.dto.engine.audit.AuditTrailSearchParams;
import it.doqui.index.ecmengine.client.webservices.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Association;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Category;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Content;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.EncryptionInfo;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.FileFormatInfo;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.FileFormatVersion;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.FileInfo;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Mimetype;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Rendition;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Rule;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.SimpleWorkflow;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Version;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.RenditionDocument;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.RenditionTransformer;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.NodeResponse;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.ResultAssociation;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.ResultAspect;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.TopCategory;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.ResultContentData;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.SearchResponse;
import it.doqui.index.ecmengine.client.webservices.dto.engine.search.SearchParamsAggregate;
import it.doqui.index.ecmengine.client.webservices.dto.engine.security.Document;
import it.doqui.index.ecmengine.client.webservices.dto.engine.security.EnvelopedContent;
import it.doqui.index.ecmengine.client.webservices.dto.engine.security.VerifyReport;
import it.doqui.index.ecmengine.client.webservices.exception.EcmEngineException;
import it.doqui.index.ecmengine.client.webservices.exception.InvalidParameterException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.NoDataExtractedException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.AuditTrailException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.CheckInCheckOutException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.CopyException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.MoveException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.ReadException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.RenditionException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.TransformException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.UnsupportedTransformationException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.WorkflowException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.search.TooManyResultsException;


public class EcmEngineWebServiceDelegateImpl extends AbstractWebServiceDelegateBase implements EcmEngineWebServiceDelegate {

    private EcmEngineDelegate ecmEngineDelegate;

    public EcmEngineWebServiceDelegateImpl() {
        ecmEngineDelegate = new EcmEngineDirectDelegateImpl(log);
    }

    public void addSimpleWorkflowRule(Node node, SimpleWorkflow workflow,
            Rule rule, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    NoSuchNodeException, WorkflowException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::addSimpleWorkflowRule] BEGIN");
        try {
            this.ecmEngineDelegate.addSimpleWorkflowRule(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow)convertDTO(workflow, it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Rule)convertDTO(rule, it.doqui.index.ecmengine.dto.engine.management.Rule.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::addSimpleWorkflowRule] END");
        }
    }

    public void approveContent(Node node, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    NoSuchNodeException, WorkflowException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::approveContent] BEGIN");
        try {
            this.ecmEngineDelegate.approveContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::approveContent] END");
        }
    }

    public Node cancelCheckOutContent(Node node, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    NoSuchNodeException, CheckInCheckOutException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::cancelCheckOutContent] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.cancelCheckOutContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::cancelCheckOutContent] END");
        }
        return result;
    }

    public EncryptionInfo checkEncryption(Node node, OperationContext context)
    throws InvalidParameterException, EcmEngineException,
    NoSuchNodeException, InvalidCredentialsException,
    PermissionDeniedException,
    EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::checkEncryption] BEGIN");
        EncryptionInfo result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.EncryptionInfo resultEncInfo = this.ecmEngineDelegate.checkEncryption(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (EncryptionInfo)convertDTO(resultEncInfo, EncryptionInfo.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::checkEncryption] END");
        }
        return result;
    }

    public Node checkInContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    CheckInCheckOutException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::checkInContent] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.checkInContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::checkInContent] END");
        }
        return result;
    }

    public Node checkOutContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    CheckInCheckOutException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::checkOutContent] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.checkOutContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::checkOutContent] END");
        }
        return result;
    }

    public Node createContent(Node parent, Content content,
            OperationContext context) throws InvalidParameterException,
            InsertException, NoSuchNodeException, InvalidCredentialsException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::createContent] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.createContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(parent, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(content, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::createContent] END");
        }
        return result;
    }

    public void deleteContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    DeleteException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::deleteContent] BEGIN");
        try {
            this.ecmEngineDelegate.deleteContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::deleteContent] END");
        }
    }

    public Version[] getAllVersions(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    EcmEngineException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getAllVersions] BEGIN");
        Version[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.Version[] resultVersionArray = this.ecmEngineDelegate.getAllVersions(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Version[])convertDTOArray(resultVersionArray, Version.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getAllVersions] END");
        }
        return result;
    }

    public AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca,
            OperationContext context) throws InvalidParameterException,
            AuditTrailException, PermissionDeniedException, InvalidCredentialsException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::ricercaAuditTrail] BEGIN");
        AuditInfo[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.audit.AuditInfo[] resultAuditInfoArray = this.ecmEngineDelegate.ricercaAuditTrail(
                    (it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams)convertDTO(parametriRicerca, it.doqui.index.ecmengine.dto.engine.audit.AuditTrailSearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (AuditInfo[])convertDTOArray(resultAuditInfoArray, AuditInfo.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::ricercaAuditTrail] END");
        }
        return result;
    }

    public ResultContent getContentMetadata(Node node, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    NoSuchNodeException, ReadException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getContentMetadata] BEGIN");
        ResultContent result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultContent resultContent = this.ecmEngineDelegate.getContentMetadata(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (ResultContent)convertDTO(resultContent, ResultContent.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getContentMetadata] END");
        }
        return result;
    }

    public Version getVersion(Node node, String versionLabel,
            OperationContext context) throws InvalidParameterException,
            InvalidCredentialsException, NoSuchNodeException,
            EcmEngineException, EcmEngineTransactionException,
            PermissionDeniedException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getVersion] BEGIN");
        Version result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.Version resultVersion = this.ecmEngineDelegate.getVersion(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    versionLabel,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Version)convertDTO(resultVersion, Version.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getVersion] END");
        }
        return result;
    }

    public ResultContent getVersionMetadata(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    PermissionDeniedException, EcmEngineTransactionException,
    ReadException, InvalidCredentialsException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getVersionMetadata] BEGIN");
        ResultContent result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultContent resultContent = this.ecmEngineDelegate.getVersionMetadata(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (ResultContent)convertDTO(resultContent, ResultContent.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getVersionMetadata] END");
        }
        return result;
    }

    public Node getWorkingCopy(Node node, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    NoSuchNodeException, EcmEngineException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getWorkingCopy] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.getWorkingCopy(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getWorkingCopy] END");
        }
        return result;
    }

    public void linkContent(Node source, Node destination,
            Association association, OperationContext context)
    throws InvalidParameterException, InsertException,
    NoSuchNodeException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::linkContent] BEGIN");
        try {
            this.ecmEngineDelegate.linkContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(source, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(destination, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Association)convertDTO(association, it.doqui.index.ecmengine.dto.engine.management.Association.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::linkContent] END");
        }
    }

    public void logTrail(AuditInfo auditTrail, OperationContext context)
    throws InvalidParameterException, AuditTrailException,
    InvalidCredentialsException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::logTrail] BEGIN");
        try {
            this.ecmEngineDelegate.logTrail(
                    (it.doqui.index.ecmengine.dto.engine.audit.AuditInfo)convertDTO(auditTrail, it.doqui.index.ecmengine.dto.engine.audit.AuditInfo.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::logTrail] END");
        }
    }

    public void moveAggregation(Node source, Node destinationParent,
            OperationContext context) throws InvalidParameterException,
            MoveException, NoSuchNodeException, InvalidCredentialsException,
            EcmEngineTransactionException, PermissionDeniedException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::moveAggregation] BEGIN");
        try {
            this.ecmEngineDelegate.moveAggregation(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(source, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(destinationParent, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::moveAggregation] END");
        }
    }

    public void purgeAllContents(OperationContext context)
    throws InvalidParameterException, DeleteException,
    InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::purgeAllContents] BEGIN");
        try {
            this.ecmEngineDelegate.purgeAllContents(
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::purgeAllContents] END");
        }
    }

    public void purgeContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    DeleteException, InvalidCredentialsException,
    PermissionDeniedException,
    EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::purgeContent] BEGIN");
        try {
            this.ecmEngineDelegate.purgeContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::purgeContent] END");
        }
    }

    public Node[] restoreAllContents(OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException,
    EcmEngineException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::restoreAllContents] BEGIN");
        Node[] result = null;
        try {
            it.doqui.index.ecmengine.dto.Node[] resultNodeArray = this.ecmEngineDelegate.restoreAllContents((it.doqui.index.ecmengine.dto.OperationContext) convertDTO(
                    context,
                    it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (Node[])convertDTOArray(resultNodeArray, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::restoreAllContents] END");
        }
        return result;
    }

    public Node restoreContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, EcmEngineException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::restoreContent] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.restoreContent(
                    (it.doqui.index.ecmengine.dto.Node) convertDTO( node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext) convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::restoreContent] END");
        }
        return result;
    }

    public void rejectContent(Node node, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException,
    NoSuchNodeException, WorkflowException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::rejectContent] BEGIN");
        try {
            this.ecmEngineDelegate.rejectContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::rejectContent] END");
        }
    }

    public byte[] retrieveContentData(Node node, Content content,
            OperationContext context) throws InvalidParameterException,
            NoSuchNodeException, ReadException, InvalidCredentialsException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::retrieveContentData] BEGIN");
        byte[] result = null;
        try {
            result = this.ecmEngineDelegate.retrieveContentData(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(content, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::retrieveContentData] END");
        }
        return result;
    }

    public byte[] retrieveVersionContentData(Node node, Content content,
            OperationContext context) throws InvalidParameterException,
            InvalidCredentialsException, NoSuchNodeException, ReadException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::retrieveVersionContentData] BEGIN");
        byte[] result = null;
        try {
            result = this.ecmEngineDelegate.retrieveVersionContentData(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(content, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::retrieveVersionContentData] END");
        }
        return result;
    }

    public void revertVersion(Node node, String versionLabel,
            OperationContext context) throws InvalidParameterException,
            InvalidCredentialsException, NoSuchNodeException,
            EcmEngineException, PermissionDeniedException,
            EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::revertVersion] BEGIN");
        try {
            this.ecmEngineDelegate.revertVersion(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    versionLabel,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::revertVersion] END");
        }
    }

    public void startSimpleWorkflow(Node node, SimpleWorkflow workflow,
            OperationContext context) throws InvalidParameterException,
            InvalidCredentialsException, NoSuchNodeException,
            WorkflowException, PermissionDeniedException,
            EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::startSimpleWorkflow] BEGIN");
        try {
            this.ecmEngineDelegate.startSimpleWorkflow(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow)convertDTO(workflow, it.doqui.index.ecmengine.dto.engine.management.SimpleWorkflow.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::startSimpleWorkflow] END");
        }
    }

    public byte[] transformContent(Node node, String targetMimeType,
            OperationContext context) throws InvalidParameterException,
            InvalidCredentialsException, NoSuchNodeException,
            UnsupportedTransformationException, TransformException,
            EcmEngineTransactionException, PermissionDeniedException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::transformContent] BEGIN");
        byte[] result = null;
        try {
            result = this.ecmEngineDelegate.transformContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    targetMimeType,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::transformContent] END");
        }
        return result;
    }

    public void unLinkContent(Node source, Node destination,
            Association association, OperationContext context)
    throws InvalidParameterException, InsertException,
    NoSuchNodeException, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::unLinkContent] BEGIN");
        try {
            this.ecmEngineDelegate.unLinkContent(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(source, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(destination, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Association)convertDTO(association, it.doqui.index.ecmengine.dto.engine.management.Association.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::unLinkContent] END");
        }
    }

    public void updateContentData(Node node, Content content,
            OperationContext context) throws InvalidParameterException,
            UpdateException, NoSuchNodeException,
            EcmEngineTransactionException, InvalidCredentialsException,
            PermissionDeniedException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::updateContentData] BEGIN");
        try {
            this.ecmEngineDelegate.updateContentData(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(content, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::updateContentData] END");
        }
    }

    public void updateMetadata(Node node, Content newContent,
            OperationContext context) throws InvalidParameterException,
            UpdateException, NoSuchNodeException, InvalidCredentialsException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::updateMetadata] BEGIN");
        try {
            this.ecmEngineDelegate.updateMetadata(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(newContent, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::updateMetadata] END");
        }
    }

    public SearchResponse genericGlobalSearch(SearchParams params, OperationContext context) throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::genericGlobalSearch] BEGIN");
        SearchResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.SearchResponse resultResponse = this.ecmEngineDelegate.genericGlobalSearch(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(params, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (SearchResponse)convertDTO(resultResponse, SearchResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::genericGlobalSearch] END");
        }
        return result;
    }

    public ResultAssociation[] getAssociations(Node node, String assocType, int maxResults, OperationContext context) throws InvalidParameterException, NoSuchNodeException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getAssociations] BEGIN");
        ResultAssociation[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultAssociation[] resultPathArray = this.ecmEngineDelegate.getAssociations(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    assocType,
                    maxResults,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (ResultAssociation[])convertDTOArray(resultPathArray, ResultAssociation.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getAssociations] END");
        }
        return result;
    }

    public Path[] getPaths(Node node, OperationContext context) throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getPaths] BEGIN");
        Path[] result = null;
        try {
            it.doqui.index.ecmengine.dto.Path[] resultPathArray = this.ecmEngineDelegate.getPaths(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Path[])convertDTOArray(resultPathArray, Path.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getPaths] END");
        }
        return result;
    }

    public int getTotalResults(SearchParams xpath, OperationContext context) throws InvalidParameterException, SearchException, InvalidCredentialsException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getTotalResults] BEGIN");
        int result = 0;
        try {
            result = this.ecmEngineDelegate.getTotalResults(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(xpath, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getTotalResults] END");
        }
        return result;
    }

    public int getTotalResultsLucene(SearchParams lucene, OperationContext context) throws InvalidParameterException, SearchException, InvalidCredentialsException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getTotalResultsLucene] BEGIN");
        int result = 0;
        try {
            result = this.ecmEngineDelegate.getTotalResultsLucene(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(lucene, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getTotalResultsLucene] END");
        }
        return result;
    }

    public Node getUid(SearchParams xpath, OperationContext context) throws InvalidParameterException, NoDataExtractedException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getUid] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.getUid(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(xpath, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getUid] END");
        }
        return result;
    }

    public SearchResponse listDeletedNodes(NodeArchiveParams params, OperationContext context) throws InvalidParameterException, NoSuchNodeException,InvalidCredentialsException, PermissionDeniedException, SearchException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::listDeletedNodes] BEGIN");
        SearchResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.SearchResponse resultResponse = this.ecmEngineDelegate.listDeletedNodes(
                    (it.doqui.index.ecmengine.dto.engine.NodeArchiveParams)convertDTO(params, it.doqui.index.ecmengine.dto.engine.NodeArchiveParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (SearchResponse)convertDTO(resultResponse, SearchResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::listDeletedNodes] END");
        }
        return result;
    }

    public SearchResponse luceneSearch(SearchParams lucene, OperationContext context) throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::luceneSearch] BEGIN");
        SearchResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.SearchResponse resultResponse = this.ecmEngineDelegate.luceneSearch(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(lucene, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (SearchResponse)convertDTO(resultResponse, SearchResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::luceneSearch] END");
        }
        return result;
    }

    public String nodeExists(SearchParams xpath, OperationContext context) throws InvalidParameterException, SearchException, NoDataExtractedException, InvalidCredentialsException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::nodeExists] BEGIN");
        String result = null;
        try {
            result = this.ecmEngineDelegate.nodeExists(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(xpath, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::nodeExists] END");
        }
        return result;
    }

    public OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca, OperationContext context) throws EcmEngineException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::ricercaAudit] BEGIN");
        OperazioneAudit[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit[] resultAuditArray = this.ecmEngineDelegate.ricercaAudit(
                    (it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams)convertDTO(parametriRicerca, it.doqui.index.ecmengine.dto.engine.audit.AuditSearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (OperazioneAudit[])convertDTOArray(resultAuditArray, OperazioneAudit.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::ricercaAudit] END");
        }
        return result;
    }

    public SearchResponse xpathSearch(SearchParams xpath, OperationContext context) throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::xpathSearch] BEGIN");
        SearchResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.SearchResponse resultResponse = this.ecmEngineDelegate.xpathSearch(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(xpath, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (SearchResponse)convertDTO(resultResponse, SearchResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::xpathSearch] END");
        }
        return result;
    }

    public NodeResponse luceneSearchNoMetadata(SearchParams lucene, OperationContext context)
    throws InvalidParameterException, TooManyResultsException, SearchException, Exception, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::luceneSearchNoMetadata] BEGIN");
        NodeResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.NodeResponse resultResponse = this.ecmEngineDelegate.luceneSearchNoMetadata(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(lucene, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (NodeResponse)convertDTO(resultResponse, NodeResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::luceneSearchNoMetadata] END");
        }
        return result;
    }

    public NodeResponse xpathSearchNoMetadata(SearchParams xpath, OperationContext context)
    throws InvalidParameterException, TooManyResultsException, SearchException, Exception, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::xpathSearchNoMetadata] BEGIN");
        NodeResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.NodeResponse resultResponse = this.ecmEngineDelegate.xpathSearchNoMetadata(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(xpath, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (NodeResponse)convertDTO(resultResponse, NodeResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::xpathSearchNoMetadata] END");
        }
        return result;
    }

    public NodeResponse genericGlobalSearchNoMetadata(SearchParams params, OperationContext context)
    throws InvalidParameterException, TooManyResultsException, SearchException, Exception, InvalidCredentialsException,
    PermissionDeniedException, EcmEngineTransactionException {
        log.debug("[EcmEngineWebServiceDelegateImpl::genericGlobalSearchNoMetadata] BEGIN");
        NodeResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.NodeResponse resultResponse = this.ecmEngineDelegate.genericGlobalSearchNoMetadata(
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParams)convertDTO(params, it.doqui.index.ecmengine.dto.engine.search.SearchParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (NodeResponse)convertDTO(resultResponse, NodeResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::genericGlobalSearchNoMetadata] END");
        }
        return result;
    }

    public NodeResponse listDeletedNodesNoMetadata(NodeArchiveParams params, OperationContext context) throws InvalidParameterException,
    InvalidCredentialsException, NoSuchNodeException,PermissionDeniedException, SearchException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::listDeletedNodesNoMetadata] BEGIN");
        NodeResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.NodeResponse resultResponse = this.ecmEngineDelegate.listDeletedNodesNoMetadata(
                    (it.doqui.index.ecmengine.dto.engine.NodeArchiveParams)convertDTO(params, it.doqui.index.ecmengine.dto.engine.NodeArchiveParams.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (NodeResponse)convertDTO(resultResponse, NodeResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::listDeletedNodesNoMetadata] END");
        }
        return result;
    }

    public NodeResponse selectNodes(Node node, SearchParamsAggregate parameterAggregate, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, TooManyResultsException, SearchException, InvalidCredentialsException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        this.log.debug("[EcmEngineWebServiceDelegateImpl::selectNodes] BEGIN");
        NodeResponse result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.NodeResponse resultResponse = this.ecmEngineDelegate.selectNodes(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate)convertDTO(parameterAggregate, it.doqui.index.ecmengine.dto.engine.search.SearchParamsAggregate.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (NodeResponse)convertDTO(resultResponse, NodeResponse.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            this.log.debug("[EcmEngineWebServiceDelegateImpl::selectNodes] END");
        }
        return result;
    }

    public Mimetype[] getMimetype(Mimetype mimetype)
    throws InvalidParameterException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::getMimetype] BEGIN");
        Mimetype[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.Mimetype[] mt = this.ecmEngineDelegate.getMimetype(
                    (it.doqui.index.ecmengine.dto.engine.management.Mimetype)convertDTO(mimetype, it.doqui.index.ecmengine.dto.engine.management.Mimetype.class)
            );
            result=new Mimetype[mt.length];
            for(int i=0;i<mt.length;i++){
                result[i] = (Mimetype)convertDTO(mt[i], Mimetype.class);
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getMimetype] END");
        }
        return result;
    }

    public FileFormatInfo[] getFileFormatInfo(Node node, Content content, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, Exception, EcmEngineException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getFileFormatInfo] BEGIN");
        FileFormatInfo[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.FileFormatInfo[] ffi = this.ecmEngineDelegate.getFileFormatInfo(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(content, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=new FileFormatInfo[ffi.length];
            for(int i=0;i<ffi.length;i++){
                result[i] = (FileFormatInfo)convertDTO(ffi[i], FileFormatInfo.class);
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getFileFormatInfo] END");
        }
        return result;
    }

    public FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, Exception, EcmEngineException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getFileFormatInfo] BEGIN");
        FileFormatInfo[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.FileFormatInfo[] ffi = this.ecmEngineDelegate.getFileFormatInfo(
                    (it.doqui.index.ecmengine.dto.engine.management.FileInfo)convertDTO(fileInfo, it.doqui.index.ecmengine.dto.engine.management.FileInfo.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=new FileFormatInfo[ffi.length];
            for(int i=0;i<ffi.length;i++){
                result[i] = (FileFormatInfo)convertDTO(ffi[i], FileFormatInfo.class);
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getFileFormatInfo] END");
        }
        return result;
    }

    public FileFormatVersion getFileFormatVersion(OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, Exception, EcmEngineException {
        log.debug("[EcmEngineWebServiceDelegateImpl::getFileFormatVersion] BEGIN");
        FileFormatVersion result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.FileFormatVersion ffv = this.ecmEngineDelegate.getFileFormatVersion(
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );

            result=(FileFormatVersion)convertDTO(ffv, FileFormatVersion.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getFileFormatVersion] END");
        }
        return result;
    }

    public VerifyReport verifyDocument(EnvelopedContent envelopedContent, OperationContext context) throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::verifyDocument] BEGIN");
        VerifyReport result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.security.VerifyReport vr = this.ecmEngineDelegate.verifyDocument(
                    (it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent)convertDTO(envelopedContent, it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );

            //TODO conversione ricorsiva delle signature.
            result=(VerifyReport)convertDTO(vr,VerifyReport.class);
        } catch (Exception e) {
            handleException(e);

        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::verifyDocument] END");
        }
        return result;
    }

    public VerifyReport verifyDocument(Node node, OperationContext context) throws InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::verifyDocument] BEGIN");
        VerifyReport result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.security.VerifyReport vr = this.ecmEngineDelegate.verifyDocument(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            //TODO conversione ricorsiva delle signature.
            result=(VerifyReport)convertDTO(vr,VerifyReport.class);
        } catch (Exception e) {
            handleException(e);

        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::verifyDocument] END");
        }
        return result;
    }

    public Document extractDocumentFromEnvelope(EnvelopedContent envelopedContent, OperationContext context)throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::extractDocumentFromEnvelope] BEGIN");
        Document result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.security.Document doc = this.ecmEngineDelegate.extractDocumentFromEnvelope(
                    (it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent)convertDTO(envelopedContent, it.doqui.index.ecmengine.dto.engine.security.EnvelopedContent.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(Document)convertDTO(doc,Document.class);
        } catch (Exception e) {
            handleException(e);

        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::extractDocumentFromEnvelope] END");
        }
        return result;
    }

    public Document extractDocumentFromEnvelope(Node node, OperationContext context) throws InsertException,InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::extractDocumentFromEnvelope] BEGIN");
        Document result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.security.Document doc = this.ecmEngineDelegate.extractDocumentFromEnvelope(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(node, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(Document)convertDTO(doc,Document.class);
        } catch (Exception e) {
            handleException(e);

        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::extractDocumentFromEnvelope] END");
        }
        return result;
    }

    public Node createContentFromTemporaney(Node parentNode, Content content,OperationContext context, Node tempNode)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::createContentFromTemporaney] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.createContentFromTemporaney(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(parentNode, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(content, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(tempNode, it.doqui.index.ecmengine.dto.Node.class)
            );
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::createContentFromTemporaney] END");
        }
        return result;
    }

    public Node copyNode(Node source, Node parent, OperationContext context)throws InvalidParameterException, InsertException, CopyException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::copyNode] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.copyNode(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(source, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(parent, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::copyNode] END");
        }
        return result;
    }

    public void moveNode(Node source, Node parent, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::moveNode] BEGIN");
        try {
            this.ecmEngineDelegate.moveNode(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(source, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(parent, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::moveNode] END");
        }
    }

    public Node createCategory(Node categoryParent, Category category,OperationContext context) throws InvalidParameterException, InsertException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException, Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::createCategory] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.createCategory(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(categoryParent, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Category)convertDTO(category, it.doqui.index.ecmengine.dto.engine.management.Category.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::createCategory] END");
        }
        return result;
    }

    public Node createRootCategory(Category rootCategory,OperationContext context) throws InvalidParameterException, InsertException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException, Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::createRootCategory] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultNode = this.ecmEngineDelegate.createRootCategory(
                    (it.doqui.index.ecmengine.dto.engine.management.Category)convertDTO(rootCategory, it.doqui.index.ecmengine.dto.engine.management.Category.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (Node)convertDTO(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::createRootCategory] END");
        }
        return result;
    }

    public void deleteCategory(Node categoryNode,OperationContext context) throws InvalidParameterException, DeleteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException, EcmEngineTransactionException, Exception, EcmEngineException{
        log.debug("[EcmEngineWebServiceDelegateImpl::deleteCategory] BEGIN");
        try {
            this.ecmEngineDelegate.deleteCategory(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(categoryNode, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::deleteCategory] END");
        }
    }


    public ResultAssociation[] getCategories(Category category, String depth,OperationContext context) throws InvalidParameterException,NoSuchNodeException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException{
        log.debug("[EcmEngineWebServiceDelegateImpl::getCategories] BEGIN");
        ResultAssociation[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultAssociation[] resultPathArray = this.ecmEngineDelegate.getCategories(
                    (it.doqui.index.ecmengine.dto.engine.management.Category)convertDTO(category, it.doqui.index.ecmengine.dto.engine.management.Category.class),
                    depth,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (ResultAssociation[])convertDTOArray(resultPathArray, ResultAssociation.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getCategories] END");
        }
        return result;
    }

    public ResultAssociation[] getCategoryChildren(Node categoryNode, String mode, String depth,OperationContext context) throws InvalidParameterException,NoSuchNodeException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException{
        log.debug("[EcmEngineWebServiceDelegateImpl::getCategoryChildren] BEGIN");
        ResultAssociation[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultAssociation[] resultPathArray = this.ecmEngineDelegate.getCategoryChildren(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(categoryNode, it.doqui.index.ecmengine.dto.Node.class),
                    mode,
                    depth,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (ResultAssociation[])convertDTOArray(resultPathArray, ResultAssociation.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getCategoryChildren] END");
        }
        return result;
    }

    public ResultAspect[] getClassificationAspects(OperationContext context) throws InvalidParameterException, SearchException, InvalidCredentialsException,PermissionDeniedException, Exception, EcmEngineTransactionException{
        log.debug("[EcmEngineWebServiceDelegateImpl::getClassificationAspects] BEGIN");
        ResultAspect[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultAspect[] resultPathArray = this.ecmEngineDelegate.getClassificationAspects(
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (ResultAspect[])convertDTOArray(resultPathArray, ResultAspect.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getClassificationAspects] END");
        }
        return result;
    }

    public ResultAssociation[] getClassifications(OperationContext context) throws InvalidParameterException, NoSuchNodeException, SearchException, InvalidCredentialsException, PermissionDeniedException, Exception, EcmEngineTransactionException{
        log.debug("[EcmEngineWebServiceDelegateImpl::getClassifications] BEGIN");
        ResultAssociation[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultAssociation[] resultPathArray = this.ecmEngineDelegate.getClassifications(
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (ResultAssociation[])convertDTOArray(resultPathArray, ResultAssociation.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getClassifications] END");
        }
        return result;
    }

    public ResultAssociation[] getRootCategories(Category category,OperationContext context) throws InvalidParameterException,NoSuchNodeException, SearchException, InvalidCredentialsException,PermissionDeniedException, Exception, EcmEngineTransactionException{
        log.debug("[EcmEngineWebServiceDelegateImpl::getRootCategories] BEGIN");
        ResultAssociation[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultAssociation[] resultPathArray = this.ecmEngineDelegate.getRootCategories(
                    (it.doqui.index.ecmengine.dto.engine.management.Category)convertDTO(category, it.doqui.index.ecmengine.dto.engine.management.Category.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (ResultAssociation[])convertDTOArray(resultPathArray, ResultAssociation.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getRootCategories] END");
        }
        return result;
    }

    public TopCategory[] getTopCategories(Category category,int count,OperationContext context) throws InvalidParameterException,NoSuchNodeException, SearchException, InvalidCredentialsException,PermissionDeniedException, Exception, EcmEngineTransactionException{
        log.debug("[EcmEngineWebServiceDelegateImpl::getTopCategories] BEGIN");
        TopCategory[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.TopCategory[] resultPathArray = this.ecmEngineDelegate.getTopCategories(
                    (it.doqui.index.ecmengine.dto.engine.management.Category)convertDTO(category, it.doqui.index.ecmengine.dto.engine.management.Category.class),
                    count,
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class));
            result = (TopCategory[])convertDTOArray(resultPathArray, TopCategory.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getTopCategories] END");
        }
        return result;
    }

    public Node[] massiveCreateContent(Node[] parents, Content[] contents,
            OperationContext context) throws InvalidParameterException,
            InsertException, NoSuchNodeException, InvalidCredentialsException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::massiveCreateContent] BEGIN");
        Node[] result = null;
        try {
            it.doqui.index.ecmengine.dto.Node[] resultNode = this.ecmEngineDelegate.massiveCreateContent(
                    (it.doqui.index.ecmengine.dto.Node[])convertDTOArray(parents, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content[])convertDTOArray(contents, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result = (Node[])convertDTOArray(resultNode, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::massiveCreateContent] END");
        }
        return result;
    }

    public void massiveDeleteContent(Node[] nodes,OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::massiveDeleteContent] BEGIN");
        try {
        	this.ecmEngineDelegate.massiveDeleteContent(
                    (it.doqui.index.ecmengine.dto.Node[])convertDTOArray(nodes, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::massiveDeleteContent] END");
        }
    }

    public void massiveUpdateMetadata(Node[] nodes, Content[] newContents,
            OperationContext context) throws InvalidParameterException,
            UpdateException, NoSuchNodeException, InvalidCredentialsException,
            PermissionDeniedException, EcmEngineTransactionException, Exception {
        log.debug("[EcmEngineWebServiceDelegateImpl::massiveUpdateMetadata] BEGIN");
        try {
            this.ecmEngineDelegate.massiveUpdateMetadata(
                    (it.doqui.index.ecmengine.dto.Node[])convertDTOArray(nodes, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content[])convertDTOArray(newContents, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::massiveUpdateMetadata] END");
        }
    }

    public ResultContentData[] massiveRetrieveContentData(Node[] nodes, Content[] contents, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::massiveRetrieveContentData] BEGIN");
        ResultContentData[] result = null;
        try {
        	it.doqui.index.ecmengine.dto.engine.search.ResultContentData[] resultCD= this.ecmEngineDelegate.massiveRetrieveContentData(
                    (it.doqui.index.ecmengine.dto.Node[])convertDTOArray(nodes, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.Content[])convertDTOArray(contents, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        	result=(ResultContentData[])convertDTOArray(resultCD, ResultContentData.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::massiveRetrieveContentData] END");
        }
        return result;
    }

    public ResultContent[] massiveGetContentMetadata(Node[] nodes, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::massiveRetrieveContentData] BEGIN");
        ResultContent[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.search.ResultContent[] resultC=this.ecmEngineDelegate.massiveGetContentMetadata(
                    (it.doqui.index.ecmengine.dto.Node[])convertDTOArray(nodes, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(ResultContent[])convertDTOArray(resultC, ResultContent.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::massiveRetrieveContentData] END");
        }
        return result;
    }

	public Node addRenditionTransformer(Node nodoXml, RenditionTransformer renditionTransformer, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::addRenditionTransformer] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultC=this.ecmEngineDelegate.addRenditionTransformer(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(nodoXml, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer)convertDTO(renditionTransformer, it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(Node)convertDTO(resultC, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::addRenditionTransformer] END");
        }
        return result;
    }

	public Node setRendition(Node nodoTransformer, RenditionDocument renditionDocument, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::setRendition] BEGIN");
        Node result = null;
        try {
            it.doqui.index.ecmengine.dto.Node resultC=this.ecmEngineDelegate.setRendition(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(nodoTransformer, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.engine.management.RenditionDocument)convertDTO(renditionDocument, it.doqui.index.ecmengine.dto.engine.management.RenditionDocument.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(Node)convertDTO(resultC, Node.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::setRendition] END");
        }
        return result;
    }

	public RenditionTransformer getRenditionTransformer(Node nodoTransformer, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::getRenditionTransformer] BEGIN");
        RenditionTransformer result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer resultC=this.ecmEngineDelegate.getRenditionTransformer(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(nodoTransformer, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(RenditionTransformer)convertDTO(resultC, RenditionTransformer.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getRenditionTransformer] END");
        }
        return result;
    }

	public RenditionDocument getRendition(Node nodoTransformer, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::getRendition] BEGIN");
        RenditionDocument result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.RenditionDocument resultC=this.ecmEngineDelegate.getRendition(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(nodoTransformer, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(RenditionDocument)convertDTO(resultC, RenditionDocument.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getRendition] END");
        }
        return result;
    }

	public void deleteRenditionTransformer(Node xml, Node renditionTransformer, OperationContext context)throws InvalidParameterException, DeleteException, NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::deleteRenditionTransformer] BEGIN");
        try {
            this.ecmEngineDelegate.deleteRenditionTransformer(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(xml, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(renditionTransformer, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::deleteRenditionTransformer] END");
        }
    }

	public RenditionTransformer[] getRenditionTransformers(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::getRenditionTransformers] BEGIN");
        RenditionTransformer[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer[] resultC=this.ecmEngineDelegate.getRenditionTransformers(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(xml, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(RenditionTransformer[])convertDTOArray(resultC, RenditionTransformer.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getRenditionTransformers] END");
        }
        return result;
    }

	public RenditionDocument[] getRenditions(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::getRenditions] BEGIN");
        RenditionDocument[] result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.RenditionDocument[] resultC=this.ecmEngineDelegate.getRenditions(
                    (it.doqui.index.ecmengine.dto.Node)convertDTO(xml, it.doqui.index.ecmengine.dto.Node.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(RenditionDocument[])convertDTOArray(resultC, RenditionDocument.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::getRenditions] END");
        }
        return result;
    }

	public RenditionDocument generateRendition(Content xml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException, Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::generateRendition] BEGIN");
        RenditionDocument result = null;
        try {
            it.doqui.index.ecmengine.dto.engine.management.RenditionDocument resultC=this.ecmEngineDelegate.generateRendition(
                    (it.doqui.index.ecmengine.dto.engine.management.Content)convertDTO(xml, it.doqui.index.ecmengine.dto.engine.management.Content.class),
                    (it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer)convertDTO(renditionTransformer, it.doqui.index.ecmengine.dto.engine.management.RenditionTransformer.class),
                    (it.doqui.index.ecmengine.dto.OperationContext)convertDTO(context, it.doqui.index.ecmengine.dto.OperationContext.class)
            );
            result=(RenditionDocument)convertDTO(resultC, RenditionDocument.class);
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::generateRendition] END");
        }
        return result;
    }
	
	public boolean testResources()throws Exception{
        log.debug("[EcmEngineWebServiceDelegateImpl::testResources] BEGIN");
        boolean result = false;
        try {
        	result=this.ecmEngineDelegate.testResources();
        } catch (Exception e) {
            handleException(e);
        } finally {
            log.debug("[EcmEngineWebServiceDelegateImpl::testResources] END");
        }
        return result;		
	}
}
