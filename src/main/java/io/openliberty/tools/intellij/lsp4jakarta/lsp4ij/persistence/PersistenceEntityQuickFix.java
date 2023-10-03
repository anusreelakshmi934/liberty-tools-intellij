/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.tools.intellij.lsp4jakarta.lsp4ij.persistence;


import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.openliberty.tools.intellij.lsp4jakarta.lsp4ij.Messages;
import io.openliberty.tools.intellij.lsp4jakarta.lsp4ij.codeAction.proposal.AddConstructorProposal;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.java.codeaction.ExtendedCodeAction;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.java.codeaction.IJavaCodeActionParticipant;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.java.codeaction.JavaCodeActionContext;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.java.codeaction.JavaCodeActionResolveContext;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4mp.commons.CodeActionResolveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * QuickFix for fixing {@link PersistenceConstants#DIAGNOSTIC_CODE_MISSING_ATTRIBUTES} error
 * by providing several code actions to remove incorrect modifiers or add missing constructor:
 *
 * {@link PersistenceConstants#DIAGNOSTIC_CODE_MISSING_EMPTY_CONSTRUCTOR}
 * <ul>
 * <li> Add a (no-arg) void constructor to this class if the class has other constructors
 * which do not conform to this
 * </ul>
 *
 * {@link PersistenceConstants#DIAGNOSTIC_CODE_FINAL_METHODS}
 * <ul>
 * <li> Remove the FINAL modifier from all methods in this class
 * </ul>
 *
 * {@link PersistenceConstants#DIAGNOSTIC_CODE_FINAL_VARIABLES}
 * <ul>
 * <li> Remove the FINAL modifier from all variables in this class
 * </ul>
 *
 * {@link PersistenceConstants#DIAGNOSTIC_CODE_FINAL_CLASS}
 * <ul>
 * <li> Remove the FINAL modifier from this class
 * </ul>
 *
 * @author Leslie Dawson (lamminade)
 *
 */
public class PersistenceEntityQuickFix implements IJavaCodeActionParticipant {
    @Override
    public String getParticipantId() {
        return PersistenceEntityQuickFix.class.getName();
    }

    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic) {
        List<CodeAction> codeActions = new ArrayList<>();
        PsiElement node = context.getCoveredNode();
        PsiClass parentType = getBinding(node);
        if (parentType != null) {

            // add constructor
            if (diagnostic.getCode().getLeft().equals(PersistenceConstants.DIAGNOSTIC_CODE_MISSING_EMPTY_CONSTRUCTOR)) {
                codeActions.addAll(addConstructor(diagnostic, context, parentType));
            }

        }
        return codeActions;
    }

    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        // Call the addConstructor method to generate code actions
        List<CodeAction> codeActions = addConstructor(null, context, null);

        // Check if code actions were generated
        if (!codeActions.isEmpty()) {
            // Return the first code action from the list (you can customize this logic as needed)
            return codeActions.get(0);
        }

        // If no code actions were generated, return null
        return null;
    }


    protected PsiClass getBinding(PsiElement node) {
        return PsiTreeUtil.getParentOfType(node, PsiClass.class);
    }

    private List<CodeAction> addConstructor(Diagnostic diagnostic, JavaCodeActionContext context, PsiClass notUsed) {
        List<CodeAction> codeActions = new ArrayList<>();
        JavaCodeActionContext targetContext = null;
        PsiElement node = null;
        PsiClass parentType = null;

        String[] constructorNames = {"AddNoArgProtectedConstructor", "AddNoArgPublicConstructor"};

        for (String name : constructorNames) {
            // option for protected and public constructors
            targetContext = context.copy();
            node = targetContext.getCoveredNode();
            parentType = getBinding(node);
            String accessModifier = name.equals("AddNoArgProtectedConstructor") ? "protected" : "public";
            ChangeCorrectionProposal proposal = new AddConstructorProposal(name,
                    targetContext.getSource().getCompilationUnit(), targetContext.getASTRoot(), parentType, 0, accessModifier);
            CodeAction codeAction = createCodeAction(targetContext, diagnostic);
            codeAction.setTitle(Messages.getMessage(name));
            codeAction.setEdit(targetContext.convertToWorkspaceEdit(proposal));
            codeActions.add(codeAction);
        }

        return codeActions;
    }
    private CodeAction createCodeAction(JavaCodeActionContext context, Diagnostic diagnostic) {
        ExtendedCodeAction codeAction = new ExtendedCodeAction("");
        codeAction.setRelevance(0);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(),
                context.getParams().getRange(), Collections.emptyMap(),
                context.getParams().isResourceOperationSupported(),
                context.getParams().isCommandConfigurationUpdateSupported()));
        return codeAction;
    }

}