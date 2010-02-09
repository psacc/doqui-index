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

import it.doqui.index.ecmengine.business.foundation.repository.NodeSvc;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * Classe che implementa l'ordinamento mediante l'algoritmo di merge sort.
 * 
 * @author Doqui
 */
@SuppressWarnings("unchecked")
public final class MergeSort extends Sort {
	
	/**
	 * Costruttore predefinito.
	 * 
	 * @param data L'oggetto {@code List} da ordinare. 
	 * @param field {@code QName} della property su cui eseguire l'ordinamento.
	 * @param bForward true for a forward sort, false for a reverse sort
	 * @param mode Sort mode da utilizzare (case sensitive/case insensitive).
	 * @param nodeService Il {@code NodeSvc} da utilizzare nelle operazioni.
	 */
	public MergeSort(List data, QName field, boolean bForward, String mode,
			NodeSvc nodeService) {
		super(data, field, bForward, mode, nodeService);
	}

	/**
	 * Esecuzione dell'ordinamento.
	 * 
	 * @see it.doqui.index.ecmengine.business.publishing.util.Sort#sort()
	 */
	public void sort() {
		if (!this.data.isEmpty()) {
			mergesort(this.data, 0, this.data.size() - 1);
		}
	}

	/**
	 * Implementazione dell'algoritmo <i>merge sort</i>.
	 * 
	 * <p>Implementazione originale disponibile all'URL:</p>
	 * <ul>
	 *  <li><a href="http://www.cs.ubc.ca/spider/harrison/Java/MergeSortAlgorithm.java.html">
	 *   http://www.cs.ubc.ca/spider/harrison/Java/MergeSortAlgorithm.java.html
	 *  </a></li>
	 * </ul>
	 * 
	 * <p>Rispetto alla versione originale:</p>
	 * <ul>
	 *  <li>&egrave; stato implementato l'ordinamento inverso;</li>
	 *  <li>l'algoritmo &egrave; stato reso stabile modificando le condizioni di spostamento degli elementi.</li>
	 * </ul>
	 * 
	 * @param data La {@code List} di dati da orginare.
	 * @param lo0 L'estremo inferiore.
	 * @param hi0 L'estremo superiore.
	 */
	private void mergesort(final List data, int lo0, int hi0) {
		int lo = lo0;
		final int hi = hi0;
		if (lo >= hi) {
			return;
		}
		final int mid = (lo + hi) / 2;

		mergesort(data, lo, mid);
		mergesort(data, mid + 1, hi);
		int endLo = mid;
		int startHi = mid + 1;
		while ((lo <= endLo) && (startHi <= hi)) {
			if (bForward) {
				// ASC order
				// Versione originale:
				//   if (getComparator().compare(this.keys.get(lo), this.keys.get(start_hi)) < 0) {
				if (getComparator().compare(this.keys.get(lo), this.keys.get(startHi)) <= 0) {
					lo++;
				} else {
					final Object tmpKey = this.keys.get(startHi);
					final Object tmpObj = this.data.get(startHi);
					for (int k = startHi - 1; k >= lo; k--) {
						this.keys.set(k + 1, this.keys.get(k));
						this.data.set(k + 1, this.data.get(k));
					}
					this.keys.set(lo, tmpKey);
					this.data.set(lo, tmpObj);
					lo++;
					endLo++;
					startHi++;
				}
			} else {
				// DESC order
				if (getComparator().compare(this.keys.get(lo), this.keys.get(startHi)) >= 0) {
					lo++;
				} else {
					final Object tmpKey = this.keys.get(startHi);
					final Object tmpObj = this.data.get(startHi);
					for (int k = startHi - 1; k >= lo; k--) {
						this.keys.set(k + 1, this.keys.get(k));
						this.data.set(k + 1, this.data.get(k));
					}
					this.keys.set(lo, tmpKey);
					this.data.set(lo, tmpObj);
					lo++;
					endLo++;
					startHi++;
				}
			}
		}
	}
}
