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
 
package it.doqui.index.ecmengine.business.job.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class BatchJob implements Serializable {

	private static final long serialVersionUID = 1793968585055315630L;

	private int id;
	private String ref;
	private Date timestampCreazione;
	private String status;
	private String message;
	private Date lastUpdate;
	private Map<String,BatchJobParam> params;

	public BatchJob() {
		this(null);
	}

	public BatchJob(String jobRef) {
		super();
		this.ref = jobRef;
		this.params = new HashMap<String,BatchJobParam>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<BatchJobParam> getParams() {
		Vector<BatchJobParam> paramList = new Vector<BatchJobParam>();
		for (BatchJobParam param : params.values()) {
			paramList.add(param);
		}
		return paramList;
	}

	public BatchJobParam getParam(String paramName) {
		return this.params.get(paramName);
	}

	public void addParam(BatchJobParam param) {
		this.params.put(param.getName(), param);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getTimestampCreazione() {
		return timestampCreazione;
	}

	public void setTimestampCreazione(Date timestampCreazione) {
		this.timestampCreazione = timestampCreazione;
	}

}
