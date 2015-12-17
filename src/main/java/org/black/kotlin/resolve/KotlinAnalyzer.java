package org.black.kotlin.resolve;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import java.util.Collection;

//import org.netbeans.api.project.Project;
import org.jetbrains.annotations.NotNull;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.resolve.lazy.JvmResolveUtil;

import org.jetbrains.kotlin.psi.KtFile;

public class KotlinAnalyzer {
    @NotNull
    private static AnalysisResult analyzeFileP(@NotNull Project ijProject, 
            org.netbeans.api.project.Project nbProject,
            @NotNull KtFile jetFile) {
        return NBAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                ijProject,
                nbProject,
                Lists.newArrayList(jetFile));
    }
    @NotNull
    private static AnalysisResult analyzeFilesP(@NotNull Project ijProject,
            org.netbeans.api.project.Project nbProject,
            @NotNull Collection<KtFile> filesToAnalyze) 
    {
        return NBAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                ijProject,
                nbProject,
                filesToAnalyze);
    }
        
    public static AnalysisResult analyzeFiles(@NotNull Project ijProject, 
            org.netbeans.api.project.Project nbProject,
            @NotNull Collection<KtFile> filesToAnalyze) {
              
        if (filesToAnalyze.size() == 1) {
            return analyzeFileP(ijProject, nbProject, filesToAnalyze.iterator().next());
        }
                
        return analyzeFilesP(ijProject, nbProject, filesToAnalyze);
    }

    @NotNull
    public static AnalysisResult analyzeFile(@NotNull Project ijProject, 
            org.netbeans.api.project.Project nbProject,
            @NotNull KtFile jetFile) {
        return JvmResolveUtil.analyzeOneFileWithJavaIntegrationAndCheckForErrors(jetFile);
    }
    
}