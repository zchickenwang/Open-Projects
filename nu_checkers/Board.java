public class Board {

    private Piece[][] pieces; // array of pieces on the board
    private int N; // length of side
    private boolean fireTurn; // which player's turn
    private Piece fireSelect; // selected Fire piece
    private Piece waterSelect; // selected Water piece
    private boolean fireHasMoved;
    private boolean waterHasMoved;
    private int xSelect;
    private int ySelect;
    
    public Board (boolean shouldBeEmpty) {
    	this.N = 8;
    	this.fireTurn = true;
        this.fireSelect = null;
        this.waterSelect = null;
        this.fireHasMoved = false;
        this.waterHasMoved = false;
        this.xSelect = -13;
        this.ySelect = -13;
        this.pieces = new Piece[this.N][this.N];
        if (!shouldBeEmpty) {
        	this.defaultLayout();
        }
    }
   
   /* Tests if (x, y) is invalid coordinate */
   private boolean outOfBounds(int x, int y) {
	   return x>=this.N || x<0 || y>=this.N || y<0;
   }
   
   public Piece pieceAt(int x, int y) {
	   if (outOfBounds(x, y)) {
		   return null;
	   }
	   else return this.pieces[x][y];
   }
   
   public void place(Piece p, int x, int y) {
	   if (outOfBounds(x, y) || p == null) {
		   return;
	   }
	   Piece old = pieceAt(x, y);
	   if (old != null) {
		   remove(x, y);
	   }
	   this.pieces[x][y] = p;
   }
   
   public Piece remove(int x, int y) {
	   if (outOfBounds(x, y)) {
		   System.out.println("Coordinates ("+x+", "+y+") are out of bounds");
		   return null;
	   }
	   Piece old = pieceAt(x, y);
	   if (old == null) {
		   System.out.println("There is no piece at ("+x+", "+y+").");
		   return null;
	   }
	   this.pieces[x][y] = null;
	   return old;
   }
   
   public boolean canSelect(int x, int y) {
	   Piece p = pieceAt(x, y);
	   if (x == this.xSelect && y == this.ySelect) return false;
	   System.out.println("trying to select" + Integer.toString(x) + Integer.toString(y));
	   if (p != null) {
		   if ((p.isFire() && !this.fireTurn) || (!p.isFire() && this.fireTurn)) {
			   System.out.println("Wrong side");
			   return false;
		   }
		   // once move is made make sure fireTurn changes and selected goes back to null
		   if (this.fireTurn && !this.fireHasMoved && !p.hasCaptured() && p.isFire()) {
			   //this.fireSelect = p;
			   return true;
		   }
		   if (!this.fireTurn && !this.waterHasMoved && !p.hasCaptured() && !p.isFire()){
			   //this.waterSelect = p;
			   return true;
		   }
	   }
	   else {
		   if ((this.fireTurn && this.fireSelect == null) || !this.fireTurn && this.waterSelect == null) {
			   System.out.println("Haven't selected a piece yet");
			   return false;
		   }
		   if (this.fireTurn && this.fireSelect != null && !this.fireHasMoved) {
			   int xi = findX(this.fireSelect);
			   int yi = findY(this.fireSelect);
			   return validMove(xi, yi, x, y);
		   }
		   else if (!this.fireTurn && this.waterSelect != null && !this.waterHasMoved) { //removed && !FireHasMoved
			   int xi = findX(this.waterSelect);
			   int yi = findY(this.waterSelect);
			   return validMove(xi, yi, x, y);
		   }
	   }
	   System.out.println("Can't select");
	   return false;
   }
   
   private int findX(Piece p) {
	   Piece curr = null;
	   for (int i = 0; i < this.N; i++) {
		   for (int j = 0; j < this.N; j++) {
			   if ((i + j) % 2 == 0) {
				   curr = pieceAt(i, j);
				   if (curr == p) return i;
			   }
		   }
	   }
	   return -1;
   }
   
   private int findY(Piece p) {
	   Piece curr = null;
	   for (int i = 0; i < this.N; i++) {
		   for (int j = 0; j < this.N; j++) {
			   if ((i + j) % 2 == 0) {
				   curr = pieceAt(i, j);
				   if (curr == p) return j;
			   }
		   }
	   }
	   return -1;
   }
   
   private boolean validMove(int xi, int yi, int xf, int yf) {
	   Piece p = pieceAt(xi, yi);
	   if (p == null) {
		   System.out.println("No piece here");
		   return false;
	   }
	   boolean fire = p.isFire();
	   boolean x1 = false;
	   boolean x2 = false;
	   boolean y1 = false;
	   boolean y2 = false;
	   int xDis = Math.abs(xi - xf);
	   int yDis = Math.abs(yi - yf);
	   
	   // test for x
	   if (true) {//if ((xi + xf) % 2 == 1) {
		   // if simple maneouever
		   if (xDis == 1 && !p.hasCaptured()) {
			   x1 = true;
		   }
		   // if taking a piece
		   else if (xDis == 2) {
			   Piece mid = pieceAt((xi+xf)/2, (yi+yf)/2);
			   if (mid != null) {
				   if ((fire && !mid.isFire()) || (!fire && mid.isFire())) {
					   x2 = true;
				   }
			   }
		   }
	   }
	   
	   // tests for y
	   if (true) {//if ((yi + yf) % 2 == 1) {
		   // if simple maneouever
		   if (yDis == 1 && !p.hasCaptured()) {
			   if (p.isKing()) y1 = true;
			   else if (p.isFire() && yf > yi) y1 = true;
			   else if (!p.isFire() && yi > yf) y1 = true;
		   }
		   //if taking a piece
		   else if (yDis == 2) {
			   Piece mid = pieceAt((xi+xf)/2, (yi+yf)/2);
			   if (mid != null) {
				   if ((fire && !mid.isFire()) || (!fire && mid.isFire())) {
					   if (p.isKing()) y2 = true;
					   else if (p.isFire() && yf > yi) y2 = true;
					   else if (!p.isFire() && yi > yf) y2 = true;
				   }
			   }
		   }
	   }
	   
	   if ((x1 && y1) || (x2 && y2)) {
		   return true;
	   }
	   else {
		   System.out.println("Can't select");
		   return false;
	   }
   }
   
   // assumes canSelect
   public void select(int x, int y) {
	   Piece p = pieceAt(x, y);
	   if (p != null) {//and not more than one
		   if (this.fireTurn && !this.fireHasMoved && !p.hasCaptured() && p.isFire()) {//for double takes, must not allow change of object
			   this.fireSelect = p; //cant change fireSelect!
			   return;
		   }
		   if (!this.fireTurn && !this.waterHasMoved && !p.hasCaptured() && !p.isFire()) {
			   this.waterSelect = p;
			   return;
		   }
	   }
	   else {
		   int xc;
		   int yc;
		   int xDis;
		   int yDis;
		   if (this.fireTurn && this.fireSelect != null) {
			   xc = findX(this.fireSelect);
			   yc = findY(this.fireSelect);
			   xDis = Math.abs(xc-x);
			   yDis = Math.abs(yc-y);
			   if (xDis == 1 && yDis == 1) {
				   this.fireHasMoved = true;
			   }
			   if (this.fireSelect.isBomb() && xDis == 2 && yDis == 2) {
				   this.fireHasMoved = true;
			   }
			   this.fireSelect.move(x, y);
			   return;
		   }
		   if (!this.fireTurn && this.waterSelect != null) {
			   xc = findX(this.waterSelect);
			   yc = findY(this.waterSelect);
			   xDis = Math.abs(xc-x);
			   yDis = Math.abs(yc-y);
			   if (xDis == 1 && yDis == 1) {
				   this.waterHasMoved = true;
			   }
			   if (this.waterSelect.isBomb() && xDis == 2 && yDis == 2) {
				   this.waterHasMoved = true;
			   }
			   this.waterSelect.move(x, y);
			   return;//neccessary?
		   }
	   }
   }
   
   private String getImage(Piece p) {
	   boolean fire = p.isFire();
	   boolean king = p.isKing();
	   if (fire && king && p.isBomb()) return "img/bomb-fire-crowned.png";
	   if (fire && king && p.isShield()) return "img/shield-fire-crowned.png";
	   if (fire && king) return "img/pawn-fire-crowned.png";
	   if (fire && p.isBomb()) return "img/bomb-fire.png";
	   if (fire && p.isShield()) return "img/shield-fire.png";
	   if (fire) return "img/pawn-fire.png";
	   if (!fire && king && p.isBomb()) return "img/bomb-water-crowned.png";
	   if (!fire && king && p.isShield()) return "img/shield-water-crowned.png";
	   if (!fire && king) return "img/pawn-water-crowned.png";
	   if (!fire && p.isBomb()) return "img/bomb-water.png";
	   if (!fire && p.isShield()) return "img/shield-water.png";
	   if (!fire) return "img/pawn-water.png";
	   else {
		   System.out.println("Cannot find image file for this piece");
		   return null;
	   }
   }
   
   private void drawBoard() {
        for (int i = 0; i < this.N; i++) {
            for (int j = 0; j < this.N; j++) {
            	if (i == this.xSelect && j == this.ySelect) {
            		StdDrawPlus.setPenColor(StdDrawPlus.WHITE);
            	}            	
            	else if ((i + j) % 2 == 0) {
                	StdDrawPlus.setPenColor(StdDrawPlus.GRAY);
                }
                else {
                	StdDrawPlus.setPenColor(StdDrawPlus.RED);
                }
                StdDrawPlus.filledSquare(i + .5, j + .5, .5);
                StdDrawPlus.setPenColor(StdDrawPlus.WHITE);
                Piece curr = pieceAt(i, j);
                if (curr != null) {
                	StdDrawPlus.picture(i + .5, j + .5, getImage(curr), 1, 1);
                }
            }
        }
        
    }
    
    // assert N > 8
    private void defaultLayout() {
    	for (int i = 0; i < this.N; i++) {
            for (int j = 0; j < this.N; j++) {
            	if (j == 0 && i % 2 == 0) {
            		this.place(new Piece(true, this, i, j, "pawn"), i, j);
            	}
            	else if (j == 1 && i % 2 == 1) {
            		this.place(new Piece(true, this, i, j, "shield"), i, j);
            	}
            	else if (j == 2 && i % 2 == 0) {
            		this.place(new Piece(true, this, i, j, "bomb"), i, j);
            	}
            	else if (j == this.N-1 && i % 2 == 1) {
            		this.place(new Piece(false, this, i, j, "pawn"), i, j);
            	}
            	else if (j == this.N-2 && i % 2 == 0) {
            		this.place(new Piece(false, this, i, j, "shield"), i, j);
            	}
            	else if (j == this.N-3 && i % 2 == 1) {
            		this.place(new Piece(false, this, i, j, "bomb"), i, j);
            	}
            }
        }
    }
    
    // ENSURE THAT SELECTED BECOMES NULL ONLY WHEN TURN ENDS
    public boolean canEndTurn() {
    	if (this.fireTurn) {
    		if (this.fireHasMoved) return true;
    		else if (this.fireSelect != null && this.fireSelect.hasCaptured()) return true; 
    	}
    	else {
    		if (this.waterHasMoved) return true;
    		else if (this.waterSelect != null && this.waterSelect.hasCaptured()) return true;
    	}
    	return false;
    }
    
    public void endTurn() {
    	if(this.fireSelect != null) {
    		this.fireSelect.doneCapturing();
        	this.fireSelect = null;
    	}
    	if(this.waterSelect != null) {
    		this.waterSelect.doneCapturing();
        	this.waterSelect = null;
    	}
    	this.fireHasMoved = false;
    	this.waterHasMoved = false;
    	this.fireTurn = !this.fireTurn;
    	this.xSelect = -13;
    	this.ySelect = -13;
    }
    
    public String winner() {
    	int fireCount = 0;
    	int waterCount = 0;
    	Piece curr = null;
    	for (int i = 0; i < this.N; i++) {
    		for (int j = 0; j < this.N; j++) {
    			if ((i + j) % 2 == 0) {
    				curr = this.pieceAt(i, j);
    				if (curr != null) {
    					if (curr.isFire()) fireCount++;
    					else waterCount++;
    				}
    			}
    		}
    	}
    	if (fireCount == 0 && waterCount == 0) return "No one";
    	else if (fireCount == 0) return "Water";
    	else if (waterCount == 0) return "Fire";
    	else return null;
    }

    public static void main(String[] args) {
        Board game = new Board(false);
        StdDrawPlus.setXscale(0, game.N);
        StdDrawPlus.setYscale(0, game.N);
        game.drawBoard();
        while(game.winner() == null) {
        	//game.drawBoard();
        	if(StdDrawPlus.mousePressed()) {
        		int a = (int) StdDrawPlus.mouseX();
        		int b = (int) StdDrawPlus.mouseY();
        		if (game.canSelect(a, b)) {
        			game.xSelect = a;
        			game.ySelect = b;
        			game.select(a, b);
        		}
        	}
        	if(StdDrawPlus.isSpacePressed()) {
        		if(game.canEndTurn()) {
        			game.endTurn();
        		}
        	}
        	game.drawBoard();
        	StdDrawPlus.show(99);
        }
        //game.drawBoard();
        System.out.println(game.winner());
    }
}