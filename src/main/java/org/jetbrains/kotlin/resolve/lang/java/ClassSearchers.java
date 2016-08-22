package org.jetbrains.kotlin.resolve.lang.java;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementUtil;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class ClassSearchers {

    public static class InnerClassesSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private final Collection<JavaClass> innerClasses = Lists.newArrayList();;
        private final Project project;
        
        public InnerClassesSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = (TypeElement) handle.resolve(info);
            List<? extends Element> enclosedElements = elem.getEnclosedElements();
            for (Element element : enclosedElements){
                if (element.asType().getKind() == TypeKind.DECLARED 
                        && element instanceof TypeElement){
                    innerClasses.add(new NetBeansJavaClass(
                            new FqName(((TypeElement) element).
                                    getQualifiedName().toString()),
                            project));
                }
            }
        }
        
        public Collection<JavaClass> getInnerClasses() {
            return innerClasses;
        }
    }
    
    public static class OuterClassSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private JavaClass outerClass = null;
        private final Project project;
        
        public OuterClassSearcher(ElementHandle handle, Project project) {
            this.handle = handle;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement elem = (TypeElement) handle.resolve(info);
            Element outerCl = elem.getEnclosingElement();
            if (outerCl == null || outerCl.asType().getKind() != TypeKind.DECLARED){
                return;
            }
            
            outerClass = new NetBeansJavaClass(
                            new FqName(((TypeElement) outerCl).
                                    getQualifiedName().toString()),
                            project);
        }
        
        public JavaClass getOuterClass() {
            return outerClass;
        }
    }
 
    public static class VisibilitySearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private Visibility visibility = null;
        
        public VisibilitySearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement member = (TypeElement) handle.resolve(info);
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
    
    public static class IsAbstractSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isAbstract = false;
        
        public IsAbstractSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement member = (TypeElement) handle.resolve(info);
            isAbstract = NetBeansJavaElementUtil.isAbstract(member.getModifiers());
        }
        
        public boolean isAbstract() {
            return isAbstract;
        }
    }
    
    public static class IsStaticSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isStatic = false;
        
        public IsStaticSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement member = (TypeElement) handle.resolve(info);
            isStatic = NetBeansJavaElementUtil.isStatic(member.getModifiers());
        }
        
        public boolean isStatic() {
            return isStatic;
        }
    }
    
    public static class IsFinalSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isFinal = false;
        
        public IsFinalSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement member = (TypeElement) handle.resolve(info);
            isFinal = NetBeansJavaElementUtil.isFinal(member.getModifiers());
        }
        
        public boolean isFinal() {
            return isFinal;
        }
    }
    
    public static class IsDeprecatedSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private boolean isDeprecated;
        
        public IsDeprecatedSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement element = (TypeElement) handle.resolve(info);
            isDeprecated = info.getElements().isDeprecated(element);
        }
        
        public boolean isDeprecated() {
            return isDeprecated;
        }
    }
    
    public static class ElementKindSearcher implements Task<CompilationController> {

        private final ElementHandle handle;
        private ElementKind kind;
        
        public ElementKindSearcher(ElementHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement element = (TypeElement) handle.resolve(info);
            kind = element.getKind();
        }
        
        public ElementKind getKind() {
            return kind;
        }
    }
    
    public static class ElementHandleSearcher implements Task<CompilationController> {

        private final String fqName;
        private ElementHandle handle;
        
        public ElementHandleSearcher(String fqName) {
            this.fqName = fqName;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            TypeElement element = info.getElements().getTypeElement(fqName);
            handle = ElementHandle.create(element);
        }
        
        public ElementHandle getElementHandle() {
            return handle;
        }
    }
    
}
