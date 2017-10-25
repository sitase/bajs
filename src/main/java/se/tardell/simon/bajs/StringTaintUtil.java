package se.tardell.simon.bajs;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**

 */
public class StringTaintUtil {

  public static final String JAVA_LANG_STRING = "java/lang/String";
  public static final String JAVA_LANG_ABSTRACT_STRING_BUILDER = "java/lang/AbstractStringBuilder";
  private static final String JAVA_LANG_STRING_BUFFER = "java/lang/StringBuffer";
  private static final String JAVA_LANG_STRING_BUILDER = "java/lang/StringBuilder";


  static public void addTaintPropagationForString(CtClass cc){
    try {

      copyConstructorTaintPropagation(cc.getConstructor("(L" + JAVA_LANG_STRING + ";)V"));
      //new String(StringBuffer)
      copyConstructorTaintPropagation(cc.getConstructor("(L" + JAVA_LANG_STRING_BUILDER+";)V"));
      //new String(StringBuilder)
      copyConstructorTaintPropagation(cc.getConstructor("(L" + JAVA_LANG_STRING + ";)V"));

     // listMethods(cc);
      //concat(String)
      final CtMethod concat = cc.getMethod("concat", "(L" + JAVA_LANG_STRING + ";)L" + JAVA_LANG_STRING+";");
      concat.insertAfter("{$_.isTainted=$0.isTainted||$1.isTainted;}");

      //TODO java.lang.String#format(java.lang.String, java.lang.Object...)

      //substring(int)
      copyPropagatesTaint(cc.getMethod("substring", "(I)L" + JAVA_LANG_STRING + ";"));
      //substring(int,int)
      copyPropagatesTaint(cc.getMethod("substring", "(II)L" + JAVA_LANG_STRING + ";"));
      //toUpperCase(java.util.Locale)
      copyPropagatesTaint(cc.getMethod("toUpperCase", "(Ljava/util/Locale;)L" + JAVA_LANG_STRING + ";"));
      //toLowerCase(java.util.Locale)
      copyPropagatesTaint(cc.getMethod("toLowerCase", "(Ljava/util/Locale;)L" + JAVA_LANG_STRING + ";"));
    } catch (CannotCompileException e) {
      throw new RuntimeException(e);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void listMethods(CtClass cc) {
    final CtMethod[] methods = cc.getMethods();
    for (CtMethod m: methods){
      System.out.println(m.getModifiers()+" "+m.getName()+" "+m.getMethodInfo().getDescriptor());
    }
  }

  static public void addTaintPropagationForAbstractStringBuilder(CtClass cc){

    try {
      listMethods(cc);
      fluentTaintPropagation(
          cc.getMethod("append", "(L" + JAVA_LANG_STRING + ";)L" + JAVA_LANG_ABSTRACT_STRING_BUILDER + ";"), 1);
      fluentTaintPropagation(
          cc.getMethod("append", "(L" + JAVA_LANG_STRING_BUFFER+";)L" + JAVA_LANG_ABSTRACT_STRING_BUILDER + ";"), 1);
      fluentTaintPropagation(
          cc.getMethod("append", "(L" + JAVA_LANG_STRING + ";)L" + JAVA_LANG_ABSTRACT_STRING_BUILDER + ";"), 1);
      copyPropagatesTaint(cc.getMethod("substring", "(II)L" + JAVA_LANG_STRING + ";"));
      fluentTaintPropagation(
          cc.getMethod("replace", "(IIL" + JAVA_LANG_STRING + ";)L" + JAVA_LANG_ABSTRACT_STRING_BUILDER + ";"), 3);


    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    } catch (CannotCompileException e) {
      throw new RuntimeException(e);
    }

  }

  private static void fluentTaintPropagation(CtMethod m, int index) throws CannotCompileException {
    m.insertAfter("{$_.isTainted|=$"+index+".isTainted;}");
  }

  static private void copyPropagatesTaint(CtMethod m) throws CannotCompileException {
    m.insertAfter("{$_.isTainted=$0.isTainted;}");
  }

  static private void copyConstructorTaintPropagation(CtConstructor c) throws CannotCompileException {
    try {
      final CtField[] declaredFields = c.getParameterTypes()[0].getDeclaredFields();
      for(CtField f : declaredFields){
        System.out.println(f.getModifiers()+" "+f.getName()+" "+f.getSignature());
      }
      System.out.println("---");
      final CtField[] declaredFields1 = c.getDeclaringClass().getDeclaredFields();
      for (CtField f: declaredFields1){
        System.out.println(f.getModifiers()+" "+f.getName()+" "+f.getSignature());
      }
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
    c.insertAfter("{$0.isTainted = $1.isTainted;}");
  }

  static public CtClass addTaintFieldtoClass(ClassPool cp, String classname)
      throws NotFoundException, CannotCompileException {
    CtClass cc = cp.get(classname);
    cc.defrost();

    cc.addInterface(cp.get(Taintable.class.getName()));

    CtField taintField = new CtField(CtClass.booleanType, "isTainted", cc);
    taintField.setModifiers(Modifier.PUBLIC);
    cc.addField(taintField,"false");

    cc.addMethod(CtMethod.make("public void setTaint(boolean value){this.isTainted=value;}",cc));
    cc.addMethod(CtMethod.make("public boolean isTainted(){ return this.isTainted;}",cc));

    final CtField isTainted = cc.getField("isTainted");
    if(isTainted==null){
      System.out.println("isTainted is null");
    } else {
      System.out.println(cc.getName()+"."+isTainted.getName()+" "+isTainted.getSignature());
    }
    return cc;
  }
}
