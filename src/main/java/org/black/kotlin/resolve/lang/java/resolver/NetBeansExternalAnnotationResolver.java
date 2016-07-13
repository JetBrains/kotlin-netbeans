package org.black.kotlin.resolve.lang.java.resolver;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.components.ExternalAnnotationResolver;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.name.FqName;

/**
 *
 * @author Александр
 */
public class NetBeansExternalAnnotationResolver implements ExternalAnnotationResolver {

    @Override
    @Nullable
    public JavaAnnotation findExternalAnnotation(JavaAnnotationOwner jao, FqName fqname) {
        return null;
    }
    
}
