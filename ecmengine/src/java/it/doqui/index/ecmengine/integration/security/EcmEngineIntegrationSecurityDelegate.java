package it.doqui.index.ecmengine.integration.security;

import it.doqui.dosign.dosign.dto.envelope.Document;
import it.doqui.dosign.dosign.dto.envelope.EnvelopedBuffer;
import it.doqui.dosign.dosign.dto.signature.SignedBuffer;
import it.doqui.dosign.dosign.dto.signature.VerifyReport;
import it.doqui.index.ecmengine.integration.exception.EcmEngineIntegrationException;

public interface EcmEngineIntegrationSecurityDelegate {
	public VerifyReport verifyDocument(SignedBuffer buffer) throws EcmEngineIntegrationException;
	public Document extractDocumentFromEnvelope(EnvelopedBuffer buffer)throws EcmEngineIntegrationException;
}
