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
package it.doqui.index.ecmengine.business.personalization.multirepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
//import java.text.MessageFormat;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.ContentStore;
//import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
//import org.alfresco.service.cmr.repository.ContentWriter;
//import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides direct access to a local file.
 * <p>
 * This class does not provide remote access to the file.
 *
 * @author Derek Hulley
 */
public class FileContentReaderDynamic extends AbstractContentReader
{
    /**
     * message key for missing content.  Parameters are
     * <ul>
     *    <li>{@link org.alfresco.service.cmr.repository.NodeRef NodeRef}</li>
     *    <li>{@link ContentReader ContentReader}</li>
     * </ul>
     */
    public static final String MSG_MISSING_CONTENT = "content.content_missing";

    private static final Log logger = LogFactory.getLog(FileContentReaderDynamic.class);

    private File file;
    private String protocol;
    private boolean allowRandomAccess;

    /**
     * Checks the existing reader provided and replaces it with a reader onto some
     * fake content if required.  If the existing reader is invalid, an debug message
     * will be logged under this classname category.
     * <p>
     * It is a convenience method that clients can use to cheaply get a reader that
     * is valid, regardless of whether the initial reader is valid.
     *
     * @param existingReader a potentially invalid reader or null
     * @param msgTemplate the template message that will used to format the final <i>fake</i> content
     * @param args arguments to put into the <i>fake</i> content
     * @return Returns a the existing reader or a new reader onto some generated text content
     */
    /*
    public static ContentReader getSafeContentReader(ContentReader existingReader, String msgTemplate, Object ... args)
    {
        ContentReader reader = existingReader;
        if (existingReader == null || !existingReader.exists())
        {
            // the content was never written to the node or the underlying content is missing
            String fakeContent = MessageFormat.format(msgTemplate, args);

            // log it
            if (logger.isDebugEnabled())
            {
                logger.debug(fakeContent);
            }

            // fake the content
            File tempFile = TempFileProvider.createTempFile("getSafeContentReader_", ".txt");
            ContentWriter writer = new FileContentWriterDynamic(tempFile,protocol);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(fakeContent);
            // grab the reader from the temp writer
            reader = writer.getReader();
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created safe content reader: \n" +
                    "   existing reader: " + existingReader + "\n" +
                    "   safe reader: " + reader);
        }
        return reader;
    }
    */

    /**
     * Constructor that builds a URL based on the absolute path of the file.
     *
     * @param file the file for reading.  This will most likely be directly
     *      related to the content URL.
     */
    public FileContentReaderDynamic(File file, String protocol)
    {
        this(
                file,
                protocol + ContentStore.PROTOCOL_DELIMITER + file.getAbsolutePath(),
                protocol );
    }

    /**
     * Constructor that explicitely sets the URL that the reader represents.
     *
     * @param file the file for reading.  This will most likely be directly
     *      related to the content URL.
     * @param url the relative url that the reader represents
     */
    public FileContentReaderDynamic(File file, String url, String protocol)
    {
        super(url);

        this.file = file;
        this.protocol = protocol;
        allowRandomAccess = true;
    }

    /* package */ void setAllowRandomAccess(boolean allow)
    {
        this.allowRandomAccess = allow;
    }

    /**
     * @return Returns the file that this reader accesses
     */
    public File getFile()
    {
        return file;
    }

    public boolean exists()
    {
        return file.exists();
    }

    /**
     * @see File#length()
     */
    public long getSize()
    {
        if (!exists())
        {
            return 0L;
        }
        else
        {
            return file.length();
        }
    }

    /**
     * @see File#lastModified()
     */
    public long getLastModified()
    {
        if (!exists())
        {
            return 0L;
        }
        else
        {
            return file.lastModified();
        }
    }

    /**
     * The URL of the write is known from the start and this method contract states
     * that no consideration needs to be taken w.r.t. the stream state.
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        FileContentReaderDynamic reader = new FileContentReaderDynamic(this.file, getContentUrl(),protocol);
        reader.setAllowRandomAccess(this.allowRandomAccess);
        return reader;
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        try
        {
            // the file must exist
            if (!file.exists())
            {
                throw new IOException("File does not exist: " + file);
            }
            // create the channel
            ReadableByteChannel channel = null;
            if (allowRandomAccess)
            {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");  // won't create it
                channel = randomAccessFile.getChannel();
            }
            else
            {
                InputStream is = new FileInputStream(file);
                channel = Channels.newChannel(is);
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Opened write channel to file: \n" +
                        "   file: " + file + "\n" +
                        "   random-access: " + allowRandomAccess);
            }
            return channel;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to open file channel: " + this, e);
        }
    }

    /**
     * @return Returns false as this is a reader
     */
    public boolean canWrite()
    {
        return false;   // we only allow reading
    }
}
