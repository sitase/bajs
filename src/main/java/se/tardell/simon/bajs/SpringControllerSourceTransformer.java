package se.tardell.simon.bajs;


import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.annotation.AnnotationImpl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import lombok.extern.java.Log;

@Log
public class SpringControllerSourceTransformer implements ClassFileTransformer{

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    try {
      if(!className.endsWith("Controller"))return  null;
      log.log(Level.INFO,"Attempting to transform as controller "+className);
 //     if(!className.startsWith("se.tardell")) return null;
      final CtClass cc = TransformerUtils.getCtClass(className, classfileBuffer);
      final Object[] annotations = cc.getAnnotations();
      if(!doesAnnotationMatch(annotations,"org.springframework.stereotype.Controller")) return null;

      log.log(Level.INFO,"Transforming as controller "+className);

      Stream.of(cc.getMethods())
          .filter(this::hasMapping)
          .forEach(this::transformMethod);

      return cc.toBytecode();
    } catch (Throwable e) {
      log.log(Level.WARNING,e.getMessage(),e);
      throw new RuntimeException(e);
    }
  }

  private boolean doesAnnotationMatch(Object[] annotations, String className) {
    if(annotations==null){
      log.log(Level.INFO,"No annotations");
      return false;}
    return Stream.of(annotations)
        .map(Object::toString)
        .map(s->{log.log(Level.INFO,s); return s;})
        .anyMatch(s->s.equals("@"+className));
  }

  private void transformMethod(CtMethod method) {
    try {
      log.log(Level.INFO,"method "+method.getName());
      final Object[][] parameterAnnotations = method.getParameterAnnotations();
      StringBuilder before = new StringBuilder("{");
      for(int i=0; i<parameterAnnotations.length; i++){
        if(shouldTaintParameter(method.getParameterTypes()[i], parameterAnnotations[i]))
          before.append(taintParameter(method.getDeclaringClass(),method.getParameterTypes()[i], i+1));
      }
      before.append("}");
      final String src = before.toString();
      log.log(Level.INFO, src);
      method.insertBefore(src);
      //TODO transform return value
      StringBuilder after = new StringBuilder("{");
      after.append("se.tardell.simon.bajs.TaintUtil.checkTaint(")
          .append("$_")
          .append(",\"")
          .append(method.getDeclaringClass().getName()+"#"+method.getLongName())
          .append("\");");
      after.append("}");
      final String src1 = after.toString();
      log.log(Level.INFO, src1);

      method.insertAfter(src1);
    } catch (ClassNotFoundException | CannotCompileException | NotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  List<String> params = Arrays.asList("org.springframework.web.bind.annotation.PathVariable","org.springframework.web.bind.annotation.RequestHeader","org.springframework.web.bind.annotation.RequestBody");
  private boolean shouldTaintParameter(CtClass ctClass, Object[] annotation) {
    if(ctClass.isPrimitive()||ctClass.isArray()) return false;
    return true;
   /* return Stream.of(annotation)
        .anyMatch(o ->  params.contains(o.getClass().getName()));
        */
  }

  private String taintParameter(CtClass declaringClass, CtClass type, int index){
    if(type.getName().equals("java.lang.String")){
      return "$"+index+".setTaint(true);";
    } else {
      StringBuilder inner = new StringBuilder();
      //TODO traverse bean
      final CtField[] fields = type.getFields();
      for(CtField f: fields){
        try {
          if(f.visibleFrom(declaringClass))
            if(f.getType().getName().equals("java.lang.String"))
              inner.append("$"+index+"."+f.getName()+".setTaint(true);");
           else; //TODO we should recurse
        } catch (NotFoundException e) {
          //TODO log instead
          throw new RuntimeException(e);
        }
      }
      //TODO should handle bean property getters

      return inner.toString();
    }

  }
  private boolean hasMapping(CtMethod method) {
    try {
      final Object[] annotations = method.getAnnotations();
      return Stream.of(annotations)
          .anyMatch(o -> o.toString().contains("Mapping"));
    } catch (ClassNotFoundException e) {
      return false;
    }

  }
}
