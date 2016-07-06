define(['vertx', 'classpath:type!io.vertx.ext.mongo.MongoClient', 'classpath:type!io.vertx.ext.auth.mongo.MongoAuth'], function (vertx, MongoClient, MongoAuth) {
  
  var client = MongoClient.createShared(vertx, JSON.native({db_name: 'auth_test2'}));
  
  var authProvider = MongoAuth.create(client, JSON.native({}));
  
  var authInfo = {
    "username": "tim",
    "password": "mypassword"
  };


  var roles = [];
  var permission = [];

  client.remove('user', JSON.native({username: 'tim'}), function (res) {
    if (res.succeeded()) {
      console.log('cleaned up users');

      authProvider.insertUser(authInfo.username, authInfo.password, roles, permission, function (res) {
        if (res.succeeded()) {
          console.log('created user with _id ' + res.result());

          authProvider.authenticate(JSON.native(authInfo), function (res) {
            if (res.succeeded()) {
              var user = res.result();
              console.log("User " + user.principal() + " is now authenticated");
            } else {
              res.cause().printStackTrace();
            }
          });
        }
      });

    } else {
      res.cause().printStackTrace();
    }
  });
});


