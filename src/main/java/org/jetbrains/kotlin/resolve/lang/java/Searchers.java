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

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;

/**
 *
 * @author Alexander.Baratynski
 */
public class Searchers {

    public static class TypeElementSearcher implements CancellableTask<CompilationController> {

        private TypeElement element;
        private final String fqName;

        public TypeElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            element = info.getElements().getTypeElement(fqName);
        }

        public TypeElement getElement() {
            return element;
        }

    }

    public static class PackageElementSearcher implements CancellableTask<CompilationController> {

        private PackageElement element;
        private final String fqName;

        public PackageElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            element = info.getElements().getPackageElement(fqName);
        }

        public PackageElement getElement() {
            return element;
        }

    }

    public static class AnnotationMirrorsSearcher implements CancellableTask<CompilationController> {

        private List<? extends AnnotationMirror> annotations = 
                new ArrayList<AnnotationMirror>();
        private final ElementHandle handle;

        public AnnotationMirrorsSearcher(ElementHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element element = handle.resolve(info);
            if (element != null) {
                annotations = element.getAnnotationMirrors();
            }
        }

        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            return annotations;
        }
    }
    
    public static class ModifiersSearcher implements CancellableTask<CompilationController> {

        private Set<Modifier> modifiers = 
                new HashSet<Modifier>();
        private final ElementHandle handle;

        public ModifiersSearcher(ElementHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element element = handle.resolve(info);
            if (element != null) {
                modifiers = element.getModifiers();
            }
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }
    }
    
    public static class SimpleNameSearcher implements CancellableTask<CompilationController> {

        private Name simpleName;
        private final ElementHandle handle;

        public SimpleNameSearcher(ElementHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element element = handle.resolve(info);
            if (element != null) {
                simpleName = element.getSimpleName();
            }
        }

        public Name getSimpleName() {
            return simpleName;
        }
    }
    
    public static class EnclosedElementsSearcher implements CancellableTask<CompilationController> {

        private List<? extends Element> elements = Collections.emptyList();
        private final ElementHandle<TypeElement> handle;

        public EnclosedElementsSearcher(ElementHandle<TypeElement> handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement element = handle.resolve(info);
            if (element != null) {
                elements = element.getEnclosedElements();
                
            }
        }

        public List<? extends Element> getEnclosedElements() {
            return elements;
        }
    }
    
    public static class EnclosingElementSearcher implements CancellableTask<CompilationController> {

        private Element elem = null;
        private final ElementHandle<TypeElement> handle;

        public EnclosingElementSearcher(ElementHandle<TypeElement> handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement element = handle.resolve(info);
            if (element != null) {
                elem = element.getEnclosingElement();
                
            }
        }

        public Element getEnclosingElement() {
            return elem;
        }
    }
    
    public static class ElementSearcher implements CancellableTask<CompilationController> {

        private TypeElement elem = null;
        private final ElementHandle<TypeElement> handle;

        public ElementSearcher(ElementHandle<TypeElement> handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            elem = handle.resolve(info);
        }

        public TypeElement getElement() {
            return elem;
        }
    }
    
    public static class ElementHandleInstanceOfTypeElementChecker implements CancellableTask<CompilationController> {

        private boolean isInstanceof = false;
        private final ElementHandle<? extends Element> handle;

        public ElementHandleInstanceOfTypeElementChecker(ElementHandle<? extends Element> handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element element = handle.resolve(info);
            if (element != null) {
                if (element instanceof TypeElement) {
                    isInstanceof = true;
                }
            }
        }

        public boolean getInstanceof() {
            return isInstanceof;
        }
    }
    
}
