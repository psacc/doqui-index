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
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.EmptyContentReader;
import org.alfresco.repo.content.UnsupportedContentUrlException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a store of node content directly to the file system.  The writers
 * are generated using information from the {@link ContentContext simple content context}.
 * <p>
 * The file names obey, as they must, the URL naming convention
 * as specified in the {@link org.alfresco.repo.content.ContentStore ContentStore interface}.
 *
 * @author Derek Hulley
 */
public class FileContentStoreDynamic extends AbstractContentStore implements ContentStoreDynamic
{
    /**
     * <b>store</b> is the new prefix for file content URLs
     * @see ContentStore#PROTOCOL_DELIMITER
     */
    private static final Log logger = LogFactory.getLog(FileContentStoreDynamic.class);

    private File rootDirectory;
    private String rootAbsolutePath;
    private boolean allowRandomAccess;
    private boolean readOnly;
    private String protocol;

    /**
     * Create an empty FileContentStoreDynamic
     *
     * @see FileContentStoreDynamic#setRootDirectory(File)
     */
    public FileContentStoreDynamic() {}

    public void setProtocol(String protocol)
    {
       this.protocol = protocol;
    }
    public String getProtocol()
    {
       return protocol;
    }

    /**
     * @param rootDirectoryStr  the root under which files will be stored.
     *                          The directory will be created if it does not exist.
     *
     * @see FileContentStoreDynamic#setRootDirectory(File)
     */
    public void setResource(String rootDirectoryStr)
    {
        setResource(new File(rootDirectoryStr));
    }

    /**
     * @param rootDirectory     the root under which files will be stored.
     *                          The directory will be created if it does not exist.
     */
    public void setResource(File rootDirectory)
    {
        if (!rootDirectory.exists())
        {
            if (!rootDirectory.mkdirs())
            {
                throw new ContentIOException("Failed to create store root: " + rootDirectory, null);
            }
        }
        this.rootDirectory = rootDirectory.getAbsoluteFile();
        rootAbsolutePath = rootDirectory.getAbsolutePath();
        allowRandomAccess = true;
        readOnly = false;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder(36);
        sb.append("FileContentStoreDynamic")
          .append("[ root=").append(rootDirectory)
          .append(", allowRandomAccess=").append(allowRandomAccess)
          .append(", readOnly=").append(readOnly)
          .append(", protocol=").append(protocol)
          .append("]");
        return sb.toString();
    }

    /**
     * Stores may optionally produce readers and writers that support random access.
     * Switch this off for this store by setting this to <tt>false</tt>.
     * <p>
     * This switch is primarily used during testing to ensure that the system has the
     * ability to spoof random access in cases where the store is unable to produce
     * readers and writers that allow random access.  Typically, stream-based access
     * would be an example.
     *
     * @param allowRandomAccess true to allow random access, false to have it faked
     */
    public void setAllowRandomAccess(boolean allowRandomAccess)
    {
        this.allowRandomAccess = allowRandomAccess;
    }

    /**
     * File stores may optionally be declared read-only.  This is useful when configuring
     * a store, possibly temporarily, to act as a source of data but to preserve it against
     * any writes.
     *
     * @param readOnly      <tt>true</tt> to force the store to only allow reads.
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * Generates a new URL and file appropriate to it.
     *
     * @return Returns a new and unique file
     * @throws IOException if the file or parent directories couldn't be created
     */
    private File createNewFile() throws IOException
    {
        String contentUrl = createNewFileStoreUrl();
        return createNewFile(contentUrl);
    }

    /**
     * Creates a file for the specifically provided content URL.  The URL may
     * not already be in use.
     * <p>
     * The store prefix is stripped off the URL and the rest of the URL
     * used directly to create a file.
     *
     * @param newContentUrl the specific URL to use, which may not be in use
     * @return Returns a new and unique file
     * @throws IOException
     *      if the file or parent directories couldn't be created or if the URL is already in use.
     * @throws UnsupportedOperationException
     *      if the store is read-only
     *
     * @see #setReadOnly(boolean)
     */
    private File createNewFile(String newContentUrl) throws IOException
    {
        if (readOnly)
        {
            throw new UnsupportedOperationException("This store is currently read-only: " + this);
        }

        File file = makeFile(newContentUrl);

        // create the directory, if it doesn't exist
        File dir = file.getParentFile();
        if (!dir.exists())
        {
            makeDirectory(dir);
        }

        // create a new, empty file
        boolean created = file.createNewFile();
        if (!created)
        {
            throw new ContentIOException(
                    "When specifying a URL for new content, the URL may not be in use already. \n" +
                    "   store: " + this + "\n" +
                    "   new URL: " + newContentUrl);
        }

        // done
        return file;
    }

    /**
     * Synchronized and retrying directory creation.  Repeated attempts will be made to create the
     * directory, subject to a limit on the number of retries.
     *
     * @param dir               the directory to create
     * @throws IOException      if an IO error occurs
     */
    private synchronized void makeDirectory(File dir) throws IOException
    {
        /*
         * Once in this method, the only contention will be from other file stores or processes.
         * This is OK as we have retrying to sort it out.
         */
        if (dir.exists())
        {
            // Beaten to it during synchronization
            return;
        }
        // 20 attempts with 20 ms wait each time
        for (int i = 0; i < 20; i++)
        {
            boolean created = dir.mkdirs();
            if (created)
            {
                // Successfully created
                return;
            }
            // Wait
            try { this.wait(20L); } catch (InterruptedException e) {}
            // Did it get created in the meantime
            if (dir.exists())
            {
                // Beaten to it while asleep
                return;
            }
        }
        // It still didn't succeed
        throw new ContentIOException("Failed to create directory for file storage: " +  dir);
    }

    /**
     * Takes the file absolute path, strips off the root path of the store
     * and appends the store URL prefix.
     *
     * @param file the file from which to create the URL
     * @return Returns the equivalent content URL
     * @throws Exception
     */
    private String makeContentUrl(File file)
    {
        String path = file.getAbsolutePath();
        // check if it belongs to this store
        if (!path.startsWith(rootAbsolutePath))
        {
            throw new AlfrescoRuntimeException(
                    "File does not fall below the store's root: \n" +
                    "   file: " + file + "\n" +
                    "   store: " + this);
        }
        // strip off the file separator char, if present
        int index = rootAbsolutePath.length();
        if (path.charAt(index) == File.separatorChar)
        {
            index++;
        }
        // strip off the root path and adds the protocol prefix
        String url = protocol + ContentStore.PROTOCOL_DELIMITER + path.substring(index);
        // replace '\' with '/' so that URLs are consistent across all filesystems
        url = url.replace('\\', '/');
        // done
        return url;
    }

    /**
     * Creates a file from the given relative URL.
     *
     * @param contentUrl    the content URL including the protocol prefix
     * @return              Returns a file representing the URL - the file may or may not
     *                      exist
     * @throws UnsupportedContentUrlException
     *                      if the URL is invalid and doesn't support the
     *                      {@link FileContentStoreDynamic#STORE_PROTOCOL correct protocol}
     *
     * @see #checkUrl(String)
     */
    private File makeFile(String contentUrl)
    {
        // take just the part after the protocol
        Pair<String, String> urlParts = super.getContentUrlParts(contentUrl);
        String protocol = urlParts.getFirst();
        String relativePath = urlParts.getSecond();
        // Check the protocol
        if (!protocol.equals(protocol))
        {
            throw new UnsupportedContentUrlException(this, contentUrl);
        }
        // get the file
        File file = new File(rootDirectory, relativePath);
        // done
        return file;
    }

    /**
     * @return      Returns <tt>true</tt> always
     */
    public boolean isWriteSupported()
    {
        return !readOnly;
    }

    /**
     * Performs a direct check against the file for its existence.
     */
    @Override
    public boolean exists(String contentUrl)
    {
        File file = makeFile(contentUrl);
        return file.exists();
    }

    /**
     * This implementation requires that the URL start with
     * {@link FileContentStoreDynamic#STORE_PROTOCOL }.
     */
    public ContentReader getReader(String contentUrl)
    {
        try
        {
            File file = makeFile(contentUrl);
            ContentReader reader = null;
            if (file.exists())
            {
                FileContentReaderDynamic FileContentReaderDynamic = new FileContentReaderDynamic(file, contentUrl, protocol);
                FileContentReaderDynamic.setAllowRandomAccess(allowRandomAccess);
                reader = FileContentReaderDynamic;
            }
            else
            {
                reader = new EmptyContentReader(contentUrl);
            }

            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created content reader: \n" +
                        "   url: " + contentUrl + "\n" +
                        "   file: " + file + "\n" +
                        "   reader: " + reader);
            }
            return reader;
        }
        catch (UnsupportedContentUrlException e)
        {
            // This can go out directly
            throw e;
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Failed to get reader for URL: " + contentUrl, e);
        }
    }

    /**
     * @return Returns a writer onto a location based on the date
     */
    public ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
    {
        try
        {
            File file = null;
            String contentUrl = null;
            if (newContentUrl == null)              // a specific URL was not supplied
            {
                // get a new file with a new URL
                file = createNewFile();
                // make a URL
                contentUrl = makeContentUrl(file);
            }
            else                                    // the URL has been given
            {
                file = createNewFile(newContentUrl);
                contentUrl = newContentUrl;
            }
            // create the writer
            FileContentWriterDynamic writer = new FileContentWriterDynamic(file, contentUrl, existingContentReader, protocol);
            writer.setAllowRandomAccess(allowRandomAccess);

            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created content writer: \n" +
                        "   writer: " + writer);
            }
            return writer;
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to get writer", e);
        }
    }

    public Set<String> getUrls(Date createdAfter, Date createdBefore)
    {
        // recursively get all files within the root
        Set<String> contentUrls = new HashSet<String>(1000);
        getUrls(rootDirectory, contentUrls, createdAfter, createdBefore);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Listed all content URLS: \n" +
                    "   store: " + this + "\n" +
                    "   count: " + contentUrls.size());
        }
        return contentUrls;
    }

    /**
     * @param directory the current directory to get the files from
     * @param contentUrls the list of current content URLs to add to
     * @param createdAfter only get URLs for content create after this date
     * @param createdBefore only get URLs for content created before this date
     * @return Returns a list of all files within the given directory and all subdirectories
     */
    private void getUrls(File directory, Set<String> contentUrls, Date createdAfter, Date createdBefore)
    {
        File[] files = directory.listFiles();
        if (files == null)
        {
            // the directory has disappeared
            throw new ContentIOException("Failed list files in folder: " + directory);
        }
        for (File file : files)
        {
            if (file.isDirectory())
            {
                // we have a subdirectory - recurse
                getUrls(file, contentUrls, createdAfter, createdBefore);
            }
            else
            {
                // check the created date of the file
                long lastModified = file.lastModified();
                if (createdAfter != null && lastModified < createdAfter.getTime())
                {
                    // file is too old
                    continue;
                }
                else if (createdBefore != null && lastModified > createdBefore.getTime())
                {
                    // file is too young
                    continue;
                }
                // found a file - create the URL
                String contentUrl = makeContentUrl(file);
                contentUrls.add(contentUrl);
            }
        }
    }

    /**
     * Attempts to delete the content.  The actual deletion is optional on the interface
     * so it just returns the success or failure of the underlying delete.
     *
     * @throws UnsupportedOperationException        if the store is read-only
     *
     * @see #setReadOnly(boolean)
     */
    public boolean delete(String contentUrl)
    {
        if (readOnly)
        {
            throw new UnsupportedOperationException("This store is currently read-only: " + this);
        }
        // ignore files that don't exist
        File file = makeFile(contentUrl);
        boolean deleted = false;
        if (!file.exists())
        {
            deleted = true;
        }
        else
        {
            deleted = file.delete();
        }

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Delete content directly: \n" +
                    "   store: " + this + "\n" +
                    "   url: " + contentUrl);
        }
        return deleted;
    }

    /**
     * Creates a new content URL.  This must be supported by all
     * stores that are compatible with Alfresco.
     *
     * @return Returns a new and unique content URL
     */
    public String createNewFileStoreUrl()
    {
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(protocol)
          .append(ContentStore.PROTOCOL_DELIMITER)
          .append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(hour).append('/')
          .append(minute).append('/')
          .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        // done
        return newContentUrl;
    }
}
