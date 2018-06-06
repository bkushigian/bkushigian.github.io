---
layout: post
title:  "Building an AST-Printer"
date:   2018-06-03 23:28:50 -0400
comments: true
categories: java compiler javac-plugin
---

In my [last post][optimizing javac plugin] we got a Javac plugin set up that
performed right constant foldings. I was planning on building a bytecode
optimization TreeScanner next but I decided it would be nice to have something
to visualize ASTs so that we don't have to fire up the debugger every time we
want to see the structure of the code. The code is [on my github][repo-url]
under the `ast-printer` release (or if you cloned last time, just do a pull).

After my last post this one should be very easy---all that we will do is create
a new TreeScanner that adds the node names and relevant information to a
StringBuilder and then print out the built up string at the end.

For example, given method

{% highlight java %}
  public int forTest(int a, int b){
      int total = 0;
      for (int i = a; i < b; ++i){
          total += i;
      }
      return total;
  }
{% endhighlight %}

our AST Printer's output is:

{% highlight clojure %}
(method-def forTest { params [int a,int b], returns int, modifiers public }
    (var-def a:int )
    (var-def b:int )
    (block
        (var-def total:int (literal 0:INT))
        (for (var-def i:int (ident a))
            (bin-op LT
                (ident i)
                (ident b))
            (unary PREINC (ident i))
            (block
                (assign-op(ident total)
                    (ident i))))
        (return (ident total)))) 
{% endhighlight %}

You will notice a certain Lispy style to it---in fact we are pretty much writing
Lisp pseudocode since Lisp is a [homoiconic language][homoiconicity] which is a
fancy way of saying that Lispers write ASTs instead of programs. Anyways, I
digress...

As I was saying, this is a pretty easy plugin to write: we just need to override
all the `visitXXX` methods in `TreeScanner` to
1. Print the appropriate data of the node, i.e.,  

       (method-def forTest {params [int a, int b], returns int, modifiers public }

2. Recur into any sub-trees as needed (we do this with a call to
   `super.visitXXX` which takes care of any recursion); and

3. Print a closing parenthesis `)`

Easy, no?

## Wrapping StringBuilder---The ASTPrinter
It will be useful to have a class that can handle the printing and indenting for
us. To that end we define the `ASTPrinter` which prints all things AST-related.
This is an easy enough class so I'll just list it and we can move on with our
lives:

{% highlight java %}
/**
 * A helper class to add structured AST representation to a string builder
 */
class ASTPrinter {
    /**
     * Current depth into the AST
     */
    private int depth = 0;

    private StringBuilder sb = new StringBuilder();

    /**
     * Should we print a newline at the next chance? Used by {@code indent()}
     */
    private boolean newline = true;

    private final String indentString = "    ";

    public int getDepth() { return depth; }

    /**
     * Reset the printer
     * @return
     */
    void clear(){
        sb = new StringBuilder();
        newline = true;
        depth = 0;
    }

    /**
     * Mark a newline to be printed at the next chance
     */
    void newline(){
        newline = true;
    }

    /**
     * If a newline is ready to be printed (i.e. {@code newline()} is true), 
     * print it and indent. Otherwise, do nothing.
     */
    private void indent(){
        if (newline){
            sb.append("\n");
            newline = false;
            for (int i = 0; i < depth; ++i){
                sb.append(indentString);
            }
        }
    }

    /**
     * Push an AST node by passing a string representative
     * (i.e. "literal 32:INT")
     */
    void push(String s){
        indent();
        sb.append('(');
        sb.append(s);
        ++depth;
    }

    /**
     * Pop from a node
     */
    void pop(){
        sb.append(")");
        newline();
        --depth;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
{% endhighlight %}

This is easy enough to customize if you see fit---the output isn't perfect but
it's good enough for most use cases.


## Building the TreeScanner
Now we build the `TreeScanner`. I won't include everything but I'll include
enough to get you going. All we will be doing is calling into `push` and `pop`
with an occasional `newline` call. Anyways, here it is



{% highlight java %}
public class ASTPrintTreeScanner extends TreeScanner {

    ASTPrinter printer;
    public ASTPrintTreeScanner(){
        printer = new ASTPrinter();
    }

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit tree) {
        printer.push("top-level { " + "package : " + tree.packge + " }");
        printer.newline();
        super.visitTopLevel(tree);
        printer.pop();
        System.out.println(printer.toString());
        printer.clear();
    }

    @Override
    public void visitImport(JCTree.JCImport tree) {
        printer.push("import " tree.qualid);
        printer.pop();
    }

    @Override
    public void visitAssignop(JCTree.JCAssignOp tree) {
        printer.push("assign-op");
        super.visitAssignop(tree);
        printer.pop();
    }

    @Override
    public void visitAssign(JCTree.JCAssign tree) {
        printer.push("=");
        super.visitAssign(tree);
        printer.pop();
    }

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
        printer.push("bin-op "  + tree.getTag().toString());
        printer.newline();
        super.visitBinary(tree);
        printer.pop();
    }

    @Override
    public void visitUnary(JCTree.JCUnary tree) {
        printer.push("unary " + tree.getTag().toString() + " ");
        super.visitUnary(tree);
        printer.pop();
    }

    @Override
    public void visitIdent(JCTree.JCIdent tree) {
        printer.push("ident " + tree.name);
        super.visitIdent(tree);
        printer.pop();
    }

    @Override
    public void visitLiteral(JCTree.JCLiteral tree) {
        String value = tree.value instanceof String ? "\"" + tree.value + "\"" : tree.value.toString();
        printer.push("literal " + value + ":" + tree.typetag.toString());
        super.visitLiteral(tree);
        printer.pop();
    }

    @Override
    public void visitBlock(JCTree.JCBlock tree) {
        printer.push("block " + "{ " + "is-static : " + tree.isStatic() + " }");
        printer.newline();
        super.visitBlock(tree);
        printer.pop();
    }

    @Override
    public void visitWhileLoop(JCTree.JCWhileLoop tree) {
        printer.push("while ");
        super.visitWhileLoop(tree);
        printer.pop();
    }

    @Override
    public void visitForLoop(JCTree.JCForLoop tree) {
        printer.push("for ");
        super.visitForLoop(tree);
        printer.pop();
    }

    @Override
    public void visitBreak(JCTree.JCBreak tree) {
        printer.push("break");
        super.visitBreak(tree);
        printer.pop();
    }

    @Override
    public void visitReturn(JCTree.JCReturn tree) {
        printer.push("return ");
        super.visitReturn(tree);
        printer.pop();
    }
    @Override
    public void visitNewClass(JCTree.JCNewClass tree) {
        printer.push("new-class " );
        super.visitNewClass(tree);
        printer.pop();
    }

    @Override
    public void visitParens(JCTree.JCParens tree) {
        printer.push("parens ");
        super.visitParens(tree);
        printer.pop();
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl tree) {
        printer.push("class-def " + tree.name);
        printer.newline();
        super.visitClassDef(tree);
        printer.pop();
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        printer.push("method-def " + tree.name + " { " + "params : [" + tree.params + "]," + " returns : " + tree.restype + ", modifiers : " + tree.mods + "}");
        printer.newline();
        super.visitMethodDef(tree);
        printer.pop();
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        printer.push("var-def " + tree.name + ":" + tree.vartype  + " ");
        super.visitVarDef(tree);
        printer.pop();
    }

    @Override
    public void visitIf(JCTree.JCIf tree) {
        printer.push("if ");
        super.visitIf(tree);
        printer.pop();
    }

}
{% endhighlight %}

Alright, I included quite a bit more than I intended to but not to worry---there
are still some things for you to add in. It also might be nice to pretty-print
some of the operators (`PLUS` could be replaced with `+` and so on).

## Invoking your AST-Printer
We now have two possible task that can run from our plugin. How should we run
them? Obviously we should hard code and recompile whenever we want our program
to have a new configuration. Oooh, or we could have it read a config file! Yeah,
that's it!  Or, failing that, we should accept command line options.  Tomato
tomato, really.

But how do we add arguments to our plugin from the Javac CLI? How indeed...

### Digging through the Javac source code

The Javac compiler has an option for passing arguments into a plugin (you may
have noticed that our plugin's `init` method had a variadic `String ... args`
parameter). It actually took me digging through a bit of the Javac source to
figure out exactly how to pass these in so I'll include the relevant bit here
for some nice reading. Feel free to skip ahead to the next section.

**From `com.sun.tools.javac.main.Main:452`:**

{% highlight java %}
if (plugins != null) {
    JavacProcessingEnvironment pEnv = JavacProcessingEnvironment.instance(context);
    ClassLoader cl = pEnv.getProcessorClassLoader();
    ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, cl);
    Set<List<String>> pluginsToCall = new LinkedHashSet<List<String>>();
    for (String plugin: plugins.split("\\x00")) {
        pluginsToCall.add(List.from(plugin.split("\\s+")));
    }
    JavacTask task = null;
    Iterator<Plugin> iter = sl.iterator();
    while (iter.hasNext()) {
        Plugin plugin = iter.next();
        for (List<String> p: pluginsToCall) {
            if (plugin.getName().equals(p.head)) {
                pluginsToCall.remove(p);
                try {
                    if (task == null)
                        task = JavacTask.instance(pEnv);
                    plugin.init(task, p.tail.toArray(new String[p.tail.size()]));
                } catch (Throwable ex) {
                    if (apiMode)
                        throw new RuntimeException(ex);
                    pluginMessage(ex);
                    return Result.SYSERR;
                }
            }
        }
    }
    for (List<String> p: pluginsToCall) {
        log.printLines(PrefixKind.JAVAC, "msg.plugin.not.found", p.head);
    }
}
{% endhighlight %}

**Do you see it?** The 20th(ish) line, in the try-catch block, is

    plugin.init(task, p.tail.toArray(new String[p.tail.size()]));

And we see that the value `p.tail.toArray(new String[p.tail.size()])` gets
passed to `init` in the `args` location. What is this value, you ask? Good
question! It is the iterating variable in the for-each loop that we are in,
which iterates over `pluginsToCall`. And what are the values that we are
iterating over?  We see that we add `List<String>`s to `pluginsToCall` by
calling

    pluginsToCall.add(List.from(plugin.split("\\s+")));

A little more investigation/debugger work tells us that `plugin` is actually the
argument we passed in `-XPlugin:OptimizationPlugin` with the leading `-Xplugin:`
stripped away. This means that Javac splits everything in our `-Xplugin:...`
argument after the colon at whitespace and passes the tail of the new list to
`init` as args.  We can separate arguments with whitespace and they are passed
along to `init`. Thus we can call

    javac "-Xplugin:OptimizationPlugin arg1 arg2 arg3" ...

It's up to you how you want to pass args along. I'll include how I did it since
it is simple enough.


### Building a Basic Argument Parser

For our friends who skipped the above bit, the TL;DR is that we can pass in
arguments to our plugin like this:

    javac "-Xplugin:OptimizationPlugin arg1 arg2 arg3" ...

Now it is a case of setting up a basic arg parser and executing based on user
input. This isn't particularly exciting so I'll just list the code and you can
use it if you'd like.

{% highlight java %}

public class OptimizationTaskListener implements TaskListener {
    private TreeScanner visitor;

    boolean printAST = false;
    boolean foldConsts = true;
    boolean printASTAfterFolds = false;

    public OptimizationTaskListener(String ... args){
        handleArgs(args);
    }

    private void handleArgs(String[] args) {
        for (String arg : args){
            if ("print-ast".equals(arg)){
                printAST = true;
            } else if ("no-folds".equals(args)){
                foldConsts = false;
            } else if("print-ast-after-folds".equals(arg)){
                printASTAfterFolds = true;
            } else if ("usage".equals(arg) || "help".equals(arg)){
                System.out.println("OptimizationTaskListener Usage: \n" +
                "  Invoke with either: \n" +
                "      javac -Xplugin:OptimizationPlugin -cp classpath/with/plugin/compilation/base\n" +
                "      javac \"-Xplugin:OptimizationPlugin with space separated args\" -cp classpath/with/plugin/compilation/base\n" +
                "  Optional args include: \n" +
                "      print-ast: Print the original AST\n" +
                "      print-ast-after-folds: Print the AST after right folding--no effect if folding is disabled\n" +
                "      no-folds: Disable folding optimization\n" +
                "      usage | help: This message\n");
                System.exit(0);
            } else {
                System.err.println("Unknown option " + arg + "... ignoring");
            }
        }
    }

    @Override
    public void started(TaskEvent e) {}

    @Override
    public void finished(TaskEvent e) {
        if (e.getKind() == TaskEvent.Kind.PARSE){
            if (printAST) {
                visitor = new ASTPrintTreeScanner();
                visitor.scan((JCTree) e.getCompilationUnit());
            }
            if (foldConsts) {
                visitor = new ConstFoldTreeScanner();
                visitor.scan((JCTree)e.getCompilationUnit());
                if (printASTAfterFolds){
                    visitor = new ASTPrintTreeScanner();
                    visitor.scan((JCTree) e.getCompilationUnit());
                }
            }
        }
    }
}
{% endhighlight %}

Anyways, that's all there is to say about that. The code is on [my
Github][repo-url] under release `ast-printer`.


<!-- LINKS -->
[optimizing javac plugin]: {% post_url 2018-06-03-optimizing-javac-plugin %}
[homoiconicity]: https://en.wikipedia.org/wiki/Homoiconicity
[repo-url]: https://github.com/bkushigian/javac-optimization-plugin/tree/print-scanner
