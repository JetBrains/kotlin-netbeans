package org.black.kotlin.resolve;

import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import java.util.Set;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.netbeans.api.project.Project;
//import com.intellij.openapi.project.Project;

public class KotlinPackagePartProvider implements PackagePartProvider 
{
    Set<VirtualFile> roots; 
    
    public KotlinPackagePartProvider(Project nbproject, com.intellij.openapi.project.Project ijProject) {
        roots = KotlinEnvironment.getEnvironment(nbproject).getRoots();
    }

    @Override
    public List<String> findPackageParts(String packageName) 
    {
        String pkgNameParts[] = packageName.split(".");
         
//        val mappings = roots.filter {
//            //filter all roots by package path existing
//            pathParts.fold(it) {
//                parent, part ->
//                if (part.isEmpty()) parent
//                else  parent.findChild(part) ?: return@filter false
//            }
//            true
//        }.map {
//            it.findChild("META-INF")
//        }.filterNotNull().flatMap {
//            it.children.filter { it.name.endsWith(ModuleMapping.MAPPING_FILE_EXT) }.toList()
//        }.map {
//            ModuleMapping.create(it.contentsToByteArray())
//        }
//
//        return mappings.map { it.findPackageParts(packageFqName) }.filterNotNull().flatMap { it.parts }.distinct()
//    }
        return Lists.newArrayList(pkgNameParts);
    }
}
    
    
