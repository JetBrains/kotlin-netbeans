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
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaClassifier extends NetBeansJavaElement implements JavaClassifier {

    public NetBeansJavaClassifier(FqName fqName, Project project) {
        super(fqName, project);
    }
    
    public static JavaClassifier create(FqName fqName, Project project, TypeMirrorHandle handle){
        if (handle.getKind() == TypeKind.TYPEVAR){
            return new NetBeansJavaTypeParameter(fqName, project, handle);
        }
        ElementKind elementKind = NBElementUtils.getElementKind(fqName, project);
        if (elementKind.isClass() || elementKind.isInterface() 
                || elementKind == ElementKind.ENUM){
            return new NetBeansJavaClass(fqName, project);
        }
        else
            throw new IllegalArgumentException("Element" + fqName.shortName().toString() + "is not JavaClassifier");
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
        return false;
    }

    @Override
    public Name getName() {
        return SpecialNames.safeIdentifier(getFqName().shortName().asString());
    }
    
}
