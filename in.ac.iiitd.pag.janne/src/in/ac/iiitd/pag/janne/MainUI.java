package in.ac.iiitd.pag.janne;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.sound.sampled.ReverbType;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class MainUI {
	
	private static final String FilePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\assignments\\chetan-ap\\AP2014-Simple";
	private static JTextArea textArea = null;
	private static JFrame frame = new JFrame("Code Search Tool");
	private static JScrollPane scroll = null;
	
	public static void main(String[] args) {
		
		  frame.addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent e) {
	            System.exit(0);
	         }
	      });
	      
	      JPanel content = new JPanel();
	      content.setLayout(new BorderLayout());
	      
	      addTree(content);
	      addTextArea(content);
	      
	      frame.setContentPane(content);
	      frame.pack();
	      frame.setSize(new Dimension(1200, 700));
	      frame.setVisible(true);
	      frame.setResizable(false);

	}

	private static void addTextArea(JPanel f) {
		
		textArea = new JTextArea(5,80);
				
		scroll = new JScrollPane(textArea);
		f.add(scroll, BorderLayout.CENTER);
	}

	private static void addTree(JPanel f) {
		 
		DefaultMutableTreeNode root = loadTree();
        
        //create the tree by passing in the root node
        JTree tree = new JTree(root);
        tree.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.BLACK));
        tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				String path = FilePath + File.separator + e.getPath().getLastPathComponent().toString();
				File file = new File(path);
				String text = "";
				try {
					text = new String(Files.readAllBytes(Paths.get(path)));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				textArea.setText(text);
				textArea.setCaretPosition(0);
				scroll.getVerticalScrollBar().setValue(0);
				
			}
		});
        f.add(tree, BorderLayout.WEST);       
	}

	private static DefaultMutableTreeNode loadTree() {
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		File file = new File(FilePath);
		File[] contents = file.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.toLowerCase().endsWith(".java") || name.toLowerCase().endsWith(".c") || name.toLowerCase().endsWith(".cpp") );
		    }
		});
		for(File content: contents) {
			DefaultMutableTreeNode contentNode = new DefaultMutableTreeNode(content.getName());
			root.add(contentNode);
		}		
		return root;
		
	}
}
