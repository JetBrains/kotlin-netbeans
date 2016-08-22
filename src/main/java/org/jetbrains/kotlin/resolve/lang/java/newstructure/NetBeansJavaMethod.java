package org.jetbrains.kotlin.resolve.lang.java.newstructure;

import java.util.List;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils;
import org.netbeans.api.project.Project;

public class NetBeansJavaMethod extends NetBeansJavaMember implements JavaMethod {

    
    public NetBeansJavaMethod(FqName fqName, Project project, JavaClass containingClass) {
        super(fqName, project, containingClass);
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaType getReturnType() {
        return NBElementUtils.getReturnType(getHandle(), getProject());
    }

    @Override
    public boolean getHasAnnotationParameterDefaultValue() {
        return NBElementUtils.hasDefaultValue(getHandle(), getProject());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
