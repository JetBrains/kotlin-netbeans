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
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class ClassSearchers {

    public static class InnerClassesSearcher implements Task<CompilationController> {

        private final String fqName;
        private final Collection<JavaClass> innerClasses = Lists.newArrayList();;
        private final Project project;
        
        public InnerClassesSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement elem = info.getElements().getTypeElement(fqName);
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

        private final String fqName;
        private JavaClass outerClass = null;
        private final Project project;
        
        public OuterClassSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement elem = info.getElements().getTypeElement(fqName);
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

        private final String fqName;
        private Visibility visibility = null;
        private final Project project;
        
        public VisibilitySearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement member = info.getElements().getTypeElement(fqName);
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

        private final String fqName;
        private boolean isAbstract = false;
        private final Project project;
        
        public IsAbstractSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement member = info.getElements().getTypeElement(fqName);
            isAbstract = NetBeansJavaElementUtil.isAbstract(member.getModifiers());
        }
        
        public boolean isAbstract() {
            return isAbstract;
        }
    }
    
    public static class IsStaticSearcher implements Task<CompilationController> {

        private final String fqName;
        private boolean isStatic = false;
        private final Project project;
        
        public IsStaticSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement member = info.getElements().getTypeElement(fqName);
            isStatic = NetBeansJavaElementUtil.isStatic(member.getModifiers());
        }
        
        public boolean isStatic() {
            return isStatic;
        }
    }
    
    public static class IsFinalSearcher implements Task<CompilationController> {

        private final String fqName;
        private boolean isFinal = false;
        private final Project project;
        
        public IsFinalSearcher(String fqName, Project project) {
            this.fqName = fqName;
            this.project = project;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement member = info.getElements().getTypeElement(fqName);
            isFinal = NetBeansJavaElementUtil.isFinal(member.getModifiers());
        }
        
        public boolean isFinal() {
            return isFinal;
        }
    }
    
    public static class IsDeprecatedSearcher implements Task<CompilationController> {

        private final String fqName;
        private boolean isDeprecated;
        
        public IsDeprecatedSearcher(String fqName) {
            this.fqName = fqName;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement element = info.getElements().getTypeElement(fqName);
            isDeprecated = info.getElements().isDeprecated(element);
        }
        
        public boolean isDeprecated() {
            return isDeprecated;
        }
    }
    
    public static class ElementKindSearcher implements Task<CompilationController> {

        private final String fqName;
        private ElementKind kind;
        
        public ElementKindSearcher(String fqName) {
            this.fqName = fqName;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            TypeElement element = info.getElements().getTypeElement(fqName);
            kind = element.getKind();
        }
        
        public ElementKind getKind() {
            return kind;
        }
    }
    
}
