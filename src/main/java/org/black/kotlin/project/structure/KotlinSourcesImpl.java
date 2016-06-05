package org.black.kotlin.project.structure;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.black.kotlin.project.ui.customizer.KotlinProjectProperties;
import org.openide.util.Mutex;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Implementation of {@link Sources} interface for J2SEProject.
 */
public class KotlinSourcesImpl implements Sources, PropertyChangeListener, ChangeListener  {
    
    private static final String BUILD_DIR_PROP = "${" + KotlinProjectProperties.BUILD_DIR + "}";    //NOI18N
    private static final String DIST_DIR_PROP = "${" + KotlinProjectProperties.DIST_DIR + "}";    //NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private SourcesHelper sourcesHelper;
    private Sources delegate;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public KotlinSourcesImpl(Project project, AntProjectHelper helper, PropertyEvaluator evaluator,
                SourceRoots sourceRoots, SourceRoots testRoots) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.sourceRoots.addPropertyChangeListener(KotlinSourcesImpl.this);
        this.testRoots.addPropertyChangeListener(KotlinSourcesImpl.this);        
        this.evaluator.addPropertyChangeListener(KotlinSourcesImpl.this);
        initSources();
    }

    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            @Override
            public SourceGroup[] run() {
                Sources _delegate;
                synchronized (KotlinSourcesImpl.this) {
                    if (delegate == null) {                    
                        delegate = initSources();
                        delegate.addChangeListener(KotlinSourcesImpl.this);
                    }
                    _delegate = delegate;
                }
                SourceGroup[] groups = _delegate.getSourceGroups(type);
                if (type.equals(Sources.TYPE_GENERIC)) {
                    FileObject libLoc = getSharedLibraryFolderLocation();
                    if (libLoc != null) {
                        SourceGroup[] grps = new SourceGroup[groups.length + 1];
                        System.arraycopy(groups, 0, grps, 0, groups.length);
                        grps[grps.length - 1] = GenericSources.group(project, libLoc, 
                                "sharedlibraries", // NOI18N
                                NbBundle.getMessage(KotlinSourcesImpl.class, "LibrarySourceGroup_DisplayName"), 
                                null, null);
                        return grps;
                    }
                }
                return groups;
            }
        });
    }
    
    private FileObject getSharedLibraryFolderLocation() {
        String libLoc = helper.getLibrariesLocation();
        if (libLoc != null) {
            String libLocEval = evaluator.evaluate(libLoc);
            File file = null;
            if (libLocEval != null) {
                file = helper.resolveFile(libLocEval);
            }
            FileObject libLocFO = FileUtil.toFileObject(file);
            if (libLocFO != null) {
                //#126366 this can happen when people checkout the project but not the libraries description 
                //that is located outside the project
                FileObject libLocParent = libLocFO.getParent();
                return libLocParent;
            }
        } 
        return null;
    }
    
    private Sources initSources() {
        this.sourcesHelper = new SourcesHelper(project, helper, evaluator);   //Safe to pass APH        
        register(sourceRoots);
        register(testRoots);
        this.sourcesHelper.addNonSourceRoot(BUILD_DIR_PROP);
        this.sourcesHelper.addNonSourceRoot(DIST_DIR_PROP);
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT, false);
        return this.sourcesHelper.createSources();
    }

    private void register(SourceRoots roots) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            String includes = "${" + ProjectProperties.INCLUDES + "}"; // NOI18N
            String excludes = "${" + ProjectProperties.EXCLUDES + "}"; // NOI18N
            sourcesHelper.addPrincipalSourceRoot(loc, includes, excludes, displayName, null, null); // NOI18N
            sourcesHelper.addTypedSourceRoot(loc, includes, excludes, JavaProjectConstants.SOURCES_TYPE_JAVA, displayName, null, null); // NOI18N
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private void fireChange() {
        synchronized (this) {
            if (delegate != null) {
                delegate.removeChangeListener(this);
                delegate = null;
            }
        }
        changeSupport.fireChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) ||
            KotlinProjectProperties.BUILD_DIR.equals(propName)  ||
            KotlinProjectProperties.DIST_DIR.equals(propName)) {
            this.fireChange();
        }
    }
    
    @Override
    public void stateChanged (ChangeEvent event) {
        this.fireChange();
    }

}