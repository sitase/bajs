package se.tardell.simon.bajs;


import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransformerUtils {

  public static List<MethodReference> matchClass(String className, List<MethodReference> methods) {
    String c = className.replace('.','/');
    return methods.stream()
        .filter(r -> r.getClazz().equals(c))
        .collect(Collectors.toList());
  }

  static public CtClass getCtClass(String className, byte[] classfileBuffer) throws NotFoundException {
    ClassPool cp = ClassPool.getDefault();
    cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    cp.insertClassPath(new ByteArrayClassPath(className.replaceAll(".","/"), classfileBuffer));
    return cp.get(className.replaceAll("/", "."));
  }

  static void writeFile(String className, byte[] bytes) throws IOException {
    System.out.println(className+": "+bytes.length);
    final String s = className.replace(".", "/");
    File f = new File("build/taint/" + s + ".class");
    f.getParentFile().mkdirs();
    final FileOutputStream fos = new FileOutputStream(f);
    fos.write(bytes);
    fos.flush();

    fos.close();
  }

  static public byte[] transformMethods(CtClass cc, List<MethodReference> methods, Consumer<CtMethod> action)
      throws IOException, CannotCompileException {
    ThrowingFunction<MethodReference, CtMethod> f = (MethodReference r) ->    cc.getMethod(r.getMethod(), r.getDescriptor());
    methods.stream()
        .map(f)
        .forEach(action);

    return cc.toBytecode();
  }
}
