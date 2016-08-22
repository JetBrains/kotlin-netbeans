package org.jetbrains.kotlin.resolve.lang.java;

import com.google.common.collect.Lists;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClassifierType;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class Searchers {

    public static class TypeElementSearcher implements Task<CompilationController> {

        private TypeElement element;
        private final String fqName;

        public TypeElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            element = info.getElements().getTypeElement(fqName);
        }

        public TypeElement getElement() {
            return element;
        }

    }

    public static class PackageElementSearcher implements Task<CompilationController> {

        private PackageElement element;
        private final String fqName;

        public PackageElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            element = info.getElements().getPackageElement(fqName);
        }

        public PackageElement getElement() {
            return element;
        }

    }

    public static class BinaryNameSearcher implements Task<CompilationController> {

        private final String name;
        private String binaryName;
        
        public BinaryNameSearcher(String name) {
            this.name = name;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = info.getElements().getTypeElement(name);
            binaryName = info.getElements().getBinaryName(elem).toString();
        }
        
        public String getBinaryName() {
            return binaryName;
        }
    }
    
    public static class IsDeprecatedSearcher implements Task<CompilationController> {

        private final Element element;
        private boolean isDeprecated;
        
        public IsDeprecatedSearcher(Element element) {
            this.element = element;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            isDeprecated = info.getElements().isDeprecated(element);
        }
        
        public boolean isDeprecated() {
            return isDeprecated;
        }
    }

    public static class UpperBoundsSearcher implements Task<CompilationController> {

        private final List<JavaClassifierType> bounds = Lists.newArrayList();
        private final TypeMirrorHandle handle;
        private final Project project;
        
        public UpperBoundsSearcher(TypeMirrorHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            
            TypeVariable type = (TypeVariable) handle.resolve(info);
            TypeParameterElement element = (TypeParameterElement) type.asElement();
            
            for (TypeMirror bound : element.getBounds()){
                bounds.add(new NetBeansJavaClassifierType(TypeMirrorHandle.create(bound), project));
            }
        }
        
        public List<JavaClassifierType> getBounds() {
            return bounds;
        }
    }
    
    public static class ElementHandleForTypeVariable implements Task<CompilationController> {

        private final TypeMirrorHandle typeHandle;
        private ElementHandle handle;
        
        public ElementHandleForTypeVariable(TypeMirrorHandle typeHandle) {
            this.typeHandle = typeHandle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeVariable type = (TypeVariable) typeHandle.resolve(info);
            handle = ElementHandle.create(type.asElement());
        }
        
        public ElementHandle getElementHandle() {
            return handle;
        }
        
    }
    
}
