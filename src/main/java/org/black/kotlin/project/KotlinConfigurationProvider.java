package org.black.kotlin.project;

import org.black.kotlin.project.ui.customizer.KotlinCustomizerProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.black.kotlin.project.ui.customizer.KotlinCompositePanelProvider;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Александр
 */
public class KotlinConfigurationProvider 
        implements ProjectConfigurationProvider<KotlinConfigurationProvider.Config> {
    
    public static final String PROP_CONFIG = "config";
    
    public static final String CONFIG_PROPS_PATH = "nbproject/private/config.properties";
    
    public static final class Config implements ProjectConfiguration {
        
        private final String displayName;
        private final String name;
        
        public Config(String name, String displayName){
            this.name = name;
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
        
        public String getName(){
            return name;
        }
        
        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
 
        @Override
        public boolean equals(Object o) {
            return (o instanceof Config) && Utilities.compareObjects(name, ((Config) o).name);
        }
 
        
    }
    
    private static final Config CONFIG = new Config(null, "KotlinProject");
    
    private final KotlinProject project;
    private final PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(this);
    private final FileChangeListener fileChangeListener = new FileChangeAdapter(){
        
        @Override
        public void fileFolderCreated(FileEvent fileEvent) {
            update(fileEvent);
        }
        
        @Override
        public void fileDataCreated(FileEvent fileEvent) {
            update(fileEvent);
        }
        
        @Override
        public void fileDeleted(FileEvent fileEvent) {
            update(fileEvent);
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fileEvent) {
            update(fileEvent);
        }
        private void update(FileEvent ev) {
            Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
            configDir = project.getProjectDirectory().getFileObject("nbproject/configs"); 
            if (configDir != null) {
                configDir.removeFileChangeListener(fclWeak);
                configDir.addFileChangeListener(fclWeak);
            }
            calculateConfigs();
            Set<String> newConfigs = configs.keySet();
            if (!oldConfigs.equals(newConfigs)) {
                propertyChangeSupport.
                        firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
            }
        }
    };
    
    private final FileChangeListener fclWeak;
    private FileObject configDir;
    private Map<String, Config> configs;
    private final FileObject nbp;
    
    public KotlinConfigurationProvider(KotlinProject project){
        this.project = project;
        fclWeak = FileUtil.weakFileChangeListener(fileChangeListener,null);
        nbp = project.getProjectDirectory().getFileObject("nbproject");
        if (nbp != null){
            nbp.addFileChangeListener(fclWeak);
            configDir = nbp.getFileObject("configs");
            if (configDir != null) {
                configDir.addFileChangeListener(fclWeak);
            }
        }
        project.getPropertyEvaluator().addPropertyChangeListener(new PropertyChangeListener(){
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                propertyChangeSupport.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
            }
            
        });
    }    
    
    private void calculateConfigs() {
        configs = new HashMap<String, Config>();
        if (configDir != null){
            for (FileObject child : configDir.getChildren()){
                if (!child.hasExt("properties")) {
                    continue;
                }
                try {
                    InputStream inputStream = child.getInputStream();
                    try {
                        Properties properties = new Properties();
                        properties.load(inputStream);
                        String name = child.getName();
                        String label = properties.getProperty("$label");
                        configs.put(name, new Config(name, label != null ? label : name));
                    } finally {
                        inputStream.close();
                    }
                } catch (IOException x) {
                    
                }
            }
        }
    }
    
    @Override
    public Collection<Config> getConfigurations() {
        calculateConfigs();
        List<Config> list = new ArrayList<Config>();
        list.addAll(configs.values());
        Collections.sort(list, new Comparator<Config>(){
            Collator collator = Collator.getInstance();
            
            @Override
            public int compare(Config config1, Config config2) {
                return collator.compare(config1.getDisplayName(), config2.getDisplayName());
            }
        });
        
        list.add(0, CONFIG);
        return list;
    }
    
    @Override
    public Config getActiveConfiguration() {
        calculateConfigs();
        String config = project.getPropertyEvaluator().getProperty(PROP_CONFIG);
        if (config != null && configs.containsKey(config)) {
            return configs.get(config);
        } else {
            return CONFIG;
        }
    }
    
    @Override
    public void setActiveConfiguration(Config c) throws IllegalArgumentException, IOException {
        if (c != CONFIG && !configs.values().contains(c)) {
            throw new IllegalArgumentException();
        }
        final String name = c.getName();
        EditableProperties ep = project.getUpdateHelper().getProperties(CONFIG_PROPS_PATH);
        if (Utilities.compareObjects(name, ep.getProperty(PROP_CONFIG))) {
            return;
        }
        if (name != null) {
            ep.setProperty(PROP_CONFIG, name);
        } else {
            ep.remove(PROP_CONFIG);
        }
        project.getUpdateHelper().putProperties(CONFIG_PROPS_PATH, ep);
        propertyChangeSupport.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        ProjectManager.getDefault().saveProject(project);
        assert project.getProjectDirectory().getFileObject(CONFIG_PROPS_PATH) != null;
    }
    
    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        project.getLookup().lookup(KotlinCustomizerProvider.class).showCustomizer(KotlinCompositePanelProvider.RUN);
    }
    
    @Override
    public boolean configurationsAffectAction(String command) {
        return command.equals(ActionProvider.COMMAND_RUN) ||
               command.equals(ActionProvider.COMMAND_DEBUG);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        propertyChangeSupport.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        propertyChangeSupport.removePropertyChangeListener(lst);
}
    
}

