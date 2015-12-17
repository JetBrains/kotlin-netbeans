package org.black.kotlin.resolve;


import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import com.google.common.collect.Sets;
//import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Collection;
import java.util.Set;
import kotlin.CollectionsKt;
import kotlin.Pair;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.frontend.java.di.InjectionKt;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.netbeans.api.project.Project;

import org.black.kotlin.resolve.KotlinPackagePartProvider;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
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

        MutableModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(ijProject, ijProject.toString());
        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);
        BindingTrace trace = new CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace();

        
        ContainerForTopDownAnalyzerForJvm container = 
                InjectionKt.createContainerForTopDownAnalyzerForJvm(moduleContext, 
                        trace, 
                        providerFactory,
                        GlobalSearchScope.allScope(ijProject),  
                        LookupTracker.DO_NOTHING, 
                        new KotlinPackagePartProvider (nbProject, ijProject));
           //val additionalProviders = listOf(container.javaDescriptorResolver.packageFragmentProvider);

            try {
                container.getLazyTopDownAnalyzerForTopLevel().analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, 
                        allFiles, 
                        null
                       // CollectionsKt.listOf((container.getJavaDescriptorResolver().getPackageFragmentProvider()))
                        );
        } catch(KotlinFrontEndException ex) {
//          Editor will break if we do not catch this exception
//          and will not be able to save content without reopening it.
//          In IDEA this exception throws only in CLI
            ex.printStackTrace();
        }
    
    
        return AnalysisResult.success(trace.getBindingContext(), moduleContext.getModule());
    }
    
}