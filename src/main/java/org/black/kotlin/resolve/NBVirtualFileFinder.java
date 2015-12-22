package org.black.kotlin.resolve;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinder;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory;
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClassFinder;
import org.jetbrains.kotlin.name.ClassId;
import org.netbeans.api.project.Project;

/**
 *
 * @author polina
 */
public class NBVirtualFileFinder extends VirtualFileKotlinClassFinder implements JvmVirtualFileFinderFactory{

    Project nbProject;
    public NBVirtualFileFinder(Project project) 
    {
         this.nbProject = project;
    }
     
    @Override
    public VirtualFile findVirtualFileWithHeader(ClassId ci) {
        
//        String parts[] = ci.getPackageFqName().asString().split(".");
//        
//        Repository r = TopManager.getDefault().getRepository(); 
//        try {
//            DataObject d = DataObject.find(r.findResource("/"
//                + ci.getPackageFqName().asString().replace('.', '/') 
//                + "/"
//                + ci.getRelativeClassName().asString()
//                + ".class"));
//        }
//        catch (DataObjectNotFoundException donfe)
//        {
//            return null;
//            
//        }
        return KotlinEnvironment.getEnvironment(nbProject).getVirtualFile("/"
                + ci.getPackageFqName().asString().replace('.', '/') 
                + "/"
                + ci.getRelativeClassName().asString()
                + ".class");
    }

    @Override
    public JvmVirtualFileFinder create(GlobalSearchScope gss) {
        return new NBVirtualFileFinder(nbProject); //To change body of generated methods, choose Tools | Templates.
    }
}
