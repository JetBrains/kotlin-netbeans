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
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;

/**
 *
 * @author Александр
 */
public class NetBeansJavaTypeParameter extends NetBeansJavaClassifier<ElementHandle<TypeParameterElement>> implements JavaTypeParameter {
    
    public NetBeansJavaTypeParameter(TypeParameterElement binding){
        super(binding);
    }

    @Override
    public Name getName() {
        return SpecialNames.safeIdentifier(getElement().getSimpleName().toString());
    }

    @Override
    public Collection<JavaClassifierType> getUpperBounds() {
        List<JavaClassifierType> bounds = Lists.newArrayList();
        
        for (TypeMirror bound : ((TypeParameterElement) getElement()).getBounds()){
            bounds.add(new NetBeansJavaClassifierType(TypeMirrorHandle.create(bound)));
        }
        
        return bounds;
    }
    
}
