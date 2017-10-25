package se.tardell.simon.bajs;


import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
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
    cp.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
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
}
