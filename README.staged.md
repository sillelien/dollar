
[ ![Binary Distribution](download.png)](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.3.2483.tgz)

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

Download the [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.3.2483.tgz) then

```bash
    tar -zxvf dollar-0.3.2483.tgz
    ./dollar/bin/dollar <filename>.ds
```

# Docs

Documentation is at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar).


# Maven

The artifacts used to build the Dollar project can be accessed using

```xml
            <repositories>
                <repository>
                    <id>dollar-s3-release-repo</id>
                    <name>S3 Release Repository for component1</name>
                    <url>s3://dollar-repo/release</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                </repository>
                <repository>
                    <id>dollar-s3-snapshot-repo</id>
                    <name>Component1-s3-snapshot-repo</name>
                    <url>s3://dollar-repo/snapshot</url>
                    <releases>
                        <enabled>false</enabled>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                    </snapshots>
                </repository>
            </repositories>
```

and

```xml
        <dependency>
            <groupId>com.sillelien</groupId>
            <artifactId>dollar-xxx</artifactId>
            <version>0.3.2483</version>
        </dependency>
```


Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)



# Docker

```bash
docker run -v $HOME/.github:/root/.github -v $HOME/.dollar:/root/.dollar -v $(pwd):/build -it sillelien/dollarscript-headless:${MAJOR_VERSION}  <filename>.ds
 ```
