package org.black.kotlin.project.structure;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.black.kotlin.project.ui.customizer.KotlinCustomizerProvider;
import org.black.kotlin.project.ui.customizer.KotlinProjectProperties;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Class for ant-based Kotlin project.
 *
 * @author Александр
 */
@AntBasedProjectRegistration(type = "org.black.kotlin.project.KotlinProject",
        iconResource = "org/black/kotlin/kotlin.png",
        sharedName = "data",
        sharedNamespace = "http://www.netbeans.org/ns/kotlin-project/1",
        privateName = "project-private",
        privateNamespace = "http://www.netbeans.org/ns/kotlin-project-private/1")
public class KotlinProject implements Project {

    private final AntProjectHelper helper;
    private final KotlinSources kotlinSources;
    private final GlobalPathRegistry pathRegistry = GlobalPathRegistry.getDefault();
    private final AuxiliaryConfiguration auxiliaryConfiguration;
    private final PropertyEvaluator propertyEvaluator;
    private final UpdateHelper updateHelper;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private Lookup lkp;
    private AntBuildExtender buildExtender;
    
    public KotlinProject(AntProjectHelper helper) {
        this.helper = helper;
        kotlinSources = new KotlinSources(this);
        auxiliaryConfiguration = helper.createAuxiliaryConfiguration();
        propertyEvaluator = createEvaluator();
        updateHelper = new UpdateHelper(new UpdateProjectImpl(this,helper,auxiliaryConfiguration), helper);
        refHelper = new ReferenceHelper(helper, auxiliaryConfiguration, propertyEvaluator);
        
        buildExtender = AntBuildExtenderFactory.createAntExtender(new KotlinExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        KotlinProject.this.getLookup();
    }

    private PropertyEvaluator createEvaluator(){
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(), 
                helper.getPropertyProvider(KotlinConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(), 
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        ConfigPropertyProvider configPropertyProvider1 = 
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper);
        ConfigPropertyProvider configPropertyProvider2 = 
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper);
        baseEval1.addPropertyChangeListener(configPropertyProvider1);
        baseEval1.addPropertyChangeListener(configPropertyProvider2);
        
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(), 
                helper.getPropertyProvider(KotlinConfigurationProvider.CONFIG_PROPS_PATH),
                configPropertyProvider1,
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2, "user.properties.file", FileUtil.toFile(getProjectDirectory())),
                configPropertyProvider2,
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }
    
    private static final class ConfigPropertyProvider extends FilterPropertyProvider 
            implements PropertyChangeListener{
        
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper){
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.helper = helper;
            this.prefix = prefix;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent event){
            if (KotlinConfigurationProvider.PROP_CONFIG.equals(event.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval,
                String prefix, AntProjectHelper helper){
            String config = baseEval.getProperty(KotlinConfigurationProvider.PROP_CONFIG);
            if (config != null){
                return helper.getPropertyProvider(prefix + "/" + config + ".properties");
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
            }
        }
        
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    private Lookup createLookup(final AuxiliaryConfiguration auxiliaryConfiguration, 
            final ActionProvider actionProvider){
        final SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileEncodingQueryImplementation encodingQuery = 
                QuerySupport.createFileEncodingQuery(getPropertyEvaluator(), KotlinProjectProperties.SOURCE_ENCODING);
        
        final Lookup base = Lookups.fixed(new Object[]{
                    KotlinProject.this,
                    KotlinProjectInfo.createInformation(KotlinProject.this, updateHelper),
                    auxiliaryConfiguration,
                    helper.createAuxiliaryProperties(),
                    helper.createCacheDirectoryProvider(),
                    spp,
                    actionProvider,
                    new KotlinProjectLogicalView(this),
                    new KotlinCustomizerProvider(this,updateHelper,getPropertyEvaluator(), refHelper, this.genFilesHelper),
                    QuerySupport.createCompiledSourceForBinaryQuery(helper, getPropertyEvaluator(), getSourceRoots(), getTestSourceRoots()),
                    UILookupMergerSupport.createProjectOpenHookMerger(new KotlinProjectOpenedHook(this)),
                    new KotlinConfigurationProvider(this),
                    encodingQuery,
                    new KotlinActionProvider(this),
                    new KotlinPrivilegedTemplates(),
                    new KotlinClassPathProvider(this)
        });
//        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-black-kotlin/Lookup");
        return base;
    }
    
    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            KotlinActionProvider actionProvider = new KotlinActionProvider(this);
            lkp = createLookup(auxiliaryConfiguration, actionProvider);
        }
        return lkp;
    }

    public AntProjectHelper getHelper() {
        return helper;
    }

    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public GlobalPathRegistry getPathRegistry() {
        return pathRegistry;
    }

    public KotlinSources getKotlinSources() {
        return kotlinSources;
    }
    
    public PropertyEvaluator getPropertyEvaluator(){
        return propertyEvaluator;
    }
    
    public ReferenceHelper getReferenceHelper() {
        return refHelper;
    }
    
    public synchronized SourceRoots getSourceRoots(){
        if (sourceRoots == null) {
            sourceRoots = SourceRoots.create(updateHelper, 
                    propertyEvaluator, 
                    refHelper, 
                    "http://www.netbeans.org/ns/kotlin-project/1", 
                    "source-roots", 
                    false, 
                    "src.{0}{1}.dir");
        }
        return sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots(){
        if (testRoots == null){
            testRoots = SourceRoots.create(updateHelper, 
                    propertyEvaluator, 
                    refHelper, 
                    "http://www.netbeans.org/ns/kotlin-project/1", 
                    "test-roots", 
                    true, 
                    "test.{0}{1}.dir");
        }
        return testRoots;
    }
    
    private static final class KotlinPrivilegedTemplates implements PrivilegedTemplates {

        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/Kotlin/content.kt",
            "Templates/Classes/Class.java",
            "Templates/Classes/Interface.java",
            "Templates/Classes/Package"
        };

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

    }
    
    private class KotlinExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..

        @Override
        public List<String> getExtensibleTargets() {
            String[] targets = new String[]{
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile", "-do-compile", "-do-compile-single" //NOI18N

            };
            return Arrays.asList(targets);
        }

        @Override
        public Project getOwningProject() {
            return KotlinProject.this;
        }
}

}
