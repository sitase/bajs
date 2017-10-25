package se.tardell.simon.bajs;


public class TaintException extends RuntimeException {
  String source;

  public TaintException(String message, String source) {
    super(message);
    this.source = source;
  }
}
