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

package it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene;

import it.doqui.index.ecmengine.business.personalization.multirepository.RepositoryManager;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts.RepositoryAwareFullTextSearchIndexer;
import it.doqui.index.ecmengine.business.personalization.multirepository.index.lucene.fts.RepositoryFTSIndexerAware;
import it.doqui.index.ecmengine.business.personalization.multirepository.util.EcmEngineMultirepositoryConstants;
import it.doqui.index.fileformat.business.service.FileFormatServiceImpl;
import it.doqui.index.fileformat.exception.FileFormatException;
import it.doqui.index.ecmengine.business.personalization.splitting.index.lucene.M7mHandler;
import it.doqui.index.ecmengine.business.personalization.splitting.index.lucene.P7mHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.util.SimplePath;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.search.IndexMode;
import org.alfresco.repo.search.impl.lucene.LuceneConfig;
import org.alfresco.repo.search.impl.lucene.LuceneIndexException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.TempFileProvider.TempFileCleanerJob;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Searcher;

public class RepositoryAwareAVMLuceneIndexerImpl
extends RepositoryAwareAbstractLuceneIndexerImpl<String>
implements RepositoryAwareAVMLuceneIndexer {

	private File m7mTempFile,p7mTempFile;
	
	private enum IndexChannel {
		MAIN, DELTA;
	}

	private static String SNAP_SHOT_ID = "SnapShot";

	static Logger s_logger = Logger.getLogger(EcmEngineMultirepositoryConstants.MULTIREPOSITORY_INDEX_LOG_CATEGORY);

	private AVMService avmService;

	private AVMSyncService avmSyncService;

	@SuppressWarnings("unused")
	private ContentStore contentStore;

	private ContentService contentService;

	private RepositoryFTSIndexerAware callBack;

	private RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer;

	private FileFormatServiceImpl fileformatService;

	private int remainingCount;

	private int startVersion = -1;

	private int endVersion = -1;

	/**
	 * Set the AVM Service
	 *
	 * @param avmService
	 */
	public void setAvmService(AVMService avmService) {
		this.avmService = avmService;
	}

	/**
	 * Set the AVM sync service
	 *
	 * @param avmSyncService
	 */
	public void setAvmSyncService(AVMSyncService avmSyncService) {
		this.avmSyncService = avmSyncService;
	}

	/**
	 * Set the content service
	 *
	 * @param contentStore
	 */
	public void setContentStore(ContentStore contentStore) {
		this.contentStore = contentStore;
	}

	/**
	 * @param contentService
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setFileformatService(FileFormatServiceImpl fileformatService) {
		this.fileformatService = fileformatService;
	}

	/**
	 * Generate an indexer
	 *
	 * @param storeRef
	 * @param deltaId
	 * @param config
	 * @return - the indexer instance
	 * @throws LuceneIndexException
	 */
	public static RepositoryAwareAVMLuceneIndexerImpl getUpdateIndexer(StoreRef storeRef, String deltaId, LuceneConfig config, String repository)
	throws LuceneIndexException {
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::getUpdateIndexer] " +
					"Repository '" + repository + "' -- " +
			"Creating indexer");
		}
		RepositoryAwareAVMLuceneIndexerImpl indexer = new RepositoryAwareAVMLuceneIndexerImpl();
		indexer.setLuceneConfig(config);
		indexer.initialise(storeRef, deltaId, repository);

		return indexer;
	}

	/**
	 * Index a specified change to a store
	 *
	 * @param store
	 * @param srcVersion
	 * @param dstVersion
	 */
	public void index(String store, int srcVersion, int dstVersion, IndexMode mode) {
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			switch (mode) {
			case ASYNCHRONOUS:
				asynchronousIndex(store, srcVersion, dstVersion);
				break;
			case SYNCHRONOUS:
				synchronousIndex(store, srcVersion, dstVersion);
				break;
			case UNINDEXED:
				// nothing to do
					break;
			}
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Snapshot index failed", e);
		}
	}

	private void asynchronousIndex(String store, int srcVersion, int dstVersion) {
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("Async index for " + store + " from " + srcVersion + " to " + dstVersion);
		}
		index("\u0000BG:STORE:" + store + ":" + srcVersion + ":" + dstVersion + ":" + GUID.generate());
		fullTextSearchIndexer.requiresIndex(AVMNodeConverter.ToStoreRef(store), getRepository());
	}

	private void synchronousIndex(String store, int srcVersion, int dstVersion) {
		if (startVersion == -1) {
			startVersion = srcVersion;
		}

		endVersion = dstVersion;

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::synchronousIndex] " +
					"Repository '" + getRepository() + "' -- " +
					"Sync index for " + store + " from " + srcVersion + " to " + dstVersion);
		}
		String path = store + ":/";
		List<AVMDifference> changeList = avmSyncService.compare(srcVersion, path, dstVersion, path, null);
		for (AVMDifference difference : changeList) {
			switch (difference.getDifferenceCode()) {
			case AVMDifference.CONFLICT:
			case AVMDifference.NEWER:
			case AVMDifference.OLDER:
				AVMNodeDescriptor srcDesc = avmService.lookup(difference.getSourceVersion(), difference.getSourcePath(), true);
				AVMNodeDescriptor dstDesc = avmService.lookup(difference.getDestinationVersion(), difference.getDestinationPath(), true);

				// New
				if (srcDesc == null) {
					index(difference.getDestinationPath());
					if (dstDesc.isDirectory()) {
						indexDirectory(dstDesc);
					}
					reindexAllAncestors(difference.getDestinationPath());
				} else if (!srcDesc.isDeleted() && ((dstDesc == null) || dstDesc.isDeleted())) {
					// New Delete

					delete(difference.getSourcePath());
					delete(difference.getDestinationPath());
					reindexAllAncestors(difference.getDestinationPath());
				} else if (srcDesc.isDeleted() && dstDesc.isDeleted()) {
					// Existing delete
					// Nothing to do for this case
				} else {
					// Anything else then we reindex

					if (!difference.getSourcePath().equals(difference.getDestinationPath())) {
						reindex(difference.getSourcePath(), srcDesc.isDirectory());
						reindex(difference.getDestinationPath(), dstDesc.isDirectory());
						reindexAllAncestors(difference.getSourcePath());
						reindexAllAncestors(difference.getDestinationPath());
					} else {
						// If it is a directory, it is at the same path,
						// so no cascade update is required for the bridge table data.
						reindex(difference.getDestinationPath(), false);
						reindexAllAncestors(difference.getDestinationPath());
					}
				}
				break;
			case AVMDifference.DIRECTORY:
				// Never seen
				break;
			case AVMDifference.SAME:
				// No action
				break;
			}
		}
		// record the snapshot id
		reindex(SNAP_SHOT_ID + ":" + store + ":" + srcVersion + ":" + dstVersion, false);
	}

	/*
	 * Nasty catch all fix up (as changes imply the parents may all have changed
	 */
	private void reindexAllAncestors(String destinationPath) {
		String[] splitPath = splitPath(destinationPath);
		String store = splitPath[0];
		String pathInStore = splitPath[1];
		SimplePath simplePath = new SimplePath(pathInStore);

		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(store).append(":/");
		reindex(pathBuilder.toString(), false);
		boolean requiresSep = false;
		for (int i = 0; i < simplePath.size() - 1; i++) {
			if (requiresSep) {
				pathBuilder.append("/");
			} else {
				requiresSep = true;
			}
			pathBuilder.append(simplePath.get(i));
			reindex(pathBuilder.toString(), false);
		}
	}

	private void indexDirectory(AVMNodeDescriptor dir) {
		Map<String, AVMNodeDescriptor> children = avmService.getDirectoryListing(dir);
		for (AVMNodeDescriptor child : children.values()) {
			index(child.getPath());
			if (child.isDirectory()) {
				indexDirectory(child);
			}
		}

	}

	@Override
	protected List<Document> createDocuments(String stringNodeRef, boolean isNew,
			boolean indexAllProperties, boolean includeDirectoryDocuments) {
		List<Document> docs = new ArrayList<Document>();
		if (stringNodeRef.startsWith("\u0000")) {
			Document idoc = new Document();
			idoc.add(new Field("ID", stringNodeRef, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			docs.add(idoc);
			return docs;
		} else if (stringNodeRef.startsWith(SNAP_SHOT_ID)) {
			Document sdoc = new Document();
			sdoc.add(new Field("ID", stringNodeRef, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			docs.add(sdoc);
			return docs;
		}

		AVMNodeDescriptor desc = avmService.lookup(endVersion, stringNodeRef);
		if (desc == null) {
			return docs;
		}

		if (desc.isLayeredDirectory() || desc.isLayeredFile()) {
			return docs;
		}

		// Naughty, Britt should come up with a fix that doesn't require this.
		AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(desc.getId());

		if (desc != null) {

			NodeRef nodeRef = AVMNodeConverter.ToNodeRef(endVersion, stringNodeRef);

			Document xdoc = new Document();
			// TODO: id settato due volte?
			xdoc.add(new Field("ID", nodeRef.toString(), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			xdoc.add(new Field("ID", stringNodeRef, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			xdoc.add(new Field("TX", AlfrescoTransactionSupport.getTransactionId(), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

			boolean isAtomic = true;

			Map<QName, Serializable> properties = getIndexableProperties(desc, nodeRef, endVersion, stringNodeRef);
			for (QName propertyName : properties.keySet()) {
				Serializable value = properties.get(propertyName);
				if (indexAllProperties) {
					indexProperty(nodeRef, propertyName, value, xdoc, false, properties);
				} else {
					isAtomic &= indexProperty(nodeRef, propertyName, value, xdoc, true, properties);
				}
			}

			StringBuilder qNameBuffer = new StringBuilder(64);
			if (node.getIsRoot()) {

			} else { 	// pseudo roots?
					String[] splitPath = splitPath(stringNodeRef);
			String store = splitPath[0];
			String pathInStore = splitPath[1];
			SimplePath simplePath = new SimplePath(pathInStore);

			StringBuilder xpathBuilder = new StringBuilder();
			for (int i = 0; i < simplePath.size(); i++) {
				xpathBuilder.append("/{}").append(simplePath.get(i));
			}
			String xpath = xpathBuilder.toString();

			if (qNameBuffer.length() > 0) {
				qNameBuffer.append(";/");
			}
			// Get the parent

			ArrayList<String> ancestors = new ArrayList<String>();

			StringBuilder pathBuilder = new StringBuilder();
			pathBuilder.append(store).append(":/");
			ancestors.add(pathBuilder.toString());
			boolean requiresSep = false;
			for (int i = 0; i < simplePath.size() - 1; i++) {
				if (requiresSep) {
					pathBuilder.append("/");
				} else {
					requiresSep = true;
				}
				pathBuilder.append(simplePath.get(i));
				ancestors.add(pathBuilder.toString());
			}

			qNameBuffer.append(ISO9075.getXPathName(QName.createQName("", simplePath.get(simplePath.size() - 1))));
			xdoc.add(new Field("PARENT", ancestors.get(ancestors.size() - 1), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
			// TODO: Categories and LINKASPECT

			if (includeDirectoryDocuments) {
				if (desc.isDirectory()) {
					// TODO: Exclude category paths

					Document directoryEntry = new Document();
					directoryEntry.add(new Field("ID", nodeRef.toString(), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

					directoryEntry.add(new Field("ID", stringNodeRef, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

					directoryEntry.add(new Field("PATH", xpath, Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));

					// Find all parent nodes.

					for (String toAdd : ancestors) {
						directoryEntry.add(new Field("ANCESTOR", toAdd, Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
					}
					directoryEntry.add(new Field("ISCONTAINER", "T", Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

					docs.add(directoryEntry);
				}
			}
			}

			if (node.getIsRoot()) {
				// TODO: Does the root element have a QName?
				xdoc.add(new Field("ISCONTAINER", "T", Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				xdoc.add(new Field("PATH", "", Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
				xdoc.add(new Field("QNAME", "", Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));
				xdoc.add(new Field("ISROOT", "T", Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				xdoc.add(new Field("ISNODE", "T", Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				docs.add(xdoc);

			} else {	// not a root node
				xdoc.add(new Field("QNAME", qNameBuffer.toString(), Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.NO));

				QName typeQName = getType(desc);

				xdoc.add(new Field("TYPE", ISO9075.getXPathName(typeQName), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

				for (QName classRef : avmService.getAspects(desc)) {
					xdoc.add(new Field("ASPECT", ISO9075.getXPathName(classRef), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				}

				xdoc.add(new Field("ISROOT", "F", Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				xdoc.add(new Field("ISNODE", "T", Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

				docs.add(xdoc);
			}
		} else {
			boolean root = node.getIsRoot();
			boolean deleted = desc.isDeleted();
			System.out.println("Is Root " + root);
			System.out.println("Is deleted " + deleted);
		}
		return docs;
	}

	private String[] splitPath(String path) {
		String[] pathParts = path.split(":");
		if (pathParts.length != 2) {
			throw new AVMException("Invalid path: " + path);
		}
		return pathParts;
	}

	private QName getType(AVMNodeDescriptor desc) {
		if (desc.isPlainDirectory()) {
			return WCMModel.TYPE_AVM_PLAIN_FOLDER;
		} else if (desc.isPlainFile()) {
			return WCMModel.TYPE_AVM_PLAIN_CONTENT;
		} else if (desc.isLayeredDirectory()) {
			return WCMModel.TYPE_AVM_LAYERED_FOLDER;
		} else {
			return WCMModel.TYPE_AVM_LAYERED_CONTENT;
		}
	}

	private Map<QName, Serializable> getIndexableProperties(AVMNodeDescriptor desc, NodeRef nodeRef,
			Integer version, String path) {
		Map<QName, PropertyValue> properties = avmService.getNodeProperties(desc);

		Map<QName, Serializable> result = new HashMap<QName, Serializable>();
		for (QName qName : properties.keySet()) {
			PropertyValue value = properties.get(qName);
			PropertyDefinition def = getDictionaryService().getProperty(qName);
			result.put(qName, makeSerializableValue(def, value));
		}

		// Now spoof properties that are built in.
		result.put(ContentModel.PROP_CREATED, new Date(desc.getCreateDate()));
		result.put(ContentModel.PROP_CREATOR, desc.getCreator());
		result.put(ContentModel.PROP_MODIFIED, new Date(desc.getModDate()));
		result.put(ContentModel.PROP_MODIFIER, desc.getLastModifier());
		result.put(ContentModel.PROP_OWNER, desc.getOwner());
		result.put(ContentModel.PROP_NAME, desc.getName());
		result.put(ContentModel.PROP_NODE_UUID, "UNKNOWN");
		result.put(ContentModel.PROP_NODE_DBID, new Long(desc.getId()));
		result.put(ContentModel.PROP_STORE_PROTOCOL, "avm");
		result.put(ContentModel.PROP_STORE_IDENTIFIER, nodeRef.getStoreRef().getIdentifier());
		if (desc.isLayeredDirectory())
		{
			result.put(WCMModel.PROP_AVM_DIR_INDIRECTION, AVMNodeConverter.ToNodeRef(endVersion, desc.getIndirection()));
		}
		if (desc.isLayeredFile())
		{
			result.put(WCMModel.PROP_AVM_FILE_INDIRECTION, AVMNodeConverter.ToNodeRef(endVersion, desc.getIndirection()));
		}
		if (desc.isFile())
		{
			try
			{
				ContentData contentData = null;
				if (desc.isPlainFile())
				{
					contentData = avmService.getContentDataForRead(desc);
				}
				else
				{
					contentData = avmService.getContentDataForRead(endVersion, path);
				}
				result.put(ContentModel.PROP_CONTENT, contentData);
			}
			catch (AVMException e)
			{
				// TODO For now ignore.
			}
		}
		return result;

	}

	protected Serializable makeSerializableValue(PropertyDefinition propertyDef, PropertyValue propertyValue)
	{
		if (propertyValue == null)
		{
			return null;
		}
		// get property attributes
		QName propertyTypeQName = null;
		if (propertyDef == null)
		{
			// allow this for now
			propertyTypeQName = DataTypeDefinition.ANY;
		}
		else
		{
			propertyTypeQName = propertyDef.getDataType().getName();
		}
		try
		{
			Serializable value = propertyValue.getValue(propertyTypeQName);
			// done
			return value;
		}
		catch (TypeConversionException e)
		{
			throw new TypeConversionException("The property value is not compatible with the type defined for the property: \n"
					+ "   property: " + (propertyDef == null ? "unknown" : propertyDef) + "\n" + "   property value: " + propertyValue, e);
		}
	}

	protected boolean indexProperty(NodeRef banana, QName propertyName, Serializable value, Document doc, boolean indexAtomicPropertiesOnly, Map<QName, Serializable> properties) {
		String attributeName = "@" + QName.createQName(propertyName.getNamespaceURI(), ISO9075.encode(propertyName.getLocalName()));

		boolean store = true;
		boolean index = true;
		boolean tokenise = true;
		@SuppressWarnings("unused")
		boolean atomic = true;
		boolean isContent = false;
		boolean isMultiLingual = false;
		boolean isText = false;


		PropertyDefinition propertyDef = getDictionaryService().getProperty(propertyName);
		if (propertyDef != null) {
			index = propertyDef.isIndexed();
			store = propertyDef.isStoredInIndex();
			tokenise = propertyDef.isTokenisedInIndex();
			atomic = propertyDef.isIndexedAtomically();
			isContent = propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT);
			isMultiLingual = propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT);
			isText = propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT);





		}

		if (value == null) {
			// the value is null
			return true;
			//} else if (indexAtomicPropertiesOnly && !atomic) {
			// we are only doing atomic properties and the property is definitely non-atomic
			//    return false;
		}

		if (!indexAtomicPropertiesOnly) {
			doc.removeFields(propertyName.toString());
		}
		// boolean wereAllAtomic = true;

		// convert value to String
		for (Serializable serializableValue : DefaultTypeConverter.INSTANCE.getCollection(Serializable.class, value)) {
			String strValue = null;
			try {
				strValue = DefaultTypeConverter.INSTANCE.convert(String.class, serializableValue);
			} catch (TypeConversionException e) {
				doc.add(new Field(attributeName, NOT_INDEXED_NO_TYPE_CONVERSION, Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				continue;
			}

			if (strValue == null) {
				// nothing to index
				continue;
			}

			if (isContent) {
				ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, serializableValue);
				if (!index || contentData.getMimetype() == null) {
					// no mimetype or property not indexed
					continue;
				}
				// store mimetype in index - even if content does not index it is useful
				// Added size and locale - size needs to be tokenised correctly
				doc.add(new Field(attributeName + ".mimetype", contentData.getMimetype(), Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
				doc.add(new Field(attributeName + ".size", Long.toString(contentData.getSize()), Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));

				// TODO: Use the node locale in preferanced to the system locale
				Locale locale = contentData.getLocale();
				// No default locale in AVM





				if (locale == null) {
					locale = I18NUtil.getLocale();
				}
				doc.add(new Field(attributeName + ".locale", locale.toString().toLowerCase(), Field.Store.NO, Field.Index.UN_TOKENIZED, Field.TermVector.NO));

				ContentReader reader = null;
				try
				{
					reader = contentService.getRawReader(contentData.getContentUrl());
					reader.setEncoding(contentData.getEncoding());
					reader.setLocale(contentData.getLocale());
					reader.setMimetype(contentData.getMimetype());
				}
				catch (Exception e)
				{
					reader = null;
				}
				// ContentReader reader = contentService.getReader(banana, propertyName);
				if (reader != null && reader.exists()) {
					boolean readerReady = true;
					boolean isP7mText = false;

					// transform if necessary (it is not a UTF-8 text document)
					if (!EqualsHelper.nullSafeEquals(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN) || !EqualsHelper.nullSafeEquals(reader.getEncoding(), "UTF-8")) {

						boolean isM7m = false;
						byte[] bytes_sbustati_m7m=null;

						// Verifica di M7M
						// M7M e' un formato DIKE che contiene un P7M
						if(reader.getMimetype().equals("application/m7m-mime")){

							// Trasforma il contenuto in ByteArray
							ByteArrayOutputStream baos=new ByteArrayOutputStream();
							reader.getContent(baos);
							bytes_sbustati_m7m = M7mHandler.sbusta_m7m(baos.toByteArray());
							try{ baos.close();} catch(IOException e){}

							if( bytes_sbustati_m7m!=null ){
								// Se riesco a sbustare un dato, allora e' un M7M valido
								isM7m=true;

								// Scrivo il contenuto in un file temporaneo
								m7mTempFile = TempFileProvider.createTempFile("getSafeContentReader_", ".txt");
								ContentWriter writer = new FileContentWriter(m7mTempFile);
								InputStream ris = new ByteArrayInputStream(bytes_sbustati_m7m);
								writer.putContent(ris);
								try{ ris.close();} catch(IOException e){}

								// Prende il reader e imposta il nuovo mimetype
								reader = writer.getReader();
								reader.setMimetype("application/x-pkcs7-mime");
							}
						}

						// Verifica di P7M
						if(reader.getMimetype().equals("application/x-pkcs7-mime")){

							// Prendo il contenuto binario del file
							byte[] bytes_originali;
							if( isM7m ){
								bytes_originali = bytes_sbustati_m7m;
							} else {
								ByteArrayOutputStream baos=new ByteArrayOutputStream();
								reader.getContent(baos);
								bytes_originali = baos.toByteArray();
								try{ baos.close();} catch(IOException e){}
							}

							// Sbusto dalla busta P7M
							byte[] bytes_sbustati = P7mHandler.sbusta(bytes_originali);
							if(bytes_sbustati!=null){
								// Identifico il tipo di file
								InputStream imp = new ByteArrayInputStream(bytes_sbustati);
								it.doqui.index.fileformat.dto.FileFormatInfo[] arrayFFinfo=null;
								try{
									arrayFFinfo = fileformatService.getFileFormatInfo("temp.p7m", imp);
								} catch( FileFormatException e ){
									arrayFFinfo = new it.doqui.index.fileformat.dto.FileFormatInfo[0];
									s_logger.error("[MultiRepositorySplittingADMIndexerImpl::indexProperty] FileFormat service error: skip recognition and try to read content as a plain/text file.");
								}

								// verifico se è stato trovato un solo mimetype con risultato positivo, se sì ne prendo la stringa e la setto nel reader
								if (arrayFFinfo.length==1 && arrayFFinfo[0].getTypeDescription().equals("Positive (Specific Format)")){
									String mimetype_sbustato = arrayFFinfo[0].getMimeType();
									reader.setMimetype(mimetype_sbustato);

									// Scrivo il contenuto in un file temporaneo
									p7mTempFile = TempFileProvider.createTempFile("getSafeContentReader_", ".txt");
									ContentWriter writer = new FileContentWriter(p7mTempFile);
									InputStream ris = new ByteArrayInputStream(bytes_sbustati);
									writer.putContent(ris);
									try{ ris.close();} catch(IOException e){}

									// Prende il reader e imposta il nuovo mimetype
									reader = writer.getReader();
									reader.setMimetype(mimetype_sbustato);

									// se il mimetype non si trova (si presuppone che sia text/plain) creado un ByteArrayInputStream dall'array di byte.
									// Questo ByteArrayInputStream viene fatto leggere all'InputStreamReader.
								} else {
									isP7mText=true; // così vengono saltate alcune parti di codice, una volta usciti da questo else
									addFieldContent( doc, locale, new ByteArrayInputStream(bytes_sbustati), attributeName );
								}
							}
						}
						if (!isP7mText){
							// get the transformer
							ContentTransformer transformer = contentService.getTransformer(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN);

							// is this transformer good enough?
							if (transformer == null) {
								// log it
								if (s_logger.isDebugEnabled()) {
									s_logger.debug("Not indexed: No transformation: \n" + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN);
								}

								// don't index from the reader
								readerReady = false;

								// not indexed: no transformation
								// doc.add(new Field("TEXT", NOT_INDEXED_NO_TRANSFORMATION, Field.Store.NO,
								// Field.Index.TOKENIZED, Field.TermVector.NO));
								doc.add(new Field(attributeName, NOT_INDEXED_NO_TRANSFORMATION, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
								//} else if (indexAtomicPropertiesOnly && transformer.getTransformationTime() > maxAtomicTransformationTime) {
								// only indexing atomic properties
								// indexing will take too long, so push it to the background
								//wereAllAtomic = false;
							} else {
								// We have a transformer that is fast enough
								ContentWriter writer = contentService.getTempWriter();
								writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
								// this is what the analyzers expect on the stream
								writer.setEncoding("UTF-8");
								try {

									transformer.transform(reader, writer);
									// point the reader to the new-written content
									reader = writer.getReader();





								} catch (ContentIOException e) {
									// log it
									if (s_logger.isDebugEnabled()) {
										s_logger.debug("Not indexed: Transformation failed", e);
									}
									// don't index from the reader
									readerReady = false;
									// not indexed: transformation
									// failed
									// doc.add(new Field("TEXT", NOT_INDEXED_TRANSFORMATION_FAILED, Field.Store.NO,
									// Field.Index.TOKENIZED, Field.TermVector.NO));
									doc.add(new Field(attributeName, NOT_INDEXED_TRANSFORMATION_FAILED, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
								}
							}
						}
					}
					if (!isP7mText){
						// add the text field using the stream from the
						// reader, but only if the reader is valid
						if (readerReady) {










							addFieldContent( doc, locale, reader.getReader().getContentInputStream(), attributeName );
						}
					}
				} else {
					// URL not present (null reader) or no content at the URL (file missing)
					// log it
					if (s_logger.isDebugEnabled()) {
						s_logger.debug("Not indexed: Content Missing \n" + "   node: " + banana + "\n" + "   reader: " + reader + "\n" + "   content exists: "
								+ (reader == null ? " --- " : Boolean.toString(reader.exists())));
					}
					// not indexed: content missing
					doc.add(new Field("TEXT", NOT_INDEXED_CONTENT_MISSING, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
					doc.add(new Field(attributeName, NOT_INDEXED_CONTENT_MISSING, Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
				}
			} else {
				Field.Store fieldStore = store ? Field.Store.YES : Field.Store.NO;
				Field.Index fieldIndex;

				if (index)
				{
					if (tokenise)
					{
						fieldIndex = Field.Index.TOKENIZED;
					}
					else
					{
						fieldIndex = Field.Index.UN_TOKENIZED;
					}
				}
				else
				{
					fieldIndex = Field.Index.NO;
				}

				if ((fieldIndex != Field.Index.NO) || (fieldStore != Field.Store.NO))
				{
					if (isMultiLingual)
					{
						MLText mlText = DefaultTypeConverter.INSTANCE.convert(MLText.class, serializableValue);
						for (Locale locale : mlText.getLocales())
						{
							String localeString = mlText.getValue(locale);
							StringBuilder builder = new StringBuilder();
							builder.append("\u0000").append(locale.toString()).append("\u0000").append(localeString);
							doc.add(new Field(attributeName, builder.toString(), fieldStore, fieldIndex, Field.TermVector.NO));
						}
					}
					else if (isText)
					{
						// Temporary special case for uids and gids
						if (propertyName.equals(ContentModel.PROP_USER_USERNAME)
								|| propertyName.equals(ContentModel.PROP_USERNAME) || propertyName.equals(ContentModel.PROP_AUTHORITY_NAME)
								|| propertyName.equals(ContentModel.PROP_MEMBERS))
						{
							doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));
						}

						// TODO: Use the node locale in preferanced to the system locale
						Locale locale = null;

						Serializable localeProperty = properties.get(ContentModel.PROP_LOCALE);
						if (localeProperty != null)
						{
							locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, localeProperty);
						}

						if (locale == null)
						{
							locale = I18NUtil.getLocale();
						}
						if (tokenise)
						{
							StringBuilder builder = new StringBuilder();
							builder.append("\u0000").append(locale.toString()).append("\u0000").append(strValue);
							doc.add(new Field(attributeName, builder.toString(), fieldStore, fieldIndex, Field.TermVector.NO));
						}
						else
						{
							doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));
						}
					}
					else
					{
						doc.add(new Field(attributeName, strValue, fieldStore, fieldIndex, Field.TermVector.NO));
					}
				}
			}
		}
		if(m7mTempFile!=null){
			m7mTempFile.delete();
		}
		if(p7mTempFile!=null){
			p7mTempFile.delete();
		}
		// return wereAllAtomic;
		return true;
	}

	@Override
	protected void doPrepare() throws IOException
	{
		saveDelta();
		flushPending();
	}

	@Override
	protected void doCommit() throws IOException
	{
		if (indexUpdateStatus == IndexUpdateStatus.ASYNCHRONOUS)
		{
			setInfo(docs, getDeletions(), false);
			// FTS does not trigger indexing request
		}
		else
		{
			setInfo(docs, getDeletions(), false);
			// TODO: only register if required
			fullTextSearchIndexer.requiresIndex(store, getRepository());
		}
		if (callBack != null)
		{
			callBack.indexCompleted(store, getRepository(), remainingCount, null);
		}

		setInfo(docs, deletions, false);
	}

	@Override
	protected void doRollBack() throws IOException
	{
		if (callBack != null)
		{
			callBack.indexCompleted(store, getRepository(), 0, null);
		}
	}

	@Override
	protected void doSetRollbackOnly() throws IOException
	{

	}

	// The standard indexer API - although implemented it is not likely to be used at the moment
	// Batch indexing makes more sense for AVM at snapshot time

	public void createNode(ChildAssociationRef relationshipRef) {
		final String repository = RepositoryManager.getCurrentRepository();

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::createNode] " +
					"Repository '" + repository + "' -- " +
					"Create node: " + relationshipRef.getChildRef());
		}
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			NodeRef childRef = relationshipRef.getChildRef();
			Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
			index(versionPath.getSecond());
			// TODO: Deal with a create on the root node deleting the index.
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Create node failed", e);
		}
	}

	public void updateNode(NodeRef nodeRef) {
		updateNode(nodeRef,false);
	}

	public void updateNode(NodeRef nodeRef, boolean cascade) {
		final String repository = RepositoryManager.getCurrentRepository();

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::updateNode] " +
					"Repository '" + repository +
					"' -- Update node: " + nodeRef);
		}
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(nodeRef);
			reindex(versionPath.getSecond(), cascade);
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Update node failed", e);
		}
	}

	public void deleteNode(ChildAssociationRef relationshipRef) {
		final String repository = RepositoryManager.getCurrentRepository();

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::deleteNode] " +
					"Repository '" + repository + "' -- " +
					"Delete node: " + relationshipRef.getChildRef());
		}
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			NodeRef childRef = relationshipRef.getChildRef();
			Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
			reindex(versionPath.getSecond(), true);
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Delete node failed", e);
		}
	}

	public void createChildRelationship(ChildAssociationRef relationshipRef) {
		final String repository = RepositoryManager.getCurrentRepository();

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::createChildRelationship] " +
					"Repository '" + repository + "' -- " +
					"Create child: " + relationshipRef);
		}
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			NodeRef childRef = relationshipRef.getChildRef();
			Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
			reindex(versionPath.getSecond(), true);
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Failed to create child relationship", e);
		}
	}

	public void updateChildRelationship(ChildAssociationRef relationshipBeforeRef,
			ChildAssociationRef relationshipAfterRef) {
		final String repository = RepositoryManager.getCurrentRepository();

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::updateChildRelationship] " +
					"Repository '" + repository + "' -- " +
					"Update child: " + relationshipBeforeRef + " to " + relationshipAfterRef);
		}
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			NodeRef childRef = relationshipBeforeRef.getChildRef();
			Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
			reindex(versionPath.getSecond(), true);
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Failed to update child relationship", e);
		}
	}

	public void deleteChildRelationship(ChildAssociationRef relationshipRef) {
		final String repository = RepositoryManager.getCurrentRepository();

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::deleteChildRelationship] " +
					"Repository '" + repository + "' -- " +
					"Delete child: " + relationshipRef);
		}
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			NodeRef childRef = relationshipRef.getChildRef();
			Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(childRef);
			reindex(versionPath.getSecond(), true);
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Failed to delete child relationship", e);
		}
	}

	public void deleteIndex(String store, IndexMode mode) {
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			switch (mode) {
			case ASYNCHRONOUS:
				asyncronousDeleteIndex(store);
				break;
			case SYNCHRONOUS:
				syncronousDeleteIndex(store);
				break;
			case UNINDEXED:
				// nothing to do
					break;
			}
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Delete index failed", e);
		}
	}

	/**
	 * Sync delete of this index
	 *
	 * @param store
	 */
	public void syncronousDeleteIndex(String store) {

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::synchronousDeleteIndex]" +
					"Repository '" + getRepository() + "' -- " +
					"Sync delete for store: " + store);
		}
		deleteAll(); // TODO: ???
	}

	/**
	 * Support to delete all entries from the index in the background
	 *
	 * @param store
	 */
	public void asyncronousDeleteIndex(String store) {
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::asynchronousDeleteIndex] " +
					"Repository '" + getRepository() + "' -- " +
					"Async delete for store: " + store);
		}
		index("\u0000BG:DELETE:" + store + ":" + GUID.generate());
		fullTextSearchIndexer.requiresIndex(AVMNodeConverter.ToStoreRef(store), getRepository());
	}

	public void createIndex(String store, IndexMode mode) {
		checkAbleToDoWork(IndexUpdateStatus.SYNCRONOUS);
		try {
			switch (mode) {
			case ASYNCHRONOUS:
				asyncronousCreateIndex(store);
				break;
			case SYNCHRONOUS:
				syncronousCreateIndex(store);
				break;
			case UNINDEXED:
				// nothing to do
					break;
			}
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Create index failed", e);
		}
	}

	/**
	 * Sync create index
	 *
	 * @param store
	 */
	public void syncronousCreateIndex(String store) {

		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::synchronousCreateIndex]" +
					"Repository '" + getRepository() + "' -- " +
					"Sync create for " + store);
		}
		@SuppressWarnings("unused")
		AVMNodeDescriptor rootDesc = avmService.getStoreRoot(-1, store);
		index(store + ":/");
	}

	/**
	 * Asynchronously create index.
	 *
	 * @param store
	 */
	public void asyncronousCreateIndex(String store) {
		if (s_logger.isDebugEnabled()) {
			s_logger.debug("[RepositoryAwareAVMLuceneIndexerImpl::asyncronousCreateIndex]" +
					"Repository '" + getRepository() + "' -- " +
					"Async create for " + store);
		}
		index("\u0000BG:CREATE:" + store + ":" + GUID.generate());
		fullTextSearchIndexer.requiresIndex(AVMNodeConverter.ToStoreRef(store), getRepository());
	}

	public void registerCallBack(RepositoryFTSIndexerAware callBack) {
		this.callBack = callBack;
	}

	public int updateFullTextSearch(int size) {

		checkAbleToDoWork(IndexUpdateStatus.ASYNCHRONOUS);

		try {
			PrefixQuery query = new PrefixQuery(new Term("ID", "\u0000BG:"));

			String action = null;

			Searcher searcher = null;
			try {
				searcher = getSearcher(null);
				// commit on another thread - appears like there is no index ...try later
				if (searcher == null) {
					remainingCount = size;
					return 0;
				}
				Hits hits;
				try {
					hits = searcher.search(query);
				} catch (IOException e) {
					throw new LuceneIndexException("Failed to execute query to find content which needs updating in the index", e);
				}

				if (hits.length() > 0) {
					Document doc = hits.doc(0);
					action = doc.getField("ID").stringValue();
					String[] split = action.split(":");
					if (split[1].equals("DELETE")) {
						deleteAll("\u0000BG:");
//						service.deleteAll("\u0000BG:",deletions);
					} else if (split[1].equals("CREATE")) {
						syncronousCreateIndex(split[2]);
					} else if (split[1].equals("STORE")) {
						synchronousIndex(split[2], Integer.parseInt(split[3]), Integer.parseInt(split[4]));
					}
					deletions.add(action);
					remainingCount = hits.length() - 1;
					return 1;
				} else {
					remainingCount = 0;
					return 0;
				}

			} finally {
				if (searcher != null) {
					try {
						searcher.close();
					} catch (IOException e) {
						throw new LuceneIndexException("Failed to close searcher", e);
					}
				}
			}
		} catch (IOException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Failed FTS update", e);
		} catch (LuceneIndexException e) {
			setRollbackOnly();
			throw new LuceneIndexException("Failed FTS update", e);
		}
	}

	public void setFullTextSearchIndexer(RepositoryAwareFullTextSearchIndexer fullTextSearchIndexer) {
		this.fullTextSearchIndexer = fullTextSearchIndexer;
	}

	public int getLastIndexedSnapshot(String store) {
		int last = getLastAsynchronousSnapshot(store);
		if (last > 0) {
			return last;
		}
		last = getLastSynchronousSnapshot(store);
		if (last > 0) {
			return last;
		}
		return hasIndexBeenCreated(store) ? 0 : -1;
	}

	public boolean isSnapshotIndexed(String store, int id) {
		if (id == 0) {
			return hasIndexBeenCreated(store);
		} else {
			return (id <= getLastAsynchronousSnapshot(store))
			|| (id <= getLastSynchronousSnapshot(store));
		}
	}

	public boolean isSnapshotSearchable(String store, int id) {
		if (id == 0) {
			return hasIndexBeenCreated(store);
		} else {
			return (id <= getLastSynchronousSnapshot(store));
		}
	}

	private int getLastSynchronousSnapshot(String store)
	{
		int answer = getLastSynchronousSnapshot(store, IndexChannel.DELTA);
		if (answer >= 0) {
			return answer;
		}

		answer = getLastSynchronousSnapshot(store, IndexChannel.MAIN);
		if (answer >= 0) {
			return answer;
		}
		return -1;
	}

	private int getLastSynchronousSnapshot(String store, IndexChannel channel) {
		String prefix = SNAP_SHOT_ID + ":" + store + ":";
		IndexReader reader = null;
		int end = -1;
		try {
			if (channel == IndexChannel.DELTA) {
				flushPending();
//				reader = getDeltaReader();
reader = service.getDeltaReader(deltaId, getRepoStorePath());
			} else {
//				reader = getReader();
				reader = service.getReader(getRepoStorePath());
			}

			TermEnum terms = null;
			try {
				terms = reader.terms();

				if (terms.skipTo(new Term("ID", prefix))) {

					do {
						Term term = terms.term();
						if (term.text().startsWith(prefix)) {
							TermDocs docs = null;
							try {
								docs = reader.termDocs(term);
								if (docs.next()) {
									String[] split = term.text().split(":");
									int test = Integer.parseInt(split[3]);
									if(test > end) {
										end = test;
									}
								}
							} finally {
								if (docs != null) {
									docs.close();
								}
							}
						} else {
							break;
						}
					} while (terms.next());
				}

			} finally {
				if (terms != null) {
					terms.close();
				}
			}
			return end;
		} catch (IOException e) {
			throw new AlfrescoRuntimeException("IO error", e);
		} finally {
			try {
				if (reader != null) {
					if (channel == IndexChannel.DELTA) {
						service.closeDeltaReader(deltaId, getRepoStorePath());
					} else {
//						reader.close();
service.closeMainReader(getRepoStorePath());
					}
				}
			} catch (IOException e) {
				s_logger.warn("[RepositoryAwareAVMLuceneIndexerImpl::getLastSynchronousSnapshot]" +
						"Repository: '" + getRepository() + "' -- " +
						"Failed to close main reader.", e);
			}
		}
	}

	private int getLastAsynchronousSnapshot(String store)
	{
		int answer = getLastAsynchronousSnapshot(store, IndexChannel.DELTA);
		if (answer >= 0) {
			return answer;
		}
		answer = getLastAsynchronousSnapshot(store, IndexChannel.MAIN);
		if (answer >= 0) {
			return answer;
		}
		return -1;
	}

	private int getLastAsynchronousSnapshot(String store, IndexChannel channel) {
		String prefix = "\u0000BG:STORE:" + store + ":";
		IndexReader reader = null;
		int end = -1;
		try {
			if (channel == IndexChannel.DELTA) {
				flushPending();
//				reader = getDeltaReader();
				reader = service.getDeltaReader(deltaId, getRepoStorePath());
			} else {
//				reader = getReader();
				reader = service.getReader(getRepoStorePath());
			}
			TermEnum terms = null;
			try {
				terms = reader.terms();

				if (terms.skipTo(new Term("ID", prefix))) {
					do {
						Term term = terms.term();
						if (term.text().startsWith(prefix)) {
							TermDocs docs = null;
							try {
								docs = reader.termDocs(term);
								if (docs.next()) {
									String[] split = term.text().split(":");
									int test = Integer.parseInt(split[4]);
									if(test > end) {
										end = test;
									}
								}
							} finally {
								if (docs != null) {
									docs.close();
								}
							}
						} else {
							break;
						}
					} while (terms.next());
				}
			} finally {
				if (terms != null) {
					terms.close();
				}
			}
			return end;
		} catch (IOException e) {
			throw new AlfrescoRuntimeException("IO error", e);
		} finally {
			try {
				if (reader != null) {
					if (channel == IndexChannel.DELTA) {
						service.closeDeltaReader(deltaId, getRepoStorePath());
					} else {
//						reader.close();
service.closeMainReader(getRepoStorePath());
					}
				}
			} catch (IOException e) {
				s_logger.warn("[RepositoryAwareAVMLuceneIndexerImpl::getLastAsynchronousSnapshot]" +
						"Repository: '" + getRepository() + "' -- " +
						"Failed to close main reader.", e);
			}
		}
	}

	public boolean hasIndexBeenCreated(String store) {
		IndexReader mainReader = null;
		try {
//			mainReader = getReader();
			mainReader = service.getReader(getRepoStorePath());
			TermDocs termDocs = null;
			try {
				termDocs = mainReader.termDocs(new Term("ISROOT", "T"));
				return termDocs.next();
			} finally {
				if (termDocs != null) {
					termDocs.close();
				}
			}
		} catch (IOException e) {
			throw new AlfrescoRuntimeException("IO error", e);
		} finally {
			try {
				if (mainReader != null) {
//					mainReader.close();
					service.closeMainReader(getRepoStorePath());
				}
			} catch (IOException e) {
				s_logger.warn("[RepositoryAwareAVMLuceneIndexerImpl::hasIndexBeenCreated]" +
						"Repository: '" + getRepository() + "' -- " +
						"Failed to close main reader.", e);
			}
		}
	}

	//MB: Metodo duplicato in 4 classi
	private void addFieldContent( Document doc, Locale locale, InputStream ris, String attributeName ){
		InputStreamReader isr = null;
		//InputStream ris = new ByteArrayInputStream(bytes_sbustati);
		try {
			isr = new InputStreamReader(ris, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			isr = new InputStreamReader(ris);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("\u0000").append(locale.toString()).append("\u0000");
		StringReader prefix = new StringReader(builder.toString());
		Reader multiReader = new MultiReader(prefix, isr);
		// Vecchia implementazione di ALF
		// doc.add(new Field(attributeName, multiReader, Field.TermVector.NO));

		// MB: 12:40:50 mercoledi' 18 febraio 2009
		// Modifica per replicare via terracotta il Field in un ambiente di cluster
		// TC replica bene String, se dovessimo replicare un MultiReader, ci sarebbero
		// troppe classi da trasportare a livello di TC
		try {
			char[] buffer = new char[1000];
			while( true ){
				int nRead = multiReader.read(buffer,0,1000);
				if( nRead==-1 ) break;
				builder.append(buffer,0,nRead);
			}
			if (s_logger.isDebugEnabled()) {
				s_logger.debug("Valore del campo (" +builder.toString() +")");
			}
			doc.add(new Field(attributeName, builder.toString(), Field.Store.NO, Field.Index.TOKENIZED, Field.TermVector.NO));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// MB: 12:37:28 martedi' 10 febraio 2009: evita il problema ERROR [trace] Content IO Channel was opened but not closed:
		try {
			ris.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
