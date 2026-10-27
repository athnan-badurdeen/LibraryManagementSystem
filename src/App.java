import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import model.Book;
import model.Member;
import service.Library;
import ui.LoginFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            Library library = new Library();
            library.addBook(new Book("B001", "Clean Code", "Robert C. Martin", "Programming"));
            library.addBook(new Book("B002", "Java: The Complete Reference", "Herbert Schildt", "Programming"));
            library.addMember(new Member("M001", "Sample Student", "0712345678"));
            new LoginFrame(library).setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        // FlatLaf is used when it is available on the classpath. The app remains runnable with plain JDK too.
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            return;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
            // Fall back gracefully for users running with javac/java only.
        }
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) { }
    }
}
