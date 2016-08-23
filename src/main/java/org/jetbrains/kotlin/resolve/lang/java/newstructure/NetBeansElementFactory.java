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

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.FqName;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansElementFactory {

    public static List<JavaClassifierType> classifierTypes(TypeMirror[] classTypes, Project project) {
        if (classTypes.length == 0) {
            return Collections.emptyList();
        }
        List<JavaClassifierType> result = Lists.newArrayList();
        for (TypeMirror mirror : classTypes) {
            
            result.add(new NetBeansJavaClassifierType(TypeMirrorHandle.create(mirror), project));
            
        }
        return result;
    }

    public static List<JavaTypeParameter> typeParameters(@NotNull TypeParameterElement[] typeParameters, Project project){
        if (typeParameters.length == 0) {
            return Collections.emptyList();
        }
        List<JavaTypeParameter> result = Lists.newArrayList();
        for (TypeParameterElement element : typeParameters) {
            TypeMirrorHandle handle = TypeMirrorHandle.create(element.asType());
            result.add(new NetBeansJavaTypeParameter(new FqName(element.getSimpleName().toString()), project, handle));
        }
        return result;
    }
    
    public static List<JavaType> types(@NotNull TypeMirror[] types, Project project) {
        if (types.length == 0) {
            return Collections.emptyList();
        }
        List<JavaType> result = Lists.newArrayList();
        for (TypeMirror mirror : types) {
            NetBeansJavaType.create(TypeMirrorHandle.create(mirror), project);
        }
        return result;
    }
    
    public static List<JavaAnnotation> annotations(@NotNull AnnotationMirror[] annotations, Project project, ElementHandle from){
        if (annotations.length == 0) {
            return Collections.emptyList();
        }
        List<JavaAnnotation> result = Lists.newArrayList();
        for (AnnotationMirror mirror : annotations) {
            result.add(new NetBeansJavaAnnotation(from, project, TypeMirrorHandle.create(mirror.getAnnotationType())));
        }
        return result;
    }
    
}
