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

import java.nio.file.Paths;

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
        
        // Click on server.xml to bring the permission popup into focus
        UIBotTestUtils.clickOnFileTab(remoteRobot, "server.xml");
        
        // Wait for 10-15 seconds to allow the popup to appear
        TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Waiting for permission popup to appear...");
        TestUtils.sleepAndIgnoreException(12);
        
        // Execute AppleScript to click the "Allow" button on macOS
        if (remoteRobot.isMac()) {
            TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Attempting to click 'Allow' button via AppleScript...");
            try {
                String appleScript = "tell application \"System Events\"\n" +
                        "    tell process \"SecurityAgent\"\n" +
                        "        try\n" +
                        "            click button \"Allow\" of window 1\n" +
                        "        on error\n" +
                        "            -- If SecurityAgent is not found, try with System Settings\n" +
                        "            tell application \"System Events\"\n" +
                        "                tell process \"System Settings\"\n" +
                        "                    try\n" +
                        "                        click button \"Allow\" of window 1\n" +
                        "                    end try\n" +
                        "                end tell\n" +
                        "            end tell\n" +
                        "        end try\n" +
                        "    end tell\n" +
                        "end tell";
                
                ProcessBuilder processBuilder = new ProcessBuilder("osascript", "-e", appleScript);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Successfully executed AppleScript to click 'Allow' button.");
                } else {
                    TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "AppleScript execution completed with exit code: " + exitCode);
                }
                
                // Wait a moment for the click to take effect
                TestUtils.sleepAndIgnoreException(2);
            } catch (Exception e) {
                TestUtils.printTrace(TestUtils.TraceSevLevel.ERROR, "Failed to execute AppleScript: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Not running on macOS, skipping AppleScript execution.");
        }
    }
}
