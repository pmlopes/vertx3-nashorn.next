//my/shirt.js now has some dependencies, a cart and inventory
//module in the same directory as shirt.js
define("my/shirt", ["./cart", "./inventory"], function (cart, inventory) {
  print('shirt');
  print(test);
  test.complete();
  //return an object to define the "my/shirt" module.
  return {
    color: "blue",
    size: "large",
    addToCart: function () {
      inventory.decrement(this);
      cart.add(this);
    }
  }
});