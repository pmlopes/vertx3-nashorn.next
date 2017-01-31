package xyz.jetdrone.vertx.factory.jython;

import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import org.python.core.PyDictionary;
import xyz.jetdrone.vertx.JSON;

public class JythonPyDictionaryMessageCodec implements MessageCodec<PyDictionary, Object> {

  @Override
  public void encodeToWire(Buffer buffer, PyDictionary dict) {
    String strJson = JSON.stringify(dict);
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
  public Object transform(PyDictionary dict) {
    return JSON.to_native(dict);
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

