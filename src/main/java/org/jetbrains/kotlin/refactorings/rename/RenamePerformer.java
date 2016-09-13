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

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.highlighter.occurrences.OccurrencesUtils;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class RenamePerformer {
    
    public static Map<FileObject, List<OffsetRange>> getRenameRefactoringMap(FileObject fo, PsiElement psi, String newName) {
        Map<FileObject, List<OffsetRange>> ranges = 
            new HashMap<FileObject, List<OffsetRange>>();
        KtFile ktFile = (KtFile) psi.getContainingFile();
        KtElement ktElement = PsiTreeUtil.getNonStrictParentOfType(psi, KtElement.class);
        if (ktElement == null) {
            return ranges;
        }
        
        List<? extends SourceElement> sourceElements = ReferenceUtils.resolveToSourceDeclaration(ktElement);
        if (sourceElements.isEmpty()) {
            return ranges;
        }
        
        List<? extends SourceElement> searchingElements = OccurrencesUtils.getSearchingElements(sourceElements);
        List<OffsetRange> occurrencesRanges = OccurrencesUtils.search(searchingElements, ktFile);
        
        ranges.put(fo, occurrencesRanges);
        
        return ranges;
    }
    
    
}
