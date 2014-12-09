footer: © Neil Ellis, All rights reserved, 2014
slidenumbers: true

# DollarScript

### An Internet centric language for the JVM

^This is a presentation that can best be viewed using [Deckset](http://www.decksetapp.com)

![](images/architecture-3014.jpg)

---

#What Dollar is

+ Dynamic
+ Weakly Typed
+ Supports Reactive Programming
+ Has Functional Idioms

![](images/clouds-cloudy-fog-2956.jpg)

---

#What it isn't
+ Complex
+ Over Engineered
+ Too Verbose
+ Or Too Concise

![](images/berlin-blocks-germany-1915.jpg)

---

>  And it's not Java!

![](images/beans-coffee-kitchen-2742.jpg)

---

#Cool Features
+ Reactive
+ Lazy Evaluation (It's all Lambdas)
+ Pluggable Module System
+ URI Addressable Resources

![](images/beach-black-and-white-desert-1569.jpg)

---

#What it's Good For
+ Small to Medium Projects
+ Server side applications
+ API centric applications
+ High level functionality
+ Quick prototypes

![](images/bird-black-and-white-clouds-689.jpg)

---

#What it's Bad For
+ Engineering - Use Java/Kotlin
+ Large Projects - Use Java/Kotlin
+ Large Development Teams - Use Java/Kotlin
+ Nuclear power stations

![](images/black-and-white-city-skyline-2255.jpg)

---

#This is what Dollar looks like

```dollar
geo= read http://freegeoip.net/json/
.: geo is Map

lat= geo.latitude
lon= geo.longitude

month= (DATE()-30)['MONTH_OF_YEAR']
year= (DATE()-30)['YEAR_OF_ERA']
crimes= read (("http://data.police.uk/api/crimes-at-location?date=" + year + "-" + month + "&lat=" + lat + "&lng=" + lon) as Uri)
categories= crimes each { $1.category }
.: categories is List

if (~ categories["anti-social-behaviour"]) {
  @@ "Anti-Social behaviour in your area last month!"
} else {
  @@ "No anti-social behaviour in your area last month!"
}

"The End!"
```

