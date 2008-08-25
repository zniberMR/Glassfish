/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.InvocationManagerImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() throws NamingException {
        NamingManager.setInitialContextFactoryBuilder(new GFInitialContextFactoryBuilder());
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    public void testCreateNewInitialContext() {
        try {
            new InitialContext();
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testBind() {
        GlassfishNamingManager nm = null;
        try {
            InvocationManager im = new InvocationManagerImpl();
            nm = new GlassfishNamingManagerImpl();
            nm.publishObject("foo", "Hello: foo", false);
            InitialContext ic = new InitialContext();
            System.out.println("**lookup() ==> " + ic.lookup("foo"));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
            try {
                nm.unpublishObject("foo");
            } catch (Exception ex) {

            }
        }
    }

    public void testEmptySubContext() {
        try {
            String name1 = "rmi://a//b/c/d/name1";
            String name2 = "rmi://a//b/c/d/name2";

            Context ctx = new InitialContext();
            ctx.bind(name1, "Name1");
            ctx = new InitialContext();
            ctx.bind(name2, "Name2");

	    Object v1 = ctx.lookup(name1);
	    Object v2 = ctx.lookup(name2);

	    Context ctx2 = (Context) (new InitialContext()).lookup("rmi://a//b");
	    Object val1 = ctx2.lookup("c/d/name1");
	    Object val2 = ctx2.lookup("c/d/name2");

            System.out.println("testEmptySubContext: " + v1 + "  " + v2);
            System.out.println("testEmptySubContext: " + val1 + "  " + val2);
            boolean check1 = v1.equals("Name1") && v2.equals("Name2");
            boolean check2 = val1.equals("Name1") && val2.equals("Name2");
	
	    assert(check1 && check2);

        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
        }
    }


    public void testCachingNamingObjectFactory() {
        GlassfishNamingManager nm = null;
        try {
            InvocationManager im = new InvocationManagerImpl();
            nm = new GlassfishNamingManagerImpl();
            nm.publishObject("foo", "Hello: foo", false);
            InitialContext ic = new InitialContext();
            System.out.println("**lookup() ==> " + ic.lookup("foo"));

            NamingObjectFactory factory = new NamingObjectFactory() {
                private int counter = 1;

                public boolean isCreateResultCacheable() {
                    return true;
                }

                public Object create(Context ic) {
                    return ("FACTORY_Created: " + counter++);
                }

                public String toString() {
                    return "Huh? ";
                }
            };
            nm.publishObject("bar", factory, false);
            System.out.println("**lookup() ==> " + ic.lookup("bar"));
            System.out.println("**lookup() ==> " + ic.lookup("bar"));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
            try {
                nm.unpublishObject("foo");
                nm.unpublishObject("bar");
            } catch (Exception ex) {

            }
        }
    }

    private static class Binding
            implements JNDIBinding {
        String logicalName;
        Object value;

        public Binding(String logicalName, Object value) {
            this.logicalName = "java:comp/env/" + logicalName;
            this.value = value;
        }

        public String getName() {
            return logicalName;
        }

        public String getJndiName() {
            return null;
        }

        public Object getValue() {
            return value;
        }
   } 

    public void testEmptyJavaCompEnv() {
        GlassfishNamingManagerImpl nm = null;
        InvocationManager im = new InvocationManagerImpl();
        ComponentInvocation inv = null;
        try {
            nm = new GlassfishNamingManagerImpl();
            nm.setInvocationManager(im);

            inv = new ComponentInvocation("comp1",
                    ComponentInvocation.ComponentInvocationType.EJB_INVOCATION,
                    null, null, null);
            im.preInvoke(inv);
            InitialContext ic = new InitialContext();
            Context ctx = (Context) ic.lookup("java:comp/env");
            System.out.println("**lookup(java:comp/env) ==> " + ctx);
            assert(true);
        } catch (javax.naming.NamingException nnfEx) {
            nnfEx.printStackTrace();
            assert (false);
        }
    }

    public void testNonCachingNamingObjectFactory() {
        GlassfishNamingManagerImpl nm = null;
        InvocationManager im = new InvocationManagerImpl();
        ComponentInvocation inv = null;
        try {
            nm = new GlassfishNamingManagerImpl();
            nm.setInvocationManager(im);

            List<Binding> bindings =
                    new ArrayList<Binding>();

            NamingObjectFactory intFactory = new NamingObjectFactory() {
                private int counter = 1;

                public boolean isCreateResultCacheable() {
                    return false;
                }

                public Object create(Context ic) {
                    return new Integer(++counter);
                }

                public String toString() {
                    return "Huh? ";
                }
            };
            bindings.add(new Binding("conf/area", intFactory));
            bindings.add(new Binding("conf/location", "Santa Clara"));

            nm.bindToComponentNamespace("app1", "comp1", bindings);

            inv = new ComponentInvocation("comp1",
                    ComponentInvocation.ComponentInvocationType.EJB_INVOCATION,
                    null, null, null);
            im.preInvoke(inv);
            InitialContext ic = new InitialContext();
            System.out.println("**lookup(java:comp/env/conf/area) ==> " + ic.lookup("java:comp/env/conf/area"));
            System.out.println("**lookup(java:comp/env/conf/location) ==> " + ic.lookup("java:comp/env/conf/location"));

            NamingObjectFactory floatFactory = new NamingObjectFactory() {
                private int counter = 1;

                public boolean isCreateResultCacheable() {
                    return false;
                }

                public Object create(Context ic) {
                    return Float.valueOf(("7" + (++counter)) + "." + 2323);
                }

                public String toString() {
                    return "Huh? ";
                }
            };
            List<Binding> bindings2 =
                    new ArrayList<Binding>();
            bindings2.add(new Binding("conf/area", floatFactory));
            bindings2.add(new Binding("conf/location", "Santa Clara[14]"));

            nm.bindToComponentNamespace("app1", "comp2", bindings2);

            inv = new ComponentInvocation("comp2",
                    ComponentInvocation.ComponentInvocationType.EJB_INVOCATION,
                    null, null, null);
            im.preInvoke(inv);
            System.out.println("**lookup(java:comp/env/conf/area) ==> " + ic.lookup("java:comp/env/conf/area"));
            System.out.println("**lookup(java:comp/env/conf/location) ==> " + ic.lookup("java:comp/env/conf/location"));

            assert (true);
        } catch (InvocationException inEx) {
            inEx.printStackTrace();
            assert(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
            try {
                im.postInvoke(inv);
                nm.unbindObjects("comp1");
            } catch (InvocationException inEx) {
                
            } catch (Exception ex) {

            }
        }
    }

}
