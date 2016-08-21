package org.jetbrains.kotlin.resolve.lang.java;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.newstructure.NetBeansJavaClass;
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
    
}
