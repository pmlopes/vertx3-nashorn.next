package xyz.jetdrone.vertx;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.python.core.PyDictionary;
import org.python.core.PyList;

public final class JSON {

  private JSON() {}

  public static JsonObject to_native(PyDictionary dict) {
    if (dict == null) {
      return null;
    }
    return new JsonObject(dict);
  }

  public static JsonArray to_native(PyList list) {
    if (list == null) {
      return null;
    }
    return new JsonArray(list);
  }

  public static PyDictionary parse(JsonObject json) {
    if (json == null) {
      return null;
    }
    PyDictionary dict = new PyDictionary();
    dict.putAll(json.getMap());
    return dict;
  }

  public static PyList parse(JsonArray json) {
    if (json == null) {
      return null;
    }
    return new PyList(json.getList());
  }

  public static String stringify(PyDictionary dict) {
    if (dict == null) {
      return null;
    }

    return to_native(dict).encode();
  }

  public static String stringify(PyList list) {
    if (list == null) {
      return null;
    }

    return to_native(list).encode();
  }

  public static String stringify(JsonObject dict) {
    if (dict == null) {
      return null;
    }

    return dict.encode();
  }

  public static String stringify(JsonArray list) {
    if (list == null) {
      return null;
    }

    return list.encode();
  }
}
