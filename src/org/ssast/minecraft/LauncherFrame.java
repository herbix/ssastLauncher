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
	JTextArea console = new JTextArea();
	JScrollPane consoleOuter = new JScrollPane(console);
	JLabel modulesLabel = new JLabel(Lang.getString("ui.module.label"));
	DefaultTableModel modulesModel = new UneditableTableModel();
	JTable modules = new JTable(modulesModel);
	JScrollPane modulesOuter = new JScrollPane(modules);
	JLabel modsLabel = new JLabel(Lang.getString("ui.mod.label"));
	DefaultTableModel modsModel = new UneditableTableModel();
	JTable mods = new JTable(modsModel);
	JScrollPane modsOuter = new JScrollPane(mods);
	JSeparator hseparator = new JSeparator();
	JSeparator vseparator = new JSeparator();
	JButton installModules = new JButton(Lang.getString("ui.module.install"));
	JButton uninstallModules = new JButton(Lang.getString("ui.module.uninstall"));
	JButton loadMod = new JButton(Lang.getString("ui.mod.load"));
	JButton unloadMod = new JButton(Lang.getString("ui.mod.unload"));
	JLabel userLabel = new JLabel(Lang.getString("ui.username.label"));
	JTextField user = new JTextField();
	JLabel passLabel = new JLabel(Lang.getString("ui.password.label"));
	JTextField pass = new JPasswordField();
	JButton launch = new JButton(Lang.getString("ui.launch"));
	JCheckBox savePass = new JCheckBox(Lang.getString("ui.savepassword"));
	JLabel authTypeLabel = new JLabel(Lang.getString("ui.auth.type.label"));
	JComboBox authType = new JComboBox();
	JLabel gameVersionLabel = new JLabel(Lang.getString("ui.version.label"));
	JComboBox gameVersion = new JComboBox();
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
		modulesLabel.setLocation(0, 0);
		modulesLabel.setSize(300, 20);
		
		base.add(modulesOuter);
		modulesOuter.setLocation(0, 20);
		modulesOuter.setSize(300, 150);
		modulesModel.addColumn(Lang.getString("ui.table.name"));
		modulesModel.addColumn(Lang.getString("ui.table.type"));
		modulesModel.addColumn(Lang.getString("ui.table.state"));
		modules.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(125);
		modules.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(75);
		modules.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(75);
		modules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		base.add(modsLabel);
		modsLabel.setLocation(0, 173);
		modsLabel.setSize(300, 16);
		
		base.add(modsOuter);
		modsOuter.setLocation(0, 190);
		modsOuter.setSize(300, 150);
		modsModel.addColumn(Lang.getString("ui.table.name"));
		modsModel.addColumn(Lang.getString("ui.table.state"));
		mods.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(225);
		mods.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(75);
		mods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		base.add(hseparator);
		hseparator.setLocation(1, 344);
		hseparator.setSize(598, 2);
		
		base.add(vseparator);
		vseparator.setOrientation(SwingConstants.VERTICAL);
		vseparator.setLocation(400, 1);
		vseparator.setSize(2, 342);
		
		base.add(installModules);
		installModules.setLocation(305, 20);
		installModules.setSize(90, 30);
		
		base.add(uninstallModules);
		uninstallModules.setLocation(305, 55);
		uninstallModules.setSize(90, 30);
		
		base.add(loadMod);
		loadMod.setLocation(305, 190);
		loadMod.setSize(90, 30);
		
		base.add(unloadMod);
		unloadMod.setLocation(305, 225);
		unloadMod.setSize(90, 30);
		
		base.add(userLabel);
		userLabel.setLocation(405, 5);
		userLabel.setSize(200, 20);
		
		base.add(user);
		user.setLocation(405, 25);
		user.setSize(190, 25);
		
		base.add(passLabel);
		passLabel.setLocation(405, 47);
		passLabel.setSize(200, 20);
		
		base.add(pass);
		pass.setLocation(405, 67);
		pass.setSize(190, 25);
		pass.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e) {
				pass.setSelectionStart(0);
				pass.setSelectionEnd(pass.getText().length());
			}
		});
		
		base.add(launch);
		launch.setLocation(405, 162);
		launch.setSize(190, 38);
		
		base.add(savePass);
		savePass.setLocation(405, 93);
		savePass.setSize(190, 20);

		base.add(gameVersion);
		gameVersion.setLocation(485, 138);
		gameVersion.setSize(110, 20);
		
		base.add(gameVersionLabel);
		gameVersionLabel.setLocation(405, 139);
		gameVersionLabel.setSize(80, 20);
		
		base.add(authType);
		authType.setLocation(485, 115);
		authType.setSize(110, 20);
		for(AuthType at : AuthType.values()) {
			authType.addItem(at);
		}
		
		base.add(authTypeLabel);
		authTypeLabel.setLocation(405, 116);
		authTypeLabel.setSize(80, 20);

		runningMode.add(runningMode32);
		runningMode.add(runningMode64);
		runningMode.add(runningModeDefault);
		
		base.add(runningMode32);
		runningMode32.setLocation(405, 250);
		runningMode32.setSize(62, 20);
		
		base.add(runningMode64);
		runningMode64.setLocation(467, 250);
		runningMode64.setSize(63, 20);
		
		base.add(runningModeDefault);
		runningModeDefault.setLocation(530, 250);
		runningModeDefault.setSize(70, 20);
		
		base.add(jrePathLabel);
		jrePathLabel.setLocation(405, 200);
		jrePathLabel.setSize(200, 20);
		
		base.add(jrePath);
		jrePath.setLocation(405, 220);
		jrePath.setSize(160, 25);
		
		base.add(jrePathSearch);
		jrePathSearch.setLocation(570, 220);
		jrePathSearch.setSize(25, 25);
		jrePathSearch.addActionListener(new ActionListener() {
			private JFileChooser fc = new JFileChooser();
			public void actionPerformed(ActionEvent e) {
				fc.setCurrentDirectory(new File(jrePath.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.showDialog(LauncherFrame.this, Lang.getString("ui.filechooser.title"));
				if(fc.getSelectedFile() != null)
					jrePath.setText(fc.getSelectedFile().getPath());
			}
		});
		
		base.add(memorySizeLabel);
		memorySizeLabel.setLocation(405, 280);
		memorySizeLabel.setSize(110, 20);
		
		base.add(memorySize);
		memorySize.setLocation(515, 280);
		memorySize.setSize(80, 25);
		
		base.add(memorySizeSlider);
		memorySizeSlider.setLocation(405, 305);
		memorySizeSlider.setSize(190, 38);
		memorySizeSlider.setMinimum(0);
		memorySizeSlider.setMaximum(8192);
		memorySizeSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				memorySize.setText(String.valueOf(memorySizeSlider.getValue()));
			}
		});
	}

	public void setStdOut() {
		thisStdOut = new PrintStream(new ConsoleOutputStream(), true);
		oldStdOut = System.out;
		System.setOut(thisStdOut);
	}

	public void outputConsole(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized(console) {
					console.append(message);
					consoleOuter.getVerticalScrollBar().setValue(999999);
				}
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
			componentList.add(user);
			componentList.add(pass);
			componentList.add(savePass);
			componentList.add(authType);
			componentList.add(gameVersion);
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
