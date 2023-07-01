# Fabric Yaml Configuration library mod

This modification allows you to use yaml configuration files as a configuration for your mod.
The modification already includes the snakeyaml library.

## FabricConfiguration methods

```java
// Sets the path to the final configuration
setFile(File file)
setFile(String file)

// Sets the stream of the original configuration file
// as a rule the original configuration is located in the mod resources.
setDefault(InputStream defaultConfigurationFile)
        
// Initializes the configuration
intialize()

// Saves the configuration to a file
saveConfiguration()
        
// Reloads the configuration from the file 
// needed to update the configuration during mod operation.
relaodConfiguration()
```

## Simple configuration example

assets/example-mod/config/example-mod.yml
```yaml
# Тестовая ветка
test:
  integer: 1
  double: 1.1
  string: 'test'
  list:
    - 'test-1'
    - 'test-2'
    - 'test-3'
  section:
    test: 'test'
```

```java
public class ExampleMod implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();
    public FabricConfiguration configuration = new FabricConfiguration();

    @Override
    public void onInitialize() {
        // Getting default configuration file from mod assets
        InputStream defaultConfigurationFile = ExampleMod.class.getResourceAsStream("/assets/example-mod/config/example-mod.yml");
        
        // Setting default configuration file to configuration
        configuration.setDefault(defaultConfigurationFile);
        // Setting configuration file path
        configuration.setFile("config/example-mod.yml");
        // Initialize configuration (load, if file exists else generate configuration)
        try {
            configuration.initialize();
        } catch (IOException e) {
            LOGGER.error("Configuration file cannot be generated", e);
        } catch (InvalidConfigurationException e) {
            LOGGER.error("Configuration file cannot be loaded", e);
        }
        
        
    }
}
```

## Example code (save and get ItemStack from/to configuration)
```java
ItemStack exampleItemStack = new ItemStack(Items.BEDROCK);
NbtCompound itemStackNbt = exampleItemStack.writeNbt(new NbtCompound());
        
configuration.set("item-stack", NbtHelper.toNbtProviderString(itemStackNbt));
        
try {
    configuration.saveConfiguration();
} catch (IOException e) {
    LOGGER.error("Configuration cannot be saved", e);
}

ItemStack itemStack = null;
try {
    itemStack = ItemStack.fromNbt(NbtHelper.fromNbtProviderString(configuration.getString("item-stack")));
} catch (CommandSyntaxException e) {
    LOGGER.error("ItemStack cannot be loaded from configuration", e);
}
```