package org.black.kotlin.resolve

import org.black.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.descriptors.PackagePartProvider
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.netbeans.api.project.Project
//import com.intellij.openapi.project.Project;

public class KotlinPackagePartProviderKT(nbProject : Project, 
                                    ijProject:com.intellij.openapi.project.Project) : PackagePartProvider {
    val roots = KotlinEnvironment.getEnvironment(nbProject).getRoots()
    
    override fun findPackageParts(packageFqName: String): List<String> {
        val pathParts = packageFqName.split('.')
        val mappings = roots.filter {
            //filter all roots by package path existing
            pathParts.fold(it) {
                parent, part ->
                if (part.isEmpty()) parent
                else  parent.findChild(part) ?: return@filter false
            }
            true
        }.map {
            it.findChild("META-INF")
        }.filterNotNull().flatMap {
            it.children.filter { it.name.endsWith(ModuleMapping.MAPPING_FILE_EXT) }.toList()
        }.map {
            ModuleMapping.create(it.contentsToByteArray())
        }

        return mappings.map { it.findPackageParts(packageFqName) }.filterNotNull().flatMap { it.parts }.distinct()
    }
}