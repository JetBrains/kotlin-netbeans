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
package org.jetbrains.kotlin.filesystem

import kotlin.Pair
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.ClassNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode

object JavaStubGenerator {

    fun generate(byteCode: ByteArray): Pair<ClassNode?, String> {
        val javaStub = StringBuilder()
        
        val classNode = ClassNode()
        try {
            ClassReader(byteCode).accept(classNode, 0)
        } catch(ex: Exception) {
            return Pair(null, "")
        }
        javaStub.append(classNode.packageString)
        javaStub.append(classNode.classDeclaration())
        javaStub.append(classNode.methods())
        
        javaStub.append("}")
        return Pair(classNode, javaStub.toString())
    }
    
    val ClassNode.packageString: String
        get() = "package ${name.substringBeforeLast("/").replace("/", ".")};\n"
    
    val ClassNode.className: String
        get() = name.substringAfterLast("/")
    
    private fun ClassNode.classDeclaration(): String {
        val declaration = StringBuilder()
        
        declaration.append(getAccess(access)).append(" ")
        declaration.append(getFinal(access)).append(" ")
        declaration.append(getStatic(access)).append(" ")
        declaration.append(getAbstract(access)).append(" ")
        declaration.append(getClassType(access)).append(" ")
        declaration.append(className).append("{\n")
        
        return declaration.toString();
    }
    
    private fun ClassNode.methods(): String {
        val methodsStub = StringBuilder()
        
        methods.forEach {
            methodsStub.append(it.getString(className))
        }
        
        return methodsStub.toString()
    }
    
    private fun MethodNode.getString(className: String): String {
        val method = StringBuilder()
        
        method.append(getAccess(access)).append(" ")
        method.append(getFinal(access)).append(" ")
        method.append(getStatic(access)).append(" ")
        method.append(getAbstract(access)).append(" ")
        
        val indexOfRightBracket = desc.indexOf(")")
        val methodName = if (name == "<init>") className else name
        if (indexOfRightBracket == -1) return ""
        
        val returnTypeSig = desc.substring(indexOfRightBracket + 1)
        val returnType = returnTypeSig.replace("/", ".").toType().replace(";", "")
        
        if (name != "<init>") method.append(returnType).append(" ")
        
        method.append(methodName).append("(")
        
        val argsSig = desc.substring(1, indexOfRightBracket)
        if (argsSig.isEmpty()) return method.append("){}").toString()
        
        val args = argsSig.split(";")
        args.forEachIndexed { i, it -> 
            val argument = "${args[i].replace("/", ".").toType()} a$i"
            val void = argument == "void a$i"
            if (!void) {
                method.append(argument)
                if (i != args.size - 1) method.append(",")
            }
        }
        if (method.endsWith(",")) method.deleteCharAt(method.length - 1)
        method.append("){}\n")
        
        return method.toString()
    }
    
    private fun getClassType(access: Int) = when {
        access.contains(Opcodes.ACC_INTERFACE) -> "interface"
        access.contains(Opcodes.ACC_ENUM) -> "enum"
        else -> "class"
    }
    
    private fun getAccess(access: Int) = when {
        access.contains(Opcodes.ACC_PUBLIC) -> "public"
        access.contains(Opcodes.ACC_PRIVATE) -> "private"
        access.contains(Opcodes.ACC_PROTECTED) -> "protected"
        else -> ""
    }
   
    private fun getFinal(access: Int) = if (access.contains(Opcodes.ACC_FINAL)) "final" else ""
    
    private fun getStatic(access: Int) = if (access.contains(Opcodes.ACC_STATIC)) "static" else ""
    
    private fun getAbstract(access: Int) = if (access.contains(Opcodes.ACC_ABSTRACT)) "abstract" else ""
    
    private fun Int.contains(opcode: Int) = (this and opcode) == opcode
    
    private fun String.toType() = when {
        this.startsWith("Z") -> "boolean"
        this.startsWith("V") -> "void"
        this.startsWith("B") -> "byte"
        this.startsWith("C") -> "char"
        this.startsWith("S") -> "short"
        this.startsWith("I") -> "int"
        this.startsWith("J") -> "long"
        this.startsWith("F") -> "float"
        this.startsWith("D") -> "double"
        this.startsWith("L") -> this.substring(1)
        this.startsWith("[") -> this.substring(1) + "[]"
        else -> "void"
    }
    
}