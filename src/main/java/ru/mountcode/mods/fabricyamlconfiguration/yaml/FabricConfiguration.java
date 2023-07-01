package ru.mountcode.mods.fabricyamlconfiguration.yaml;

import org.apache.commons.lang3.Validate;
import ru.mountcode.libraries.yaml.InvalidConfigurationException;
import ru.mountcode.libraries.yaml.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FabricConfiguration extends YamlConfiguration {

    private InputStream defaultFile = null;
    private File file = null;

    public void setDefault(InputStream defaultFile) {
        this.defaultFile = defaultFile;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFile(String file) {
        this.file = new File(file);
    }

    public void initialize() throws IOException, InvalidConfigurationException {
        Validate.notNull(this.file, "Configuration file cannot be null");

        if (this.file.exists()) {
            this.load(this.file);
        } else {
            this.generateFile();
        }
    }

    private void generateFile() throws IOException, InvalidConfigurationException {
        if (this.defaultFile == null) {
            this.options().copyDefaults(true);
            saveConfiguration();
            return;
        }

        com.google.common.io.Files.createParentDirs(this.file);

        Files.copy(this.defaultFile, this.file.toPath());
        this.load(this.file);
    }

    public void reloadConfiguration() throws IOException, InvalidConfigurationException {
        initialize();
    }

    public void saveConfiguration() throws IOException {
        this.save(file);
    }
}
