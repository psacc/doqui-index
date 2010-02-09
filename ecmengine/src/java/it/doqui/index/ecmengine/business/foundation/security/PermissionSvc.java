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
 
package it.doqui.index.ecmengine.business.foundation.security;

import it.doqui.index.ecmengine.exception.security.PermissionRuntimeException;

import java.util.Set;

import javax.ejb.EJBLocalObject;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;

/**
 * Interfaccia pubblica del servizio di gestione dei permessi (o autorizzazioni)
 * esportata come componente EJB 2.1. L'implementazione dei metodi qui dichiarati 
 * &egrave; contenuta nella classe {@link PermissionSvcBean}.
 * 
 * <p>Tutti i metodi esportati dal bean di gestione dei permessi rimappano le 
 * {@code RuntimeException} ricevute in 
 * {@link PermissionRuntimeException}.</p>
 * 
 * <p><strong>NB:</strong> in seguito verranno utilizzati indifferentemente i
 * termini <i>permesso</i> e <i>autorizzazione</i>.
 * 
 * @author Doqui
 * 
 * @see PermissionSvcBean
 * @see PermissionRuntimeException
 */

public interface PermissionSvc extends EJBLocalObject {
	
	/**
	 * Rimuove dalla Access Control List di un nodo tutte le voci relative
	 * ai permessi garantiti o negati all'authority specificata.
	 * 
	 * @param nodeRef Il nodo la cui ACL deve essere modificata.
	 * @param authority L'authority i cui permessi devono essere rimossi.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void clearPermission(NodeRef nodeRef, String authority) throws PermissionRuntimeException;

	/**
	 * <p>Rimuove dalla Access Control List la voce (o le voci) relativa (o
	 * relative) al permesso e all'authority specificati.</p>
	 * 
	 * <p><strong>NB:</strong> &egrave; possibile utilizzare varie combinazioni
	 * dei parametri di input per rimuovere pi&ugrave; voci contemporaneamente:</p>
	 * <ul>
	 *   <li>Una voce specifica, indicando sia <code>authority</code> che 
	 *   <code>permission</code>.</li>
	 *   <li>Tutte le voci relative a un'authority, se <code>permission</code> 
	 *   &egrave; <code>null</code>.</li>
	 *   <li>Tutte le voci relative a un permesso, se <code>authority</code> 
	 *   &egrave; <code>null</code>.</li>
	 *   <li>Tutte le voci della ACL, se sia <code>authority</code> che 
	 *   <code>permission</code> sono <code>null</code>.</li>
	 * </ul>
	 * 
	 * @param nodeRef Il nodo la cui ACL deve essere modificata.
	 * @param authority L'authority (o <code>null</code> 
	 * per indicare qualsiasi authority).
	 * @param permission Il permesso da rimuovere 
	 * (o <code>null</code> per indicare qualsiasi permesso).
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void deletePermission(NodeRef nodeRef, String authority, String permission) 
			throws PermissionRuntimeException;

	/**
	 * Rimuove tutte le voci dalla Access Control List di un nodo.
	 * 
	 * @param nodeRef Il nodo la cui ACL deve essere svuotata.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void deletePermissions(NodeRef nodeRef) throws PermissionRuntimeException;

	/**
	 * Restituisce il nome dell'authority speciale che le rappresenta tutte (ALL_AUTHORITIES).
	 * 
	 * @return Il nome dell'authority che rappresenta il valore speciale ALL_AUTHORITIES.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getAllAuthorities() throws PermissionRuntimeException;
	
	/**
	 * Restituisce il nome del permesso speciale che li rappresenta tutti (ALL_PERMISSIONS).
	 * 
	 * @return Il nome del permesso che rappresenta il valore speciale ALL_PERMISSIONS.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getAllPermission() throws PermissionRuntimeException;

	/**
	 * Restituisce tutte le voci della Access Control List di un nodo.
	 * 
	 * @param nodeRef Il nodo di cui si richiede la ACL.
	 * 
	 * @return Un <code>Set</code> contenente tutte le voci della ACL del nodo.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef) throws PermissionRuntimeException;

	/**
	 * Restituisce il valore del flag che determina se un nodo eredita la 
	 * Access Control List del suo nodo padre.
	 * 
	 * @param nodeRef Il nodo su cui effettuare il controllo.
	 * 
	 * @return <code>true</code> se il nodo eredita la ACL del proprio nodo padre,
	 * <code>false</code> altrimenti.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	boolean getInheritParentPermissions(NodeRef nodeRef) throws PermissionRuntimeException;

	/**
	 * Restituisce il nome dell'authority speciale che rappresenta il proprietario
	 * di un nodo.
	 * 
	 * @return Il nome dell'authority che rappresenta il proprietario di un nodo.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	String getOwnerAuthority() throws PermissionRuntimeException;

	/**
	 * Restituisce tutte le voci della Access Control List di un nodo relative
	 * all'utente corrente.
	 * 
	 * @param nodeRef Il nodo di cui si richiede la ACL.
	 * 
	 * @return Un <code>Set</code> contenente tutte le voci della ACL del nodo che sono
	 * relative all'utente corrente.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<AccessPermission> getPermissions(NodeRef nodeRef) throws PermissionRuntimeException;

	/**
	 * Restituisce un elenco dei nomi di tutti i permessi che possono essere
	 * impostati per un particolare nodo.
	 * 
	 * @param nodeRef Il nodo.
	 * 
	 * @return Un <code>Set</code> contenente i nomi di tutti i permessi settabili.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getSettablePermissions(NodeRef nodeRef) throws PermissionRuntimeException;

	/**
	 * Restituisce un elenco dei nomi di tutti i permessi che possono essere
	 * impostati per un particolare tipo.
	 * 
	 * @param type Il tipo.
	 * 
	 * @return Un <code>Set</code> contenente i nomi di tutti i permessi settabili.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	Set<String> getSettablePermissions(QName type) throws PermissionRuntimeException;

	/**
	 * Restituisce lo stato di disponibilit&agrave; (garantita, negata) di
	 * un'autorizzazione su un nodo per l'utente corrente.
	 * 
	 * @param nodeRef Il nodo la cui ACL deve essere controllata.
	 * @param permission L'autorizzazione di cui si richiede lo stato.
	 * 
	 * @return Lo stato, sul nodo specificato, dell'autorizzazione specificata
	 * per l'utente corrente.
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	AccessStatus hasPermission(NodeRef nodeRef, String permission) throws PermissionRuntimeException;

	/**
	 * Imposta il valore del flag che determina se un nodo eredita la 
	 * Access Control List del suo nodo padre.
	 * 
	 * @param nodeRef Il nodo su cui impostare il flag.
	 * @param inheritParentPermission Il valore da impostare:
	 * <ul>
	 *   <li><code>true</code> = eredita la ACL dal nodo padre</li>
	 *   <li><code>false</code> = non eredita la ACL dal nodo padre</li>
	 * </ul>
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermission) 
			throws PermissionRuntimeException;

	/**
	 * Imposta (o aggiunge) una voce nella Access Control List di un nodo.
	 * 
	 * @param nodeRef Il nodo la cui ACL deve essere modificata.
	 * @param authority Il nome dell'authority a cui si applica la voce aggiunta 
	 * o modificata.
	 * @param permission Il nome del permesso a cui si riferisce la voce aggiunta
	 * o modificata.
	 * @param allow Lo stato di accesso al permesso da parte dell'authority
	 * specificata:
	 * <ul>
	 *   <li><code>true</code> = accesso garantito</li>
	 *   <li><code>false</code> = accesso negato</li>
	 * </ul>
	 * 
	 * @throws PermissionRuntimeException Se si verifica un errore durante l'esecuzione.
	 */
	void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow) 
			throws PermissionRuntimeException;
	
	/**
	 * Verifica se l'implementazione del Permission Service sottostante supporta l'ereditariet&agrave;
	 * multipla delle ACL.
	 * 
	 * @return {@code true} se l'ereditariet&agrave; multipla &egrave; supportata, {@code false} altrimenti.
	 */
	boolean supportsMultipleInheritance();

/*
 * Metodi dichiarati in PermissionServiceSPI.java.
 * 
 * Non sono stati esportati, per ora, poiche` non sono direttamente accessibili 
 * da ServiceRegistry.getPermissionService(), quindi non dovrebbero essere 
 * utilizzati pubblicamente. Inoltre duplicano funzionalita` gia` pre&agrave;enti in 
 * PermissionService.
 * 
 * Se sara` necessario potranno essere esportati in futuro.
 */
	
//	public void deletePermission(PermissionEntry permissionEntry) throws ServiceRegistryMBeanNotFoundException;

//	public void deletePermissions(NodePermissionEntry nodePermissionEntry) throws ServiceRegistryMBeanNotFoundException;

//	public void deletePermissions(String recipient) throws ServiceRegistryMBeanNotFoundException;

//	public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
//					throws ServiceRegistryMBeanNotFoundException;

//	public PermissionReference getAllPermissionReference() throws ServiceRegistryMBeanNotFoundException;

//	public String getPermission(PermissionReference permissionReference) throws ServiceRegistryMBeanNotFoundException;

//	public PermissionReference getPermissionReference(QName qname, String permissionName)
//					throws ServiceRegistryMBeanNotFoundException;

//	public PermissionReference getPermissionReference(String permissionName)
//					throws ServiceRegistryMBeanNotFoundException;

//	public NodePermissionEntry getSetPermissions(NodeRef nodeRef) throws ServiceRegistryMBeanNotFoundException;

//	public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
//					throws ServiceRegistryMBeanNotFoundException;

//	public Set<PermissionReference> getSettablePermissionReferences(QName type)
//					throws ServiceRegistryMBeanNotFoundException;

//	public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
//					throws ServiceRegistryMBeanNotFoundException;

//	public void setPermission(NodePermissionEntry nodePermissionEntry) throws ServiceRegistryMBeanNotFoundException;
	
//	public void setPermission(PermissionEntry permissionEntry) throws ServiceRegistryMBeanNotFoundException;
}
