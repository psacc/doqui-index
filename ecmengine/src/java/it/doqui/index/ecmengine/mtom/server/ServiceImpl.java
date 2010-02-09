package it.doqui.index.ecmengine.mtom.server;

import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jws.WebService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.csi.util.performance.StopWatch;
import it.doqui.index.ecmengine.business.publishing.management.*;
import it.doqui.index.ecmengine.business.publishing.search.EcmEngineSearch;
import it.doqui.index.ecmengine.business.publishing.search.EcmEngineSearchHome;
import it.doqui.index.ecmengine.dto.Node;
import it.doqui.index.ecmengine.dto.OperationContext;
import it.doqui.index.ecmengine.dto.Path;
import it.doqui.index.ecmengine.dto.engine.management.Aspect;
import it.doqui.index.ecmengine.dto.engine.management.Content;
import it.doqui.index.ecmengine.dto.engine.management.Property;
import it.doqui.index.ecmengine.dto.engine.search.ResultContent;
import it.doqui.index.ecmengine.dto.engine.search.SearchParams;
import it.doqui.index.ecmengine.util.EcmEngineConstants;
import it.doqui.index.ecmengine.util.EcmEngineConstantsReader;

@MTOM
@WebService(endpointInterface = "it.doqui.index.ecmengine.mtom.server.StreamingService", serviceName = "StreamingService")
public class ServiceImpl implements StreamingService {

	private static final String PROPS_FILE_LOCATION = "/usr/prod/trombotto4/mtom/myPropsFile.properties";
	private static final int BUFFER_SIZE = 100000;
	protected static Log logger;
	private String ejbExtension;
	
	private EcmEngineConstantsReader constantReader=new EcmEngineConstantsReader();
	protected StopWatch stopwatch;
	
	public ServiceImpl() {
		ejbExtension=constantReader.getEjbExtension();
		logger=LogFactory.getLog("index.ecmenginecxf"+ejbExtension);
	}

	public String uploadMethod(Attachment myFile, String usr, String pwd, String repo,String parent) throws SystemException {
		logger.debug("[ServiceImpl:uploadMethod] BEGIN");
		start();
		String destinationPathUL = "";
		DataHandler handler = myFile.attachmentDataHandler;
		long size = 0L;
		Node nodec=new Node();
		try {

			ResourceBundle resources = ResourceBundle.getBundle("mtom");
			Properties prop=new Properties();
			prop.put("java.naming.factory.initial", resources.getString("CONTEXT"));
			prop.put("java.naming.provider.url", resources.getString("URL_TO_CONNECT"));
			Context ctx = new InitialContext(prop);
			EcmEngineManagement management_bean = ((EcmEngineManagementHome)ctx.lookup(resources.getString("JNDI_NAME_MANAGEMENT"))).create();
			EcmEngineSearch search_bean = ((EcmEngineSearchHome)ctx.lookup(resources.getString("JNDI_NAME_SEARCH"))).create();
			OperationContext context=new OperationContext();
			context.setUsername(usr);
			context.setPassword(pwd);
			context.setRepository(repo);
			context.setFruitore(usr);
			context.setNomeFisico(usr);
			
			String contentName=myFile.fileName;
			Property[] props = new Property[1];
			props[0] = createPropertyDTO("cm:name", "text", false);
			props[0].setValues(new String [] { contentName });
			Property [] authorProps = new Property[1];
			authorProps[0] = createPropertyDTO("cm:author", "text", false);
			authorProps[0].setValues(new String [] { context.getUsername() + " da browser" });
			Property [] titledProps = new Property[2];
			titledProps[0] = createPropertyDTO("cm:title", "mltext", false);
			titledProps[0].setValues(new String [] { contentName });
			titledProps[1] = createPropertyDTO("cm:description", "mltext", false);
			titledProps[1].setValues(new String [] { "Contenuto aggiunto da browser." });			
			Aspect titled = new Aspect();						titled.setPrefixedName("cm:titled");
			titled.setModelPrefixedName("cm:contentmodel");
			titled.setProperties(titledProps);
			Content content = new Content();
			content.setPrefixedName("cm:" + contentName);
			content.setParentAssocTypePrefixedName("cm:contains");
			content.setModelPrefixedName("cm:contentmodel");
			content.setTypePrefixedName("cm:content");
			content.setContentPropertyPrefixedName("cm:content");
			content.setMimeType(myFile.fileType);
			content.setEncoding("UTF-8");
			Aspect[] aspects=new Aspect[1];
			aspects[0]=new Aspect();
			aspects[0].setModelPrefixedName("ecm-sys:ecmengineSystemModel");
			aspects[0].setPrefixedName("ecm-sys:streamedContent");
			content.setAspects(aspects);
			byte[] data=new byte[0];
			content.setContent(data);
			content.setProperties(props);
			SearchParams search=new SearchParams();
			nodec=management_bean.createContent(new Node(parent),content, context);
			Path absolutePath=search_bean.getAbsolutePath(nodec, context);
			InputStream is = handler.getInputStream();
			OutputStream os = new FileOutputStream(absolutePath.getPath());
			byte b[] = new byte[BUFFER_SIZE];
			for (int bytesRead = 0; (bytesRead = is.read(b)) != -1;) {
				os.write(b, 0, bytesRead);
				size += bytesRead;
			}
			os.flush();
			os.close();
			is.close();
			if(logger.isInfoEnabled()){
				logger.info("[ServiceImpl:uploadMethod] Upload del file " + myFile.fileName + " (" + myFile.fileType + ") completato: salvato con uid " + nodec.getUid() + " in "+absolutePath.getPath());
			}
			dumpElapsed("uploadMethod", "UID: "+nodec.getUid()+" - FILENAME: "+myFile.fileName+" - FILETYPE: "+myFile.fileType, "Upload completato.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SystemException("Errore durante la scrittura del file sul server", e);
		}finally{
			stop();
			logger.debug("[ServiceImpl:uploadMethod] END");
		}
		return nodec.getUid();
	}
	
	public static Property createPropertyDTO(String prefixedName, String dataType, boolean multivalue) {
		Property prop = new Property();
		prop.setPrefixedName(prefixedName);
		prop.setDataType(dataType);
		prop.setMultivalue(multivalue);
		return prop;
	}

	public Attachment downloadMethod(String uid, String usr, String pwd, String repo) throws SystemException {
		logger.debug("[ServiceImpl:downloadMethod] BEGIN");
		start();
		//String sourcePathDL = getPropsValue("sourcePathDL");
		Attachment myFile = new Attachment();
		//myFile.fileName = fileName;
		//myFile.fileType = fileType;
		try {
			ResourceBundle resources = ResourceBundle.getBundle("mtom");
			Properties prop=new Properties();
			prop.put("java.naming.factory.initial", resources.getString("CONTEXT"));
			prop.put("java.naming.provider.url", resources.getString("URL_TO_CONNECT"));
			Context ctx = new InitialContext(prop);
			EcmEngineManagement management_bean = ((EcmEngineManagementHome)ctx.lookup(resources.getString("JNDI_NAME_MANAGEMENT"))).create();
			EcmEngineSearch search_bean = ((EcmEngineSearchHome)ctx.lookup(resources.getString("JNDI_NAME_SEARCH"))).create();
			OperationContext context=new OperationContext();
			context.setUsername(usr);
			context.setPassword(pwd);
			context.setRepository(repo);
			context.setFruitore(usr);
			context.setNomeFisico(usr);
			ResultContent rc=management_bean.getContentMetadata(new Node(uid), context);
			String pn=rc.getPrefixedName();
			
			Path absolutePath=search_bean.getAbsolutePath(new Node(uid), context);
			File sourceFile=new File(absolutePath.getPath());
			myFile.fileSize = sourceFile.length();
			myFile.fileName=pn.substring(pn.indexOf(":")+1,pn.lastIndexOf("."));
			myFile.fileType=pn.substring(pn.lastIndexOf(".")+1);
			javax.activation.DataSource source = new FileDataSource(sourceFile);
			myFile.attachmentDataHandler = new DataHandler(source);
			if(logger.isInfoEnabled()){
				logger.info("[ServiceImpl:downloadMethod] Download di "+myFile.fileName+"."+myFile.fileType+" iniziato.");
			}
			dumpElapsed("downloadMethod", "UID: "+uid+" - FILENAME: "+myFile.fileName+"."+myFile.fileType+" - FILESIZE: "+myFile.fileSize+" - FILEPATH: "+absolutePath.getPath(), "Download iniziato.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SystemException("Errore durante la lettura del file sul server", e);
		}finally{
			stop();
			logger.debug("[ServiceImpl:downloadMethod] END");
		}
		/*File sourceFile = new File(sourcePathDL + fileName + "." + fileType);
		if (!sourceFile.canRead()) {
			throw new SystemException("Il file da scaricare " + fileName + "." + fileType + " non esiste.");
		} else {
			myFile.fileSize = sourceFile.length();
			javax.activation.DataSource source = new FileDataSource(sourceFile);
			myFile.attachmentDataHandler = new DataHandler(source);
			return myFile;
		}*/
		return myFile;
	}

	public static String getPropsValue(String field) {
		
		Properties props = new Properties();
		String value = null;
		try {
			props.load(new FileInputStream(PROPS_FILE_LOCATION));
			if (props.getProperty(field) != null)
				value = props.getProperty(field);
			System.out.println("got value: " + field + " = " + value);
		} catch (IOException e) {
			System.out.println("\n");
			System.out.println("CANNOT FIND PROPS FILE! PLEASE VERIFY WHERE IT IS LOCATED");
			System.out.println("\n");
			System.out.println(e);
		}
		return value;
	}
	
	protected void start() {
		this.stopwatch = new StopWatch("index.ecmenginecxf"+ejbExtension+".util.stopwatch");
		this.stopwatch.start();
	}
	
	protected void stop() {
		this.stopwatch.stop();
	}
	
	protected void dumpElapsed(String methodName, String ctx, String message) {
		this.stopwatch.dumpElapsed("ServiceImpl", methodName, ctx, message);
	}
}
