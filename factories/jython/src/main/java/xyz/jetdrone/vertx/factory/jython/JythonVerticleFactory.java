package xyz.jetdrone.vertx.factory.jython;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.VerticleFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.File;

public class JythonVerticleFactory implements VerticleFactory {

  private Vertx vertx;

  @Override
  public void init(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public String prefix() {
    return "py";
  }

  @Override
  public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {

    final File verticle = ((VertxInternal) vertx).resolveFile(VerticleFactory.removePrefix(verticleName));

    final Loader loader;

    synchronized (this) {
      // create a new CommonJS loader
      loader = new Loader(vertx);
      final Bindings bindings = loader.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
      // expose vertx
      bindings.put("vertx", vertx);
    }

    return new Verticle() {

      private Vertx vertx;
      private Context context;

      @Override
      public Vertx getVertx() {
        return vertx;
      }

      @Override
      public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.context = context;
      }

      @Override
      public void start(Future<Void> startFuture) throws Exception {
        // expose config
        if (context != null && context.config() != null) {
          loader.config(context.config());
        }

        loader.main(verticle.getCanonicalPath());
        startFuture.complete();
      }

      @Override
      public void stop(Future<Void> stopFuture) throws Exception {
        stopFuture.complete();
      }
    };
  }
}
