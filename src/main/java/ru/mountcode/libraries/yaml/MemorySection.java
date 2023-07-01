package ru.mountcode.libraries.yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.commons.lang3.Validate;


public class MemorySection implements ru.mountcode.libraries.yaml.ConfigurationSection {

  protected final Map<String, Object> map = new LinkedHashMap<>();


  private final Configuration root;


  private final ru.mountcode.libraries.yaml.ConfigurationSection parent;

  private final String path;

  private final String fullPath;


  protected MemorySection() {
    if (!(this instanceof Configuration)) {
      throw new IllegalStateException("Cannot construct a root MemorySection when not a Configuration");
    }

    path = "";
    fullPath = "";
    parent = null;
    root = ((Configuration) this);
  }


  protected MemorySection(ru.mountcode.libraries.yaml.ConfigurationSection parent, String path) {
    Validate.notNull(parent, "Parent cannot be null");
    Validate.notNull(path, "Path cannot be null");

    this.path = path;
    this.parent = parent;
    root = parent.getRoot();

    Validate.notNull(root, "Path cannot be orphaned");

    fullPath = createPath(parent, path);
  }

  public Set<String> getKeys(boolean deep) {
    Set<String> result = new LinkedHashSet();

    Configuration root = getRoot();
    if ((root != null) && (root.options().copyDefaults())) {
      ru.mountcode.libraries.yaml.ConfigurationSection defaults = getDefaultSection();

      if (defaults != null) {
        result.addAll(defaults.getKeys(deep));
      }
    }

    mapChildrenKeys(result, this, deep);

    return result;
  }

  public Map<String, Object> getValues(boolean deep) {
    Map<String, Object> result = new LinkedHashMap();

    Configuration root = getRoot();
    if ((root != null) && (root.options().copyDefaults())) {
      ru.mountcode.libraries.yaml.ConfigurationSection defaults = getDefaultSection();

      if (defaults != null) {
        result.putAll(defaults.getValues(deep));
      }
    }

    mapChildrenValues(result, this, deep);

    return result;
  }

  public boolean contains(String path) {
    return get(path) != null;
  }

  public boolean isSet(String path) {
    Configuration root = getRoot();
    if (root == null) {
      return false;
    }
    if (root.options().copyDefaults()) {
      return contains(path);
    }
    return get(path, null) != null;
  }

  public String getCurrentPath() {
    return fullPath;
  }

  public String getName() {
    return path;
  }

  public Configuration getRoot() {
    return root;
  }

  public ru.mountcode.libraries.yaml.ConfigurationSection getParent() {
    return parent;
  }

  public void addDefault(String path, Object value) {
    Validate.notNull(path, "Path cannot be null");

    Configuration root = getRoot();
    if (root == null) {
      throw new IllegalStateException("Cannot add default without root");
    }
    if (root == this) {
      throw new UnsupportedOperationException("Unsupported addDefault(String, Object) implementation");
    }
    root.addDefault(createPath(this, path), value);
  }

  public ru.mountcode.libraries.yaml.ConfigurationSection getDefaultSection() {
    Configuration root = getRoot();
    Configuration defaults = root == null ? null : root.getDefaults();

    if ((defaults != null) &&
        (defaults.isConfigurationSection(getCurrentPath()))) {
      return defaults.getConfigurationSection(getCurrentPath());
    }

    return null;
  }

  public void set(String path, Object value) {
    Validate.notEmpty(path, "Cannot set to an empty path");

    Configuration root = getRoot();
    if (root == null) {
      throw new IllegalStateException("Cannot use section without a root");
    }

    char separator = root.options().pathSeparator();

    int i1 = -1;
    ru.mountcode.libraries.yaml.ConfigurationSection section = this;
    int i2;
    while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
      String node = path.substring(i2, i1);
      ru.mountcode.libraries.yaml.ConfigurationSection subSection = section.getConfigurationSection(node);
      if (subSection == null) {
        section = section.createSection(node);
      } else {
        section = subSection;
      }
    }

    String key = path.substring(i2);
    if (section == this) {
      if (value == null) {
        map.remove(key);
      } else {
        map.put(key, value);
      }
    } else {
      section.set(key, value);
    }
  }

  public Object get(String path) {
    return get(path, getDefault(path));
  }

  public Object get(String path, Object def) {
    Validate.notNull(path, "Path cannot be null");

    if (path.length() == 0) {
      return this;
    }

    Configuration root = getRoot();
    if (root == null) {
      throw new IllegalStateException("Cannot access section without a root");
    }

    char separator = root.options().pathSeparator();

    int i1 = -1;
    ru.mountcode.libraries.yaml.ConfigurationSection section = this;
    int i2;
    while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
      section = section.getConfigurationSection(path.substring(i2, i1));
      if (section == null) {
        return def;
      }
    }

    String key = path.substring(i2);
    if (section == this) {
      Object result = map.get(key);
      return result == null ? def : result;
    }
    return section.get(key, def);
  }

  public ru.mountcode.libraries.yaml.ConfigurationSection createSection(String path) {
    Validate.notEmpty(path, "Cannot create section at empty path");
    Configuration root = getRoot();
    if (root == null) {
      throw new IllegalStateException("Cannot create section without a root");
    }

    char separator = root.options().pathSeparator();

    int i1 = -1;
    ru.mountcode.libraries.yaml.ConfigurationSection section = this;
    int i2;
    while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
      String node = path.substring(i2, i1);
      ru.mountcode.libraries.yaml.ConfigurationSection subSection = section.getConfigurationSection(node);
      if (subSection == null) {
        section = section.createSection(node);
      } else {
        section = subSection;
      }
    }

    String key = path.substring(i2);
    if (section == this) {
      ru.mountcode.libraries.yaml.ConfigurationSection result = new ru.mountcode.libraries.yaml.MemorySection(this,
          key);
      map.put(key, result);
      return result;
    }
    return section.createSection(key);
  }

  public ru.mountcode.libraries.yaml.ConfigurationSection createSection(String path, Map<?, ?> map) {
    ru.mountcode.libraries.yaml.ConfigurationSection section = createSection(path);

    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if ((entry.getValue() instanceof Map)) {
        section.createSection(entry.getKey().toString(), (Map) entry.getValue());
      } else {
        section.set(entry.getKey().toString(), entry.getValue());
      }
    }

    return section;
  }

  public String getString(String path) {
    Object def = getDefault(path);
    return getString(path, def != null ? def.toString() : null);
  }

  public String getString(String path, String def) {
    Object val = get(path, def);
    return val != null ? val.toString() : def;
  }

  public boolean isString(String path) {
    Object val = get(path);
    return val instanceof String;
  }

  public int getInt(String path) {
    Object def = getDefault(path);
    return getInt(path, (def instanceof Number) ? NumberConversions.toInt(def) : 0);
  }

  public int getInt(String path, int def) {
    Object val = get(path, Integer.valueOf(def));
    return (val instanceof Number) ? NumberConversions.toInt(val) : def;
  }

  public boolean isInt(String path) {
    Object val = get(path);
    return val instanceof Integer;
  }

  public boolean getBoolean(String path) {
    Object def = getDefault(path);
    return getBoolean(path, (def instanceof Boolean) ? ((Boolean) def).booleanValue() : false);
  }

  public boolean getBoolean(String path, boolean def) {
    Object val = get(path, Boolean.valueOf(def));
    return (val instanceof Boolean) ? ((Boolean) val).booleanValue() : def;
  }

  public boolean isBoolean(String path) {
    Object val = get(path);
    return val instanceof Boolean;
  }

  public double getDouble(String path) {
    Object def = getDefault(path);
    return getDouble(path, (def instanceof Number) ? NumberConversions.toDouble(def) : 0.0D);
  }

  public double getDouble(String path, double def) {
    Object val = get(path, Double.valueOf(def));
    return (val instanceof Number) ? NumberConversions.toDouble(val) : def;
  }

  public boolean isDouble(String path) {
    Object val = get(path);
    return val instanceof Double;
  }

  public long getLong(String path) {
    Object def = getDefault(path);
    return getLong(path, (def instanceof Number) ? NumberConversions.toLong(def) : 0L);
  }

  public long getLong(String path, long def) {
    Object val = get(path, Long.valueOf(def));
    return (val instanceof Number) ? NumberConversions.toLong(val) : def;
  }

  public boolean isLong(String path) {
    Object val = get(path);
    return val instanceof Long;
  }

  public List<?> getList(String path) {
    Object def = getDefault(path);
    return getList(path, (def instanceof List) ? (List) def : null);
  }

  public List<?> getList(String path, List<?> def) {
    Object val = get(path, def);
    return (List) ((val instanceof List) ? val : def);
  }

  public boolean isList(String path) {
    Object val = get(path);
    return val instanceof List;
  }

  public List<String> getStringList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<String> result = new ArrayList();

    for (Object object : list) {
      if (((object instanceof String)) || (isPrimitiveWrapper(object))) {
        result.add(String.valueOf(object));
      }
    }

    return result;
  }

  public List<Integer> getIntegerList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Integer> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Integer)) {
        result.add((Integer) object);
      } else if ((object instanceof String)) {
        try {
          result.add(Integer.valueOf((String) object));
        } catch (Exception ex) {
        }
      } else if ((object instanceof Character)) {
        result.add(Integer.valueOf(((Character) object).charValue()));
      } else if ((object instanceof Number)) {
        result.add(Integer.valueOf(((Number) object).intValue()));
      }
    }

    return result;
  }

  public List<Boolean> getBooleanList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Boolean> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Boolean)) {
        result.add((Boolean) object);
      } else if ((object instanceof String)) {
        if (Boolean.TRUE.toString().equals(object)) {
          result.add(Boolean.valueOf(true));
        } else if (Boolean.FALSE.toString().equals(object)) {
          result.add(Boolean.valueOf(false));
        }
      }
    }

    return result;
  }

  public List<Double> getDoubleList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Double> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Double)) {
        result.add((Double) object);
      } else if ((object instanceof String)) {
        try {
          result.add(Double.valueOf((String) object));
        } catch (Exception ex) {
        }
      } else if ((object instanceof Character)) {
        result.add(Double.valueOf(((Character) object).charValue()));
      } else if ((object instanceof Number)) {
        result.add(Double.valueOf(((Number) object).doubleValue()));
      }
    }

    return result;
  }

  public List<Float> getFloatList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Float> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Float)) {
        result.add((Float) object);
      } else if ((object instanceof String)) {
        try {
          result.add(Float.valueOf((String) object));
        } catch (Exception ex) {
        }
      } else if ((object instanceof Character)) {
        result.add(Float.valueOf(((Character) object).charValue()));
      } else if ((object instanceof Number)) {
        result.add(Float.valueOf(((Number) object).floatValue()));
      }
    }

    return result;
  }

  public List<Long> getLongList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Long> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Long)) {
        result.add((Long) object);
      } else if ((object instanceof String)) {
        try {
          result.add(Long.valueOf((String) object));
        } catch (Exception ex) {
        }
      } else if ((object instanceof Character)) {
        result.add(Long.valueOf(((Character) object).charValue()));
      } else if ((object instanceof Number)) {
        result.add(Long.valueOf(((Number) object).longValue()));
      }
    }

    return result;
  }

  public List<Byte> getByteList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Byte> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Byte)) {
        result.add((Byte) object);
      } else if ((object instanceof String)) {
        try {
          result.add(Byte.valueOf((String) object));
        } catch (Exception ex) {
        }
      } else if ((object instanceof Character)) {
        result.add(Byte.valueOf((byte) ((Character) object).charValue()));
      } else if ((object instanceof Number)) {
        result.add(Byte.valueOf(((Number) object).byteValue()));
      }
    }

    return result;
  }

  public List<Character> getCharacterList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Character> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Character)) {
        result.add((Character) object);
      } else if ((object instanceof String)) {
        String str = (String) object;

        if (str.length() == 1) {
          result.add(Character.valueOf(str.charAt(0)));
        }
      } else if ((object instanceof Number)) {
        result.add(Character.valueOf((char) ((Number) object).intValue()));
      }
    }

    return result;
  }

  public List<Short> getShortList(String path) {
    List<?> list = getList(path);

    if (list == null) {
      return new ArrayList(0);
    }

    List<Short> result = new ArrayList();

    for (Object object : list) {
      if ((object instanceof Short)) {
        result.add((Short) object);
      } else if ((object instanceof String)) {
        try {
          result.add(Short.valueOf((String) object));
        } catch (Exception ex) {
        }
      } else if ((object instanceof Character)) {
        result.add(Short.valueOf((short) ((Character) object).charValue()));
      } else if ((object instanceof Number)) {
        result.add(Short.valueOf(((Number) object).shortValue()));
      }
    }

    return result;
  }

  public List<Map<?, ?>> getMapList(String path) {
    List<?> list = getList(path);
    List<Map<?, ?>> result = new ArrayList();

    if (list == null) {
      return result;
    }

    for (Object object : list) {
      if ((object instanceof Map)) {
        result.add((Map) object);
      }
    }

    return result;
  }

  public Vector getVector(String path) {
    Object def = getDefault(path);
    return getVector(path, (def instanceof Vector) ? (Vector) def : null);
  }

  public Vector getVector(String path, Vector def) {
    Object val = get(path, def);
    return (val instanceof Vector) ? (Vector) val : def;
  }

  public boolean isVector(String path) {
    Object val = get(path);
    return val instanceof Vector;
  }

  public ru.mountcode.libraries.yaml.ConfigurationSection getConfigurationSection(String path) {
    Object val = get(path, null);
    if (val != null) {
      return (val instanceof ru.mountcode.libraries.yaml.ConfigurationSection)
          ? (ru.mountcode.libraries.yaml.ConfigurationSection) val : null;
    }

    val = get(path, getDefault(path));
    return (val instanceof ru.mountcode.libraries.yaml.ConfigurationSection) ? createSection(path) : null;
  }

  public boolean isConfigurationSection(String path) {
    Object val = get(path);
    return val instanceof ru.mountcode.libraries.yaml.ConfigurationSection;
  }

  protected boolean isPrimitiveWrapper(Object input) {
    return ((input instanceof Integer)) || ((input instanceof Boolean)) || ((input instanceof Character))
        || ((input instanceof Byte)) || ((input instanceof Short)) || ((input instanceof Double))
        || ((input instanceof Long)) || ((input instanceof Float));
  }


  protected Object getDefault(String path) {
    Validate.notNull(path, "Path cannot be null");

    Configuration root = getRoot();
    Configuration defaults = root == null ? null : root.getDefaults();
    return defaults == null ? null : defaults.get(createPath(this, path));
  }

  protected void mapChildrenKeys(Set<String> output, ru.mountcode.libraries.yaml.ConfigurationSection section,
      boolean deep) {
    if ((section instanceof ru.mountcode.libraries.yaml.MemorySection)) {
      ru.mountcode.libraries.yaml.MemorySection sec = (ru.mountcode.libraries.yaml.MemorySection) section;

      for (Map.Entry<String, Object> entry : map.entrySet()) {
        output.add(createPath(section, (String) entry.getKey(), this));

        if ((deep) && ((entry.getValue() instanceof ru.mountcode.libraries.yaml.ConfigurationSection))) {
          ru.mountcode.libraries.yaml.ConfigurationSection subsection = (ru.mountcode.libraries.yaml.ConfigurationSection) entry.getValue();
          mapChildrenKeys(output, subsection, deep);
        }
      }
    } else {
      Set<String> keys = section.getKeys(deep);

      for (String key : keys) {
        output.add(createPath(section, key, this));
      }
    }
  }

  protected void mapChildrenValues(Map<String, Object> output, ru.mountcode.libraries.yaml.ConfigurationSection section,
      boolean deep) {
    if ((section instanceof ru.mountcode.libraries.yaml.MemorySection)) {
      ru.mountcode.libraries.yaml.MemorySection sec = (ru.mountcode.libraries.yaml.MemorySection) section;

      for (Map.Entry<String, Object> entry : map.entrySet()) {
        output.put(createPath(section, (String) entry.getKey(), this), entry.getValue());

        if (((entry.getValue() instanceof ru.mountcode.libraries.yaml.ConfigurationSection)) &&
            (deep)) {
          mapChildrenValues(output, (ru.mountcode.libraries.yaml.ConfigurationSection) entry.getValue(), deep);
        }
      }
    } else {
      Map<String, Object> values = section.getValues(deep);

      for (Map.Entry<String, Object> entry : values.entrySet()) {
        output.put(createPath(section, (String) entry.getKey(), this), entry.getValue());
      }
    }
  }


  public static String createPath(ru.mountcode.libraries.yaml.ConfigurationSection section, String key) {
    return createPath(section, key, section == null ? null : section.getRoot());
  }


  public static String createPath(ru.mountcode.libraries.yaml.ConfigurationSection section, String key,
      ru.mountcode.libraries.yaml.ConfigurationSection relativeTo) {
    Validate.notNull(section, "Cannot create path without a section");
    Configuration root = section.getRoot();
    if (root == null) {
      throw new IllegalStateException("Cannot create path without a root");
    }
    char separator = root.options().pathSeparator();

    StringBuilder builder = new StringBuilder();
    if (section != null) {
      for (ru.mountcode.libraries.yaml.ConfigurationSection parent = section;
          (parent != null) && (parent != relativeTo); parent = parent.getParent()) {
        if (builder.length() > 0) {
          builder.insert(0, separator);
        }

        builder.insert(0, parent.getName());
      }
    }

    if ((key != null) && (key.length() > 0)) {
      if (builder.length() > 0) {
        builder.append(separator);
      }

      builder.append(key);
    }

    return builder.toString();
  }

  public String toString() {
    Configuration root = getRoot();
    return getClass().getSimpleName() + "[path='" + getCurrentPath() + "', root='" + (root == null ? null
        : root.getClass().getSimpleName()) + "']";
  }
}
