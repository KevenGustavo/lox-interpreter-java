package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {

  final String name;
  private final Map<String, LoxFunction> methods;

  LoxClass(String name, Map<String, LoxFunction> methods) {
    this.name = name;
    this.methods = methods;
  }

  LoxFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }
    return null;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // 1. Cria a instância
    LoxInstance instance = new LoxInstance(this);

    // 2. Procura por init()
    LoxFunction initializer = findMethod("init");
    if (initializer != null) {
      // 3. Liga 'this' e chama o init
      initializer.bind(instance).call(interpreter, arguments);
    }

    // 4. Sempre retorna a instância
    return instance;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    if (initializer == null) return 0;
    return initializer.arity();
  }

  @Override
  public String toString() {
    return name;
  }
}
