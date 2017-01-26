package org.jetbrains.kotlin.resolve

import com.intellij.util.SmartList
import java.io.EOFException
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.descriptors.PackagePartProvider
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.jetbrains.kotlin.cli.jvm.compiler.JavaRoot

// Mostly copied from org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider
class KotlinPackagePartProvider(val project: Project) : PackagePartProvider {
    
    private val notLoadedRoots by lazy(LazyThreadSafetyMode.NONE) {
        KotlinEnvironment.getEnvironment(project)
                .roots
                .map { it.file }
                .filter { it.findChild("META-INF") != null }
                .toMutableList()
    }
    
    private val loadedModules: MutableList<ModuleMapping> = SmartList()
    
//    @Synchronized
    override fun findPackageParts(packageFqName: String): List<String> {
        processNotLoadedRelevantRoots(packageFqName)

        return loadedModules.flatMap { it.findPackageParts(packageFqName)?.parts ?: emptySet<String>() }.distinct()
    }
    
    private fun processNotLoadedRelevantRoots(packageFqName: String) {
        if (notLoadedRoots.isEmpty()) return

        val pathParts = packageFqName.split('.')

        val relevantRoots = notLoadedRoots.filter {
            //filter all roots by package path existing
            pathParts.fold(it) {
                parent, part ->
                if (part.isEmpty()) parent
                else parent.findChild(part) ?: return@filter false
            }
            true
        }
        notLoadedRoots.removeAll(relevantRoots)

        loadedModules.addAll(relevantRoots.mapNotNull {
            it.findChild("META-INF")
        }.flatMap {
            it.children.filter { it.name.endsWith(ModuleMapping.MAPPING_FILE_EXT) }
        }.map { file ->
            try {
                ModuleMapping.create(file.contentsToByteArray(), file.toString())
            }
            catch (e: EOFException) {
                throw RuntimeException("Error on reading package parts for '$packageFqName' package in '$file', roots: $notLoadedRoots", e)
            }
        })
    }
    
}