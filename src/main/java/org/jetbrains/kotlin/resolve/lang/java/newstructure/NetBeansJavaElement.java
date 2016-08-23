package org.jetbrains.kotlin.resolve.lang.java.newstructure;

import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.name.FqName;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

public class NetBeansJavaElement implements JavaElement {
    
    private final Project project;
    private final FqName fqName;
    private ElementHandle handle = null;
    
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
    
    public void setHandle(ElementHandle handle) {
        this.handle = handle;
    }
    
    public ElementHandle getHandle() {
        return handle;
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
