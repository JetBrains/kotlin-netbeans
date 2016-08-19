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
package org.jetbrains.kotlin.resolve.lang.java.structure;

import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.annotations;

import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public class NetBeansJavaValueParameter extends NetBeansJavaElement<VariableElement> implements JavaValueParameter{

    private final String name;
    private final boolean isVararg;
    private final JavaType type;
    private final List<? extends AnnotationMirror> annotationMirrors;
    
    public NetBeansJavaValueParameter(VariableElement type, String name, boolean isVararg){
        super(type);
        this.name = name;
        this.isVararg = isVararg;
        this.type = NetBeansJavaType.create(type.asType());
        annotationMirrors = type.getAnnotationMirrors();
    }
    
    @Override
    public Name getName() {
        return Name.identifier(name);
    }

    @Override
    public JavaType getType() {
        return type;
    }

    @Override
    public boolean isVararg() {
        return isVararg;
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return annotations(annotationMirrors.toArray(new AnnotationMirror[annotationMirrors.size()]));
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        return NetBeansJavaElementUtil.findAnnotation(annotationMirrors, fqName);
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return false;//temporary
    }
    
}
