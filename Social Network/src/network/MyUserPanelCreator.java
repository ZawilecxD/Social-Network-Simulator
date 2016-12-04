package network;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import repast.simphony.userpanel.ui.UserPanelCreator;

public class MyUserPanelCreator implements UserPanelCreator{

	@Override
	public JPanel createPanel() {
		JPanel newPanel = new JPanel(new GridBagLayout());
		newPanel.setBorder(BorderFactory.createTitledBorder(
		        BorderFactory.createEtchedBorder(), "Login Panel"));
		JButton buttonLogin = new JButton("Login");
		JTextField textUsername = new JTextField(20);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        
        buttonLogin.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            SocialNetworkContext.testUserPanel(textUsername.getText());
          }
        });
        newPanel.add(textUsername);
        newPanel.add(buttonLogin, constraints);
		JLabel label = new JLabel("Enter username:");
		newPanel.add(label);
		return newPanel;
	}

}
