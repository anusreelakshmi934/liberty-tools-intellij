/*******************************************************************************
 * Copyright (c) 2023, 2025 IBM Corporation.
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
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.utils.Keyboard;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import io.openliberty.tools.intellij.it.fixtures.ProjectFrameFixture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitForIgnoringError;
import static io.openliberty.tools.intellij.util.Constants.LIBERTY_GRADLE_START_CONTAINER_CMD;
import static io.openliberty.tools.intellij.util.Constants.LIBERTY_MAVEN_START_CONTAINER_CMD;

/**
 * Holds common tests that use a single module MicroProfile project.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class SingleModMPProjectTestCommon {

    /**
     * URL to display the UI Component hierarchy. This is used to obtain xPath related
     * information to find UI components.
     */
    public static final String REMOTE_BOT_URL = "http://localhost:8082";

    /**
     * To clean the terminal.
     */
    private boolean shouldCleanupTerminal = true;

    /**
     * Supported build types.
     */
    public enum BuildType {
        MAVEN_TYPE, GRADLE_TYPE
    }

    /**
     * The remote robot object.
     */
    public static final RemoteRobot remoteRobot = new RemoteRobot(REMOTE_BOT_URL);

    /**
     * Single module Microprofile project name.
     */
    private String smMpProjectName = null;

    /**
     * The path to the folder containing the test projects.
     */
    private String projectsPath = null;

    /**
     * Project port.
     */
    private int smMpProjectPort = 0;

    /**
     * Project resource URI.
     */
    private String smMpProjectResUri = null;

    /**
     * Project response.
     */
    private String smMpProjectOutput = null;

    /**
     * Relative location of the WLP installation.
     */
    private String wlpInstallPath = null;

    /**
     * The path to the test report.
     */
    private Path testReportPath = null;

    /**
     * Build file name.
     */
    private String buildFileName = null;

    /**
     * Action command to open the build file.
     */
    private String buildFileOpenCmd = null;

    /**
     * Dev mode configuration start parameters.
     */
    private String devModeStartParams = null;

    /**
     * Dev mode configuration custom start parameters for debugging.
     */
    private String devModeStartParamsDebug = null;

    /**
     * Build Category.
     */
    private BuildType buildCategory = null;

    /**
     * Check for the single project or multiple projects
     */
    private boolean isMultiple = false;

    /**
     * Absolute WLP Path
     */
    private String absoluteWLPPath = null;

    /**
     * Returns the path where the Liberty server was installed.
     *
     * @return The path where the Liberty server was installed.
     */
    public String getWLPInstallPath() {
        return wlpInstallPath;
    }
    public void setWLPInstallPath(String path) {
        wlpInstallPath = path;
    }

    /**
     * Sets the path where the Liberty server stores test reports.
     *
     */
    public void setTestReportPath(Path path) {
        testReportPath = path;
    }

    /**
     * Returns the projects directory path.
     *
     * @return The projects directory path.
     */
    public String getProjectsDirPath() {
        return projectsPath;
    }
    public void setProjectsDirPath(String path) {
        projectsPath = path;
    }

    /**
     * Returns the name of the single module MicroProfile project.
     *
     * @return The name of the single module MicroProfile project.
     */
    public String getSmMPProjectName() {
        return smMpProjectName;
    }
    public void setSmMPProjectName(String name) {
        smMpProjectName = name;
    }

    /**
     * Returns the expected HTTP response payload associated with the single module
     * MicroProfile project.
     *
     * @return The expected HTTP response payload associated with the single module
     * MicroProfile project.
     */
    public String getSmMPProjOutput() {
        return smMpProjectOutput;
    }
    public void setSmMPProjOutput(String s) {
        smMpProjectOutput = s;
    }

    /**
     * Returns the port number associated with the single module MicroProfile project.
     *
     * @return The port number associated with the single module MicroProfile project.
     */
    public int getSmMpProjPort() {
        return smMpProjectPort;
    }
    public void setSmMpProjPort(int port) {
        smMpProjectPort = port;
    }

    /**
     * Return the Resource URI associated with the single module MicroProfile project.
     *
     * @return The Resource URI associated with the single module MicroProfile project.
     */
    public String getSmMpProjResURI() {
        return smMpProjectResUri;
    }
    public void setSmMpProjResURI(String uri) {
        smMpProjectResUri = uri;
    }

    /**
     * Returns the name of the build file used by the project.
     *
     * @return The name of the build file used by the project.
     */
    public String getBuildFileName() {
        return buildFileName;
    }
    public void setBuildFileName(String name) {
        buildFileName = name;
    }

    /**
     * Returns the name of the custom action command used to open the build file.
     *
     * @return The name of the custom action command used to open the build file.
     */
    public String getBuildFileOpenCommand() {
        return buildFileOpenCmd;
    }
    public void setBuildFileOpenCommand(String command) {
        buildFileOpenCmd = command;
    }

    /**
     * Returns the custom start parameters to be used to start dev mode.
     *
     * @return The custom start parameters to be used to start dev mode.
     */
    public String getStartParams() {
        return devModeStartParams;
    }
    public void setStartParams(String params) {
        devModeStartParams = params;
    }

    /**
     * Returns the custom start parameters for debugging to start dev mode.
     *
     * @return The custom start parameters for debugging to start dev mode.
     */
    public String getStartParamsDebugPort() {
        return devModeStartParamsDebug;
    }
    public void setStartParamsDebugPort(String params) {
        devModeStartParamsDebug = params;
    }

    /**
     * Returns Build Category
     */
    public BuildType getBuildCategory() {
        return buildCategory;
    };
    public void setBuildCategory(BuildType type) {
        buildCategory = type;
    };

    /**
     * Returns a check for the single project or multiple projects.
     *
     * @return a check for the single project or multiple projects.
     */
    public boolean getProjectTypeIsMutliple() {
        return isMultiple;
    }
    public void setProjectTypeIsMultiple(boolean value) {
        isMultiple = value;
    }

    /**
     * Returns the absolute WLP path
     *
     * @return a String representing the absolute path to the WLP installation directory.
     */
    public String getAbsoluteWLPPath() {
        return absoluteWLPPath;
    }

    /**
     * Sets the absolute WLP path
     *
     * @param absoluteWLPPath a String representing the path to be used as the WLP installation location.
     */
    public void setAbsoluteWLPPath(String absoluteWLPPath) {
        this.absoluteWLPPath = absoluteWLPPath;
    }

    /**
     * Prepares the environment for test execution. Subclasses must implement this method
     * with the setup logic previously held in @BeforeAll, so that it runs as a recorded test.
     */
    @Test
    @Video
    @Order(0)
    public abstract void testSetup();

    /**
     * Processes actions before each test.
     *
     * @param info Test information.
     */
    @Test
    @Video
    @Order(1)
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
        if (shouldCleanupTerminal) {
            cleanAndResetTerminal();
        }
        TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, this.getClass().getSimpleName() + "." + info.getDisplayName() + ". Exit");
        TestUtils.detectFatalError();
    }

    /**
     * Cleanup.
     */
    @AfterAll
    public static void cleanup() {
        closeProjectView();
    }

    /**
     * Close project.
     */
    protected static void closeProjectView() {
        if (!remoteRobot.isMac()) {
            UIBotTestUtils.runActionFromSearchEverywherePanel(remoteRobot, "Close All Tabs", 3);
            UIBotTestUtils.runActionFromSearchEverywherePanel(remoteRobot, "Compact Mode", 3);
        }
        UIBotTestUtils.closeLibertyToolWindow(remoteRobot);
        UIBotTestUtils.closeProjectView(remoteRobot);
        UIBotTestUtils.closeProjectFrame(remoteRobot);
        UIBotTestUtils.validateProjectFrameClosed(remoteRobot);
    }

    /**
     * Tests the liberty: View <project build file> action run from the project's pop-up action menu.
     */
    @Test
    @Video
    @Order(3)
    public void testOpenBuildFileActionUsingPopUpMenu() {
        shouldCleanupTerminal = false;
        String editorTabName = getBuildFileName() + " (" + getSmMPProjectName() + ")";

        // Close the editor tab if it was previously opened.
        UIBotTestUtils.closeFileEditorTab(remoteRobot, editorTabName, "5");

        // Open the build file.
        UIBotTestUtils.openLibertyToolWindow(remoteRobot);
        UIBotTestUtils.runActionLTWPopupMenu(remoteRobot, getSmMPProjectName(), getBuildFileOpenCommand(), 3);

        // Verify that build file tab is opened.
        Assertions.assertNotNull(UIBotTestUtils.getEditorTabCloseButton(remoteRobot, editorTabName, "10"),
                "Editor tab with the name of " + editorTabName + " could not be found.");

        // Close the editor tab.
        UIBotTestUtils.rightClickCloseOnFileTab(remoteRobot, editorTabName);
    }

    /**
     * Tests dashboard start.../stop actions run from the project's drop-down action menu.
     */
    @Test
    @Video
    @Order(4)
    public void testStartWithParamsActionUsingDropDownMenu() {
        String testName = "testStartWithParamsActionUsingDropDownMenu";
        String absoluteWLPPath = getAbsoluteWLPPath();

        // Remove all other configurations first.
        UIBotTestUtils.deleteLibertyRunConfigurations(remoteRobot);

        // Delete any existing test report files.
        deleteTestReports();

        // Trigger the start with parameters configuration dialog.
        UIBotTestUtils.runLibertyActionFromLTWDropDownMenu(remoteRobot, "Start...", getSmMPProjectName(), false, 3);

        // Run the configuration dialog.
        UIBotTestUtils.runStartParamsConfigDialog(remoteRobot, getStartParams());

        try {
            // Validate that the project started.
            TestUtils.validateProjectStarted(testName, getSmMpProjResURI(), getSmMpProjPort(), getSmMPProjOutput(), absoluteWLPPath, false);

            // Validate that the report was generated.
            validateTestReportsExist();
        } finally {
            if (TestUtils.isServerStopNeeded(absoluteWLPPath)) {
                // Sleep for a few seconds to allow dev mode to finish running the tests. Specially
                // for those times when the tests are run twice. Not waiting, opens up a window
                // that leads to false negative results, and the Liberty server being left active.
                // If the Liberty server is left active, subsequent tests will fail.
                TestUtils.sleepAndIgnoreException(60);

                // Stop Liberty dev mode and validates that the Liberty server is down.
                UIBotTestUtils.runStopAction(remoteRobot, testName, UIBotTestUtils.ActionExecType.LTWDROPDOWN, absoluteWLPPath, getSmMPProjectName(), 3, getProjectTypeIsMutliple());
            }
        }

        // Validate that the start with params action brings up the configuration previously used.
        try {
            UIBotTestUtils.runLibertyActionFromLTWDropDownMenu(remoteRobot, "Start...", getSmMPProjectName(), false, 3);
            Map<String, String> cfgEntries = UIBotTestUtils.getOpenedLibertyConfigDataAndCloseOnExit(remoteRobot);
            String activeCfgName = cfgEntries.get(UIBotTestUtils.ConfigEntries.NAME.toString());
            Assertions.assertEquals(getSmMPProjectName(), activeCfgName, "The active config name " + activeCfgName + " does not match expected name of " + getSmMPProjectName());
            String activeCfgParams = cfgEntries.get(UIBotTestUtils.ConfigEntries.PARAMS.toString());
            Assertions.assertEquals(getStartParams(), activeCfgParams, "The active config params " + activeCfgParams + " does not match expected params of " + getStartParams());
        } finally {
            // Cleanup configurations.
            UIBotTestUtils.deleteLibertyRunConfigurations(remoteRobot);
        }
    }


    /**
     * Prepares the environment to run the tests.
     * This method calls the overloaded {@code prepareEnv} method with {@code isMultiple} set to {@code false}.
     *
     * @param projectPath The path of the project.
     * @param projectName The name of the project being used.
     */
    public static void prepareEnv(String projectPath, String projectName) {
        prepareEnv(projectPath, projectName, false);
    }

    /**
     * Prepares the environment to run the tests.
     *
     * @param projectPath The path of the project.
     * @param projectName The name of the project being used.
     */
    public static void prepareEnv(String projectPath, String projectName, boolean isMultiple) {
        TestUtils.printTrace(TestUtils.TraceSevLevel.INFO,
                "prepareEnv. Entry. ProjectPath: " + projectPath + ". ProjectName: " + projectName);
        waitForIgnoringError(Duration.ofMinutes(4), Duration.ofSeconds(5), "Wait for IDE to start", "IDE did not start", () -> remoteRobot.callJs("true"));
        UIBotTestUtils.findWelcomeFrame(remoteRobot);
        if (isMultiple) {
            UIBotTestUtils.importProject(remoteRobot, projectPath, "multiple-project");
            UIBotTestUtils.clickOnLoad(remoteRobot);
        }
        else {
            UIBotTestUtils.importProject(remoteRobot, projectPath, projectName);
        }
        UIBotTestUtils.openProjectView(remoteRobot);
        // IntelliJ does not start building and indexing until the Project View is open
        UIBotTestUtils.waitForIndexing(remoteRobot);
        if (!remoteRobot.isMac()) {
            UIBotTestUtils.runActionFromSearchEverywherePanel(remoteRobot, "Compact Mode", 3);
        }
        UIBotTestUtils.openAndValidateLibertyToolWindow(remoteRobot, projectName);
        UIBotTestUtils.expandLibertyToolWindowProjectTree(remoteRobot, projectName);

        // Close all open editors.
        // The expansion of the project tree in the Liberty tool window causes the editor tab for
        // the project's build file to open. That is the result of clicking on the project to give it
        // focus. The action of clicking on the project causes the build file to be opened automatically.
        // Closing the build file editor here prevents it from opening automatically when the project
        // in the Liberty tool window is clicked or right-clicked again. This is done on purpose to
        // prevent false negative tests related to the build file editor tab.
        if (remoteRobot.isMac()) {
            UIBotTestUtils.closeAllEditorTabs(remoteRobot);
        }
        else {
            UIBotTestUtils.runActionFromSearchEverywherePanel(remoteRobot, "Close All Tabs", 3);
        }

        TestUtils.printTrace(TestUtils.TraceSevLevel.INFO,
                "prepareEnv. Exit. ProjectName: " + projectName);
    }

    /**
     * Clear all the text in the terminal and just show the command line prompt.
     */
    public void terminalClearBuffer() {
        ProjectFrameFixture projectFrame;
        ComponentFixture terminal;
        try {
            projectFrame = remoteRobot.find(ProjectFrameFixture.class, Duration.ofSeconds(10));
            terminal = remoteRobot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"), Duration.ofSeconds(10));
        } catch (WaitForConditionTimeoutException w) {
            return; // there is no terminal with a Liberty to stop
        }
        terminal.rightClick();
        ComponentFixture clearMenuItem = projectFrame.getActionMenuItem("Clear Buffer");
        clearMenuItem.click();
        TestUtils.sleepAndIgnoreException(1);
    }

    /**
     * Copy all the text in the terminal and return it to the caller.
     * @return empty string in error situations
     */
    public String terminalCopyBuffer() {
        ProjectFrameFixture projectFrame;
        ComponentFixture terminal;
        try {
            projectFrame = remoteRobot.find(ProjectFrameFixture.class, Duration.ofSeconds(10));
            terminal = remoteRobot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"), Duration.ofSeconds(10));
        } catch (WaitForConditionTimeoutException w) {
            return ""; // there is no terminal with a Liberty to stop
        }
        // Select all text in the terminal screen
        terminal.rightClick();
        ComponentFixture selectAllMenuItem = projectFrame.getActionMenuItem("Select All");
        selectAllMenuItem.click();
        // Copy all text to the clipboard
        terminal.rightClick();
        ComponentFixture copyMenuItem = projectFrame.getActionMenuItem("Copy");
        copyMenuItem.click();
        // Retrieve the copied value from the system clipboard.
        try {
            String copiedValue = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
            return copiedValue;
        } catch (UnsupportedFlavorException | IOException e) {
            return ""; // shouldn't happen
        }
    }

    /**
     * Cleans up the server and resets the terminal.
     */
    public void cleanAndResetTerminal() {
        stopTerminal();
        UIBotTestUtils.closeTerminalTabs(remoteRobot);
        UIBotTestUtils.openTerminalWindow(remoteRobot);
        cleanTerminal();
        UIBotTestUtils.closeTerminalTabs(remoteRobot);
    }

    /**
     * Stop the Server.
     */
    public void stopTerminal() {
        Keyboard keyboard = new Keyboard(remoteRobot);
        ProjectFrameFixture projectFrame;
        ComponentFixture terminal;
        try {
            projectFrame = remoteRobot.find(ProjectFrameFixture.class, Duration.ofSeconds(10));
            terminal = remoteRobot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"), Duration.ofSeconds(10));
        } catch (WaitForConditionTimeoutException w) {
            return; // there is no terminal with a Liberty to stop
        }
        terminal.rightClick();
        ComponentFixture openFixtureNewTab = projectFrame.getActionMenuItem("New Tab");
        openFixtureNewTab.click();

        // Perform Stop Action
        if (getBuildCategory() == BuildType.MAVEN_TYPE) {
            // For multiple project, a new tab will open with the project directory. The robot will add the cd command to change the directory to the project directory.
            // Run the multiple project tests only in the Maven project, so navigate to the Maven project directory.
            if (getProjectsDirPath().contains("multiple-project")) {
                keyboard.enterText("cd singleModMavenMP");
                keyboard.enter();
            }
            keyboard.enterText("./mvnw liberty:stop");
        } else if (getBuildCategory() == BuildType.GRADLE_TYPE) {
            keyboard.enterText("./gradlew libertyStop");
        } else {
            TestUtils.printTrace(TestUtils.TraceSevLevel.ERROR,  "Invalid build type specified");
            return;
        }
        keyboard.enter();
        TestUtils.sleepAndIgnoreException(10);
    }

    /**
     * Clean project.
     */
    public void cleanTerminal() {
        Keyboard keyboard = new Keyboard(remoteRobot);
        // Perform clean
        if (getBuildCategory() == BuildType.MAVEN_TYPE) {
            // For multiple project, a new tab will open with the project directory. The robot will add the cd command to change the directory to the project directory.
            // Run the multiple project tests only in the Maven project, so navigate to the Maven project directory.
            if (getProjectsDirPath().contains("multiple-project")) {
                keyboard.enterText("cd singleModMavenMP");
                keyboard.enter();
            }
            keyboard.enterText("./mvnw clean");
        } else if (getBuildCategory() == BuildType.GRADLE_TYPE) {
            keyboard.enterText("./gradlew clean");
        } else {
            TestUtils.printTrace(TestUtils.TraceSevLevel.ERROR,  "Invalid build type specified");
            return;
        }
        keyboard.enter();
        TestUtils.sleepAndIgnoreException(10);
    }

    /**
     * Deletes the directory specified by dirPath if it exists.
     *
     * @param dirPath The path to the directory that may be deleted.
     */
    public static void deleteDirectoryIfExists(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            TestUtils.deleteDirectory(dir);
        }
    }

    /**
     * Deletes test reports.
     */
    public void deleteTestReports() {
        boolean testReportDeleted = TestUtils.deleteFile(testReportPath);
        Assertions.assertTrue(testReportDeleted, () -> "Test report file: " + testReportPath + " was not be deleted.");
    }

    /**
     * Validates that test reports were generated.
     */
    public void validateTestReportsExist() {
        //TODO: rewrite validateTestReportExists() to accept one argument or to accept a null as the second argument
        TestUtils.validateTestReportExists(testReportPath, testReportPath);
    }
}