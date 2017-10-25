package se.tardell.simon.bajs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;


public class BajsAgent {
  public static void premain(String agentArgs, Instrumentation inst) {

    System.out.println("Starting the agent");

    System.out.println(new TaintUtil().isTainted("Kaka"));
    //     inst.addTransformer(new StringTaintPropagationTransformer(), true);
    inst.addTransformer(new SourceTransformer(load("/sources.json")),true);
    inst.addTransformer(new SinkTransformer(load("/sinks.json")), true);

    /**     inst.retransformClasses(String.class, Class.forName("java.lang.AbstractStringBuilder"), StringBuilder.class, StringBuffer.class);

    inst.redefineClasses(getClassDefinition(String.class));
    inst.redefineClasses(getClassDefinition(Class.forName("java.lang.AbstractStringBuilder")));
    inst.redefineClasses(getClassDefinition(StringBuilder.class));
    inst.redefineClasses(getClassDefinition(StringBuffer.class));
**/
    System.out.println("String is modifiable "+ inst.isModifiableClass(String.class));
//      inst.retransformClasses(String.class);

  }

  private static List<MethodReference> load(String s) {
    try {
      final URL resource = BajsAgent.class.getResource(s);
      final ObjectMapper mapper = new ObjectMapper();
      final CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, MethodReference.class);
      return mapper.readValue(resource, type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static ClassDefinition getClassDefinition(Class<?> aClass) throws IOException {
    return new ClassDefinition(aClass, getBytesOfClass(aClass));
  }

  private static byte[] getBytesOfClass(Class<?> aClass) throws IOException {
    final InputStream resource = aClass.getResourceAsStream(aClass.getSimpleName()+".class");
    if(resource==null){
      System.out.println(aClass.getSimpleName()+" not found");
      return null;
    }
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    IOUtils.copy(resource, output);
    return output.toByteArray();
  }
}