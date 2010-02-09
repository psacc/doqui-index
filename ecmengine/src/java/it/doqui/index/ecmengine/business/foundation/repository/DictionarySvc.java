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

package it.doqui.index.ecmengine.business.foundation.repository;

import it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException;

import javax.ejb.EJBLocalObject;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

/**
 * <p>Interfaccia pubblica del servizio di dizionario del modello dei contenuti
 * esportata come componente EJB 2.1.
 * L'implementazione dei metodi qui dichiarati &egrave;
 * contenuta nella classe {@link DictionarySvcBean}.
 * </p>
 * <p>Tutti i metodi esportati dal bean di autenticazione rimappano le
 * {@code RuntimeException} ricevute in
 * {@link it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException}.
 * </p>
 *
 * @author Doqui
 *
 * @see DictionarySvcBean
 * @see it.doqui.index.ecmengine.exception.repository.DictionaryRuntimeException
 */
public interface DictionarySvc extends EJBLocalObject {

	/** Il carattere utilizzato come separatore nei nomi con prefisso. */
	char PREFIXED_NAME_SEPARATOR = ':';

	/** Il path interno del folder "Company Home" di Alfresco. */
	String COMPANY_HOME_PATH = "/app:company_home";

	/** Lo SpacesStore di Alfresco. */
	StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

	/** L'archivio dei nodi elimitati. */
	StoreRef ARCHIVE_SPACES_STORE = new StoreRef("archive", "SpacesStore");

	/** L'alfrescoUserStore. */
	StoreRef USER_STORE = new StoreRef("user", "alfrescoUserStore");

	/** lightWeightVersionStore di Alfresco. */
	StoreRef VERSIONS_STORE = new StoreRef(VersionService.VERSION_STORE_PROTOCOL, VersionModel.STORE_ID);

	/**
	 * Restituisce i nomi di tutti i data model definiti.
	 *
	 * @return Un array contenente i nomi (QName) dei data model.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	QName[] getAllModels() throws DictionaryRuntimeException;

	/**
	 * Restituisce la definizione del data model specificato.
	 *
	 * @param modelQName
	 *            Il nome (QName) del data model.
	 *
	 * @return La definizione (ModelDefinition) specificata.
	 *
	 * @throws DictionaryRuntimeException
	 *             Se si verifica un errore durante l'esecuzione.
	 */
	ModelDefinition getModelByName(QName modelQName) throws DictionaryRuntimeException;

	/**
	 * Restituisce la definizione della property passata come parametro di input.
	 *
	 * @param propertyQName Il nome (QName) della property di cui si vuole la definizione.
	 *
	 * @return La definizione (PropertyDefinition) della property data in input.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	PropertyDefinition getProperty(QName propertyQName) throws DictionaryRuntimeException;

	/**
	 * Restituisce la definizione dei tipi di contenuto definiti all'interno del modello
	 * specificato.
	 *
	 * @param modelQName Il nome (QName) del modello.
	 *
	 * @return Un array contenente le definizioni (TypeDefinition) dei tipi.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	TypeDefinition[] getTypesByModelName(QName modelQName) throws DictionaryRuntimeException;

	/**
	 * Restituisce la definizione degli aspect definiti all'interno del modello
	 * specificato.
	 *
	 * @param modelQName Il nome (QName) del modello.
	 *
	 * @return Un array contenente le definizioni (AspectDefinition) degli aspect.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	AspectDefinition[] getAspectsByModelName(QName modelQName)
			throws DictionaryRuntimeException;

	/**
	 * Restituisce il tipo del contenuto definito all'interno del modello.
	 *
	 * @param sourceType Il nome (QName) del tipo specificato.
	 *
	 * @return La definizione (TypeDefinition) del tipo.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	TypeDefinition getType(QName sourceType) throws DictionaryRuntimeException;

	/**
	 * Restituisce l'aspect definito all'interno del modello.
	 *
	 * @param sourceAspect Il nome (QName) dell'aspect specificato.
	 *
	 * @return La definizione (AspectDefinition) del tipo.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	AspectDefinition getAspect(QName sourceAspect) throws DictionaryRuntimeException;

	/**
	 * Restituisce le definizioni delle propriet&agrave; definite all'interno del tipo di
	 * contenuto specificato.
	 *
	 * @param typeQName Il nome (QName) del tipo.
	 *
	 * @return Un array contenente le definizioni (PropertyDefinition) delle propriet&agave;
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	PropertyDefinition[] getPropertiesByTypeName(QName typeQName)
			throws DictionaryRuntimeException;

	/**
	 * Restituisce le definizioni degli aspect definiti all'interno del tipo di
	 * contenuto specificato.
	 *
	 * @param typeQName Il nome esterno del tipo.
	 *
	 * @return Un array contenente le definizioni (AspectDefinition) degli aspect.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	AspectDefinition[] getAspectsByTypeName(QName typeQName)
			throws DictionaryRuntimeException;

	/**
	 * Restituisce le definizioni delle propriet&agrave; definite all'interno dell'aspect
	 * specificato.
	 *
	 * @param aspectQName Il nome (QName) dell'aspect.
	 *
	 * @return Un array contenente le definizioni (PropertyDefinition) delle propriet&agave;
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	PropertyDefinition[] getPropertiesByAspectName(QName aspectQName)
			throws DictionaryRuntimeException;

	/**
	 * Restituisce le definizioni delle associazioni di tipo target definite all'interno
	 * del tipo di contenuto specificato.
	 *
	 * @param typeQName Il nome (QName) del tipo.
	 *
	 * @return Un array contenente le definizioni (AssociationDefinition) delle asscociazioni.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	AssociationDefinition[] getAssociationsByTypeName(QName typeQName)
			throws DictionaryRuntimeException;

	/**
	 * Restituisce le definizioni delle associazioni di tipo padre-figlio definite all'interno
	 * del tipo di contenuto specificato.
	 *
	 * @param typeQName Il nome (QName) del tipo.
	 *
	 * @return Un array contenente le definizioni (ChildAssociationDefinition) delle asscociazioni.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	ChildAssociationDefinition[] getChildAssociationsByTypeName(QName typeQName)
			throws DictionaryRuntimeException;

	/**
	 * Risolve un {@code QName} nella stringa con prefisso corrispondente.
	 *
	 * <p><strong>NB:</strong> per ottenere la stringa con URI &egrave; possibile usare
	 * il metodo {@code toString()} dell'oggetto {@code QName}.</p>
	 *
	 * @param qname Il {@code QName} da risolvere.
	 *
	 * @return Il {@code QName} trasformato in stringa con prefisso.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String resolveQNameToPrefixName(QName qname) throws DictionaryRuntimeException;

	/**
	 * Risolve una stringa con prefisso nel {@code QName} corrispondente.
	 *
	 * <p><strong>NB:</strong> per convertire in QName una stringa con URI &egrave; possibile usare
	 * il metodo {@code createQName()} dell'oggetto {@code QName}.</p>
	 *
	 * @param prefixName La stringa con prefisso da risolvere.
	 *
	 * @return Il {@code QName} risultante dalla trasformazione.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	QName resolvePrefixNameToQName(String prefixName) throws DictionaryRuntimeException;

	/**
	 * Traduce il path specificato in input in una stringa composta dei nomi con prefisso.
	 *
	 * @param path Il {@code Path} da tradurre.
	 *
	 * @return La stringa composta di nomi con prefisso.
	 *
	 * @throws DictionaryRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String resolvePathToPrefixNameString(Path path) throws DictionaryRuntimeException;
}
