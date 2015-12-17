
/*package org.jetbrains.kotlin.core.resolve

import java.util.LinkedHashSet
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport
import org.jetbrains.kotlin.context.GlobalContext
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.core.utils.ProjectUtils
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm
import org.jetbrains.kotlin.psi.JetFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import java.util.HashSet
import org.jetbrains.kotlin.utils.KotlinFrontEndException

import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.descriptors.PackagePartProvider


public object EclipseAnalyzerFacadeForJVM {
    public fun analyzeFilesWithJavaIntegration(ijProject: Project, 
                тиproject: org.netbeans.api.project.Project, 
                filesToAnalyze: Collection<JetFile>): AnalysisResult {
            val filesSet = filesToAnalyze.toSet()
            //if (filesSet.size() != filesToAnalyze.size()) {
            //    KotlinLogger.logWarning("Analyzed files have duplicates")
            //}

            val allFiles = LinkedHashSet<JetFile>(filesSet)
            val addedFiles = filesSet.map { getPath(it) }


            val moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(ijProject, ijProject.getName())
            val providerFactory = FileBasedDeclarationProviderFactory(moduleContext.storageManager, allFiles)
            val trace = CliLightClassGenerationSupport.CliBindingTrace()

            val containerAndProvider = createContainerForTopDownAnalyzerForJvm(moduleContext, trace, providerFactory,
                    GlobalSearchScope.allScope(project), nbProject, LookupTracker.DO_NOTHING, KotlinPackagePartProviderKT(nbProject, ijProject))
            val container = containerAndProvider.first
            val additionalProviders = listOf(container.javaDescriptorResolver.packageFragmentProvider)

            try {
                container.lazyTopDownAnalyzerForTopLevel.analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, filesSet, additionalProviders)
        } catch(e: KotlinFrontEndException) {
//          Editor will break if we do not catch this exception
//          and will not be able to save content without reopening it.
//          In IDEA this exception throws only in CLI
            KotlinLogger.logError(e)
        }
        
        return AnalysisResultWithProvider(
                AnalysisResult.success(trace.getBindingContext(), moduleContext.module),
                containerAndProvider.second)
    }
    
    private fun getPath(jetFile: JetFile): String = jetFile.getVirtualFile().getPath()
}
*/