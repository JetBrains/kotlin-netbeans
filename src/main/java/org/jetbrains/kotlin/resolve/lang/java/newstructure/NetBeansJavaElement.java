package org.jetbrains.kotlin.resolve.lang.java.newstructure;

import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.name.FqName;
import org.netbeans.api.project.Project;

public class NetBeansJavaElement implements JavaElement {
    
    private final Project project;
    private final FqName fqName;
    
    public NetBeansJavaElement(FqName fqName, Project project) {
        this.fqName = fqName;
        this.project = project;
    }
    
    public Project getProject() {
        return project;
    }
    
    public FqName getFqName() {
        return fqName;
    }
    
    @Override
    public int hashCode() {
        return project.hashCode() + fqName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        return obj instanceof NetBeansJavaElement 
                && project.equals(((NetBeansJavaElement) obj).getProject())
                && fqName.equals(((NetBeansJavaElement) obj).getFqName());
    }
    
}
