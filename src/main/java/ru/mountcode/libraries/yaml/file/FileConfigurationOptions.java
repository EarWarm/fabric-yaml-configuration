package ru.mountcode.libraries.yaml.file;

import ru.mountcode.libraries.yaml.MemoryConfiguration;
import ru.mountcode.libraries.yaml.MemoryConfigurationOptions;

public class FileConfigurationOptions extends MemoryConfigurationOptions {

    private String header = null;
    private boolean copyHeader = true;

    protected FileConfigurationOptions(MemoryConfiguration configuration) {
        super(configuration);
    }

    public FileConfiguration configuration() {
        return (FileConfiguration) super.configuration();
    }

    public FileConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    public FileConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }


    public String header() {
        return header;
    }


    public FileConfigurationOptions header(String value) {
        header = value;
        return this;
    }


    public boolean copyHeader() {
        return copyHeader;
    }


    public FileConfigurationOptions copyHeader(boolean value) {
        copyHeader = value;

        return this;
    }
}
