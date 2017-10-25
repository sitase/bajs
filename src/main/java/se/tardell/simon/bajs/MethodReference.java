package se.tardell.simon.bajs;


import lombok.Value;

@Value
public class MethodReference {
  private String clazz;
  private String method;
  private String descriptor;
}
