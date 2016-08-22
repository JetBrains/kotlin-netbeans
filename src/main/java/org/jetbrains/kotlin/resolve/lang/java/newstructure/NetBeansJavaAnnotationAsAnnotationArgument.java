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
package org.jetbrains.kotlin.resolve.lang.java.newstructure;

import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationAsAnnotationArgument;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaAnnotationAsAnnotationArgument  implements JavaAnnotationAsAnnotationArgument {

    private final ElementHandle from;
    private final Project project;
    private final String mirrorName;
    private final Name name;
    
    public NetBeansJavaAnnotationAsAnnotationArgument(ElementHandle from, Project project, 
            String mirrorName, Name name) {
        this.from = from;
        this.project = project;
        this.mirrorName = mirrorName;
        this.name = name;
    }
    
    @Override
    public JavaAnnotation getAnnotation() {
        return new NetBeansJavaAnnotation(from, project, mirrorName);
    }

    @Override
    public Name getName() {
        return name;
    }
    
}
