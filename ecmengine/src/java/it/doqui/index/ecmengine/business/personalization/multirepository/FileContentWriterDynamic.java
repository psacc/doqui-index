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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides direct access to a local file.
 * <p>
 * This class does not provide remote access to the file.
 *
 * @author Derek Hulley
 */
public class FileContentWriterDynamic extends AbstractContentWriter
{
    private static final Log logger = LogFactory.getLog(FileContentWriterDynamic.class);

    private File file;
    private String protocol;
    private boolean allowRandomAccess;

    /**
     * Constructor that builds a URL based on the absolute path of the file.
     *
     * @param file the file for writing.  This will most likely be directly
     *      related to the content URL.
     */
    public FileContentWriterDynamic(File file, String protocol)
    {
        this(file, null, protocol);
    }

    /**
     * Constructor that builds a URL based on the absolute path of the file.
     *
     * @param file the file for writing.  This will most likely be directly
     *      related to the content URL.
     * @param existingContentReader a reader of a previous version of this content
     */
    public FileContentWriterDynamic(File file, ContentReader existingContentReader, String protocol)
    {
        this(
                file,
                protocol + ContentStore.PROTOCOL_DELIMITER + file.getAbsolutePath(),
                existingContentReader,
                protocol );
    }

    /**
     * Constructor that explicitely sets the URL that the reader represents.
     *
     * @param file the file for writing.  This will most likely be directly
     *      related to the content URL.
     * @param url the relative url that the reader represents
     * @param existingContentReader a reader of a previous version of this content
     */
    public FileContentWriterDynamic(File file, String url, ContentReader existingContentReader, String protocol)
    {
        super(url, existingContentReader);

        this.file = file;
        this.protocol = protocol;
        allowRandomAccess = true;
    }

    /* package */ void setAllowRandomAccess(boolean allow)
    {
        this.allowRandomAccess = allow;
    }

    /**
     * @return Returns the file that this writer accesses
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return Returns the size of the underlying file or
     */
    public long getSize()
    {
        if (file == null)
            return 0L;
        else if (!file.exists())
            return 0L;
        else
            return file.length();
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
    protected WritableByteChannel getDirectWritableChannel() throws ContentIOException
    {
        try
        {
            // we may not write to an existing file - EVER!!
            if (file.exists() && file.length() > 0)
            {
                throw new IOException("File exists - overwriting not allowed");
            }
            // create the channel
            WritableByteChannel channel = null;
            if (allowRandomAccess)
            {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");  // will create it
                channel = randomAccessFile.getChannel();
            }
            else
            {
                OutputStream os = new FileOutputStream(file);
                channel = Channels.newChannel(os);
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
     * @return Returns true always
     */
    public boolean canWrite()
    {
        return true;    // this is a writer
    }
}
