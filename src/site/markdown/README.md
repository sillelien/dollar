dollar_vertx
============

Making JSON easier for Vert.x

Install using Maven

        <dependency>
            <groupId>com.cazcade</groupId>
            <artifactId>dollar-vertx</artifactId>
            <version>0.0.10</version>
        </dependency>


Example
=======

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

Or [ ![Download](https://api.bintray.com/packages/cazcade/maven/dollar_vertx/images/download.png) ](https://bintray.com/cazcade/maven/dollar_vertx/_latestVersion)

#[![Circle CI](https://circleci.com/gh/cazcade/dollar_vertx/tree/master.png?style=badge)](https://circleci.com/gh/cazcade/dollar_vertx/tree/master)
