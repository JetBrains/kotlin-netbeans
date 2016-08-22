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

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaAnnotation implements JavaAnnotation, JavaElement {

    private final Project project;
    private final ElementHandle fromElement;
    private final String mirrorName;
    private final TypeMirrorHandle typeHandle;
    
    public NetBeansJavaAnnotation(ElementHandle fromElement, Project project, String name, TypeMirrorHandle typeHandle) {
        this.project = project;
        this.fromElement = fromElement;
        this.mirrorName = name;
        this.typeHandle = typeHandle;
    }
    
    public Project getProject() {
        return project;
    }
    
    public ElementHandle getFromElement() {
        return fromElement;
    }
    
    public String getName() {
        return mirrorName;
    }
    
    public JavaAnnotationArgument findArgument(@NotNull Name name) {
        return NBElementUtils.findArgument(fromElement, mirrorName, name, project);
    }
    
    @Override
    public Collection<JavaAnnotationArgument> getArguments() {
        return NBElementUtils.getArguments(fromElement, mirrorName, project);
    }

    @Override
    public ClassId getClassId() {
        return NBElementUtils.getClassId(typeHandle, project);
    }

    @Override
    public JavaClass resolve() {
        return NBElementUtils.getJavaClassForAnnotation(typeHandle, project);
    }
    
    
    @Override
    public int hashCode() {
        return project.hashCode() + fromElement.hashCode() + mirrorName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetBeansJavaAnnotation && project.equals(((NetBeansJavaAnnotation) obj).getProject())
                && fromElement.equals(((NetBeansJavaAnnotation) obj).getFromElement())
                && mirrorName.equals(((NetBeansJavaAnnotation) obj).getName());
    }
}
