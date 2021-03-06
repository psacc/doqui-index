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

package it.doqui.index.ecmengine.business.startup.jboss;

//import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//import java.io.IOException;
//import java.util.Properties;

import javax.servlet.ServletContext;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

//import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.access.BeanFactoryLocator;
//import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextException;
//import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.support.PropertiesLoaderUtils;
//import org.springframework.util.ClassUtils;
//import org.springframework.util.StringUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by ContextLoaderListener and ContextLoaderServlet.
 *
 * <p>Looks for a "contextClass" parameter at the web.xml context-param level
 * to specify the context class type, falling back to the default of
 * {@link XmlWebApplicationContext} if not found. With the default ContextLoader
 * implementation, any context class specified needs to implement
 * ConfigurableWebApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" context-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by
 * any number of commas and spaces, like "applicationContext1.xml,
 * applicationContext2.xml". If not explicitly specified, the context
 * implementation is supposed to use a default location (with
 * XmlWebApplicationContext: "/WEB-INF/applicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Above and beyond loading the root application context, this class can
 * optionally load or obtain and hook up a shared parent context to the root
 * application context. See the
 * {@link #loadParentContext(ServletContext)} method for more information.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 17.02.2003
 * @see ContextLoaderListener
 * @see ContextLoaderServlet
 * @see ConfigurableWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 */
public class EcmWebApplicationContext extends ClassPathXmlApplicationContext implements WebApplicationContext {

    ServletContext s;

    public EcmWebApplicationContext(String configLocation) throws BeansException {
        super(new String[] {configLocation});
    }

    public EcmWebApplicationContext(String[] configLocations) throws BeansException {
        super(configLocations, (ApplicationContext) null);
    }

    public EcmWebApplicationContext(String[] configLocations, ApplicationContext parent)
            throws BeansException {
        super(configLocations,parent);
    }

    public EcmWebApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
        super(configLocations, refresh, null);
    }

    public EcmWebApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
            throws BeansException {
        super(configLocations,refresh,parent);
    }

    public EcmWebApplicationContext(String path, Class clazz) throws BeansException {
        super(new String[] {path}, clazz);
    }

    public EcmWebApplicationContext(String[] paths, Class clazz) throws BeansException {
        super(paths, clazz, null);
    }

    public EcmWebApplicationContext(String[] paths, Class clazz, ApplicationContext parent)
            throws BeansException {
        super(paths,clazz,parent);
    }

    public void setServletContext( ServletContext s )
    {
        this.s=s;
    }

    public ServletContext getServletContext()
    {
        return s;
    }

    private final Object oCount = new Object();
    private int nCount = 1;
    public void notifyNewInstance()
    {
        // Increase instance count
        synchronized (this.oCount) {
            nCount++;
        }
    }

    public void close()
    {
        // Close only last instance
        synchronized (this.oCount) {
            nCount--;
            //if( nCount==0 ){
                super.close();
            //}
        }
    }

}
