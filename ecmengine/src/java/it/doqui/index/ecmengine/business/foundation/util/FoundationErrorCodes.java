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

package it.doqui.index.ecmengine.business.foundation.util;

public enum FoundationErrorCodes {

	/* Errore sconosciuto. */
	UNKNOWN_ERROR,

	/* Errori generici. */
	GENERIC_NODE_SERVICE_ERROR,
	GENERIC_CONTENT_SERVICE_ERROR,
	GENERIC_AUTHENTICATION_SERVICE_ERROR,
	GENERIC_AUTHORITY_SERVICE_ERROR,
	GENERIC_PERMISSION_SERVICE_ERROR,
	GENERIC_OWNABLE_SERVICE_ERROR,
	GENERIC_SEARCH_SERVICE_ERROR,
	GENERIC_VERSION_SERVICE_ERROR,
	GENERIC_CHECKOUT_CHECKIN_SERVICE_ERROR,
	GENERIC_ACTION_SERVICE_ERROR,
	GENERIC_PERSON_SERVICE_ERROR,
	GENERIC_DICTIONARY_SERVICE_ERROR,
	GENERIC_COPY_SERVICE_ERROR,
	GENERIC_LOCK_SERVICE_ERROR,
	GENERIC_RULE_SERVICE_ERROR,
	GENERIC_TENANT_ADMIN_SERVICE_ERROR,
	GENERIC_REPO_ADMIN_SERVICE_ERROR,
	GENERIC_JOB_SERVICE_ERROR,
	GENERIC_AUDIT_SERVICE_ERROR,
	GENERIC_AUDIT_TRAIL_SERVICE_ERROR,
	GENERIC_MIMETYPE_SERVICE_ERROR,
	GENERIC_INTEGRITY_SERVICE_ERROR,
	GENERIC_FILEFOLDER_SERVICE_ERROR,
	GENERIC_NAMESPACE_SERVICE_ERROR,
	GENERIC_WORKFLOW_SERVICE_ERROR,
	GENERIC_FILE_FORMAT_SERVICE_ERROR,
	GENERIC_CATEGORY_SERVICE_ERROR,
	GENERIC_EXPORT_EXCEPTION,
	GENERIC_IMPORT_EXCEPTION,


	/* Errori specifici. */
	INVALID_NODE_REF_ERROR,
	INVALID_TYPE_ERROR,
	INVALID_ASPECT_ERROR,
	ACCESS_DENIED_ERROR,
	NODE_LOCKED_ERROR,
	BAD_CREDENTIALS_ERROR,
	DUPLICATE_CHILD_ERROR,
	UNKNOWN_AUTHORITY_ERROR,
	NO_SUCH_PERSON_ERROR,

	/* Errori encryption. */
	ENCRYPTION_GENERIC_ERROR,
	ENCRYPTION_INVALID_ALGORITHM_ERROR,
	ENCRYPTION_INVALID_PADDING_ERROR,
	ENCRYPTION_INVALID_KEY_ERROR,
	ENCRYPTION_INVALID_PARAM_ERROR,

	/* Errori check-in check-out */
	ALREADY_CHECKED_OUT,

	GENERIC_NODE_ARCHIVE_SERVICE_ERROR
}
