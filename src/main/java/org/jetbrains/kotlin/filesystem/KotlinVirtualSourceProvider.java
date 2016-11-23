/** *****************************************************************************
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
 ****************************************************************************** */
package org.jetbrains.kotlin.filesystem;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.org.objectweb.asm.ClassReader;
//import org.jetbrains.org.objectweb.asm.ClassVisitor;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import org.jetbrains.org.objectweb.asm.tree.ClassNode;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = VirtualSourceProvider.class)
public class KotlinVirtualSourceProvider implements VirtualSourceProvider {

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("kt");
    }

    @Override
    public boolean index() {
        return true;
    }

    @Override
    public void translate(Iterable<File> files, File sourceRoot, Result result) {
        for (File file : files) {
            List<byte[]> codeList = getByteCode(file);
            if (codeList.isEmpty()) continue;
            
            File normalizedFile = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(normalizedFile);
            if (fo == null) continue;
            
            byte[] byteCode = codeList.get(0);
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(byteCode);
            reader.accept(classNode, 0);
            
            String className = classNode.name.substring(classNode.name.lastIndexOf("/") + 1);
            String packageName = classNode.name.substring(0, classNode.name.lastIndexOf("/")).replace("/", ".");
            
            String code = "package " + packageName + "; class " + className + "{}";
            
            KotlinLogger.INSTANCE.logInfo(code);
            result.add(normalizedFile, packageName, className, code);
        }
    }
    
    private List<byte[]> getByteCode(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        Project project = ProjectUtils.getKotlinProjectForFileObject(fo);
        KtFile ktFile = ProjectUtils.getKtFile(fo);
        AnalysisResultWithProvider result = KotlinAnalyzer.analyzeFile(project, ktFile);
        
        return KotlinLightClassGeneration.INSTANCE.getByteCode(fo, project, result.getAnalysisResult());
    }
    
//    private String generateVirtualJavaSource(byte[] byteCode) {
//        StringBuilder code = new StringBuilder();
//        
//        ClassNode classNode = new ClassNode();
//        new ClassReader(byteCode).accept(classNode, 0);
//        
//        
//        String name = classNode.name;
//        code.append("");
//    }
    
}
