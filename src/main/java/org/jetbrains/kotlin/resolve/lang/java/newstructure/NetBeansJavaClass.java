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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

public class NetBeansJavaClass extends NetBeansJavaClassifier implements JavaClass {

    private final ElementHandle handle;
    
    public NetBeansJavaClass(FqName fqName, Project project) {
        super(fqName, project);
        handle = NBElementUtils.getElementHandle(fqName, project);
    }
    
    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        return NBElementUtils.getSuperTypes(handle, getProject());
    }

    @Override
    public Collection<JavaClass> getInnerClasses() {
        return NBElementUtils.getInnerClasses(handle, getProject());
    }

    @Override
    public JavaClass getOuterClass() {
        return NBElementUtils.getOuterClass(handle, getProject());
    }

    @Override
    public boolean isInterface() {
        return handle.getKind() == ElementKind.INTERFACE;
    }

    @Override
    public boolean isAnnotationType() {
        return handle.getKind() == ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public boolean isEnum() {
        return handle.getKind() == ElementKind.ENUM;
    }

    @Override
    public boolean isKotlinLightClass() {
        return false;
    }

    @Override
    public Collection<JavaMethod> getMethods() {
        return NBElementUtils.getMethods(handle, this, getProject());
    }

    @Override
    public Collection<JavaField> getFields() {
        return NBElementUtils.getFields(handle, this, getProject());
    }

    @Override
    public Collection<JavaConstructor> getConstructors() {
        return NBElementUtils.getConstructors(handle, this, getProject());
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return NBElementUtils.isDeprecated(handle, getProject());
    }

    @Override
    public boolean isAbstract() {
        return NBElementUtils.isAbstract(handle, getProject());
    }

    @Override
    public boolean isStatic() {
        return NBElementUtils.isStatic(handle, getProject());
    }

    @Override
    public boolean isFinal() {
        return NBElementUtils.isFinal(handle, getProject());
    }

    @Override
    public Visibility getVisibility() {
        return NBElementUtils.getVisibility(handle, getProject());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        return NBElementUtils.getTypeParametersForTypeElement(handle, getProject());
    }
    
    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return NBElementUtils.getAnnotationsForClass(handle, getProject());
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        return NBElementUtils.findAnnotationForClass(handle, getProject(), fqName);
    }
    
}
