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
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaMember;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public abstract class NetBeansJavaMember<T extends Element> 
        extends NetBeansJavaElement<T> implements JavaMember {
    
    private final Collection<JavaAnnotation> annotations;
    private final List<? extends AnnotationMirror> annotationMirrors;
    private final Set<Modifier> modifiers;
    private final Visibility visibility;
    private final Name name;
    private final boolean isDeprecated;
    
    protected NetBeansJavaMember(@NotNull T javaElement){
        super(javaElement);
        annotations = getAnnotations(javaElement);
        annotationMirrors = javaElement.getAnnotationMirrors();
        modifiers = javaElement.getModifiers();
        visibility = NetBeansJavaElementUtil.getVisibility(javaElement);
        name = Name.identifier(javaElement.getSimpleName().toString());
        isDeprecated = NetBeansJavaProjectElementUtils.isDeprecated(javaElement);
    }
    
    @NotNull
    private Collection<JavaAnnotation> getAnnotations(T javaElement){
        List<? extends AnnotationMirror> annotationList = javaElement.getAnnotationMirrors();
        return annotations(annotationList.toArray(new AnnotationMirror[annotationList.size()]));
    }
    
    @Override
    @NotNull
    public Collection<JavaAnnotation> getAnnotations(){
        return annotations;
    }
    
    @Override
    @Nullable
    public JavaAnnotation findAnnotation(@NotNull FqName fqName){
        return NetBeansJavaElementUtil.findAnnotation(annotationMirrors, fqName);
    }
    
    @Override
    public boolean isAbstract(){
        return NetBeansJavaElementUtil.isAbstract(modifiers);
    }
    
    @Override
    public boolean isStatic(){
        return NetBeansJavaElementUtil.isStatic(modifiers);
    }
    
    @Override
    public boolean isFinal(){
        return NetBeansJavaElementUtil.isFinal(modifiers);
    }
    
    @Override
    @NotNull
    public Visibility getVisibility(){
        return visibility;
    }
    
    @Override
    @NotNull
    public Name getName(){
        return name;
    }
    
    @Override 
    public boolean isDeprecatedInJavaDoc(){
        return isDeprecated;
    }
    
}
