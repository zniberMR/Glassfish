/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.common;

import org.glassfish.api.ActionReport;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class PlainTextActionReporterTest {

    public PlainTextActionReporterTest() {
    }

    @Before
    public void beforeTest() throws Exception {
        System.out.println(
            "\n-------------------------------------------------------------------------------");
    }
    @AfterClass
    public static void afterTest() throws Exception {
        System.out.println(
            "-------------------------------------------------------------------------------");
    }

    @Test
    public void failureTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("FailureTest Message Here!!");
        report.setFailureCause(new IndexOutOfBoundsException("Hi I am a phony Exception!!"));
        report.writeReport(System.out);
    }
    @Test
    public void babyTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("BabyTest Message Here!!");
        report.writeReport(System.out);
    }

    @Test
    public void mamaTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("Mama Test Top Message");
        top.setChildrenType("Module");

        for(int i = 0; i < 8; i++) {
            ActionReport.MessagePart childPart = top.addChild();
            childPart.setMessage("child" + i + " Message here");
            childPart.addProperty("ChildKey" + i, "ChildValue" + i);
            childPart.addProperty("AnotherChildKey" + i, "AnotherChildValue" + i);

            ActionReport.MessagePart grandkids = childPart.addChild();
            grandkids.setMessage("Grand Kids #" + i + " Top Message");
        }
        report.writeReport(System.out);
    }

    @Test
    public void papaTest() throws Exception {
        ActionReport report = new PlainTextActionReporter();
        report.setActionDescription("My Action Description");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("Papa Test Top Message");
        top.setChildrenType("Module");

        for(int i = 0; i < 8; i++) {
            ActionReport.MessagePart childPart = top.addChild();
            childPart.setMessage("child" + i + " Message here");
            childPart.addProperty("ChildKey" + i, "ChildValue" + i);
            childPart.addProperty("AnotherChildKey" + i, "AnotherChildValue" + i);

            for(int j = 0; j < 3; j++) {
                ActionReport.MessagePart grandkids = childPart.addChild();
                grandkids.setMessage("Grand Kid#" + j + " from child#" + i + " Top Message");
                grandkids.addProperty("Grand Kid#" + j + " from child#" + i + "key", "value");
            }
        }
        report.writeReport(System.out);
    }

    @Test
    public void aggregateTest() {
        ActionReporter successfulRoot = new PlainTextActionReporter();
        assert successfulRoot.hasSuccesses();
        assert !successfulRoot.hasFailures();
        assert !successfulRoot.hasWarnings();
        ActionReport failedChild = successfulRoot.addSubActionsReport();
        failedChild.setActionExitCode(ActionReport.ExitCode.FAILURE);
        assert successfulRoot.hasSuccesses();
        assert successfulRoot.hasFailures();
        assert !successfulRoot.hasWarnings();
        assert !failedChild.hasSuccesses();
        assert !failedChild.hasWarnings();
        assert failedChild.hasFailures();
        ActionReport warningChild = failedChild.addSubActionsReport();
        warningChild.setActionExitCode(ActionReport.ExitCode.WARNING);
        assert successfulRoot.hasSuccesses();
        assert successfulRoot.hasFailures();
        assert successfulRoot.hasWarnings();
        assert !failedChild.hasSuccesses();
        assert failedChild.hasWarnings();
        assert failedChild.hasFailures();
        assert warningChild.hasWarnings();
        assert !warningChild.hasSuccesses();
        ActionReport successfulChild = warningChild.addSubActionsReport();
        assert failedChild.hasSuccesses();
        assert warningChild.hasSuccesses();
        assert !warningChild.hasFailures();
        StringBuilder sb = new StringBuilder();
        successfulRoot.setMessage("sr");
        successfulRoot.getCombinedMessages(successfulRoot, sb);
        assertEquals("sr%%%EOL%%%", sb.toString());
        warningChild.setMessage("wc");
        sb = new StringBuilder();
        successfulRoot.getCombinedMessages(successfulRoot, sb);
        assertEquals("sr%%%EOL%%%wc%%%EOL%%%", sb.toString());
        failedChild.setMessage("fc");
        sb = new StringBuilder();
        successfulRoot.getCombinedMessages(successfulRoot, sb);
        assertEquals("sr%%%EOL%%%fc%%%EOL%%%wc%%%EOL%%%", sb.toString());
    }
}