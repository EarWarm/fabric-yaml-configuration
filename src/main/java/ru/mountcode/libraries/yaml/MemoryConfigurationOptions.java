package ru.mountcode.libraries.yaml;

public class MemoryConfigurationOptions
    extends ru.mountcode.libraries.yaml.ConfigurationOptions {

  protected MemoryConfigurationOptions(ru.mountcode.libraries.yaml.MemoryConfiguration configuration) {
    super(configuration);
  }

  public ru.mountcode.libraries.yaml.MemoryConfiguration configuration() {
    return (ru.mountcode.libraries.yaml.MemoryConfiguration) super.configuration();
  }

  public ru.mountcode.libraries.yaml.MemoryConfigurationOptions copyDefaults(boolean value) {
    super.copyDefaults(value);
    return this;
  }

  public ru.mountcode.libraries.yaml.MemoryConfigurationOptions pathSeparator(char value) {
    super.pathSeparator(value);
    return this;
  }
}
