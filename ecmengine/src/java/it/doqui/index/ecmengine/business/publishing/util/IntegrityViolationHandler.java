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
 
package it.doqui.index.ecmengine.business.publishing.util;

import it.doqui.index.ecmengine.business.foundation.repository.DictionarySvc;
import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;
import it.doqui.index.ecmengine.util.EcmEngineConstants;

import java.text.MessageFormat;

import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class IntegrityViolationHandler {

	private static String PROPERTY_NOT_SET_FORMAT = "Metadato obbligatorio non impostato: {0} (Tipo: {1})";
	private static String ASPECT_NOT_SET_FORMAT = "Aspect obbligatorio non impostato: {0} (Tipo: {1})";
	private static String CONSTRAINT_VIOLATED_FORMAT = "Violazione del vincolo: {0} (Metadato: \"{1}\" - Tipo: \"{2}\")";
	private static String GENERIC_MESSAGE_FORMAT = "Violazione: {0}";
	
	private static String MANDATORY_PROP_NOT_SET = "Mandatory property not set:";
	private static String INVALID_PROP_VALUE = "Invalid property value:";
	private static String MANDATORY_ASPECT_NOT_SET = "Mandatory aspect not set:";
	
	private static String TYPE_STR = "Type:";
	private static String ASPECT_STR = "Aspect:";
	private static String PROP_STR = "Property:";
	private static String CONSTR_STR = "Constraint:";
	private static String LINE_FEED = "\n";
	
	private static Log logger = LogFactory.getLog(EcmEngineConstants.ECMENGINE_BUSINESS_LOG_CATEGORY);
	
	public static String translateIntegrityRecordMessage(IntegrityRecord record, DictionarySvc dictionary) 
	throws DictionaryRuntimeException {
		final SliceableString original = new SliceableString(record.getMessage());
		
		logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] BEGIN");

		try {
			if (original.toString().startsWith(MANDATORY_PROP_NOT_SET)) {
				/*
				 * Mandatory property not set:
				 *		Node: workspace://SpacesStore/c1c943f8-f43c-11dc-a14c-997f3884250a
				 * 		Type: {http://www.doqui.it/model/crecedo/1.0}cedolino
				 * 		Property: {http://www.doqui.it/model/crecedo/1.0}crittografato
				 */

				final String type = original.from(TYPE_STR).to(LINE_FEED).toString();
				final String prop = original.from(PROP_STR).to(LINE_FEED).toString();

				logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] Type: " + type);
				logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] Prop: " + prop);
				
				return MessageFormat.format(PROPERTY_NOT_SET_FORMAT, 
						dictionary.resolveQNameToPrefixName(QName.createQName(prop)),
						dictionary.resolveQNameToPrefixName(QName.createQName(type)));

			} else if (original.toString().startsWith(INVALID_PROP_VALUE)) {
				/*
				 * Invalid property value:
				 * 		Node: workspace://SpacesStore/c1c943f8-f43c-11dc-a14c-997f3884250a
				 * 		Type: {http://www.doqui.it/model/crecedo/1.0}cedolino
				 * 		Property: {http://www.doqui.it/model/crecedo/1.0}anno
				 * 		Constraint: Numeric value '1.999' is not in range [2.007; 2.020]
				 */

				final String type = original.from(TYPE_STR).to(LINE_FEED).toString();
				final String prop = original.from(PROP_STR).to(LINE_FEED).toString();
				final String constr = original.from(CONSTR_STR).to(LINE_FEED).toString();
				
				logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] Type: " + type);
				logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] Prop: " + prop);
				logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] Constr: " + constr);

				return MessageFormat.format(CONSTRAINT_VIOLATED_FORMAT, 
						constr,
						dictionary.resolveQNameToPrefixName(QName.createQName(prop)), 
						dictionary.resolveQNameToPrefixName(QName.createQName(type)));
			} else if (original.toString().startsWith(MANDATORY_ASPECT_NOT_SET)) {
				/*
				 * Mandatory aspect not set: 
				 *		Node: workspace://SpacesStore/04bc53ee-ea011-11dc-a9444-25e1b57bc00e 
				 *		Type: {http://www.alfresco.org/model/content/1.0}folder 
				 *		Aspect: {http://www.alfresco.org/model/content/1.0}auditable 
				 */
				
				final String type = original.from(TYPE_STR).to(LINE_FEED).toString();
				final String aspect = original.from(ASPECT_STR).to(LINE_FEED).toString();
				
				return MessageFormat.format(ASPECT_NOT_SET_FORMAT, 
						dictionary.resolveQNameToPrefixName(QName.createQName(aspect)),
						dictionary.resolveQNameToPrefixName(QName.createQName(type)));
				
			} else {
				return MessageFormat.format(GENERIC_MESSAGE_FORMAT, 
						original.toString().replace("\n", " - ").replace("\t", ""));
			}
		} finally {
			logger.debug("[IntegrityViolationHandler::translateIntegrityRecordMessage] END");
		}
	}
}
