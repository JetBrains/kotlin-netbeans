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
    
    public NetBeansJavaMember(FqName fqName, Project project, JavaClass containingClass) {
        super(fqName, project);
        this.containingClass = containingClass;
        super.setHandle(NBElementUtils.getElementhandleForMember(fqName, project, containingClass));
    }
    
    @Override
    public JavaClass getContainingClass() {
        return containingClass;
    }
    
    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        if (getHandle().getKind().isField()) {
            return NBElementUtils.getAnnotationsForField(getHandle(), getProject());
        } else {
            return NBElementUtils.getAnnotationsForExecutable(getHandle(), getProject());
        }
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        if (getHandle().getKind().isField()) {
            return NBElementUtils.findAnnotationForField(getHandle(), getProject(), fqName);
        } else {
            return NBElementUtils.findAnnotationForExecutable(getHandle(), getProject(), fqName);
        }
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return NBElementUtils.isMemberDeprecated(getProject(), getHandle());
    }

    @Override
    public boolean isAbstract() {
        return NBElementUtils.isMemberAbstract(getProject(), getHandle());
    }

    @Override
    public boolean isStatic() {
        return NBElementUtils.isMemberStatic(getProject(), getHandle());
    }

    @Override
    public boolean isFinal() {
        return NBElementUtils.isMemberFinal(getProject(), getHandle());
    }

    @Override
    public Visibility getVisibility() {
        return NBElementUtils.getMemberVisibility(getProject(), getHandle());
    }

    @Override
    public Name getName() {
        return Name.identifier(getFqName().shortName().asString());
    }
    
}
