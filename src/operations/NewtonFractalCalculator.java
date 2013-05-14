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

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

/**
 * Simple class to compute an fractal image for a given 
 * formula with the Newton method
 * 
 * @author maxstrauch
 */
public class NewtonFractalCalculator extends SwingWorker<BufferedImage, Void> {

	/**
	 * Predefined set of colors to work with
	 */
	public static final int[] COLORS = { 
		0xF76A6A, 0xF7926A, 0xFCC441, 0xF7C06A, 0xF7E16A, 
		0xF6F76A, 0xCEF76A, 0xCEDD5D, 0xA8E577, 0x78F76A, 
		0x6AF7D9, 0x6AE3F7, 0x6AB8F7, 0x6A83F7, 0xAA6AF7, 
		0xCB6AF7, 0xF76AD7, 0xF76AB9, 0xF76A98, 0xF76A74,
		0x000000
	};
	
	/**
	 * Regular expression to match the input formula
	 */
	public static final String INPUT_PATTERN = "[x0-9\\-\\+\\*/\\^\\(\\)\\.]+";

	/**
	 * Attributes for plot parameters
	 */
	private double range, stepSize, calculationSteps;
	
	/**
	 * Stores the size of the image and the index of
	 * the current color
	 */
	private int size, colorCnt = 0;
	
	/**
	 * The resulting image
	 */
	private BufferedImage resultImage;
	
	/**
	 * The function to work on
	 */
	private String f, fd;
	
	/**
	 * Color index
	 */
	private Map<double[], Integer> colorIndex;
	
	/**
	 * Constructs a new Newton method calculator which calculates
	 * a fractal for a given formula and paints the result onto a
	 * {@link BufferedImage}
	 * 
	 * @param f The function
	 * @param fd The 1st derivation of the function
	 * @param rangeOffset The range of the image are; the image will
	 * show the result for x, y in [-rangeOffset, rangeOffset]
	 * @param stepSize The size of the steps between the points. More
	 * less step size will make the result image and computation time
	 * bigger (depending on your CPU)
	 * @param pcl The {@link PropertyChangeListener} to attach
	 */
	public NewtonFractalCalculator(String f, String fd, double rangeOffset, 
			double stepSize, PropertyChangeListener pcl) {
		
		// Input check
		if (!f.matches(INPUT_PATTERN) || !fd.matches(INPUT_PATTERN) || 
				rangeOffset < 0 || stepSize < 0 || stepSize > rangeOffset)
			throw new IllegalArgumentException("At least one of the " +
					"supplied arguments is bad");
		
		// Set attributes
		this.f = f;
		this.fd = fd;
		this.range = rangeOffset;
		this.stepSize = stepSize;
		
		// Calculate the number of calculation steps to perform
		size = (2 * (int) Math.round(range/stepSize));
		calculationSteps = size*size;
		
		// Create the image
		resultImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		
		// Create the color index
		colorIndex = new HashMap<double[], Integer>();
		colorIndex.put(new double[] {Double.NaN, Double.NaN}, 0x0);
		
		// Add the property change listener
		addPropertyChangeListener(pcl);
	}
	
	/**
	 * Returns the associated color for the given complex
	 * root found by the Newton method; if the given complex
	 * number is not found a new color is associated
	 * 
	 * @param root The complex root
	 * @return The color associated
	 */
	public int getColor(double[] root) {
		// Check if there is already a color associated
		for (double[] value : colorIndex.keySet())
			if (Math.abs(value[0]-root[0]) + Math.abs(value[1]-root[1]) < 1e-4 ||
					((Double.isNaN(value[0]) && Double.isNaN(root[0])) ||
							(Double.isNaN(value[1]) && Double.isNaN(root[1]))))
				return colorIndex.get(value);
		
		// Create a new entry for the new root
		int color = COLORS[colorCnt];
		colorIndex.put(root, color);
		if (colorCnt < COLORS.length)
			colorCnt++;
		
		// Return the color
		return color;
	}
	
	/**
	 * Returns an array of all complex roots found by the Newton
	 * method
	 * 
	 * @return A list of all complex roots and the complex number
	 * NaN
	 */
	public double[][] getRoots() {
		Set<double[]> s = colorIndex.keySet();
		return s.toArray(new double[s.size()][2]);
	}
	
	@Override
	protected BufferedImage doInBackground() throws Exception {
		int xcnt = 0, ycnt = 0;
		double i = 0;
		
		// Loop through the image area and calculate for every
		// point the root
		for (double y = -range; y <= range; y += stepSize) {
			if (ycnt >= size)
				continue;
			
			for (double x = -range; x <= range; x += stepSize) {
				if (xcnt >= size)
					continue;
				
				double[] r = NewtonComplex.newton(f, fd, new double[] {x, y});
				resultImage.setRGB(xcnt, ycnt, getColor(r));
				
				xcnt++;
				super.setProgress((int) Math.round(100f*(i++)/calculationSteps));
			}
			
			xcnt = 0;
			ycnt++;
		}
		
		super.setProgress(100);
		return resultImage;
	}
	
}
