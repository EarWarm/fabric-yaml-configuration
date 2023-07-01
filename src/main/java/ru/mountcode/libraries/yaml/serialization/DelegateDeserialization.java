package ru.mountcode.libraries.yaml.serialization;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
public @interface DelegateDeserialization {

  Class<? extends ConfigurationSerializable> value();
}
