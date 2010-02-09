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

package it.doqui.index.ecmengine.business.publishing.massive;

import java.rmi.RemoteException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.business.publishing.EcmEngineFeatureBean;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.ResultContentData;
import it.doqui.index.ecmengine.exception.EcmEngineException;
import it.doqui.index.ecmengine.exception.EcmEngineFoundationException;
import it.doqui.index.ecmengine.exception.InvalidParameterException;
import it.doqui.index.ecmengine.exception.publishing.EcmEngineTransactionException;
import it.doqui.index.ecmengine.exception.publishing.InvalidCredentialsException;
import it.doqui.index.ecmengine.exception.publishing.NoSuchNodeException;
import it.doqui.index.ecmengine.exception.publishing.PermissionDeniedException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.DeleteException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.InsertException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.UpdateException;
import it.doqui.index.ecmengine.exception.publishing.engine.management.ReadException;

public class EcmEngineMassiveBean extends EcmEngineFeatureBean{

	private static final long serialVersionUID = 3631637615726377750L;

	public Node[] massiveCreateContent(Node[] parents, Content[] contents, OperationContext context) throws InvalidParameterException, InsertException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException{
		Node[] response=null;
		UserTransaction transaction=null;
		int cicloMassive=0;
		logger.debug("[EcmEngineMassiveBean::massiveCreateContent] BEGIN");
		start();
		try{
			validate(ValidationType.NOT_NULL, "parents", parents);
			validate(ValidationType.NOT_NULL, "contents", contents);
			validate(ValidationType.IS_ZERO, "parents size - contents size", new Long(parents.length-contents.length));
			validate(ValidationType.OPERATION_CONTEXT, "context", context);
			for(cicloMassive=0;cicloMassive<contents.length;cicloMassive++){
				validate(ValidationType.NODE             , "parents["+cicloMassive+"]" , parents[cicloMassive]);
				validate(ValidationType.CONTENT_WRITE_NEW, "contents["+cicloMassive+"]", contents[cicloMassive]);
			}
			validate(ValidationType.MASSIVE_CREATE_CONTENT,"contents",contents);
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			response=new Node[contents.length];
			for(cicloMassive=0;cicloMassive<contents.length;cicloMassive++){
				response[cicloMassive]=createContentNoTransaction(parents[cicloMassive], contents[cicloMassive], context);
			}
			transaction.commit();
		} catch(InvalidParameterException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(InsertException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(NoSuchNodeException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(InvalidCredentialsException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(PermissionDeniedException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<InsertException>checkIntegrityException(
					e, "EcmEngineManagementBean", "massiveCreateContent", InsertException.class);
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			stop();
			logger.debug("[EcmEngineMassiveBean::massiveCreateContent] END");
		}
		return response;
	}
	
	public void massiveDeleteContent(Node[] nodes, OperationContext context)throws InvalidParameterException, NoSuchNodeException, DeleteException, InvalidCredentialsException, PermissionDeniedException,RemoteException, EcmEngineTransactionException{
		UserTransaction transaction=null;
		int cicloMassive=0;
		logger.debug("[EcmEngineMassiveBean::massiveCreateContent] BEGIN");
		start();
		try{
			validate(ValidationType.NOT_NULL, "nodes", nodes);
			validate(ValidationType.MASSIVE_DELETE_CONTENT,"nodes",nodes);
			validate(ValidationType.OPERATION_CONTEXT, "context", context);
			for(cicloMassive=0;cicloMassive<nodes.length;cicloMassive++){
				validate(ValidationType.NODE, "nodes["+cicloMassive+"]" , nodes[cicloMassive]);
			}
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			for(cicloMassive=0;cicloMassive<nodes.length;cicloMassive++){
				deleteContentNoTransaction(nodes[cicloMassive],context);
			}
			transaction.commit();
		} catch(InvalidParameterException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (EcmEngineFoundationException e) {
			checkCredentialsException(e, "EcmEngineManagementBean", "deleteContent", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "deleteContent", "User: " + context.getUsername(), transaction);
			logger.error("[EcmEngineManagementBean::deleteContent] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new DeleteException("Backend services error: " + e.getCode());
		} catch(NoSuchNodeException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(InvalidCredentialsException e){
			rollbackQuietely(transaction);
			throw e;
		} catch(PermissionDeniedException e){
			rollbackQuietely(transaction);
			throw e;
		} catch (RollbackException e) {
			this.<DeleteException>checkIntegrityException(
					e, "EcmEngineManagementBean", "deleteContent", DeleteException.class);
			handleTransactionException(e, "transaction rolled-back.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		}finally{
			stop();
			logger.debug("[EcmEngineMassiveBean::massiveCreateContent] END");
		}		
	}

	public void massiveUpdateMetadata(Node[] nodes, Content[] newContents, OperationContext context) throws InvalidParameterException, UpdateException, NoSuchNodeException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException{
		UserTransaction transaction = null;
		Content         newContent  = null;
		logger.debug("[EcmEngineMassiveBean::massiveUpdateMetadata] END");
		start();
		try {
			validate(ValidationType.NOT_NULL, "nodes", nodes);
			validate(ValidationType.NOT_NULL, "newContents", newContents);
			validate(ValidationType.OPERATION_CONTEXT, "context", context);
			validate(ValidationType.MASSIVE_UPDATE_METADATA,"newContents",newContents);
			validate(ValidationType.IS_ZERO, "nodes size - newContents size", new Long(nodes.length-newContents.length));
			for(int cicloMassive=0;cicloMassive<nodes.length;cicloMassive++){
				validate(ValidationType.NODE, "nodes["+cicloMassive+"]", nodes[cicloMassive]);
				validate(ValidationType.CONTENT_WRITE_METADATA, "newContents["+cicloMassive+"]", newContents[cicloMassive]);
			}
			authenticateOnRepository(context, null);
			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			for(int cicloMassive=0;cicloMassive<nodes.length;cicloMassive++){
				updateMetadataNoTransaction(nodes[cicloMassive], newContents[cicloMassive], context);
			}
			transaction.commit();

		} catch (EcmEngineFoundationException e) {
			if (e.getCode().equals(FoundationErrorCodes.DUPLICATE_CHILD_ERROR)) {
				logger.debug("[EcmEngineMassiveBean::massiveUpdateMetadata] Cannot create node. " +
						"Duplicate child: " + newContent.getPrefixedName());
				rollbackQuietely(transaction);
				throw new UpdateException("Cannot create node. Duplicate child: " + newContent.getPrefixedName());
			}

			checkCredentialsException(e, "EcmEngineManagementBean", "massiveUpdateMetadata", context.getUsername(), transaction);
			checkAccessException(e, "EcmEngineManagementBean", "massiveUpdateMetadata", "User: " + context.getUsername(), transaction);

			logger.error("[EcmEngineMassiveBean::massiveUpdateMetadata] Foundation services error: " + e.getCode());
			rollbackQuietely(transaction);
			throw new UpdateException("Backend services error: " + e.getCode());
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			this.<UpdateException>checkIntegrityException(
					e, "EcmEngineManagementBean", "massiveUpdateMetadata", UpdateException.class);

			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineMassiveBean::massiveUpdateMetadata] END");
		}
	}


	public ResultContentData[] massiveRetrieveContentData(Node[] nodes, Content[] contents, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException {
		ResultContentData [] response = null;
		UserTransaction transaction = null;
		logger.debug("[EcmEngineMassiveBean::massiveRetrieveContentData] BEGIN");
		start(); // Avvia stopwatch
		try {
			validate(ValidationType.NOT_NULL, "nodes"   , nodes    );
			validate(ValidationType.NOT_NULL, "contents", contents );
			validate(ValidationType.OPERATION_CONTEXT, "context", context);
			validate(ValidationType.IS_ZERO, "nodes size - newContents size", new Long(nodes.length-contents.length));
			validate(ValidationType.MASSIVE_RETRIEVE, "nodes size", nodes);
			for(int cicloMassive=0;cicloMassive<nodes.length;cicloMassive++){
				validate(ValidationType.NODE          , "nodes["   +cicloMassive+"]", nodes[   cicloMassive]);
				validate(ValidationType.CONTENT_READ  , "contents["+cicloMassive+"]", contents[cicloMassive]);
			}
			response=new ResultContentData[nodes.length];
			transaction = transactionService.getService().getNonPropagatingUserTransaction(true);
			transaction.begin();
			Long totalSize=new Long(0);
			for(int cicloMassive=0;cicloMassive<nodes.length;cicloMassive++){
				response[cicloMassive]=new ResultContentData();
				response[cicloMassive].setContent(retrieveContentDataNoTransaction(nodes[cicloMassive], contents[cicloMassive], context));
				totalSize=totalSize+( (response[cicloMassive].getContent()==null) ? 0 : response[cicloMassive].getContent().length );
				validate(ValidationType.MASSIVE_RETRIEVE_SIZE, "totalSize", totalSize);
			}
			transaction.commit();
		} catch (ReadException e) {
			logger.error("[EcmEngineManagementBean::massiveRetrieveContentData] Foundation services error: " + e);
			rollbackQuietely(transaction);
			throw new ReadException("Backend services error: " + e);
		} catch (InvalidParameterException ipe) {
			logger.error("[EcmEngineManagementBean::massiveRetrieveContentData] Invalid parameter error: " + ipe.getMessage());
			rollbackQuietely(transaction);
			throw ipe;
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		} finally {
			stop(); // Ferma stopwatch
			logger.debug("[EcmEngineMassiveBean::massiveRetrieveContentData] END");
		}
		return response;
	}

	public ResultContent[] massiveGetContentMetadata(Node[] nodes, OperationContext context) throws InvalidParameterException, NoSuchNodeException, ReadException, InvalidCredentialsException, PermissionDeniedException, EcmEngineTransactionException, RemoteException {
		ResultContent[] result=null;
		UserTransaction transaction=null;
		int ciclomassive=0;
		logger.debug("[EcmEngineMassiveBean::massiveGetContentMetadata] BEGIN");
		start();
		try{
			validate(ValidationType.NOT_NULL, "nodes", nodes);
			validate(ValidationType.OPERATION_CONTEXT, "context", context);
			validate(ValidationType.MASSIVE_GET_METADATA,"nodes size",new Long(nodes.length));
			for(int i=0;i<nodes.length;i++){
				validate(ValidationType.NODE, "nodes["+i+"]", nodes[i]);
			}

			transaction = transactionService.getService().getNonPropagatingUserTransaction();
			transaction.begin();
			result=new ResultContent[nodes.length];
			for(ciclomassive=0;ciclomassive<nodes.length;ciclomassive++){
				result[ciclomassive]=getContentMetadataNoTransaction(nodes[ciclomassive], context);
			}
			transaction.commit();
		} catch (EcmEngineException e) {
			logger.error("[EcmEngineManagementBean::massiveGetContentMetadata] Foundation services error: " + e);
			rollbackQuietely(transaction);
			throw new ReadException("Backend services error: "+e);
		} catch (SecurityException e) {
			handleTransactionException(e, "security violation.");
		} catch (IllegalStateException e) {
			handleTransactionException(e, e.getMessage());
		} catch (RollbackException e) {
			handleTransactionException(e, "transaction rolled-back.");
		} catch (HeuristicMixedException e) {
			handleTransactionException(e, "transaction rolled-back (partial, heuristic).");
		} catch (HeuristicRollbackException e) {
			handleTransactionException(e, "transaction rolled-back (heuristic).");
		} catch (SystemException e) {
			handleTransactionException(e, "system error.");
		} catch (NotSupportedException e) {
			handleTransactionException(e, "not supported.");
		}finally{
			logger.debug("[EcmEngineMassiveBean::massiveGetContentMetadata] END");
			stop();
		}
		return result;
	}
}
