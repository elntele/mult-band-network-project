package br.cns24.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileMap implements Cloneable {


  private HashMap<Integer, Set<Integer>> file;

  public FileMap() {
    this.file = new HashMap<>();
  }

  public FileMap(HashMap<Integer, Set<Integer>> file) {
    this.file = file;
  }

  // Getter
  public HashMap<Integer, Set<Integer>> getFile() {
    return file;
  }

  // Add method for convenience
  public void put(Integer key, Set<Integer> value) {
    this.file.put(key, value);
  }

  // Remove method
  public void remove(Integer key) {
    this.file.remove(key);
  }

  // Clone method
  @Override
  public FileMap clone() {
    try {
      FileMap cloned = (FileMap) super.clone();
      cloned.file = new HashMap<>(); // Deep copy of the map

      for (Map.Entry<Integer, Set<Integer>> entry : this.file.entrySet()) {
        cloned.file.put(entry.getKey(), new HashSet<>(entry.getValue())); // Deep copy of sets
      }

      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Error cloning FileMap", e);
    }
  }

}
