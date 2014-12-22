---
layout: post
comments: true
title:  "A heuristic statistical approach to a type system for a dynamic language"
date:   2014-12-22 11:23:00
categories: dollar socketio type type-safety predictive
---

A common complaint about dynamic languages is the lack of type safety, and that the only solution is a complete formal one. Interestingly there really does seem to be a very binary view about type systems. So let's get to the bottom of that before we discuss the heuristic approach.

Static typing is largely a good thing because it helps us to spot bugs before our code goes into production and reduces (in theory) the amount of unit testing required to find bugs. The received wisdom is that unless you use a formally proven post-doctorate level type system then your code is sloppy and weak. In reality this is a super complex subject within a huge terrain of possible solutions and that many answers are appropriate for many different situations.

The problem with the simple binary thinking is that it oftend does not actually allow for the extra required work to maintain a viable type system and therefore to get it's rewards. This extra effort if used in systems that require minimal type safety can cause less experienced developers to try and throw the baby out with the bathwater - and stick to only using languages with a very loose type system. I think this somewhat illusory dichotomy is better understood with the metaphor of real world contracts.

If I lend my friend £20 there is an implied contract that I she will return that money by her next paycheck. If she doesn't then my trust is weakened but hey I'm only down £20.  However if I then went to court over my £20 (like I would!) I have nothing but my word to rely upon. This is the level of type safety we would find in, say, JavaScript. Most of the time it works - there's little consequence (in the browser!) if it doesn't and it's a quick simple friendly solution that doesn't slow people's lives down.

Now let's go to the other extreme.

I am writing a contract between two multinational corporations for about 3 squidgillion dollars. If I make a mistake my job, career and possibly the jobs and careers at my entire company could be at stake. So I write a contract with a team of a 50 lawyers and produce a 200 page contract which absolutely nobody but a corporate lawyer understands. However there is pretty much no way anyone could wriggle out of that contract. This is the land of formal languages and languages with advanced type systems like Haskell.

Now what interests me is that there is a *huge* gap in between, a vast spectrum of possibilities. Contracts for car hire, EULAs, privacy policies, employment contracts and so forth. In these cases I do want some formality to protect myself - because these people aren't close friends that I trust and the consequences can be expensive. However they need to legible to both parties without a team of 50 specialist lawyers because, hey we have other things to do in our lives.

Each language, each project and each developer puts there stake in the ground as to how much or little type safety they want. For wiser, older developers this is usually an educated trade off - for the rest it's a flame war over languages and religious technical beliefs.

So where does [DollarScript](http://neilellis.github.io/dollar) lie on this continuum? Well I'm taking a fairly radical approach and allowing the type system to learn from experience.

Heh?

DollarScript learns from runtime usage what types are usually returned from expressions, modules, resources etc. and then warns you if your code depends on a type that in all probability is wrong. The massive advantage of this approach is that you get a lot of the type safety of a statically typed language without the constant fussing around with importing type declarations from external resources. It also means that type safety can be applied in places that a static type system cannot calculate the type - i.e. from an external service.

We're not enforcing a type system, just warning you if you appear to be breaching it. This gives 98% of the result for 2% of the effort.

To go back to our analogy - this is saying that our contracts are based on what other people are currently using as contracts. We're taking a calculated risk that those borrowed contracts may be flawed but we're getting the vast majority of the safety for a fraction of the cost/effort.

So here is this in action:

{% highlight bash %}
i= "1+2"
// this is never executed but we will get a type warning for it anyway
// during the initial parse phase. The error will be 'Type assertion may fail,
// expected INTEGER most likely type is STRING (100%)'.

if(false) {
    <Integer> c= 3 + i
}

{% endhighlight %}

The above example is really too over simplified to be useful (better ones to come) but it does illustrate the principle that static analysis is being performed - and that analysis is based on past experience. In this case the experience of what happens when a String is added to an Integer. The `<Integer>` represents a type assertion during assignment.

Of course the above example could easily be hardcoded into the language, but what if `i` came from an external service? Or a function from a separate module? Without a reasonable degree of type annotation the type system would quickly weaken. DollarScript avoids this by using a statistical approach to predicting the type from previous runtime usage.

If I can emphasize this a little further, a type system is like the [Travelling Salesperson Problem](http://en.wikipedia.org/wiki/Travelling_salesman_problem), that is, a complete formal solution with 100% accuracy is extremely (in that case NP) hard, yet the 98% solution is relatively fast and simple to achieve.

The actual prediction mechanism in DollarScript is pluggable - allowing for anything from simple statistics to machine learning to be used to increase the accuracy of predictions. Naturally in later versions of DollarScript this mechanism will be switched off outside of development environments.

I know this an incredibly brief introduction - but I really wanted to get some early thoughts and feedback (please no religious feedback, thanks - your chosen programming language is great - keep using it!).

