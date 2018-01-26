package se.tardell.simon.bajs.sample;


import se.tardell.simon.bajs.TaintUtil;

public class Test {
  public static void main(String[] args){
    TaintUtil t = new TaintUtil();

    String s = "Kaka";

    System.out.println("### TAINT AND DETAINT");
    logtaint(s, false);
    t.taint(s);
    logtaint(s, true);
    t.detaint(s);
    logtaint(s,false);

    System.out.println("### PROPAGATE TAINT");
    t.taint(s);
    logtaint(s,true);
    String scopy = new String(s);
    logtaint(scopy, true);
    logtaint(s.substring(2),true);
    logtaint(s.toLowerCase(), true);
    logtaint(s.toUpperCase(), true);
    t.detaint(s);
    logtaint(s.substring(2), false);

    String taint = "Taint";
    t.taint(taint);
    String untainted = "Untainted";

    logtaint(taint,true);
    logtaint(untainted,false);
    logtaint(taint.concat(untainted), true);
    logtaint(untainted.concat(taint), true);

    //AbstractStringBuilder
    StringBuilder sb = new StringBuilder(taint);
    logtaint(sb, true);
    logtaint(new StringBuilder(untainted).append(taint).toString(),true);
    logtaint(new StringBuilder(taint).append(untainted).toString(),true);



  }

  public static void logtaint(Object s, boolean taintExpected){
    final boolean tainted = new TaintUtil().isTainted(s);
    System.out.println((tainted==taintExpected?" OK":"NOK")+" \"" + s + "\" is expected to " + (taintExpected ? "" : "not ") + "be tainted, taint = " + tainted);
  }

}
