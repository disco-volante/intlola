package fmv;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fmv.db.DataRetriever;
import fmv.tools.Tools;

/**
 * Main class for the Fault Measurement Visualizer.
 * 
 * @author jaco
 */
public class FMV {

	private static Logger logger;

	/**
	 * The main frame that constitutes this application.
	 */
	private static JFrame mainFrame;

	/**
	 * A dialog for the preferences window.
	 */
	public static PreferencesDialog prefs;

	/**
	 * Dialog for showing the compiler/tester progress.
	 */
	public static ToolRunnerDialog toolrunner;

	/**
	 * A container for a list of zip files inside a directory.
	 */
	private static Directory directory;

	private static JSplitPane splitPane;

	public static VersionTimeline timeGraph;

	public static AnnotateDialog annotateDialog;

	public static DiffPane diffPane;

	public static TablePane tablePane;

	private static Image fmvImage;

	private static ImageIcon fmvIcon;

	private static HelpContents help;

	private static JComboBox<String> toolBox;

	public static DataRetriever retriever;

	private static DirectoryPane directoryPane;
	private static DBPane dbPane;

	public static String getArchiveProperty(Archive archive, String defualt) {
		if (directory != null) {
			return directory.getXArchiveProperty(archive, null, defualt);
		} else {
			return defualt;
		}
	}

	public static String getArchiveProperty(Archive archive, String key,
			String defualt) {
		if (directory != null) {
			return directory.getXArchiveProperty(archive, key, defualt);
		} else {
			return defualt;
		}
	}

	public static void setArchiveProperty(Archive archive, String value) {
		if (directory != null) {
			directory.setXArchiveProperty(archive, null, value);
		}
	}

	public static void setArchiveProperty(Archive archive, String key,
			String value) {
		if (directory != null) {
			directory.setXArchiveProperty(archive, key, value);
		}
	}

	public static String getVersionProperty(Archive archive, Source source,
			Date date, String key, String defualt) {
		if (directory != null) {
			return directory.getXVersionProperty(archive, source, date, key,
					defualt);
		} else {
			return defualt;
		}
	}

	public static void setVersionProperty(Archive archive, Source source,
			Date date, String key, String value) {
		if (directory != null) {
			directory.setXVersionProperty(archive, source, date, key, value);
		}
	}

	public static String getDirectoryProperty(String key, String defualt) {
		if (directory != null) {
			return directory.getXDirectoryProperty(key, defualt);
		} else {
			return defualt;
		}
	}

	public static void setDirectoryProperty(String key, String value) {
		if (directory != null) {
			directory.setXDirectoryProperty(key, value);
		}
	}

	public static void saveProperties() {
		if (directory != null) {
			prefs.saveProperties();
			directory.saveXProperties();
		}
	}

	public static Image getMyImage(String imageName) {
		URL imageUrl = Thread.currentThread().getContextClassLoader()
				.getResource(imageName);
		System.out.println("URL: " + imageUrl);
		if (imageUrl == null) {
			return null;
		}
		Image image = null;
		try {
			image = ImageIO.read(imageUrl);
		} catch (IOException e) {
			image = null;
		}
		return image;
	}

	public static ImageIcon getMyImageIcon(String imageName) {
		URL imageUrl = Thread.currentThread().getContextClassLoader()
				.getResource(imageName);
		if (imageUrl == null) {
			return null;
		}
		ImageIcon imageIcon = null;
		try {
			imageIcon = new ImageIcon(ImageIO.read(imageUrl));
		} catch (IOException e) {
			imageIcon = null;
		}
		return imageIcon;
	}

	private static JMenuItem createMenuItem(String name, String cmd,
			MenuAction action, int key) {
		JMenuItem item = new JMenuItem(name, getMyImageIcon(cmd + ".gif"));
		item.setActionCommand(cmd);
		item.addActionListener(action);
		if (key != -1) {
			item.setAccelerator(KeyStroke.getKeyStroke(key,
					ActionEvent.CTRL_MASK));
		}
		return item;
	}

	/**
	 * Create a menu bar for our application.
	 * 
	 * @return the new menu bar
	 */
	private static JMenuBar createMenu() {
		MenuAction a = new MenuAction();
		JMenuBar b = new JMenuBar();

		JMenu m = new JMenu("File");
		m.add(createMenuItem("Open Directory", "file.open", a, KeyEvent.VK_O));
		m.addSeparator();
		m.add(createMenuItem("Get Server Projects", "server.open", a,
				KeyEvent.VK_U));
		m.addSeparator();
		m.add(createMenuItem("Compile/test all", "file.comptest", a,
				KeyEvent.VK_C));
		m.add(createMenuItem("Print", "file.print", a, KeyEvent.VK_P));
		m.addSeparator();
		m.add(createMenuItem("Preferences", "file.prefs", a, -1));
		m.addSeparator();
		m.add(createMenuItem("Quit", "file.quit", a, KeyEvent.VK_Q));
		b.add(m);

		m = new JMenu("View");
		b.add(m);
		m.add(createMenuItem("Summary table", "view.table", a, KeyEvent.VK_T));
		m.add(createMenuItem("Progress graph", "view.progress", a,
				KeyEvent.VK_G));
		m.add(createMenuItem("Differences", "view.diffs", a, KeyEvent.VK_D));

		m = new JMenu("Help");
		b.add(m);
		m.add(createMenuItem("Help contents", "help.contents", a, -1));
		m.addSeparator();
		m.add(createMenuItem("About FMV", "help.about", a, -1));
		return b;
	}

	protected static String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf(".");
		return classString.substring(dotIndex + 1);
	}

	@SuppressWarnings({ "unchecked" })
	private static Container createContentPane() {

		tablePane = new TablePane();

		diffPane = new DiffPane();

		timeGraph = new VersionTimeline(false);
		timeGraph.setMinimumSize(new Dimension(650, 400));

		directoryPane = new DirectoryPane();

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, directoryPane,
				tablePane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0);
		splitPane.setDividerLocation(150);

		JButton compileBtn = new JButton("Compile");
		compileBtn.addActionListener(new CompileListener());
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controls.add(compileBtn);
		toolBox = new JComboBox<String>(Tools.getTools());
		controls.add(toolBox);
		JButton toolButton = new JButton("Run");
		toolButton.setToolTipText("Run static analysis tools.");
		toolButton.addActionListener(new ToolListener());
		controls.add(toolButton);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		contentPane.add(controls, BorderLayout.NORTH);
		contentPane.add(splitPane, BorderLayout.CENTER);
		return contentPane;
	}

	protected static void retrieveProjectDates(String project) {
		List<Date> dates = retriever.getProjectDates(project);
	}

	private static void createAndShowGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		mainFrame = new JFrame("Fault Measurement Visualizer");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setJMenuBar(createMenu());
		mainFrame.setContentPane(createContentPane());
		mainFrame.setSize(800, 400);
		mainFrame.setLocationRelativeTo(null);
		fmvIcon = new ImageIcon(FMV.getMyImage("fmv.gif"));
		fmvImage = fmvIcon.getImage();
		mainFrame.setIconImage(fmvImage);
		toolrunner = new ToolRunnerDialog(mainFrame);
		prefs = new PreferencesDialog(mainFrame);
		help = new HelpContents(mainFrame);
		annotateDialog = new AnnotateDialog(mainFrame);
		mainFrame.setVisible(true);
	}

	public static void main(String[] args) {
		logger = Logger.getLogger(FMV.class.getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		try {
			logger.addHandler(new FileHandler("fmv.log"));
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
				}
			});
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void log(String className, String content) {
		logger.log(Level.SEVERE, className + "\n" + content);
	}

	/**
	 * Inner class that responds to menu commands.
	 */
	private static class MenuAction implements ActionListener {

		/**
		 * A file chooser for opening directories.
		 */
		private JFileChooser fc = null;

		public MenuAction() {
			fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}

		/**
		 * Respond to a menu button.
		 * 
		 * @param event
		 *            the event that caused this action
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void actionPerformed(ActionEvent event) {
			if ("file.open".equals(event.getActionCommand())) {
				saveProperties();
				if (fc == null) {
					fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}
				if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					try {
						directory = new Directory(f.getCanonicalPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
					tablePane.clear();
					for (String n : f.list()) {
						if (n.endsWith(".zip") && !n.endsWith("TESTING.zip")) {
							directory.addArchive(n);
						}
					}
					prefs.loadProperties();
					directoryPane.directoryList.setModel(directory.getModel());
					directoryPane.sourceList.setModel(new DefaultListModel());
					splitPane.setRightComponent(tablePane);
				}
			} else if ("server.open".equals(event.getActionCommand())) {
				saveProperties();
				showProjects();
			} else if ("file.prefs".equals(event.getActionCommand())) {
				prefs.activate();
			} else if ("file.quit".equals(event.getActionCommand())) {
				saveProperties();
				mainFrame.dispose();
				System.exit(0);
			} else if ("view.table".equals(event.getActionCommand())) {
				splitPane.setRightComponent(tablePane);
			} else if ("view.progress".equals(event.getActionCommand())) {
				splitPane.setRightComponent(timeGraph);
			} else if ("view.diffs".equals(event.getActionCommand())) {
				splitPane.setRightComponent(diffPane);
			} else if ("help.contents".equals(event.getActionCommand())) {
				help.setVisible(true);
			} else if ("help.about".equals(event.getActionCommand())) {
				JOptionPane
						.showMessageDialog(
								mainFrame,
								"Fault Measurement Visualizer\n"
										+ "(C) 2009 Computer Science, Stellenbosch University",
								"About FMV", JOptionPane.INFORMATION_MESSAGE,
								fmvIcon);
			}
		}
	}

	public static OutputDialog getDialog() {
		return new OutputDialog(mainFrame);
	}

	public static SplitDialog getSplitDialog() {
		return new SplitDialog(mainFrame);
	}

	public static void showProjects() {
		if (retriever == null) {
			try {
				retriever = new DataRetriever(prefs.getDBHost());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		DefaultListModel<String> model = new DefaultListModel<String>();
		List<String> found = retriever.getProjects();
		for (String f : found) {
			model.addElement(f);
		}
	}

	public static class ToolListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (directoryPane.isVisible()) {
				int i = directoryPane.directoryList.getSelectedIndex();
				if (i != -1 && toolBox.getSelectedIndex() != -1) {
					Archive archive = directory.getArchive(i);
					if (!archive.isExtracted()) {
						archive.extract();
					}
					archive.runTool((String) toolBox.getSelectedItem());
				}
			} else if (dbPane.isVisible()) {

			}
		}
	}

	public static class CompileListener implements ActionListener {

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			if (directoryPane.isVisible()) {
				int i = directoryPane.directoryList.getSelectedIndex();
				if (i != -1) {
					Archive archive = directory.getArchive(i);
					if (!archive.isExtracted()) {
						archive.extract();
					}
					if (!archive.isCompiled()) {
						archive.runTool("Tests");
					}
					directoryPane.sourceList.setModel(directory.getModel(i));
					directoryPane.sourceList.setSelectedIndex(0);
					directory.setDiff(i, 0);
				}
			} else if (dbPane.isVisible()) {

			}
		}
	}

}