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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import it.doqui.index.ecmengine.business.foundation.EcmEngineWrapperBean;
import it.doqui.index.ecmengine.business.foundation.util.FoundationErrorCodes;
import it.doqui.index.ecmengine.exception.repository.ExportRuntimeException;

import java.io.UnsupportedEncodingException;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.ReferenceType;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportSvcBean extends EcmEngineWrapperBean {

	private static final long serialVersionUID = -9084583399572312013L;

	public byte[] export(String tenant) throws ExportRuntimeException{
		logger.debug("[ExportSvcBean::export] BEGIN");
		ByteArrayOutputStream os=null;
		byte[] data=null;
		try {
			os = new ByteArrayOutputStream();
			Location loc=new Location(new StoreRef("workspace","@"+tenant+"@SpacesStore"));
			ExporterCrawlerParameters ecp=new ExporterCrawlerParameters();
			ecp.setCrawlAssociations(true);
			ecp.setCrawlChildNodes(true);
			ecp.setCrawlContent(true);
			ecp.setCrawlNullProperties(true);
			ecp.setCrawlSelf(true);
			ecp.setExportFrom(loc);
			getExportService().exportView(createXMLExporter(os, ecp.getReferenceType()),ecp,null);
			//getExportService().exportView(os, ecp,null);
			data=os.toByteArray();
			os.close();
		}catch(Exception e){
			logger.error("[ExportSvcBean::export] ERROR:\n"+e.getMessage());
			throw new ExportRuntimeException(FoundationErrorCodes.GENERIC_EXPORT_EXCEPTION);
		}finally{
			try{os.close();}catch(Exception e){}
			logger.debug("[ExportSvcBean::export] END");
		}
		return data;
	}


	private ExporterService getExportService() {
		return (ExporterService)serviceRegistry.getExporterService();
	}
	
    private Exporter createXMLExporter(OutputStream viewWriter, ReferenceType referenceType)
    {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(2);
        format.setEncoding("UTF-8");

        // Construct an XML Exporter
        try
        {
            XMLWriter writer = new XMLWriter(viewWriter, format);
            ECMEngineExporter exporter = new ECMEngineExporter(serviceRegistry.getNamespaceService(), serviceRegistry.getNodeService(), serviceRegistry.getSearchService(), serviceRegistry.getDictionaryService(), serviceRegistry.getPermissionService(), writer);
            exporter.setReferenceType(referenceType);
            return exporter;
        }
        catch (UnsupportedEncodingException e)        
        {
            throw new ExporterException("Failed to create XML Writer for export", e);            
        }
        catch (Exception e)        
        {
            throw new ExporterException("Failed to create XML Writer for export", e);            
        }
    }
}
