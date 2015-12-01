/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.black.kotlin.resolve;

import java.util.LinkedHashSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.context.GlobalContext;
import org.jetbrains.kotlin.context.ModuleContext;
//import org.jetbrains.kotlin.core.utils.ProjectUtils;//
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.utils.KotlinFrontEndException;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport.CliBindingTrace;

//import static org.jetbrains.kotlin.context.ContextPackage.ContextForNewModule;


/**
 *
 * @author polina
 */

public class NBAnalyzerFacadeForJVM {
    public static AnalysisResult analyzeFilesWithJavaIntegration(Project project, 
            Collection<KtFile> filesToAnalyze)
    {
        Set<KtFile> fileSet = Sets.newHashSet(filesToAnalyze);
        Set<KtFile> allFiles = Sets.newLinkedHashSet(fileSet);

        MutableModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule((com.intellij.openapi.project.Project) project, project.toString());
        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);
        BindingTrace trace = new CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace();
        
        return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationNoIncremental(
                moduleContext,
                allFiles,
                trace,
                TopDownAnalysisMode.TopLevelDeclarations, 
                PackagePartProvider.EMPTY);

    }
    
}