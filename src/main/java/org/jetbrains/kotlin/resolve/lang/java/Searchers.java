package org.jetbrains.kotlin.resolve.lang.java;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClassifierType;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaElementUtil;
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

        private ElementHandle<TypeElement> element;
        private final String fqName;

        public TypeElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            element = ElementHandle.create(info.getElements().getTypeElement(fqName));
        }

        public ElementHandle<TypeElement> getElement() {
            return element;
        }

    }
    
    public static class TypeMirrorSearcher implements Task<CompilationController> {

        private TypeMirror mirror;
        private final String fqName;

        public TypeMirrorSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            mirror = info.getElements().getTypeElement(fqName).asType();
        }

        public TypeMirror getMirror() {
            return mirror;
        }

    }

    public static class PackageElementSearcher implements Task<CompilationController> {

        private ElementHandle<PackageElement> element;
        private final String fqName;

        public PackageElementSearcher(String fqName) {
            this.fqName = fqName;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            element = ElementHandle.create(info.getElements().getPackageElement(fqName));
        }

        public ElementHandle<PackageElement> getElement() {
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
    
    public static class FqNameForTypeVariable implements Task<CompilationController> {

        private final TypeMirrorHandle typeHandle;
        private String fqName;
        
        public FqNameForTypeVariable(TypeMirrorHandle typeHandle) {
            this.typeHandle = typeHandle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeVariable type = (TypeVariable) typeHandle.resolve(info);
            fqName = type.asElement().getSimpleName().toString();
        }
        
        public String getFqName() {
            return fqName;
        }
        
    }
    
    public static class TypeMirrorHandleFromFQNameSearcher implements Task<CompilationController> {

        private final FqName fqName;
        private final Project project;
        private TypeMirrorHandle handle;
        
        public TypeMirrorHandleFromFQNameSearcher(FqName fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement element = info.getElements().getTypeElement(fqName.asString());
            TypeMirror mirror = element.asType();
            handle = TypeMirrorHandle.create(mirror);
        }
        
        
        public TypeMirrorHandle getHandle() {
            return handle;
        }
    }
    
    public static class ClassIdComputer implements Task<CompilationController> {

        private final ElementHandle handle;
        private ClassId classId = null;
        
        public ClassIdComputer(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = (TypeElement) handle.resolve(info);
            classId = NetBeansJavaElementUtil.computeClassId(elem);
        }
        
        public ClassId getClassId() {
            return classId;
        }
        
    }
    
}
