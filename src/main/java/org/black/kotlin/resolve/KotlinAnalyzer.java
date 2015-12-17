package org.black.kotlin.resolve;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import java.util.Collection;

//import org.netbeans.api.project.Project;
import org.jetbrains.annotations.NotNull;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.analyzer.AnalysisResult;

import org.jetbrains.kotlin.psi.KtFile;

public class KotlinAnalyzer {
    @NotNull
    private static AnalysisResult analyzeFile(@NotNull Project ijProject, 
            org.netbeans.api.project.Project nbProject,
            @NotNull KtFile jetFile) {
        return NBAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                ijProject,
                nbProject,
                Lists.newArrayList(jetFile));
    }
    @NotNull
    private static AnalysisResult analyzeFiles_t(@NotNull Project ijProject,
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
            return analyzeFile(ijProject, nbProject, filesToAnalyze.iterator().next());
        }
                
        return analyzeFiles_t(ijProject, nbProject, filesToAnalyze);
    }
    
   
}