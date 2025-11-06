/*******************************************************************************
 * Copyright (c) 2023, 2024 IBM Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.tools.intellij.it;

import com.automation.remarks.junit5.Video;
import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.JTreeFixture;
import io.openliberty.tools.intellij.it.fixtures.ProjectFrameFixture;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitForIgnoringError;

public abstract class SingleModMPLSTestCommon {
    public static final String REMOTEBOT_URL = "http://localhost:8082";
    public static final RemoteRobot remoteRobot = new RemoteRobot(REMOTEBOT_URL);
    

    String projectName;
    String projectsPath;

    public SingleModMPLSTestCommon(String projectName, String projectsPath) {
        this.projectName = projectName;
        this.projectsPath = projectsPath;
    }

    /**
     * Processes actions before each test.
     *
     * @param info Test information.
     */
    @BeforeEach
    public void beforeEach(TestInfo info) {
        TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, this.getClass().getSimpleName() + "." + info.getDisplayName() + ". Entry");
    }

    /**
     * Processes actions after each test.
     *
     * @param info Test information.
     */
    @AfterEach
    public void afterEach(TestInfo info) {
        TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, this.getClass().getSimpleName() + "." + info.getDisplayName() + ". Exit");
        TestUtils.detectFatalError();
    }

    /**
     * Cleanup.
     */
    @AfterAll
    public static void cleanup() {
        ProjectFrameFixture projectFrame = remoteRobot.find(ProjectFrameFixture.class, Duration.ofMinutes(2));
        projectFrame.findText("health").doubleClick();
        projectFrame.findText("META-INF").doubleClick();
        projectFrame.findText("resources").doubleClick();

        UIBotTestUtils.closeFileEditorTab(remoteRobot, "ServiceLiveHealthCheck.java", "5");
        UIBotTestUtils.closeFileEditorTab(remoteRobot, "microprofile-config.properties, properties file", "5");
        if (!remoteRobot.isMac()) {
            UIBotTestUtils.runActionFromSearchEverywherePanel(remoteRobot, "Compact Mode", 3);
        }
        UIBotTestUtils.closeProjectView(remoteRobot);
        UIBotTestUtils.closeProjectFrame(remoteRobot);
        UIBotTestUtils.validateProjectFrameClosed(remoteRobot);
    }

    /**
     * Tests MicroProfile Language Server code snippet support in a Java source file
     */
    @Test
    @Video
    @Order(2)
    public void testInsertCodeSnippetIntoJavaPart() {
        String snippetStr = "mp";
        String snippetChooser = "liveness";
        String insertedCode = "public class ServiceLiveHealthCheck implements HealthCheck {";

        // get focus on file tab prior to copy
        UIBotTestUtils.clickOnFileTab(remoteRobot, "ServiceLiveHealthCheck.java");

        // Save the current content.
        UIBotTestUtils.copyWindowContent(remoteRobot);

        // Delete the current src code
        UIBotTestUtils.clearWindowContent(remoteRobot);

        // Insert a code snippet into java part
        try {
            UIBotTestUtils.insertCodeSnippetIntoSourceFile(remoteRobot, "ServiceLiveHealthCheck.java", snippetStr, snippetChooser);
            Path pathToSrc = Paths.get(projectsPath, projectName, "src", "main", "java", "io", "openliberty", "mp", "sample", "health","ServiceLiveHealthCheck.java");
            TestUtils.validateCodeInJavaSrc(pathToSrc.toString(), insertedCode);
        } finally {
            // Replace modified content with the original content
            UIBotTestUtils.pasteOnActiveWindow(remoteRobot);
        }
    }


    /**
     * Prepares the environment to run the tests.
     *
     * @param projectPath The path of the project.
     * @param projectName The name of the project being used.
     */

    public static void prepareEnv(String projectPath, String projectName) {
        waitForIgnoringError(Duration.ofMinutes(4), Duration.ofSeconds(5), "Wait for IDE to start", "IDE did not start", () -> remoteRobot.callJs("true"));
        UIBotTestUtils.findWelcomeFrame(remoteRobot);

        UIBotTestUtils.importProject(remoteRobot, projectPath, projectName);
        UIBotTestUtils.openProjectView(remoteRobot);
        if (!remoteRobot.isMac()) {
            UIBotTestUtils.runActionFromSearchEverywherePanel(remoteRobot, "Compact Mode", 3);
        }
        // IntelliJ does not start building and indexing until the Project View is open
        UIBotTestUtils.waitForIndexing(remoteRobot);
        UIBotTestUtils.openAndValidateLibertyToolWindow(remoteRobot, projectName);
        UIBotTestUtils.closeLibertyToolWindow(remoteRobot);

        // pre-open project tree before attempting to open files needed by testcases
        ProjectFrameFixture projectFrame = remoteRobot.find(ProjectFrameFixture.class, Duration.ofMinutes(2));
        JTreeFixture projTree = projectFrame.getProjectViewJTree(remoteRobot, projectName);

        UIBotTestUtils.openFile(remoteRobot, projectName, "ServiceLiveHealthCheck", projectName, "src", "main", "java", "io.openliberty.mp.sample", "health");
        UIBotTestUtils.openFile(remoteRobot, projectName, "microprofile-config.properties", projectName, "src", "main", "resources", "META-INF");

        // Removes the build tool window if it is opened. This prevents text to be hidden by it.
        UIBotTestUtils.removeToolWindow(remoteRobot, "Build:");
    }
}

