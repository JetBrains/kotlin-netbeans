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
    
    public NetBeansJavaClass(FqName fqName, Project project) {
        super(fqName, project);
        setHandle(NBElementUtils.getElementHandle(fqName, project));
    }
    
    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        return NBElementUtils.getSuperTypes(getHandle(), getProject());
    }

    @Override
    public Collection<JavaClass> getInnerClasses() {
        return NBElementUtils.getInnerClasses(getHandle(), getProject());
    }

    @Override
    public JavaClass getOuterClass() {
        return NBElementUtils.getOuterClass(getHandle(), getProject());
    }

    @Override
    public boolean isInterface() {
        return getHandle().getKind() == ElementKind.INTERFACE;
    }

    @Override
    public boolean isAnnotationType() {
        return getHandle().getKind() == ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public boolean isEnum() {
        return getHandle().getKind() == ElementKind.ENUM;
    }

    @Override
    public boolean isKotlinLightClass() {
        return false;
    }

    @Override
    public Collection<JavaMethod> getMethods() {
        return NBElementUtils.getMethods(getHandle(), this, getProject());
    }

    @Override
    public Collection<JavaField> getFields() {
        return NBElementUtils.getFields(getHandle(), this, getProject());
    }

    @Override
    public Collection<JavaConstructor> getConstructors() {
        return NBElementUtils.getConstructors(getHandle(), this, getProject());
    }

    @Override
    public boolean isDeprecatedInJavaDoc() {
        return NBElementUtils.isDeprecated(getHandle(), getProject());
    }

    @Override
    public boolean isAbstract() {
        return NBElementUtils.isAbstract(getHandle(), getProject());
    }

    @Override
    public boolean isStatic() {
        return NBElementUtils.isStatic(getHandle(), getProject());
    }

    @Override
    public boolean isFinal() {
        return NBElementUtils.isFinal(getHandle(), getProject());
    }

    @Override
    public Visibility getVisibility() {
        return NBElementUtils.getVisibility(getHandle(), getProject());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        return NBElementUtils.getTypeParametersForTypeElement(getHandle(), getProject());
    }
    
    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return NBElementUtils.getAnnotationsForClass(getHandle(), getProject());
    }

    @Override
    public JavaAnnotation findAnnotation(FqName fqName) {
        return NBElementUtils.findAnnotationForClass(getHandle(), getProject(), fqName);
    }
    
}
