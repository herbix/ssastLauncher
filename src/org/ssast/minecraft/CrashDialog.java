package org.ssast.minecraft;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ssast.minecraft.util.Lang;

public class CrashDialog extends JDialog {

	private static final long serialVersionUID = 666751861226461825L;

	private JPanel base = new JPanel();
	private JButton ok = new JButton(Lang.getString("ui.ok"));
	private JTextArea report = new JTextArea();
	private JScrollPane reportOuter = new JScrollPane(report);
	
	public CrashDialog() {
		setResizable(false);
		setModal(true);

		base.setPreferredSize(new Dimension(650, 400));
		base.setLayout(null);

		add(base);
		pack();
		createFrame();
		setTitle(Lang.getString("ui.crash.title"));

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
	}
	
	public void setReport(String reportStr) {
		report.setText(reportStr);
	}
	
	private void createFrame() {
		base.add(ok);
		ok.setSize(100, 30);
		ok.setLocation(275, 360);
		ok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				CrashDialog.this.setVisible(false);
			}
		});
		
		base.add(reportOuter);
		reportOuter.setSize(600, 340);
		reportOuter.setLocation(25, 15);
	}

}
