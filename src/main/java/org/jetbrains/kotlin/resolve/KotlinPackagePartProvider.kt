package org.jetbrains.kotlin.resolve

import org.netbeans.api.project.Project
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.descriptors.PackagePartProvider
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.jetbrains.kotlin.cli.jvm.index.JavaRoot
import org.jetbrains.kotlin.utils.SmartList

class KotlinPackagePartProvider(val project: Project) : PackagePartProvider {
    private val notLoadedRoots by lazy(LazyThreadSafetyMode.NONE) { 
        KotlinEnvironment.getEnvironment(project).roots
                .map { it.file }
                .filter { it.findChild("META-INF") != null }
                .toMutableList()
    }
    
    private val loadedModules: MutableList<ModuleMapping> = SmartList()
    
    private fun processNotLoadedRelevantRoots(packageFqName: String) {
        if (notLoadedRoots.isEmpty()) return
        
        val pathParts = packageFqName.split('.')
        val relevantRoots = notLoadedRoots.filter {
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
        }.map {
            ModuleMapping.create(it.contentsToByteArray(), it.toString())
        })
    }
    
    @Synchronized
    override fun findPackageParts(packageFqName: String): List<String> {
        processNotLoadedRelevantRoots(packageFqName)
        
        return loadedModules.flatMap { it.findPackageParts(packageFqName)?.parts ?: emptySet<String>() }.distinct()
    }
}