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
    }
}
