package se.tardell.simon.bajs;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Given a String class that already contains a taint-field, add in contagion.
 */
public class StringTaintPropagationTransformer implements ClassFileTransformer {
  //TODO handle StringBuffer
  //TODO handle StringBuilder
  //TODO differentiate by sources and sink type?
  @Override
  public byte[] transform(ClassLoader loader,
                          String className,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) throws IllegalClassFormatException {
    try {
      if(!className.equals("java/lang/String")) {
        return null;
      }
      System.out.println("transforming "+className);

      ClassPool cp = ClassPool.getDefault();
      cp.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
      CtClass cc = cp.get(className.replaceAll("/","."));
      cc.defrost();

      //check for presence of taint field
      final CtField isTainted = cc.getField("isTainted");
      if(isTainted==null){
        System.out.println("no taint field in String");
        return null;
      } else {
        System.out.println(isTainted.getName()+" "+isTainted.getSignature());
      }
      StringTaintUtil.addTaintPropagationForString(cc);

      final byte[] bytes = cc.toBytecode();
      return bytes;
    } catch (Throwable ignored) {
      System.out.println("ERROR: "+ignored);
    } finally { return null; }
  }

}