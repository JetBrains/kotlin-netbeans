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

import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRefactoringElement extends SimpleRefactoringElementImplementation {

    private final FileObject fo;
    private final PositionBounds bounds;
    private final String text;
    private final Difference diff;
    
    public KotlinRefactoringElement(FileObject fo, PositionBounds bounds, String text, Difference diff) {
        this.bounds = bounds;
        this.fo = fo;
        this.text = text;
        this.diff = diff;
    }
    
    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getDisplayText() {
        return text;
    }

    @Override
    public void performChange() {
        
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(fo, diff);
    }

    @Override
    public void setEnabled(boolean enabled) {
        diff.exclude(!enabled);
        super.setEnabled(enabled);
    }
    
    @Override
    public FileObject getParentFile() {
        return fo;
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String getNewFileContent() {
        return "HDHDH";
    }
    
}
