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

package it.doqui.index.ecmengine.business.personalization.mimetype;

import it.doqui.index.ecmengine.integration.mimetype.dao.MimetypeDAO;
import it.doqui.index.ecmengine.integration.mimetype.vo.MimetypeVO;

import it.doqui.index.ecmengine.business.mimetype.MimetypeBusinessInterface;
//import it.doqui.index.ecmengine.business.mimetype.dto.Mimetype;
import it.doqui.index.ecmengine.dto.engine.management.Mimetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MimetypeManager extends org.alfresco.repo.content.MimetypeMap implements MimetypeBusinessInterface
{
    private List<String> mimetypes;
    private Map<String, String> extensionsByMimetype;
    private Map<String, String> mimetypesByExtension;
    private Map<String, String> displaysByMimetype;
    private Map<String, String> displaysByExtension;
    private Set<String> textMimetypes;

    private ArrayList<MimetypeVO> arrayMimetype;
    
    private MimetypeDAO mtDAO;

    public void setMimetypeDAO( MimetypeDAO mt ){
       mtDAO = mt;
    }

    public void init() {
        // Inizializzo
        super.init();

        // Creo le liste
        this.mimetypes = new ArrayList<String>(40);
        this.extensionsByMimetype = new HashMap<String, String>(59);
        this.mimetypesByExtension = new HashMap<String, String>(59);
        this.displaysByMimetype = new HashMap<String, String>(59);
        this.displaysByExtension = new HashMap<String, String>(59);
        this.textMimetypes = new HashSet<String>(23);
        
        this.arrayMimetype=new ArrayList<MimetypeVO>();

        // Le clono
        cloneMap( extensionsByMimetype , super.getExtensionsByMimetype() );
        cloneMap( mimetypesByExtension , super.getMimetypesByExtension() );
        cloneMap( displaysByMimetype   , super.getDisplaysByMimetype()   );
        cloneMap( displaysByExtension  , super.getDisplaysByExtension()  );
        
        List<String> mtl = super.getMimetypes();
        for( String s : mtl ){
           mimetypes.add(s);
        }

        // Arricchisco
        //System.out.println( "A:" +mimetypesByExtension.size() );

        try {
			MimetypeVO[] mta = mtDAO.getAllMimetypes();
            //System.out.println( "DAO:" +mta.length );
			for( MimetypeVO mt : mta ){
			    if( super.isText( mt.getMimeType() ) || mt.getMimeType().startsWith(PREFIX_TEXT) ){
			        textMimetypes.add( mt.getMimeType() );
			    }
			    extensionsByMimetype.put( mt.getMimeType(), mt.getFileExtension() );
			    mimetypesByExtension.put( mt.getFileExtension(), mt.getMimeType() );
			    displaysByMimetype.put( mt.getMimeType(), mt.getMimeType()  );
			    
			    arrayMimetype.add(mt);
                //System.out.println( "DAOt:" +mt.getMimeType() );
                //System.out.println( "DAOe:" +mt.getFileExtension() );
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //System.out.println( "A:" +mimetypesByExtension.size() );

        // make the collections read-only
        this.mimetypes = Collections.unmodifiableList(this.mimetypes);
        this.extensionsByMimetype = Collections.unmodifiableMap(this.extensionsByMimetype);
        this.mimetypesByExtension = Collections.unmodifiableMap(this.mimetypesByExtension);
        this.displaysByMimetype = Collections.unmodifiableMap(this.displaysByMimetype);
        this.displaysByExtension = Collections.unmodifiableMap(this.displaysByExtension);

        
        /*
        // TEST
        // plain text
        assertEquals("txt", extensionsByMimetype.get("text/plain"));
        assertEquals("text/plain", mimetypesByExtension.get("txt"));
        assertEquals("text/plain", mimetypesByExtension.get("csv"));
        assertEquals("text/plain", mimetypesByExtension.get("java"));
        System.out.println("*******************"+displaysByMimetype.get("image/pjpeg"));
        
        // JPEG
        assertEquals("jpg", extensionsByMimetype.get("image/jpeg"));
        assertEquals("jpg", extensionsByMimetype.get("image/pjpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpe"));

        // MS Word
        assertEquals("doc", extensionsByMimetype.get("application/msword"));
        assertEquals("application/msword", mimetypesByExtension.get("doc"));

        // Star Office
        assertEquals("sds", extensionsByMimetype.get("application/vnd.stardivision.chart"));

        // Test
        assertEquals("matteo", extensionsByMimetype.get("prova di lettura DAO"));
        assertEquals("prova di lettura DAO", mimetypesByExtension.get("matteo"));
        */
    }

    /*
    private void assertEquals( String a, String b ){
        System.out.println( "A:" +a );
        System.out.println( "B:" +b );
        System.out.println( "A=B:" +a.equals(b) );
    }
    */
    private void cloneMap( Map destination, Map source ){
       Set<String> keys = source.keySet();
       for (String key : keys){
           String value = (String)source.get(key);
           destination.put( key, value );
       }
    }

    public MimetypeManager() {
        super();
    }

    public String getExtension(String mimetype)
    {
        String extension = extensionsByMimetype.get(mimetype);
        if (extension == null)
        {
            return EXTENSION_BINARY;
        }
        else
        {
            return extension;
        }
    }

    public Map<String, String> getDisplaysByExtension()
    {
        return displaysByExtension;
    }

    public Map<String, String> getDisplaysByMimetype()
    {
        return displaysByMimetype;
    }

    public Map<String, String> getExtensionsByMimetype()
    {
        return extensionsByMimetype;
    }

    public List<String> getMimetypes()
    {
        return mimetypes;
    }

    public Map<String, String> getMimetypesByExtension()
    {
        return mimetypesByExtension;
    }

    public boolean isText(String mimetype)
    {
        return textMimetypes.contains(mimetype);
    }

    /**
     * @see #MIMETYPE_BINARY
     */
    public String guessMimetype(String filename)
    {
        filename = filename.toLowerCase();
        String mimetype = MIMETYPE_BINARY;
        // extract the extension
        int index = filename.lastIndexOf('.');
        if (index > -1 && (index < filename.length() - 1))
        {
            String extension = filename.substring(index + 1);
            if (mimetypesByExtension.containsKey(extension))
            {
                mimetype = mimetypesByExtension.get(extension);
            }
        }
        return mimetype;
    }

    // MimetypeBusinessInterface
    public Mimetype[] getMimetype(Mimetype mt)
    {
        String extension = mt.getFileExtension().toLowerCase();
        String mimetype = MIMETYPE_BINARY;
        List<Mimetype> risposta=new ArrayList<Mimetype>();
        /*if (mimetypesByExtension.containsKey(extension))
        {
            mimetype = mimetypesByExtension.get(extension);
        }
        mt.setMimetype(mimetype);*/
        for(MimetypeVO mime:arrayMimetype){
        	if(mime.getFileExtension().equals(extension)){
        		Mimetype temp=new Mimetype();
        		temp.setMimetype(mime.getMimeType());
        		risposta.add(temp);
        	}
        }
        Mimetype[] valori=new Mimetype[risposta.size()];
        return risposta.toArray(valori);
    }

}
