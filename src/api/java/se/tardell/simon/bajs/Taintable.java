package se.tardell.simon.bajs;

/**
  Marker interface for taintability
 */
public interface Taintable {
  void setTaint(boolean value);
  boolean isTainted();
}
