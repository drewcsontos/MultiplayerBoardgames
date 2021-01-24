package BoardGame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class BoardGameServer {

    static Piece[][] board = new Piece[8][8];

    static Player currentPlayer;

    // Instantiates Pieces and accepts clients
    public static void main(String[] args) throws Exception {
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 2; i++) {
                board[i][j] = new Piece(i, j, false, board);
            }
            for (int i = 6; i < 8; i++) {
                board[i][j] = new Piece(i, j, true, board);
            }
        }
        try (var listener = new ServerSocket(59090)) {
            System.out.println("Running Board Game Server...");
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Player(listener.accept(), "Player1"));
                pool.execute(new Player(listener.accept(), "Player2"));
            }
        }
    }

    // need to do, checks whether the player has won
    public static boolean hasWon() {
        return false;
    }

    // changes the board by deleting piece from original position and setting it to
    // new position
    public static void changeBoard(int startX, int startY, int endX, int endY) {
        board[endX][endY] = new Piece(endX, endY, board[startX][startY].isWhite, board);
        board[startX][startY] = null;
    }

    // prints out board, uses unicode characters for pieces
    public static String printBoard() {
        String b = "";
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == null) {
                    b += " ";
                } else {
                    if (board[i][j].isWhite)
                        b += "\u2659";
                    else
                        b += "\u265F";
                }
            }
            b += "n";
        }
        return b;
    }

    // checks whether the input is written correctly, originally used for terminal
    // version
    public static boolean isAllowedInput(String strNum) {
        if (strNum.length() == 4) {
            try {
                Integer.parseInt(strNum);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    // Player class which holds the client socket, the opponent, and the type (black
    // or white)
    private static class Player implements Runnable {
        private Socket socket;
        private String type;
        private Player opponent;
        private Scanner in;
        private PrintWriter out;

        Player(Socket socket, String type) throws IOException {
            this.socket = socket;
            this.type = type;
            in = new Scanner(this.socket.getInputStream());
            out = new PrintWriter(this.socket.getOutputStream(), true);
            if (this.type == "Player1") {
                currentPlayer = this;
                out.println("Waiting for opponent to join.");
            } else {
                out.println("Waiting for opponent to move");
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.out.println("Your Turn");
                opponent.out.println(printBoard());
            }
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try {
                while (in.hasNextLine()) {
                    String selection = in.nextLine();
                    if (opponent == null)
                        out.println("The other player has not joined!");
                    else if (this.type != currentPlayer.type)
                        out.println("It's not your turn yet!");
                    else if (isAllowedInput(selection)) { // input is 4 numbers, a start row and col, as well as an end
                                                          // row and col
                        changeBoard(Character.getNumericValue(selection.charAt(0)),
                                Character.getNumericValue(selection.charAt(1)),
                                Character.getNumericValue(selection.charAt(2)),
                                Character.getNumericValue(selection.charAt(3)));
                        System.out.println("sending board");
                        out.println(printBoard());
                        if (hasWon()) {
                            out.println("You won!");
                            opponent.out.println(printBoard());
                            opponent.out.println("You lost! Better luck next time.");
                            opponent.socket.close();
                            this.socket.close();
                            System.exit(0);
                        } else {
                            out.println("You just moved. Waiting for opponents move.");
                            currentPlayer = opponent;
                            opponent.out.println("Your Turn");
                            opponent.out.println(printBoard());
                        }
                    } else
                        out.println("Type a valid selection.");
                }
            } catch (Exception e) {
                System.out.print(e);
                System.out.println("Error:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                System.out.println("Closed: " + socket);
            }
        }
    }
}