/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.nbbuild;
import java.io.File;
import java.util.jar.JarFile;
import junit.framework.AssertionFailedError;

/** Checks that javac.target gets reflected in the manifest.
 *
 * @author Jaroslav Tulach
 */
public class JarWithModuleAttributesTest extends TestBase {
    public JarWithModuleAttributesTest (String name) {
        super (name);
    }
    
    public void testAddThereVersionFromJavacTarget() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = extractString (
"OpenIDE-Module: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"OpenIDE-Module-Specification-Version: 1.9\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='2.87'/>" +
            "  <property name='public.packages' value=''/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        execute (f, new String[] { "-verbose" });
        
        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNotNull("Attribute created:\n" + readFile(extracted), value);

        String[] arr = value.split(">");
        assertEquals("Two parts", 2, arr.length);
        assertEquals("Java", arr[0].trim());

        assertVersionAtLeast("2.87", arr[1]);
    }

    public void testKeepOldVersion() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = extractString (
"OpenIDE-Module: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"OpenIDE-Module-Specification-Version: 1.9\n" +
"OpenIDE-Module-Java-Dependencies: Java > 1.3\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='2.87'/>" +
            "  <property name='public.packages' value=''/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        execute (f, new String[] { "-verbose" });
        
        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNotNull("Attribute created:\n" + readFile(extracted), value);

        String[] arr = value.split(">");
        assertEquals("Two parts", 2, arr.length);
        assertEquals("Java", arr[0].trim());

        assertVersionAtLeast("1.3", arr[1]);
        boolean fail;
        try {
            assertVersionAtLeast("1.4", arr[1]);
            fail = true;
        } catch (AssertionFailedError ex) {
            fail = false;
        }
        if (fail) {
            fail("Version shall not be 1.4 or higher, as it is specified in manifest to be 1.3: " + arr[1]);
        }
    }

    public void testKeepOldVersionForNetigso() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = extractString (
"Bundle-SymbolicName: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"Bundle-Version: 1.9\n" +
"Bundle-RequireExecutionEnvironment: J2SE-1.3\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='2.87'/>" +
            "  <property name='public.packages' value='x.y.ahoj'/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        execute (f, new String[] { "-verbose" });

        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNull("We are in Netigso mode", value);
        value = file.getManifest().getMainAttributes().getValue("Bundle-RequireExecutionEnvironment");
        assertNotNull("Attribute created:\n" + readFile(extracted), value);

        String[] arr = value.split("-");
        assertEquals("Two parts", 2, arr.length);
        if (arr[0].trim().equals("J2SE") || arr[0].trim().equals("JavaSE")) {
            // OK
        } else {
            fail("Unexpected value: " + value);
        }

        assertVersionAtLeast("1.3", arr[1]);
        boolean fail;
        try {
            assertVersionAtLeast("1.4", arr[1]);
            fail = true;
        } catch (AssertionFailedError ex) {
            fail = false;
        }
        if (fail) {
            fail("Version shall not be 1.4 or higher, as it is specified in manifest to be 1.3: " + arr[1]);
        }

        String bundleV = file.getManifest().getMainAttributes().getValue("Bundle-Version");
        assertEquals("Correct version of the module", "1.9", bundleV);

        String manV = file.getManifest().getMainAttributes().getValue("Bundle-ManifestVersion");
        assertEquals("Manifest version shall be specified", "2", manV);

        String pkgs = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Public-Packages");
        assertNull("No exported packages, we are in Netigso mode", pkgs);

        pkgs = file.getManifest().getMainAttributes().getValue("Export-Package");
        assertEquals("We want exported packages", "x.y.ahoj", pkgs);
    }

    public void testIgnoreWeirdJavacTarget() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = extractString (
"OpenIDE-Module: org.netbeans.modules.sendopts\n" +
"OpenIDE-Module-Localizing-Bundle: org/netbeans/modules/sendopts/Bundle.properties\n" +
"OpenIDE-Module-Specification-Version: 1.9\n" +
"OpenIDE-Module-Layer: org/netbeans/modules/sendopts/layer.xml\n"
        );
        File jar = new File(getWorkDir(), "x.jar");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='javac.target' value='jsr99'/>" +
            "  <property name='public.packages' value=''/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        execute (f, new String[] { "-verbose" });
        
        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Java-Dependencies");
        assertNull("Attribute not created:\n" + readFile(extracted), value);
    }

    public void testExportPackage() throws Exception {
        File output = new File(getWorkDir(), "output");
        java.io.File manifest = extractString (
"Bundle-SymbolicName: org.netbeans.modules.sendopts\n" +
"  \n" +
"  \n\n\n"
        );
        File jar = new File(getWorkDir(), "x.jar");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"njar\" classname=\"org.netbeans.nbbuild.JarWithModuleAttributes\" classpath=\"${nb_all}/nbbuild/nbantext.jar\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <property name='public.packages' value='org.netbeans.api.sendopts.*'/>" +
            "  <property name='buildnumber' value='BLDprivateTESTBuild'/>" +
            "  <property name='code.name.base.slashes' value='org/netbeans/modules/sendopts'/>" +
            "  <property name='spec.version.base' value='1.9'/>" +
            "  <property name='module.dependencies' value='com.othercom.anothermodule > 2.1.3,org.netbeans.libs.osgi > 1.1,org.netbeans.modules.applet/1 > 1.7,nospec/3'/>" +
            "  <njar manifest='" + manifest + "'   destfile='" + jar + "'>" +
            "  </njar>" +
            "  <unzip src='" + jar + "' dest='" + output + "'/>" +
            "</target>" +
            "</project>"
        );


        execute (f, new String[] { "-verbose" });

        assertTrue ("JAR created", jar.isFile());

        File extracted = new File(new File(output, "META-INF"), "MANIFEST.MF");
        assertTrue("Manifest extracted", extracted.isFile());

        JarFile file = new JarFile(jar);
        String value = file.getManifest().getMainAttributes().getValue("Export-Package");
        assertEquals("org.netbeans.api.sendopts", value);

        String req = file.getManifest().getMainAttributes().getValue("Require-Bundle");
        if (req.indexOf("com.othercom.anothermodule;bundle-version=\"[2.1.3,100)\"") == -1) {
            fail("Wrong dependency on com.othercom.anothermodule:\n" + req);
        }

        if (req.indexOf("org.netbeans.modules.applet;bundle-version=\"[101.7,200)\"") == -1) {
            fail("Wrong dependency on applet/1:\n" + req);
        }
        if (req.indexOf("nospec;bundle-version=\"[300,400)\"") == -1) {
            fail("Wrong dependency on nospec/3:\n" + req);
        }
        if (req.indexOf("org.netbeans.libs.osgi") != -1) {
            fail("Unwanted dependency on org.netbeans.libs.osgi:\n" + req);
        }
    }
    
    private static void assertVersionAtLeast(String limit, String value) {
        int[] segLimit = segments(limit);
        int[] segValue = segments(value);

        for (int i = 0; i < segLimit.length && i < segValue.length; i++) {
            if (segValue[i] < segLimit[i]) {
                fail("Version is younger than it should be. Expected: " + limit + " was: " + value);
            }
            if (segValue[i] > segLimit[i]) {
                return;
            }
        }
    }

    private static int[] segments(String version) {
        String[] arr = version.split("\\.");
        int[] ret = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i] = Integer.parseInt(arr[i].trim());
        }
        return ret;
    }
}
