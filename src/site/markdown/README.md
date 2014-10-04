dollar
======

Making Java dynamic and easy to use.

Install using Maven

        <dependency>
            <groupId>com.cazcade</groupId>
            <artifactId>dollar-vertx</artifactId>
            <version>0.0.16</version>
        </dependency>


Examples
========

Basic
-----

        int age = new Date().getYear() + 1900 - 1970;
        $ profile = $("name", "Neil")
                .$("age", age)
                .$("gender", "male")
                .$("projects", $array("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        assertEquals(age /11, (int)profile.$("$['age']/11").$int());
        assertEquals("male", profile.$("$.gender").$());
        assertEquals(10, profile.$("5*2").$());
        assertEquals(10, (int)$eval("10").$int());
        assertEquals($("{\"name\":\"Dave\"}").$("name").$$(),"Dave");
        assertEquals($().$("({name:'Dave'})").$("name").$$(), "Dave");

Webserver
---------

Create a webserver at http://localhost:4567/headers and return the request headers as a JSON string.

        GET("/headers", (context) -> context.headers());

Characteristics
===============

Dollar is designed for production, it is designed for code you are going to have to fix. Every library and language has it's sweet spot. Dollar's sweetspot is working with schema-less data in a production environment. It is not designed for high performance systems (there is a 99.9% chance your project isn't a high performance system) but there is no reason to expect it to be slow either. Where possible the code has been written with JVM optimization in mind.

With this in mind the following are Dollar's characteristics:

* Typeless - if you *need* strongly typed code stop reading now. If you're writing internet centric modest sized software this is unlikely to be the case.
* Synchronous - asynchronous flows are hard to follow and even harder to debug in production. We do not expose asynchronous behaviour to the programmer.
* Metered - key execution's are metered using Coda Hale's metrics library, this makes production monitoring and debugging easier.
* Nullsafe - Special null type reduces null pointer exceptions, which can be replaced by an isNull() check.
* Threadsafe - No shared state, always copy on write. No shared state means avoidance of synchronization primitives, reduces memory leaks and generally leaves you feeling happier. It comes at a cost (object creation) but that cost is an acceptable cost as far as Dollar is concerned.

The Rules
=========

1. Do not create your own Threads.
2. Do not create your own Threads.
3. Always run from a *static* context (e.g. a public static void main method)
4. All $ objects are immutable, so use the returned value after 'mutation' actions.

Or [ ![Download](https://api.bintray.com/packages/cazcade/maven/dollar/images/download.png) ](https://bintray.com/cazcade/maven/dollar/_latestVersion)

#[![Circle CI](https://circleci.com/gh/cazcade/dollar/tree/master.png?style=badge)](https://circleci.com/gh/cazcade/dollar/tree/master)
