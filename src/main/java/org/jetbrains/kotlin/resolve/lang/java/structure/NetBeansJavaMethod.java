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

import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaMethod extends NetBeansJavaMember<ExecutableElement> implements JavaMethod{
    
    private final List<JavaValueParameter> valueParameters;
    private final boolean hasAnnotationParameterDefaultValue;
    private final JavaType returnType;
    private final JavaClass containingClass;
    private final List<JavaTypeParameter> typeParameters;
    
    public NetBeansJavaMethod(ExecutableElement method){
        super(method);
        valueParameters = NetBeansJavaElementUtil.getValueParameters(method);
        hasAnnotationParameterDefaultValue = method.getDefaultValue() != null;
        returnType = NetBeansJavaType.create(method.getReturnType());
        containingClass = new NetBeansJavaClass((TypeElement) method.getEnclosingElement());
        typeParameters = getTypeParameters(method);
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return valueParameters;
    }

    @Override
    public boolean getHasAnnotationParameterDefaultValue() {
        return hasAnnotationParameterDefaultValue;
    }

    @Override
    @NotNull
    public JavaType getReturnType() {
        return returnType;
    }

    @Override
    public JavaClass getContainingClass() {
        return containingClass;
    }
    
    private List<JavaTypeParameter> getTypeParameters(ExecutableElement el) {
        List<? extends TypeParameterElement> params = el.getTypeParameters();
        return typeParameters(params.toArray(new TypeParameterElement[params.size()]));
    }
    
    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        return typeParameters;
    }
}
