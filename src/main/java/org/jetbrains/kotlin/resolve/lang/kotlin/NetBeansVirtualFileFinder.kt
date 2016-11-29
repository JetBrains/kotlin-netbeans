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
package org.jetbrains.kotlin.resolve.lang.kotlin

import org.netbeans.api.project.Project
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClassFinder
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass
import org.jetbrains.kotlin.builtins.BuiltInSerializerProtocol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.cli.jvm.index.JavaRoot
import org.jetbrains.kotlin.cli.jvm.index.JvmDependenciesIndex
import java.io.InputStream
import com.intellij.openapi.vfs.VirtualFile
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassifier
import org.jetbrains.kotlin.resolve.lang.java.computeClassId
import javax.lang.model.element.TypeElement

class NetBeansVirtualFileFinder(private val project: Project,
                                private val scope: GlobalSearchScope) : VirtualFileKotlinClassFinder() {

    override fun findBuiltInsData(packageFqName: FqName): InputStream? {
        val fileName = BuiltInSerializerProtocol.getBuiltInsFileName(packageFqName)

        val classId = ClassId(packageFqName, Name.special("<builtins-metadata>"))
        val index = KotlinEnvironment.getEnvironment(project).index

        return index.findClass(classId, acceptedRootTypes = JavaRoot.OnlyBinary) { dir, rootType ->
            dir.findChild(fileName)?.check(VirtualFile::isValid)
        }?.check { it in scope }?.inputStream
    }

    private fun String.isClassFileName(): Boolean {
        val suffixClass = ".class".toCharArray()
        val suffixClass2 = ".CLASS".toCharArray()
        val suffixLength = suffixClass.size
        if (length < suffixLength) return false

        var i = 0
        while (i < suffixLength) {
            val c = this[length - i - 1]
            val suffixIndex = suffixLength - i - 1
            if (c != suffixClass[suffixIndex] && c != suffixClass2[suffixIndex]) return false
            i++
        }
        return true
    }

    private fun getJarPath(file: FileObject) = file.fileSystem.displayName

    private fun JavaClass.classFileName(): String {
        return if (outerClass == null) name.asString() else {
                (outerClass as JavaClass).classFileName() + "$" + name.asString()
            }
    }

    override fun findVirtualFileWithHeader(classId: ClassId): VirtualFile? {
        val proxy = KotlinProjectHelper.INSTANCE.getFullClassPath(project)
        val rPath = if (classId.isNestedClass) {
            val className = classId.shortClassName.asString()
            val fqName = classId.asSingleFqName().asString()
            val rightPath = StringBuilder(fqName.substring(0, fqName.length - className.length - 1).replace(".", "/"))
            rightPath.append("$").append(className).append(".class")
            rightPath.toString()
        } else classId.asSingleFqName().asString().replace(".", "/") + ".class"
        
        val resource = proxy.findResource(rPath)
        if (resource == null) {
            if (rPath.isClassFileName()) {
                return KotlinEnvironment.getEnvironment(project).getVirtualFile(rPath)
            } else throw IllegalArgumentException("Virtual file not found for " + rPath)
        }
        
        val path = resource.toURL().path
        if (path.contains("!/")) {
            val pathToJar = getJarPath(resource)
            val splittedPath = path.split("!/")
            if (splittedPath.size < 2) return null
            
            return KotlinEnvironment.getEnvironment(project).getVirtualFileInJar(pathToJar, splittedPath[1])
        }
        
        return if (path.isClassFileName()) {
            KotlinEnvironment.getEnvironment(project).getVirtualFile(rPath)
        } else throw IllegalArgumentException("Virtual file not found for " + rPath)
    }
    
    override fun findKotlinClass(javaClass: JavaClass): KotlinJvmBinaryClass? {
        if (javaClass.fqName == null) return null
        
        val classId = (javaClass as NetBeansJavaClassifier<TypeElement>).elementHandle.computeClassId(project) ?: return null
        var file: VirtualFile? = findVirtualFileWithHeader(classId) ?: return null
        
        if (javaClass.outerClass != null) {
            val classFileName = javaClass.classFileName() + ".class"
            file = file!!.parent.findChild(classFileName)
            if (file != null) throw IllegalStateException("Virtual file not found")
        }
        return KotlinBinaryClassCache.getKotlinBinaryClass(file!!)
    }   
}

class NetBeansVirtualFileFinderFactory(private val project: Project) : JvmVirtualFileFinderFactory {
    override fun create(scope: GlobalSearchScope) = NetBeansVirtualFileFinder(project, scope)
}

fun <T : Any> T.check(predicate: (T) -> Boolean): T? = if (predicate(this)) this else null