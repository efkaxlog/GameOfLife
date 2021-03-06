package gameoflife;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author xlog
 */
public class GameOfLife extends Application {
    
    int cellsNumber = 40; // number of cells in rows and columns
    int cellPadding = 2; // empty space between cells
    int windowSize = 500 + cellPadding;
    int cellSize = calculateCellSize(); // fit cells to window size
    
    int totalSize = windowSize + cellsNumber * cellPadding;
    Pane root = new Pane();
    
    //must add this handler here populateCells() needs to have it defined
    EventHandler<MouseEvent> cellClickHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            Cell c = (Cell) e.getSource();
            if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
                c.toggleAlive();
            } else if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                c.drawBorder();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
                c.deleteBorder();
            }
        }
        
    };

    Cell[][] cells = populateCells(); // get first set of cells and draw
    Scene scene = new Scene(root, totalSize, totalSize);
    boolean atRunning = false;
    
    
    
    EventHandler<MouseEvent> mouseDragHandler= new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			 for (int y=0; y < cellsNumber; y++) {
		            for (int x=0; x < cellsNumber; x++) {
		            	Cell c = cells[y][x];
		            	double xPos = c.getX();
		            	double yPos = c.getY();
		            	double w = c.getWidth();
		            	double h = c.getHeight();
		            	// C as in coordinate
		            	double xC = e.getX();
		            	double yC = e.getY();
		            	if ((xC >= xPos && xC <= xPos + w) && (yC >= yPos && yC <= yPos + h)) {
		            		c.makeAlive();
		            	}
		            }
			 }	
		}};
    
    EventHandler keyHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent keyEvent) {
        	KeyCode key = keyEvent.getCode();
            if (key != null) switch (key) {
                case RIGHT:
                    drawNewSet(); // one iteration forward
                    break;
                case SPACE:
                    if (atRunning) {
                        at.stop();
                        atRunning = false;
                    } else {
                        atRunning = true;
                        at.start();
                    }   break;
                case C:
                    killAllCells();
                    break;
                case R:
                    generateRandomCells();
                    break;
                default:
                    break;
            }
            
    }};
    
    AnimationTimer at = new AnimationTimer() {
        @Override
        public void handle(long now) {
            drawNewSet();
        }
    };
    
    /**
     * generates random cells and draws them
     */
    public void generateRandomCells() {
    	for (int y=0; y < cellsNumber; y++) {
            for (int x=0; x < cellsNumber; x++) {
            	if (Math.random() < 0.5) { 
            		cells[y][x].makeAlive(); 
            	} else {
            		cells[y][x].makeDead();
            	}
            }
    	}
    }
 
    /**
     * gets the next cell generation and draws them
     */
    public void drawNewSet() {
        Cell[][] newCells = getNewCellSet(cells);
        for (int y=0; y < cellsNumber; y++) {
            for (int x=0; x < cellsNumber; x++) {
            	Cell c = cells[x][y];
            	c.alive = newCells[x][y].alive;
            	c.setFill(c.getColor());
            }
        }
    }
    
    /**
     * 
     * @param currentCells
     * @return next generation of cells
     */
    public Cell[][] getNewCellSet(Cell[][] currentCells) {
        Cell[][] newCells = new Cell[cellsNumber][cellsNumber];
        for(int y=0; y < cellsNumber; y++) {
            for (int x=0; x < cellsNumber; x++) {
                Cell c = currentCells[y][x];
                Cell newC = new Cell(c.getX(), c.getY(), cellSize, cellSize, c.xIndex, c.yIndex);
                newC.alive = getCellFate(c);
                newCells[y][x] = newC;
            }
        }
        return newCells;
    }
    
    public Cell getCell(int y, int x) {
        try {
            return cells[y][x];
        } catch (IndexOutOfBoundsException e) {
            // returns Cell that is not alive
            // as the x or y are out of bounds
            return new Cell();
            
        }     
    }
    
    public boolean getCellFate(Cell c) {
        int x = c.xIndex;
        int y = c.yIndex;
        int neighsAlive = 0;
        Cell[] neighbours = new Cell[8]; //   8 number surrounding cells
        neighbours[0] = getCell(y-1, x-1); // top left
        neighbours[1] = getCell(y-1, x); //   top
        neighbours[2] = getCell(y-1, x+1); // top right
        neighbours[3] = getCell(y, x-1); //   left
        neighbours[4] = getCell(y, x+1); //   right
        neighbours[5] = getCell(y+1, x-1); // bottom left
        neighbours[6] = getCell(y+1, x); //   bottom
        neighbours[7] = getCell(y+1, x+1); // bottom right
        
        for (int i=0; i < 8; i++) {
            if (neighbours[i].alive) {
                neighsAlive += 1;
            }  
        }
        
        if ((c.alive) && (neighsAlive == 2 || neighsAlive == 3)) {
            return true; // just right to live on
        } else if (c.alive && neighsAlive < 2) {
            return false; // underpopulation
        } else if (c.alive && neighsAlive > 3) {
            return false; // overpopulation
        } else if (!c.alive && neighsAlive == 3) {
            return true; // dead cell comes alive
            // IDE says the above if is reduntant, however
            // the game doesn't work properly without it
        } else {
            return false; // if none of the above apply the cell will be dead
        } 
    }
    
    public void killAllCells() {
    	for (int y=0; y < cellsNumber; y++) {
            for (int x=0; x < cellsNumber; x++) {
            	cells[y][x].makeDead();
            }
    	}
    }
    
    /**
     * populates cells[][] with first generation of Cells
     * and draws by adding to root. Adding to root should
     * only be done once - here.
     * @return 2d Cell array of Cells
     */
    public Cell[][] populateCells() {
        int xPos = 0 + cellPadding;
        int yPos = 0 + cellPadding;
        int size = cellSize;
        Cell[] row;
        Cell c;
        Cell[][] cellsArray = new Cell[cellsNumber][];
        for (int y=0; y < cellsNumber; y++) {
            row = new Cell[cellsNumber];
            for (int x=0; x < cellsNumber; x++) {
                c = new Cell(xPos, yPos, size, size, x, y);
                row[x] = c;
                xPos += size + cellPadding;
                c.setFill(c.getColor());
                c.addEventHandler(MouseEvent.ANY, cellClickHandler);
                root.getChildren().add(c);
                
            }
            xPos = 0 + cellPadding;
            cellsArray[y] = row;
            yPos += size + cellPadding;
        }
        
        return cellsArray;
    }
    
    /**
     * calculate a cell size to be used by all cells
     * @return 
     */
    public int calculateCellSize() {
        return (windowSize + cellPadding) / cellsNumber;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(scene);
        stage.setTitle("Game of Life");
        scene.setOnKeyPressed(keyHandler);        
        stage.show();
       // at.start();
        
        root.setOnMouseDragged(mouseDragHandler);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       launch(args);
    }  
}
