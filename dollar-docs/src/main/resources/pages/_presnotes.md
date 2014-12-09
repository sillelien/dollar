footer: © Unsigned Integer UG, 2014
slidenumbers: true

# Deckset Basics

### Everything you need to know to start making presentations

---

# Built on Markdown

Deckset uses standard Markdown syntax to transform your thoughts into a beautiful presentation.

This slide is one of the simplest types of slides you can create, it contains two paragraphs of text and a header.

---

# Creating slides

A new slide is defined by three dashes `‘---’` typed on a single line, with an empty line above and below.

	Like

	---

	So

---

# Paragraphs

Creating paragraphs is simple, just type — no special syntax needed.

You can include a paragraph break by leaving an empty line between the paragraphs.
Otherwise lines will follow on directly like this.

---

# Headers

Headers are created by including a # before the text:

`# This is a header `.

Deckset has four different heading sizes. You can change size by simply adding #, ##, ### or #### before your heading.


---

# Lists

5. Ordered lists
6. Type `‘1. ’` before your text
7. Your list items will begin with a number

- Unordered lists
- Type `‘-’` or `‘*’` or `‘+’` before your text
- Your list items will begin with a bullet

---

# Emphasis

Use **strong**, _emphasis_ or a combination of **_both_** to make your point stand out.

**strong** text by wrapping words in double asterisks or underlines `‘**like this**’` or `‘__this__’`. *Emphasis* is added by wrapping words in single asterisks or underlines `‘*like this*’` or `‘_this_’`. Combine a single with a double for both effects at the same time.


---

# Links

Create links to any external resource—like [a website](http://www.decksetapp.com)—by wrapping link text in square brackets, followed immediately by a set of regular parentheses containing the URL where you want the link to point:

`‘[a website](http://www.decksetapp.com)’`

Your links will be clickable in exported PDFs as well!


---


# Code samples

Wrap your code with three backticks and specify the language for automatic syntax highlighting.

```objectivec
UIView *someView = [[UIView alloc] init];
NSString *string = @"some string that is fairly long, with many words";
```

We scale the text dynamically so it always looks great. You can also use a single indent to switch to a monospace font.


---

# Images

The simplest way to get images into your presentation is to drop a local or web image onto the Deckset window — the markdown is automatically created and copied to your clipboard.

You can also add images with the following syntax:

`‘![](your image.jpg)’`


---

![](red.jpg)

---

## If you use text and images together, the image is filtered so the text is always readable.

![](red.jpg)

---

### Take a look at the ‘Working with images’ example presentation for a complete overview of what you can do with images in Deckset.

![right](plant.jpg)

---

## Videos can be included too, either as local files or YouTube links.

![autoplay](water.mov)

---

# Quotes

All slides containing a single quote have special formatting for extra impact.

Add `‘>’` in front of every quote line
And add `‘--’` for the quote author reference

---

> The best way to predict the future is to invent it
-- Alan Kay

---

# Footers and Slide Numbers

Include persistent custom footers and/or running slide numbers by using directives:

```
footer: © Unsigned Integer UG, 2014
slidenumbers: true
```

Make sure the two directives start on the *first line* of your markdown file, and ensure there are *no empty lines* between the two.


---

# Speaker notes

Add speaker notes to any slide by adding `‘^’` before your notes. Write as much as you like, all notes will be scaled to fit in the display.

^ This is what speaker notes look like when you are presenting with a external display, or practicing in rehearsal mode.

---

![](presenter.jpg)

---

# Rehearsal mode

Choose _Rehearsal Slideshow_ from the _View_ menu to run through your presentation and see how it will work on the day.

---

# Aspect Ratios

Easily swap between _16:9_ and _4:3_ in the _Presentation_ menu to suit whichever projector or screen you are using.

---

# More control with a little HTML

If you really must tweak line breaks, you can use the `<br/>` tag to split any line of text.
