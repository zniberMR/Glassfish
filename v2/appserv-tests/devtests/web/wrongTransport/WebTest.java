/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.io.*;
import java.util.Properties;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebTest{


    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;

    static String host = null;
    static String port = null;

    public static void main(String args[]) throws Exception{
        host = args[0];
        port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("Wrong Protocol SSL test");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);
            System.out.println("Connecting to: " + "https://" + host  + ":" + port + "/");
            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/", ssf);
            checkStatus(connection); 
            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("wrongProtocol", stat.FAIL);
        }           

        stat.printSummary("wrongProtocol");
    }

    private static SSLSocketFactory getSSLSocketFactory(String trustStorePath)
                    throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private static HttpsURLConnection doSSLHandshake(String urlAddress,
                                                     SSLSocketFactory ssf)
                    throws Exception{
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });
        connection.setDoOutput(true);
        return connection;
    }

    private static void checkStatus(HttpsURLConnection connection)
                    throws Exception{

        int responseCode=connection.getResponseCode();
        String location = connection.getHeaderField("location");
        System.out.println("Response code: " + responseCode + " Expected code: 302"); 
        if (location!=null && location.equals("http://" + host + ":" + port + "/")){
            stat.addStatus("wrongProtocol", stat.PASS);
        } else {
            stat.addStatus("wrongProtocol", stat.FAIL);
        }
    }

    private static void parseResponse(HttpsURLConnection connection)
                    throws Exception{

       BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            
        String line = "";
        int index;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }

        in.close();
    }

    private static TrustManager[] getTrustManagers(String path)
                    throws Exception {

        TrustManager[] tms = null;
        InputStream istream = null;

        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            istream = new FileInputStream(path);
            trustStore.load(istream, null);
            istream.close();
            istream = null;
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();

        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }

        return tms;
    }

}
