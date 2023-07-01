package ru.mountcode.libraries.yaml.serialization;

import java.util.Map;

public abstract interface ConfigurationSerializable {

  public abstract Map<String, Object> serialize();
}
