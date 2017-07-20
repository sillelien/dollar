
[ ![Binary Distribution](download.png)](https://bintray.com/sillelien/binary/download_file?file_path=dollar-${RELEASE}.tgz)

Distribution:[ ![Binary Distribution](https://api.bintray.com/packages/sillelien/binary/dollar/images/download.svg) ](https://bintray.com/sillelien/binary/dollar/_latestVersion)

Headless Docker Image: [ ![Download](https://api.bintray.com/packages/sillelien/docker/dollarscript-headless/images/download.svg) ](https://bintray.com/sillelien/docker/dollarscript-headless/_latestVersion)

```bash
alias dollar="docker run sillelien/dollarscript-headless:${RELEASE}"
 ```

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
            <version>${RELEASE}</version>
        </dependency>
```


Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ae285534ff3e2204000002)

Please check out http://sillelien.github.io/dollar for latest details.
