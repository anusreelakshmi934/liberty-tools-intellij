/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package io.openliberty.tools.intellij.lsp4mp4ij.psi.core.inspections;

import io.openliberty.tools.intellij.lsp4mp4ij.MicroProfileBundle;
import com.redhat.devtools.lsp4ij.inspections.AbstractDelegateInspectionWithExclusions;

import java.util.Arrays;

/**
 * Dummy inspection for unknown properties in Microprofile properties files
 */
public class MicroProfilePropertiesUnknownInspection extends AbstractDelegateInspectionWithExclusions {
    public static final String ID = getShortName(MicroProfilePropertiesUnknownInspection.class.getSimpleName());

    public MicroProfilePropertiesUnknownInspection() {
        super(MicroProfileBundle.message("microprofile.properties.validation.excluded.properties"));
        excludeList.addAll(Arrays.asList("*/mp-rest/providers/*/priority", "mp.openapi.schema.*", "kafka-streams.*", "camel.*"));
    }
}
