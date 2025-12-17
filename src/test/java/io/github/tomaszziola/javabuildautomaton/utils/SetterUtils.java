package io.github.tomaszziola.javabuildautomaton.utils;

import java.util.function.BiConsumer;

public class SetterUtils {

  public static <T, V> void setField(T entity, BiConsumer<T, V> setter, V value) {
    setter.accept(entity, value);
  }
}
