package ru.mountcode.libraries.yaml.file;

import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;
import ru.mountcode.libraries.yaml.serialization.ConfigurationSerializable;
import ru.mountcode.libraries.yaml.serialization.ConfigurationSerialization;

public class YamlRepresenter extends Representer {

  public YamlRepresenter(DumperOptions options) {
    super(options);
    multiRepresenters.put(ru.mountcode.libraries.yaml.ConfigurationSection.class,
        new RepresentConfigurationSection(null));
    multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable(null));
  }

  private class RepresentConfigurationSection extends RepresentMap {

    private RepresentConfigurationSection(Object o) {
      super();
    }


    public Node representData(Object data) {
      return super.representData(((ru.mountcode.libraries.yaml.ConfigurationSection) data).getValues(false));
    }
  }

  private class RepresentConfigurationSerializable extends RepresentMap {

    private RepresentConfigurationSerializable(Object o) {
      super();
    }

    public Node representData(Object data) {
      ConfigurationSerializable serializable = (ConfigurationSerializable) data;
      Map<String, Object> values = new java.util.LinkedHashMap();
      values.put("==", ConfigurationSerialization.getAlias(serializable.getClass()));
      values.putAll(serializable.serialize());

      return super.representData(values);
    }
  }
}
