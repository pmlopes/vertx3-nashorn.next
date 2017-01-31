package xyz.jetdrone.vertx.factory.nashorn;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptEngine;
import java.util.List;
import java.util.Map;

public class NashornJSObjectMessageCodec implements MessageCodec<ScriptObjectMirror, Object> {

  private final ScriptObjectMirror JSON;
  private final ScriptObjectMirror Java;

  public NashornJSObjectMessageCodec(ScriptEngine engine) {
    JSON = (ScriptObjectMirror) engine.get("JSON");
    Java = (ScriptObjectMirror) engine.get("Java");
  }

  public NashornJSObjectMessageCodec(ScriptObjectMirror JSON, ScriptObjectMirror Java) {
    this.JSON = JSON;
    this.Java = Java;
  }

  @Override
  public void encodeToWire(Buffer buffer, ScriptObjectMirror jsObject) {
    String strJson = (String) JSON.callMember("stringify", jsObject);
    byte[] encoded = strJson.getBytes(CharsetUtil.UTF_8);
    buffer.appendInt(encoded.length);
    Buffer buff = Buffer.buffer(encoded);
    buffer.appendBuffer(buff);
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
    Object compat = Java.callMember("asJSONCompatible", jsObject);
    if (compat instanceof Map) {
      return new JsonObject((Map) compat);
    }
    if (compat instanceof List) {
      return new JsonArray((List) compat);
    }

    return compat;
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
