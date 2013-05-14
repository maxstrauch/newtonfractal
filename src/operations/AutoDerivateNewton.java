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

import helpers.SimpleComplexMath;

/**
 * Simple class wrapping the Newton method with automatic
 * derivation calculation (numeric way)
 * 
 * @author maxstrauch
 */
public class AutoDerivateNewton {

	/**
	 * Simple automation to execute a formula with a value
	 * for the variable x 
	 * 
	 * @param f The formula
	 * @param in The input x
	 * @return The resulting number
	 */
	private static double[] f(String f, double[] in) {
		return ComplexMathParser.eval(f.toCharArray(), '+', "0".toCharArray(), 
				new Object[][] {
			{'x', in}
		});
	}
	
	/**
	 * Alternative implementation of the Newton method in manner
	 * of {@link NewtonComplex} with automatic calculation of the
	 * derivation
	 * 
	 * @param f The function term
	 * @param start The initial complex number
	 * @return The root for the input
	 */
	public static double[] newton(String f, double[] start) {
		// Check if start value is null
		if (Math.abs(start[0]) + Math.abs(start[1]) < 1e-8)
			start = new double[] {0, 0};

		boolean forcedTerm = true;
		double e, h = 1e-8;
		
		// Create the start element
		double[] x0 = start.clone(), t;
		for (int i = 0; i < 1000; i++) {
			double[] r1 = f(f, new double[] {x0[0] + h, x0[1]});
			double[] r2 = f(f, x0);
			double[] down = {h, 0};
			double[] ab = SimpleComplexMath.div(SimpleComplexMath.sub(r1, r2), down);
			
			// Round the calculated derivation at location x0
			ab = new double[] {
					Math.round(ab[0]*100000.0)/100000.0,
					Math.round(ab[1]*100000.0)/100000.0
			};
			
			// Calculate the normal Newton method
			t = SimpleComplexMath.sub(x0, SimpleComplexMath.div(r2, ab));
			
			// Round the result
			t = new double[] {
					Math.round(t[0]*100000.0)/100000.0,
					Math.round(t[1]*100000.0)/100000.0
			};
			
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
