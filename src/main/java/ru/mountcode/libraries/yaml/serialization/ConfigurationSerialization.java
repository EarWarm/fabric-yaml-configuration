package ru.mountcode.libraries.yaml.serialization;


import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConfigurationSerialization {

    public static final String SERIALIZED_TYPE_KEY = "==";
    private static final Map<String, Class<? extends ConfigurationSerializable>> aliases = new HashMap<>();
    private final Class<? extends ConfigurationSerializable> clazz;

    protected ConfigurationSerialization(Class<? extends ConfigurationSerializable> clazz) {
        this.clazz = clazz;
    }

    public static ConfigurationSerializable deserializeObject(Map<String, ?> args,
                                                              Class<? extends ConfigurationSerializable> clazz) {
        return new ConfigurationSerialization(clazz).deserialize(args);
    }

    public static ConfigurationSerializable deserializeObject(Map<String, ?> args) {
        Class<? extends ConfigurationSerializable> clazz = null;

        if (args.containsKey("==")) {
            try {
                String alias = (String) args.get("==");

                if (alias == null) {
                    throw new IllegalArgumentException("Cannot have null alias");
                }
                clazz = getClassByAlias(alias);
                if (clazz == null) {
                    throw new IllegalArgumentException("Specified class does not exist ('" + alias + "')");
                }
            } catch (ClassCastException ex) {
                ex.fillInStackTrace();
                throw ex;
            }
        } else {
            throw new IllegalArgumentException("Args doesn't contain type key ('==')");
        }

        return new ConfigurationSerialization(clazz).deserialize(args);
    }

    public static void registerClass(Class<? extends ConfigurationSerializable> clazz) {
        DelegateDeserialization delegate = clazz.getAnnotation(DelegateDeserialization.class);

        if (delegate == null) {
            registerClass(clazz, getAlias(clazz));
            registerClass(clazz, clazz.getName());
        }
    }

    public static void registerClass(Class<? extends ConfigurationSerializable> clazz, String alias) {
        aliases.put(alias, clazz);
    }

    public static void unregisterClass(String alias) {
        aliases.remove(alias);
    }

    public static void unregisterClass(Class<? extends ConfigurationSerializable> clazz) {
        while (aliases.values().remove(clazz)) {
        }
    }

    public static Class<? extends ConfigurationSerializable> getClassByAlias(String alias) {
        return aliases.get(alias);
    }

    public static String getAlias(Class<? extends ConfigurationSerializable> clazz) {
        DelegateDeserialization delegate = clazz.getAnnotation(DelegateDeserialization.class);

        if (delegate != null) {
            if ((delegate.value() == null) || (delegate.value() == clazz)) {
                delegate = null;
            } else {
                return getAlias(delegate.value());
            }
        }

        if (delegate == null) {
            SerializableAs alias = clazz.getAnnotation(SerializableAs.class);

            if ((alias != null) && (alias.value() != null)) {
                return alias.value();
            }
        }

        return clazz.getName();
    }

    protected Method getMethod(String name, boolean isStatic) {
        try {
            Method method = clazz.getDeclaredMethod(name, Map.class);

            if (!ConfigurationSerializable.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            if (Modifier.isStatic(method.getModifiers()) != isStatic) {
                return null;
            }

            return method;
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ignored) {
        }
        return null;
    }

    protected Constructor<? extends ConfigurationSerializable> getConstructor() {
        try {
            return clazz.getConstructor(Map.class);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ignored) {
        }
        return null;
    }

    protected ConfigurationSerializable deserializeViaMethod(Method method, Map<String, ?> args) {
        try {
            ConfigurationSerializable result = (ConfigurationSerializable) method.invoke(null, new Object[]{args});

            if (result == null) {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                        "Could not call method '" + method + "' of " + clazz
                                + " for deserialization: method returned null");
            } else {
                return result;
            }
        } catch (Throwable ex) {
            Logger.getLogger(ConfigurationSerialization.class.getName())
                    .log(Level.SEVERE, "Could not call method '" + method.toString() + "' of " + clazz + " for deserialization",
                            (ex instanceof InvocationTargetException) ? ex.getCause() : ex);
        }

        return null;
    }

    protected ConfigurationSerializable deserializeViaCtor(Constructor<? extends ConfigurationSerializable> ctor,
                                                           Map<String, ?> args) {
        try {
            return ctor.newInstance(args);
        } catch (Throwable ex) {
            Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                    "Could not call constructor '" + ctor.toString() + "' of " + clazz + " for deserialization",
                    (ex instanceof InvocationTargetException) ? ex.getCause() : ex);
        }

        return null;
    }

    public ConfigurationSerializable deserialize(Map<String, ?> args) {
        Validate.notNull(args, "Args must not be null");

        ConfigurationSerializable result = null;
        Method method = null;

        if (result == null) {
            method = getMethod("deserialize", true);

            if (method != null) {
                result = deserializeViaMethod(method, args);
            }
        }

        if (result == null) {
            method = getMethod("valueOf", true);

            if (method != null) {
                result = deserializeViaMethod(method, args);
            }
        }

        if (result == null) {
            Constructor<? extends ConfigurationSerializable> constructor = getConstructor();

            if (constructor != null) {
                result = deserializeViaCtor(constructor, args);
            }
        }

        return result;
    }
}
