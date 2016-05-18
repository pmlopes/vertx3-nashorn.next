define(['vertx', 'classpath:type!io.vertx.polyglot.blockchain.Blockchain'], function (vertx, Blockchain) {

  var bs = new Blockchain(vertx);

  bs.exceptionHandler(function (e) {
    console.trace(e);
  });

  bs.subscribeStatus(function (msg) {
    console.info(msg);
  });

  bs.connect('wss://ws.blockchain.info/inv', function () {
    console.debug("Connected!");

    bs.subscribeBlocks(function (res) {
      console.log(JSON.stringify(res));
    });

    bs.subscribeUnconfirmed(function (res) {
      console.log(JSON.stringify(res));
    });
  });
});
