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

package it.doqui.index.ecmengine.util;

import org.alfresco.service.namespace.QName;

public interface EcmEngineModelConstants {
	String ECMENGINE_SYS_MODEL_URI    = "http://www.doqui.it/model/ecmengine/system/1.0";
	String ECMENGINE_SYS_MODEL_PREFIX = "ecm-sys";

	QName ASPECT_SPLITTED                        = QName.createQName(ECMENGINE_SYS_MODEL_URI, "splitted");
	QName ASPECT_PART                            = QName.createQName(ECMENGINE_SYS_MODEL_URI, "part");
	QName ASPECT_AOO_ADMINISTRABLE               = QName.createQName(ECMENGINE_SYS_MODEL_URI, "aooadministrable");
	QName ASPECT_STATE                           = QName.createQName(ECMENGINE_SYS_MODEL_URI, "state");
	QName ASPECT_DESTINATION                     = QName.createQName(ECMENGINE_SYS_MODEL_URI, "destination");
	QName ASPECT_MODIFIED                        = QName.createQName(ECMENGINE_SYS_MODEL_URI, "modified");
	QName ASPECT_ENCRYPTED                       = QName.createQName(ECMENGINE_SYS_MODEL_URI, "encrypted");
    // MB: Rendition support
	QName ASPECT_RENDITIONABLE                   = QName.createQName(ECMENGINE_SYS_MODEL_URI, "renditionable");
	QName ASPECT_RENDITIONTRANSFORMER            = QName.createQName(ECMENGINE_SYS_MODEL_URI, "renditionTransformer");
    // MB: MultiContentStore support
	QName ASPECT_STORAGE                         = QName.createQName(ECMENGINE_SYS_MODEL_URI, "storage");

	QName ASPECT_STREAMED_CONTENT                = QName.createQName(ECMENGINE_SYS_MODEL_URI, "streamedContent");
	
	QName ASSOC_PARTS                            = QName.createQName(ECMENGINE_SYS_MODEL_URI, "parts");
	

	QName PROP_AOO_ID                            = QName.createQName(ECMENGINE_SYS_MODEL_URI, "aooid");
	QName PROP_AOO_ADMINISTRATOR                 = QName.createQName(ECMENGINE_SYS_MODEL_URI, "aooadministrator");
	QName PROP_STATO                             = QName.createQName(ECMENGINE_SYS_MODEL_URI, "stato");
	QName PROP_ID_NODE_DEST                      = QName.createQName(ECMENGINE_SYS_MODEL_URI, "idNodeDestination");
	QName PROP_REPO_DEST                         = QName.createQName(ECMENGINE_SYS_MODEL_URI, "repoDestination");
	QName PROP_ID_NODE_SOURCE                    = QName.createQName(ECMENGINE_SYS_MODEL_URI, "idNodeSource");
	QName PROP_REPO_SOURCE                       = QName.createQName(ECMENGINE_SYS_MODEL_URI, "repoSource");
	QName PROP_MODIFICATO                        = QName.createQName(ECMENGINE_SYS_MODEL_URI, "modificato");
	QName PROP_AUTORE                            = QName.createQName(ECMENGINE_SYS_MODEL_URI, "autore");
	QName PROP_DATA_MODIFICA                     = QName.createQName(ECMENGINE_SYS_MODEL_URI, "dataModifica");
	QName PROP_ENCRYPTION_KEY_ID                 = QName.createQName(ECMENGINE_SYS_MODEL_URI, "keyId");
	QName PROP_ENCRYPTION_TRANSFORMATION         = QName.createQName(ECMENGINE_SYS_MODEL_URI, "transformation");
	QName PROP_ENCRYPTED_FROM_SOURCE             = QName.createQName(ECMENGINE_SYS_MODEL_URI, "encryptedBySource");
	QName PROP_INITIALIZATION_VECTOR             = QName.createQName(ECMENGINE_SYS_MODEL_URI, "initializationVector");
    // MB: Rendition support
	QName PROP_RENDITION_XSL_ID                  = QName.createQName(ECMENGINE_SYS_MODEL_URI, "xslId");
	QName PROP_RENDITION_ID                      = QName.createQName(ECMENGINE_SYS_MODEL_URI, "renditionId");
	QName PROP_RENDITION_GENMIMETYPE             = QName.createQName(ECMENGINE_SYS_MODEL_URI, "genMimeType");
	QName PROP_RENDITION_TRANSFORMER_DESCRIPTION = QName.createQName(ECMENGINE_SYS_MODEL_URI, "transformerDescription");
	QName PROP_RENDITION_DOCUMENT_DESCRIPTION    = QName.createQName(ECMENGINE_SYS_MODEL_URI, "renditionDescription");
    // MB: MultiContentStore support
	QName PROP_STORAGE_ID                        = QName.createQName(ECMENGINE_SYS_MODEL_URI, "storageId");
}
