import java.util.*;

abstract class Piece {
    String color;
    String name;

    Piece(String color, String name) {
        this.color = color;
        this.name = name;
    }

    abstract boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board);

    public String toString() {
        return color + name;
    }
}

// ================= Pieces ===================
class Pawn extends Piece {
    Pawn(String c) { super(c, "P"); }

    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        int dir = color.equals("W") ? -1 : 1;
        if (sy == ey && board[ex][ey] == null && ex == sx + dir) return true;
        if (Math.abs(ey - sy) == 1 && ex == sx + dir && board[ex][ey] != null) return true;
        return false;
    }
}

class Rook extends Piece {
    Rook(String c) { super(c, "R"); }

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
    Knight(String c) { super(c, "N"); }

    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        int dx = Math.abs(ex - sx), dy = Math.abs(ey - sy);
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }
}

class Bishop extends Piece {
    Bishop(String c) { super(c, "B"); }

    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        if (Math.abs(ex - sx) != Math.abs(ey - sy)) return false;
        int dx = Integer.compare(ex, sx), dy = Integer.compare(ey, sy);
        int x = sx + dx, y = sy + dy;
        while (x != ex) {
            if (board[x][y] != null) return false;
            x += dx; y += dy;
        }
        return true;
    }
}

class Queen extends Piece {
    Queen(String c) { super(c, "Q"); }

    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        return new Rook(color).isValidMove(sx, sy, ex, ey, board) ||
               new Bishop(color).isValidMove(sx, sy, ex, ey, board);
    }
}

class King extends Piece {
    King(String c) { super(c, "K"); }

    boolean isValidMove(int sx, int sy, int ex, int ey, Piece[][] board) {
        return Math.abs(ex - sx) <= 1 && Math.abs(ey - sy) <= 1;
    }
}

// ================= Chess Game ===================
public class ChessAI {

    private static Piece[][] board = new Piece[8][8];
    private static String turn = "W";

    public static void main(String[] args) {

        setupBoard();
        Scanner sc = new Scanner(System.in);

        while (true) {

            printBoard();

            if (turn.equals("W")) {
                System.out.print("Your move (A2 A3): ");
                String in = sc.nextLine().toUpperCase();

                int sx = 8 - Character.getNumericValue(in.charAt(1));
                int sy = in.charAt(0) - 'A';
                int ex = 8 - Character.getNumericValue(in.charAt(4));
                int ey = in.charAt(3) - 'A';

                if (!tryMove(sx, sy, ex, ey)) continue;

            } else {
                System.out.println("AI thinking...");
                makeAIMove();
            }

            turn = turn.equals("W") ? "B" : "W";
        }
    }

    // ================= Move ===================
    static boolean tryMove(int sx,int sy,int ex,int ey){
        Piece p = board[sx][sy];
        if(p==null || !p.color.equals(turn)) return false;
        if(board[ex][ey]!=null && board[ex][ey].color.equals(turn)) return false;

        if(p.isValidMove(sx,sy,ex,ey,board)){
            board[ex][ey]=p;
            board[sx][sy]=null;
            return true;
        }
        return false;
    }

    // ================= MINIMAX AI ===================
    static boolean makeAIMove(){

        int bestScore=Integer.MIN_VALUE;
        int[] bestMove=null;

        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                Piece p=board[i][j];
                if(p!=null && p.color.equals("B")){
                    for(int x=0;x<8;x++){
                        for(int y=0;y<8;y++){
                            if(p.isValidMove(i,j,x,y,board)){

                                Piece cap=board[x][y];
                                board[x][y]=p;
                                board[i][j]=null;

                                int score=minimax(2,false);

                                board[i][j]=p;
                                board[x][y]=cap;

                                if(score>bestScore){
                                    bestScore=score;
                                    bestMove=new int[]{i,j,x,y};
                                }
                            }
                        }
                    }
                }
            }
        }

        board[bestMove[2]][bestMove[3]]=board[bestMove[0]][bestMove[1]];
        board[bestMove[0]][bestMove[1]]=null;

        System.out.println("AI moved.\n");
        return true;
    }

    static int minimax(int depth, boolean max){
        if(depth==0) return evaluate();

        int best=max?Integer.MIN_VALUE:Integer.MAX_VALUE;

        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                Piece p=board[i][j];
                if(p!=null && ((max && p.color.equals("B"))||(!max && p.color.equals("W")))){
                    for(int x=0;x<8;x++){
                        for(int y=0;y<8;y++){
                            if(p.isValidMove(i,j,x,y,board)){
                                Piece cap=board[x][y];
                                board[x][y]=p;
                                board[i][j]=null;

                                int val=minimax(depth-1,!max);

                                board[i][j]=p;
                                board[x][y]=cap;

                                best=max?Math.max(best,val):Math.min(best,val);
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    static int evaluate(){
        int score=0;
        for(Piece[] r:board){
            for(Piece p:r){
                if(p!=null){
                    int v=switch(p.name){
                        case "P"->1;
                        case "N","B"->3;
                        case "R"->5;
                        case "Q"->9;
                        case "K"->100;
                        default->0;
                    };
                    score+=p.color.equals("B")?v:-v;
                }
            }
        }
        return score;
    }

    // ================= Board ===================
    static void setupBoard(){
        for(int i=0;i<8;i++){ board[1][i]=new Pawn("B"); board[6][i]=new Pawn("W"); }
        board[0][0]=new Rook("B"); board[0][7]=new Rook("B");
        board[7][0]=new Rook("W"); board[7][7]=new Rook("W");
        board[0][1]=new Knight("B"); board[0][6]=new Knight("B");
        board[7][1]=new Knight("W"); board[7][6]=new Knight("W");
        board[0][2]=new Bishop("B"); board[0][5]=new Bishop("B");
        board[7][2]=new Bishop("W"); board[7][5]=new Bishop("W");
        board[0][3]=new Queen("B"); board[7][3]=new Queen("W");
        board[0][4]=new King("B"); board[7][4]=new King("W");
    }

    static void printBoard(){
        System.out.println("\n   A  B  C  D  E  F  G  H");
        for(int i=0;i<8;i++){
            System.out.print((8-i)+" ");
            for(int j=0;j<8;j++)
                System.out.print((board[i][j]==null?"--":board[i][j])+" ");
            System.out.println();
        }
    }
}
