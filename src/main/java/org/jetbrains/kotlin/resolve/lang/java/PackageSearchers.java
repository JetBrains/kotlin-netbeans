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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import kotlin.jvm.functions.Function1;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClass;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class PackageSearchers {
    
    public static class ClassesInPackageSearcher implements Task<CompilationController> {

        private final ElementHandle<PackageElement> handle;
        private final Function1<? super Name, ? extends Boolean> nameFilter;
        private final Project project;
        private List<JavaClass> javaClasses;
        
        public ClassesInPackageSearcher(ElementHandle<PackageElement> handle, Function1<? super Name, ? extends Boolean> nameFilter,
                Project project) {
            this.handle = handle;
            this.nameFilter = nameFilter;
            this.project = project;
        }

        private boolean isOuterClass(TypeElement classFile){
            return !classFile.getSimpleName().toString().contains("$");
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(JavaSource.Phase.RESOLVED);
            PackageElement pack = handle.resolve(info);
            List<? extends Element> classes = pack.getEnclosedElements();
        
            for (Element cl : classes){
                if (isOuterClass((TypeElement) cl)){
                    String elementName = cl.getSimpleName().toString();
                    if (Name.isValidIdentifier(elementName) && nameFilter.invoke(Name.identifier(elementName))){
                        FqName fqName = new FqName(((TypeElement) cl).getQualifiedName().toString());
                        javaClasses.add(new NetBeansJavaClass(fqName, project));
                    }
                }
            }
        }

        public List<JavaClass> getJavaClasses() {
            return javaClasses;
        }

    }
    
}
