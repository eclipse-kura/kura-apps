# Eclipse Kura™ Apps BOM (Bill of Materials)

The **Eclipse Kura™ Apps BOM** is a Bill of Materials that provides centralized version management for all Eclipse Kura example and prototype bundles.

## Purpose

This BOM simplifies dependency management for projects using Eclipse Kura™ Examples and Eclipse Kura™ Addon Prototypes bundles, ensuring version consistency and reducing configuration duplication.

## Included Bundles

The BOM includes **26 bundles** organized in the following categories:

### Demo Bundles (2)
- `org.eclipse.kura.demo.heater` - Heating simulator
- `org.eclipse.kura.demo.modbus` - Modbus communication example

### Example Bundles (21)
- `org.eclipse.kura.example.ble.tisensortag.dbus` - Bluetooth Low Energy integration
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

### Importing the BOM

To use the BOM in your project, add the following configuration in the `<dependencyManagement>` section:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.eclipse.kura</groupId>
            <artifactId>kura-apps-bom</artifactId>
            <version>6.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Using bundles

After importing the BOM, you can use bundles without specifying versions:

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
                <artifactId>kura-apps-bom</artifactId>
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

The BOM manages the following versions:

- **Example Bundles**: `2.0.0-SNAPSHOT` (except `demo.modbus` which is `3.0.0-SNAPSHOT`)
- **Raspberry Pi and Wire Development Bundles**: `2.0.0-SNAPSHOT`
- **BOM Version**: `6.0.0-SNAPSHOT`

## Benefits

✅ **Centralized management**: All versions are defined in a single place

✅ **Consistency**: Ensures all bundles use compatible versions

✅ **Simplicity**: No need to specify versions for each bundle

✅ **Maintainability**: Centralized version updates

✅ **Error reduction**: Prevents version conflicts between bundles

## Repository

The BOM is published to Eclipse Kura repositories:

- **Releases**: https://repo.eclipse.org/content/repositories/kura-releases/
- **Snapshots**: https://repo.eclipse.org/content/repositories/kura-snapshots/

