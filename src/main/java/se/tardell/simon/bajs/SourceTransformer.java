package se.tardell.simon.bajs;


import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;

public class SourceTransformer implements ClassFileTransformer {


  public SourceTransformer(List<MethodReference> sources) {
    this.sources = sources;
  }

  private List<MethodReference> sources = new LinkedList<>();
  //TODO load a list of source method references

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    final List<MethodReference> methods = TransformerUtils.matchClass(className, sources);

    if(methods.size()==0) return null;

    try {
      final CtClass cc = TransformerUtils.getCtClass(className, classfileBuffer);

      return TransformerUtils.transformMethods(cc, methods, this::wrapReturn);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private void wrapReturn(CtMethod method) {
    try {
      method.insertAfter("{if($_==null)return null; $_.isTainted=true;}");
    } catch (CannotCompileException e) {
      throw new RuntimeException(e);
    }
  }

}
