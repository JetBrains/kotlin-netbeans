/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRenameRefactoring extends ProgressProviderAdapter implements RefactoringPlugin {

    private final RenameRefactoring refactoring;
    
    public KotlinRenameRefactoring(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        List<PositionBounds> bounds = Lists.newArrayList();
        String newName = refactoring.getNewName();
        FileObject fo = refactoring.getRefactoringSource().lookup(FileObject.class);
        PsiElement psi = refactoring.getRefactoringSource().lookup(PsiElement.class);
        
        Map<FileObject, List<OffsetRange>> renameMap = RenamePerformer.getRenameRefactoringMap(fo, psi, newName);
        for (Entry<FileObject, List<OffsetRange>> entry : renameMap.entrySet()) {
            bounds.addAll(createPositionBoundsForFO(entry.getKey(), entry.getValue()));
        }
        
        for (PositionBounds bound : bounds) {
            try {
                bound.setText(newName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return null;
    }
    
    private List<PositionBounds> createPositionBoundsForFO(FileObject fo, List<OffsetRange> ranges) {
        List<PositionBounds> bounds = Lists.newArrayList();
        CloneableEditorSupport ces = GsfUtilities.findCloneableEditorSupport(fo);
        
        if (ces == null) {
            return bounds;
        }
        
        for (OffsetRange range : ranges) {
            PositionRef startRef = ces.createPositionRef(range.getStart(), Position.Bias.Forward);
            PositionRef endRef = ces.createPositionRef(range.getEnd(), Position.Bias.Forward);
            
            bounds.add(new PositionBounds(startRef, endRef));
        }
        
        return bounds;
    }
    
}
