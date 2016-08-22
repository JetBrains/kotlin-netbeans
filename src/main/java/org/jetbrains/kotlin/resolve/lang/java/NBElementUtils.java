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
package org.jetbrains.kotlin.resolve.lang.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.ArgumentSearcher;
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.ArgumentsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.ClassIdSearcher;
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.JavaClassForAnnotationSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.ConstructorsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.ElementHandleSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.ElementKindSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.FieldsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.InnerClassesSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsAbstractSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsDeprecatedSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsFinalSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsStaticSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.MethodsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.OuterClassSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.SuperTypesSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.TypeParametersSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.VisibilitySearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.AnnotationParameterDefaultValueSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.ElementHandleForMemberSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberAbstractSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberDeprecatedSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberFinalSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberStaticSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.MemberClassSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.MemberTypeParametersSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.MemberVisibilitySearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.ReturnTypeSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.TypeMirrorHandleSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.ValueParametersSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.BinaryNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.FqNameForTypeVariable;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.PackageElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeMirrorHandleFromFQNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.UpperBoundsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.BoundSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.ComponentTypeSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsExtendsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsRawSearcher;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class NBElementUtils {
    
    private static final Map<Project,JavaSource> JAVA_SOURCE = new HashMap<Project,JavaSource>();
    private static final Map<Project,ClasspathInfo> CLASSPATH_INFO = new HashMap<Project,ClasspathInfo>();
    
    private static ClasspathInfo getClasspathInfo(Project kotlinProject){
        
        assert kotlinProject != null : "Project cannot be null";
        
        ClassPathExtender extendedProvider = KotlinProjectHelper.INSTANCE.getExtendedClassPath(kotlinProject);
        
        ClassPath boot = extendedProvider.getProjectSourcesClassPath(ClassPath.BOOT);
        ClassPath src = extendedProvider.getProjectSourcesClassPath(ClassPath.SOURCE);
        ClassPath compile = extendedProvider.getProjectSourcesClassPath(ClassPath.COMPILE);
        
        ClassPath bootProxy = ClassPathSupport.createProxyClassPath(boot, compile);
        
        return ClasspathInfo.create(bootProxy, src, compile);
    }
    
    public static Set<String> getPackages(Project project, String name) {
        if (!CLASSPATH_INFO.containsKey(project)){
            CLASSPATH_INFO.put(project, getClasspathInfo(project));
        }
        return CLASSPATH_INFO.get(project).getClassIndex().
                getPackageNames(name, false, EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
    }
    
    public static void updateClasspathInfo(Project kotlinProject){
        CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        JAVA_SOURCE.put(kotlinProject, JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
    }
    
    private static void checkJavaSource(Project kotlinProject) {
        if (!CLASSPATH_INFO.containsKey(kotlinProject)){
            CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        }
        if (!JAVA_SOURCE.containsKey(kotlinProject)){
            JAVA_SOURCE.put(kotlinProject,JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
        }
    }
    
    private static void execute(Task<CompilationController> searcher, Project project) {
        try {
            JAVA_SOURCE.get(project).runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static Collection<JavaClass> getInnerClasses(ElementHandle handle, Project project) {
        checkJavaSource(project);
        InnerClassesSearcher searcher = new InnerClassesSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getInnerClasses();
    }
    
    public static JavaClass getOuterClass(ElementHandle handle, Project project) {
        checkJavaSource(project);
        OuterClassSearcher searcher = new OuterClassSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getOuterClass();
    }
    
    public static Visibility getVisibility(ElementHandle handle, Project project) {
        checkJavaSource(project);
        VisibilitySearcher searcher = new VisibilitySearcher(handle);
        execute(searcher, project);
        
        return searcher.getVisibility();
    }
    
    public static boolean isAbstract(ElementHandle handle, Project project) {
        checkJavaSource(project);
        IsAbstractSearcher searcher = new IsAbstractSearcher(handle);
        execute(searcher, project);
        
        return searcher.isAbstract();
    }
    
    public static boolean isStatic(ElementHandle handle, Project project) {
        checkJavaSource(project);
        IsStaticSearcher searcher = new IsStaticSearcher(handle);
        execute(searcher, project);
        
        return searcher.isStatic();
    }
    
    public static boolean isFinal(ElementHandle handle, Project project) {
        checkJavaSource(project);
        IsFinalSearcher searcher = new IsFinalSearcher(handle);
        execute(searcher, project);
        
        return searcher.isFinal();
    }
    
    public static boolean isDeprecated(ElementHandle handle, Project project) {
        checkJavaSource(project);
        IsDeprecatedSearcher searcher = new IsDeprecatedSearcher(handle);
        execute(searcher, project);
        
        return searcher.isDeprecated();
    }
    
    public static List<JavaClassifierType> getUpperBounds(TypeMirrorHandle handle, Project project) {
        checkJavaSource(project);
        UpperBoundsSearcher searcher = new UpperBoundsSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getBounds();
    }
    
    public static String getFqNameForTypeVariable(TypeMirrorHandle handle, Project project) {
        checkJavaSource(project);
        FqNameForTypeVariable searcher = new FqNameForTypeVariable(handle);
        execute(searcher, project);
        
        return searcher.getFqName();
    }
    
    public static Collection<JavaClassifierType> getSuperTypes(ElementHandle handle, Project project) {
        checkJavaSource(project);
        SuperTypesSearcher searcher = new SuperTypesSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getSuperTypes();
    }
    
    public static List<JavaTypeParameter> getTypeParametersForTypeElement(ElementHandle handle, Project project) {
        checkJavaSource(project);
        TypeParametersSearcher searcher = new TypeParametersSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getTypeParameters();
    }
    
    public static List<JavaTypeParameter> getTypeParametersForMember(ElementHandle handle, Project project) {
        checkJavaSource(project);
        MemberTypeParametersSearcher searcher = new MemberTypeParametersSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getTypeParameters();
    }
    
    public static JavaType getReturnType(ElementHandle handle, Project project) {
        checkJavaSource(project);
        ReturnTypeSearcher searcher = new ReturnTypeSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getReturnType();
    }
    
    public static boolean hasDefaultValue(ElementHandle handle, Project project) {
        checkJavaSource(project);
        AnnotationParameterDefaultValueSearcher searcher = 
                new AnnotationParameterDefaultValueSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.hasDefaultValue();
    }
    
    public static List<JavaValueParameter> getValueParameters(ElementHandle handle, Project project) {
        checkJavaSource(project);
        ValueParametersSearcher searcher = new ValueParametersSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getValueParameters();
    }
    
    public static TypeMirrorHandle getTypeMirrorHandle(ElementHandle handle, Project project) {
        checkJavaSource(project);
        TypeMirrorHandleSearcher searcher = new TypeMirrorHandleSearcher(handle);
        execute(searcher, project);
        
        return searcher.getTypeMirrorHandle();
    }
    
    public static TypeMirrorHandle getTypeMirrorHandleFromFQName(FqName fqName, Project project) {
        checkJavaSource(project);
        TypeMirrorHandleFromFQNameSearcher searcher = new TypeMirrorHandleFromFQNameSearcher(fqName, project);
        execute(searcher, project);
        
        return searcher.getHandle();
    }
    
    public static JavaClass getJavaClassOfMember(ElementHandle handle, Project project) {
        checkJavaSource(project);
        MemberClassSearcher searcher = new MemberClassSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getJavaClass();
    }
    
    public static JavaAnnotationArgument findArgument(ElementHandle from, String mirrorName, Name name, Project project) {
        checkJavaSource(project);
        ArgumentSearcher searcher = new ArgumentSearcher(from, mirrorName, name, project);
        execute(searcher, project);
        
        return searcher.getArgument();
    }
    
    public static Collection<JavaAnnotationArgument> getArguments(ElementHandle from, String mirrorName, Project project) {
        checkJavaSource(project);
        ArgumentsSearcher searcher = new ArgumentsSearcher(from, mirrorName, project);
        execute(searcher, project);
        
        return searcher.getArguments();
    }
    
    public static ClassId getClassId(TypeMirrorHandle handle, Project project) {
        checkJavaSource(project);
        ClassIdSearcher searcher = new ClassIdSearcher(handle);
        execute(searcher, project);
        
        return searcher.getClassId();
    }
    
    public static JavaClass getJavaClassForAnnotation(TypeMirrorHandle handle, Project project) {
        checkJavaSource(project);
        JavaClassForAnnotationSearcher searcher = new JavaClassForAnnotationSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getJavaClass();
    }
    
    public static ElementHandle getElementhandleForMember(FqName fqName, Project project, JavaClass containingClass) {
        checkJavaSource(project);
        ElementHandleForMemberSearcher searcher = new ElementHandleForMemberSearcher(fqName.asString(), containingClass);
        execute(searcher, project);
        
        return searcher.getHandle();
    }
    
    public static boolean isMemberDeprecated(Project project, ElementHandle handle) {
        checkJavaSource(project);
        IsMemberDeprecatedSearcher searcher = new IsMemberDeprecatedSearcher(handle);
        execute(searcher, project);
        
        return searcher.isDeprecated();
    }
    
    public static boolean isMemberAbstract(Project project, ElementHandle handle) {
        checkJavaSource(project);
        IsMemberAbstractSearcher searcher = new IsMemberAbstractSearcher(handle);
        execute(searcher, project);
        
        return searcher.isAbstract();
    }
    
    public static boolean isMemberFinal(Project project, ElementHandle handle) {
        checkJavaSource(project);
        IsMemberFinalSearcher searcher = new IsMemberFinalSearcher(handle);
        execute(searcher, project);
        
        return searcher.isFinal();
    }
    
    public static boolean isMemberStatic(Project project, ElementHandle handle) {
        checkJavaSource(project);
        IsMemberStaticSearcher searcher = new IsMemberStaticSearcher(handle);
        execute(searcher, project);
        
        return searcher.isStatic();
    }
    
    public static Visibility getMemberVisibility(Project project, ElementHandle handle) {
        checkJavaSource(project);
        MemberVisibilitySearcher searcher = new MemberVisibilitySearcher(handle);
        execute(searcher, project);
        
        return searcher.getVisibility();
    }
    
    public static JavaType getBoundForWildcard(Project project, TypeMirrorHandle handle) {
        checkJavaSource(project);
        BoundSearcher searcher = new BoundSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getBound();
    }
    
    public static boolean isExtends(Project project, TypeMirrorHandle handle) {
        checkJavaSource(project);
        IsExtendsSearcher searcher = new IsExtendsSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.isExtends();
    }
    
    public static JavaType getComponentType(Project project, TypeMirrorHandle handle) {
        checkJavaSource(project);
        ComponentTypeSearcher searcher = new ComponentTypeSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.getComponentType();
    }
    
    public static boolean isRaw(Project project, TypeMirrorHandle handle) {
        checkJavaSource(project);
        IsRawSearcher searcher = new IsRawSearcher(handle, project);
        execute(searcher, project);
        
        return searcher.isRaw();
    }
    
    public static Collection<JavaMethod> getMethods(ElementHandle handle, JavaClass containingClass, Project project) {
        checkJavaSource(project);
        MethodsSearcher searcher = new MethodsSearcher(handle, containingClass, project);
        execute(searcher, project);
        
        return searcher.getMethods();
    }
    
    public static Collection<JavaConstructor> getConstructors(ElementHandle handle, JavaClass containingClass, Project project) {
        checkJavaSource(project);
        ConstructorsSearcher searcher = new ConstructorsSearcher(handle, containingClass, project);
        execute(searcher, project);
        
        return searcher.getConstructors();
    }
    
    public static Collection<JavaField> getFields(ElementHandle handle, JavaClass containingClass, Project project) {
        checkJavaSource(project);
        FieldsSearcher searcher = new FieldsSearcher(handle, containingClass, project);
        execute(searcher, project);
        
        return searcher.getFields();
    }
    
    public static ElementHandle getElementHandle(FqName fqName, Project project) {
        checkJavaSource(project);
        ElementHandleSearcher searcher = new ElementHandleSearcher(fqName.asString());
        execute(searcher, project);
        
        return searcher.getElementHandle();
    }
    
    public static TypeElement findTypeElement(Project kotlinProject, String fqName){
        checkJavaSource(kotlinProject);
        TypeElementSearcher searcher = new TypeElementSearcher(fqName);
        execute(searcher, kotlinProject);
        
        return searcher.getElement();
    }
    
    public static PackageElement findPackageElement(Project kotlinProject, String fqName){
        checkJavaSource(kotlinProject);
        PackageElementSearcher searcher = new PackageElementSearcher(fqName);
        execute(searcher, kotlinProject);
        
        return searcher.getElement();
    }
    
    public static List<String> findFQName(Project kotlinProject, String name) {
        checkJavaSource(kotlinProject);
        List<String> fqNames = new ArrayList<String>();
        
        final Set<ElementHandle<TypeElement>> result = 
                CLASSPATH_INFO.get(kotlinProject).getClassIndex().
                        getDeclaredTypes(name, ClassIndex.NameKind.SIMPLE_NAME, EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
        
        for (ElementHandle<TypeElement> handle : result) {
            fqNames.add(handle.getQualifiedName());
        }
        
        return fqNames;
    }
    
    public static Project getProject(Element element){
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        
        if (projects.length == 1){
            return projects[0];
        }
        
        for (Project project : projects){
            if (!KotlinProjectHelper.INSTANCE.checkProject(project)){
                continue;
            }
            
            ClasspathInfo cpInfo = CLASSPATH_INFO.get(project);
            if (cpInfo == null) {
                continue;
            }
            
            FileObject file = SourceUtils.getFile(ElementHandle.create(element), cpInfo);

            if (file != null){
                return project;
            }
            
        }
        return null;
    }
    
    public static boolean isDeprecated(final Element element){
        Project kotlinProject = NBElementUtils.getProject(element);
        
        if (kotlinProject == null){
            return false;
        }
        
        checkJavaSource(kotlinProject);
        org.jetbrains.kotlin.resolve.lang.java.Searchers.IsDeprecatedSearcher searcher 
                = new org.jetbrains.kotlin.resolve.lang.java.Searchers.IsDeprecatedSearcher(element);
        execute(searcher, kotlinProject);
        
        
        return searcher.isDeprecated();
    }
    
    public static String toBinaryName(Project kotlinProject, final String name){
        checkJavaSource(kotlinProject);
        
        BinaryNameSearcher searcher = new BinaryNameSearcher(name);
        execute(searcher, kotlinProject);
        
        return searcher.getBinaryName();
    }
    
    public static FileObject getFileObjectForElement(Element element, Project kotlinProject){
        if (element == null){
            return null;
        }
        ElementHandle<? extends Element> handle = ElementHandle.create(element);
        return SourceUtils.getFile(handle, CLASSPATH_INFO.get(kotlinProject));
    }
    
    public static void openElementInEditor(Element element, Project kotlinProject){
        ElementHandle<? extends Element> handle = ElementHandle.create(element);
        ElementOpen.open(CLASSPATH_INFO.get(kotlinProject), handle);
    }
    
}
