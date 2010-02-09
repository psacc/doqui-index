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
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.TransformException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.UnsupportedTransformationException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.WorkflowException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.search.SearchException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.search.TooManyResultsException;
import it.doqui.index.ecmengine.client.webservices.exception.publishing.engine.management.RenditionException;
import it.doqui.index.ecmengine.client.webservices.dto.engine.management.Rendition;


public interface EcmEngineWebServiceDelegate {

	Node checkOutContent(Node node, OperationContext context)
		throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, InvalidCredentialsException,
		PermissionDeniedException, EcmEngineTransactionException, Exception;

	Node checkInContent(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, CheckInCheckOutException, InvalidCredentialsException,
	PermissionDeniedException, EcmEngineTransactionException, Exception;

    void moveAggregation(Node source, Node destinationParent,OperationContext context)
	throws InvalidParameterException, MoveException, NoSuchNodeException, InvalidCredentialsException,
	EcmEngineTransactionException, PermissionDeniedException, Exception;

    Node createContent(Node parent, Content content, OperationContext context)
    throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, Exception;

    void deleteContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, Exception;

    byte [] retrieveContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, Exception;

    void linkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, Exception;

    void unLinkContent(Node source, Node destination, Association association, OperationContext context)
	throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, Exception;

    void updateMetadata(Node node, Content newContent, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, Exception;

    void logTrail(AuditInfo auditTrail, OperationContext context) throws InvalidParameterException,
    AuditTrailException, InvalidCredentialsException, EcmEngineTransactionException, Exception;

	AuditInfo[] ricercaAuditTrail(AuditTrailSearchParams parametriRicerca, OperationContext context)
	throws InvalidParameterException, AuditTrailException, PermissionDeniedException, InvalidCredentialsException, EcmEngineTransactionException, Exception;

    void updateContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException, Exception, EcmEngineTransactionException,
	InvalidCredentialsException, PermissionDeniedException;

	Version[] getAllVersions(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, EcmEngineException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, Exception;

	Version getVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, EcmEngineTransactionException,
	PermissionDeniedException, Exception;

	byte [] retrieveVersionContentData(Node node, Content content, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, ReadException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	void revertVersion(Node node, String versionLabel, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	Node cancelCheckOutContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, CheckInCheckOutException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	Node getWorkingCopy(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, EcmEngineException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

    ResultContent getContentMetadata(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, ReadException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	ResultContent getVersionMetadata(Node node, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, PermissionDeniedException,
	EcmEngineTransactionException, ReadException, InvalidCredentialsException, Exception;

	EncryptionInfo checkEncryption(Node node, OperationContext context)
	throws InvalidParameterException, EcmEngineException, NoSuchNodeException, InvalidCredentialsException,
	PermissionDeniedException, Exception, EcmEngineTransactionException;

	byte [] transformContent(Node node, String targetMimeType, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, UnsupportedTransformationException,
	TransformException, Exception, EcmEngineTransactionException, PermissionDeniedException;

	void startSimpleWorkflow(Node node, SimpleWorkflow workflow, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	void addSimpleWorkflowRule(Node node, SimpleWorkflow workflow, Rule rule, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	void approveContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

	void rejectContent(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, WorkflowException, Exception,
	PermissionDeniedException, EcmEngineTransactionException;

    void purgeContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
    Exception, EcmEngineTransactionException;

    void purgeAllContents(OperationContext context)
    throws InvalidParameterException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
    Exception, EcmEngineTransactionException;

    Node restoreContent(Node node, OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, EcmEngineException, Exception;

    Node[] restoreAllContents(OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, EcmEngineException , Exception;

	ResultAssociation[] getAssociations(Node node, String assocType, int maxResults, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, SearchException, InvalidCredentialsException, PermissionDeniedException,
			Exception, EcmEngineTransactionException;


	ResultAssociation[] getCategories(Category category, String depth,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, InvalidCredentialsException,
	PermissionDeniedException, Exception, EcmEngineTransactionException;

	ResultAssociation[] getCategoryChildren(Node categoryNode, String mode, String depth,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException, InvalidCredentialsException,
	PermissionDeniedException, Exception, EcmEngineTransactionException;

	ResultAspect[] getClassificationAspects(OperationContext context) throws InvalidParameterException,
	SearchException, InvalidCredentialsException,PermissionDeniedException, Exception, EcmEngineTransactionException;

	ResultAssociation[] getClassifications(OperationContext context) throws InvalidParameterException,
	NoSuchNodeException, SearchException, InvalidCredentialsException,
	PermissionDeniedException, Exception, EcmEngineTransactionException;

	ResultAssociation[] getRootCategories(Category category,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException,
	InvalidCredentialsException,PermissionDeniedException, Exception, EcmEngineTransactionException;

	TopCategory[] getTopCategories(Category category,int count,OperationContext context)
	throws InvalidParameterException,NoSuchNodeException, SearchException,
	InvalidCredentialsException,PermissionDeniedException, Exception, EcmEngineTransactionException;


	String nodeExists(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, NoDataExtractedException, InvalidCredentialsException, Exception,
	EcmEngineTransactionException;

	SearchResponse luceneSearch(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
	Exception, EcmEngineTransactionException;

	SearchResponse xpathSearch(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
	Exception, EcmEngineTransactionException;

	SearchResponse genericGlobalSearch(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, InvalidCredentialsException, PermissionDeniedException,
	Exception, EcmEngineTransactionException;

	Node getUid(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, NoDataExtractedException, SearchException, InvalidCredentialsException,
	PermissionDeniedException, Exception, EcmEngineTransactionException;

	int getTotalResults(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, SearchException, InvalidCredentialsException, Exception, EcmEngineTransactionException;

	int getTotalResultsLucene(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, SearchException, InvalidCredentialsException, Exception, EcmEngineTransactionException;

	Path[] getPaths(Node node, OperationContext context)
	throws InvalidParameterException, InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException,
	Exception;

	OperazioneAudit[] ricercaAudit(AuditSearchParams parametriRicerca, OperationContext context)
	throws EcmEngineException, Exception;

	SearchResponse listDeletedNodes(NodeArchiveParams params, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, SearchException, Exception;

	NodeResponse luceneSearchNoMetadata(SearchParams lucene, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, Exception, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException;

	NodeResponse xpathSearchNoMetadata(SearchParams xpath, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, Exception, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException;

	NodeResponse genericGlobalSearchNoMetadata(SearchParams params, OperationContext context)
	throws InvalidParameterException, TooManyResultsException, SearchException, Exception, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException;

	NodeResponse listDeletedNodesNoMetadata(NodeArchiveParams params, OperationContext context) throws InvalidParameterException,
			InvalidCredentialsException, NoSuchNodeException, PermissionDeniedException, SearchException, Exception;

	NodeResponse selectNodes(Node node, SearchParamsAggregate parameterAggregate, OperationContext context)
	throws InvalidParameterException, NoSuchNodeException, TooManyResultsException, SearchException, InvalidCredentialsException,
			PermissionDeniedException, EcmEngineTransactionException, Exception;

	Mimetype[] getMimetype(Mimetype mimetype)
	throws InvalidParameterException, Exception;

    FileFormatInfo[] getFileFormatInfo(Node node, Content content, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, Exception, EcmEngineException;

    FileFormatInfo[] getFileFormatInfo(FileInfo fileInfo, OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, Exception, EcmEngineException;

    FileFormatVersion getFileFormatVersion(OperationContext context)
    throws InvalidParameterException, InvalidCredentialsException, Exception, EcmEngineException;

    // ---- SECURITY ----
	VerifyReport verifyDocument(EnvelopedContent envelopedContent, OperationContext context) throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,Exception, EcmEngineException;

	VerifyReport verifyDocument(Node node, OperationContext context) throws InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,Exception, EcmEngineException;

	Document extractDocumentFromEnvelope(EnvelopedContent envelopedContent, OperationContext context)throws InsertException,NoSuchNodeException,InvalidParameterException, InvalidCredentialsException,PermissionDeniedException,EcmEngineTransactionException,Exception, EcmEngineException;

	Document extractDocumentFromEnvelope(Node node, OperationContext context) throws InsertException,InvalidParameterException,InvalidCredentialsException,PermissionDeniedException,NoSuchNodeException,EcmEngineTransactionException,Exception, EcmEngineException;

	Node createContentFromTemporaney(Node parentNode, Content content,OperationContext context, Node tempNode)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, Exception, EcmEngineException;
    // ---- SECURITY ----

    Node copyNode(Node source, Node parent, OperationContext context)throws InvalidParameterException, InsertException, CopyException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, Exception, EcmEngineException;

    void moveNode(Node source, Node parent, OperationContext context)throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,EcmEngineTransactionException, Exception, EcmEngineException;

    Node createCategory(Node categoryParent, Category category,OperationContext context) throws InvalidParameterException,
    InsertException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
    EcmEngineTransactionException, Exception, EcmEngineException;

    Node createRootCategory(Category rootCategory,OperationContext context) throws InvalidParameterException,
    InsertException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
    EcmEngineTransactionException, Exception, EcmEngineException;

    void deleteCategory(Node categoryNode,OperationContext context) throws InvalidParameterException,
    DeleteException,NoSuchNodeException,InvalidCredentialsException,PermissionDeniedException,
    EcmEngineTransactionException, Exception, EcmEngineException;

    Node[] massiveCreateContent(Node[] parents, Content[] contents,OperationContext context)
    throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, Exception;

    void massiveDeleteContent(Node[] nodes,OperationContext context)
    throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,
    EcmEngineTransactionException, Exception;

    void massiveUpdateMetadata(Node[] nodes, Content[] newContents, OperationContext context)
	throws InvalidParameterException, UpdateException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException,
	EcmEngineTransactionException, Exception;

    ResultContentData[] massiveRetrieveContentData(Node[] nodes, Content[] contents, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	ResultContent[] massiveGetContentMetadata(Node[] nodes, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

    // ---- RENDITION ----
	Node addRenditionTransformer(Node nodoXml, RenditionTransformer renditionTransformer, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	Node setRendition(Node nodoTransformer, RenditionDocument renditionDocument, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	RenditionTransformer getRenditionTransformer(Node nodoTransformer, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	RenditionDocument getRendition(Node nodoTransformer, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException,
    InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	void deleteRenditionTransformer(Node xml, Node renditionTransformer, OperationContext context)throws InvalidParameterException, DeleteException, NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	RenditionTransformer[] getRenditionTransformers(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	RenditionDocument[] getRenditions(Node xml, OperationContext context) throws InvalidParameterException,NoSuchNodeException,
	InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, Exception;

	RenditionDocument generateRendition(Content xml, RenditionTransformer renditionTransformer, OperationContext context)throws InvalidParameterException, Exception;
    // ---- RENDITION ----
	
	boolean testResources() throws Exception;
}
