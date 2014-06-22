package org.ssast.minecraft;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.ssast.minecraft.auth.AuthType;
import org.ssast.minecraft.download.Downloader;
import org.ssast.minecraft.util.HttpFetcher;
import org.ssast.minecraft.util.Lang;

public class LauncherFrame extends JFrame {

	private static final long serialVersionUID = -1923022699772187651L;

	JPanel base = new JPanel();

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
	JProgressBar installProgress = new JProgressBar();

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
		setTitle("SSAST Minecraft Launcher Concise");
		setIconImage(new ImageIcon(getClass().getResource("/favicon.png")).getImage());
		base.setPreferredSize(new Dimension(600, 400));
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
		base.add(userLabel);
		userLabel.setLocation(205, 68);
		userLabel.setSize(200, 20);
		
		base.add(user);
		user.setLocation(205, 88);
		user.setSize(190, 25);
		
		base.add(passLabel);
		passLabel.setLocation(205, 115);
		passLabel.setSize(200, 20);
		
		base.add(pass);
		pass.setLocation(205, 135);
		pass.setSize(190, 25);
		pass.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e) {
				pass.setSelectionStart(0);
				pass.setSelectionEnd(pass.getText().length());
			}
		});
		
		base.add(savePass);
		savePass.setLocation(205, 163);
		savePass.setSize(190, 20);

		base.add(authType);
		authType.setLocation(285, 188);
		authType.setSize(110, 23);
		for(AuthType at : AuthType.values()) {
			authType.addItem(at);
		}
		
		base.add(authTypeLabel);
		authTypeLabel.setLocation(205, 188);
		authTypeLabel.setSize(80, 20);

		base.add(gameVersion);
		gameVersion.setLocation(285, 216);
		gameVersion.setSize(110, 23);
		
		base.add(gameVersionLabel);
		gameVersionLabel.setLocation(205, 216);
		gameVersionLabel.setSize(80, 20);
		
		base.add(launch);
		launch.setLocation(205, 245);
		launch.setSize(190, 38);
		
		base.add(installProgress);
		installProgress.setLocation(0, 380);
		installProgress.setSize(600, 20);
		HttpFetcher.setJProgressBar(installProgress);
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
