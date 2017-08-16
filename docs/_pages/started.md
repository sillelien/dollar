---
layout: page
title:  "Getting Started"
---

# Getting Started with Dollar

## Words of Caution
Dollar is in an experimental state, all the APIs and language are subject to changes without warning. The intention is to stabilize the language and API once sufficient feedback and experience has been gained by other tinkerers.

## Installation

### Runtime

At this stage I recommend downloading the full DollarScript runtime [distribution](http://dollarscript.s3-website-eu-west-1.amazonaws.com/dist/dollar-{{site.release}}.tgz). This only runs on Ubuntu / Mac OS X at present, although it is trivial to port to other platforms. I'm aware the runtime is pretty darn heavy. At a later date expect better modularization and more options.

When you've downloaded the binary just untar it and put it somewhere useful.

Note: Dollar is based on JDK 1.8

## Running DollarScript

Make sure your PATH variable includes the `bin` directory within the `dollar` directory. Then just create a file called `colors.ds` containing this:

{% highlight bash %}
j *= random:dummy
colors= ["red","green","blue"]
k := colors[j * #colors]
{% endhighlight %}

And run it:

{% highlight bash %}
dollar colors.ds
{% endhighlight %}

And that's it, make sure you read the full manual before continuing.

