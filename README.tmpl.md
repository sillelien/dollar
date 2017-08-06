
[ ![Binary Distribution](assets/download.png)](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-${RELEASE}.tgz)

[![GitHub License](https://img.shields.io/github/license/sillelien/dollar.svg)](https://raw.githubusercontent.com/sillelien/dollar/master/LICENSE) 
[![GitHub Issues](https://img.shields.io/github/issues/sillelien/dollar.svg)](https://github.com/sillelien/dollar/issues)
[![GitHub Release](https://img.shields.io/github/release/sillelien/dollar.svg)](https://github.com/sillelien/dollar)
[![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)

[![Join the chat at https://gitter.im/sillelien/dollar](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sillelien/dollar?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitQ](https://gitq.com/badge.svg)](https://gitq.com/sillelien/dollar)




![dollar](assets/gh-title-dollar.png)

Dollar is an integration centric, reactive dynamic programming language which runs on the JVM. It is a language built on top of the [dollar-core](https://github.com/sillelien/dollar-core) library which helps to provide a consistent view of multiple dynamic data types. 

It is being designed for the needs of server-side development such as integration, messaging etc. It is also an internet centric language where JSON and URIs are first class citizens.

**The language is currently in early but active development.**

I'm also working full-time on this, if you wish to support efforts drop me some money at [https://www.paypal.me/neilellis](https://www.paypal.me/neilellis) every little helps!

**Thanks!**

Below is a fully functional persistent chat server in dollar:

```dollar
    //Fully working persistent chat server
    var server= socketio://127.0.0.1:8092/bulletin?eventType=chatevent
    var lastMessages= db:circular://messages/tmp/messages10.db?size=10

    var message *= server
    const timestampedMessage := (message + {"timestamp":DATE()})
    timestampedMessage >> lastMessages

    ("chatevent" : timestampedMessage) publish server

    http://127.0.0.1:8091/messages subscribe {
        {"body":all lastMessages, "headers":{"Access-Control-Allow-Origin":"*"}}
    }
```

Learn more at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar).

![Install](assets/gh-title-install.png)


Download the [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-${RELEASE}.tgz) then run the following in your shell:

```bash
    tar -zxvf dollar-${RELEASE}.tgz
```

To use dollar:

```bash
    ./dollar/bin/dollar <filename>.ds
```

![Docs](assets/gh-title-docs.png)

Documentation is at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar).

Q&A: https://gitq.com/sillelien/dollar
 

![Maven](assets/gh-title-maven.png)

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
            </repositories>
```

and 

```xml
        <dependency>
            <groupId>com.sillelien</groupId>
            <artifactId>dollar-xxx</artifactId>
            <version>${RELEASE}</version>
        </dependency>
```


Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)



![Docker](assets/gh-title-docker.png)

There is a docker image, but it is in it's infancy so better to work with the  [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-${RELEASE}.tgz) for now.
```bash
docker run -v $HOME/.github:/root/.github -v $HOME/.dollar:/root/.dollar -v $(pwd):/build -it sillelien/dollarscript-headless:${MAJOR_VERSION}  <filename>.ds
 ```
 
 [![Docker Registry](https://img.shields.io/docker/pulls/sillelien/dollarscript-headless.svg)](https://registry.hub.docker.com/u/sillelien/dollarscript-headless)
 
 
## Research & Background Reading

http://www.drdobbs.com/architecture-and-design/so-you-want-to-write-your-own-language/240165488

https://www.youtube.com/watch?v=Sg4U4r_AgJU


$BLURB
