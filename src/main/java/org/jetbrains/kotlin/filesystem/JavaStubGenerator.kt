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
import org.jetbrains.org.objectweb.asm.tree.FieldNode

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
        javaStub.append(classNode.fields())
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
        
        val classType = getClassType(access)
        
        declaration.append(classType).append(" ")
        declaration.append(className)
        
        if (classType == "class") declaration.append(" extends ${superName.replace("/", ".")}")
        
        if (interfaces != null && interfaces.isNotEmpty()) {
            when(classType) {
                "interface" -> declaration.append(" extends ")
                "class" -> declaration.append(" implements ")
            }
        }
        
        interfaces.forEachIndexed { i, it->
            declaration.append(it.replace("/", "."))
            if (i != interfaces.size - 1) declaration.append(",")
        }
        
        declaration.append("{\n")
        
        return declaration.toString();
    }
    
    private fun ClassNode.fields(): String {
        val fieldsStub = StringBuilder()
        
        if (getClassType(access) == "enum") {
            fields.forEachIndexed {i, it ->
                fieldsStub.append(it.name)
                if (i != fields.size - 1) fieldsStub.append(", ") else fieldsStub.append(";")
            }
        } else {
            fields.forEach {
                fieldsStub.append(it.getString())
            }
        }
        return fieldsStub.toString()
    }
    
    private fun FieldNode.getString(): String {
        val field = StringBuilder()
        
        field.append(getAccess(access)).append(" ")
        field.append(getFinal(access)).append(" ")
        field.append(getStatic(access)).append(" ")
        field.append(desc.toType().replace("/", ".").replace(";", "")).append(" ")
        field.append(name).append(";\n")
        
        return field.toString()
    }
    
    private fun ClassNode.methods(): String {
        val methodsStub = StringBuilder()
        
        for (it in methods) {
            if (getClassType(access) == "enum" && it.name == "<init>") continue
            
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
        startsWith("Z") -> "boolean"
        startsWith("V") -> "void"
        startsWith("B") -> "byte"
        startsWith("C") -> "char"
        startsWith("S") -> "short"
        startsWith("I") -> "int"
        startsWith("J") -> "long"
        startsWith("F") -> "float"
        startsWith("D") -> "double"
        startsWith("L") -> substring(1)
        startsWith("[") -> {
            val type = substring(1)
            if (type.startsWith("L")) type.substring(1) + "[]" else type + "[]"
        }
        else -> "void"
    }
    
}