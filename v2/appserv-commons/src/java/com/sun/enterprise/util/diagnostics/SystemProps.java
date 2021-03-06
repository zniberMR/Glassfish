/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * SystemProps.java
 *
 * Created on October 2, 2001, 2:53 PM
 */

package com.sun.enterprise.util.diagnostics;

import java.util.*;
import com.sun.enterprise.util.StringUtils;

/**
 *
 * @author  bnevins
 * @version 
 */
public class SystemProps 
{
    public static List get() 
	{
		Properties p = System.getProperties();
		// these 2 lines woul;d be nice -- but it's case-sensitive...
		//Map sortedMap = new TreeMap(p);
		//Set sortedSet = sortedMap.entrySet();
		Set		set = p.entrySet();
		List	list = new ArrayList(set);
		Collections.sort(list, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				Map.Entry me1 = (Map.Entry)o1;
				Map.Entry me2 = (Map.Entry)o2;
				
				return ((String)me1.getKey()).compareToIgnoreCase((String)me2.getKey());
			}
		});
		return list;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static String toStringStatic()
	{
		int				longestKey	= 0;
		List			list		= get();
		StringBuffer	sb			= new StringBuffer();

		for(Iterator it = list.iterator(); it.hasNext(); )
		{
			Map.Entry entry = (Map.Entry)it.next();
			int len = ((String)entry.getKey()).length();

			if(len > longestKey)
				longestKey = len;
		}

		longestKey += 1;

		for(Iterator it = list.iterator(); it.hasNext(); )
		{
			Map.Entry entry = (Map.Entry)it.next();
			sb.append(StringUtils.padRight((String)entry.getKey(), longestKey));
			sb.append("= ");
			sb.append((String)entry.getValue());
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private SystemProps()
	{
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		System.out.println(toStringStatic());
	}
}
