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

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaType;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementUtil;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class MemberSearchers {
    
    public static class ElementHandleForMemberSearcher implements Task<CompilationController> {

        private final String fqName;
        private final JavaClass containingClass;
        private ElementHandle handle;
        
        public ElementHandleForMemberSearcher(String fqName, JavaClass containingClass) {
            this.fqName = fqName;
            this.containingClass = containingClass;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement element = info.getElements().getTypeElement(containingClass.getFqName().asString());
            List<? extends Element> enclosedElements = element.getEnclosedElements();
            for (Element elem : enclosedElements) {
                if (fqName.equals(elem.getSimpleName().toString())) {
                    handle = ElementHandle.create(elem);
                    break;
                }
            }
        }
        
        public ElementHandle getHandle() {
            return handle;
        }
    }
    
    public static class IsMemberDeprecatedSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isDeprecated;
        
        public IsMemberDeprecatedSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            Element element = handle.resolve(info);
            isDeprecated = info.getElements().isDeprecated(element);
        }
        
        public boolean isDeprecated() {
            return isDeprecated;
        }
    }
    
    public static class IsMemberAbstractSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isAbstract = false;
        
        public IsMemberAbstractSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            Element element = handle.resolve(info);
            isAbstract = NetBeansJavaElementUtil.isAbstract(element.getModifiers());
        }
        
        public boolean isAbstract() {
            return isAbstract;
        }
    }
    
    public static class IsMemberStaticSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isStatic = false;
        
        public IsMemberStaticSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            Element element = handle.resolve(info);
            isStatic = NetBeansJavaElementUtil.isStatic(element.getModifiers());
        }
        
        public boolean isStatic() {
            return isStatic;
        }
    }
    
    public static class IsMemberFinalSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isFinal = false;
        
        public IsMemberFinalSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            Element element = handle.resolve(info);
            isFinal = NetBeansJavaElementUtil.isFinal(element.getModifiers());
        }
        
        public boolean isFinal() {
            return isFinal;
        }
    }
    
    public static class MemberVisibilitySearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private Visibility visibility = null;
        
        public MemberVisibilitySearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            Element member = handle.resolve(info);
            if (NetBeansJavaElementUtil.isPublic(member.getModifiers())){
                visibility = Visibilities.PUBLIC;
                return;
            } else if (NetBeansJavaElementUtil.isPrivate(member.getModifiers())){
                visibility = Visibilities.PRIVATE;
                return;
            } else if (NetBeansJavaElementUtil.isProtected(member.getModifiers())){
                visibility = NetBeansJavaElementUtil.isStatic(member.getModifiers()) ? JavaVisibilities.PROTECTED_STATIC_VISIBILITY :
                        JavaVisibilities.PROTECTED_AND_PACKAGE;
                return;
            }

            visibility = JavaVisibilities.PACKAGE_VISIBILITY;
        }
        
        public Visibility getVisibility() {
            return visibility;
        }
    }
    
    public static class ReturnTypeSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private final Project project;
        private JavaType returnType;
        
        public ReturnTypeSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ExecutableElement exec = (ExecutableElement) handle.resolve(info);
            returnType = NetBeansJavaType.create(TypeMirrorHandle.create(exec.getReturnType()), project);
        }
        
        public JavaType getReturnType() {
            return returnType;
        }
        
    }
    
    public static class AnnotationParameterDefaultValueSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private final Project project;
        private boolean hasDefaultValue = false;
        
        public AnnotationParameterDefaultValueSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ExecutableElement exec = (ExecutableElement) handle.resolve(info);
            hasDefaultValue = exec.getDefaultValue() != null;
        }
        
        public boolean hasDefaultValue() {
            return hasDefaultValue;
        }
        
    }
    
}
