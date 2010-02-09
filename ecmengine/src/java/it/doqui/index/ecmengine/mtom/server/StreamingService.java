package it.doqui.index.ecmengine.mtom.server;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface StreamingService {

	public String uploadMethod(@WebParam(name="myFile") Attachment myFile, String usr, String pwd, String repo,String parent) throws SystemException;
	public Attachment downloadMethod(String uid, String usr, String pwd, String repo) throws SystemException;
}
