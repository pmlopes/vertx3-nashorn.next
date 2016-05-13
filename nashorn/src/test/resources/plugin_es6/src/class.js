import sum from 'src/sum';

export default class A {

  constructor(a) {
    this.name = a;
  }

  static add(a, b) {
    return sum(a, b);
  }

  sayHello() {
    return 'Hello ' + this.name + '!';
  }
}
