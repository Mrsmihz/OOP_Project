package controller;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;

import mysql.MySQLConnector;
import myutilities.JPlaceholderPasswordField;
import myutilities.JPlaceholderTextField;
import views.*;
import model.*;
import java.util.Timer;
import java.util.TimerTask;

public class LoginController implements ActionListener, KeyListener {
    private LoginGUI loginGUI;
    private User user;
    private JButton loginBtn, registerBtn;
    private JFrame mainFrame;
    private JPlaceholderTextField usernameField;
    private JPlaceholderPasswordField passwordField;
    private RegisterController rc;
    private Timer timer;
    public LoginController(){
        this.loginGUI = new LoginGUI(this);
        this.rc = new RegisterController(this);
        setComponents();
        timer = new Timer();
        loginGUI.getLogoPanel().getImageLabel().setIcon(new ImageIcon(this.getClass().getResource("/imgs/login-gif2.gif")));
        loginGUI.getLogoPanel().getMainPanel().validate();

    }
    private void setComponents(){
        this.loginBtn = loginGUI.getLoginBtn();
        this.registerBtn = loginGUI.getRegisterBtn();
        this.mainFrame = loginGUI.getMainFrame();
        this.usernameField = loginGUI.getUsernameField();
        this.passwordField = loginGUI.getPasswordField();
    }
    public void actionPerformed(ActionEvent event){
        if (event.getSource().equals(loginBtn)){
            login();
        }
        else if (event.getSource().equals(registerBtn)){
            rc.setRegisterGUIVisible(true);
            mainFrame.setVisible(false);
        }
    }
    public void keyPressed(KeyEvent key){
        if (key.getKeyCode() == KeyEvent.VK_ENTER){
            login();
        }
    }
    public void keyTyped(KeyEvent key){

    }
    public void keyReleased(KeyEvent key){

    }
    private void login(){
        if (checkIfLoginSuccess()){
            mainFrame.setVisible(false);
            new MainController(user);
        }
        else{
            JOptionPane.showMessageDialog(mainFrame, "Please Try Again.", "Alert", JOptionPane.WARNING_MESSAGE);
        }
    }
    private boolean checkIfLoginSuccess(){
        try{
            MySQLConnector sql = new MySQLConnector("MyDatabase", "root", "031961698");
            String password = new String(passwordField.getPassword());
            user = new User(sql, usernameField.getText(), password);
            return user.getAccount();
        }
        catch (SQLException e){
            System.out.println(e);
            return false;
        }
    }
    public void setLoginGUIVisible(boolean b){
        mainFrame.setVisible(b);
    }
}
