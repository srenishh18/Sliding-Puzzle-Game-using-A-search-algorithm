import java.awt.*;
import java.util.*;
import javax.swing.*;

public class GameBoard extends JPanel {
    private int gridSize = 3;
    private JButton[][] tiles;
    private JPanel gridPanel;
    private int level = 1;
    private final int MAX_LEVEL = 4;

    private JButton nextLevelBtn, exitBtn, hintBtn;
    private JLabel levelLabel;
    private JButton firstSelected = null;

    public GameBoard() {
        setLayout(new BorderLayout());
        setBackground(Constants.BACKGROUND);
        add(UIComponents.createTitlePanel(), BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setBackground(Constants.BACKGROUND);
        add(gridPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Constants.BACKGROUND);

        levelLabel = UIComponents.createStyledLabel("Level " + level, Constants.EMOJI_PUZZLE);
        nextLevelBtn = UIComponents.createControlButton("Next Level", Constants.BUTTON);
        exitBtn = UIComponents.createControlButton("Exit", Constants.BUTTON_EXIT);
        hintBtn = UIComponents.createControlButton("Hint (Auto-swap)", Constants.BUTTON_HINT);

        bottomPanel.add(levelLabel);
        bottomPanel.add(hintBtn);
        bottomPanel.add(nextLevelBtn);
        bottomPanel.add(exitBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        nextLevelBtn.addActionListener(e -> nextLevel());
        exitBtn.addActionListener(e -> System.exit(0));
        hintBtn.addActionListener(e -> showHint());
        initializeBoard();
    }

    private void initializeBoard() {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(gridSize, gridSize, 5, 5));

        tiles = new JButton[gridSize][gridSize];
        java.util.List<String> labels = generateTileLabels();

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                String label = labels.remove(0);
                JButton tile = UIComponents.createStyledTileButton();
                tile.setBackground(Constants.TILE);
                tile.setText(label);
                tile.addActionListener(e -> selectTile(tile));
                tiles[i][j] = tile;
                gridPanel.add(tile);
            }
        }

        revalidate();
        repaint();
    }

    private java.util.List<String> generateTileLabels() {
        java.util.List<String> labels = new ArrayList<>();
        int totalTiles = gridSize * gridSize;

        if (level <= 2) {
            for (int i = 1; i <= totalTiles; i++) labels.add(String.valueOf(i));
        } else {
            char ch = 'A';
            for (int i = 0; i < totalTiles; i++) {
                labels.add(String.valueOf(ch));
                ch++;
                if (ch > 'Z') ch = 'A';
            }
        }
        java.util.Collections.shuffle(labels);
        return labels;
    }

    private void selectTile(JButton tile) {
        if (firstSelected == null) {
            firstSelected = tile;
            tile.setBackground(Constants.ACCENT);
        } else if (firstSelected == tile) {
            firstSelected.setBackground(Constants.TILE);
            firstSelected = null;
        } else {
            swapTiles(firstSelected, tile);
            firstSelected.setBackground(Constants.TILE);
            firstSelected = null;

            if (isSolved()) {
                JOptionPane.showMessageDialog(this, "🎉 Level " + level + " Completed!");
                if (level < MAX_LEVEL) nextLevel();
                else JOptionPane.showMessageDialog(this, "🏁 All Levels Completed!");
            }
        }
    }

    private void swapTiles(JButton a, JButton b) {
        String tmp = a.getText();
        a.setText(b.getText());
        b.setText(tmp);
    }

    private boolean isSolved() {
        int total = gridSize * gridSize;
        int index = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                String expected;
                if (level <= 2) expected = String.valueOf(index);
                else expected = String.valueOf((char) ('A' + index - 1));
                if (!tiles[i][j].getText().equals(expected)) return false;
                index++;
                if (index > total) break;
            }
        }
        return true;
    }

    public void nextLevel() {
        if (level < MAX_LEVEL) {
            level++;
            gridSize = 2 + level;
            levelLabel.setText("Level " + level);
            initializeBoard();
        } else {
            JOptionPane.showMessageDialog(this, "🎯 Final Level Reached!");
        }
    }

    public void showHint() {
        int[][] current = boardFromTiles();
        int[][] goal = buildGoal();

        // Try A* to find a shortest sequence of adjacent swaps to the goal.
        java.util.List<int[][]> path = findPathAStar(current, goal, 50000);

        if (path != null && path.size() > 1) {
            int[][] next = path.get(1);
            // find the two cells that differ between current and next (they should be swapped)
            int r1 = -1, c1 = -1, r2 = -1, c2 = -1;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    if (current[i][j] != next[i][j]) {
                        if (r1 == -1) { r1 = i; c1 = j; }
                        else { r2 = i; c2 = j; }
                    }
                }
            }
            if (r1 != -1 && r2 != -1) {
                JButton b1 = tiles[r1][c1];
                JButton b2 = tiles[r2][c2];
                Color orig1 = b1.getBackground();
                Color orig2 = b2.getBackground();
                b1.setBackground(Color.YELLOW);
                b2.setBackground(Color.YELLOW);

                javax.swing.Timer t = new javax.swing.Timer(350, e -> {
                    swapTiles(b1, b2);
                    b1.setBackground(orig1);
                    b2.setBackground(orig2);
                    if (isSolved()) {
                        JOptionPane.showMessageDialog(this, "🎉 Level " + level + " Completed!");
                        nextLevel();
                    }
                });
                t.setRepeats(false);
                t.start();
                return;
            }
        }

        // Fallback: greedy single-swap heuristic (previous behavior)
        int bestR1 = -1, bestC1 = -1, bestR2 = -1, bestC2 = -1;
        long bestScore = Long.MAX_VALUE;

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (c + 1 < gridSize) {
                    int[][] sim = deepCopy(current);
                    swapSim(sim, r, c, r, c + 1);
                    long score = heuristic(sim, goal);
                    if (score < bestScore) {
                        bestScore = score; bestR1 = r; bestC1 = c; bestR2 = r; bestC2 = c + 1;
                    }
                }
                if (r + 1 < gridSize) {
                    int[][] sim = deepCopy(current);
                    swapSim(sim, r, c, r + 1, c);
                    long score = heuristic(sim, goal);
                    if (score < bestScore) {
                        bestScore = score; bestR1 = r; bestC1 = c; bestR2 = r + 1; bestC2 = c;
                    }
                }
            }
        }

        if (bestR1 == -1) return;

        JButton b1 = tiles[bestR1][bestC1];
        JButton b2 = tiles[bestR2][bestC2];
        Color orig1 = b1.getBackground();
        Color orig2 = b2.getBackground();
        b1.setBackground(Color.YELLOW);
        b2.setBackground(Color.YELLOW);

        javax.swing.Timer t = new javax.swing.Timer(450, e -> {
            swapTiles(b1, b2);
            b1.setBackground(orig1);
            b2.setBackground(orig2);
            if (isSolved()) {
                JOptionPane.showMessageDialog(this, "🎉 Level " + level + " Completed!");
                nextLevel();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private int[][] boardFromTiles() {
        int[][] b = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                String s = tiles[i][j].getText();
                if (level <= 2) b[i][j] = Integer.parseInt(s);
                else b[i][j] = s.charAt(0) - 'A' + 1;
            }
        }
        return b;
    }

    private int[][] buildGoal() {
        int[][] goal = new int[gridSize][gridSize];
        int val = 1;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) goal[i][j] = val++;
        }
        return goal;
    }

    private int[][] deepCopy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) dst[i] = java.util.Arrays.copyOf(src[i], src[i].length);
        return dst;
    }

    private void swapSim(int[][] sim, int r1, int c1, int r2, int c2) {
        int tmp = sim[r1][c1];
        sim[r1][c1] = sim[r2][c2];
        sim[r2][c2] = tmp;
    }

    private java.util.List<int[][]> findPathAStar(int[][] start, int[][] goal, int maxIterations) {
        class Node implements Comparable<Node> {
            int[][] board;
            int g;
            int f;
            Node parent;

            Node(int[][] b, int g, Node p) {
                this.board = b;
                this.g = g;
                this.parent = p;
                this.f = g + (int)heuristic(b, goal);
            }

            @Override
            public int compareTo(Node o) { return Integer.compare(this.f, o.f); }
        }

        String goalKey = boardKey(goal);
        PriorityQueue<Node> open = new PriorityQueue<>();
        HashMap<String, Integer> gScore = new HashMap<>();

        String startKey = boardKey(start);
        open.add(new Node(deepCopy(start), 0, null));
        gScore.put(startKey, 0);

        int iterations = 0;
        while (!open.isEmpty() && iterations++ < maxIterations) {
            Node cur = open.poll();
            String curKey = boardKey(cur.board);
            if (curKey.equals(goalKey)) {
                // reconstruct
                java.util.List<int[][]> path = new java.util.ArrayList<>();
                Node p = cur;
                while (p != null) {
                    path.add(0, deepCopy(p.board));
                    p = p.parent;
                }
                return path;
            }

            // expand neighbors (adjacent swaps)
            for (int r = 0; r < gridSize; r++) {
                for (int c = 0; c < gridSize; c++) {
                    // right
                    if (c + 1 < gridSize) {
                        int[][] nb = deepCopy(cur.board);
                        swapSim(nb, r, c, r, c + 1);
                        String nbKey = boardKey(nb);
                        int tentativeG = cur.g + 1;
                        if (!gScore.containsKey(nbKey) || tentativeG < gScore.get(nbKey)) {
                            gScore.put(nbKey, tentativeG);
                            open.add(new Node(nb, tentativeG, cur));
                        }
                    }
                    // down
                    if (r + 1 < gridSize) {
                        int[][] nb = deepCopy(cur.board);
                        swapSim(nb, r, c, r + 1, c);
                        String nbKey = boardKey(nb);
                        int tentativeG = cur.g + 1;
                        if (!gScore.containsKey(nbKey) || tentativeG < gScore.get(nbKey)) {
                            gScore.put(nbKey, tentativeG);
                            open.add(new Node(nb, tentativeG, cur));
                        }
                    }
                }
            }
        }

        return null; // no path found or timed out
    }

    private String boardKey(int[][] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) sb.append(b[i][j]).append(',');
        }
        return sb.toString();
    }

    private long heuristic(int[][] current, int[][] goal) {
        int n = gridSize;
        int maxVal = n * n;
        int[] goalR = new int[maxVal + 1];
        int[] goalC = new int[maxVal + 1];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                int v = goal[i][j];
                goalR[v] = i; goalC[v] = j;
            }

        long sum = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                int v = current[i][j];
                sum += Math.abs(i - goalR[v]) + Math.abs(j - goalC[v]);
            }
        return sum;
    }
}
