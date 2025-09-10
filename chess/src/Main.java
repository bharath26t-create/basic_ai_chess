import java.util.*;

abstract class Piece {
    String color; // "W" or "B"
    String name;  // P, R, N, B, Q, K

    Piece(String color, String name) {
        this.color = color;
        this.name = name;
    }

    abstract boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board);

    @Override
    public String toString() {
        return color + name;
    }
}

// ================= Pieces ===================
class Pawn extends Piece {
    Pawn(String color) { super(color, "P"); }

    @Override
    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        int dir = color.equals("W") ? -1 : 1;
        if (sy == ey && board[ex][ey] == null && ex == sx + dir) return true;
        if (Math.abs(ey - sy) == 1 && ex == sx + dir && board[ex][ey] != null) return true;
        return false;
    }
}

class Rook extends Piece {
    Rook(String color) { super(color, "R"); }

    @Override
    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        if (sx != ex && sy != ey) return false;
        int dx = Integer.compare(ex, sx), dy = Integer.compare(ey, sy);
        int x = sx + dx, y = sy + dy;
        while (x != ex || y != ey) {
            if (board[x][y] != null) return false;
            x += dx; y += dy;
        }
        return true;
    }
}

class Knight extends Piece {
    Knight(String color) { super(color, "N"); }

    @Override
    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        int dx = Math.abs(ex - sx), dy = Math.abs(ey - sy);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }
}

class Bishop extends Piece {
    Bishop(String color) { super(color, "B"); }

    @Override
    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        if (Math.abs(ex - sx) != Math.abs(ey - sy)) return false;
        int dx = Integer.compare(ex, sx), dy = Integer.compare(ey, sy);
        int x = sx + dx, y = sy + dy;
        while (x != ex && y != ey) {
            if (board[x][y] != null) return false;
            x += dx; y += dy;
        }
        return true;
    }
}

class Queen extends Piece {
    Queen(String color) { super(color, "Q"); }

    @Override
    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        return new Rook(color).isValidMove(sx, sy, ex, ey, board) ||
                new Bishop(color).isValidMove(sx, sy, ex, ey, board);
    }
}

class King extends Piece {
    King(String color) { super(color, "K"); }

    @Override
    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        return Math.abs(ex - sx) <= 1 && Math.abs(ey - sy) <= 1;
    }
}

// ================= Main Chess Game ===================
class ChessAI {
    private static Piece[][] board = new Piece[8][8];
    private static String turn = "W"; // White starts
    private static Random rand = new Random();

    public static void main(String[] args) {
        setupBoard();
        Scanner sc = new Scanner(System.in);

        while (true) {
            printBoard();

            if (turn.equals("W")) {
                System.out.print("Your move (e.g., A2 A3): ");
                String input = sc.nextLine().trim().toUpperCase();

                if (!input.matches("[A-H][1-8] [A-H][1-8]")) {
                    System.out.println("❌ Invalid format! Use like A2 A3.\n");
                    continue;
                }

                int sx = 8 - Character.getNumericValue(input.charAt(1));
                int sy = input.charAt(0) - 'A';
                int ex = 8 - Character.getNumericValue(input.charAt(4));
                int ey = input.charAt(3) - 'A';

                if (!tryMove(sx, sy, ex, ey)) {
                    System.out.println("❌ Invalid move, try again.\n");
                    continue;
                }
            } else {
                System.out.println("🤖 AI (Black) is thinking...");
                if (!makeAIMove()) {
                    System.out.println("AI has no moves. You win!");
                    break;
                }
            }

            if (isKingCaptured()) {
                printBoard();
                System.out.println((turn.equals("W") ? "Black (AI)" : "White (You)") + " wins!");
                break;
            }

            turn = turn.equals("W") ? "B" : "W";
        }
        sc.close();
    }

    private static boolean tryMove(int sx, int sy, int ex, int ey) {
        Piece piece = board[sx][sy];
        if (piece == null || !piece.color.equals(turn)) return false;
        if (piece.isValidMove(sx, sy, ex, ey, board)) {
            board[ex][ey] = piece;
            board[sx][sy] = null;
            return true;
        }
        return false;
    }

    // ================= Simple AI ===================
    private static boolean makeAIMove() {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board[i][j];
                if (p != null && p.color.equals("B")) {
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            if (p.isValidMove(i, j, x, y, board)) {
                                moves.add(new int[]{i, j, x, y});
                            }
                        }
                    }
                }
            }
        }

        if (moves.isEmpty()) return false;

        List<int[]> captures = new ArrayList<>();
        for (int[] m : moves) {
            if (board[m[2]][m[3]] != null) captures.add(m);
        }

        int[] move = captures.isEmpty() ? moves.get(rand.nextInt(moves.size())) :
                captures.get(rand.nextInt(captures.size()));

        System.out.println("🤖 AI moves: " + toChessNotation(move[0], move[1]) +
                " → " + toChessNotation(move[2], move[3]) + "\n");

        board[move[2]][move[3]] = board[move[0]][move[1]];
        board[move[0]][move[1]] = null;
        return true;
    }

    private static boolean isKingCaptured() {
        boolean whiteKing = false, blackKing = false;
        for (Piece[] row : board) {
            for (Piece p : row) {
                if (p instanceof King) {
                    if (p.color.equals("W")) whiteKing = true;
                    if (p.color.equals("B")) blackKing = true;
                }
            }
        }
        return !(whiteKing && blackKing);
    }

    private static void setupBoard() {
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("B");
            board[6][i] = new Pawn("W");
        }
        board[0][0] = new Rook("B"); board[0][7] = new Rook("B");
        board[7][0] = new Rook("W"); board[7][7] = new Rook("W");

        board[0][1] = new Knight("B"); board[0][6] = new Knight("B");
        board[7][1] = new Knight("W"); board[7][6] = new Knight("W");

        board[0][2] = new Bishop("B"); board[0][5] = new Bishop("B");
        board[7][2] = new Bishop("W"); board[7][5] = new Bishop("W");

        board[0][3] = new Queen("B"); board[7][3] = new Queen("W");

        board[0][4] = new King("B"); board[7][4] = new King("W");
    }

    private static void printBoard() {
        System.out.println("\n    A    B    C    D    E    F    G    H");
        for (int i = 0; i < 8; i++) {
            System.out.print((8 - i) + "  ");
            for (int j = 0; j < 8; j++) {
                System.out.printf("%-3s ", board[i][j] == null ? "--" : board[i][j]);
            }
            System.out.println(" " + (8 - i));
        }
        System.out.println("    A    B    C    D    E    F    G    H\n");
    }

    private static String toChessNotation(int x, int y) {
        return "" + (char) ('A' + y) + (8 - x);
    }
}
