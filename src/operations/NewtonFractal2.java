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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * Simple GUI panel to generate a Newton fractal by a
 * given function term (2nd version)
 * 
 * @author maxstrauch
 */
public class NewtonFractal2 extends JPanel implements ActionListener, PropertyChangeListener {
 
	private static final long serialVersionUID = 1L;
	
	/**
	 * A predefined list of zoom levels
	 */
	public static final Double[] ZOOM_SIZES = {
			0.009, 0.005, 0.003, 0.002, 0.001, 0.0007, 0.0005, 0.0001
	};
	
	/**
	 * The two buttons to start the computation and
	 * export the result
	 */
    private JButton startButton, exportBtn;
    
    /**
     * Text area to output the root
     */
    private JEditorPane taskOutput;

	/**
	 * The input fields for the function and the
	 * 1st derivation
	 */
	private JTextField formula;

	/**
	 * Some output parameters
	 */
	private JSpinner range;
	
	/**
	 * The worker who creates the output image
	 */
	private NewtonFractalCalculator task;
	
	/**
	 * List of all possible zoom sizes
	 */
	private JComboBox<Double> zoomSize;
    
	/**
	 * The resulting image
	 */
    private BufferedImage buf = null;
    
    /**
     * The preview pane
     */
    private PreviewPane previewPane;    
    
    /**
     * The dimensions of the main frame
     */
    private Dimension mainDimension;
    
    /**
     * The preview position of the image
     */
    private int xpos = 0, ypos = 0;
 
    /**
     * Constructs a new Newton fractal panel with controls
     * to generate it.
     */
    public NewtonFractal2() {
        super(new BorderLayout());
        initGui();
        
        // Set default values
        range.setValue(new Double(1.0));
        formula.setText("x^3-1");
        requestFocus();
    }
    
    /**
     * Creates the GUI components and layouts them
     */
    private void initGui() {
		// Create the components
		range = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 20.0, 0.1));
		range.setOpaque(false);
		
		formula = new JTextField();
		formula.setOpaque(false);
		
		zoomSize = new JComboBox<Double>(ZOOM_SIZES);
		
		startButton = new JButton("Eval");
		startButton.setActionCommand("start");
		startButton.addActionListener(this);

		exportBtn = new JButton("Export");
		exportBtn.setActionCommand("export");
		exportBtn.addActionListener(this);
		exportBtn.setEnabled(false);

		taskOutput = new JEditorPane("text/html", "");
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);

		previewPane = new PreviewPane();
		
		// Layout components
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Right side controls
        Box hor = Box.createHorizontalBox();
        hor.setBorder(new EmptyBorder(15, 15, 15, 15));
        hor.setOpaque(false);
        hor.add(Box.createHorizontalGlue());
        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(150, 320));
        container.add(previewPane, BorderLayout.NORTH);
        JScrollPane ssp = new JScrollPane(taskOutput);
        ssp.setPreferredSize(new Dimension(150, 200));
        container.add(ssp, BorderLayout.CENTER);
        hor.add(container);
        add(hor, BorderLayout.NORTH);
        
        // Bottom buttons
        Box bottomButtons = Box.createHorizontalBox();
        bottomButtons.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomButtons.setOpaque(false);
        bottomButtons.add(formula);
        bottomButtons.add(zoomSize);
        bottomButtons.add(range);
        bottomButtons.add(startButton);
        bottomButtons.add(exportBtn);
        add(bottomButtons, BorderLayout.SOUTH);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	
    	g.setColor(Color.white);
    	g.fillRect(0, 0, getWidth(), getHeight());
    	
    	// Set dimensions
    	mainDimension = new Dimension(getWidth(), getHeight());
    	
    	// If no buffered image available draw the demo image
    	if (task == null || buf == null)
    		start();
    	
    	buf = task.getImage();
    	
    	// Draw the image
		g.drawImage(buf, xpos, ypos, null);
    	
		// Print some statistical data
		String str = task.toString();
		g.setColor(Color.green.darker());
		g.drawString(str, 15, 25);
		g.setColor(Color.green);
		g.drawString(str, 14, 24);
    	
    	// Repaint the preview
    	previewPane.repaint();
    }

    /**
     * Sets the state of all input fields by the method
     * {@link JComponent#setEnabled(boolean)}
     *  
     * @param enabled <code>true</code> if input fields
     * should be enabled otherwise <code>false</code>
     */
    private void setInputEnabled(boolean enabled) {
    	formula.setEnabled(enabled);
    	range.setEnabled(enabled);
    	zoomSize.setEnabled(enabled);
    }
    
    /**
     * Starts the rendering process for a new fractal
     */
    private void start() {
    	startButton.setEnabled(false);
    	exportBtn.setEnabled(false);
    	setInputEnabled(false);
    	taskOutput.setText("");
    	
    	try {
    		double r = Double.parseDouble(String.valueOf(range.getValue()));
    		
    		// Create a new task instance
    		task = new NewtonFractalCalculator(
    				formula.getText(), 
    				r, (Double) zoomSize.getSelectedItem(),
    				this
    		);
    		
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, 
					"Couldn't generate an image (" + e + ")", 
					"Error", JOptionPane.ERROR_MESSAGE);
			
			// Reset properties
			task = null;
			startButton.setEnabled(true);
			exportBtn.setEnabled(false);
			setInputEnabled(true);
			repaint();
			return;
		}
    	
    	// If task creation works fine: let's start the
    	// calculation
		task.execute();
    }

    /**
     * Invoked when the user presses some button
     */
    public void actionPerformed(ActionEvent evt) {
    	// Handle the start button
        if ("start".equals(evt.getActionCommand())) {
        	start();
        }
        
        // Handle the export image button
        if ("export".equals(evt.getActionCommand())) {
        	// Show a file chooser dialog
        	JFileChooser fc = new JFileChooser();
        	fc.setAcceptAllFileFilterUsed(false);
        	fc.setFileFilter(new FileFilter() {
				
				@Override
				public String getDescription() {
					return "PNG file (*.png)";
				}
				
				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(".png");
				}
			});
        	int retval = fc.showSaveDialog(NewtonFractal2.this);
        	
        	// Export the image if approved by the user
            if (retval == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
               
                // Append file extension if missing
                if (!file.getName().endsWith(".png"))
                	file = new File(file.getAbsolutePath() + ".png");
                
                // Try to write the image out
                try {
    				ImageIO.write(task.get(), "png", file);
    			} catch (Exception e) {
    				JOptionPane.showMessageDialog(this, 
    					"Export failed (" + e + ")", 
    					"Error", JOptionPane.ERROR_MESSAGE);
    			}
            }
        }
    }
    
    /**
     * Invoked when task's progress property changes
     */
    public void propertyChange(PropertyChangeEvent evt) {
    	// Event to update the progress bar
        if ("progress" == evt.getPropertyName()) {
            repaint();
        } 
    	
        // Event when image calculation is finished
        if (task.isDone()) {
        	// Display the generated image in the view port
        	try {
        		buf = task.get();
        		repaint();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, 
					"Couldn't display the generated image (" + e + ")", 
					"Error", JOptionPane.ERROR_MESSAGE);
			}

        	// Display all computed roots
        	double[][] roots = task.getRoots();
        	String num;
        	int e = 1;
        	
        	StringBuffer buf = new StringBuffer();
        	buf.append("<html><body>");
        	for (int i = 0; i < roots.length; i++) {
        		if ((num = SimpleComplexMath.formatComplex(roots[i])) != null) {
        			int c = task.getColor(roots[i]);
        			buf.append("<span bgcolor=\"#" +Integer.toHexString(c)  +
        						"\">x<sub>" + e++ + "</sub></span><b> = " + 
        						num + "</b><br/>");     
        		}
        	}
        	buf.append("</body></html>");
        	taskOutput.setText(buf.toString());
        	
        	// Set state of controls
        	startButton.setEnabled(true);
        	exportBtn.setEnabled(true);
        	setInputEnabled(true);
        }
    }
    
    /**
     * Create the GUI and show it
     */
    private static void createAndShowGUI() {
        // Create and set up the window
    	JFrame frame = new JFrame("Newton fractal 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        // Create and set up the content pane
        JComponent newContentPane = new NewtonFractal2();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
 
        // Display the window
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
    	// Some LaF stuff ...
    	System.setProperty("com.apple.mrj.application." +
    			"apple.menu.about.name", "Newton fractal 2");
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Ignore
		}
    	
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    /**
     * Simple preview panel to navigate through the fractal
     * 
     * @author maxstrauch
     */
    private class PreviewPane extends JPanel implements MouseListener {
    	
		private static final long serialVersionUID = 1L;

		private Rectangle area = new Rectangle(0, 0, 0, 0);
		
		/**
		 * Create a new instance of the preview pane
		 */
		public PreviewPane() {
    		addMouseListener(this);
    		setPreferredSize(new Dimension(150, 150));
    		setSize(new Dimension(150, 150));
    		setMinimumSize(new Dimension(150, 150));
    		setMaximumSize(new Dimension(150, 150));
    	}
    	
    	@Override
    	public void paint(Graphics g) {
    		if (task == null || buf == null)
    			return;
    		
    		// Fill the background
    		g.setColor(Color.black);
    		g.fillRect(0, 0, getWidth(), getHeight());
    		
    		// Draw the preview image
    		g.drawImage(buf, 0, 0, getWidth(), getHeight(), null);
    		
    		// Draw the view port
    		area.width = (int) Math.round(getWidth() * Math.min(1.0, 
    				((double) mainDimension.width)/buf.getWidth()));
    		area.height = (int) Math.round(getHeight() * Math.min(1.0, 
    				((double) mainDimension.height)/buf.getHeight()));
    		
    		g.setColor(Color.red);
    		g.drawRect(area.x, area.y, area.width-1, area.height-1);
    		
    		// Draw the black border
    		g.setColor(Color.black);
    		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
    	}
    	
    	@Override
		public void mouseClicked(MouseEvent e) {
			// On mouse click move the view port
			Point p = e.getPoint();
			
			area.x = p.x - area.width/2;
    		area.y = p.y - area.height/2;
    		
    		xpos = -(int) Math.round(mainDimension.width * 
    				((double) area.x)/((double) area.width));
    		ypos = -(int) Math.round(mainDimension.height *
    				((double) area.y)/((double) area.height));
    		
    		// Repaint everything
    		NewtonFractal2.this.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) { }

		@Override
		public void mouseEntered(MouseEvent e) { }

		@Override
		public void mouseExited(MouseEvent e) { }

		@Override
		public void mouseReleased(MouseEvent e) { }
    	
    }
    
}