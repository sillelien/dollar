#Getting Started with Dollar
##Words of Caution
Dollar is in an experimental state, all the APIs and language are subject to changes without warning. The intention is to stabilize the language and API once sufficient feedback and experience has been gained by other tinkerers.
##Installation
###Runtime
At this stage I recommend downloading the full DollarScript runtime from bintray [ ![Download](https://api.bintray.com/packages/neilellis/dollar/dollar-runtime-osx/images/download.svg) ](https://bintray.com/neilellis/dollar/dollar-runtime-osx/_latestVersion). This only runs on Mac OS X at present, although it is trivial to port to other platforms. I'm aware the runtime is pretty darn heavy at 144MB - but for now I'd like us all to be using the same JDK and for you all to be able to just download and run. At a later date expect better modularization and more options.

When you've downloaded the binary just untar it and put it somewhere useful.

Note: Dollar is based on JDK 1.8

##Running DollarScript
Make sure your PATH variable includes the `bin` directory within the `dollar` directory. Then just create a file like this:

```dollar

```

```bash
dollar test.ds
```
