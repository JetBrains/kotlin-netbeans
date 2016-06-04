package org.black.kotlin.project.structure;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.openide.util.ImageUtilities;

/**
* This class provides information about Kotlin project.
*/
public final class KotlinProjectInfo implements ProjectInformation {
    
        private final KotlinProject project;
    
        @StaticResource()
        public static final String KOTLIN_ICON = "org/black/kotlin/kotlin.png";

        public static ProjectInformation createInformation(KotlinProject project, UpdateHelper updateHelper){
            return QuerySupport.createProjectInformation(updateHelper, project, 
                    new ImageIcon(ImageUtilities.loadImage(KOTLIN_ICON)));
        }
        
        public KotlinProjectInfo(KotlinProject project){
            this.project = project;
        }
        
        @Override
        public String getName() {
            return project.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(KOTLIN_ICON));
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
        }

    }
