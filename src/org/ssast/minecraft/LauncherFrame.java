package org.ssast.minecraft;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.ssast.minecraft.auth.AuthType;
import org.ssast.minecraft.download.Downloader;
import org.ssast.minecraft.util.Lang;

public class LauncherFrame extends JFrame {

	private static final long serialVersionUID = -1923022699772187651L;

	JPanel base = new JPanel();

	JSeparator hseparator = new JSeparator();
	JSeparator hseparator2 = new JSeparator();
	JSeparator vseparator = new JSeparator();

	JTextArea console = new JTextArea();
	JScrollPane consoleOuter = new JScrollPane(console);

	JLabel modulesLabel = new JLabel(Lang.getString("ui.module.label"));
	DefaultTableModel modulesModel = new UneditableTableModel();
	JTable modules = new JTable(modulesModel);
	JScrollPane modulesOuter = new JScrollPane(modules);
	JButton installModules = new JButton(Lang.getString("ui.module.install"));
	JButton uninstallModules = new JButton(Lang.getString("ui.module.uninstall"));
	JCheckBox showOld = new JCheckBox(Lang.getString("ui.module.old"));
	JCheckBox showSnapshot = new JCheckBox(Lang.getString("ui.module.snapshot"));

	JLabel userLabel = new JLabel(Lang.getString("ui.username.label"));
	JTextField user = new JTextField();
	JLabel passLabel = new JLabel(Lang.getString("ui.password.label"));
	JTextField pass = new JPasswordField();
	JCheckBox savePass = new JCheckBox(Lang.getString("ui.savepassword"));
	JLabel authTypeLabel = new JLabel(Lang.getString("ui.auth.type.label"));
	JComboBox authType = new JComboBox();
	JLabel gameVersionLabel = new JLabel(Lang.getString("ui.version.label"));
	JComboBox gameVersion = new JComboBox();
	JButton launch = new JButton(Lang.getString("ui.launch"));

	JLabel profilesLabel = new JLabel(Lang.getString("ui.profile.label"));
	JComboBox profiles = new JComboBox();
	JButton addProfile = new JButton(Lang.getString("ui.profile.add"));
	JButton removeProfile = new JButton(Lang.getString("ui.profile.remove"));

	JLabel runPathLabel = new JLabel(Lang.getString("ui.runpath.label"));
	JTextField runPath = new JTextField();
	JButton runPathSearch = new JButton("...");

	ButtonGroup runningMode = new ButtonGroup();
	JRadioButton runningMode32 = new JRadioButton(Lang.getString("ui.mode.d32"), false);
	JRadioButton runningMode64 = new JRadioButton(Lang.getString("ui.mode.d64"), false);
	JRadioButton runningModeDefault = new JRadioButton(Lang.getString("ui.mode.default"), true);
	JLabel jrePathLabel = new JLabel(Lang.getString("ui.jrepath.label"));
	JTextField jrePath = new JTextField();
	JButton jrePathSearch = new JButton("...");
	JLabel memorySizeLabel = new JLabel(Lang.getString("ui.memory.label"));
	JTextField memorySize = new JTextField();
	JSlider memorySizeSlider = new JSlider();
	
	PrintStream thisStdOut = null;
	PrintStream oldStdOut = null;
	
	public LauncherFrame() {
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				if(thisStdOut != null && thisStdOut == System.out) {
					System.setOut(oldStdOut);
				}
				Config.updateFromFrame(LauncherFrame.this);
				Config.saveConfig();
				Downloader.stopAll();
				dispose();
			}
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});

		setResizable(false);
		setTitle("SSAST Minecraft Launcher");
		setIconImage(new ImageIcon(getClass().getResource("/favicon.png")).getImage());
		base.setPreferredSize(new Dimension(600, 450));
		add(base);
		pack();
		setLocationByPlatform(true);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);

		base.setLayout(null);
		createFrame();
		base.getRootPane().setDefaultButton(launch);

		this.setFocusTraversalPolicy(new LauncherFrameFocusTraversalPolicy());
	}
	
	private void createFrame() {
		base.add(consoleOuter);
		consoleOuter.setLocation(0, 350);
		consoleOuter.setSize(600, 100);
		consoleOuter.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		console.setEditable(false);
		console.setBackground(Color.white);
		console.setLineWrap(true);
		console.setWrapStyleWord(true);

		base.add(modulesLabel);
		modulesLabel.setLocation(5, 0);
		modulesLabel.setSize(300, 20);
		
		base.add(modulesOuter);
		modulesOuter.setLocation(0, 20);
		modulesOuter.setSize(395, 200);
		modulesModel.addColumn(Lang.getString("ui.table.name"));
		modulesModel.addColumn(Lang.getString("ui.table.type"));
		modulesModel.addColumn(Lang.getString("ui.table.state"));
		modules.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(125);
		modules.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(75);
		modules.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(75);
		modules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		base.add(hseparator);
		hseparator.setLocation(1, 344);
		hseparator.setSize(598, 2);

		base.add(hseparator2);
		hseparator2.setLocation(1, 260);
		hseparator2.setSize(398, 2);
		
		base.add(vseparator);
		vseparator.setOrientation(SwingConstants.VERTICAL);
		vseparator.setLocation(400, 1);
		vseparator.setSize(2, 342);
		
		base.add(installModules);
		installModules.setLocation(5, 225);
		installModules.setSize(90, 30);
		
		base.add(uninstallModules);
		uninstallModules.setLocation(100, 225);
		uninstallModules.setSize(90, 30);
		
		base.add(showOld);
		showOld.setLocation(195, 227);
		showOld.setSize(100, 25);

		base.add(showSnapshot);
		showSnapshot.setLocation(295, 227);
		showSnapshot.setSize(100, 25);
		
		base.add(userLabel);
		userLabel.setLocation(405, 68);
		userLabel.setSize(200, 20);
		
		base.add(user);
		user.setLocation(405, 88);
		user.setSize(190, 25);
		
		base.add(passLabel);
		passLabel.setLocation(405, 115);
		passLabel.setSize(200, 20);
		
		base.add(pass);
		pass.setLocation(405, 135);
		pass.setSize(190, 25);
		pass.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e) {
				pass.setSelectionStart(0);
				pass.setSelectionEnd(pass.getText().length());
			}
		});
		
		base.add(savePass);
		savePass.setLocation(405, 163);
		savePass.setSize(190, 20);

		base.add(authType);
		authType.setLocation(485, 188);
		authType.setSize(110, 23);
		for(AuthType at : AuthType.values()) {
			authType.addItem(at);
		}
		
		base.add(authTypeLabel);
		authTypeLabel.setLocation(405, 188);
		authTypeLabel.setSize(80, 20);

		base.add(gameVersion);
		gameVersion.setLocation(485, 216);
		gameVersion.setSize(110, 23);
		
		base.add(gameVersionLabel);
		gameVersionLabel.setLocation(405, 216);
		gameVersionLabel.setSize(80, 20);
		
		base.add(launch);
		launch.setLocation(405, 302);
		launch.setSize(190, 38);

		runningMode.add(runningMode32);
		runningMode.add(runningMode64);
		runningMode.add(runningModeDefault);
		
		base.add(runningMode32);
		runningMode32.setLocation(5, 320);
		runningMode32.setSize(62, 20);
		
		base.add(runningMode64);
		runningMode64.setLocation(67, 320);
		runningMode64.setSize(63, 20);
		
		base.add(runningModeDefault);
		runningModeDefault.setLocation(130, 320);
		runningModeDefault.setSize(70, 20);
		
		base.add(jrePathLabel);
		jrePathLabel.setLocation(5, 265);
		jrePathLabel.setSize(200, 20);
		
		base.add(jrePath);
		jrePath.setLocation(5, 290);
		jrePath.setSize(160, 25);
		
		base.add(jrePathSearch);
		jrePathSearch.setLocation(170, 290);
		jrePathSearch.setSize(25, 25);
		jrePathSearch.addActionListener(new ActionListener() {
			private JFileChooser fc = new JFileChooser();
			public void actionPerformed(ActionEvent e) {
				fc.setCurrentDirectory(new File(jrePath.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.showDialog(LauncherFrame.this, Lang.getString("ui.jrepath.filechooser.title"));
				if(fc.getSelectedFile() != null)
					jrePath.setText(fc.getSelectedFile().getPath());
			}
		});
		
		base.add(memorySizeLabel);
		memorySizeLabel.setLocation(205, 270);
		memorySizeLabel.setSize(110, 20);
		
		base.add(memorySize);
		memorySize.setLocation(315, 270);
		memorySize.setSize(80, 25);
		
		base.add(memorySizeSlider);
		memorySizeSlider.setLocation(205, 295);
		memorySizeSlider.setSize(190, 38);
		memorySizeSlider.setMinimum(0);
		memorySizeSlider.setMaximum(8192);
		memorySizeSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				memorySize.setText(String.valueOf(memorySizeSlider.getValue()));
			}
		});
		
		base.add(profilesLabel);
		profilesLabel.setLocation(405, 5);
		profilesLabel.setSize(65, 20);
		
		base.add(profiles);
		profiles.setLocation(470, 5);
		profiles.setSize(125, 23);
		
		base.add(addProfile);
		addProfile.setLocation(405, 35);
		addProfile.setSize(92, 30);

		base.add(removeProfile);
		removeProfile.setLocation(503, 35);
		removeProfile.setSize(92, 30);

		base.add(runPathLabel);
		runPathLabel.setLocation(405, 245);
		runPathLabel.setSize(200, 20);
		
		base.add(runPath);
		runPath.setLocation(405, 270);
		runPath.setSize(160, 25);
		
		base.add(runPathSearch);
		runPathSearch.setLocation(570, 270);
		runPathSearch.setSize(25, 25);
		runPathSearch.addActionListener(new ActionListener() {
			private JFileChooser fc = new JFileChooser();
			public void actionPerformed(ActionEvent e) {
				fc.setCurrentDirectory(new File(runPath.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.showDialog(LauncherFrame.this, Lang.getString("ui.runpath.filechooser.title"));
				if(fc.getSelectedFile() != null)
					runPath.setText(fc.getSelectedFile().getPath());
			}
		});
	}

	public void setStdOut() {
		if(thisStdOut != null && thisStdOut == System.out) {
			System.setOut(oldStdOut);
		} else {
			thisStdOut = new PrintStream(new ConsoleOutputStream(), true);
			oldStdOut = System.out;
			System.setOut(thisStdOut);
			if(Config.showDebugInfo) {
				System.setErr(thisStdOut);
			}
		}
	}

	public void outputConsole(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				console.append(message);
				consoleOuter.getVerticalScrollBar().setValue(999999);
			}
		});
	}
	
	class ConsoleOutputStream extends OutputStream {

		byte[] buffer = new byte[65536];
		int pos = 0;

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (buffer) {
				super.write(b, off, len);
			}
		}

		@Override
		public void flush() throws IOException {
			synchronized (buffer) {
				outputConsole(new String(buffer, 0, pos));
				pos = 0;
			}
		}

		public void write(int b) throws IOException {
			synchronized (buffer) {
				buffer[pos] = (byte) b;
				pos++;
			}
		}
	}
	
	class UneditableTableModel extends DefaultTableModel {

		private static final long serialVersionUID = -2140422989547704774L;

		@Override
		public boolean isCellEditable(int col, int row) {
			return false;
		}
	}
	
	class LauncherFrameFocusTraversalPolicy extends FocusTraversalPolicy {
		
		public List<Component> componentList = new ArrayList<Component>();
		
		public LauncherFrameFocusTraversalPolicy() {
			componentList.add(profiles);
			componentList.add(addProfile);
			componentList.add(removeProfile);
			componentList.add(user);
			componentList.add(pass);
			componentList.add(savePass);
			componentList.add(authType);
			componentList.add(gameVersion);
			componentList.add(runPath);
			componentList.add(runPathSearch);
			componentList.add(launch);
			componentList.add(jrePath);
			componentList.add(jrePathSearch);
			componentList.add(runningMode32);
			componentList.add(runningMode64);
			componentList.add(runningModeDefault);
			componentList.add(memorySize);
			componentList.add(memorySizeSlider);
		}

		public Component getComponentAfter(Container aContainer,
				Component aComponent) {
			int i = componentList.indexOf(aComponent);
			return componentList.get((i + 1) % componentList.size());
		}

		public Component getComponentBefore(Container aContainer,
				Component aComponent) {
			int i = componentList.indexOf(aComponent);
			return componentList.get((i + componentList.size() - 1) % componentList.size());
		}

		public Component getFirstComponent(Container aContainer) {
			return componentList.get(0);
		}

		public Component getLastComponent(Container aContainer) {
			return componentList.get(componentList.size() - 1);
		}

		public Component getDefaultComponent(Container aContainer) {
			return componentList.get(0);
		}
		
	}
}
