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

import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.model.KotlinEnvironment;
//import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
//import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaPackage;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.JavaClassFinder;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.resolve.jvm.JavaClassFinderPostConstruct;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaPackage;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassFinder implements JavaClassFinder {

    private org.netbeans.api.project.Project kotlinProject = null;
    
    @Inject
    public void setProjectScope(@NotNull org.netbeans.api.project.Project project){
        kotlinProject = project;
    }
    
    @Inject
    public void setComponentPostConstruct(@NotNull JavaClassFinderPostConstruct finderPostConstruct) {
    }
    
    @Override
    @Nullable
    public JavaClass findClass(ClassId classId) {
        ElementHandle<TypeElement> element = findType(classId.asSingleFqName(), kotlinProject);
        if (element != null) {
            return new NetBeansJavaClass(new FqName(element.getQualifiedName()), kotlinProject);
        }
        
        return null;
    }

    @Override
    public JavaPackage findPackage(FqName fqName) {
        ElementHandle<PackageElement> packageEl = NBElementUtils.findPackageElement(kotlinProject, fqName.asString());
        if (packageEl != null){
            return new NetBeansJavaPackage(packageEl, kotlinProject);
        } 
        
        return null;
    }

    @Nullable
    public static ElementHandle<TypeElement> findType(@NotNull FqName fqName, @NotNull org.netbeans.api.project.Project project){
        ElementHandle<TypeElement> type = NBElementUtils.findTypeElement(project, fqName.asString());
        return type;
    }

    
    @Nullable 
    public static ElementHandle<PackageElement>[] findPackageFragments(org.netbeans.api.project.Project kotlinProject, String name,
            boolean partialMatch, boolean patternMatch){
        Set<String> packages = NBElementUtils.getPackages(kotlinProject, name);
        List<ElementHandle<PackageElement>> subpackageElements = Lists.newArrayList();
        for (String pack : packages) {
            ElementHandle<PackageElement> subpackageElement = NBElementUtils.
                    findPackageElement(kotlinProject,pack);
            if (subpackageElement == null){
                continue;
            }
            subpackageElements.add(subpackageElement);
        }
        
        if (subpackageElements.isEmpty()){
            return null;
        }
        
        return subpackageElements.toArray(new ElementHandle[subpackageElements.size()]);
    }
    
    public static boolean isInKotlinBinFolder(@NotNull Element element){
        
        return false;
    }

    @Override
    public Set<String> knownClassNamesInPackage(FqName packageFqName) {
        return null;
    }
    
}

