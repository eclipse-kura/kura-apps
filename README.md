Eclipse Kura™
=============

<p align="center">
<img src="https://eclipse.dev/kura/images/kura.png" alt="Kura™ logo" width="500"/>
</p>

<div align="center">

[![GitHub Tag](https://img.shields.io/github/v/tag/eclipse/kura?label=Latest%20Tag)](https://github.com/eclipse-kura/kura/tags)
[![GitHub](https://img.shields.io/github/license/eclipse/kura?label=License)](https://github.com/eclipse-kura/kura/blob/develop/LICENSE)

[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https:%2F%2Fci.eclipse.org%2Fkura%2Fjob%2Fmultibranch%2Fjob%2Fdevelop&label=Jenkins%20Build&logo=jenkins)](https://ci.eclipse.org/kura/job/multibranch/job/develop/)
[![Jenkins](https://img.shields.io/jenkins/tests?compact_message&failed_label=%E2%9D%8C&jobUrl=https:%2F%2Fci.eclipse.org%2Fkura%2Fjob%2Fmultibranch%2Fjob%2Fdevelop%2F&label=Jenkins%20CI&passed_label=%E2%9C%85&skipped_label=%E2%9D%95&logo=jenkins)](https://ci.eclipse.org/kura/job/multibranch/) <br/>
  
</div>

## Applications for Eclipse Kura™ framework
In this repository you can find some application that can be installed on an Eclipse Kura™ instance.

### What Applications for Eclipse Kura™ can I build?
* **Kura™ Examples:** provides examples of component that can be developed for the Eclipse Kura™ framework.

Build
-----

### Prerequisites

In order to be able to build the applications for Eclipse Kura™ on your development machine, you need to have the following programs installed in your system:
* JDK 17
* Maven 3.9.x

<details>
<summary>

#### Installing Prerequisites in Mac OS 

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

#### Installing Prerequisites in Linux

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
git clone -b master https://github.com/eclipse-kura/kura-apps.git
```

Move inside the newly created directory and build the target platform:

```bash
mvn -f target-platform/pom.xml clean install
```

Build the examples:

```bash
mvn -f kura-examples/pom.xml clean install
```

> [!TIP]
You can skip tests by adding `-Dmaven.test.skip=true` in the commands above.

Contributing
------------

Contributing to the Applications for Eclipse Kura™ is fun and easy! To start contributing you can follow our guide [here](CONTRIBUTING.md).
