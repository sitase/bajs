package se.tardell.simon.bajs;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;

public class SinkTransformer implements ClassFileTransformer{

  private List<MethodReference> sinks = new LinkedList<>();

  public SinkTransformer(List<MethodReference> sinks) {
    this.sinks = sinks;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {final List<MethodReference> methods = TransformerUtils.matchClass(className, sinks);

    if(methods.size()==0) return null;

    try {
      CtClass cc = TransformerUtils.getCtClass(className, classfileBuffer);
      return TransformerUtils.transformMethods(cc,methods,this::wrapParameters);

    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private void wrapParameters(CtMethod method) {
    try {
      StringBuilder check = new StringBuilder();
      check.append("{");
      final CtClass[] parameterTypes = method.getParameterTypes();

      for (int i = 0; i < parameterTypes.length ; i++) {
        CtClass parameterType = parameterTypes[i];
        if(parameterType.getName().equals("java/lang/String")){
          check.append("se.tardell.simon.bajs.TaintUtil.checkTaint($"+(i+1)+","+method.getDeclaringClass()+"#"+method.getLongName()+");");
        }

      }
      method.insertBefore(check.toString());
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    } catch (CannotCompileException e) {
      throw new RuntimeException(e);
    }

  }

}
