package org.jetbrains.kotlin.resolve.lang.java.newstructure;

import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ElementKind;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils;
import org.netbeans.api.project.Project;

public class NetBeansJavaClass extends NetBeansJavaClassifier implements JavaClass {

    private final ElementKind kind;
    
    public NetBeansJavaClass(FqName fqName, Project project) {
        super(fqName, project);
        kind = NBElementUtils.getElementKind(fqName, project);
    }
    
    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaClass> getInnerClasses() {
        return NBElementUtils.getInnerClasses(getFqName(), getProject());
    }

    @Override
    public JavaClass getOuterClass() {
        return NBElementUtils.getOuterClass(getFqName(), getProject());
    }

    @Override
    public boolean isInterface() {
        return kind == ElementKind.INTERFACE;
    }

    @Override
    public boolean isAnnotationType() {
        return kind == ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public boolean isEnum() {
        return kind == ElementKind.ENUM;
    }

    @Override
    public boolean isKotlinLightClass() {
        return false;
    }

    @Override
    public Collection<JavaMethod> getMethods() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaField> getFields() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaConstructor> getConstructors() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return NBElementUtils.isDeprecated(getFqName(), getProject());
    }

    @Override
    public boolean isAbstract() {
        return NBElementUtils.isAbstract(getFqName(), getProject());
    }

    @Override
    public boolean isStatic() {
        return NBElementUtils.isStatic(getFqName(), getProject());
    }

    @Override
    public boolean isFinal() {
        return NBElementUtils.isFinal(getFqName(), getProject());
    }

    @Override
    public Visibility getVisibility() {
        return NBElementUtils.getVisibility(getFqName(), getProject());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
