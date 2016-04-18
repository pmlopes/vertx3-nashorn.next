package com.jetdrone.nashorn.next;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

public class NashornJSObjectMessageCodec implements MessageCodec<ScriptObjectMirror, Object> {

  private final ScriptEngine engine;
  private final Object JSON;
  private final Object Java;

  public NashornJSObjectMessageCodec(ScriptEngine engine) {
    this.engine = engine;
    JSON = engine.get("JSON");
    Java = engine.get("Java");
  }

  @Override
  public void encodeToWire(Buffer buffer, ScriptObjectMirror jsObject) {
    try {
      String strJson = (String) ((Invocable) engine).invokeMethod(JSON, "stringify", jsObject);
      byte[] encoded = strJson.getBytes(CharsetUtil.UTF_8);
      buffer.appendInt(encoded.length);
      Buffer buff = Buffer.buffer(encoded);
      buffer.appendBuffer(buff);
    } catch (ScriptException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonObject decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    byte[] encoded = buffer.getBytes(pos, pos + length);
    String str = new String(encoded, CharsetUtil.UTF_8);
    return new JsonObject(str);
  }

  @Override
  public Object transform(ScriptObjectMirror jsObject) {
    try {
      Object compat = ((Invocable) engine).invokeMethod(Java, "asJSONCompatible", jsObject);
      if (compat instanceof Map) {
        return new JsonObject((Map) compat);
      }
      if (compat instanceof List) {
        return new JsonArray((List) compat);
      }

      return compat;
    } catch (ScriptException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
