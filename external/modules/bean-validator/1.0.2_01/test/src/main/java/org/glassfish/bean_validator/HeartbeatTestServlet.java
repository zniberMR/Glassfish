/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.bean_validator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

public class HeartbeatTestServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.print("<html><head><title>HeartbeatTestServlet</title></head><body>");

            ValidatorFactory validatorFactory = null;

            validatorFactory = Validation.buildDefaultValidatorFactory();

            if (null != validatorFactory) {

                out.print("<p>");
                out.print("Obtained ValidatorFactory: " + validatorFactory + ".");
                out.print("</p>");
            }

            ValidatorContext validatorContext = validatorFactory.usingContext();
            javax.validation.Validator beanValidator = validatorContext.getValidator();

            out.print("<h1>");
            out.print("Validating person class using validateValue with valid property");
            out.print("</h1>");

            List<String> listOfString = new ArrayList<String>();
            listOfString.add("one");
            listOfString.add("two");
            listOfString.add("three");

            Set<ConstraintViolation<Person>> violations =
                    beanValidator.validateValue(Person.class, "listOfString", listOfString);

            printConstraintViolations(out, violations, "case1");

            out.print("<h1>");
            out.print("Validating person class using validateValue with invalid property");
            out.print("</h1>");

            try {
                violations =
                        beanValidator.validateValue(Person.class, "nonExistentProperty", listOfString);
            } catch (IllegalArgumentException iae) {
                out.print("<p>");
                out.print("case2: caught IllegalArgumentException.  Message: "
                          + iae.getMessage());
                out.print("</p>");
            }
            Person person = new Person();

            out.print("<h1>");
            out.print("Validating invalid person instance using validate.");
            out.print("</h1>");

            violations = beanValidator.validate(person);

            printConstraintViolations(out, violations, "case3");

            out.print("<h1>");
            out.print("Validating valid person.");
            out.print("</h1>");

            person.setFirstName("John");
            person.setLastName("Yaya");
            person.setListOfString(listOfString);

            violations = beanValidator.validate(person);
            printConstraintViolations(out, violations, "case4");

            out.print("</body></html>");
        } finally {
            out.close();
        }
    }

    private void printConstraintViolations(PrintWriter out,
            Set<ConstraintViolation<Person>> violations, String caseId) {
        if (violations.isEmpty()) {
            out.print("<p>");
            out.print(caseId + ": No ConstraintViolations found.");
            out.print("</p>");
        } else {
            for (ConstraintViolation<Person> curViolation : violations) {
                out.print("<p>");
                out.print(caseId + ": ConstraintViolation: message: " + curViolation.getMessage() +
                        " propertyPath: " + curViolation.getPropertyPath());
                out.print("</p>");
            }
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
