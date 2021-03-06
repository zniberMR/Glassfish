<%--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

  Portions Copyright Apache Software Foundation.

  The contents of this file are subject to the terms of either the GNU
  General Public License Version 2 only ("GPL") or the Common Development
  and Distribution License("CDDL") (collectively, the "License").  You
  may not use this file except in compliance with the License. You can obtain
  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
  language governing permissions and limitations under the License.

  When distributing the software, include this License Header Notice in each
  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
  Sun designates this particular file as subject to the "Classpath" exception
  as provided by Sun in the GPL Version 2 section of the License file that
  accompanied this code.  If applicable, add the following below the License
  Header, with the fields enclosed by brackets [] replaced by your own
  identifying information: "Portions Copyrighted [year]
  [name of copyright owner]"

  Contributor(s):

  If you wish your version of this file to be governed by only the CDDL or
  only the GPL Version 2, indicate your decision by adding "[Contributor]
  elects to include this software in this distribution under the [CDDL or GPL
  Version 2] license."  If you don't indicate a single choice of license, a
  recipient has the option to distribute your version of this file under
  either the CDDL, the GPL Version 2 or to extend the choice of license to
  its licensees as provided above.  However, if you add GPL Version 2 code
  and therefore, elected the GPL Version 2 license, then the option applies
  only if the new code is made subject to such option by the copyright
  holder.
--%>

<%@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"%>
<html>
  <head>
    <title>JSP 2.0 Examples - Dynamic Attributes</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - Dynamic Attributes</h1>
    <hr>
    <p>This JSP page invokes a custom tag that accepts a dynamic set 
    of attributes.  The tag echoes the name and value of all attributes
    passed to it.</p>
    <hr>
    <h2>Invocation 1 (six attributes)</h2>
    <ul>
      <my:echoAttributes x="1" y="2" z="3" r="red" g="green" b="blue"/>
    </ul>
    <h2>Invocation 2 (zero attributes)</h2>
    <ul>
      <my:echoAttributes/>
    </ul>
    <h2>Invocation 3 (three attributes)</h2>
    <ul>
      <my:echoAttributes dogName="Scruffy" 
	   		 catName="Fluffy" 
			 blowfishName="Puffy"/>
    </ul>
  </body>
</html>
