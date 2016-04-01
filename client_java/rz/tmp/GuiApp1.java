package rz;

//Imports are listed in full to show what's being used
//could just import javax.swing.* and java.awt.* etc..
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.stream.Collectors;

public class GuiApp1 {
    
    //Note: Typically the main method will be in a
    //separate class. As this is a simple one class
    //example it's all in the one class.
    public static void main(String[] args) {
        new GuiApp1();
    }

    public GuiApp1()
    {
        JFrame guiFrame = new JFrame();
        
        //make sure the program exits when the frame closes
        guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        guiFrame.setTitle("Example GUI");
        guiFrame.setSize(300,250);
      
        //This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
 
 
        
        //The first JPanel contains a JLabel and JCombobox
        final JPanel filePanel = new JPanel();
        JLabel fileLabel = new JLabel("Files:");

        ArrayList<String> fileNames = new ArrayList<String>();
        File.getFileList().forEach(f -> fileNames.add(f.toString()));
        
        String[] fileNames2 = new String[ fileNames.size() ];
        fileNames2 = fileNames.toArray(fileNames2);
        String[] lol = {"lel", "loul"};
        JList fileList = new JList(fileNames2);
        //JComboBox fileCombo = new JComboBox(lol);
        
        filePanel.add(fileLabel);
        filePanel.add(fileList);
        
        JButton clickBut = new JButton("click");
        
        //The ActionListener class is used to handle the
        //event that happens when the user clicks the button.
        //As there is not a lot that needs to happen we can 
        //define an anonymous inner class to make the code simpler.
        clickBut.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent event)
                {
                    //When the fruit of veg button is pressed
                    //the setVisible value of the listPanel and
                    //comboPanel is switched from true to 
                    //value or vice versa.
                    filePanel.setVisible(!filePanel.isVisible());

                }
            });
        
        //The JFrame uses the BorderLayout layout manager.
        //Put the two JPanels and JButton in different areas.
        guiFrame.add(filePanel, BorderLayout.NORTH);
        guiFrame.add(clickBut,BorderLayout.SOUTH);
        
        //make sure the JFrame is visible
        guiFrame.setVisible(true);
    }
    
}
