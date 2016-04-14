package com.jetdrone.nashorn.next;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

@FunctionalInterface
public interface Generator<T> {

  Object call(T arg);

  static <T> JSObject create(Generator<T> generator) {
    return new AbstractJSObject() {

      @Override
      @SuppressWarnings("unchecked")
      public Object call(Object o, Object... arguments) {
        T arg = null;
        if (arguments != null && arguments.length > 0) {
          arg = (T) arguments[0];
        }
        return generator.call(arg);
      }

      @Override
      public boolean isFunction() {
        return true;
      }
    };
  }
}
