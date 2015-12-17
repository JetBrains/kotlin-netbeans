package org.black.kotlin.resolve;


import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import com.google.common.collect.Sets;
import com.intellij.openapi.util.Disposer;
//import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import kotlin.CollectionsKt;
import kotlin.Pair;
import kotlin.reflect.jvm.internal.impl.load.java.lazy.LazyJavaPackageFragmentProvider;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.frontend.java.di.InjectionKt;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.netbeans.api.project.Project;

import org.black.kotlin.resolve.KotlinPackagePartProvider;
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.messages.MessageSeverityCollector;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import static org.jetbrains.kotlin.context.ContextKt.ModuleContext;
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.resolve.TopDownAnalysisContext;
import org.jetbrains.kotlin.utils.KotlinFrontEndException;


//import static org.jetbrains.kotlin.context.ContextPackage.ContextForNewModule;


/**
 *
 * @author polina
 */

public class NBAnalyzerFacadeForJVM {
    
    
    
    
    
    public static AnalysisResult analyzeFilesWithJavaIntegration(com.intellij.openapi.project.Project ijProject, 
            Project nbProject,
            Collection<KtFile> filesToAnalyze)
    {
        Set<KtFile> allFiles = Sets.newLinkedHashSet(filesToAnalyze);

        ModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(ijProject, ijProject.toString());
        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);
        BindingTrace trace = new CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace();

       
        CompilerConfiguration configuration = new CompilerConfiguration();
        
        KotlinCoreEnvironment environment = KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(), 
                configuration, 
                EnvironmentConfigFiles.JVM_CONFIG_FILES);
        
        
        //container thing as seen in eclipse-plugin
//        ContainerForTopDownAnalyzerForJvm container = 
//                InjectionKt.createContainerForTopDownAnalyzerForJvm(moduleContext, 
//                        trace, 
//                        providerFactory,
//                        GlobalSearchScope.allScope(ijProject),  
//                        LookupTracker.DO_NOTHING,
//                        PackagePartProvider.EMPTY
//                        );
//     
//        try {
//                 analysisContext = container.getLazyTopDownAnalyzerForTopLevel().analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, 
//                        allFiles, 
//                        additionalProviders 
//                       
//                        );
//        } catch(KotlinFrontEndException ex) {
//            ex.printStackTrace();
//        }
        
        //as seen in AbstractLoadJavaTest.java  
        TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationNoIncremental(
                moduleContext,
                allFiles,
                trace,
                TopDownAnalysisMode.TopLevelDeclarations,
                new KotlinPackagePartProvider(nbProject, ijProject)
        );
        AnalysisResult analysisResult = AnalysisResult.success(trace.getBindingContext(), moduleContext.getModule());
    
        
        return analysisResult;
    }
    
}