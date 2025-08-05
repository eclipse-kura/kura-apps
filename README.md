Eclipse Kura™
=============

<p align="center">
<img src="https://eclipse.dev/kura/images/kura.png" alt="Kura™ logo" width="500"/>
</p>

## Applications for Eclipse Kura™ framework
In this repository you can find some application that can be installed on an Eclipse Kura™ instance.

### What Applications for Eclipse Kura™ can I build?
* **Kura™ Examples:** provides examples of component that can be developed for the Eclipse Kura™ framework.
* **Kura™ Prototypes:** provides prototypes of components that can be developed for the Eclipse Kura™ framework.

Build
-----

For the complete guide to setup the development environment, please refer to the [Eclipse Kura™ documentation](https://eclipse-kura.github.io/kura/latest/java-application-development/development-environment-setup/).

### Prerequisites

In order to be able to build the applications for Eclipse Kura™ on your development machine, you need to have the following programs installed in your system:

* JDK 17
* Maven 3.9.x

<details>
<summary>

#### Installing Mandatory Programs in Mac OS 

</summary>

To install Java 17, download the JDK tar archive from the [Adoptium Project Repository](https://adoptium.net/en-GB/temurin/releases/?variant=openjdk8&jvmVariant=hotspot&version=17).

Once downloaded, copy the tar archive in `/Library/Java/JavaVirtualMachines/` and cd into it. Unpack the archive with the following command:

```bash
sudo tar -xzf <archive-name>.tar.gz
```

The tar archive can be deleted afterwards.

Depending on which terminal you are using, edit the profiles (.zshrc, .profile, .bash_profile) to contain:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/<archive-name>/Contents/Home
```

Reload the terminal and run `java -version` to make sure it is installed correctly.

Using [Brew](https://brew.sh/) you can easily install Maven from the command line:

```bash
brew install maven@3.9
```
Run `mvn -version` to ensure that Maven has been added to the PATH. If Maven cannot be found, try running `brew link maven@3.9 --force` or manually add it to your path with:

```bash
export PATH="/usr/local/opt/maven@3.9/bin:$PATH"
```

</details>

<details>
<summary>

#### Installing Mandatory Programs in Linux

</summary>

For Java
```bash
sudo apt install openjdk-17-jdk
```
For Maven   

You can follow the tutorial from the official [Maven](http://maven.apache.org/install.html) site. Remember that you need to install the 3.9.x version.

</details>

### Build the Applications for Eclipse Kura™

Change to the new directory and clone the Applications Eclipse Kura™ repo:

```bash
git clone https://github.com/eclipse-kura/kura-apps.git
```

Build the project:

```bash
mvn clean install
```

> [!TIP]
You can skip tests by adding `-Dmaven.test.skip=true` in the commands above.

The Debian installers for the examples and prototypes will be generated in the `kura-apps-distrib` directory, under `kura-examples/target/deb` and `kura-addon-prototypes/target/deb` respectively.

Contributing
------------

Contributing to the Applications for Eclipse Kura™ is fun and easy! To start contributing you can follow our guide [here](CONTRIBUTING.md).
