
[ ![Binary Distribution](assets/download.png)](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-${RELEASE}.tgz)

[![GitHub License](https://img.shields.io/github/license/sillelien/dollar.svg)](https://raw.githubusercontent.com/sillelien/dollar/master/LICENSE) 
[![GitHub Issues](https://img.shields.io/github/issues/sillelien/dollar.svg)](https://github.com/sillelien/dollar/issues)
[![GitHub Release](https://img.shields.io/github/release/sillelien/dollar.svg)](https://github.com/sillelien/dollar)
[![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)
[![BCH compliance](https://bettercodehub.com/edge/badge/sillelien/dollar?branch=master)](https://bettercodehub.com/)

[![Join the chat at https://gitter.im/sillelien/dollar](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sillelien/dollar?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitQ](https://gitq.com/badge.svg)](https://gitq.com/sillelien/dollar)
[![Patreon](https://img.shields.io/badge/back_on-patreon-red.svg)](https://www.patreon.com/neilellis)

Full documentation at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar) and the manual is at [http://sillelien.github.io/dollar/manual/](http://sillelien.github.io/dollar/manual/)

![dollar](assets/gh-title-dollar.png)

Dollar is an integration centric, reactive dynamic programming language which runs on the JVM. It is a language built on top of the [dollar-core](https://github.com/sillelien/dollar-core) library which helps to provide a consistent view of multiple dynamic data types. 

It is being designed for the needs of server-side development such as integration, messaging etc. It is also an internet centric language where JSON and URIs are first class citizens.

**The language is currently in early but active development.**

I'm also working full-time on this, if you wish to support efforts back me on [Patreon](https://www.patreon.com/neilellis):

[![Patreon](https://img.shields.io/badge/back_on-patreon-red.svg)](https://www.patreon.com/neilellis)

**Thanks!**

______

Find out your local air quality:

```dollar
//First we get the Geo Location of our IP address
Value geo= read http://freegeoip.net/json/
Value lat= geo.latitude
Value lon= geo.longitude

Value quality= read ("https://api.openaq.org/v1/measurements?radius=10000&limit=5&coordinates="+ lat + "," + lon) as URI;

//Now output the quality from each result
quality.results each {
    @@ $1.location
    @@ $1.parameter +" was "+ $1.value +  " " + $1["unit"] +" on " + $1.date.utc
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
### Language Design

http://www.drdobbs.com/architecture-and-design/so-you-want-to-write-your-own-language/240165488

https://www.youtube.com/watch?v=Sg4U4r_AgJU

### Lambdas and Scope Closure

https://martinfowler.com/bliki/Lambda.html

### Font Ligatures

https://github.com/tonsky/FiraCode

$BLURB
