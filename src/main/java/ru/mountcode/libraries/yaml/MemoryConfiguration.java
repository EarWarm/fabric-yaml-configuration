package ru.mountcode.libraries.yaml;

import java.util.Map;
import org.apache.commons.lang3.Validate;


public class MemoryConfiguration
    extends ru.mountcode.libraries.yaml.MemorySection
    implements Configuration {

  protected Configuration defaults;
  protected MemoryConfigurationOptions options;

  public MemoryConfiguration() {
  }

  public MemoryConfiguration(Configuration defaults) {
    this.defaults = defaults;
  }

  public void addDefault(String path, Object value) {
    Validate.notNull(path, "Path may not be null");

    if (defaults == null) {
      defaults = new ru.mountcode.libraries.yaml.MemoryConfiguration();
    }

    defaults.set(path, value);
  }

  public void addDefaults(Map<String, Object> defaults) {
    Validate.notNull(defaults, "Defaults may not be null");

    for (Map.Entry<String, Object> entry : defaults.entrySet()) {
      addDefault((String) entry.getKey(), entry.getValue());
    }
  }

  public void addDefaults(Configuration defaults) {
    Validate.notNull(defaults, "Defaults may not be null");

    addDefaults(defaults.getValues(true));
  }

  public void setDefaults(Configuration defaults) {
    Validate.notNull(defaults, "Defaults may not be null");

    this.defaults = defaults;
  }

  public Configuration getDefaults() {
    return defaults;
  }

  public ru.mountcode.libraries.yaml.ConfigurationSection getParent() {
    return null;
  }

  public MemoryConfigurationOptions options() {
    if (options == null) {
      options = new MemoryConfigurationOptions(this);
    }

    return options;
  }
}
