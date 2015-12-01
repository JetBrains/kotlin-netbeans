package org.black.kotlin.utils;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
/**
 *
 * @author polina
 */
public class HintsUtil {

    public static Map<FileObject, List<ErrorDescription>> parseAnalysisResult (AnalysisResult analysisResult) 
    {
        Map<FileObject, List<ErrorDescription>> annotations = new HashMap<FileObject, List<ErrorDescription>>();
        
        TopComponent active = TopComponent.getRegistry().getActivated();
        DataObject dataLookup = active.getLookup().lookup(DataObject.class);
        FileObject currFileObject = dataLookup.getPrimaryFile();
        
        Diagnostics diagnostics = analysisResult.getBindingContext().getDiagnostics();
        for (Diagnostic diagnostic : diagnostics) 
        {
            if (diagnostic.getTextRanges().isEmpty()) {
                continue;
            }
            VirtualFile virtualFile = diagnostic.getPsiFile().getVirtualFile();
            if (virtualFile == null) {
                continue;
            }
            
            if (!annotations.containsKey(currFileObject)) 
            {
                annotations.put(currFileObject, new ArrayList<ErrorDescription>());
            }
            
            
            annotations.get(currFileObject).add(ErrorDescriptionFactory.createErrorDescription(
                    Severity.valueOf(diagnostic.getSeverity().toString()), 
                    diagnostic.toString(), 
                    currFileObject,
                    diagnostic.getTextRanges().get(0).getStartOffset(), 
                    diagnostic.getTextRanges().get(0).getEndOffset()));
            
        }
        return annotations;
    }
}
