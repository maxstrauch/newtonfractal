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
package helpers;

/**
 * This class contains some static methods to do basic
 * complex math operations
 * 
 * @author maxstrauch
 */
public class SimpleComplexMath {

	/**
	 * Adds two complex numbers: <code>a + b</code>
	 * 
	 * @param a A complex number
	 * @param b A complex number
	 * @return The resulting complex number
	 */
	public static double[] add(double[] a, double[] b) {
		return new double[] { a[0] + b[0], a[1] + b[1] };
	}
	
	/**
	 * Subtracts two complex numbers: <code>a - b</code>
	 * 
	 * @param a A complex number
	 * @param b A complex number
	 * @return The resulting complex number
	 */
	public static double[] sub(double[] a, double[] b) {
		return new double[] { a[0] - b[0], a[1] - b[1] };
	}

	/**
	 * Multiplies two complex numbers: <code>a * b</code>
	 * 
	 * @param a A complex number
	 * @param b A complex number
	 * @return The resulting complex number
	 */
	public static double[] mult(double[] a, double[] b) {
		return new double[] {
				a[0] * b[0] - a[1] * b[1],
				a[0] * b[1] + b[0] * a[1]
		};
	} 

	/**
	 * Divides two complex numbers: <code>a / b</code>
	 * 
	 * @param a A complex number
	 * @param b A complex number
	 * @return The resulting complex number
	 */
	public static double[] div(double[] a, double[] b) {
		double base = b[0] * b[0] + b[1] * b[1];
		return new double[] {
				(b[0] * a[0] + b[1] * a[1]) / base,
				(b[0] * a[1] - b[1] * a[0]) / base
		};
	}

	/**
	 * Calculate the power: <code>a^b</code>
	 * 
	 * @param a A complex number (base)
	 * @param b A complex number (exponent)
	 * @return The resulting complex number
	 */
	public static double[] pow(double[] a, double[] b) {
		long times = Math.round(b[0]) - 1;
		
		b = a.clone();
		while (times > 0) {
			a = new double[] {
					a[0] * b[0] - a[1] * b[1],
					a[0] * b[1] + b[0] * a[1]
			};
			times--;
		}
		return a;
	}
	
	/**
	 * Formats a complex number represented by a 1d array
	 * field with two fields filled
	 * 
	 * @param c The array field which represents a complex
	 * number
	 * @return <code>null</code> if the complex number is 
	 * Double.NaN (re and im) or the input array has not exactly
	 * two fields. Otherwise the string representation of the
	 * complex number is returned
	 */
	public static String formatComplex(double[] c) {
		if (c.length != 2 || Double.isNaN(c[0]) || Double.isNaN(c[1]))
			return null;
		
		// Round input
		double[] r = new double[] {
				Math.round(c[0]*1000.0)/1000.0,
				Math.round(c[1]*1000.0)/1000.0
		};
		
		if (r[0] == 0 && r[1] == 0)
			return "0";
		
		// Build string
		return (r[0] == 0.0 ? "" : r[0]) + " " + (r[1] == 0.0 ? "" : 
			(r[1] < 0 ? "-" : "+") + " " + Math.abs(r[1]) + " i");
	}
	
}
