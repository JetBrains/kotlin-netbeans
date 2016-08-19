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


import com.google.common.collect.Lists;
import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.types;
import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;
import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.classifierTypes;
import java.util.Collections;
import java.util.List;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassifierType extends NetBeansJavaType<TypeMirror> implements JavaClassifierType {
    
    private JavaClassifier classifier;
    private final boolean isRaw;
    private final List<JavaType> typeArguments;
    private TypeMirror type;
    
    public NetBeansJavaClassifierType(TypeMirror typeBinding){
        super(typeBinding);
//        classifier = getClassifier(typeBinding);
        isRaw = isRaw(typeBinding);
        typeArguments = getTypeArguments(typeBinding);
        type = typeBinding;
    }

    private JavaClassifier getClassifier(TypeMirror typeBinding) {
        switch (getBinding().getKind()) {
            case DECLARED:
                return NetBeansJavaClassifier.create(((DeclaredType)typeBinding).asElement());
            case TYPEVAR:
                return NetBeansJavaClassifier.create(((TypeVariable) typeBinding).asElement());
            default:
                return null;
        }
    }
    
    @Override
    public JavaClassifier getClassifier() {
        if (classifier == null) {
            classifier = getClassifier(type);
            type = null;
        }
        return classifier;
    }

    @Override
    public String getPresentableText() {
        return getBinding().toString();
    }

    private boolean isRaw(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
                return ((DeclaredType) type).getTypeArguments().isEmpty();
        } else return true;
    }
    
    @Override
    public boolean isRaw() {
        return isRaw;
    }

    private List<JavaType> getTypeArguments(TypeMirror type) {
        List<TypeMirror> typeArgs = Lists.newArrayList();
        
        if (getBinding().getKind() == TypeKind.DECLARED){
            
            for (TypeMirror elem : ((DeclaredType) type).getTypeArguments()){
                typeArgs.add(elem);
            }
            return types(typeArgs.toArray(new TypeMirror[typeArgs.size()]));
        }
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public List<JavaType> getTypeArguments() {
        return typeArguments;
    }
    
    @Override
    public String getCanonicalText() {
        return getBinding().toString();
    }
    
}
