/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Mike Norman - May 2008, created DBWS Oracle test package
 ******************************************************************************/
package dbws.testing.visit;

//javase imports
import java.io.IOException;
import java.io.StringReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//java eXtension imports
import javax.annotation.PostConstruct;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import static javax.xml.ws.Service.Mode.MESSAGE;

//JUnit4 imports
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

//EclipseLink imports
import org.eclipse.persistence.tools.dbws.ProcedureOperationModel;

// test imports
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_BUILD_TBL1;
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_BUILD_TBL1_NAMESPACE;
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_BUILD_TBL1_PORT;
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_BUILD_TBL1_SERVICE;
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_BUILD_TBL1_SERVICE_NAMESPACE;
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_BUILD_TBL1_TEST;
import static dbws.testing.visit.DBWSTestHelper.ADVJDBC_TOPLEVEL_PACKAGE_NAME;

@WebServiceProvider(
    targetNamespace = ADVJDBC_BUILD_TBL1_SERVICE_NAMESPACE,
    serviceName = ADVJDBC_BUILD_TBL1_SERVICE,
    portName = ADVJDBC_BUILD_TBL1_PORT
)
@ServiceMode(MESSAGE)
public class BuildTbl1TestWebServiceSuite extends WebServiceTestSuite implements Provider<SOAPMessage> {

    static final String ENDPOINT_ADDRESS = "http://localhost:9999/" + ADVJDBC_BUILD_TBL1_TEST;

    @BeforeClass
    public static void setUp() throws WSDLException {
        builder.setProjectName(ADVJDBC_BUILD_TBL1);
        builder.setTargetNamespace(ADVJDBC_BUILD_TBL1_NAMESPACE);
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName(ADVJDBC_BUILD_TBL1_TEST);
        pModel.setIsAdvancedJDBCProcedureOperation(true);
        pModel.setCatalogPattern(ADVJDBC_TOPLEVEL_PACKAGE_NAME);
        pModel.setProcedurePattern(ADVJDBC_BUILD_TBL1);
        pModel.setReturnType("somepackage_tbl1Type");
        builder.getOperations().add(pModel);
        serviceSetup(ENDPOINT_ADDRESS, new BuildTbl1TestWebServiceSuite());
    }

    @PostConstruct
    public void init() {
        super.init();
    }

    static final String REQUEST_MSG = 
        "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
          "<env:Header/>" +
          "<env:Body>" +
            "<srvc:" + ADVJDBC_BUILD_TBL1_TEST + " xmlns:srvc=\"" + 
                ADVJDBC_BUILD_TBL1_SERVICE_NAMESPACE + 
                "\" xmlns=\"" + ADVJDBC_BUILD_TBL1_NAMESPACE + "\">" +
              "<srvc:NUM>3</srvc:NUM>" +
            "</srvc:" + ADVJDBC_BUILD_TBL1_TEST + ">" +
          "</env:Body>" +
        "</env:Envelope>";
    @Test
    public void buildTbl1Test() throws SOAPException, IOException, SAXException,
        ParserConfigurationException, TransformerException {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage();
        SOAPPart part = request.getSOAPPart(); 
        DOMSource domSource = new DOMSource(getDocumentBuilder().parse(
            new InputSource(new StringReader(REQUEST_MSG))));
        part.setContent(domSource);
        Dispatch<SOAPMessage> dispatch = testService.createDispatch(portQName, SOAPMessage.class,
            Service.Mode.MESSAGE);
        SOAPMessage response = null;
        try {
            response = dispatch.invoke(request);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (response != null) {
            Source src = response.getSOAPPart().getContent();
            DOMResult result = new DOMResult();
            getTransformer().transform(src, result);
            Document resultDoc = (Document)result.getNode();
            Document controlDoc = xmlParser.parse(new StringReader(TEST_RESPONSE));
            assertTrue("control document not same as instance document",
                comparer.isNodeEqual(controlDoc, resultDoc));
        }
    }

    static final String TEST_RESPONSE = 
        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
          "<SOAP-ENV:Header/>" +
          "<SOAP-ENV:Body>" +          
             "<srvc:" + ADVJDBC_BUILD_TBL1_TEST + "Response xmlns=\"" + ADVJDBC_BUILD_TBL1_NAMESPACE +"\" xmlns:srvc=\"" + ADVJDBC_BUILD_TBL1_SERVICE_NAMESPACE + "\">" +
                 "<srvc:result>" +
                     "<somepackage_tbl1Type xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                        "<item>entry 1</item>" +
                        "<item>entry 2</item>" +
                        "<item>entry 3</item>" +
                     "</somepackage_tbl1Type>" +
                 "</srvc:result>" +
             "</srvc:" + ADVJDBC_BUILD_TBL1_TEST + "Response>" +
          "</SOAP-ENV:Body>" +
        "</SOAP-ENV:Envelope>";
}