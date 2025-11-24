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
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GradleSingleModLSTest extends SingleModLibertyLSTestCommon {

    /**
     * Application Name
     */
    public static String PROJECT_NAME = "singleModGradleMP";

    /**
     * The path to the folder containing the test projects.
     */
    public static String PROJECTS_PATH = Paths.get("src", "test", "resources", "projects", "gradle").toAbsolutePath().toString();

    /**
     * Flag to track if AppleScript has been executed
     */
    private static boolean appleScriptExecuted = false;

    /**
     * Application resoruce URL.
     */
    public GradleSingleModLSTest() {
        super(PROJECT_NAME, PROJECTS_PATH);
    }

    /**
     * Prepares the environment for test execution.
     */
    @Test
    @Video
    @Order(1)
    public void setup() {
        prepareEnv(PROJECTS_PATH, PROJECT_NAME);
    }

    /**
     * Tests Liberty Lemminx Extension Hover support in server.xml for a
     * Liberty Server Feature - This is the first test with @Video annotation
     * where screen recording permission popup appears
     */
    @Test
    @Video
    @Order(2)
    public void testServerXMLFeatureHover() {
        // Handle permission popup on first video test (only on macOS and only once)
        if (remoteRobot.isMac() && !appleScriptExecuted) {
            TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "First @Video test - handling permission popup...");
            
            // Click on server.xml to ensure the window is in focus
            UIBotTestUtils.clickOnFileTab(remoteRobot, "server.xml");
            
            // Wait for the permission popup to appear (10-15 seconds)
            TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Waiting for permission popup to appear...");
            TestUtils.sleepAndIgnoreException(12);
            
            // Execute AppleScript to click the "Allow" button
            TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Attempting to click 'Allow' button via AppleScript...");
            try {
                // More comprehensive AppleScript that tries multiple approaches
                String appleScript = 
                    "tell application \"System Events\"\n" +
                    "    set dialogFound to false\n" +
                    "    \n" +
                    "    -- Try to find and click Allow button in various processes\n" +
                    "    repeat with proc in (every process whose visible is true)\n" +
                    "        try\n" +
                    "            tell proc\n" +
                    "                if exists (button \"Allow\" of window 1) then\n" +
                    "                    click button \"Allow\" of window 1\n" +
                    "                    set dialogFound to true\n" +
                    "                    exit repeat\n" +
                    "                end if\n" +
                    "            end tell\n" +
                    "        end try\n" +
                    "    end repeat\n" +
                    "    \n" +
                    "    -- If not found, try specific processes\n" +
                    "    if not dialogFound then\n" +
                    "        try\n" +
                    "            tell process \"UserNotificationCenter\"\n" +
                    "                if exists button \"Allow\" of window 1 then\n" +
                    "                    click button \"Allow\" of window 1\n" +
                    "                    set dialogFound to true\n" +
                    "                end if\n" +
                    "            end tell\n" +
                    "        end try\n" +
                    "    end if\n" +
                    "    \n" +
                    "    return dialogFound\n" +
                    "end tell";
                
                ProcessBuilder processBuilder = new ProcessBuilder("osascript", "-e", appleScript);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                
                // Read output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                int exitCode = process.waitFor();
                
                TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "AppleScript output: " + output.toString().trim());
                TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "AppleScript exit code: " + exitCode);
                
                if (output.toString().contains("true")) {
                    TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Successfully found and clicked 'Allow' button.");
                } else {
                    TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Allow button not found or already clicked.");
                }
                
                // Wait a moment for the click to take effect
                TestUtils.sleepAndIgnoreException(2);
                appleScriptExecuted = true;
            } catch (Exception e) {
                TestUtils.printTrace(TestUtils.TraceSevLevel.ERROR, "Failed to execute AppleScript: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Now run the actual test
        String testHoverTarget = "mpHealth-4.0";
        String hoverExpectedOutcome = "This feature provides support for the MicroProfile Health specification.";

        //mover cursor to hover point
        UIBotTestUtils.hoverInAppServerCfgFile(remoteRobot, testHoverTarget, "server.xml", UIBotTestUtils.PopupType.DOCUMENTATION);
        String hoverFoundOutcome = UIBotTestUtils.getHoverStringData(remoteRobot, UIBotTestUtils.PopupType.DOCUMENTATION);

        // Validate that the hover action raised the expected hint text
        TestUtils.validateHoverData(hoverExpectedOutcome, hoverFoundOutcome);
    }
}

// Made with Bob
