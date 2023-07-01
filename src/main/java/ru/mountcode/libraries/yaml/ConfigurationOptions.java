package ru.mountcode.libraries.yaml;


public class ConfigurationOptions {

    private final Configuration configuration;
    private char pathSeparator = '.';
    private boolean copyDefaults = false;

    protected ConfigurationOptions(Configuration configuration) {
        this.configuration = configuration;
    }


    public Configuration configuration() {
        return configuration;
    }


    public char pathSeparator() {
        return pathSeparator;
    }


    public ConfigurationOptions pathSeparator(char value) {
        pathSeparator = value;
        return this;
    }


    public boolean copyDefaults() {
        return copyDefaults;
    }


    public ConfigurationOptions copyDefaults(boolean value) {
        copyDefaults = value;
        return this;
    }
}
