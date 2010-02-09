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
 
package it.doqui.index.ecmengine.integration.job.vo;

/**
 * Value object (VO) per lo scambio dei dati della tabella dei parametri dei job
 * sul database.
 * 
 * @author DoQui
 * 
 */
public class JobParamVO {

	private int id;
	private int jobId;
	private String name;
	private String val;

	/**
	 * Costruttore predefinito.
	 */
	public JobParamVO() {
		super();
		this.name = null;
		this.val = null;
	}

	/**
	 * Restituisce l'id del parametro.
	 * 
	 * @return L'id del paramtero.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Imposta l'id del parametro.
	 * 
	 * @param jobId L'id del paramtero.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Restituisce l'id del job.
	 * 
	 * @return L'id del job.
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * Imposta l'id del job.
	 * 
	 * @param jobId L'id del job.
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	/**
	 * Restituisce il nome del parametro.
	 * 
	 * @return Il nome del parametro.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Imposta il nome del parametro.
	 * 
	 * @param name Il nome del parametro.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Restituisce il valore del parametro.
	 * 
	 * @return Il valore del parametro.
	 */
	public String getVal() {
		return val;
	}

	/**
	 * Imposta il valore del parametro.
	 * 
	 * @param val Il valore del parametro.
	 */
	public void setVal(String val) {
		this.val = val;
	}

}
