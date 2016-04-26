public class Piece {
	
	private boolean fire;
	private Board board;
	private int xc;
	private int yc;
	private String form;
	private boolean king;
	private boolean hasCapt;
	
	public Piece(boolean isFire, Board b, int x, int y, String type) {
		fire = isFire;
		board = b;
		xc = x;
		yc = y;
		form = type;
		king = false;
		hasCapt = false;
	}
	
	public boolean isFire() {
		return this.fire;
	}
	
	public int side() {
		if (this.fire) {
			return 0;
		}
		else return 1;
	}
	
	public boolean isKing() {
		return this.king;
	}
	
	public boolean isBomb() {
		return this.form == "bomb";
	}
	
	public boolean isShield() {
		return this.form == "shield";
	}
	
	// assumes the move to (X, Y) is valid!
	public void move(int x, int y) {
		Piece land = board.pieceAt(x, y);
		int xDis = Math.abs(this.xc-x);
		int yDis = Math.abs(this.yc-y);
		if (land == null) { //redundant?
			// Simple move
			if (xDis == 1 && yDis == 1) { //redundant?
				board.place(this, x, y);
				board.remove(this.xc, this.yc);
				this.xc = x;
				this.yc = y;
			}
			// A piece is taken
			else if (xDis == 2 && yDis == 2) {
				int midX = (int) (this.xc + x)/2;
				int midY = (int) (this.yc + y)/2;
				Piece mid = board.pieceAt(midX, midY);
				if (this.isBomb()) {
					for (int i = x-1; i < x+2; i++) {
						for (int j = y-1; j < y+2; j++) {
							Piece curr = board.pieceAt(i, j);
							if (curr != null && !curr.isShield()) {
								board.remove(i, j);
							}
						}
					}
					if (mid.isShield()) {
						board.remove(midX, midY);
					}
					board.remove(this.xc, this.yc); //neccessary?
				}
				else {
					System.out.println("Death");
					if (mid != null) {
						board.place(this, x, y);
						board.remove(midX, midY);
						board.remove(this.xc, this.yc);
						this.xc = x;
						this.yc = y;
					}
				}
				this.hasCapt = true;
			}
		}
		if ((this.fire && y == 7) || (!this.fire && y == 0)) {
			this.king = true;
		}
	}
	
	public boolean hasCaptured() {
		return this.hasCapt;
	}
	
	public void doneCapturing() {
		// what else
		this.hasCapt = false;
	}
}