---
layout: post
title:  "Building an Optimizing Javac Plugin"
date:   2018-06-02 23:28:50 -0400
comments: true
categories: java compiler javac-plugin
group: javac-plugin
published: false
---

The release of Java 8 brought us the Compiler Plugin.  This solves a long
standing problem that was previously dealt with via a rather ugly hack--using
annotation processors. The Compiler Plugin allows us to get into the innards of
the compiler and update the compilation process.

In what follows I will outline two basic compiler optimizations, one working on
the AST and one working on generated bytecode. This post will focus on the AST
optimization and the next post will add to this one, optimizing generated
bytecode. Since this is a rather long post it might make sense to dig around the
codebase first which can be found [on my Github][my Github].  I have marked
different phases of this posting under different releases. In particular, you
can find:

- [The code for the first section](https://github.com/bkushigian/javac-optimization-plugin/tree/30a628c5477c9d186cb43a2e5b8aeb1dda941061)
  at release `basic-plugin-works`
- [The completed code for this post](https://github.com/bkushigian/javac-optimization-plugin/tree/ad34f6dd3e1c7dc4e9f9a52759c5737dda529341)
  at release `foldr-works`


Before we begin, let's look at the first motivating problem.

## The Problem: Right Constant Folding

Consider the following two methods:

{% highlight java %}
public class TestClass { 
  public int addOneTwoArg(int a) {
    return 1 + 2 + a; 
  }
  public int addArgOneTwo(int a) {
    return a + 1 + 2; 
  } 
}
{% endhighlight %}

After compiling these they have generated bytecode:

{% highlight asm %}
Compiled from "TestClass.java" 
public class TestClass { 
  public int addOneTwoArg(int); 
    Code: 
       0: iconst_3 
       1: iload_1 
       2: iadd 
       3: ireturn 
  public int addArgOneTwo(int);
    Code: 
       0: iload_1
       1: iconst_1 
       2: iadd 
       3: iconst_2 
       4: iadd 
       5: ireturn 
}
{% endhighlight %}

An interesting thing is happening here. If we consider the expression `1 + 2 +
a` then the Java compiler simplifies it to `3 + a`: this optimization is called
*constant folding* and is a very common compiler optimization. However when we
consider `a + 1 + 2`, the compiler does not simplify it. Why is that? It turns
out that the [abstract syntax tree][wiki-ast] that Java builds when parsing
leans to the left, so that they have respective ASTs:

```
             +          +
            / \        / \
           +   a      +   2
          / \        / \
         1   2      a   1
```

which we will write inline as `(+ (+ 1 2) a)` and `(+ (+ a 1) 2)` respectively.
The java compiler can easily see that `(+ 1 2)` is equal to `3` and it
simplifies for us, while it is not quite smart enough to reason about the other
tree. In this post we will make Javac a bit smarter. To do this we will build a
plugin for the compiler which will let us tinker with the compilation pipeline.

## Creating a Basic Java Compiler Plugin

Here's the skinny: we want to execute arbitrary code during compilation with
full access to the compiler's innards by running something stupidly simple like
`javac --use-plugin class/to/compile`. In this section we will get that
set up. 

This might take a little tinkering, especially if you aren't familiar with the
`ServiceLoader` (docs [here][service loader docs]). There is nothing *super*
difficult but there are some magic motions you have to go through that will seem
mysterious if you haven't encountered them before.

Also, make sure you have added the JDK's `tools.jar` dependency to your
project---if you need help there are lots of resources out on the interwebz.

### Project Structure
Here is the basic structure I am using but you can tweak to fit your needs or
just download the source code---I've tagged the repo `basic-plugin-working` for
the code in this section (that is, up through the **Working With ASTs**
subsection).

```
optimizing-plugin/
    src/
        com/tangentiallyrelated/plugin/
            OptimizationPlugin.java
            OptimizationTaskListener.java
            ConstFoldTreeScanner.java
        META-INF/services/
            com.sun.source.util.Plugin
    resources/classes/
        TestClass.java
```

* Our source code lives in `src/com/tangentiallyrelated/plugin`. 
  - Note the file `src/META-INF/services`. The contents of this will be the
    fully qualified name of your plugin---in my case, that is
    `com.tangentiallyrelated.plugin.OptimizingPlugin`
  - `OptimizationPlugin.java`: this is our plugin (duh) and it is pretty simple
    (see below). All it does is register a task listener to be called back to
    when a compilation phase starts or ends.
  - `OptimizationTaskListener.java`: The task listener waits for the compiler to
    tell it that some sort of compilation event has happened (for example, if
    parsing just finished) and is the entrypoint into our code. Basically, just
    the [Observer Pattern][observer pattern]
  - `ConstFoldTreeScanner.java`: If you know about compiler design then you know
    that the first phase (lexing/parsing) creates an *abstract syntax tree*
    (AST). A `TreeScanner` is used to traverse the AST and do what needs doing.
    Ours will go through and fold any constants that need folding.


### Basic Implementation
We will implement the [`com.sun.tools.util.Plugin`][javac-plugin-docs] interface
which specifies two methods: `getName()` and `init(JavacTask task, String ...
args)`. To get this we have to add a dependency to JDK's `tools.jar`. The sum
total responsibility of this plugin is to register a TaskListener with the
compiler that will sit there and wait for the right part of the compilation to
begin or end to do its job. In this section we will do just enough to test that
we are getting compilation information from the compiler.

{% highlight java%}

public class OptimizationPlugin implements Plugin {

    @Override
    public String getName() {
        return "OptimizationPlugin";
    }

    @Override
    public void init(JavacTask task, String... args) {
        task.addTaskListener(new OptimizationTaskListener());
    }
}
{% endhighlight %}

A couple notes: 
* First, double check `getName()`---this *has* to return the same name as what
  you specify via command line (details below) and can cause some frustrating
  behavior if there is a typo.
* We haven't implemented the `OptimizationTaskListener` yet, so let's go ahead
  and do that now.

{% highlight java%}
public class OptimizationTaskListener implements TaskListener {
    public OptimizationTaskListener() {
    }

    @Override
    public void started(TaskEvent e) {}

    @Override
    public void finished(TaskEvent e) {
        if (e.getKind() == TaskEvent.Kind.PARSE){
            // TODO: optimize!
            System.out.println("Task event " + e + " has ended");
        }
    }
}
{% endhighlight %}

Whenever a compilation phase is started the compiler will notify any registered
task listener `t` via `t.started(event)`, where `event` describes which phase is
starting up. Likewise, when a phase finishes `t.finished(event)` is called. At this
point `t` has access to the compilation innards via the `event` argument that is
passed in. In our example we have interrupted the compiler just after the
`PARSE` phase ended. This means we can make some last minute modifications to
the AST. But first, let's ensure that this is working properly.


### Building and Running

Go ahead and build your project. From our root directory we can invoke the
`javac` CLI with

```
javac -cp path/to/compiled/files -Xplugin:OptimizationPlugin resources/classes/TestClass.java
```

If you are using an IDE then your classpath will be something along the lines of
`out/production/project-name` (this is where mine is); otherwise, if you are
outputting class files where your source files are you can just set classpath as
`src`.

If you have the option it is also an excellent idea to use a debugger and
explore the different moving parts here. The one subtlety here is that you won't
be able to run the `javac` command in your debugger without some setting up.
However, you *can* call into `com.sun.tools.javac.Main` which gives the CLI for
javac. Here is my RunConfigurations for Intellij:

{% highlight xml %}
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="OptimizationPlugin" type="Application" factoryName="Application">
    <extension name="coverage" enabled="false" merge="false" sample_coverage="true" runner="idea" />
    <option name="MAIN_CLASS_NAME" value="com.sun.tools.javac.Main" />
    <option name="VM_PARAMETERS" value="" />
    <option name="PROGRAM_PARAMETERS" value="-Xplugin:OptimizationPlugin -cp resources/classes resources/classes/TestClass.java" />
    <option name="WORKING_DIRECTORY" value="file://$PROJECT_DIR$" />
    <option name="ALTERNATIVE_JRE_PATH_ENABLED" value="false" />
    <option name="ALTERNATIVE_JRE_PATH" />
    <option name="ENABLE_SWING_INSPECTOR" value="false" />
    <option name="ENV_VARIABLES" />
    <option name="PASS_PARENT_ENVS" value="true" />
    <module name="JavaOptimizingCompiler" />
    <envs />
    <method />
  </configuration>
</component>
{% endhighlight %}


and here is an image of my RunConfig in IntelliJ:

![Run Configuration](/assets/img/OptimizationPluginRunConfig.png)

Set a breakpoint inside of `started` and `finished` and you will be able to poke
around at the compilation innards and come up with neat ideas on how to break
stuff.

### Working With ASTs
We now have a basic plugin that can identify which compiler phase just started
or ended; now we build on top of that to update the abstract syntax tree.

We will want to traverse the AST and Java handles this with the
`com.sun.tools.javac.tree.TreeScanner` class. This visits the AST node by node
using the [Visitor Pattern][visitor pattern] and we can override methods to
elicit specific behavior. Below is the listing of the class
`ConstFoldTreeScanner` which we will use to implement constant folding.

{% highlight java %}
public class ConstFoldTreeScanner extends TreeScanner{
    @Override
    public void visitBinary(JCTree.JCBinary tree) {
        System.out.println("visitBinary: " + tree);
        super.visitBinary(tree);
    }
}
{% endhighlight %}

We have overridden the `visitBinary` method which accepts a binary expression
such as `(+ 1 2)` so that we can fold them together when appropriate. We print
the binary expression we are visiting and then make a call to
`super.visitBinary` to recur into the binary expression.

Next, let's update our TaskListener to call this after the AST has been created.
Change the line `System.out.println("Task event " + e + " has ended");` after
the `TODO: optimize!` above to

{% highlight java %}
    TreeScanner visitor = new ConstFoldTreeScanner();
    visitor.scan((JCTree)e.getCompilationUnit);
{% endhighlight %}

Since our visitor only accepts Java Compiler Trees (`JCTree`) we have to
cast our compilation unit which is, in fact, the root node of our AST. Go ahead
and run Javac with the plugin and look at the output.  On my system it looks
like this:

```
ben@bbeonx$ javac -cp out/production/JavaOptimizingCompiler -Xplugin:OptimizationPlugin resources/classes/TestClass.java
visitBinary: 1 + 2 + a
visitBinary: 1 + 2
visitBinary: a + 1 + 2
visitBinary: a + 1
```


## Back To The Problem
### Implementing Right Constant Folding
#### Pattern Matching
We are basically going to be pattern matching on binary expression trees.
Consider the following expression:

{% highlight java %}
int yearsToSeconds(int years){
  int seconds = years * 60 * 60 * 24 * 365;
  return seconds;
}
{% endhighlight %}

In fact, lets add this to our `TestClass.java` file since this will demonstrate
another optimization that we will be performing later. We compile with our
plugin which outputs

```
visitMethodDef: yearsToSeconds
   visitBinary: years * 60 * 60 * 24 * 365
   visitBinary: years * 60 * 60 * 24
   visitBinary: years * 60 * 60
   visitBinary: years * 60
```

Thus the innermost binary node in our expression is `(* years 60)`, and its
parent is `(* (* years 60) 60)`, etc, and our AST is of the form

{% highlight clojure %}
  (* (* (* (* years 60) 60) 24) 
     365)
{% endhighlight %}

The root node has the literal `365` for its right branch, and its left branch has
the literal `24` for *its* right branch. In particular, we have
an expression tree of the form

{% highlight clojure %}
  (op (op Subtree LITERAL) LITERAL) 
{% endhighlight %}

and since we are working with a nice operator (multiplication) we can replace
the above tree with

{% highlight clojure %}
  (op Subtree (op LITERAL LITERAL))
{% endhighlight %}
where `(op LITERAL LITERAL)` is evaluated and folded.  We want to work from the
bottom up, pattern matching the `(op (op Subtree LITERAL) LITERAL)` pattern as
we go. For example,

```
             +        ROOT OF --> +                 +
 ROOT OF    / \         FOLD     / \               / \ 
  FOLD --> +   1                +   1             a   6
          / \                  / \ 
         +   2                a   5
        / \ 
       a   3
```

#### Building the TreeFolder
Alright, so we should create a class that updates a binary operator tree node if
it can be folded. We will call this class `TreeFolder` because we are not very
creative. There is a bit of work to be done here so I'll give the listing
followed by an explanation of what everything does.

{% highlight java %}
public class TreeFolder {

    /**
     * For ease of use, lookup table for operators
     */
    private static HashMap<JCTree.Tag, BiFunction<JCTree.JCLiteral, JCTree.JCLiteral, JCTree.JCLiteral>> interpreters;

    static {
        interpreters = new HashMap<>();
        interpreters.put(JCTree.Tag.PLUS,
                (l, r) -> createIntLiteral(((Integer)l.getValue()) + ((Integer)r.getValue())));
        interpreters.put(JCTree.Tag.MUL,
                (l, r) -> createIntLiteral(((Integer)l.getValue()) * ((Integer)r.getValue())));
    }

    /**
     * Create a new JCTree.JCLiteral with value {@code value}
     * @param value
     * @return
     */
    private static JCTree.JCLiteral createIntLiteral(int value){
        try {
            Constructor<JCTree.JCLiteral> constructor;
            constructor = JCTree.JCLiteral.class.getDeclaredConstructor(TypeTag.class, Object.class);
            constructor.setAccessible(true);
            return constructor.newInstance(TypeTag.INT, value);
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Test if the operator is associative---here operators are specified with
     * the {@code JCTree.Tag} type.
     * @param tag
     * @return
     */
    private static boolean tagIsAssociative(JCTree.Tag tag){
        switch (tag){
            case PLUS:
            case MUL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Test if this is right foldable
     * @param tree AST node to test
     * @return {@code true} if we can bold this and {@code false} otherwise
     */
    private static boolean isRightFoldable(JCTree.JCBinary tree){
        final JCTree.Tag tag = tree.getTag();
        final JCTree.JCExpression lhs = tree.lhs;
        final JCTree.JCExpression rhs = tree.rhs;
        return tagIsAssociative(tag) && interpreters.containsKey(tag)
                                     && rhs instanceof JCTree.JCLiteral
                                     && lhs instanceof JCTree.JCBinary
                                     && ((JCTree.JCLiteral)((JCTree.JCBinary)lhs).rhs).typetag == TypeTag.INT
                                     && ((JCTree.JCLiteral) rhs).typetag == TypeTag.INT;
    }

    /**
     * Perform a right constant fold if possible, otherwise do nothing
     * @param tree
     */
    static void foldr(JCTree.JCBinary tree){

        if (isRightFoldable(tree))
        {
            final JCTree.Tag tag = tree.getTag();
            final JCTree.JCBinary lhs = (JCTree.JCBinary)tree.lhs;
            final JCTree.JCLiteral rhs = (JCTree.JCLiteral)tree.rhs;
            final BiFunction<JCTree.JCLiteral, JCTree.JCLiteral, JCTree.JCLiteral> fn = interpreters.get(tag);

            if (lhs.getTag() == tag && lhs.rhs instanceof JCTree.JCLiteral){
                final JCTree.JCLiteral lrLit = (JCTree.JCLiteral)lhs.rhs;
                final JCTree.JCExpression llExpr = lhs.lhs;
                final JCTree.JCLiteral newLiteral = fn.apply(lrLit, rhs);
                if (newLiteral == null){
                   return;
                }
                tree.lhs = llExpr;
                tree.rhs = newLiteral;
            }
        }
    }
}
{% endhighlight %}

Now we just need to update our `visitBinary()` method in
`ConstFoldTreeScanner.java` to try to fold when possible. Since our logic in
`TreeFolder` checks if folding is safe we can just call `foldr(tree)` on every
binary tree we come across. Of course, we should recur first so that any nesting
folds propagate upwards:

{% highlight java %}
public class ConstFoldTreeScanner extends TreeScanner{

    // ...

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
        super.visitBinary(tree);
        TreeFolder.foldr(tree);
    }
}
{% endhighlight %}

Here are the high points of the above code. First, we do some static
initialization by mapping `JCTree.Tag` instances `PLUS` and `MUL` to lambdas
that perform their  respective operations. Next we will briefly list each of the
methods:

* `createIntLiteral(int value)`: We have to do a bit of reflection to get a
  constructor for a `JCLiteral` so we factored it out into its own method to
  make our lives easy. It returns `null` on error, and since I'm a terrible
  developer I didn't reflect that in the Javadocs.

* `tagIsAssociative(JCTree.Tag tag)`: This just checks if it is PLUS or MUL,
  which are the only two tags we are worrying about for now.

* `isRightFoldable(JCTree.JCBinary tree)`: this is basically a giant boolean
  expression that checks a binary node to ensure that it meets our criteria to
  be folded. Go ahead and read the code---it's not too complicated.

* `foldr(JCTree.JCBinary tree)`: Checks if the tree is foldable and, if so,
  performs the right fold.

Lastly, we add `TreeFolder.foldr(tree)` to `visitBinary()`, which performs the
folding.

### Testing Our Code

Let's take it for a spin. Here is what I get when I run my code (note that I
have a few more test methods included here):

```
visitMethodDef: leftConstantFold
    visitBinary: 1 + 2 + a
    visitBinary: 1 + 2
  revisitBinary: 1 + 2
  revisitBinary: 1 + 2 + a
visitMethodDef: rightConstantFold
    visitBinary: a + 1 + 2
    visitBinary: a + 1
  revisitBinary: a + 1
  revisitBinary: a + 3
visitMethodDef: parenConstantFold
    visitBinary: a + (1 + 2)
    visitBinary: 1 + 2
  revisitBinary: 1 + 2
  revisitBinary: a + (1 + 2)
visitMethodDef: yearsToSeconds
    visitBinary: years * 60 * 60 * 24 * 365
    visitBinary: years * 60 * 60 * 24
    visitBinary: years * 60 * 60
    visitBinary: years * 60
  revisitBinary: years * 60
  revisitBinary: years * 3600
  revisitBinary: years * 86400
  revisitBinary: years * 31536000
visitMethodDef: complicatedConstantFold
    visitBinary: 1 + (a + 2 + 3) + 4
    visitBinary: 1 + (a + 2 + 3)
    visitBinary: a + 2 + 3
    visitBinary: a + 2
  revisitBinary: a + 2
  revisitBinary: a + 5
  revisitBinary: 1 + (a + 5)
  revisitBinary: 1 + (a + 5) + 4
```

and here is the `javap` output:

```
public class TestClass {
  public int leftConstantFold(int);
    Code:
       0: iconst_3
       1: iload_1
       2: iadd
       3: ireturn

  public int rightConstantFold(int);
    Code:
       0: iload_1
       1: iconst_3
       2: iadd
       3: ireturn

  public int yearsToSeconds(int);
    Code:
       0: iload_1
       1: ldc           #2  // int 31536000
       3: imul
       4: istore_2
       5: iload_2
       6: ireturn
}
```

Woah! Look at that! `yearsToSeconds()` works perfectly! So does
`rightConstantFold()` and any other right-folding we have encountered! One thing
to notice, however: the left folding hasn't happend yet. Since Javac does this
automatically for us (but later on) I haven't included it here but it would be
good to have both of these be performed in a single place.

There is also the issue of parentheses. It would be nice to simplify `1 + (a +
5) + 4` in `complicatedConstantFold()` but we don't. That can be added easily
enough though and may be a fun exercise for the reader.



<!-- git-tag: basic-plugin-working -->



<!-- LINKS -->
[javac-plugin-docs]: https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/com/sun/source/util/Plugin.html
[observer pattern]: https://en.wikipedia.org/wiki/Observer_pattern
[visitor pattern]: https://en.wikipedia.org/wiki/Visitor_pattern
[my github]: https://github.com/bkushigian/javac-optimization-plugin
[service loader docs]: https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
[wiki-ast]:https://en.wikipedia.org/wiki/Abstract_syntax_tree
