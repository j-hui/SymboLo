package SymboLo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;


public interface Symbol {
	public String getSymbol();
	public int compareTo(String s);
	
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

		public int compareTo(String s) {
			return symbol.compareTo(s);
		}
		
	}
	
	public enum Operator implements Symbol {
		NOT("~", 5) {
			public Logic.Operator operator() {
				return new Symbol.Logic.MonadicOperator(symbol,
						EnumSet.of(TruthEnum.Monadic.F));
			}
		}, AND("&", 4) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FF, TruthEnum.Binary.FT, TruthEnum.Binary.TF));
			}
		}, OR("|", 3) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FF));
			}
		}, IF(">", 2) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.TF));
			}
		}, IFF("<>", 1) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FT, TruthEnum.Binary.TF));
			}
		}, DEFAULT("", 0) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.noneOf(TruthEnum.Binary.class));
			}
		};
		
		public abstract Logic.Operator operator();
		
		Operator(String symbol, int precedence) {
			this.symbol = symbol;
			this.precedence = precedence;
		}
		
		String symbol;
		public String getSymbol() {
			return symbol;
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
		
	}
	
	public abstract class Logic implements Symbol {
		String symbol;
		public String getSymbol() {
			return symbol;
		}
		
		public abstract int compareTo(String s);
		ArrayList<Boolean> truthValues;
		public abstract ArrayList<Boolean> eval(ArrayList<Boolean>[] operands);
		
		
		public static abstract class Operator extends Logic {
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
			public BinaryOperator(String symbol, EnumSet<TruthEnum.Binary> invalidCases) {
				this.symbol = symbol;
				this.invalidCases = invalidCases;
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
			public MonadicOperator(String symbol, EnumSet<TruthEnum.Monadic> invalidCases) {
				this.symbol = symbol;
				this.invalidCases = invalidCases;
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
			static HashMap<String, ArrayList<Boolean>> varMap;
			
			public Variable(String symbol, HashMap<String, ArrayList<Boolean>> varMap) {
				this.symbol = symbol;
				Variable.varMap = varMap;
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
