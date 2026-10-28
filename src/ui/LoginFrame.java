package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import service.Library;

/** Login screen and demo credential validation for the desktop application. */
public class LoginFrame extends JFrame {
    private static final Color NAVY       = new Color(15, 23, 42);     // #0F172A
    private static final Color BLUE       = new Color(37, 99, 235);    // #2563EB
    private static final Color TEXT       = new Color(30, 41, 59);     // #1E293B
    private static final Color MUTED      = new Color(100, 116, 139);  // #64748B
    private static final Color BORDER     = new Color(226, 232, 240);  // #E2E8F0

    private final Library library;
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public LoginFrame(Library library) {
        this.library = library;
        setTitle("Public Library | Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 570);
        setMinimumSize(new Dimension(480, 530));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(244, 247, 251));
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(NAVY);
        top.setBorder(new EmptyBorder(28, 34, 28, 34));
        ImageIcon icon = new ImageIcon("/image/LMS Logo.png");
        JLabel logo = new JLabel(icon, SwingConstants.CENTER);
        logo.setOpaque(true); logo.setBackground(BLUE); logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 26)); logo.setPreferredSize(new Dimension(52, 52));
        JPanel brand = new JPanel(new BorderLayout(14, 0)); brand.setOpaque(false);
        JLabel name = new JLabel("UWU Library"); name.setForeground(Color.WHITE); name.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel tagline = new JLabel("Library Management System"); tagline.setForeground(new Color(174, 190, 216)); tagline.setFont(new Font("SansSerif", Font.BOLD, 10));
        JPanel brandText = new JPanel(new BorderLayout(0, 5)); brandText.setOpaque(false); brandText.add(name, BorderLayout.NORTH); brandText.add(tagline, BorderLayout.CENTER);
        brand.add(logo, BorderLayout.WEST); brand.add(brandText, BorderLayout.CENTER); top.add(brand, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout()); content.setOpaque(false); content.setBorder(new EmptyBorder(25, 34, 28, 34));
        GridBagConstraints c = new GridBagConstraints(); c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 0, 10, 0);
        JLabel heading = new JLabel("Welcome back"); heading.setFont(new Font("SansSerif", Font.BOLD, 24)); heading.setForeground(TEXT);
        c.gridy = 0; content.add(heading, c);
        JLabel sub = new JLabel("Sign in to continue to your library workspace."); sub.setFont(new Font("SansSerif", Font.PLAIN, 12)); sub.setForeground(MUTED);
        c.gridy = 1; c.insets = new Insets(0, 0, 22, 0); content.add(sub, c);
        c.insets = new Insets(0, 0, 7, 0); c.gridy = 2; content.add(fieldLabel("USERNAME"), c);
        styleField(username); c.gridy = 3; c.insets = new Insets(0, 0, 15, 0); content.add(username, c);
        c.gridy = 4; c.insets = new Insets(0, 0, 7, 0); content.add(fieldLabel("PASSWORD"), c);
        styleField(password); c.gridy = 5; c.insets = new Insets(0, 0, 20, 0); content.add(password, c);
        JButton login = new JButton("Sign in  →"); login.setFont(new Font("SansSerif", Font.BOLD, 13)); login.setBackground(BLUE); login.setForeground(Color.WHITE); login.setFocusPainted(false); login.setBorder(new EmptyBorder(13, 16, 13, 16)); login.setOpaque(true);
        c.gridy = 6; c.insets = new Insets(0, 0, 17, 0); content.add(login, c);
        JLabel hint = new JLabel("Demo accounts: admin / 1234     •     member / 1234"); hint.setFont(new Font("SansSerif", Font.PLAIN, 10)); hint.setForeground(MUTED);
        c.gridy = 7; c.insets = new Insets(0, 0, 0, 0); content.add(hint, c);
        add(content, BorderLayout.CENTER);

        login.addActionListener(e -> authenticate());
        password.addActionListener(e -> authenticate());
        username.addActionListener(e -> password.requestFocusInWindow());
        getRootPane().setDefaultButton(login);
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text); label.setFont(new Font("SansSerif", Font.BOLD, 10)); label.setForeground(MUTED); return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13)); field.setForeground(TEXT); field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 12, 10, 12)));
        field.setPreferredSize(new Dimension(300, 42));
    }

    private void authenticate() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword());
        String role = null;
        if ("admin".equals(user) && "1234".equals(pass)) role = "ADMIN";
        else if ("member".equals(user) && "1234".equals(pass)) role = "MEMBER";

        if (role == null) {
            JOptionPane.showMessageDialog(this, "The username or password is incorrect.", "Sign-in failed", JOptionPane.ERROR_MESSAGE);
            password.setText("");
            password.requestFocusInWindow();
            return;
        }
        dispose();
        new MainFrame(library, user, role).setVisible(true);
    }
}
