package BoardGame;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;

public class BoardGameClient {

    private static Point selection1;
    static Piece[][] board = new Piece[8][8];
    static PrintWriter out;
    static boolean canMove = false;
    static boolean isWhite;

    public static void main(String[] args) throws Exception {
        try (var socket = new Socket("localhost", 59090)) {
            System.out.println("Press Control-C to quit.");
            var scanner = new Scanner(System.in);
            var in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String line = "";
                        try {
                            line = in.nextLine();
                        } catch (java.util.NoSuchElementException e) {
                            System.out.println("error");
                            System.exit(0);
                        }
                        if (line.contains("Your Turn")) {
                            canMove = true;
                        }
                        if (line.contains("\u265F") || line.contains("\u2659")) {
                            String[] lines = line.split("n");
                            System.out.println(Arrays.toString(lines));
                            stringToBoard(line);
                            for (int i = 0; i < lines.length; i++) {
                                for (int j = 0; j < lines[0].length(); j++) {
                                    // board[i][j] = "" + lines[i].charAt(j);
                                    Window.buttons[i][j].setText("" + lines[i].charAt(j));
                                }
                            }
                        } else {
                            System.out.println(line);
                        }
                    }
                }
            };
            Thread thread1 = new Thread(runnable);
            thread1.start();
            Runnable runnable2 = new Runnable() {
                @Override
                public void run() {
                    {
                        new Window(1440, 810, "Board Game");
                    }
                }
            };
            Thread thread2 = new Thread(runnable2);
            thread2.start();
            while (true) {
                out.println(scanner.nextLine());
            }
        }
    }

    private static void updateBoard() {
        for (int row = 0; row < board.length; row++)
            for (int col = 0; col < board[row].length; col++) {
                Piece tempObject = board[row][col];
                if (tempObject != null) {
                    if (tempObject.canMove() == null)
                        Window.buttons[tempObject.row][tempObject.col].setEnabled(false);
                    else
                        Window.buttons[tempObject.row][tempObject.col].setEnabled(true);
                    if (selection1 != null)
                        showMoves();
                }
            }
    }

    private static void showMoves() {
        if (board[selection1.x][selection1.y].canMove() != null) {
            for (Point p : board[selection1.x][selection1.y].canMove()) {
                Window.buttons[p.x][p.y].setBackground(Color.red);
                Window.buttons[p.x][p.y].setEnabled(true);
            }
        }

    }

    private static void stringToBoard(String s) {
        String[] lines = s.split("n");
        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lines[0].length(); j++) {
                if (lines[i].charAt(j) == ' ') {
                    board[i][j] = null;
                } else if (lines[i].charAt(j) == '\u2659') {
                    board[i][j] = new Piece(i, j, true, board);
                } else {
                    board[i][j] = new Piece(i, j, false, board);
                }
            }
        }

    }

    public static void select(int row, int col) {
        if (Window.buttons[row][col].getBackground() == Color.RED) {
            Window.buttons[row][col].setForeground(null);
            for (Point p : board[selection1.x][selection1.y].canMove()) {
                if ((p.x + p.y) % 2 == 0)
                    Window.buttons[p.x][p.y].setBackground(Color.white);
                else
                    Window.buttons[p.x][p.y].setBackground(Color.gray);
                Window.buttons[p.x][p.y].setEnabled(true);
            }
            // board[selection1.x][selection1.y].move(row, col);
            out.println("" + selection1.x + selection1.y + row + col);
            canMove = false;
            selection1 = null;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Window.buttons[i][j].setForeground(null);
                }
            }
            System.out.println("taken");
        } else { // if the background is not red
            if (!Window.buttons[row][col].getText().equals(" ")) { // if its a piece
                if (Window.buttons[row][col].getForeground() != (Color.cyan)) { // if it hasnt been selected
                    Window.buttons[row][col].setForeground(Color.cyan);
                    if (selection1 != null && board[selection1.x][selection1.y].canMove() != null) { // if you havent
                                                                                                     // selected
                                                                                                     // anything at all
                        for (Point p : board[selection1.x][selection1.y].canMove()) {
                            if ((p.x + p.y) % 2 == 0)
                                Window.buttons[p.x][p.y].setBackground(Color.white);
                            else
                                Window.buttons[p.x][p.y].setBackground(Color.gray);
                            Window.buttons[p.x][p.y].setEnabled(true);
                        }
                        Window.buttons[selection1.x][selection1.y].setForeground(null);
                    }
                    selection1 = new Point(row, col);
                } else { // if you click a piece but it has already been selected
                    Window.buttons[row][col].setForeground(null);
                    for (Point p : board[selection1.x][selection1.y].canMove()) {
                        if ((p.x + p.y) % 2 == 0)
                            Window.buttons[p.x][p.y].setBackground(Color.white);
                        else
                            Window.buttons[p.x][p.y].setBackground(Color.gray);
                        Window.buttons[p.x][p.y].setEnabled(true);
                    }
                    selection1 = null;
                }
            }
        }
    }

    public static class Window extends Canvas {
        /**
         * 
         */
        private static final long serialVersionUID = -7259108873705494293L;
        public static JButton[][] buttons = new JButton[8][8];
        public static JFrame frame;

        public Window(int width, int height, String title) {
            frame = new JFrame(title);

            frame.setPreferredSize(new Dimension(width, height));
            frame.setMaximumSize(new Dimension(width, height));
            frame.setMinimumSize(new Dimension(width, height));

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            addButtons();
            frame.setVisible(true);
            frame.requestFocus();
        }

        private void addButtons() {
            UIManager.put("Button.disabledText", Color.black);
            JPanel firstPanel = new JPanel();
            firstPanel.setBackground(Color.darkGray);
            frame.add(firstPanel);
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(8, 8));
            controlPanel.setBackground(Color.darkGray);
            controlPanel.setPreferredSize(new Dimension(768, 768));
            frame.setForeground(Color.darkGray);
            firstPanel.add(controlPanel);
            boolean white = false;
            for (int i = 0; i < 64; i++) {
                int row = i / 8;
                int col = i % 8;
                JButton test = new JButton("");
                test.setPreferredSize(new Dimension(128, 128));
                test.setBorder(BorderFactory.createEmptyBorder());
                test.setBorderPainted(false);
                test.setFocusable(false);
                test.setFont(new Font("TimesRoman", Font.PLAIN, 75));
                if (i % 8 == 0)
                    white = !white;
                if (white)
                    test.setBackground(Color.WHITE);
                else
                    test.setBackground(Color.GRAY);
                test.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // if (Board.board[row][col] != null && Board.board[row][col].color ==
                        // Board.turn)
                        // Board.select(row, col);
                        System.out.println(row + " " + col);
                        if (canMove) {
                            select(row, col);
                            updateBoard();
                        }

                    }
                });
                // if (i == 5) {
                // test.setDisabledSelectedIcon(null);
                // test.setDisabledIcon(null);
                // test.setEnabled(false);
                // }
                controlPanel.add(test);
                buttons[row][col] = test;
                white = !white;
            }

        }
    }
}