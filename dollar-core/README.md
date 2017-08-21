${HEADER}

# The Dollar Core API ${STATE_BETA}

Dollar helps you write dynamic JavaScript-like code from the safety of Java. It provides a new type `var` to use in your Java coding. `var` acts much in the same way as a JavaScript type, i.e. it is highly dynamic. There is a lot to the Dollar framework, of which this is the core project, so best to get started just understanding how you can write dynamic code in Java.

To get started you'll need this repository:


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

and this dependency

```xml
        <dependency>
            <groupId>com.sillelien</groupId>
            <artifactId>dollar-core</artifactId>
            <version>${RELEASE}</version>
        </dependency>
```

[![Dependency Status](https://www.versioneye.com/user/projects/55bf9093653762001700287e/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/55bf9093653762001700287e)

${BLURB}

# Getting Started

Every example you see below is *Java* I emphasize that as it may not look familiar to you, that is intentional - I have done my best to make it clear that you are working with **untyped** objects, to avoid confusion.

All static methods such as `$()` can be accessed by importing `import static com.sillelien.dollar.api.DollarStatic.*;`


## Creating objects

Let's start with our `Hello World` example.

```java

    $("Hello World");

```

What we've done here is create an object of type `var`. `var` objects have an underlying type, much like JavaScript objects, however you can apply many operations without concern for it's type. So let's assign a variable.

```java

    var myObject= $("Hello World");

```

Most `var` objects are immutable, any changes you make create a new `var` object, the exceptions are queues and URIs, but more of them later. This is the *most important thing to remember*, you must use the results of a mutation to a data object - the original object was not mutated. For example


```java

    var myObject= $("Hello World");
    myObject.$append($("Goodbye"));
    assert myObject.equalsString("Hello World");
    
```

The original object is unchanged by the `$append()` call, so instead we do this:

```java

    var myObject= $("Hello World");
    var newObject= myObject.$append($("Goodbye"));
    assert ! newObject.equalsString("Hello World");
    
```

## Creating Lists and Maps

Creating a list is as simple as using the `$list()` static method which takes a list of any type of object

```java

    var myList=$list(1,2,3,"four");
    var second=myList.$(1);
    assert second.toInteger() == 2;
    
```  

If we place Pairs (Pairs are defined as simply a Map with a single entry) together using the $map() method we can also create maps

```java

    var map =$map(
                    $("one",1),
                    $("two",2)
            );
   assert map.equalsString("{\"one\":1,\"two\":2}");
           
```  

For shorthand we can also overload the $() method:

```java

    var map =$(
                    $("one",1),
                    $("two",2)
            );
   assert map.equalsString("{\"one\":1,\"two\":2}");
             
```  

## Working with Lists

### Querying a list

Dollar supports the basic list operations. To get a member at a position use `.$()` or `.$get()`, for example:

```java

    var list= $list(0,1,2,3,4);
    assert list.$(3).I() == 3;
    assert list.$(3).toInteger() == 3;
    
```

To see if a list contains a value use `$contains()`

```java

    var list= $list(0,1,2,3,4);
    assert list.$contains($(3)).isTrue();
    assert list.$contains(3).isTrue();
    assert list.contains($(3));
    assert list.contains(3);

```

To find the size of a list use `$size()` or `size()`

```java
    var list= $list(0,1,2,3,4);
    assert list.$size().toInteger() == 5;
    assert list.size() == 5;

```

And to see if it's empty:

```java

    var list= $list(0,1,2,3,4);
    assert ! list.isEmpty();

```

### Iteration and Streams



```java

    var list= DollarStatic.$list(0,1,2,3,4);
    java.util.concurrent.atomic.AtomicInteger count= new java.util.concurrent.atomic.AtomicInteger();
    list.$each((e)->{count.incrementAndGet();return e[0];});
    assert count.intValue() == 5;

```


### Modifying a list



To add to the list use either `$append()` to add at the end,  `$prepend()` to insert at the beginning or `$insert()` to insert at any position. **But remember data `var` objects are immutable, so you must use the result of the method**.

```java

    var list= $list("red", "blue");

    var insertList= list.$insert($("green"),1);
    assert insertList.toString().equals("[ \"red\", \"green\", \"blue\" ]");

    var appendList= list.$append($("green"));
    assert appendList.toString().equals("[ \"red\", \"blue\", \"green\" ]");

    var prependList= list.$prepend($("green"));
    assert prependList.toString().equals("[ \"green\", \"red\", \"blue\" ]");


```

Items can be removed using `remove()` or `$remove()`

```java

    var list= $list("red", "green", "blue");

    var removeList= list.$remove($("green"));
    removeList.err();
    assert removeList.equalsString("[ \"red\", \"blue\" ]");


```

Lists can be converted to maps using `$map`, the generated keys will be numeric position in the list of the value starting with index 0:

```java

    var list= $list("red", "green", "blue");
    assert list.$map().$("0").equalsString("red");
    assert list.toVarMap().get($(0)).equals("red");
    

```


## Working with maps

To retrieve a value from a map, just use the `$()` method with a single value. If you prefer a more verbose syntax then just use `$get()` instead.

```java

    var map= $(
                    $("name", "Neil"),
                    $("address",
                            $("street_number", 343),
                            $("town", "Brighton")
                    )
             );

     assert map.$("name").equalsString("Neil");
     assert map.$("address").$("town").equalsString("Brighton");


```

To find out if the map has a key value then use `$has()`, `$containsKey()` or `containsKey()`..

```java
    var map= $(
                    $("name", "Neil"),
                    $("address",
                            $("street_number", 343),
                            $("town", "Brighton")
                    )
             );

    assert map.$containsKey("name").isTrue();
    assert map.containsKey("name");
    assert map.$has("name").isTrue();

```

Naturally you can also use the `$containsValue()` and `containsValue()` methods.

```java
    var map= $(
                    $("name", "Neil"),
                    $("address",
                            $("street_number", 343),
                            $("town", "Brighton")
                    )
             );

    assert map.$containsValue("Neil").isTrue();
    assert map.containsValue("Neil");

```


### Modifying a map

To add to the map you can use the `$(key,value)` or `$set()` methods.

```java
    var map= $(
                    $("name", "Neil"),
                    $("address",
                            $("street_number", 343),
                            $("town", "Brighton")
                    )
             );

    var newMap= map.$("gender","male");
    
    assert newMap.$containsKey("gender").isTrue();
    assert newMap.containsKey("gender");
    assert newMap.$("gender").equalsString("male");

```



Items can be removed using `remove()` or `$remove()`

```java

    var map= $(
                    $("name", "Neil"),
                    $("address",
                            $("street_number", 343),
                            $("town", "Brighton")
                    )
             );

    var newMap= map.$remove("name");
    assert newMap.$containsValue("Neil").isFalse();
    assert ! newMap.containsKey("name");


```

## Queues

**Note: Queues are not a stable feature yet**

Queues are an important special case. They are important because they are the way that you should pass `var` objects between threads of execution. Queues can also be thought of as a special case of URIs.

```
    var queue = $blockingQueue();
    queue.$push($("Hello World"));
    assert queue.size() == 1;
    
```


# Reference

## Dollar Methods

You will hopefully notice the pattern that any method that deals solely with var objects has a `$` symbol preceeding it. Methods that return or work with other Java objects should not have that symbol.

## Factory Methods

To use the factory methods to create `var` objects you just need to statically import DollarStatic.*

### $(Object)

This constructor will attempt to create a `var` object from whatever is passed in, the following are the types that Dollar recognizes and the implementation type created.

* null - DollarVoid

* Boolean - DollarBoolean

* Pipeable - DollarLambda

* Long, Integer, Short - DollarInteger

* Double, BigDecimal, Float - DollarDecimal

* File, String, InputStream - DollarString

* String (Json Array), JsonArray, JSONArray, ArrayNode, ImmutableList, List, Collection, Array - DollarList

* String (Json Object), JsonObject, JSONObject, ObjectNode,  MultiMap, ImmutableJsonObject, Map - DollarMap

* URI - DollarURI

* Date, LocalDateTime, Instant - DollarDate

* Range - DollarRange

* InputStream - DollarStream


Otherwise Dollar will attempt to convert the Java object to a JsonObject and then into a DollarMap.


### $void()

This creates a void `var` (the implementation class is DollarVoid). Voids, unlike nulls don't have any characteristics, including type. They literally represent nothing.


### $null(Type)

This creates a null `var` which has a type of that specified in the factory method.


### $range(from,to)

This will create a Range `var`.


### $uri(URI)

This will create a URI `var`.


### $uri(String)

This will create a URI `var`.


### $date(Date)

This will create a date `var`.


### $date(LocalDateTime)

This will create a date `var`.


### $string(String)

This will create a string `var` object without any attempt to parse the string.


### $list(List)

This will parse a YAML file into a `var` object.


### $yaml(File)

This will parse a YAML file into a `var` object.


### $yaml(String)

This will parse a YAML string into a `var` object.


### $(lambda)

This will create a delayed evaluation `var` object.


## Collection style methods on `var`

### $(String) or $get(String)

Returns a var object that corresponds to the string supplied. Usually this means a map member keyed by this value.

### $append(var)

Adds the supplied var to a copy of this object, usually that means adding a member to list. Note var objects are immutable.

### $prepend(var)

Inserts the supplied var at the beginning of a copy of this object, usually that means prepending a member to a list. Note var objects are immutable.

### $contains(var) / $containsValue(var)

Returns a Boolean var which will equate to `true` if the `var` supplied is contained within the current `var`.

### $has(String) / $has(var)

Returns true if the supplied key is within this `var`.

### $size() / size()

Returns the size as either a `var` or an int.

### $(String, Object) $set(String, Object) $set(var, Object)

Creates a new version of the var object with the child set to the value supplied. This is pretty much the same as jQuery.


### $removeByKey(String) 

Removes the `var` object identified with the supplied key from this `var` object.

### remove(Object) $remove(var)

Removes the object supplied from this object.

## Other Methods on `var`


### $mimeType 

Returns a valid Mime Type for this object.

## Type conversion/modification methods

### $as(Type)

Attempt to cast this type to the specificed Type.


### $split()

Convert this object into a single var list object.


### $list()

Convert this object into an immutable Java list of var objects.

### $map()

Convert this object into an immutable Java map of var objects.

### $yaml()

Convert this object into it's YAML string equivalent.

### $pairKey()

Treat this object as a Pair (single entry map) and get the Pair's key.

### $pairValue()

Treat this object as a Pair (single entry map) and get the Pair's value.

### toList()

Convert this to an immutable Java List.

### toStrings()

Convert this to an immutable list of Java strings.

### toMap()

Convert this to a Java Map.

### toStream()

Convert this to an Input Stream.

## Type enquiry methods

### type()

Returns the definitive type of this object, this will trigger execution in dynamic values.

### is(Type)

Returns true if this object is of the supplied Type.

### collection()

Returns true if this object is a collection.

### dynamic() 

Returns true if this object is dynamically evaluated.

### infinite()

Returns true if this object is infinite in value.

### isError()

Returns true if this object represents an error.

### isNull()

Returns true if this object represents a typeable null value.

### isVoid()

Returns true if this object represents an untyped valueless void.

### list()

Returns true if this object is a list.

### pair()

Returns true if this is a pair (a single valued map).

### map()

Returns true if this is a map

### string()

Returns true if this is a string.

### number()

Returns true if this is a number (decimal|integer).

### decimal()

Returns true if this is a decimal.

### range()

Returns true if this is a range.

### uri()

Returns true if this is a URI.

### singleValue()

Returns true if this is a single value, note that void is neither a single value or a collection.

## Error handling methods on `var`

Error handling in Dollar can be either fail fast or fail slow. If it is fail fast then these methods will likely trigger the throwing of an exception. If it is fail slow then they instead will return error objects.

### $error(String)

Raise or return an error.

### $error(Throwable)

Raise or return an error.

### $error(String, ErrorType)

Raise or return an error of the specified typpe and associated message.

### $error()

Raise or return an error.

### $invalid(String)

Raise or return a validation error.

### $errors() 

Return this object's errors as a `var` object

### errors()

Returns this object's errors as an immutable list of exceptions.

### errorTexts()

Returns this object's errors as a list of Strings.

### $fail(Consumer)

If this object has Java exceptions associated with it, then execute the Consumer with those exceptions.

### hasErrors()

Returns true if this object has Java exceptions associated with it.


### clearErrors()

Return a copy of this object minus any associated exceptions.





## FAQ

### Why Dollar ?

Once you've written some Dollar code you'll get the reason pretty quickly.

### Is this like jQuery for Java?

No, but Dollar uses a lot of ideas that jQuery popularized, so I would certainly acknowledge our debt to jQuery.

### Then what is it?

If you like the ease of JavaScript, Ruby, Groovy etc. but also enjoy being able to work within the Java language then this is for you. You can write typesafe code and then drop into typeless Dollar code whenever you need to. Dollar is both an alternative paradigm and a complementary resource.


## Characteristics

Dollar is designed for production, it is designed for code you are going to have to fix. Every library and language has it's sweet spot. Dollar's sweetspot is working with schema-less data in a production environment. It is not designed for high performance systems (there is a 99.9% chance your project isn't a high performance system) but there is no reason to expect it to be slow either. Where possible the code has been written with JVM optimization in mind.

With this in mind the following are Dollar's characteristics:

* Simple - Dollar does do not expose unnecessary complexity to the programmer, we keep it hidden.

    > The secret of success is to be like a duck – smooth and unruffled on top, but paddling furiously underneath.”

* Typeless - if you *need* strongly typed code stop reading now. If you're writing internet centric modest sized software this is unlikely to be the case.
* Synchronous - asynchronous flows are hard to follow and even harder to debug in production. We do not expose asynchronous behaviour to the programmer.
* Metered - key execution's are metered using Coda Hale's metrics library, this makes production monitoring and debugging easier.
* Nullsafe - Special null type reduces null pointer exceptions, which can be replaced by an isNull() check.
* Threadsafe - No shared state, always copy on write. No shared state means avoidance of synchronization primitives, reduces memory leaks and generally leaves you feeling happier. It comes at a cost (object creation) but that cost is an acceptable cost as far as Dollar is concerned.



## Badges
Build Status: [![Circle CI](https://circleci.com/gh/sillelien/dollar-core.svg?style=svg)](https://circleci.com/gh/sillelien/dollar-core)

Chat: [![Gitter](https://badges.gitter.im/Join+Chat.svg)](https://gitter.im/sillelien/dollar-core?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Waffle Stories: [![Stories in Ready](https://badge.waffle.io/sillelien/dollar-core.png?label=ready&title=Ready)](https://waffle.io/sillelien/dollar-core)

Dependencies: [![Dependency Status](https://www.versioneye.com/user/projects/55bf9094653762001a002527/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55bf9094653762001a002527)

${FOOTER}
