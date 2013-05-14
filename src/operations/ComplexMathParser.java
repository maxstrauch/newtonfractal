/*
 * Copyright 2013 maxstrauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package operations;

/**
 * A very basic parser for mathematical expressions provided as
 * string or character array
 * 
 * @author maxstrauch
 */
public class ComplexMathParser {
	
	/**
	 * All supported operators
	 */
    public final static char[] OPS = {'+', '-', '/', '*', '^'};
    
    /**
     * Evaluates a mathematical expression
     * 
     * @param leftSide The left side of the expression
     * @param op The operator between the two sides
     * @param rightSide The right side of the expression
     * @param vars A map of variables and their values
     * @return The result as a complex number
     */
    public static double[] eval(char[] leftSide, char op, char[] rightSide,
    		Object[][] vars) {
    	int p;
		double[] leftResult = { 0.0, 0.0 }, rightResult = { 0.0, 0.0 };

		// Work on the left side
		leftSide = removeOuterBrackets(leftSide);
		p = findOperator(leftSide);
		if (p > -1) {
			leftResult = eval(substring(leftSide, 0, p), leftSide[p],
					substring(leftSide, p + 1, leftSide.length), vars);
		} else {
			leftResult = parseVar(leftSide, vars);
		}

		// Work on the right side
		rightSide = removeOuterBrackets(rightSide);
		p = findOperator(rightSide);
		if (p > -1) {
			rightResult = eval(substring(rightSide, 0, p), rightSide[p],
					substring(rightSide, p + 1, rightSide.length), vars);
		} else {
			rightResult = parseVar(rightSide, vars);
		}
		
		// Check if some of the result is not defined
		if (Double.isNaN(leftResult[0]) || Double.isNaN(leftResult[1])
				|| Double.isNaN(rightResult[0]) || Double.isNaN(rightResult[1]))
			return new double[] { Double.NaN, Double.NaN };

	    // Calculate the result
		if (op == '+') {
			return new double[] { leftResult[0] + rightResult[0],
					leftResult[1] + rightResult[1] };
		} else if (op == '-') {
			return new double[] { leftResult[0] - rightResult[0],
					leftResult[1] - rightResult[1] };
		} else if (op == '*') {
			return new double[] {
					leftResult[0] * rightResult[0] - leftResult[1]
							* rightResult[1],
					leftResult[0] * rightResult[1] + rightResult[0]
							* leftResult[1], };
		} else if (op == '/') {
			double base = rightResult[0] * rightResult[0] + rightResult[1]
					* rightResult[1];
			return new double[] {
					(rightResult[0] * leftResult[0] + rightResult[1]
							* leftResult[1])
							/ base,
					(rightResult[0] * leftResult[1] - rightResult[1]
							* leftResult[0])
							/ base };
		} else if (op == '^') {
			long times = Math.round(rightResult[0]) - 1;
			rightResult = leftResult.clone();
			while (times > 0) {
				leftResult = new double[] {
						leftResult[0] * rightResult[0] - leftResult[1]
								* rightResult[1],
						leftResult[0] * rightResult[1] + rightResult[0]
								* leftResult[1], };
				times--;
			}
			return leftResult;
		}
    	 
    	 throw new UnsupportedOperationException("Operator unknown");
    }
    
    /**
     * Parses a terminal of a formula which is:
     *  - a variable
     *  - a natural number (e.g. 42)
     *  - a floating point number (e.g. 42.42)
     *  
     * @param var The terminal to parse
     * @param vars Map of input variables and their value
     * @return The result represented as a complex number
     */
    private static double[] parseVar(char[] var, Object[][] vars) {
    	if (var.length == 1 && Character.isLowerCase(var[0])) {
    		for (Object[] varss : vars) {
    			if (((char) varss[0]) == var[0]) {
    				return (double[]) varss[1];
    			}
    		}
    		throw new IllegalArgumentException("Unknown variable: " + var[0]);
    	}
    	
    	double num = 0, fac = 1;
    	int indexOfP = indexOf('.', var, 0);
    	indexOfP = indexOfP < 0 ? var.length-1 : indexOfP;
    	
    	for (int i = 0; i < var.length; i++) {
    		if (!Character.isDigit(var[i]))
    			continue;
    		
    		for (int j = 0; j < Math.abs(indexOfP-i); j++)
    			fac *= 10;
    		
			num += (((int) var[i])-48) * (indexOfP-i < 0 ? 1/fac : fac);
			fac = 1;
		}
    	
    	return new double[] { num, 0 };
    }
    
    /**
     * A {@link String#substring(int, int)} clone to work on
     * character arrays. Without any error handling
     */
    private static char[] substring(char[] car, int start, int end) {
    	char[] res = new char[end-start];
    	System.arraycopy(car, start, res, 0, end-start);
    	return res;
    }
    
    /**
     * Removes the outer brackets of a formula without
     * breaking any other bracket rules
     * 
     * @param c The character array to work on (e.g. string)
     * @return The modified character array
     */
    private static char[] removeOuterBrackets(char[] c) {
    	if (c.length < 3)
    		return c;
    	
    	int nc = indexOf(')', c, 0), no = indexOf('(', c, 1);
    	while (c[0] == '(' && c[c.length-1] == ')' && no < nc) {
    		System.arraycopy(c, 1, c = new char[c.length-2], 0, c.length);
    		nc = indexOf(')', c, 0);
    		no = indexOf('(', c, 1);
    	}
    	
    	return c;
    }
    
    /**
     * Finds the first position of the next operator
     * sign to work on with respect to the hierarchy of
     * the mathematical operators
     *  
     * @param c The character array to search on (e.g. string)
     * @return The position
     */
    private static int findOperator(char[] c) {
    	boolean br = false;
    	int indexOfLast = -1;
    	int prio = OPS.length;
    	
    	for (int i = 0; i < c.length; i++) {
    		if (c[i] == '(' || c[i] == ')')
    			br = c[i] == '(';
    		if (br)
    			continue;
    		for (int j = OPS.length-1; j >= 0; j--) {
    			if (c[i] == OPS[j] && j < prio) {
    				indexOfLast = i;
    				prio = j;
    			}
    		}
    	
    	}
    	
    	return indexOfLast;
    }
    
    /**
     * Finds the index of the first occurrence of a 
     * char in an array of chars
     * 
     * @param c Character to find
     * @param cs Array of characters (e.g. string)
     * @param offset Offset position to start from
     * @return The index of the first occurrence or -1
     * if character is not found
     */
    public static int indexOf(char c, char[] cs, int offset) {
    	for (int i = offset; i < cs.length; i++)
			if (cs[i] == c)
				return i;
    	return -1;
    }
    
}
