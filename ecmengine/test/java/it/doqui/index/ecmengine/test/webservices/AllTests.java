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

package it.doqui.index.ecmengine.test.webservices;

import it.doqui.index.ecmengine.test.util.EcmEngineTestConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.*;

public class AllTests extends TestCase implements EcmEngineTestConstants {

  protected transient Log log;

  public AllTests(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
	suite.addTestSuite(it.doqui.index.ecmengine.test.webservices.TestBackoffice.class);
	suite.addTestSuite(it.doqui.index.ecmengine.test.webservices.TestEngine.class);
	suite.addTestSuite(it.doqui.index.ecmengine.test.webservices.TestMassive.class);
	suite.addTestSuite(it.doqui.index.ecmengine.test.webservices.TestSecurity.class);
    return suite;
  }

  public static void main(String[] args)
    {
	    Log logger = LogFactory.getLog(ECMENGINE_TEST_LOG_CATEGORY);
	    logger.debug("[AllTests::main] BEGIN");
        String runner;
        int i;
        String arguments[];
        Class cls;

        runner = null;
        for (i = 0;(i < args.length) && (null == runner); i++)
        {
            if (args[i].equalsIgnoreCase("-text"))
                runner = "junit.textui.TestRunner";
            else if (args[i].equalsIgnoreCase("-awt"))
                runner = "junit.awtui.TestRunner";
            else if (args[i].equalsIgnoreCase("-swing"))
                runner = "junit.swingui.TestRunner";
        }
        if (null != runner)
        {
            // remove it from the arguments
            arguments = new String[args.length - 1];
            System.arraycopy(args, 0, arguments, 0, i - 1);
            System.arraycopy(args, i, arguments, i - 1, args.length - i);
            args = arguments;
        }
        else
            runner = "junit.swingui.TestRunner";

        // append the test class
        arguments = new String[args.length + 1];
        System.arraycopy(args, 0, arguments, 0, args.length);
        arguments[args.length] = "it.doqui.index.ecmengine.test.AllTests";

        // invoke main() of the test runner
        try {
            cls = Class.forName(runner);
            java.lang.reflect.Method method =
                cls.getDeclaredMethod("main", new Class[] { String[].class });
            method.invoke(null, new Object[] { arguments });
        } catch (Throwable t) {
    	    logger.debug("[AllTests::main] Problem in test execution : "+t);
        } finally {
    	    logger.debug("[AllTests::main] BEGIN");

        }
    }

}
