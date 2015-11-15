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
						EnumSet.of(TruthEnum.Monadic.T), 
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
		}, IFF("=", 1, false) {
			public Logic.Operator operator() {
				return new Symbol.Logic.BinaryOperator(symbol, 
						EnumSet.of(TruthEnum.Binary.FT, 
								TruthEnum.Binary.TF), 
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
			return symbol.compareTo(s);
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
			return Integer.MAX_VALUE;
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
		public int compareTo(String s) {
			return symbol.compareTo(s);
		}
		ArrayList<Boolean> truthValues;
		public abstract ArrayList<Boolean> eval(ArrayList<Boolean>[] operands);
		public ArrayList<Boolean> getTruthValues() {
			return truthValues;
		}
		
		public static abstract class Operator extends Logic {
			int precedence;
			public int getPrecedence() {
				return precedence;
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
				Boolean[] opArray = {operands[RIGHT].get(index), operands[LEFT].get(index)};	
				for(TruthEnum invalidCase : invalidCases) {
					if(invalidCase.equivTo(opArray)) {
						return true;
					}
				}
				return false;
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
						return true;
					}
				}
				return false;
			}
		}
		
		public static class Variable extends Logic {
			public int getPrecedence() {
				return Integer.MAX_VALUE;
			}
			static HashMap<String, ArrayList<Boolean>> varMap = null;
			public Variable(String symbol) {
				this.symbol = symbol;
				if(varMap == null) {
					varMap = new HashMap<String, ArrayList<Boolean>>();
				}
				varMap.put(this.symbol, null);
			}

			public ArrayList<Boolean> eval(ArrayList<Boolean>[] operands) { // no operands
				return truthValues = varMap.get(symbol); // lookup var truthValues
			}
			
			public static int countAndEnumTVals() {
				int permutations = 2 << (varMap.size() - 1);
				int j = 1;
				Boolean tVal = true;
				for(String var : varMap.keySet()) {
					ArrayList<Boolean> tMappings = new ArrayList<Boolean>();
					for(int i = 0; i < permutations; i++) {
						if(i % j == 0) {
							tVal = !tVal;
						}
						tMappings.add(i, tVal);
					}
					varMap.put(var, tMappings);
					j++;
				}
				return varMap.size();
			}
		}
		
		public static class NonTruth extends Logic {
			public NonTruth(String symbol) {
				this.symbol = symbol;
			}
			public int getPrecedence() {
				return Integer.MAX_VALUE;
			}
			public ArrayList<Boolean> eval(ArrayList<Boolean>[] operands) {
				return null;
			}
			
		}
	}
}
