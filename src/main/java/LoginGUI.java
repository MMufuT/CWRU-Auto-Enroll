import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginGUI implements ActionListener{

    private static JLabel caseIDLabel;
    private static JTextField caseIDText;
    private static JLabel passwordLabel;
    private static JPasswordField passwordText;
    private static JLabel phoneNumberLabel;
    private static JTextField phoneNumberText;
    private static JButton loginButton;

    private static String user;
    private static String password;
    public static String phoneNumber;

    public static String getUser(){
        return user;
    }

    public static String getPassword(){
        return password;
    }

    public static String getPhoneNumber(){
        return phoneNumber;
    }



    public static void main(String[] args){
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        frame.setSize(500,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        panel.setLayout(null);

        caseIDLabel = new JLabel("CaseID");
        caseIDLabel.setBounds(50,20, 80, 25);
        panel.add(caseIDLabel);

        caseIDText = new JTextField(50);
        caseIDText.setBounds(160,20, 250, 25);
        panel.add(caseIDText);

        passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(50, 70, 80, 25);
        panel.add(passwordLabel);

        passwordText = new JPasswordField(50);
        passwordText.setBounds(160,70, 250, 25);
        panel.add(passwordText);

        phoneNumberLabel = new JLabel("Phone Number");
        phoneNumberLabel.setBounds(50, 120, 120, 25);
        panel.add(phoneNumberLabel);

        phoneNumberText = new JTextField(50);
        phoneNumberText.setBounds(160,120, 250, 25);
        panel.add(phoneNumberText);

        loginButton = new JButton("Login");
        loginButton.setBounds(185,170, 200, 25);
        loginButton.addActionListener(new LoginGUI());
        panel.add(loginButton);

        frame.setVisible(true);
    }

    @Override public void actionPerformed(ActionEvent e){
        user = caseIDText.getText();
        password = String.valueOf(passwordText.getPassword());
        phoneNumber = phoneNumberText.getText();
        CWRUAutoEnroll.main(null);
    }
}

