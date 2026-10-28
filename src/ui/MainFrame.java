package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.Book;
import model.BorrowRecord;
import model.Member;
import service.Library;

/** Modern, animated Swing interface for the Library Management System. */
public class MainFrame extends JFrame {
    private static final Color NAVY = new Color(20, 34, 61);
    private static final Color NAVY_LIGHT = new Color(34, 52, 84);
    private static final Color BLUE = new Color(55, 105, 220);
    private static final Color BLUE_HOVER = new Color(43, 88, 193);
    private static final Color TEAL = new Color(19, 151, 139);
    private static final Color BG = new Color(244, 247, 251);
    private static final Color CARD = Color.WHITE;
    private static final Color TEXT = new Color(36, 48, 68);
    private static final Color MUTED = new Color(119, 132, 153);
    private static final Color BORDER = new Color(226, 232, 240);

    private final Library library;
    private final String username;
    private final boolean memberRole;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageHost = new JPanel(pageLayout);
    private final JLabel pageTitle = new JLabel("Overview");
    private final JLabel pageSubtitle = new JLabel("Your library at a glance");
    private final JLabel totalBooks = new JLabel("0");
    private final JLabel availableBooks = new JLabel("0");
    private final JLabel borrowedBooks = new JLabel("0");
    private final JLabel totalMembers = new JLabel("0");
    private final JButton[] navButtons = new JButton[5];
    private final Runnable[] refreshers = new Runnable[5];
    private int activePage = 0;
    private Timer toastTimer;
    private JWindow toastWindow;

    public MainFrame(Library library, String username, String role) {
        this.library = library;
        this.username = username;
        this.memberRole = "MEMBER".equalsIgnoreCase(role);
        setTitle("Public Library | Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        pageHost.add(buildDashboard(), "0");
        pageHost.add(buildBooksPage(), "1");
        pageHost.add(buildMembersPage(), "2");
        pageHost.add(buildBorrowPage(), "3");
        pageHost.add(buildReturnsPage(), "4");

        refreshDashboard(false);
        showPage(0);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(228, 0));
        sidebar.setBorder(new EmptyBorder(24, 14, 18, 14));

        JPanel brand = new JPanel(new BorderLayout(11, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel("L", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(new Color(76, 125, 242));
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setPreferredSize(new Dimension(43, 43));
        logo.setBorder(BorderFactory.createEmptyBorder());
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("Public Library");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel caption = new JLabel("Management System");
        caption.setForeground(new Color(164, 178, 204));
        caption.setFont(new Font("SansSerif", Font.BOLD, 9));
        brandText.add(name);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(caption);
        brand.add(logo, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(38, 0, 0, 0));
        String[] labels = { "Dashboard", memberRole ? "Available books" : "Books", "Members", "Borrow a book", "Returns & history" };
        String[] icons = { "⌂", "▤", "♙", "⇢", "↺" };
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JButton b = new JButton("  " + icons[i] + "     " + labels[i]);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));
            b.setForeground(new Color(205, 215, 232));
            b.setBackground(NAVY);
            b.setBorder(new EmptyBorder(13, 13, 13, 8));
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> showPage(index));
            navButtons[i] = b;
            b.setVisible(!memberRole || i < 2);
            nav.add(b);
            nav.add(Box.createVerticalStrut(7));
        }
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(57, 73, 101));
        separator.setBackground(new Color(57, 73, 101));
        bottom.add(separator);
        bottom.add(Box.createVerticalStrut(14));
        JLabel admin = new JLabel(memberRole ? "●   Member workspace" : "●   Librarian workspace");
        admin.setForeground(new Color(177, 192, 215));
        admin.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bottom.add(admin);
        bottom.add(Box.createVerticalStrut(7));
        JLabel version = new JLabel("DESKTOP EDITION  •  1.0");
        version.setForeground(new Color(116, 134, 163));
        version.setFont(new Font("SansSerif", Font.PLAIN, 9));
        bottom.add(version);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(22, 30, 19, 30)));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 23));
        pageTitle.setForeground(TEXT);
        pageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pageSubtitle.setForeground(MUTED);
        heading.add(pageTitle);
        heading.add(Box.createVerticalStrut(5));
        heading.add(pageSubtitle);
        header.add(heading, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 2));
        right.setOpaque(false);
        JLabel date = new JLabel(java.time.LocalDate.now().toString());
        date.setFont(new Font("SansSerif", Font.PLAIN, 12));
        date.setForeground(MUTED);
        JLabel identity = new JLabel(username + "  ·  " + (memberRole ? "Member" : "Admin"));
        identity.setFont(new Font("SansSerif", Font.BOLD, 11));
        identity.setForeground(TEXT);
        JButton logout = secondaryButton("Log out");
        logout.addActionListener(e -> { dispose(); new LoginFrame(library).setVisible(true); });
        JLabel avatar = new JLabel(username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase(), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(224, 235, 255));
        avatar.setForeground(BLUE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 12));
        avatar.setPreferredSize(new Dimension(37, 37));
        avatar.setBorder(BorderFactory.createLineBorder(new Color(206, 220, 251)));
        right.add(date);
        right.add(Box.createHorizontalStrut(9));
        right.add(identity);
        right.add(avatar);
        right.add(logout);
        header.add(right, BorderLayout.EAST);

        pageHost.setBackground(BG);
        pageHost.setBorder(new EmptyBorder(24, 30, 28, 30));
        main.add(header, BorderLayout.NORTH);
        main.add(pageHost, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildDashboard() {
        JPanel root = pagePanel();
        root.setLayout(new BorderLayout(0, 22));

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Welcome to your library 👋");
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(TEXT);
        JLabel desc = new JLabel("Here's what's happening across your library today.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(MUTED);
        welcome.add(title);
        welcome.add(Box.createVerticalStrut(6));
        welcome.add(desc);
        top.add(welcome, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, memberRole ? 1 : 4, 15, 0));
        stats.setOpaque(false);
        if (memberRole) {
            stats.add(statCard("BOOKS AVAILABLE TO READ", availableBooks, "✓", new Color(18, 151, 129), new Color(225, 248, 242)));
        } else {
            stats.add(statCard("TOTAL BOOKS", totalBooks, "▤", new Color(67, 111, 226), new Color(235, 241, 255)));
            stats.add(statCard("AVAILABLE", availableBooks, "✓", new Color(18, 151, 129), new Color(225, 248, 242)));
            stats.add(statCard("ON LOAN", borrowedBooks, "⇢", new Color(219, 143, 43), new Color(255, 244, 225)));
            stats.add(statCard("MEMBERS", totalMembers, "♙", new Color(139, 92, 205), new Color(243, 235, 255)));
        }
        top.add(stats, BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        if (memberRole) {
            RoundedPanel memberCard = new RoundedPanel(18, CARD);
            memberCard.setLayout(new BorderLayout(0, 12));
            memberCard.setBorder(new EmptyBorder(22, 24, 22, 24));
            memberCard.add(sectionTitle("Explore the library"), BorderLayout.NORTH);
            JLabel message = new JLabel("Browse the available collection and search by book title, author, ID, or category.");
            message.setFont(new Font("SansSerif", Font.PLAIN, 13)); message.setForeground(MUTED);
            memberCard.add(message, BorderLayout.CENTER);
            JButton browse = primaryButton("Search available books  →");
            browse.addActionListener(e -> showPage(1));
            JPanel browseRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); browseRow.setOpaque(false); browseRow.add(browse);
            memberCard.add(browseRow, BorderLayout.SOUTH);
            root.add(memberCard, BorderLayout.CENTER);
        } else {
            JPanel lower = new JPanel(new GridLayout(1, 2, 18, 0));
            lower.setOpaque(false); lower.add(buildRecentActivityCard()); lower.add(buildQuickActionsCard());
            root.add(lower, BorderLayout.CENTER);
        }
        return root;
    }

    private JPanel statCard(String title, JLabel value, String icon, Color accent, Color iconBg) {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(8, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(MUTED);
        JLabel glyph = new JLabel(icon, SwingConstants.CENTER);
        glyph.setOpaque(true);
        glyph.setBackground(iconBg);
        glyph.setForeground(accent);
        glyph.setFont(new Font("SansSerif", Font.BOLD, 18));
        glyph.setPreferredSize(new Dimension(38, 38));
        row.add(label, BorderLayout.WEST);
        row.add(glyph, BorderLayout.EAST);
        value.setFont(new Font("SansSerif", Font.BOLD, 31));
        value.setForeground(TEXT);
        JLabel foot = new JLabel("Live library count");
        foot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        foot.setForeground(MUTED);
        card.add(row, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRecentActivityCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(0, 13));
        card.setBorder(new EmptyBorder(20, 20, 18, 20));
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = sectionTitle("Recent activity");
        JLabel badge = new JLabel("LATEST");
        badge.setFont(new Font("SansSerif", Font.BOLD, 9));
        badge.setForeground(BLUE);
        badge.setOpaque(true);
        badge.setBackground(new Color(235, 241, 255));
        badge.setBorder(new EmptyBorder(5, 8, 5, 8));
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(badge, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        JPanel activity = new JPanel();
        activity.setOpaque(false);
        activity.setLayout(new BoxLayout(activity, BoxLayout.Y_AXIS));
        card.putClientProperty("activityList", activity);
        card.add(activity, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActionsCard() {
        RoundedPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.add(sectionTitle("Quick actions"), BorderLayout.NORTH);
        JPanel actions = new JPanel(new GridLayout(3, 1, 0, 10));
        actions.setOpaque(false);
        actions.add(actionTile("＋", "Add a new book", "Create a book record", new Color(235, 241, 255), BLUE, () -> showPage(1)));
        actions.add(actionTile("♙", "Register a member", "Add a library member", new Color(225, 248, 242), TEAL, () -> showPage(2)));
        actions.add(actionTile("⇢", "Borrow a book", "Start a new loan", new Color(255, 244, 225), new Color(193, 124, 28), () -> showPage(3)));
        card.add(actions, BorderLayout.CENTER);
        return card;
    }

    private JButton actionTile(String icon, String title, String subtitle, Color iconBg, Color iconColor, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(12, 0));
        button.setBackground(new Color(250, 251, 253));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 12, 10, 12)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel glyph = new JLabel(icon, SwingConstants.CENTER);
        glyph.setOpaque(true);
        glyph.setBackground(iconBg);
        glyph.setForeground(iconColor);
        glyph.setFont(new Font("SansSerif", Font.BOLD, 18));
        glyph.setPreferredSize(new Dimension(39, 39));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel main = new JLabel(title);
        main.setFont(new Font("SansSerif", Font.BOLD, 12));
        main.setForeground(TEXT);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sub.setForeground(MUTED);
        texts.add(main);
        texts.add(Box.createVerticalStrut(4));
        texts.add(sub);
        JLabel arrow = new JLabel("›");
        arrow.setForeground(MUTED);
        arrow.setFont(new Font("SansSerif", Font.BOLD, 24));
        button.add(glyph, BorderLayout.WEST);
        button.add(texts, BorderLayout.CENTER);
        button.add(arrow, BorderLayout.EAST);
        button.addActionListener(e -> action.run());
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(243, 247, 253)); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(new Color(250, 251, 253)); }
        });
        return button;
    }

    private JPanel buildBooksPage() {
        JPanel root = pagePanel();
        root.setLayout(new BorderLayout(0, 16));
        JTextField id = input("e.g. B003"), title = input("Book title"), author = input("Author name"), category = input("Category");
        JPanel form = formCard("Add a book", "Enter the details below to add a book to your collection.");
        JPanel fields = new JPanel(new GridLayout(1, 4, 12, 0));
        fields.setOpaque(false);
        fields.add(labeled("BOOK ID", id)); fields.add(labeled("TITLE", title));
        fields.add(labeled("AUTHOR", author)); fields.add(labeled("CATEGORY", category));
        JButton add = primaryButton("＋  Add book");
        add.addActionListener(e -> runAction(() -> {
            library.addBook(new Book(id.getText().trim(), title.getText().trim(), author.getText().trim(), category.getText().trim()));
            clear(id, title, author, category); refreshers[1].run();
        }, "Book added successfully."));
        JPanel formBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); formBottom.setOpaque(false); formBottom.add(add);
        form.add(fields, BorderLayout.CENTER); form.add(formBottom, BorderLayout.SOUTH);

        DefaultTableModel model = tableModel(new String[]{"BOOK ID", "TITLE", "AUTHOR", "CATEGORY", "STATUS"});
        JTable table = styledTable(model);
        JTextField search = input("Search by title, author, ID...");
        JButton remove = dangerButton("Remove selected");
        JButton showAll = secondaryButton("Show all");
        JPanel toolbar = new JPanel(new BorderLayout(10, 0)); toolbar.setOpaque(false);
        toolbar.add(search, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); right.setOpaque(false); right.add(showAll); right.add(remove);
        toolbar.add(right, BorderLayout.EAST);
        JPanel tableCard = tableCard("Book collection", toolbar, table);
        Runnable all = () -> loadBooks(model, library.getBooks());
        refreshers[1] = all;
        search.getDocument().addDocumentListener(documentListener(() -> loadBooks(model, library.searchBooks(search.getText()))));
        showAll.addActionListener(e -> { search.setText(""); all.run(); });
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError("Select a book from the table first."); return; }
            String bookId = model.getValueAt(table.convertRowIndexToModel(row), 0).toString();
            int answer = JOptionPane.showConfirmDialog(this, "Remove book " + bookId + "?", "Confirm removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) runAction(() -> { library.removeBook(bookId); all.run(); }, "Book removed successfully.");
        });
        if (memberRole) {
            form.setVisible(false);
            remove.setVisible(false);
            showAll.setText("Show available");
            // Members can only browse available books, including when searching.
            Runnable availableOnly = () -> loadBooks(model, library.getAvailableBooks());
            refreshers[1] = availableOnly;
            search.getDocument().addDocumentListener(documentListener(() -> {
                List<Book> matches = library.searchBooks(search.getText());
                loadBooks(model, matches.stream().filter(Book::isAvailable).toList());
            }));
            showAll.addActionListener(e -> { search.setText(""); availableOnly.run(); });
            tableCard = tableCard("Available books", toolbar, table);
            availableOnly.run();
        } else {
            root.add(form, BorderLayout.NORTH);
            all.run();
        }
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildMembersPage() {
        JPanel root = pagePanel(); root.setLayout(new BorderLayout(0, 16));
        JTextField id = input("e.g. M002"), name = input("Full name"), contact = input("Contact number");
        JPanel form = formCard("Register a member", "Create a member profile for borrowing books.");
        JPanel fields = new JPanel(new GridLayout(1, 3, 12, 0)); fields.setOpaque(false);
        fields.add(labeled("MEMBER ID", id)); fields.add(labeled("FULL NAME", name)); fields.add(labeled("CONTACT NUMBER", contact));
        JButton add = primaryButton("＋  Add member");
        add.addActionListener(e -> runAction(() -> { library.addMember(new Member(id.getText().trim(), name.getText().trim(), contact.getText().trim())); clear(id, name, contact); refreshers[2].run(); }, "Member registered successfully."));
        JPanel formBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); formBottom.setOpaque(false); formBottom.add(add);
        form.add(fields, BorderLayout.CENTER); form.add(formBottom, BorderLayout.SOUTH);

        DefaultTableModel model = tableModel(new String[]{"MEMBER ID", "FULL NAME", "CONTACT NUMBER"});
        JTable table = styledTable(model); JTextField search = input("Search members by name, ID, or contact...");
        JButton showAll = secondaryButton("Show all");
        JPanel toolbar = new JPanel(new BorderLayout(10, 0)); toolbar.setOpaque(false); toolbar.add(search, BorderLayout.CENTER); toolbar.add(showAll, BorderLayout.EAST);
        JPanel tableCard = tableCard("Member directory", toolbar, table);
        Runnable all = () -> loadMembers(model, library.getMembers()); refreshers[2] = all;
        search.getDocument().addDocumentListener(documentListener(() -> loadMembers(model, library.searchMembers(search.getText()))));
        showAll.addActionListener(e -> { search.setText(""); all.run(); });
        root.add(form, BorderLayout.NORTH); root.add(tableCard, BorderLayout.CENTER); all.run();
        return root;
    }

    private JPanel buildBorrowPage() {
        JPanel root = pagePanel(); root.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(8, 8, 8, 8); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        RoundedPanel card = new RoundedPanel(20, CARD); card.setLayout(new BorderLayout(0, 18)); card.setBorder(new EmptyBorder(28, 30, 28, 30)); card.setPreferredSize(new Dimension(620, 360));
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(sectionTitle("Create a borrowing record")); titleBlock.add(Box.createVerticalStrut(7));
        JLabel desc = new JLabel("Choose a registered member and an available book to start a loan."); desc.setForeground(MUTED); desc.setFont(new Font("SansSerif", Font.PLAIN, 12)); titleBlock.add(desc);
        card.add(titleBlock, BorderLayout.NORTH);
        JComboBox<Member> members = styledCombo(); JComboBox<Book> books = styledCombo();
        JPanel fields = new JPanel(new GridLayout(2, 1, 0, 14)); fields.setOpaque(false);
        fields.add(labeled("SELECT MEMBER", members)); fields.add(labeled("AVAILABLE BOOK", books));
        card.add(fields, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false);
        JButton refresh = secondaryButton("↻  Refresh choices"), borrow = primaryButton("Borrow book  →");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); buttons.setOpaque(false); buttons.add(refresh); buttons.add(borrow);
        JLabel note = new JLabel("Only available books are listed."); note.setFont(new Font("SansSerif", Font.PLAIN, 11)); note.setForeground(MUTED);
        bottom.add(note, BorderLayout.WEST); bottom.add(buttons, BorderLayout.EAST); card.add(bottom, BorderLayout.SOUTH);
        g.gridx = 0; g.gridy = 0; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER; root.add(card, g);
        Runnable load = () -> { members.removeAllItems(); library.getMembers().forEach(members::addItem); books.removeAllItems(); library.getAvailableBooks().forEach(books::addItem); };
        refreshers[3] = load; refresh.addActionListener(e -> load.run());
        borrow.addActionListener(e -> runAction(() -> { library.borrowBook((Member) members.getSelectedItem(), (Book) books.getSelectedItem()); load.run(); refreshers[4].run(); }, "Book borrowed successfully."));
        load.run(); return root;
    }

    private JPanel buildReturnsPage() {
        JPanel root = pagePanel(); root.setLayout(new BorderLayout(0, 16));
        DefaultTableModel model = tableModel(new String[]{"RECORD ID", "BOOK", "MEMBER", "BORROW DATE", "RETURN DATE", "STATUS"});
        JTable table = styledTable(model);
        JButton refresh = secondaryButton("↻  Refresh"); JButton ret = primaryButton("Return selected book  ✓");
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); toolbar.setOpaque(false); toolbar.add(refresh); toolbar.add(ret);
        JPanel tableCard = tableCard("Borrowing & return history", toolbar, table);
        Runnable load = () -> loadRecords(model); refreshers[4] = load; refresh.addActionListener(e -> load.run());
        ret.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError("Select a borrowing record first."); return; }
            int modelRow = table.convertRowIndexToModel(row);
            String id = model.getValueAt(modelRow, 0).toString();
            BorrowRecord found = null; for (BorrowRecord r : library.getRecords()) if (r.getRecordId().equals(id)) found = r;
            BorrowRecord selected = found;
            if (selected != null && !selected.isActive()) { showError("This book has already been returned."); return; }
            runAction(() -> library.returnBook(selected), "Book returned successfully."); load.run();
        });
        root.add(tableCard, BorderLayout.CENTER); load.run(); return root;
    }

    private JPanel formCard(String title, String subtitle) {
        RoundedPanel card = new RoundedPanel(18, CARD); card.setLayout(new BorderLayout(0, 17)); card.setBorder(new EmptyBorder(19, 20, 18, 20));
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(sectionTitle(title)); heading.add(Box.createVerticalStrut(5)); JLabel sub = new JLabel(subtitle); sub.setFont(new Font("SansSerif", Font.PLAIN, 11)); sub.setForeground(MUTED); heading.add(sub);
        card.add(heading, BorderLayout.NORTH); return card;
    }

    private JPanel tableCard(String title, JComponent toolbar, JTable table) {
        RoundedPanel card = new RoundedPanel(18, CARD); card.setLayout(new BorderLayout(0, 15)); card.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel heading = new JPanel(new BorderLayout(0, 12)); heading.setOpaque(false); heading.add(sectionTitle(title), BorderLayout.NORTH); heading.add(toolbar, BorderLayout.CENTER);
        card.add(heading, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(BORDER)); scroll.getViewport().setBackground(Color.WHITE); scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scroll, BorderLayout.CENTER); return card;
    }

    private JPanel pagePanel() { JPanel p = new JPanel(); p.setOpaque(false); p.setBorder(new EmptyBorder(0, 0, 0, 0)); return p; }
    private JLabel sectionTitle(String text) { JLabel l = new JLabel(text); l.setFont(new Font("SansSerif", Font.BOLD, 15)); l.setForeground(TEXT); return l; }

    private JTextField input(String placeholder) {
        JTextField f = new JTextField(); f.setFont(new Font("SansSerif", Font.PLAIN, 12)); f.setForeground(TEXT); f.setCaretColor(BLUE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 10, 9, 10)));
        f.setPreferredSize(new Dimension(150, 38)); f.setToolTipText(placeholder); return f;
    }

    private JPanel labeled(String text, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 7)); p.setOpaque(false);
        JLabel label = new JLabel(text); label.setFont(new Font("SansSerif", Font.BOLD, 10)); label.setForeground(MUTED);
        p.add(label, BorderLayout.NORTH); p.add(component, BorderLayout.CENTER); return p;
    }

    private <T> JComboBox<T> styledCombo() {
        JComboBox<T> combo = new JComboBox<>(); combo.setFont(new Font("SansSerif", Font.PLAIN, 12)); combo.setBackground(Color.WHITE); combo.setForeground(TEXT); combo.setPreferredSize(new Dimension(220, 39)); return combo;
    }

    private JButton primaryButton(String text) { return styledButton(text, BLUE, Color.WHITE, BLUE_HOVER); }
    private JButton secondaryButton(String text) { return styledButton(text, new Color(247, 249, 252), TEXT, new Color(235, 240, 247)); }
    private JButton dangerButton(String text) { return styledButton(text, new Color(255, 242, 242), new Color(190, 58, 58), new Color(255, 229, 229)); }

    private JButton styledButton(String text, Color background, Color foreground, Color hover) {
        JButton b = new JButton(text); b.setFont(new Font("SansSerif", Font.BOLD, 11)); b.setBackground(background); b.setForeground(foreground);
        b.setFocusPainted(false); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(background.equals(BLUE) ? BLUE : BORDER), new EmptyBorder(9, 13, 9, 13)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); } @Override public void mouseExited(MouseEvent e) { b.setBackground(background); } });
        return b;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model); table.setFont(new Font("SansSerif", Font.PLAIN, 12)); table.setForeground(TEXT); table.setBackground(Color.WHITE);
        table.setRowHeight(36); table.setShowVerticalLines(false); table.setShowHorizontalLines(true); table.setGridColor(new Color(239, 242, 247));
        table.setSelectionBackground(new Color(231, 239, 255)); table.setSelectionForeground(TEXT); table.setFillsViewportHeight(true); table.setAutoCreateRowSorter(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 10)); table.getTableHeader().setForeground(MUTED); table.getTableHeader().setBackground(new Color(248, 250, 253));
        table.getTableHeader().setPreferredSize(new Dimension(10, 37)); table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 8));
                if (!selected) setBackground(row % 2 == 0 ? Color.WHITE : new Color(251, 252, 254));
                if (value != null && ("Available".equals(value.toString()) || "Returned".equals(value.toString()))) setForeground(new Color(17, 137, 105));
                else if (value != null && "Borrowed".equals(value.toString())) setForeground(new Color(192, 124, 27));
                else if (!selected) setForeground(TEXT);
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        return table;
    }

    private DefaultTableModel tableModel(String[] cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int col) { return false; } };
    }

    private void loadBooks(DefaultTableModel model, List<Book> books) {
        model.setRowCount(0);
        for (Book b : books) model.addRow(new Object[]{b.getBookId(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getStatus()});
    }

    private void loadMembers(DefaultTableModel model, List<Member> members) {
        model.setRowCount(0);
        for (Member m : members) model.addRow(new Object[]{m.getMemberId(), m.getName(), m.getContactNumber()});
    }

    private void loadRecords(DefaultTableModel model) {
        model.setRowCount(0);
        for (BorrowRecord r : library.getRecords()) model.addRow(new Object[]{r.getRecordId(), r.getBook().getTitle(), r.getMember().getName(), r.getBorrowDate(), r.getReturnDate() == null ? "—" : r.getReturnDate(), r.isActive() ? "Borrowed" : "Returned"});
    }

    private DocumentListener documentListener(Runnable action) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        };
    }

    private void clear(JTextField... fields) { for (JTextField f : fields) f.setText(""); }

    private void runAction(Runnable action, String success) {
        try {
            action.run(); refreshDashboard(true); showToast(success);
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
            showError(ex.getMessage() == null ? "Please check the information and try again." : ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Unable to complete action", JOptionPane.ERROR_MESSAGE);
    }

    private void refreshDashboard(boolean animate) {
        setCount(totalBooks, library.getBooks().size(), animate);
        setCount(availableBooks, (int) library.getAvailableCount(), animate);
        setCount(borrowedBooks, (int) library.getBorrowedCount(), animate);
        setCount(totalMembers, library.getMembers().size(), animate);
        refreshActivity();
    }

    private void setCount(JLabel label, int target, boolean animate) {
        Object old = label.getClientProperty("numericValue");
        int start = old instanceof Integer ? (Integer) old : 0;
        label.putClientProperty("numericValue", target);
        if (!animate || start == target) { label.setText(String.valueOf(target)); return; }
        final int steps = 12; final int[] step = {0};
        Timer timer = new Timer(18, null);
        timer.addActionListener(e -> {
            step[0]++;
            int value = start + (int) Math.round((target - start) * (step[0] / (double) steps));
            label.setText(String.valueOf(value));
            if (step[0] >= steps) { label.setText(String.valueOf(target)); timer.stop(); }
        });
        timer.start();
    }

    private void refreshActivity() {
        // The activity card is built with the dashboard. Locate its activity list without coupling the model to UI details.
        for (Component component : pageHost.getComponents()) {
            if (!(component instanceof JPanel)) continue;
            JPanel page = (JPanel) component;
            for (Component child : page.getComponents()) {
                if (child instanceof JPanel childPanel) findAndRefreshActivity(childPanel);
            }
        }
    }

    private void findAndRefreshActivity(JPanel panel) {
        Object value = panel.getClientProperty("activityList");
        if (value instanceof JPanel activity) {
            activity.removeAll();
            List<BorrowRecord> records = library.getRecords();
            int added = 0;
            for (int i = records.size() - 1; i >= 0 && added < 4; i--) {
                BorrowRecord r = records.get(i);
                activity.add(activityRow(r.isActive() ? "Book borrowed" : "Book returned", r.getBook().getTitle(), r.getMember().getName(), r.isActive()));
                activity.add(Box.createVerticalStrut(8)); added++;
            }
            if (added == 0) {
                JLabel empty = new JLabel("No activity yet. Borrow or return a book to see it here."); empty.setForeground(MUTED); empty.setFont(new Font("SansSerif", Font.PLAIN, 11));
                activity.add(Box.createVerticalStrut(22)); activity.add(empty);
            }
            activity.revalidate(); activity.repaint();
        }
        for (Component child : panel.getComponents()) if (child instanceof JPanel childPanel) findAndRefreshActivity(childPanel);
    }

    private JPanel activityRow(String title, String book, String member, boolean borrowed) {
        JPanel row = new JPanel(new BorderLayout(9, 0)); row.setOpaque(false);
        JLabel icon = new JLabel(borrowed ? "↗" : "✓", SwingConstants.CENTER); icon.setOpaque(true); icon.setBackground(borrowed ? new Color(255, 244, 225) : new Color(225, 248, 242)); icon.setForeground(borrowed ? new Color(193, 124, 28) : TEAL); icon.setFont(new Font("SansSerif", Font.BOLD, 15)); icon.setPreferredSize(new Dimension(32, 32));
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel main = new JLabel(title); main.setForeground(TEXT); main.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel detail = new JLabel(book + " · " + member); detail.setForeground(MUTED); detail.setFont(new Font("SansSerif", Font.PLAIN, 10));
        text.add(main); text.add(Box.createVerticalStrut(4)); text.add(detail);
        row.add(icon, BorderLayout.WEST); row.add(text, BorderLayout.CENTER); return row;
    }

    private void showPage(int index) {
        if (index < 0 || index >= pageHost.getComponentCount()) return;
        if (memberRole && index > 1) {
            showError("Your member account can access only the dashboard and available books.");
            return;
        }
        activePage = index;
        pageLayout.show(pageHost, String.valueOf(index));
        String[] titles = {"Overview", memberRole ? "Available books" : "Books", "Members", "Borrow a book", "Returns & history"};
        String[] subtitles = {"Your library at a glance", memberRole ? "Search and browse books currently available" : "Manage your collection and availability", "Manage registered library members", "Create a borrowing record in a few steps", "Track loans and complete book returns"};
        pageTitle.setText(titles[index]); pageSubtitle.setText(subtitles[index]);
        for (int i = 0; i < navButtons.length; i++) {
            boolean selected = i == index;
            navButtons[i].setBackground(selected ? NAVY_LIGHT : NAVY);
            navButtons[i].setForeground(selected ? Color.WHITE : new Color(205, 215, 232));
            navButtons[i].setFont(new Font("SansSerif", selected ? Font.BOLD : Font.PLAIN, 13));
        }
        if (activePage == 0) refreshDashboard(false);
        else if (refreshers[activePage] != null) refreshers[activePage].run();
    }

    private void showToast(String message) {
        if (toastTimer != null) toastTimer.stop();
        if (toastWindow != null) toastWindow.dispose();
        toastWindow = new JWindow(this);
        JPanel panel = new JPanel(new BorderLayout(10, 0)); panel.setBackground(new Color(25, 45, 70)); panel.setBorder(new EmptyBorder(13, 17, 13, 19));
        JLabel check = new JLabel("✓"); check.setForeground(new Color(91, 225, 180)); check.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel text = new JLabel(message); text.setForeground(Color.WHITE); text.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(check, BorderLayout.WEST); panel.add(text, BorderLayout.CENTER); toastWindow.add(panel); toastWindow.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        toastWindow.setLocation(screen.width - toastWindow.getWidth() - 28, screen.height - toastWindow.getHeight() - 65);
        toastWindow.setVisible(true);
        toastTimer = new Timer(2400, e -> { if (toastWindow != null) toastWindow.dispose(); }); toastTimer.setRepeats(false); toastTimer.start();
    }

    private static class RoundedPanel extends JPanel {
        private final int radius; private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(31, 49, 78, 13)); g.fillRoundRect(1, 3, getWidth() - 2, getHeight() - 4, radius, radius);
            g.setColor(fill); g.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 5, radius, radius); g.dispose(); super.paintComponent(graphics);
        }
    }
}
