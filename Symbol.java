package SymboLo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;


public interface Symbol {
	public String getSymbol();
	public int getPrecedence();
	public int compareTo(String s);
	public int compareTo(Symbol s);
	
	
	final int LEFT = 0;
	final int RIGHT = 1;
	
	public interface TruthEnum {
		public abstract boolean equivTo(Boolean[] operands);
		public enum Monadic implements TruthEnum{
			F {
				public boolean equivTo(Boolean[] operands) {
					return !operands[0];
				}
			}, T {
				public boolean equivTo(Boolean[] operands) {
					return operands[0];
				}
			};
			public abstract boolean equivTo(Boolean[] operands);
		}
		public enum Binary implements TruthEnum {
			FF {
				public boolean equivTo(Boolean[] operands) {
					return !(operands[LEFT] || operands[RIGHT]);
				}
			}, FT {
				public boolean equivTo(Boolean[] operands) {
					return !operands[LEFT] && operands[RIGHT];
				}
			}, TF {
				public boolean equivTo(Boolean[] operands) {
					return operands[LEFT] && !operands[RIGHT];
				}
			}, TT {
				public boolean equivTo(Boolean[] operands) {
					return operands[LEFT] && operands[RIGHT];
				}
			};
			public abstract boolean equivTo(Boolean[] operands);
		}
	}
	
	
	
	public enum Parentheses implements Symbol {
		OPEN("("), CLOSE(")");

		String symbol;
		
		Parentheses(String symbol) {
			this.symbol = symbol;
		}
		
		public String getSymbol() {
			return symbol;
		}
		public int getPrecedence() {
			return 0;
		}
		
		public int compareTo(Symbol s) {
			return symbol.compareTo(s.getSymbol());
		}
		public int compareTo(String s) {
			return symbol.compareTo(s);
		}
	}
	
	public enum Operator implements Symbol {
		NOT("~", 5, true) {
			public Logic.Operator operator() {
				return new Symbol.Logic.MonadicOperator(symbol,
						EnumSet.of(TruthEnum.Monadic.F), 
						precedence);
			}
		}, AND("&", 4, false) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FF, 
								TruthEnum.Binary.FT, 
								TruthEnum.Binary.TF), 
								precedence);
			}
		}, OR("|", 3, false) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FF), 
						precedence);
			}
		}, IF(">", 2, false) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.TF), 
						precedence);
			}
		}, IFF("<>", 1, false) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FT, 
								TruthEnum.Binary.TF), 
						precedence);
			}
		}, DEFAULT("", 0, false) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.noneOf(TruthEnum.Binary.class),
						precedence);
			}
		};
		
		public abstract Logic.Operator operator();
		
		Operator(String symbol, int precedence, boolean monadic) {
			this.symbol = symbol;
			this.precedence = precedence;
			this.monadic = monadic;
		}
		
		String symbol;
		public String getSymbol() {
			return symbol;
		}
		public int compareTo(Symbol s) {
			return compareTo(s.getSymbol());
		}
		
		public int compareTo(String s) {
			if(symbol.equals(s)) {
				return 0;
			} else {
				for(Operator o : Operator.values()) {
					if(o.compareTo(s) == 0) {
						return precedence - o.getPrecedence();
					}
				}
			}
			return 0; // Should throw error
		}
		
		int precedence;
		public int getPrecedence() {
			return precedence;
		}
		
		boolean monadic;
		public boolean isMonadic() {
			return monadic;
		}
		
	}
	
	public class Variable implements Symbol {
		
		public Variable(String symbol) {
			this.symbol = symbol;
		}
		
		String symbol;
		public String getSymbol() {
			return symbol;
		}

		public int getPrecedence() {
			return 0;
		}

		public int compareTo(Symbol s) {
			return compareTo(s.getSymbol());
		}
		public int compareTo(String s) {
			return symbol.compareTo(s);
		}
	}
	
	public abstract class Logic implements Symbol {
		String symbol;
		public String getSymbol() {
			return symbol;
		}
		
		public int compareTo(Symbol s) {
			return compareTo(s.getSymbol());
		}
		public abstract int compareTo(String s);
		ArrayList<Boolean> truthValues;
		public abstract ArrayList<Boolean> eval(ArrayList<Boolean>[] operands);
		
		public static abstract class Operator extends Logic {
			int precedence;
			public int getPrecedence() {
				return precedence;
			}
			public int compareTo(String s) {
				if(symbol.equals(s)) {
					return 0;
				} else {
					Symbol.Operator thisOperator = Symbol.Operator.DEFAULT;
					Symbol.Operator argOperator = Symbol.Operator.DEFAULT;
					for(Symbol.Operator o : Symbol.Operator.values()) {
						if(o.compareTo(symbol) == 0) {
							thisOperator = o;
						} else if(o.compareTo(s) == 0) {
							argOperator = o;
						}
					}
					return thisOperator.getPrecedence() - argOperator.getPrecedence();
				}
			}
			public abstract boolean invalid(ArrayList<Boolean>[] operands, int index);
			public ArrayList<Boolean> eval(ArrayList<Boolean>[] operands) {
				truthValues = new ArrayList<Boolean>();
				
				for(int i = 0; i < operands[0].size(); i++) {
					if(invalid(operands, i)) {
						truthValues.add(i, false);
					} else {
						truthValues.add(i, true); // true unless invalid
					}
				}
				return truthValues;
			}
		}
		
		public static class BinaryOperator extends Operator {
			EnumSet<TruthEnum.Binary> invalidCases;
			public BinaryOperator(String symbol, EnumSet<TruthEnum.Binary> invalidCases, int precedence) {
				this.symbol = symbol;
				this.invalidCases = invalidCases;
				this.precedence = precedence;
			}
			
			public boolean invalid(ArrayList<Boolean>[] operands, int index) {
				Boolean[] opArray = {operands[LEFT].get(index), operands[RIGHT].get(index)};	
				for(TruthEnum invalidCase : invalidCases) {
					if(invalidCase.equivTo(opArray)) {
						return false;
					}
				}
				return true;
			}
		}
		
		public static class MonadicOperator extends Operator {
			EnumSet<TruthEnum.Monadic> invalidCases;
			public MonadicOperator(String symbol, EnumSet<TruthEnum.Monadic> invalidCases, int precedence) {
				this.symbol = symbol;
				this.invalidCases = invalidCases;
				this.precedence = precedence;
			}
			
			public boolean invalid(ArrayList<Boolean>[] operands, int index) {
				Boolean[] opArray = {operands[0].get(index)};
				for(TruthEnum invalidCase : invalidCases) {
					if(invalidCase.equivTo(opArray)) {
						return false;
					}
				}
				return true;
			}
		}
		
		public static class Variable extends Logic {
			public int getPrecedence() {
				return 0;
			}
			static HashMap<String, ArrayList<Boolean>> varMap = null;
			public Variable(String symbol) {
				this.symbol = symbol;
				if(varMap == null) {
					varMap = new HashMap<String, ArrayList<Boolean>>();
				}
				varMap.put(symbol, null);
			}
			
			public int compareTo(String s) {
				return symbol.compareTo(s);
			}

			public ArrayList<Boolean> eval(ArrayList<Boolean>[] operands) { // no operands
				return truthValues = varMap.get(symbol); // lookup var truthValues
			}
		}
	}
	
}
