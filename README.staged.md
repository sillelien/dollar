
[ ![Binary Distribution](download.png)](https://bintray.com/sillelien/binary/download_file?file_path=dollar-0.3.2199.tgz)

# Dollar

Dollar is an integration centric, reactive dynamic programming language which runs on the JVM. It is currently in development.  It is being designed for the needs of serverside non-web based development, such as integration, messaging etc.

Below is a fully functional persistent chat server in Dollar:

```dollar
    //Fully working persistent chat server
    server= socketio://127.0.0.1:8092/bulletin?eventType=chatevent
    lastMessages= db:circular://messages/tmp/messages10.db?size=10

    message *= server
    timestampedMessage := (message + {"timestamp":DATE()})
    timestampedMessage >> lastMessages

    ("chatevent" : timestampedMessage) publish server

    http://127.0.0.1:8091/messages subscribe {
        {"body":all lastMessages, "headers":{"Access-Control-Allow-Origin":"*"}}
    }
```

# Install

Download the [distribution](https://bintray.com/sillelien/binary/download_file?file_path=dollar-0.3.2199.tgz) then

```bash
    tar -zxvf dollar-0.3.2199.tgz
    ./dollar/bin/dollar <filename>.ds
```

# Docs

Documentation is at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar).

# Bintray

The bintray page for the distribution binary is here: [ ![Download](https://api.bintray.com/packages/sillelien/binary/dollar/images/download.svg?version=0.3.2199) ](https://bintray.com/sillelien/binary/dollar/0.3.2199/link)


# Maven

The artifacts used to build the Dollar project can be accessed using

```xml
    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>bintray-sillelien-maven</id>
            <name>bintray</name>
            <url>http://dl.bintray.com/sillelien/maven</url>
        </repository>
    </repositories>
```  

and

```xml
        <dependency>
            <groupId>com.sillelien</groupId>
            <artifactId>dollar-xxx</artifactId>
            <version>0.3.2199</version>
        </dependency>
```


Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)



# Docker


Headless Docker Image: [ ![Download](https://api.bintray.com/packages/sillelien/docker/dollarscript-headless/images/download.svg) ](https://bintray.com/sillelien/docker/dollarscript-headless/_latestVersion)

```bash
alias dollar="docker run sillelien/dollarscript-headless:${MAJOR_VERSION}"
 ```
