import java.awt.*;
import javax.swing.*;

public class UIComponents {
    public static JButton createStyledTileButton() {
        JButton button = new JButton();
        button.setFont(Constants.TILE_FONT);
        button.setFocusPainted(false);
        button.setBackground(Constants.TILE);
        button.setForeground(Constants.TILE_TEXT);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return button;
    }

    public static JLabel createStyledLabel(String text, String emoji) {
        JLabel label = new JLabel(emoji + " " + text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    public static JButton createControlButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(Constants.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    public static JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Constants.BACKGROUND);
        JLabel titleLabel = new JLabel("Puzzle Game");
        titleLabel.setFont(Constants.TITLE_FONT);
        titleLabel.setForeground(Color.DARK_GRAY);
        titlePanel.add(titleLabel);
        return titlePanel;
    }
}
