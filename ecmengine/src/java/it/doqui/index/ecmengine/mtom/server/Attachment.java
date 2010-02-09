package it.doqui.index.ecmengine.mtom.server;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Attachment {

	public String fileName;
	public String fileType;
	public long fileSize;
	@XmlMimeType("application/octet-stream")
	public DataHandler attachmentDataHandler;

	public Attachment() {
	}
}
