/*
 * @(#)file      Useful.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.4
 * @(#)date      07/04/04
 *
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL")(collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://opendmk.dev.java.net/legal_notices/licenses.txt or in the 
 * LEGAL_NOTICES folder that accompanied this code. See the License for the 
 * specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file found at
 *     http://opendmk.dev.java.net/legal_notices/licenses.txt
 * or in the LEGAL_NOTICES folder that accompanied this code.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.
 * 
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * 
 *       "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding
 * 
 *       "[Contributor] elects to include this software in this distribution
 *        under the [CDDL or GPL Version 2] license."
 * 
 * If you don't indicate a single choice of license, a recipient has the option
 * to distribute your version of this file under either the CDDL or the GPL
 * Version 2, or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the
 * GPL Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 * 
 *
 */

package com.sun.jdmk.internal;


import java.net.*;

public class Useful {
    public static boolean isLocalHost(String host) {
        boolean ret = false;
        try {
            ret = isLocalHost(InetAddress.getByName(host));
        } catch (Exception e) {}

        return ret;
    }

    public static boolean isLocalHost(InetAddress host) {
        if (host == null) {
            return false;
        }

        boolean ret = false;

        synchronized(lock) {
            for (int i=0; i<locals.length; i++) {
                if (locals[i].equals(host)) {
                    ret = true;
                    break;
                }
            }

	    if (!ret) {
		try {
		    ServerSocket ss = new ServerSocket(0, 2, host);
		    ss.close();
		    InetAddress[] tmp = new InetAddress[locals.length+1];
		    System.arraycopy(locals, 0, tmp, 0, locals.length);
		    tmp[locals.length] = host;

		    locals = tmp;
		
		    ret = true;
		} catch (Exception e) {
		    //e.printStackTrace();
		}
            }
        }

        return ret;
    }

    private static InetAddress[] locals = null;
    static {
	try {
	    locals = new InetAddress[1];
	    locals[0] = InetAddress.getLocalHost();
	} catch (Exception e) {
	    locals = new InetAddress[0];
	}
    }

    private static final int[] lock = new int[0];
}
