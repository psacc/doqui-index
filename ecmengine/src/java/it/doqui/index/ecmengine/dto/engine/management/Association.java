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
 
package it.doqui.index.ecmengine.dto.engine.management;

import it.doqui.index.ecmengine.dto.ContentItem;


/**
 * Classe che rappresenta un'associazione tra contenuti intesa sia come
 * child-association sia come reference. Se l'associazione &egrave; di tipo padre-figlio allora 
 * devono essere valorizzati entrambi gli attributi(sia nome che tipo);
 * se invece si tratta di un semplice reference tra contenuti risulta valorizzato soltanto il tipo.
 * 
 * @author Doqui
 * 
 */
public class Association extends ContentItem {	  

		private static final long serialVersionUID = 0L;
		private boolean childAssociation;
		private String typePrefixedName;		
		
		public Association(){
			super();
		}
		
		/**
		 * Indica se l'associazione &egrave; di tipo padre-figlio.
		 * 
		 * @return {@code true} se l'associazione &egrave; di tipo padre-figlio,
		 *         {@code false} altrimenti
		 */
		public boolean isChildAssociation() {
			return childAssociation;
		}
		
		/**
		 * Imposta il tipo dell'associazione.
		 *  
		 * @param childAssociation
		 * 		{@code true} se l'associazione &egrave; di tipo padre-figlio,
		 *      {@code false} altrimenti	
		 */
		 
		 public void setChildAssociation(boolean childAssociation) {
			this.childAssociation = childAssociation;
		}

		 /**
		  * Restituisce il nome, completo di prefisso, del tipo di questa {@code Association}.
		  * @return Il nome ,completo di prefisso, del tipo di associazione.
		  */ 
		public String getTypePrefixedName() {
			return typePrefixedName;
		}
		
		/**
		 * Imposta il tipo ,completo di prefisso, di questa {@code Association}.		   
		 * @param typePrefixedName Il nome ,completo di prefisso, del tipo dell'associazione.
		 */
		public void setTypePrefixedName(String typePrefixedName) {
			this.typePrefixedName = typePrefixedName;
		}		
}
