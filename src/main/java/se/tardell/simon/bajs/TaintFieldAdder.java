package se.tardell.simon.bajs;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.function.Consumer;

import static se.tardell.simon.bajs.StringTaintUtil.addTaintFieldtoClass;
import static se.tardell.simon.bajs.StringTaintUtil.addTaintPropagationForAbstractStringBuilder;
import static se.tardell.simon.bajs.StringTaintUtil.addTaintPropagationForString;
import static se.tardell.simon.bajs.TransformerUtils.writeFile;

/**
  We need to prepare a modification of java.lang.Stringuilder ahead of time that we can put on the bootclasspath, since we are unable to add fields to any class that the java agent itself depends on (we can't get to them before they are loaded the first time).
 */
public class TaintFieldAdder {

  public static final String JAVA_LANG_ABSTRACT_STRING_BUILDER = "java.lang.AbstractStringBuilder";

  public static void main(String[] args){

    new TaintFieldAdder().run();


  }

  private void run() {
    try {
      ClassPool cp = ClassPool.getDefault();
      final CtClass stringClass = addTaintFieldtoClass(cp, String.class.getName());
      final CtClass abstractStringBuilderClass = addTaintFieldtoClass(cp, JAVA_LANG_ABSTRACT_STRING_BUILDER);

      addTaintPropagationForString(stringClass);
      writeClass(cp, String.class);

      addTaintPropagationForAbstractStringBuilder(abstractStringBuilderClass);
      writeFile(JAVA_LANG_ABSTRACT_STRING_BUILDER,abstractStringBuilderClass.toBytecode());
      writeClass(cp,StringBuilder.class);
      writeClass(cp,StringBuffer.class);
      writeClass(cp, Taintable.class);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    } catch (CannotCompileException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private void writeClass(ClassPool cp, Class<?> aClass) throws NotFoundException, IOException, CannotCompileException {
    final String className = aClass.getName();
    final CtClass ctClass = cp.get(className);
    writeFile(className, ctClass.toBytecode());

  }




}
