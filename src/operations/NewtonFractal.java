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
 
import helpers.SimpleForm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * Simple GUI panel to generate a Newton fractal by a
 * given function term and its 1st derivation
 * 
 * @author maxstrauch
 */
public class NewtonFractal extends JPanel implements ActionListener, PropertyChangeListener {
 
	private static final long serialVersionUID = 1L;
	
	/**
	 * The progress bar to show the computation progress
	 */
	private JProgressBar progressBar;
	
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
     * The result area
     */
	private JScrollPane preview;

	/**
	 * The input fields for the function and the
	 * 1st derivation
	 */
	private JTextField f, fd;

	/**
	 * Some output parameters
	 */
	private JFormattedTextField range, steps;
	
	/**
	 * The worker who creates the output image
	 */
	private NewtonFractalCalculator task;

 
    /**
     * Constructs a new Newton fractal panel with controls
     * to generate it.
     */
    public NewtonFractal() {
        super(new BorderLayout());
        initGui();
        
        // Set default values
        range.setValue(new Double(1.0));
        steps.setValue(new Double(0.01));
        f.setText("x^3-1");
        fd.setText("3*x^2");
    }
    
    /**
     * Creates the GUI components and layouts them
     */
    private void initGui() {
		// Create the components
		range = new JFormattedTextField(NumberFormat.getNumberInstance());
		range.setColumns(10);

		steps = new JFormattedTextField(NumberFormat.getNumberInstance());
		steps.setColumns(10);

		f = new JTextField();
		fd = new JTextField();

		startButton = new JButton("Calculate");
		startButton.setActionCommand("start");
		startButton.addActionListener(this);

		exportBtn = new JButton("Export as image");
		exportBtn.setActionCommand("export");
		exportBtn.addActionListener(this);
		exportBtn.setEnabled(false);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		taskOutput = new JEditorPane("text/html", "");
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);

		preview = new JScrollPane();
		preview.setPreferredSize(new Dimension(425, 425));
		
		// Layout components
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        add(preview, BorderLayout.CENTER);
        
        JScrollPane sp = new JScrollPane(taskOutput);
        sp.setPreferredSize(new Dimension(150, 200));
        
        // Create the main form on the right side
        add(new SimpleForm().
    		addLabel("f(x)=").addLastField(f).
    		addLabel("f'(x)=").addLastField(fd).
    		addLabel("Plot range:").addLastField(range).
    		addLabel("Step size:").addLastField(steps).
    		addSeperator("Calculation").
    		addLastField(progressBar).
    		addLastField(startButton).
    		addLastField(exportBtn).
    		addSeperator("Roots").
    		addLastField(sp), BorderLayout.EAST);
    }
 
    /**
     * Sets the state of all input fields by the method
     * {@link JComponent#setEnabled(boolean)}
     *  
     * @param enabled <code>true</code> if input fields
     * should be enabled otherwise <code>false</code>
     */
    private void setInputEnabled(boolean enabled) {
    	f.setEnabled(enabled);
    	fd.setEnabled(enabled);
    	range.setEnabled(enabled);
    	steps.setEnabled(enabled);
    }
    
    /**
     * Invoked when the user presses some button
     */
    public void actionPerformed(ActionEvent evt) {
    	// Handle the start button
        if ("start".equals(evt.getActionCommand())) {
        	startButton.setEnabled(false);
        	exportBtn.setEnabled(false);
        	setInputEnabled(false);
        	taskOutput.setText("");
        	
        	try {
        		// Create a new task instance
        		task = new NewtonFractalCalculator(
        				f.getText(), 
        				fd.getText(), 
        				(Double) range.getValue(), 
        				(Double) steps.getValue(),
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
				return;
			}
        	
        	// If task creation works fine: let's start the
        	// calculation
			task.execute();
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
        	int retval = fc.showSaveDialog(NewtonFractal.this);
        	
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
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    	
        // Event when image calculation is finished
        if (task.isDone()) {
        	
        	// Display the generated image in the view port
        	try {
				preview.getViewport().add(new JLabel(new ImageIcon(task.get())));
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
        		if ((num = NewtonComplex.formatComplex(roots[i])) != null) {
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
        JFrame frame = new JFrame("Newton fractal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        // Create and set up the content pane
        JComponent newContentPane = new NewtonFractal();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
 
        // Display the window
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}