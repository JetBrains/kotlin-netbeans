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
import kotlin.Pair;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.utils.ProjectUtils;
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
            
            for (byte[] byteCode : codeList) {
                Pair<ClassNode, String> nameAndStub = JavaStubGenerator.INSTANCE.generate(byteCode);
                if (nameAndStub.getFirst() == null) continue;
                
                String code = nameAndStub.getSecond();

                String className = JavaStubGenerator.INSTANCE.getClassName(nameAndStub.getFirst());
                String packageName = nameAndStub.getFirst().name
                        .substring(0, nameAndStub.getFirst().name.lastIndexOf("/")).replace("/", ".");
                
                
                
                result.add(normalizedFile, packageName, className, code);
            }
        }
    }
    
    private List<byte[]> getByteCode(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        Project project = ProjectUtils.getKotlinProjectForFileObject(fo);
        KtFile ktFile = ProjectUtils.getKtFile(fo);
        AnalysisResultWithProvider result = KotlinAnalyzer.analyzeFile(project, ktFile);
        
        return KotlinLightClassGeneration.INSTANCE.getByteCode(fo, project, result.getAnalysisResult());
    }
    
}
