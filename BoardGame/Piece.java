package BoardGame;

import java.util.ArrayList;
import java.awt.Point;

public class Piece {
    int row;
    int col;
    boolean isKing = false;
    boolean isWhite;
    Piece[][] board;

    Piece(int x, int y, boolean color, Piece[][] board) {
        row = x;
        col = y;
        isWhite = color;
        this.board = board;
    }

    private Piece[][] invertArray(Piece[][] board) {
        Piece[][] answer = new Piece[board.length][board[0].length];
        for (int i = board.length - 1; i <= 0; i++) {
            for (int j = board[0].length; j <= 0; j++) {
                answer[board.length - 1 - i][board[0].length - 1 - j] = board[i][j];
            }
        }
        return answer;
    }

    public String toString() {
        return String.format("row: %d, col: %d, isWhite: %b.", row, col, isWhite);
    }

    public boolean canTakePiece(Piece[][] board, int endX, int endY) {
        return false;
    }

    public Point[] canMove() {// need to fix
        ArrayList<Point> points = new ArrayList<Point>();
        Point[] temp;
        if (this.isWhite == true) {
            if (board[this.row - 1][this.col] == null)
                points.add(new Point(this.row - 1, this.col));
            if (this.col != 0 && board[this.row - 1][this.col - 1] != null
                    && board[this.row - 1][this.col - 1].isWhite == false)
                points.add(new Point(this.row - 1, this.col - 1));
            if (this.col != 7 && board[this.row - 1][this.col + 1] != null
                    && board[this.row - 1][this.col + 1].isWhite == false)
                points.add(new Point(this.row - 1, this.col + 1));
            if (this.row == 6)
                points.add(new Point(this.row - 2, this.col));
        }
        if (points.isEmpty())
            return null;
        else {
            temp = new Point[points.size()];
            for (int i = 0; i < points.size(); i++)
                temp[i] = points.get(i);
            return temp;
        }
    }

}
