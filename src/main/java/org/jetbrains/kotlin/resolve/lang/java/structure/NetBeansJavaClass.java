/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.resolve.lang.java.structure;

import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;
import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.classifierTypes;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClass extends NetBeansJavaClassifier<TypeElement> implements JavaClass {
    
    private final Name name;
    private Collection<JavaClass> innerClasses = null;
    private JavaClass outerClass = null;
    private Collection<JavaClassifierType> superTypes = null;
    private Collection<JavaMethod> methods = null;
    private Collection<JavaField> fields = null;
    private Collection<JavaConstructor> constructors = null;
    private List<JavaTypeParameter> typeParameters = null;
    private final Set<Modifier> modifiers;
    private final Visibility visibility;
    private TypeElement elem;
    
    public NetBeansJavaClass(TypeElement javaElement){
        super(javaElement);
        name = SpecialNames.safeIdentifier(javaElement.getSimpleName().toString());
        modifiers = getModifiers(javaElement);
        visibility = NetBeansJavaElementUtil.getVisibility(javaElement);
        elem = javaElement;
    }

    @Override
    public Name getName() {
        return name;
    }

    private boolean allInitialized() {
        return outerClass != null && superTypes != null 
                && innerClasses != null && typeParameters != null
                && methods != null && fields != null && constructors != null;
    }
    
    private Collection<JavaClass> getInnerClasses(TypeElement el) {
        List<? extends Element> enclosedElements = el.getEnclosedElements();
        List<JavaClass> inClasses = Lists.newArrayList();
        for (Element element : enclosedElements){
            if (element.asType().getKind() == TypeKind.DECLARED && element instanceof TypeElement){
                inClasses.add(new NetBeansJavaClass((TypeElement) element));
            }
        }
        return inClasses;
    }
    
    @Override
    public Collection<JavaClass> getInnerClasses() {
        if (innerClasses == null) {
            innerClasses = getInnerClasses(elem);
            if (allInitialized()) {
                elem = null;
            }
        }
        return innerClasses;
    }

    @Override
    public FqName getFqName() {
        return new FqName(getBinding().getQualifiedName());
    }

    @Override
    public boolean isInterface() {
        return getBinding().getKind() == ElementKind.INTERFACE;
    }

    @Override
    public boolean isAnnotationType() {
        return getBinding().getKind() == ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public boolean isEnum() {
        return getBinding().getKind() == ElementKind.ENUM;
    }

    private JavaClass getOuterClass(TypeElement el) {
        Element outClass = el.getEnclosingElement();
        if (outClass == null || outClass.asType().getKind() != TypeKind.DECLARED){
            return null;
        }
        return new NetBeansJavaClass((TypeElement) outClass);
    }
    
    @Override
    public JavaClass getOuterClass() {
        if (outerClass == null) {
            outerClass = getOuterClass(elem);
            if (allInitialized()) {
                elem = null;
            }
        }
        return outerClass;
    }
    
    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        if (superTypes == null) {
            superTypes = classifierTypes(NetBeansJavaElementUtil.getSuperTypesWithObject(elem));
            if (allInitialized()) {
                elem = null;
            }
        }
        return superTypes;
    }

    private Collection<JavaMethod> getMethods(TypeElement el) {
        List<? extends Element> declaredElements = el.getEnclosedElements();
        List<JavaMethod> javaMethods = Lists.newArrayList();
        
        for (Element element : declaredElements){
            if (element.getKind() == ElementKind.METHOD){
                javaMethods.add(new NetBeansJavaMethod((ExecutableElement) element));
            }
        }
        
        return javaMethods;
    }
    
    @Override
    public Collection<JavaMethod> getMethods() {
        if (methods == null) {
            methods = getMethods(elem);
            if (allInitialized()) {
                elem = null;
            }
        }
        return methods;
    }

    private Collection<JavaField> getFields(TypeElement el) {
        List<? extends Element> declaredElements = el.getEnclosedElements();
        List<JavaField> javaFields = Lists.newArrayList();
        
        for (Element element : declaredElements){
            if (element.getKind().isField()){
                String name = element.getSimpleName().toString();
                if (Name.isValidIdentifier(name)){
                    javaFields.add(new NetBeansJavaField((VariableElement) element));
                }
            }
        }
        
        return javaFields;
    }
    
    @Override
    public Collection<JavaField> getFields() {
        if (fields == null) {
            fields = getFields(elem);
            if (allInitialized()) {
                elem = null;
            }
        }
        return fields;
    }
    
    private Collection<JavaConstructor> getConstructors(TypeElement el) {
        List<? extends Element> declaredElements = el.getEnclosedElements();
        List<JavaConstructor> javaConstructors = Lists.newArrayList();
        
        for (Element element : declaredElements){
            if (element.getKind().equals(ElementKind.CONSTRUCTOR)){
                javaConstructors.add(new NetBeansJavaConstructor((ExecutableElement) element));
            }
        }
        return javaConstructors;
    }
    
    @Override
    public Collection<JavaConstructor> getConstructors() {
        if (constructors == null) {
            constructors = getConstructors(elem);
            if (allInitialized()) {
                elem = null;
            }
        }
        return constructors;
    }

    private List<JavaTypeParameter> getTypeParameters(TypeElement el) {
        List<? extends TypeParameterElement> typeParams = el.getTypeParameters();
        return typeParameters(typeParams.toArray(new TypeParameterElement[typeParams.size()]));
    }
    
    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        if (typeParameters == null) {
            typeParameters = getTypeParameters(elem);
            if (allInitialized()) {
                elem = null;
            }
        }
        return typeParameters;
    }

    private Set<Modifier> getModifiers(TypeElement el) {
        return el.getModifiers();
    }
    
    @Override
    public boolean isAbstract() {
        return NetBeansJavaElementUtil.isAbstract(modifiers);
    }

    @Override
    public boolean isStatic() {
        return NetBeansJavaElementUtil.isStatic(modifiers);
    }

    @Override
    public boolean isFinal() {
        return NetBeansJavaElementUtil.isFinal(modifiers);
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }
    
    @Override
    public boolean isKotlinLightClass() {
        return false;// temporary
    }
    
}
