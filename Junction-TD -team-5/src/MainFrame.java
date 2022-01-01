import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

@SuppressWarnings("serial")
class MainFrame extends JFrame {

	private JunctionPanel _junctionPanel;

	public MainFrame(String appName) {
		super(appName);
		ImageIcon icon = new ImageIcon("img/Images/icon.png");
		this.setIconImage(icon.getImage());
		setLayout(new BorderLayout());
		try {
			this._junctionPanel = new JunctionPanel(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		add(this._junctionPanel, BorderLayout.WEST);

		setSize(1100, 800);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}
