import java.awt.*;
import javax.swing.*;

public class PuzzleGame extends JFrame {

    public PuzzleGame() {
        setTitle("Puzzle Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);

        JLabel levelLabel = new JLabel("Level 1", SwingConstants.CENTER);
        levelLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        GameBoard board = new GameBoard(); // ✅ no arguments now
        add(board);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PuzzleGame::new);
    }
}
