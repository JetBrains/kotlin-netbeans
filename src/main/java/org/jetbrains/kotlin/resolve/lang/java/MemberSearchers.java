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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansElementFactory;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaAnnotation;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaType;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaElementUtil;
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
    
    public static class ValueParametersSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private final Project project;
        private List<JavaValueParameter> parameters = new ArrayList<JavaValueParameter>();
        
        public ValueParametersSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ExecutableElement elem = (ExecutableElement) handle.resolve(info);
            parameters = NetBeansJavaElementUtil.getValueParameters(elem, project);
        }
        
        
        public List<JavaValueParameter> getValueParameters() {
            return parameters;
        }
    }
    
    public static class TypeMirrorHandleSearcher implements Task<CompilationController> {

        private final ElementHandle elementHandle;
        private TypeMirrorHandle handle;
        
        public TypeMirrorHandleSearcher(ElementHandle elementHandle) {
            this.elementHandle = elementHandle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element element = elementHandle.resolve(info);
            handle = TypeMirrorHandle.create(element.asType());
        }
        
        public TypeMirrorHandle getTypeMirrorHandle() {
            return handle;
        }
        
    }
    
    public static class MemberClassSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private JavaClass javaClass;
        private final Project project;
        
        public MemberClassSearcher(ElementHandle handle, Project project) {
            this.project = project;
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element elem = handle.resolve(info);
            while (!elem.getKind().isClass() && !elem.getKind().isInterface()) {
                elem = elem.getEnclosingElement();
            }
            
            javaClass = new NetBeansJavaClass(new FqName(((TypeElement) elem).getQualifiedName().toString()), project);
        }
        
        public JavaClass getJavaClass() {
            return javaClass;
        }
        
    }
    
    public static class MemberTypeParametersSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private final Project project;
        private List<JavaTypeParameter> typeParams;
        
        public MemberTypeParametersSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ExecutableElement elem = (ExecutableElement) handle.resolve(info);
            List<? extends TypeParameterElement> typeParameters = elem.getTypeParameters();
            typeParams = NetBeansElementFactory.typeParameters(typeParameters.
                    toArray(new TypeParameterElement[typeParameters.size()]), project);
        }
        
        public List<JavaTypeParameter> getTypeParameters() {
            return typeParams;
        }
        
    }
    
    public static class AnnotationsForFieldSearcher implements Task<CompilationController> {

        private Collection<JavaAnnotation> annotations = Lists.newArrayList();
        private final ElementHandle handle;
        private final Project project;
        
        public AnnotationsForFieldSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            VariableElement elem = (VariableElement) handle.resolve(info);
            List<? extends AnnotationMirror> mirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : mirrors) {
                annotations.add(new NetBeansJavaAnnotation(handle, project, TypeMirrorHandle.create(mirror.getAnnotationType())));
            }
        }
        
        public Collection<JavaAnnotation> getAnnotations() {
            return annotations;
        }
        
    }
    
    public static class AnnotationsForExecutableSearcher implements Task<CompilationController> {

        private Collection<JavaAnnotation> annotations = Lists.newArrayList();
        private final ElementHandle handle;
        private final Project project;
        
        public AnnotationsForExecutableSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ExecutableElement elem = (ExecutableElement) handle.resolve(info);
            List<? extends AnnotationMirror> mirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : mirrors) {
                annotations.add(new NetBeansJavaAnnotation(handle, project, TypeMirrorHandle.create(mirror.getAnnotationType())));
            }
        }
        
        public Collection<JavaAnnotation> getAnnotations() {
            return annotations;
        }
        
    }
    
    public static class AnnotationForFieldSearcher implements Task<CompilationController> {

        private JavaAnnotation annotation = null;
        private final ElementHandle handle;
        private final Project project;
        private final FqName fqName;
        
        public AnnotationForFieldSearcher(ElementHandle handle, Project project, FqName fqName) {
            this.handle = handle;
            this.project = project;
            this.fqName = fqName;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            VariableElement elem = (VariableElement) handle.resolve(info);
            List<? extends AnnotationMirror> mirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : mirrors) {
                String annotationFQName = mirror.getAnnotationType().toString();
                if (fqName.asString().equals(annotationFQName)){
                    annotation = new NetBeansJavaAnnotation(handle, project, TypeMirrorHandle.create(mirror.getAnnotationType()));
                    break;
                }
            }
        }
        
        public JavaAnnotation getAnnotation() {
            return annotation;
        }
        
    }
    
    public static class AnnotationForExecutableSearcher implements Task<CompilationController> {

        private JavaAnnotation annotation = null;
        private final ElementHandle handle;
        private final Project project;
        private final FqName fqName;
        
        public AnnotationForExecutableSearcher(ElementHandle handle, Project project, FqName fqName) {
            this.handle = handle;
            this.project = project;
            this.fqName = fqName;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            ExecutableElement elem = (ExecutableElement) handle.resolve(info);
            List<? extends AnnotationMirror> mirrors = elem.getAnnotationMirrors();
            for (AnnotationMirror mirror : mirrors) {
                String annotationFQName = mirror.getAnnotationType().toString();
                if (fqName.asString().equals(annotationFQName)){
                    annotation = new NetBeansJavaAnnotation(handle, project, TypeMirrorHandle.create(mirror.getAnnotationType()));
                    break;
                }
            }
        }
        
        public JavaAnnotation getAnnotation() {
            return annotation;
        }
        
    }
    
}
