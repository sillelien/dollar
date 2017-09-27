
[ ![Binary Distribution](assets/download.png)](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.4.5156.tgz)

[![GitHub License](https://img.shields.io/github/license/sillelien/dollar.svg)](https://raw.githubusercontent.com/sillelien/dollar/master/LICENSE) 
[![GitHub Issues](https://img.shields.io/github/issues/sillelien/dollar.svg)](https://github.com/sillelien/dollar/issues)
[![GitHub Release](https://img.shields.io/github/release/sillelien/dollar.svg)](https://github.com/sillelien/dollar)
[![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)
[![BCH compliance](https://bettercodehub.com/edge/badge/sillelien/dollar?branch=master)](https://bettercodehub.com/)

[![Join the chat at https://gitter.im/sillelien/dollar](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sillelien/dollar?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitQ](https://gitq.com/badge.svg)](https://gitq.com/sillelien/dollar)
[![Patreon](https://img.shields.io/badge/back_on-patreon-red.svg)](https://www.patreon.com/neilellis)

Full documentation at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar) and the manual is at [http://sillelien.github.io/dollar/manual/](http://sillelien.github.io/dollar/manual/)

[Download it Now](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.4.5156.tgz) 

## Intro

Dollar is a light-weight scripting language for the JVM. More specifically it is an integration centric, reactive dynamic programming language.

It is currently being built for the needs of server-side developers and Java developers in general who need to produce quick prototypes or write simple scripts. Furthermore it is an internet centric language where JSON and URIs are first class citizens.

[Download it Now](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.4.5156.tgz) 

**The language is currently in early but active development.**

I'm also working full-time on this, if you wish to support my efforts then please back me on [Patreon](https://www.patreon.com/neilellis):

[![Patreon](https://img.shields.io/badge/back_on-patreon-red.svg)](https://www.patreon.com/neilellis)

And/or star the project on GitHub.

**Thanks!**

______

Example to find out your local air quality:

```dollar
//First we get the Geo Location of our IP address
var geo= read http://freegeoip.net/json/
var lat= geo.latitude
var lon= geo.longitude

var quality= read ("https://api.openaq.org/v1/measurements?radius=10000&limit=5&coordinates="+ lat + "," + lon) as URI;

//Now output the quality from each result
quality.results each {
    @@ $1.location
    @@ $1.parameter +" was "+ $1.value +  " " + $1["unit"] +" on " + $1.date.utc
}


```

Learn more at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar).

## Install


Download the [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.4.5156.tgz) then run the following in your shell:

```bash
    tar -zxvf dollar-0.4.5156.tgz
```

To use dollar:

```bash
    ./dollar/bin/dollar <filename>.ds
```

## Docs

Documentation is at [http://sillelien.github.io/dollar](http://sillelien.github.io/dollar).

Q&A: https://gitq.com/sillelien/dollar
 
[Download it Now](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.4.5156.tgz) 

## Maven

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
            <version>0.4.5156</version>
        </dependency>
```


Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)



## Docker

There is a docker image, but it is in it's infancy so better to work with the  [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-0.4.5156.tgz) for now.
```bash
docker run -v $HOME/.github:/root/.github -v $HOME/.dollar:/root/.dollar -v $(pwd):/build -it sillelien/dollarscript-headless:0.4  <filename>.ds
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

-------

**If you use this project please consider giving us a star on [GitHub](http://github.com/sillelien/dollar).**

Please contact me through Gitter (chat) or through GitHub Issues.

[![GitHub Issues](https://img.shields.io/github/issues/sillelien/dollar.svg)](https://github.com/sillelien/dollar/issues) [![Join the chat at https://gitter.im/sillelien/dollar](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sillelien/dollar?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

For commercial support please <a href="mailto:hello@neilellis.me">contact me directly</a>.
-------

--------

# 💰 Referral Links

This is an open source project, which means that we are giving our time to you for free. However like yourselves, we do have bills to pay. Please consider visiting some of these excellent services, they are not junk we can assure you, all services we would or do use ourselves.

[Really Excellent Dedicated Servers from Limestone Networks](http://www.limestonenetworks.com/?utm_campaign=rwreferrer&utm_medium=affiliate&utm_source=RFR16798) - fantastic service, great price.

[Low Cost and High Quality Cloud Hosting from Digital Ocean](https://www.digitalocean.com/?refcode=7b4639fc8194) - truly awesome service.

# 👮 Copyright and License

[![GitHub License](https://img.shields.io/github/license/sillelien/dollar.svg)](https://raw.githubusercontent.com/sillelien/dollar/master/LICENSE)

(c) 2014-2017 Neil Ellis all rights reserved. Please see [LICENSE](https://raw.githubusercontent.com/sillelien/dollar/master/LICENSE) for license details of this project. Please visit http://neilellis.me for help and raise issues on [GitHub](https://github.com/sillelien/dollar/issues).

For commercial support please ✉ <a href="mailto:hello@neilellis.me">contact me directly</a>.

<div width="100%" align="right">
<img>
</div>
