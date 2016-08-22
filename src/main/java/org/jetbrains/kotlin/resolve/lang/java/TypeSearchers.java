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
package org.jetbrains.kotlin.resolve.lang.java;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaType;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class TypeSearchers {
    
    public static class BoundSearcher implements Task<CompilationController> {

        private JavaType bound = null;
        private final TypeMirrorHandle handle;
        private final Project project;
        
        public BoundSearcher(TypeMirrorHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            WildcardType type = (WildcardType) handle.resolve(info);
            TypeMirror superBound = type.getSuperBound();
            if (superBound != null) {
                bound = NetBeansJavaType.create(TypeMirrorHandle.create(superBound), project);
            }
        }
        
        public JavaType getBound() {
            return bound;
        }
    }
    
    public static class IsExtendsSearcher implements Task<CompilationController> {

        private boolean isExtends = false;
        private final TypeMirrorHandle handle;
        private final Project project;
        
        public IsExtendsSearcher(TypeMirrorHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            WildcardType type = (WildcardType) handle.resolve(info);
            isExtends = type.getExtendsBound() != null;
        }
        
        public boolean isExtends() {
            return isExtends;
        }
    }
     
    public static class ComponentTypeSearcher implements Task<CompilationController> {

        private JavaType componentType = null;
        private final TypeMirrorHandle handle;
        private final Project project;
        
        public ComponentTypeSearcher(TypeMirrorHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ArrayType type = (ArrayType) handle.resolve(info);
            componentType = NetBeansJavaType.create(TypeMirrorHandle.create(type.getComponentType()), project);
        }
        
        public JavaType getComponentType() {
            return componentType;
        }
    } 
    
}
