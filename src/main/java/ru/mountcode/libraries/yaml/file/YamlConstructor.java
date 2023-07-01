package ru.mountcode.libraries.yaml.file;

import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import ru.mountcode.libraries.yaml.serialization.ConfigurationSerialization;


public class YamlConstructor extends SafeConstructor {

  public YamlConstructor(LoaderOptions loaderOptions) {
    super(loaderOptions);
    yamlConstructors.put(org.yaml.snakeyaml.nodes.Tag.MAP, new ConstructCustomObject(null));
  }

  private class ConstructCustomObject extends ConstructYamlMap {

    private ConstructCustomObject(Object o) {
      super();
    }

    public Object construct(Node node) {
      if (node.isTwoStepsConstruction()) {
        throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
      }

      Map<?, ?> raw = (Map) super.construct(node);

      if (raw.containsKey("==")) {
        Map<String, Object> typed = new java.util.LinkedHashMap(raw.size());
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
          typed.put(entry.getKey().toString(), entry.getValue());
        }
        try {
          return ConfigurationSerialization.deserializeObject(typed);
        } catch (IllegalArgumentException ex) {
          throw new YAMLException("Could not deserialize object", ex);
        }
      }

      return raw;
    }

    public void construct2ndStep(Node node, Object object) {
      throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
    }
  }
}
