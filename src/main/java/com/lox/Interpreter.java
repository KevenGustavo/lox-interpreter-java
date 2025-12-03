package com.craftinginterpreters.lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  final Environment globals = new Environment();
  private Environment environment = globals;

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  void executeBlock(List<Stmt> stmts, Environment env) {
    Environment previous = this.environment;
    try {
      this.environment = env;
      for (Stmt stmt : stmts) {
        execute(stmt);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }
    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }
    return null;
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private boolean isTruthy(Object value) {
    if (value == null) return false;
    if (value instanceof Boolean) return (boolean)value;
    return true;
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double)right;
      case BANG:
        return !isTruthy(right);
    }
    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);
    return value;
  }

  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
        }
        if (left instanceof String || right instanceof String) {
          return stringify(left) + stringify(right);
        }
        throw new RuntimeError(expr.operator, "Operands must be numbers or strings.");

      case MINUS:
        checkNumbers(expr, left, right);
        return (double)left - (double)right;

      case STAR:
        checkNumbers(expr, left, right);
        return (double)left * (double)right;

      case SLASH:
        checkNumbers(expr, left, right);
        return (double)left / (double)right;

      case GREATER:
        checkNumbers(expr, left, right);
        return (double)left > (double)right;

      case GREATER_EQUAL:
        checkNumbers(expr, left, right);
        return (double)left >= (double)right;

      case LESS:
        checkNumbers(expr, left, right);
        return (double)left < (double)right;

      case LESS_EQUAL:
        checkNumbers(expr, left, right);
        return (double)left <= (double)right;

      case BANG_EQUAL:
        return !isEqual(left, right);

      case EQUAL_EQUAL:
        return isEqual(left, right);
    }

    return null;
  }

  private void checkNumberOperand(Token op, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(op, "Operand must be a number.");
  }

  private void checkNumbers(Expr expr, Object a, Object b) {
    if (a instanceof Double && b instanceof Double) return;
    throw new RuntimeError(((Expr.Binary)expr).operator, "Operands must be numbers.");
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  private String stringify(Object obj) {
    if (obj == null) return "nil";
    if (obj instanceof Double) {
      String text = obj.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }
    return obj.toString();
  }
}
