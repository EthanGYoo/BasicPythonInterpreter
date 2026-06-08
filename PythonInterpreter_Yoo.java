import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.EmptyStackException;



//Notes: This is a "Python Interpreter" although it really only handles basic tasks such as storing numeric values in variables, calculating expressions, and printing numeric values. Errors are printed when the interpreter can not handle the code. The Interpreter is designed only to handle Integers and Doubles.

/**
Class which interprets Python code to run in Java
*/
class PythonInterpreter_Yoo{
   
   private static HashMap<String, Object> symbolTable = new HashMap();
   private static Calculator calc = new Calculator();

   /**
   Runs the program
   */
   public static void main(String[] args){
   
      Scanner s = new Scanner(System.in);
      ArrayList<String> lines = new ArrayList();
      
      System.out.println("Welcome to my basic Python Interpreter! Enter 'quit' at any time to finish and run your code");
      
      while (true){
         String line = s.nextLine();
         if (line.equals("quit")) break;
         lines.add(line);
      }
      
      for (int i = 0; i < lines.size(); i++){
         String line = lines.get(i);
         
         if (line.contains("=")){
            String[] lineThings = line.split("=");
            lineThings[0].trim();
            
            if (validVariableName(lineThings[0])){
               System.out.println("Error (line " + (i+1) + "): Invalid declaration");
               break;
            }
            
            Object variable = evaluateExpression(lineThings[1]);
            if (variable == null){
               System.out.println("Error (line " + (i+1) + "): Invalid Expression");
               break;
            }
            symbolTable.put(lineThings[0].trim(), variable);
         }
         else if(line.startsWith("print(") && line.endsWith(")")){
            try{
               Object statement = evaluateExpression(line.substring(6, line.length() - 1));
               if (statement == null){
                  System.out.println("Error (line " + (i+1) + "): Invalid Expression");
                  break;
               }
               System.out.println(statement);
            }
            catch (Exception e){
               System.out.println("Error (line " + (i+1) + "): Invalid Print Statement");
               break;
            }
         }
         else{
            System.out.println("Error (line " + (i+1) + "): Unknown Command " + line);
         }
         
      }
      
   }
   
   /**
   Checks if variable can be stored as a variable
   */
   public static boolean validVariableName(String variable){
      if (variable.contains(" ")) return false;
      try{
         Double.parseDouble(variable);
         return true;
      }
      catch(Exception e){
         return false;
      }
   }
   
   /**
   Evaluates the expression inputted even with variables resulting in an Integer or a Double depending on whether a Double is within the expression. Returns null if expression is invalid
   */
   public static Object evaluateExpression(String expression){
      for (String key: symbolTable.keySet()){
         expression = expression.replaceAll(key, symbolTable.get(key).toString());
      }
      
      expression = calc.fixFormatting(expression);
      
      try{
         expression = expression.trim();
         Double evaluated = new Double(calc.calculateDouble(expression));
         int intEvaluated = evaluated.intValue();
         if (evaluated == intEvaluated) return intEvaluated;
         return evaluated;
      }
      catch(Exception e){
         return null;
      }
   }
}


/**
Calculator class which performs basic functions of a calculator utilizing stacks
*/
class Calculator{
   LinkedStack<String> stack;
   LinkedStack<Double> doubleStack;
   String[][] precedence = {{"+", "-"}, {"*", "/"}, {"^"}};
   
   enum Type {Operator, Operand, LeftP, RightP}
   
   /**
   Constructor method
   */
   public Calculator(){
   }
   
   /**
   Method which fixes formatting to be compatible with the rest of the calculator methods
   */
   public String fixFormatting(String equation){
      String ret = "";
      for (int i = 0; i < equation.length(); i++){
         String c = equation.substring(i, i + 1);
         boolean isOperator = false;
         
         for (String[] j: precedence){
            for (String k: j){
               if (k.equals(c)) isOperator = true;
            }
         }
         if (c.equals("(")) isOperator = true;
         if (c.equals(")")) isOperator = true;
         
         if (isOperator) ret += " " + c + " ";
         else ret += c;
      }
      
      ret = fixSpaces(ret);
      
      return ret;
   }
   
   /**
   Method which is utilized by the fixFormatting method to ensure there aren't too many spaces within the equation String
   */
   public String fixSpaces(String str){
      boolean fixed = true;
      String ret = "";
      
      for (int i = 0; i < str.length() - 1; i++){
         String check = str.substring(i, i + 2);
         if (check.equals("  ")) fixed = false;
         else ret += str.substring(i, i + 1);
      }
      
      ret += str.substring(str.length() - 1);
      
      if (fixed) return ret;
      return fixSpaces(ret);
   }
   
   /**
   Method which checks if the equation has balanced parentheses
   */
   public boolean hasBalancedParentheses(String equation){
      String[] split = equation.split(" ");
      int left = 0;
      int right = 0;
      for (String op: split){
         if (op.equals("(")) left++;
         if (op.equals(")")) right++;
      }
      
      return left == right;
   }
   
   /**
   Method which converts a given equation to Postfix formatting
   */
   public String convertToPostFix(String equation){
      String ret = "";
      String[] split = equation.split(" ");
      stack = new LinkedStack();
      int beforePrecedence = 0;
      int presentPrecedence = 0;
      
      for (String op: split){
         Type type = Type.Operand;
         if (op.equals("(")) type = Type.LeftP;
         if (op.equals(")")) type = Type.RightP;
         for (int i = 0; i < precedence.length; i++){
            for (String j: precedence[i]){
               if (j.equals(op)){
                  type = Type.Operator;
                  presentPrecedence = i;
               }
            }
         }
         
         switch(type){
            case Operand:
               ret += op + " ";
               break;
            case LeftP:
               stack.push(op);
               break;
            case RightP:
               while (!stack.peek().equals("(")){
                  ret += stack.pop() + " ";
               }
               stack.pop();
               break;
            case Operator:
               if (stack.isEmpty()) stack.push(op);
               else{
                  if (stack.peek().equals("(")){
                     stack.push(op);
                  }
                  else{
                     for (int i = 0; i < precedence.length; i++){
                        for (String j: precedence[i]){
                           if (j.equals(stack.peek())) beforePrecedence = i;
                        }
                     }
                     
                     if (presentPrecedence > beforePrecedence) stack.push(op);
                     else{
                        while (presentPrecedence <= beforePrecedence){
                           ret += stack.pop() + " ";
                           
                           if (stack.isEmpty()){
                              stack.push(op);
                              break;
                           }
                           
                           if (stack.peek().equals("(")){
                              stack.push(op);
                              break;
                           }
                           
                           for (int i = 0; i < precedence.length; i++){
                              for (String j: precedence[i]){
                                 if (j.equals(stack.peek())) beforePrecedence = i;
                              }
                           }
                        }
                     }
                  }
               }
               break;
         }
      }
      
      while (!stack.isEmpty()) ret += stack.pop() + " ";
      
      return ret;
   }
   
   /**
   Method which evaluates a given Postfix
   */
   public double evaluatePostFix(String postFix){
      String[] split = postFix.split(" ");
      doubleStack = new LinkedStack();
      
      for (String op: split){
         Type type = Type.Operand;
         for (int i = 0; i < precedence.length; i++){
            for (int j = 0; j < precedence[i].length; j++){
               if (precedence[i][j].equals(op)) type = Type.Operator;
            }
         }
         
         switch(type){
            case Operand:
               doubleStack.push(Double.parseDouble(op));
               break;
            case Operator:
               double num2 = doubleStack.pop();
               double num1 = doubleStack.pop();
               
               if (op.equals("+")) doubleStack.push(num1 + num2);
               if (op.equals("-")) doubleStack.push(num1 - num2);
               if (op.equals("*")) doubleStack.push(num1 * num2);
               if (op.equals("/")) doubleStack.push(num1 / num2);
               if (op.equals("^")) doubleStack.push(Math.pow(num1, num2));
               break;
         }
         
      }
      
      return doubleStack.pop();
   }
   
   /**
   Method which returns a double calculation to the given equation
   */
   public double calculateDouble(String equation){
      return evaluatePostFix(convertToPostFix(equation));
   }
   
   /**
   Method which returns an int calculation to the given equation
   */
   public int calculateInt(String equation){
      return (int) calculateDouble(equation);
   }

}

/**
Interface ZoStack which contains all the methods needed for class LinkedStack
*/
interface ZoStack<E>{
   boolean isEmpty();
   E push(E o);
   E pop();
   E peek();
}



/**
Class Linked Stack which utilizes a LinkedList to perform as a stack data structure
*/
class LinkedStack<E> implements ZoStack<E>{
   LinkedList<E> stack;

   /**
   Constructor method for a Linked Stack
   */
   public LinkedStack(){
      stack = new LinkedList<E>();
   }
   
   /**
   Returns whether the stack is empty or not
   */
   public boolean isEmpty(){
      try{
         return stack.getFirst() == null;
      }
      catch(Exception NoSuchElementException){
         return true;
      }
   }
   
   /**
   Pushes an element onto the top of the stack
   */
   public E push(E o){
      stack.add(o);
      return o;
   }
   
   /**
   Pops the element from the top of the stack
   */
   public E pop(){
      if (isEmpty()) throw new EmptyStackException();
      E ret = stack.getLast();
      stack.removeLast();
      return ret;
   }
   
   /**
   Returns the element at the top of the stack without removing it from the stack
   */
   public E peek(){
      return stack.getLast();
   }
}