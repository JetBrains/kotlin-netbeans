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
import org.jetbrains.org.objectweb.asm.ClassReader;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import org.jetbrains.org.objectweb.asm.tree.ClassNode;
import org.jetbrains.org.objectweb.asm.tree.MethodNode;
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
     
            Pair<String, String> nameAndStub = JavaStubGenerator.generate(byteCode);
            String classNodeName = nameAndStub.getSecond();
            String code = nameAndStub.getFirst();
            
            String className = classNodeName.substring(classNodeName.lastIndexOf("/") + 1);
            String packageName = classNodeName.substring(0, classNodeName.lastIndexOf("/")).replace("/", ".");
            
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
    
    private static class JavaStubGenerator {
        
        static Pair<String, String> generate(byte[] byteCode) {
            StringBuilder stubBuilder = new StringBuilder();
            
            ClassNode classNode = new ClassNode();
            new ClassReader(byteCode).accept(classNode, 0);
            
            String className = classNode.name.substring(classNode.name.lastIndexOf("/") + 1);
            String packageName = classNode.name.substring(0, classNode.name.lastIndexOf("/")).replace("/", ".");
            stubBuilder.append("package ").append(packageName).append("; class ").append(className).append("{");
            
            stubBuilder.append(getMethods(classNode));
            
            stubBuilder.append("}");
            return new Pair(stubBuilder.toString(), classNode.name);
        }
        
        private static String getMethods(ClassNode classNode) {
            StringBuilder methodsStub = new StringBuilder();
            for (MethodNode method : classNode.methods) {
                if (method.name.equals("<init>")) continue;
                String parsedMethodSignature = parseMethodSignature(method.desc, method.name);
                methodsStub.append(parsedMethodSignature);
//                KotlinLogger.INSTANCE.logInfo(method.desc);
                KotlinLogger.INSTANCE.logInfo(parsedMethodSignature);
            }
            return methodsStub.toString();
        }
        
        private static String parseMethodSignature(String methodSignature, String methodName) {
            StringBuilder method = new StringBuilder();
            method.append("public ");
            
            int indexOfRBracket = methodSignature.indexOf(")");
            if (indexOfRBracket == -1) return "";
            
            String returnTypeSig = methodSignature.substring(indexOfRBracket + 1);
            String returnType = sigTypeToFQNType(returnTypeSig.replace("/", ".")).replace(";", "");
            
            method.append(returnType).append(" ").append(methodName).append("(");
            
            String argsSig = methodSignature.substring(1, indexOfRBracket);
            if (argsSig.isEmpty()) {
                method.append("){};");
            } else {
                String[] args = argsSig.split(";");
                for (int i = 0; i < args.length - 1; i++) {
                    String argument = sigTypeToFQNType(args[i].replace("/", ".")) + " a" + i;
                    method.append(argument).append(",");
                }
                String argument = sigTypeToFQNType(args[args.length - 1].replace("/", ".")) + " a" + (args.length - 1);
                method.append(argument).append("){}");
            }
            return method.toString();
        }
        
        private static String sigTypeToFQNType(String sigType) {
            if (sigType.startsWith("Z")) return "boolean";
            else if (sigType.startsWith("V")) return "void";
            else if (sigType.startsWith("B")) return "byte";
            else if (sigType.startsWith("C")) return "char";
            else if (sigType.startsWith("S")) return "short";
            else if (sigType.startsWith("I")) return "int";
            else if (sigType.startsWith("J")) return "long";
            else if (sigType.startsWith("F")) return "float";
            else if (sigType.startsWith("D")) return "double";
            else if (sigType.startsWith("L")) return sigType.substring(1);
            else if (sigType.startsWith("[")) return sigType.substring(1) + "[]";
            else return "void";
        }
        
    }
    
}
