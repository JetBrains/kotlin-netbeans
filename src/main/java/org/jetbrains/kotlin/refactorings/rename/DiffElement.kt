/*******************************************************************************
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
 *******************************************************************************/
package org.jetbrains.kotlin.refactorings.rename

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference
import org.openide.text.PositionBounds
import org.openide.filesystems.FileObject
import org.netbeans.modules.csl.spi.support.ModificationResult
import java.lang.ref.WeakReference
import org.openide.util.Lookup
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory
import org.openide.util.lookup.Lookups

/*

  @author Alexander.Baratynski
  Created on Aug 30, 2016
*/

class DiffElement(val diff : Difference, val bounds : PositionBounds, 
                  val parentFile : FileObject, val modification : ModificationResult) : SimpleRefactoringElementImplementation() {

    companion object {
        
        fun create(diff : Difference, fo : FileObject, modification : ModificationResult ) : DiffElement {
            val start = diff.startPosition
            val end = diff.endPosition
            val bounds = PositionBounds(start, end)
            return DiffElement(diff, bounds, fo, modification)
        }
        
    }
    
    val displayText : String
    var newFileContent : WeakReference<String>? = null
    
    init {
        displayText = diff.getDescription()
    }
    
    override fun getLookup() : Lookup {
        var composite : Any? = ElementGripFactory.getDefault().get(parentFile, bounds.begin.offset)
        if (composite == null) {
            composite = parentFile
        }
        
        return Lookups.fixed(composite, diff)
    }
    
    override fun setEnabled(enabled : Boolean) {
        diff.exclude(!enabled)
        newFileContent = null
        super.setEnabled(enabled)
    }
    
    override fun performChange() {}
    
    override fun getNewFileContent() : String? {
        if (newFileContent != null) {
            return newFileContent?.get()
        }
        
        val result = modification.getResultingSource(parentFile)
        newFileContent = WeakReference<String>(result)
        return result
    }
    
    override fun getPosition() : PositionBounds = bounds
    override fun getText() : String = displayText
    override fun getParentFile() : FileObject = parentFile
    override fun getDisplayText() : String = displayText
    
}