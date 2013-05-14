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
 * Simple implementation of the Netwon method to work with
 * the formula parser in {@link ComplexMathParser} and complex
 * numbers
 * 
 * @author maxstrauch
 */
@Deprecated
public class NewtonComplex {
	
	/**
	 * The Newton method implementation for complex numbers
	 * 
	 * @param f The function
	 * @param fd The 1st derivation of the function
	 * @param start The complex start value as double array with
	 * the re part on index 0 and the im part on index 1
	 * @return The resulting complex number calculated by the Newtons
	 * method
	 */
	@Deprecated
	public static double[] newton(String f, String fd, double[] start) {
		// Check if start value is null
		if (Math.abs(start[0]) + Math.abs(start[1]) < 1e-8)
			start = new double[] {0, 0};
		
		// Create the Newtons formula
		char[] formula = ("x-((" + f + ")/(" + fd + "))").toCharArray();
		char[] zero = "0".toCharArray();
		
		boolean forcedTerm = true;
		double e;
		
		// Create the start element
		double[] x0 = start.clone(), t;
		for (int i = 0; i < 1000; i++) {
			// Run the Newtons formula 
			t = ComplexMathParser.eval(formula, '+', zero, new Object[][] {
				{'x', x0}
			});
			
			// Check the result
			if (Double.isNaN(t[0]) || Double.isNaN(t[1]))
				break;
			
			// Calculate an epsilon to exit calculation
			e = Math.abs(t[0]-x0[0]) + Math.abs(t[1]-x0[1]);
			x0 = t;
			
			// If progress is smaller than 0,0...1 exit calculation
			if (e < 1e-8) {
				forcedTerm = false;
				break;
			}
		}
		
		// If computation timeout reset result to NaN
		if (forcedTerm)
			return new double[] {Double.NaN, Double.NaN};
		
		// Return the "real" result
		return x0;
	}
	
}
