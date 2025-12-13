package com.lox;

import java.util.List;

public class LoxCallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}
