# Eclipse Kura™ Apps PDE Deps

The **Eclipse Kura™ Apps PDE Deps** is a centralized dependency version management file for all Eclipse Kura example and prototype bundles. It allows you to manage and align the versions of the dependencies used by the various bundles in a single place, simplifying configuration and ensuring consistency across projects.

## Purpose

This PDE Deps simplifies dependency management for projects using Eclipse Kura™ Examples and Eclipse Kura™ Addon Prototypes bundles, ensuring version consistency and reducing configuration duplication.

## Included Bundles

The PDE Deps includes **26 bundles** organized in the following categories:

### Demo Bundles (2)
- `org.eclipse.kura.demo.heater` - Heating simulator
- `org.eclipse.kura.demo.modbus` - Modbus communication example

### Example Bundles (21)
- `org.eclipse.kura.example.ble.tisensortag.dbus` - Bluetooth Low Energy TI SensorTag example
- `org.eclipse.kura.example.camel.*` - Apache Camel integration examples
- `org.eclipse.kura.example.gpio.*` - GPIO control
- `org.eclipse.kura.example.wire.*` - Wire Framework components
- `org.eclipse.kura.example.publisher` - MQTT publisher
- And many more...

### Raspberry Pi SenseHat (2)
- `org.eclipse.kura.raspberrypi.sensehat` - SenseHat driver
- `org.eclipse.kura.raspberrypi.sensehat.example` - SenseHat examples

### Wire Development (1)
- `org.eclipse.kura.wire.devel.component.provider` - Wire development components

## Usage

### Importing the PDE Deps

To use the PDE Deps in your project, add the following configuration in the `<dependencyManagement>` section:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eclipse.kura</groupId>
            <artifactId>kura-apps-pde-deps</artifactId>
            <version>6.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Using bundles

After importing the PDE Deps, you can use bundles without specifying versions:

```xml
<dependencies>
    <dependency>
        <groupId>org.eclipse.kura</groupId>
        <artifactId>org.eclipse.kura.demo.heater</artifactId>
    </dependency>
    <dependency>
        <groupId>org.eclipse.kura</groupId>
        <artifactId>org.eclipse.kura.example.gpio</artifactId>
    </dependency>
    <dependency>
        <groupId>org.eclipse.kura</groupId>
        <artifactId>org.eclipse.kura.example.publisher</artifactId>
    </dependency>
</dependencies>
```

### Complete example

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>my-kura-app</artifactId>
    <version>1.0.0</version>
    <packaging>eclipse-plugin</packaging>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.eclipse.kura</groupId>
                <artifactId>kura-apps-pde-deps</artifactId>
                <version>6.0.0-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.eclipse.kura</groupId>
            <artifactId>org.eclipse.kura.example.publisher</artifactId>
        </dependency>
        <dependency>
            <groupId>org.eclipse.kura</groupId>
            <artifactId>org.eclipse.kura.example.gpio</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Managed Versions

The PDE Deps manages the following versions:

- **Example Bundles**: `3.0.0-SNAPSHOT`
- **PDE Deps Version**: `6.0.0-SNAPSHOT`

## Benefits

✅ **Centralized management**: All versions are defined in a single place

✅ **Consistency**: Ensures all bundles use compatible versions

✅ **Simplicity**: No need to specify versions for each bundle

✅ **Maintainability**: Centralized version updates

✅ **Error reduction**: Prevents version conflicts between bundles

## Repository

The PDE Deps is published to Eclipse Kura repositories:

- **Releases**: https://repo.eclipse.org/content/repositories/kura-releases/
- **Snapshots**: https://repo.eclipse.org/content/repositories/kura-snapshots/

