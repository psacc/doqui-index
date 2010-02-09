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
 
package it.doqui.index.ecmengine.business.audit.util;

import it.doqui.index.ecmengine.dto.engine.audit.AuditInfo;
import it.doqui.index.ecmengine.dto.engine.audit.OperazioneAudit;
import it.doqui.index.ecmengine.integration.audit.vo.AuditVO;
import it.doqui.index.ecmengine.integration.audittrail.vo.AuditTrailVO;

/**
 * Classe di utilit&agrave; per la traduzione da oggetti DTO a VO e viceversa.
 * 
 * @author DoQui
 * 
 */
public abstract class AuditDtoHelper {

	/**
	 * Traduce un VO di tipo {@code AuditVO} in un DTO di tipo {@code OperazioneAudit}.
	 * 
	 * @param auditVO Il VO da tradurre.
	 * @return L'istanza di {@code OperazioneAudit} contenente i dati del VO.
	 */
	public static OperazioneAudit getOperazioneAudit(AuditVO auditVO) {
		if (auditVO == null) {
			return null;
		}
		OperazioneAudit operazioneAudit = new OperazioneAudit();
		operazioneAudit.setId(new Long(auditVO.getId()));
		operazioneAudit.setFruitore(auditVO.getFruitore());
		operazioneAudit.setUtente(auditVO.getUtente());
		operazioneAudit.setNomeOperazione(auditVO.getNomeOperazione());
		operazioneAudit.setServizio(auditVO.getServizio());
		operazioneAudit.setDataOra(auditVO.getDataOra());
		operazioneAudit.setIdOggetto(auditVO.getIdOggetto());
		operazioneAudit.setTipoOggetto(auditVO.getTipoOggetto());
		return operazioneAudit;
	}

	/**
	 * Traduce un DTO di tipo {@code OperazioneAudit} in un VO di tipo {@code AuditVO}.
	 * 
	 * @param operazioneAudit Il DTO da tradurre.
	 * @return L'istanza di {@code AuditVO} contenente i dati del DTO.
	 */
	public static AuditVO getAuditVO(OperazioneAudit operazioneAudit) {
		if (operazioneAudit == null) {
			return null;
		}
		AuditVO auditVO = new AuditVO();
		if (operazioneAudit.getId() != null) {
			auditVO.setId(operazioneAudit.getId().longValue());
		}
		auditVO.setFruitore(operazioneAudit.getFruitore());
		auditVO.setUtente(operazioneAudit.getUtente());
		auditVO.setNomeOperazione(operazioneAudit.getNomeOperazione());
		auditVO.setServizio(operazioneAudit.getServizio());
		auditVO.setDataOra(operazioneAudit.getDataOra());
		auditVO.setIdOggetto(operazioneAudit.getIdOggetto());
		auditVO.setTipoOggetto(operazioneAudit.getTipoOggetto());
		return auditVO;
	}

	/**
	 * Traduce un VO di tipo {@code AuditTrailVO} in un DTO di tipo {@code AuditInfo}.
	 * 
	 * @param auditTrailVO Il VO da tradurre.
	 * @return L'istanza di {@code AuditInfo} contenente i dati del VO.
	 */
	public static AuditInfo getAuditInfo(AuditTrailVO auditTrailVO) {
		if (auditTrailVO == null) {
			return null;
		}
		AuditInfo auditInfo = new AuditInfo();
		auditInfo.setId(new Long(auditTrailVO.getId()));
		auditInfo.setUtente(auditTrailVO.getUtente());
		auditInfo.setOperazione(auditTrailVO.getOperazione());
		auditInfo.setIdOggetto(auditTrailVO.getIdOggetto());
		auditInfo.setMetaDati(auditTrailVO.getMetaDati());
		auditInfo.setData(auditTrailVO.getData());
		return auditInfo;
	}

	/**
	 * Traduce un DTO di tipo {@code AuditInfo} in un VO di tipo {@code AuditTrailVO}.
	 * 
	 * @param auditInfo Il DTO da tradurre.
	 * @return L'istanza di {@code AuditTrailVO} contenente i dati del DTO.
	 */
	public static AuditTrailVO getAuditTrailVO(AuditInfo auditInfo) {
		if (auditInfo == null) {
			return null;
		}
		AuditTrailVO auditTrailVO = new AuditTrailVO();
		if (auditInfo.getId() != null) {
			auditTrailVO.setId(auditInfo.getId().longValue());
		}
		auditTrailVO.setUtente(auditInfo.getUtente());
		auditTrailVO.setOperazione(auditInfo.getOperazione());
		auditTrailVO.setIdOggetto(auditInfo.getIdOggetto());
		auditTrailVO.setMetaDati(auditInfo.getMetaDati());
		auditTrailVO.setData(auditInfo.getData());
		return auditTrailVO;
	}

}
