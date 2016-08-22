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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaAnnotationArgument extends NetBeansJavaElement implements JavaAnnotationArgument {

    public NetBeansJavaAnnotationArgument(FqName fqName, Project project) {
        super(fqName, project);
    }
    
    @Override
    public Name getName() {
        return Name.identifier(getFqName().shortName().asString());
    }
    
    public static JavaAnnotationArgument create(Object value, Name name, Project project, ElementHandle fromElement){
        
        if (value instanceof AnnotationMirror){
            TypeMirrorHandle typeHandle = TypeMirrorHandle.create(((AnnotationMirror) value).getAnnotationType());
            return new NetBeansJavaAnnotationAsAnnotationArgument(fromElement, project, 
                    ((AnnotationMirror) value).toString(), name, typeHandle);
        }
        else if (value instanceof VariableElement){
            return new NetBeansJavaReferenceAnnotationArgument(new FqName(((VariableElement) value).getSimpleName().toString()),
                    project, ElementHandle.create((VariableElement) value));
        }
        else if (value instanceof String){
            return new NetBeansJavaLiteralAnnotationArgument(value, name);
        }
        else if (value instanceof Class<?>){
            return new NetBeansJavaClassObjectAnnotationArgument((Class) value, name, project);
        }
        else if (value instanceof Collection<?>){
            return new NetBeansJavaArrayAnnotationArgument((Collection) value, name, project, fromElement);
        } 
        else if (value instanceof AnnotationValue){
            return create(((AnnotationValue) value).getValue(), name, project, fromElement);
        } else return null;
    }
    
}
