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
        
        // Handle permission popup for screen recording (only on macOS)
        if (remoteRobot.isMac()) {
            TestUtils.printTrace(TestUtils.TraceSevLevel.INFO, "Setup - handling permission popup...");
            
            // Click on server.xml to ensure the window is in focus
            UIBotTestUtils.clickOnFileTab(remoteRobot, "server.xml");
            
            // Wait for the permission popup to appear
            TestUtils.sleepAndIgnoreException(12);
            
            // Execute AppleScript to click the "Allow" button
            try {
                String appleScript =
                    "tell application \"System Events\"\n" +
                    "    repeat with proc in (every process whose visible is true)\n" +
                    "        try\n" +
                    "            if exists (button \"Allow\" of window 1 of proc) then\n" +
                    "                click button \"Allow\" of window 1 of proc\n" +
                    "                return true\n" +
                    "            end if\n" +
                    "        end try\n" +
                    "    end repeat\n" +
                    "    return false\n" +
                    "end tell";
                
                Process process = new ProcessBuilder("osascript", "-e", appleScript).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                process.waitFor();
                
                TestUtils.printTrace(TestUtils.TraceSevLevel.INFO,
                    "true".equals(output) ? "Allow button clicked." : "Allow button not found.");
                
                TestUtils.sleepAndIgnoreException(2);
            } catch (Exception e) {
                TestUtils.printTrace(TestUtils.TraceSevLevel.ERROR, "Failed to execute AppleScript: " + e.getMessage());
            }
        }
    }
}