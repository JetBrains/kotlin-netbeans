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
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMember;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public abstract class NetBeansJavaMember extends NetBeansJavaElement implements JavaMember {

    private final JavaClass containingClass;
    private final ElementHandle handle;
    
    public NetBeansJavaMember(FqName fqName, Project project, JavaClass containingClass) {
        super(fqName, project);
        this.containingClass = containingClass;
        this.handle = NBElementUtils.getElementhandleForMember(fqName, project, containingClass);
    }
    
    @Override
    public JavaClass getContainingClass() {
        return containingClass;
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return NBElementUtils.isMemberDeprecated(getProject(), handle);
    }

    @Override
    public boolean isAbstract() {
        return NBElementUtils.isMemberAbstract(getProject(), handle);
    }

    @Override
    public boolean isStatic() {
        return NBElementUtils.isMemberStatic(getProject(), handle);
    }

    @Override
    public boolean isFinal() {
        return NBElementUtils.isMemberFinal(getProject(), handle);
    }

    @Override
    public Visibility getVisibility() {
        return NBElementUtils.getMemberVisibility(getProject(), handle);
    }

    @Override
    public Name getName() {
        return Name.identifier(getFqName().shortName().asString());
    }
    
}
