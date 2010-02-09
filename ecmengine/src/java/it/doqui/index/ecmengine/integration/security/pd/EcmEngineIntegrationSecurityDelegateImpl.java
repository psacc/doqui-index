package it.doqui.index.ecmengine.integration.security.pd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.csi.csi.porte.InfoPortaDelegata;
import it.csi.csi.porte.proxy.PDProxy;
import it.csi.csi.util.xml.PDConfigReader;

import it.doqui.dosign.dosign.dto.envelope.Document;
import it.doqui.dosign.dosign.dto.envelope.EnvelopedBuffer;
import it.doqui.dosign.dosign.dto.signature.SignedBuffer;
import it.doqui.dosign.dosign.dto.signature.VerifyReport;
import it.doqui.index.ecmengine.integration.exception.EcmEngineIntegrationException;
import it.doqui.index.ecmengine.integration.security.EcmEngineIntegrationSecurityDelegate;

public class EcmEngineIntegrationSecurityDelegateImpl implements EcmEngineIntegrationSecurityDelegate{
	private static Log log = LogFactory.getLog("ecmengine.integration");
	
	public VerifyReport verifyDocument(SignedBuffer buffer) throws EcmEngineIntegrationException{
		log.debug("[EcmEngineIntegrationSecurityDelegateImpl::verifyDocument] BEGIN");
		VerifyReport result=null;
		try{
			it.doqui.dosign.dosign.interfacecsi.dosign.DosignInterface pd = null;
			InfoPortaDelegata infopd = PDConfigReader.read(this.getClass().getResourceAsStream("/pd_dosign.xml"));
			pd = (it.doqui.dosign.dosign.interfacecsi.dosign.DosignInterface)PDProxy.newInstance(infopd);
			result=pd.verifyDocument(buffer);
		}catch(Exception e){
			log.error("[EcmEngineIntegrationSecurityDelegateImpl::verifyDocument] Exception");
			throw new EcmEngineIntegrationException(e.getMessage());
		}
		log.debug("[EcmEngineIntegrationSecurityDelegateImpl::verifyDocument] END");
		return result;
	}
	
	public Document extractDocumentFromEnvelope(EnvelopedBuffer buffer)throws EcmEngineIntegrationException{
		log.debug("[EcmEngineIntegrationSecurityDelegateImpl::ExtractDocumentFromEnvelope] BEGIN");
		Document result=null;
		try{
			it.doqui.dosign.dosign.interfacecsi.dosign.DosignInterface pd = null;
			InfoPortaDelegata infopd = PDConfigReader.read(this.getClass().getResourceAsStream("/pd_dosign.xml"));
			pd = (it.doqui.dosign.dosign.interfacecsi.dosign.DosignInterface)PDProxy.newInstance(infopd);
			result=pd.extractDocumentFromEnvelope(buffer);
		}catch(Exception e){
			log.error("[EcmEngineIntegrationSecurityDelegateImpl::ExtractDocumentFromEnvelope] Exception");
			throw new EcmEngineIntegrationException(e.getMessage());
		}
		log.debug("[EcmEngineIntegrationSecurityDelegateImpl::ExtractDocumentFromEnvelope] END");
		return result;		
	}	
}
