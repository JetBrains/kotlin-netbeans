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
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.ElementKindSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.InnerClassesSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsAbstractSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsDeprecatedSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsFinalSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.IsStaticSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.OuterClassSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.VisibilitySearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.ElementHandleForMemberSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberAbstractSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberDeprecatedSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberFinalSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsMemberStaticSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.MemberVisibilitySearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.BinaryNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.PackageElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.BoundSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsExtendsSearcher;
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
    
    public static Collection<JavaClass> getInnerClasses(FqName fqName, Project project) {
        checkJavaSource(project);
        InnerClassesSearcher searcher = new InnerClassesSearcher(fqName.asString(), project);
        execute(searcher, project);
        
        return searcher.getInnerClasses();
    }
    
    public static JavaClass getOuterClass(FqName fqName, Project project) {
        checkJavaSource(project);
        OuterClassSearcher searcher = new OuterClassSearcher(fqName.asString(), project);
        execute(searcher, project);
        
        return searcher.getOuterClass();
    }
    
    public static Visibility getVisibility(FqName fqName, Project project) {
        checkJavaSource(project);
        VisibilitySearcher searcher = new VisibilitySearcher(fqName.asString(), project);
        execute(searcher, project);
        
        return searcher.getVisibility();
    }
    
    public static boolean isAbstract(FqName fqName, Project project) {
        checkJavaSource(project);
        IsAbstractSearcher searcher = new IsAbstractSearcher(fqName.asString(), project);
        execute(searcher, project);
        
        return searcher.isAbstract();
    }
    
    public static boolean isStatic(FqName fqName, Project project) {
        checkJavaSource(project);
        IsStaticSearcher searcher = new IsStaticSearcher(fqName.asString(), project);
        execute(searcher, project);
        
        return searcher.isStatic();
    }
    
    public static boolean isFinal(FqName fqName, Project project) {
        checkJavaSource(project);
        IsFinalSearcher searcher = new IsFinalSearcher(fqName.asString(), project);
        execute(searcher, project);
        
        return searcher.isFinal();
    }
    
    public static boolean isDeprecated(FqName fqName, Project project) {
        checkJavaSource(project);
        IsDeprecatedSearcher searcher = new IsDeprecatedSearcher(fqName.asString());
        execute(searcher, project);
        
        return searcher.isDeprecated();
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
    
    public static ElementKind getElementKind(FqName fqName, Project project) {
        checkJavaSource(project);
        ElementKindSearcher searcher = new ElementKindSearcher(fqName.asString());
        execute(searcher, project);
        
        return searcher.getKind();
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
