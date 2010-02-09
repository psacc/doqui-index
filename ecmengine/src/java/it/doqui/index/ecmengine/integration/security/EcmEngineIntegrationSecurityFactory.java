package it.doqui.index.ecmengine.integration.security;

import it.doqui.index.ecmengine.integration.security.pd.EcmEngineIntegrationSecurityDelegateImpl;

public class EcmEngineIntegrationSecurityFactory {
	public static EcmEngineIntegrationSecurityDelegate getSecurityDelegate(){
		return new EcmEngineIntegrationSecurityDelegateImpl();
	}
}
