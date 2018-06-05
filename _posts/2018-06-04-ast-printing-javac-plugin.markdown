---
layout: post
title:  "Building an AST-Printer"
date:   2018-06-03 23:28:50 -0400
categories: java compiler javac-plugin
---

In my [last post][optimizing javac plugin] we got a Javac plugin set up that
performed right constant foldings. I was planning on building a bytecode
optimization TreeScanner next but I decided it would be nice to have something
to visualize ASTs so that we don't have to fire up the debugger every time we
want to see the structure of the code.

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

2. Recur into any sub-trees as needed

3. Print a closing parenthesis `)`


Easy, no?

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
        printer.push("class-def: " + tree.name);
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
        printer.push("if: ");
        super.visitIf(tree);
        printer.pop();
    }

}
{% endhighlight %}

Alright, I included quite a bit more than I intended to but not to worry---there
are still some things for you to add int. It also might be nice to pretty-print
some of the operators (`PLUS` could be replaced with `+` and so on).

Anyways, that's all there is to say about that. The code is on [my
Github][repo-url] under release `print-scanner`.


<!-- LINKS -->
[optimizing javac plugin]: {% post_url 2018-06-03-optimizing-javac-plugin %}
[homoiconicity]: https://en.wikipedia.org/wiki/Homoiconicity
[repo-url]: https://github.com/bkushigian/javac-optimization-plugin/tree/print-scanner
