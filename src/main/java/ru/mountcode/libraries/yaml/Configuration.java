package ru.mountcode.libraries.yaml;

import java.util.Map;

public abstract interface Configuration extends ru.mountcode.libraries.yaml.ConfigurationSection {

  public abstract void addDefault(String paramString, Object paramObject);

  public abstract void addDefaults(Map<String, Object> paramMap);

  public abstract void addDefaults(ru.mountcode.libraries.yaml.Configuration paramConfiguration);

  public abstract void setDefaults(ru.mountcode.libraries.yaml.Configuration paramConfiguration);

  public abstract ru.mountcode.libraries.yaml.Configuration getDefaults();

  public abstract ConfigurationOptions options();
}
