package ru.mountcode.libraries.yaml.file;

import com.google.common.io.Files;
import org.apache.commons.lang3.Validate;
import ru.mountcode.libraries.yaml.Configuration;
import ru.mountcode.libraries.yaml.InvalidConfigurationException;
import ru.mountcode.libraries.yaml.MemoryConfiguration;

import java.io.*;


public abstract class FileConfiguration extends MemoryConfiguration {

    public FileConfiguration() {
    }

    public FileConfiguration(Configuration defaults) {
        super(defaults);
    }

    public void save(File file) throws IOException {
        Validate.notNull(file, "File cannot be null");

        Files.createParentDirs(file);

        String data = saveToString();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
        }
    }

    public void save(String file) throws IOException {
        Validate.notNull(file, "File cannot be null");

        save(new File(file));
    }

    public abstract String saveToString();

    public void load(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");

        load(java.nio.file.Files.newInputStream(file.toPath()));
    }

    public void load(InputStream stream)
            throws IOException, InvalidConfigurationException {
        Validate.notNull(stream, "Stream cannot be null");

        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();

        try (BufferedReader input = new BufferedReader(reader)) {
            String line;

            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        }

        loadFromString(builder.toString());
    }

    public void load(String file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");

        load(new File(file));
    }


    public abstract void loadFromString(String paramString) throws InvalidConfigurationException;

    protected abstract String buildHeader();

    public FileConfigurationOptions options() {
        if (options == null) {
            options = new FileConfigurationOptions(this);
        }

        return (FileConfigurationOptions) options;
    }
}
