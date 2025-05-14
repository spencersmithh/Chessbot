package solutions;

import com.github.bhlangonijr.chesslib.*;
import com.github.bhlangonijr.chesslib.move.Move;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ChessMinimax {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Side userSide = null;
        Board board = new Board();
        boolean aiFlag = false;

        System.out.println("Are you playing as white or black? (1=White, 2=Black, 3=White(load board)");
        int userPieceChoice = Integer.parseInt(scanner.nextLine());
        if (userPieceChoice == 1) {
            userSide = Side.WHITE;
        } else if (userPieceChoice == 2) {
            userSide = Side.BLACK;
        } else if (userPieceChoice == 3) {
            // load puzzle from fen notation
            String fen = "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQKBNR w KQkq - 0 1";
            board.loadFromFen(fen);
            userSide = Side.WHITE;
//            aiFlag = true;
        }else {
            System.out.println("Error, Invalid choice when choosing piece color.");
            return;
        }

        System.out.println("Initial Board:");
        System.out.println(board);


        while (!board.isMated() && !board.isDraw()) {
            if ((board.getSideToMove() != userSide) || aiFlag) {

                Move aiMove = minimax(board, 4, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

                if (validateMove(board, aiMove)) {
                    System.out.println("Making AI move: " + aiMove);
                    board.doMove(aiMove);
                } else {
                    System.out.println("Error with AI move validation");
                    return;
                }

                int eval = evaluateMove(board, aiMove);
                System.out.println("Evaluation of AI move: " + eval);

            } else {
                System.out.println("Enter your move in SAN notation (e.g., e2e4):");
                String sanMove = scanner.nextLine();

                try {
                    Move move = new Move(sanMove, board.getSideToMove());

                    if (!validateMove(board, move)) {
                        System.out.println("Invalid move! Try again.");
                        continue;
                    }
                    board.doMove(move);

                    int userEval = evaluateMove(board, move);
                    System.out.println("Evaluation of user move: " + userEval);
                } catch (Exception e) {
                    System.out.println("Invalid move! Try again.");
                    continue;
                }
            }

            System.out.println("Board after " + (board.getSideToMove() != userSide ? "USER" : "AI") + " move:");
            System.out.println(board);
        }
        scanner.close();

        if (board.isMated()) {
            System.out.println("Checkmate! Game over.");
        } else if (board.isDraw()) {
            System.out.println("Game ended in a draw.");
        }
    }

    public static boolean validateMove(Board board, Move move) {
        if (move == null) {
            System.out.println("Error: Move is null.");
            return false;
        }
        if (move.getFrom() == null || move.getTo() == null) {
            System.out.println("Error: From or To squares are null in the move.");
            return false;
        }
        if (!board.isMoveLegal(move, true)) {
            System.out.println("Error: Move is not legal on the board.");
            return false;
        }
        return true;
    }

    public static int evaluateMove(Board board, Move move) {
        int evaluation = 0;

        evaluation += evaluatePieceValues(board);
        evaluation += prioritizeCenter(board, move);
        evaluation += evaluateCapture(board, move);

//        if (board.isCapture(move)) {
//            evaluation += 200; // Add value for captures
//        }


        // Check if the move is a check
//        if (board.isKingAttacked()) {
//            evaluation += 300; // Add value for checks
//        }
//
//        // is the move a king attack
//        if (isThreatMove(board, move)) {
//            evaluation += 100; // Add value for threats
//        }

        // Check if the move is a castling move
//        if (board.isCastling(move)) {
//            evaluation += 400; // Add value for castling
//        }

        return evaluation;
    }

    public static boolean isThreatMove(Board board, Move move) {
        Board copyBoard = board.clone();
        copyBoard.doMove(move);
        return copyBoard.isKingAttacked(); // Check if the opposite side's king is attacked
    }

    public static int evaluatePieceValues(Board board) {
        int whiteValue = 0;
        int blackValue = 0;

        // Evaluate pieces on the board
        for (Square square : Square.values()) {
            Piece piece = board.getPiece(square);
            if (piece != null) {
                int pieceValue = getValueOfPiece(piece);
                if (board.getSideToMove() == Side.WHITE) {
                    whiteValue += pieceValue;
                } else {
                    blackValue += pieceValue;
                }
            }
        }
        return Math.abs(whiteValue - blackValue);
//        if(board.getSideToMove() == Side.WHITE){
//            return whiteValue - blackValue;
//        } else {
//            return blackValue - whiteValue;
//        }
    }

    public static int getValueOfPiece(Piece piece) {
//        System.out.println("Get value of piece "+ piece);
        if (piece.getPieceType() == null) {
            return 0;
        }
        switch (piece.getPieceType()) {
            case PAWN:
                return 100;
            case KNIGHT:
                return 320;
            case BISHOP:
                return 330;
            case ROOK:
                return 500;
            case QUEEN:
                return 900;
            case KING:
                return 10000;
            default:
                return 0;
        }
    }

    public static int prioritizeCenter(Board board, Move move) {
        // Define the center squares
        Square[] centerSquares = {Square.D4, Square.E4, Square.D5, Square.E5};
        Square[] outerSquares = {Square.C3, Square.D3, Square.E3, Square.F3, Square.F4, Square.F5, Square.F6, Square.E6, Square.D6, Square.C6, Square.C5, Square.C4};

        // Check if the move lands on a center square
        if (move != null && move.getTo() != null && containsSquare(centerSquares, move.getTo())) {
            return 250;
        }
        if (move != null && move.getTo() != null && containsSquare(outerSquares, move.getTo())) {
            return 100;
        }

        return 0;
    }

    public static boolean containsSquare(Square[] squares, Square targetSquare) {
        for (Square square : squares) {
            if (square == targetSquare) {
                return true;
            }
        }
        return false;
    }

    public static int evaluateCapture(Board board, Move move) {
        if (move != null) {
            Square toSquare = move.getTo();
            Piece capturedPiece = board.getPiece(toSquare);

            // Check if the move results in a capture
            if (capturedPiece != null) {
                // Check if the capture is defended
                boolean isDefended = isDefendedCapture(board, move);

                // If the capture is defended, give it a higher value
                if (isDefended) {
                    return getValueOfPiece(capturedPiece) + getValueOfPiece(capturedPiece)/2;
                } else {
                    return getValueOfPiece(capturedPiece);
                }
            }
        }
        // If it's not a capture, return a default value
        return 0;
    }

    public static boolean isDefendedCapture(Board board, Move move) {
        Square toSquare = move.getTo();
        Piece capturedPiece = board.getPiece(toSquare);
        Side originalSideToMove = board.getSideToMove(); // Save the original side to move

        // set the side to move to the ai side
        board.setSideToMove(originalSideToMove.flip());

        // legal moves for ai side
        List<Move> opponentMoves = board.legalMoves();
        // reset the side to move
        board.setSideToMove(originalSideToMove);

        // Check if any opponent pieces are attacking the square after the move
        for (Move opponentMove : opponentMoves) {
            if (opponentMove.getTo() == toSquare && board.getPiece(opponentMove.getFrom()) != null) {
                return true; // The capture is defended
            }
        }

        return false; // The capture is not defended
    }

    public static Move minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0) {
//            System.out.println("Reached base case of minimax");
            // Return a dummy move or throw an exception
            return new Move(Square.NONE, Square.NONE, Piece.NONE); // Dummy move with invalid squares
        }
        if (board.isMated() || board.isDraw()) {
            // Return null if no legal moves or at terminal nodes
            return null;
        }

        List<Move> legalMoves = board.legalMoves();
        Collections.shuffle(legalMoves);
        Move bestMove = null;

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                if (validateMove(board, move)) {
                    board.doMove(move);
                    int eval = evaluateMove(board, minimax(board, depth - 1, alpha, beta, false));
                    board.undoMove();
                    if (eval > maxEval) {
                        maxEval = eval;
                        bestMove = move;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return bestMove;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                if (validateMove(board, move)) {
                    board.doMove(move);
                    int eval = evaluateMove(board, minimax(board, depth - 1, alpha, beta, true));
                    board.undoMove();
                    if (eval < minEval) {
                        minEval = eval;
                        bestMove = move;
                    }
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return bestMove;
        }
    }
}
