/* Drew Schuster */
import java.awt.*;
import javax.swing.JPanel;
import java.lang.Math;
import java.util.*;
import java.io.*;


/* Both Player and Ghost inherit Mover.  Has generic functions relevant to both*/
abstract class Mover
{
  /* Framecount is used to count animation frames*/
  private int frameCount=0;

  /* State contains the game map */
  private boolean[][] state;

  /* gridSize is the size of one square in the game.
     max is the height/width of the game.
     increment is the speed at which the object moves,
     1 increment per move() call */
  private int gridSize;
  private int max;
  private int increment;

  /* Last location */
  private int lastX;
  private int lastY;

  /* Current location */
  private int x;
  private int y;

  /* Which pellet the pacman is on top of */
  private int pelletX;
  private int pelletY;
  /* Direction ghost is heading */
  private char direction;

  /* Generic constructor */
  public Mover()
  {
    gridSize=20;
    increment = 4;
    max = 400;
    state = new boolean[20][20];
    for(int i =0;i<20;i++)
    {
      for(int j=0;j<20;j++)
      {
        getState()[i][j] = false;
      }
    }
  }

  /* Updates the state information */
  public void updateState(boolean[][] state)
  {
    for(int i =0;i<20;i++)
    {
      for(int j=0;j<20;j++)
      {
        this.getState()[i][j] = state[i][j];
      }
    }
  }


  /* Determines if a set of coordinates is a valid destination.*/
  public boolean isValidDest(int x, int y)
  {
    /* The first statements check that the x and y are inbounds.  The last statement checks the map to
       see if it's a valid location */
    if ((((x)%20==0) || ((y)%20)==0) && 20<=x && x<400 && 20<= y && y<400 && getState()[x/20-1][y/20-1] )
    {
      return true;
    }
    return false;
  }

  /* This function is used for demoMode.  It is copied from the Ghost class.  See that for comments */
  public char newDirection()
  {
    int random;
    char backwards='U';
    int newX= getX(),newY= getY();
    int lookX= getX(),lookY= getY();
    Set<Character> set = new HashSet<Character>();
    switch(getDirection())
    {
      case 'L':
        backwards='R';
        break;
      case 'R':
        backwards='L';
        break;
      case 'U':
        backwards='D';
        break;
      case 'D':
        backwards='U';
        break;
    }
    char newDirection = backwards;
    while (newDirection == backwards || !isValidDest(lookX,lookY))
    {
      if (set.size()==3)
      {
        newDirection=backwards;
        break;
      }
      lookX= getX();
      lookY= getY();
      random = (int)(Math.random()*4) + 1;
      if (random == 1)
      {
        newDirection = 'L';
        lookX-= getIncrement();
      }
      else if (random == 2)
      {
        newDirection = 'R';
        lookX+= getGridSize();
      }
      else if (random == 3)
      {
        newDirection = 'U';
        lookY-= getIncrement();
      }
      else if (random == 4)
      {
        newDirection = 'D';
        lookY+= getGridSize();
      }
      if (newDirection != backwards)
      {
        set.add(new Character(newDirection));
      }
    }
    return newDirection;
  }


  /* Determines if the location is one where the ghost has to make a decision*/
  boolean isChoiceDest()
  {
    if (  getX() % getGridSize() ==0&& getY() % getGridSize() ==0 )
    {
      return true;
    }
    return false;
  }

  void incrementCoords(char desiredDirection)
  {
    switch(desiredDirection)
    {
      case 'L':
        if ( isValidDest(getX() - getIncrement(), getY()))
          setX(getX() - getIncrement());
        break;
      case 'R':
        if ( isValidDest(getX() + getGridSize(), getY()))
          setX(getX() + getIncrement());
        break;
      case 'U':
        if ( isValidDest(getX(), getY() - getIncrement()))
          setY(getY() - getIncrement());
        break;
      case 'D':
        if ( isValidDest(getX(), getY() + getGridSize()))
          setY(getY() + getIncrement());
        break;
    }
  }


  public abstract void updatePellet();

  public abstract void paint(Graphics g, Board b);

  public abstract void move();

  public int getLastX() {
    return lastX;
  }

  public void setLastX(int lastX) {
    this.lastX = lastX;
  }

  public int getLastY() {
    return lastY;
  }

  public void setLastY(int lastY) {
    this.lastY = lastY;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getPelletX() {
    return pelletX;
  }

  public void setPelletX(int pelletX) {
    this.pelletX = pelletX;
  }

  public int getPelletY() {
    return pelletY;
  }

  public void setPelletY(int pelletY) {
    this.pelletY = pelletY;
  }

  public char getDirection() {
    return direction;
  }

  public void setDirection(char direction) {
    this.direction = direction;
  }

  public int getFrameCount() {
    return frameCount;
  }

  public void setFrameCount(int frameCount) {
    this.frameCount = frameCount;
  }

  public boolean[][] getState() {
    return state;
  }

  public int getGridSize() {
    return gridSize;
  }

  public int getMax() {
    return max;
  }

  public int getIncrement() {
    return increment;
  }
}

/* This is the pacman object */
class Player extends Mover
{
  static Image pacmanImage = Toolkit.getDefaultToolkit().getImage("img/pacman.jpg");
  static Image pacmanUpImage = Toolkit.getDefaultToolkit().getImage("img/pacmanup.jpg");
  static Image pacmanDownImage = Toolkit.getDefaultToolkit().getImage("img/pacmandown.jpg");
  static Image pacmanLeftImage = Toolkit.getDefaultToolkit().getImage("img/pacmanleft.jpg");
  static Image pacmanRightImage = Toolkit.getDefaultToolkit().getImage("img/pacmanright.jpg");

  /* Direction is used in demoMode, currDirection and desiredDirection are used in non demoMode*/



  private char currDirection;
  private char desiredDirection;

  /* Dying is used to count frames in the dying animation.  If it's non-zero,
   pacman is in the process of dying */
  private int dying=0;
  private int numLives=2;
  private int lastPelletEatenX = 0;
  private int lastPelletEatenY=0;

  /* Keeps track of pellets eaten to determine end of game */
  private int pelletsEaten;


  /* teleport is true when travelling through the teleport tunnels*/
  private boolean teleport;
  
  /* Stopped is set when the pacman is not moving or has been killed */
  private boolean stopped = false;

  /* Constructor places pacman in initial location and orientation */
  public Player(int x, int y)
  {

    teleport=false;
    pelletsEaten=0;
    setPelletX(x/ getGridSize() -1);
    setPelletY(y/ getGridSize() -1);
    this.setLastX(x);
    this.setLastY(y);
    this.setX(x);
    this.setY(y);
    currDirection='L';
    desiredDirection='L';
  }


  /* This function is used for demoMode.  It is copied from the Ghost class.  See that for comments */
  public void demoMove()
  {
    setLastX(getX());
    setLastY(getY());
    if (isChoiceDest())
    {
      setDirection(newDirection());
    }
    switchDirection(getDirection(), getGridSize());
    currDirection = getDirection();
    setFrameCount(getFrameCount() + 1);
  }

  private void switchDirection(char direction, int gridSize) {
    switch(direction)
    {
      case 'L':
         if ( isValidDest(getX() - getIncrement(), getY()))
         {
           setX(getX() - getIncrement());
         }
         else if (getY() == 9* gridSize && getX() < 2 * gridSize)
         {
           setX(getMax() - gridSize *1);
           teleport = true;
         }
         break;
      case 'R':
         if ( isValidDest(getX() + gridSize, getY()))
         {
           setX(getX() + getIncrement());
         }
         else if (getY() == 9* gridSize && getX() > getMax() - gridSize *2)
         {
           setX(1* gridSize);
           teleport=true;
         }
         break;
      case 'U':
         if ( isValidDest(getX(), getY() - getIncrement()))
           setY(getY() - getIncrement());
         break;
      case 'D':
         if ( isValidDest(getX(), getY() + gridSize))
           setY(getY() + getIncrement());
         break;
    }
  }

  /* The move function moves the pacman for one frame in non demo mode */
  public void move()
  {
    int gridSize=20;
    setLastX(getX());
    setLastY(getY());
     
    /* Try to turn in the direction input by the user */
    /*Can only turn if we're in center of a grid*/
    if (getX() %20==0 && getY() %20==0 ||
       /* Or if we're reversing*/
       (getDesiredDirection() =='L' && getCurrDirection() =='R')  ||
       (getDesiredDirection() =='R' && getCurrDirection() =='L')  ||
       (getDesiredDirection() =='U' && getCurrDirection() =='D')  ||
       (getDesiredDirection() =='D' && getCurrDirection() =='U')
       )
    {
      incrementCoords(getDesiredDirection());
    }
    /* If we haven't moved, then move in the direction the pacman was headed anyway */
    if (getLastX() == getX() && getLastY() == getY())
    {
      switchDirection(getCurrDirection(), gridSize);
    }

    /* If we did change direction, update currDirection to reflect that */
    else
    {
      currDirection= getDesiredDirection();
    }
   
    /* If we didn't move at all, set the stopped flag */    
    if (getLastX() == getX() && getLastY() == getY())
      stopped=true;
  
    /* Otherwise, clear the stopped flag and increment the frameCount for animation purposes*/
    else
    {
      stopped=false;
      setFrameCount(getFrameCount() + 1);
    }
  }

  /* Update what pellet the pacman is on top of */
  public void updatePellet()
  {
    if (getX() % getGridSize() ==0 && getY() % getGridSize() == 0)
    {
    setPelletX(getX() / getGridSize() -1);
    setPelletY(getY() / getGridSize() -1);
    }
  }

  @Override
  public void paint(Graphics g, Board board) {
    /* If we're playing the dying animation, don't update the entire screen.
       Just kill the pacman*/
    if (getDying() > 0)
    {
      /* Stop any pacman eating sounds */
      board.getSounds().nomNomStop();

      /* Draw the pacman */
      g.drawImage(Player.pacmanImage, getX(), getY(),Color.BLACK,null);
      g.setColor(Color.BLACK);

      /* Kill the pacman */
      if (getDying() == 4)
        g.fillRect(getX(), getY(),20,7);
      else if ( getDying() == 3)
        g.fillRect(getX(), getY(),20,14);
      else if ( getDying() == 2)
        g.fillRect(getX(), getY(),20,20);
      else if ( getDying() == 1)
      {
        g.fillRect(getX(), getY(),20,20);
      }

      /* Take .1 seconds on each frame of death, and then take 2 seconds
         for the final frame to allow for the sound effect to end */
      long currTime = System.currentTimeMillis();
      long temp;
      if (getDying() != 1)
        temp = 100;
      else
        temp = 2000;
      /* If it's time to draw a new death frame... */
      if (currTime - board.getTimer() >= temp)
      {
        setDying(getDying() - 1);
        board.setTimer(currTime);
        /* If this was the last death frame...*/
        if (getDying() == 0)
        {
          if (getNumLives() ==-1)
          {
            /* Demo mode has infinite lives, just give it more lives*/
            if (board.isDemo())
              setNumLives(2);
            else
            {
              /* Game over for player.  If relevant, update high score.  Set gameOver flag*/
              if (board.getCurrScore() > board.getHighScore())
              {
                board.updateScore(board.getCurrScore());
              }
              board.setOverScreen(true);
            }
          }
        }
      }
      return;
    }
    /* Eat pellets */
    if ( board.getPellets()[getPelletX()][getPelletY()] && board.getNew() !=2 && board.getNew() !=3)
    {
      lastPelletEatenX = getPelletX();
      lastPelletEatenY = getPelletY();

      /* Play eating sound */
      board.getSounds().nomNom();

      /* Increment pellets eaten value to track for end game */
      pelletsEaten = getPelletsEaten() + 1;

      /* Delete the pellet*/
      board.getPellets()[getPelletX()][getPelletY()]=false;

      /* Increment the score */
      board.setCurrScore(board.getCurrScore() + 50);
      /* Update the screen to reflect the new score */
      g.setColor(Color.BLACK);
      g.fillRect(0,0,600,20);
      g.setColor(Color.YELLOW);
      g.setFont(board.getFont());
      if (board.isDemo())
        g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: "+ board.getHighScore(),20,10);
      else
        g.drawString("Score: "+(board.getCurrScore())+"\t High Score: "+ board.getHighScore(),20,10);

      /* If this was the last pellet */
      if (getPelletsEaten() == 173)
      {
        /*Demo mode can't get a high score */
        if (!board.isDemo())
        {
          if (board.getCurrScore() > board.getHighScore())
          {
            board.updateScore(board.getCurrScore());
          }
          board.setWinScreen(true);
        }
        else
        {
          board.setTitleScreen(true);
        }
        return;
      }
    }

    /* If we moved to a location without pellets, stop the sounds */
    else if ( (getPelletX() != getLastPelletEatenX() || getPelletY() != getLastPelletEatenY()) || isStopped())
    {
      /* Stop any pacman eating sounds */
      board.getSounds().nomNomStop();
    }
    /* Draw the pacman */
    if (getFrameCount() < 5)
    {
      /* Draw mouth closed */
      g.drawImage(Player.pacmanImage, getX(), getY(),Color.BLACK,null);
    }
    else
    {
      /* Draw mouth open in appropriate direction */
      if (getFrameCount() >=10)
        setFrameCount(0);

      switch(getCurrDirection())
      {
        case 'L':
          g.drawImage(Player.pacmanLeftImage, getX(), getY(),Color.BLACK,null);
          break;
        case 'R':
          g.drawImage(Player.pacmanRightImage, getX(), getY(),Color.BLACK,null);
          break;
        case 'U':
          g.drawImage(Player.pacmanUpImage, getX(), getY(),Color.BLACK,null);
          break;
        case 'D':
          g.drawImage(Player.pacmanDownImage, getX(), getY(),Color.BLACK,null);
          break;
      }
    }
  }

  public int getDying() {
    return dying;
  }

  public void setDying(int dying) {
    this.dying = dying;
  }

  public int getNumLives() {
    return numLives;
  }

  public void setNumLives(int numLives) {
    this.numLives = numLives;
  }

  public char getCurrDirection() {
    return currDirection;
  }

  public void setCurrDirection(char currDirection) {
    this.currDirection = currDirection;
  }

  public char getDesiredDirection() {
    return desiredDirection;
  }

  public void setDesiredDirection(char desiredDirection) {
    this.desiredDirection = desiredDirection;
  }

  public int getLastPelletEatenX() {
    return lastPelletEatenX;
  }

  public int getLastPelletEatenY() {
    return lastPelletEatenY;
  }

  public int getPelletsEaten() {
    return pelletsEaten;
  }

  public boolean isTeleport() {
    return teleport;
  }

  public void setTeleport(boolean teleport) {
    this.teleport = teleport;
  }

  public boolean isStopped() {
    return stopped;
  }
}

/* Ghost class controls the ghost. */
class Ghost extends Mover
{
  private static Image ghost10 = Toolkit.getDefaultToolkit().getImage("img/ghost10.jpg");
  private static Image ghost20 = Toolkit.getDefaultToolkit().getImage("img/ghost20.jpg");
  private static Image ghost30 = Toolkit.getDefaultToolkit().getImage("img/ghost30.jpg");
  private static Image ghost40 = Toolkit.getDefaultToolkit().getImage("img/ghost40.jpg");
  private static Image ghost11 = Toolkit.getDefaultToolkit().getImage("img/ghost11.jpg");
  private static Image ghost21 = Toolkit.getDefaultToolkit().getImage("img/ghost21.jpg");
  private static Image ghost31 = Toolkit.getDefaultToolkit().getImage("img/ghost31.jpg");
  private static Image ghost41 = Toolkit.getDefaultToolkit().getImage("img/ghost41.jpg");


  private Image image0;
  private Image image1;


  /* The pellet the ghost was last on top of */
  private int lastPelletX;
  private int lastPelletY;

  /*Constructor places ghost and updates states*/
  public Ghost(int x, int y, Image image0, Image image1)
  {
    setDirection('L');
    setPelletX(x/ getGridSize() -1);
    setPelletY(x/ getGridSize() -1);
    lastPelletX= getPelletX();
    lastPelletY= getPelletY();
    this.setLastX(x);
    this.setLastY(y);
    this.setX(x);
    this.setY(y);
    this.image0 = image0;
    this.image1 = image1;
  }

  public static Image getGhost10() {
    return ghost10;
  }

  public static Image getGhost20() {
    return ghost20;
  }

  public static Image getGhost30() {
    return ghost30;
  }

  public static Image getGhost40() {
    return ghost40;
  }

  public static Image getGhost11() {
    return ghost11;
  }

  public static Image getGhost21() {
    return ghost21;
  }

  public static Image getGhost31() {
    return ghost31;
  }

  public static Image getGhost41() {
    return ghost41;
  }

  /* update pellet status */
  public void updatePellet()
  {
    int tempX,tempY;
    tempX = getX() / getGridSize() -1;
    tempY = getY() / getGridSize() -1;
    if (tempX != getPelletX() || tempY != getPelletY())
    {
      lastPelletX = getPelletX();
      lastPelletY = getPelletY();
      setPelletX(tempX);
      setPelletY(tempY);
    }
     
  }


  /* Random move function for ghost */
  public void move()
  {
    setLastX(getX());
    setLastY(getY());

    /* If we can make a decision, pick a new direction randomly */
    if (isChoiceDest())
    {
      setDirection(newDirection());
    }

    /* If that direction is valid, move that way */
    incrementCoords(getDirection());
  }


    @Override
    public void paint(Graphics g, Board board) {
      /*Draw the ghosts */
      if (getFrameCount() < 5)
      {
        /* Draw first frame of ghosts */
        g.drawImage(getImage0(), getX(), getY(),Color.BLACK,null);

        setFrameCount(getFrameCount() + 1);
      }
      else
      {
        /* Draw second frame of ghosts */
        g.drawImage(getImage1(), getX(), getY(),Color.BLACK,null);

        if (getFrameCount() >=10)
          setFrameCount(0);
        else
          setFrameCount(getFrameCount() + 1);
      }
    }

  public Image getImage0() {
    return image0;
  }

  public Image getImage1() {
    return image1;
  }

  public int getLastPelletX() {
    return lastPelletX;
  }

  public int getLastPelletY() {
    return lastPelletY;
  }
}


/*This board class contains the player, ghosts, pellets, and most of the game logic.*/
public class Board extends JPanel
{
  /* Initialize the images*/
  /* For JAR File*/
  /*
  Image pacmanImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacman.jpg"));
  Image pacmanUpImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmanup.jpg")); 
  Image pacmanDownImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmandown.jpg")); 
  Image pacmanLeftImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmanleft.jpg")); 
  Image pacmanRightImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmanright.jpg")); 
  Image ghost10 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost10.jpg")); 
  Image ghost20 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost20.jpg")); 
  Image ghost30 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost30.jpg")); 
  Image ghost40 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost40.jpg")); 
  Image ghost11 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost11.jpg")); 
  Image ghost21 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost21.jpg")); 
  Image ghost31 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost31.jpg")); 
  Image ghost41 = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost41.jpg")); 
  Image titleScreenImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/titleScreen.jpg")); 
  Image gameOverImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/gameOver.jpg")); 
  Image winScreenImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/winScreen.jpg"));
  */
  /* For NOT JAR file*/


  private Image titleScreenImage = Toolkit.getDefaultToolkit().getImage("img/titleScreen.jpg");
  private Image gameOverImage = Toolkit.getDefaultToolkit().getImage("img/gameOver.jpg");
  private Image winScreenImage = Toolkit.getDefaultToolkit().getImage("img/winScreen.jpg");

  /* Initialize the player and ghosts */
  private Player player = new Player(200,300);
  private Ghost ghost1 = new Ghost(180,180, Ghost.getGhost10(), Ghost.getGhost11());
  private Ghost ghost2 = new Ghost(200,180, Ghost.getGhost20(), Ghost.getGhost21());
  private Ghost ghost3 = new Ghost(220,180, Ghost.getGhost30(), Ghost.getGhost31());
  private Ghost ghost4 = new Ghost(220,180, Ghost.getGhost40(), Ghost.getGhost41());

  /* Timer is used for playing sound effects and animations */
  private long timer = System.currentTimeMillis();

 
  /* Score information */
  private int currScore;
  private int highScore;

  /* if the high scores have been cleared, we have to update the top of the screen to reflect that */
  private boolean clearHighScores= false;

  /*Contains the game map, passed to player and ghosts */
  private boolean[][] state;

  /* Contains the state of all pellets*/
  private boolean[][] pellets;

  /* Game dimensions */
  private int gridSize;
  private int max;

  /* State flags*/
  private boolean stopped;
  private boolean titleScreen;
  private boolean winScreen = false;
  private boolean overScreen = false;
  private boolean demo = false;
  private int New;

  /* Used to call sound effects */
  private GameSounds sounds;

  /* This is the font used for the menus */
  private Font font = new Font("Monospaced",Font.BOLD, 12);

  /* Constructor initializes state flags etc.*/
  public Board() 
  {
    initHighScores();
    sounds = new GameSounds();
    currScore=0;
    stopped=false;
    max=400;
    gridSize=20;
    New=0;
    titleScreen = true;
  }

  /* Reads the high scores file and saves it */
  public void initHighScores()
  {
    File file = new File("highScores.txt");
    Scanner sc;
    try
    {
        sc = new Scanner(file);
        highScore = sc.nextInt();
        sc.close();
    }
    catch(Exception e)
    {
    }
  }

  /* Writes the new high score to a file and sets flag to update it on screen */
  public void updateScore(int score)
  {
    PrintWriter out;
    try
    {
      out = new PrintWriter("highScores.txt");
      out.println(score);
      out.close();
    }
    catch(Exception e)
    {
    }
    highScore=score;
    clearHighScores=true;
  }

  /* Wipes the high scores file and sets flag to update it on screen */
  public void clearHighScores()
  {
    PrintWriter out;
    try
    {
      out = new PrintWriter("highScores.txt");
      out.println("0");
      out.close();
    }
    catch(Exception e)
    {
    }
    highScore=0;
    clearHighScores=true;
  }

  /* Reset occurs on a new game*/
  public void reset()
  {
    getPlayer().setNumLives(2);
    state = new boolean[20][20];
    pellets = new boolean[20][20];

    /* Clear state and pellets arrays */
    for(int i=0;i<20;i++)
    {
      for(int j=0;j<20;j++)
      {
        getState()[i][j]=true;
        getPellets()[i][j]=true;
      }
    }

    /* Handle the weird spots with no pellets*/
    for(int i = 5;i<14;i++)
    {
      for(int j = 5;j<12;j++)
      {
        getPellets()[i][j]=false;
      }
    }
    getPellets()[9][7] = false;
    getPellets()[8][8] = false;
    getPellets()[9][8] = false;
    getPellets()[10][8] = false;

  }


  /* Function is called during drawing of the map.
     Whenever the a portion of the map is covered up with a barrier,
     the map and pellets arrays are updated accordingly to note
     that those are invalid locations to travel or put pellets
  */
  public void updateMap(int x,int y, int width, int height)
  {
    for (int i = x/ getGridSize(); i<x/ getGridSize() +width/ getGridSize(); i++)
    {
      for (int j = y/ getGridSize(); j<y/ getGridSize() +height/ getGridSize(); j++)
      {
        getState()[i-1][j-1]=false;
        getPellets()[i-1][j-1]=false;
      }
    }
  } 


  /* Draws the appropriate number of lives on the bottom left of the screen.
     Also draws the menu */
  public void drawLives(Graphics g)
  {
    g.setColor(Color.BLACK);

    /*Clear the bottom bar*/
    g.fillRect(0, getMax() +5,600, getGridSize());
    g.setColor(Color.YELLOW);
    for(int i = 0; i< getPlayer().getNumLives(); i++)
    {
      /*Draw each life */
      g.fillOval(getGridSize() *(i+1), getMax() +5, getGridSize(), getGridSize());
    }
    /* Draw the menu items */
    g.setColor(Color.YELLOW);
    g.setFont(getFont());
    g.drawString("Reset",100, getMax() +5+ getGridSize());
    g.drawString("Clear High Scores",180, getMax() +5+ getGridSize());
    g.drawString("Exit",350, getMax() +5+ getGridSize());
  }
  
  
  /*  This function draws the board.  The pacman board is really complicated and can only feasibly be done
      manually.  Whenever I draw a wall, I call updateMap to invalidate those coordinates.  This way the pacman
      and ghosts know that they can't traverse this area */ 
  public void drawBoard(Graphics g)
  {
        g.setColor(Color.BLACK);
        g.fillRect(0,0,600,600);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,420,420);
        
        g.setColor(Color.BLACK);
        g.fillRect(0,0,20,600);
        g.fillRect(0,0,600,20);
        g.setColor(Color.WHITE);
        g.drawRect(19,19,382,382);
        g.setColor(Color.BLUE);

        g.fillRect(40,40,60,20);
          updateMap(40,40,60,20);
        g.fillRect(120,40,60,20);
          updateMap(120,40,60,20);
        g.fillRect(200,20,20,40);
          updateMap(200,20,20,40);
        g.fillRect(240,40,60,20);
          updateMap(240,40,60,20);
        g.fillRect(320,40,60,20);
          updateMap(320,40,60,20);
        g.fillRect(40,80,60,20);
          updateMap(40,80,60,20);
        g.fillRect(160,80,100,20);
          updateMap(160,80,100,20);
        g.fillRect(200,80,20,60);
          updateMap(200,80,20,60);
        g.fillRect(320,80,60,20);
          updateMap(320,80,60,20);

        g.fillRect(20,120,80,60);
          updateMap(20,120,80,60);
        g.fillRect(320,120,80,60);
          updateMap(320,120,80,60);
        g.fillRect(20,200,80,60);
          updateMap(20,200,80,60);
        g.fillRect(320,200,80,60);
          updateMap(320,200,80,60);

        g.fillRect(160,160,40,20);
          updateMap(160,160,40,20);
        g.fillRect(220,160,40,20);
          updateMap(220,160,40,20);
        g.fillRect(160,180,20,20);
          updateMap(160,180,20,20);
        g.fillRect(160,200,100,20);
          updateMap(160,200,100,20);
        g.fillRect(240,180,20,20);
        updateMap(240,180,20,20);
        g.setColor(Color.BLUE);


        g.fillRect(120,120,60,20);
          updateMap(120,120,60,20);
        g.fillRect(120,80,20,100);
          updateMap(120,80,20,100);
        g.fillRect(280,80,20,100);
          updateMap(280,80,20,100);
        g.fillRect(240,120,60,20);
          updateMap(240,120,60,20);

        g.fillRect(280,200,20,60);
          updateMap(280,200,20,60);
        g.fillRect(120,200,20,60);
          updateMap(120,200,20,60);
        g.fillRect(160,240,100,20);
          updateMap(160,240,100,20);
        g.fillRect(200,260,20,40);
          updateMap(200,260,20,40);

        g.fillRect(120,280,60,20);
          updateMap(120,280,60,20);
        g.fillRect(240,280,60,20);
          updateMap(240,280,60,20);

        g.fillRect(40,280,60,20);
          updateMap(40,280,60,20);
        g.fillRect(80,280,20,60);
          updateMap(80,280,20,60);
        g.fillRect(320,280,60,20);
          updateMap(320,280,60,20);
        g.fillRect(320,280,20,60);
          updateMap(320,280,20,60);

        g.fillRect(20,320,40,20);
          updateMap(20,320,40,20);
        g.fillRect(360,320,40,20);
          updateMap(360,320,40,20);
        g.fillRect(160,320,100,20);
          updateMap(160,320,100,20);
        g.fillRect(200,320,20,60);
          updateMap(200,320,20,60);

        g.fillRect(40,360,140,20);
          updateMap(40,360,140,20);
        g.fillRect(240,360,140,20);
          updateMap(240,360,140,20);
        g.fillRect(280,320,20,40);
          updateMap(280,320,20,60);
        g.fillRect(120,320,20,60);
          updateMap(120,320,20,60);
        drawLives(g);
  } 


  /* Draws the pellets on the screen */
  public void drawPellets(Graphics g)
  {
        g.setColor(Color.YELLOW);
        for (int i=1;i<20;i++)
        {
          for (int j=1;j<20;j++)
          {
            if ( getPellets()[i-1][j-1])
            g.fillOval(i*20+8,j*20+8,4,4);
          }
        }
  }

  /* Draws one individual pellet.  Used to redraw pellets that ghosts have run over */
  public void fillPellet(int x, int y, Graphics g)
  {
    g.setColor(Color.YELLOW);
    g.fillOval(x*20+28,y*20+28,4,4);
  }

  /* This is the main function that draws one entire frame of the game */
  public void paint(Graphics g)
  {


    /* If this is the title screen, draw the title screen and return */
    if (isTitleScreen())
    {
      g.setColor(Color.BLACK);
      g.fillRect(0,0,600,600);
      g.drawImage(getTitleScreenImage(),0,0,Color.BLACK,null);

      /* Stop any pacman eating sounds */
      getSounds().nomNomStop();
      New = 1;
      return;
    } 

    /* If this is the win screen, draw the win screen and return */
    else if (isWinScreen())
    {
      g.setColor(Color.BLACK);
      g.fillRect(0,0,600,600);
      g.drawImage(getWinScreenImage(),0,0,Color.BLACK,null);
      New = 1;
      /* Stop any pacman eating sounds */
      getSounds().nomNomStop();
      return;
    }

    /* If this is the game over screen, draw the game over screen and return */
    else if (isOverScreen())
    {
      g.setColor(Color.BLACK);
      g.fillRect(0,0,600,600);
      g.drawImage(getGameOverImage(),0,0,Color.BLACK,null);
      New = 1;
      /* Stop any pacman eating sounds */
      getSounds().nomNomStop();
      return;
    }

    /* If need to update the high scores, redraw the top menu bar */
    if (isClearHighScores())
    {
      g.setColor(Color.BLACK);
      g.fillRect(0,0,600,18);
      g.setColor(Color.YELLOW);
      g.setFont(getFont());
      clearHighScores= false;
      if (isDemo())
        g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: "+ getHighScore(),20,10);
      else
        g.drawString("Score: "+(getCurrScore())+"\t High Score: "+ getHighScore(),20,10);
    }
   
    /* oops is set to true when pacman has lost a life */ 
    boolean oops=false;
    
    /* Game initialization */
    if (getNew() ==1)
    {
      reset();
      player = new Player(200,300);
      ghost1 = new Ghost(180,180, Ghost.getGhost10(), Ghost.getGhost11());
      ghost2 = new Ghost(200,180, Ghost.getGhost20(), Ghost.getGhost21());
      ghost3 = new Ghost(220,180, Ghost.getGhost30(), Ghost.getGhost31());
      ghost4 = new Ghost(220,180, Ghost.getGhost40(), Ghost.getGhost41());
      currScore = 0;
      drawBoard(g);
      drawPellets(g);
      drawLives(g);
      /* Send the game map to player and all ghosts */
      getPlayer().updateState(getState());
      /* Don't let the player go in the ghost box*/
      getPlayer().getState()[9][7]=false;
      getGhost1().updateState(getState());
      getGhost2().updateState(getState());
      getGhost3().updateState(getState());
      getGhost4().updateState(getState());
   
      /* Draw the top menu bar*/
      g.setColor(Color.YELLOW);
      g.setFont(getFont());
      if (isDemo())
        g.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: "+ getHighScore(),20,10);
      else
        g.drawString("Score: "+(getCurrScore())+"\t High Score: "+ getHighScore(),20,10);
      New = getNew() + 1;
    }
    /* Second frame of new game */
    else if (getNew() == 2)
    {
      New = getNew() + 1;
    }
    /* Third frame of new game */
    else if (getNew() == 3)
    {
      New = getNew() + 1;
      /* Play the newGame sound effect */
      getSounds().newGame();
      timer = System.currentTimeMillis();
      return;
    }
    /* Fourth frame of new game */
    else if (getNew() == 4)
    {
      /* Stay in this state until the sound effect is over */
      long currTime = System.currentTimeMillis();
      if (currTime - getTimer() >= 5000)
      {
        New=0;
      }
      else
        return;
    }

    /* Drawing optimization */
    g.copyArea(getPlayer().getX() -20, getPlayer().getY() -20,80,80,0,0);
    g.copyArea(getGhost1().getX() -20, getGhost1().getY() -20,80,80,0,0);
    g.copyArea(getGhost2().getX() -20, getGhost2().getY() -20,80,80,0,0);
    g.copyArea(getGhost3().getX() -20, getGhost3().getY() -20,80,80,0,0);
    g.copyArea(getGhost4().getX() -20, getGhost4().getY() -20,80,80,0,0);



    /* Detect collisions */
    if (getPlayer().getX() == getGhost1().getX() && Math.abs(getPlayer().getY() - getGhost1().getY()) < 10)
      oops=true;
    else if (getPlayer().getX() == getGhost2().getX() && Math.abs(getPlayer().getY() - getGhost2().getY()) < 10)
      oops=true;
    else if (getPlayer().getX() == getGhost3().getX() && Math.abs(getPlayer().getY() - getGhost3().getY()) < 10)
      oops=true;
    else if (getPlayer().getX() == getGhost4().getX() && Math.abs(getPlayer().getY() - getGhost4().getY()) < 10)
      oops=true;
    else if (getPlayer().getY() == getGhost1().getY() && Math.abs(getPlayer().getX() - getGhost1().getX()) < 10)
      oops=true;
    else if (getPlayer().getY() == getGhost2().getY() && Math.abs(getPlayer().getX() - getGhost2().getX()) < 10)
      oops=true;
    else if (getPlayer().getY() == getGhost3().getY() && Math.abs(getPlayer().getX() - getGhost3().getX()) < 10)
      oops=true;
    else if (getPlayer().getY() == getGhost4().getY() && Math.abs(getPlayer().getX() - getGhost4().getX()) < 10)
      oops=true;

    /* Kill the pacman */
    if (oops && !isStopped())
    {
      /* 4 frames of death*/
      getPlayer().setDying(4);
      
      /* Play death sound effect */
      getSounds().death();
      /* Stop any pacman eating sounds */
      getSounds().nomNomStop();

      /*Decrement lives, update screen to reflect that.  And set appropriate flags and timers */
      getPlayer().setNumLives(getPlayer().getNumLives() - 1);
      stopped=true;
      drawLives(g);
      timer = System.currentTimeMillis();
    }

    /* Delete the players and ghosts */
    g.setColor(Color.BLACK);
    g.fillRect(getPlayer().getLastX(), getPlayer().getLastY(),20,20);
    g.fillRect(getGhost1().getLastX(), getGhost1().getLastY(),20,20);
    g.fillRect(getGhost2().getLastX(), getGhost2().getLastY(),20,20);
    g.fillRect(getGhost3().getLastX(), getGhost3().getLastY(),20,20);
    g.fillRect(getGhost4().getLastX(), getGhost4().getLastY(),20,20);



    /* Replace pellets that have been run over by ghosts */
    if ( getPellets()[getGhost1().getLastPelletX()][getGhost1().getLastPelletY()])
      fillPellet(getGhost1().getLastPelletX(), getGhost1().getLastPelletY(),g);
    if ( getPellets()[getGhost2().getLastPelletX()][getGhost2().getLastPelletY()])
      fillPellet(getGhost2().getLastPelletX(), getGhost2().getLastPelletY(),g);
    if ( getPellets()[getGhost3().getLastPelletX()][getGhost3().getLastPelletY()])
      fillPellet(getGhost3().getLastPelletX(), getGhost3().getLastPelletY(),g);
    if ( getPellets()[getGhost4().getLastPelletX()][getGhost4().getLastPelletY()])
      fillPellet(getGhost4().getLastPelletX(), getGhost4().getLastPelletY(),g);

    getPlayer().paint(g, this);
    getGhost1().paint(g, this);
    getGhost2().paint(g, this);
    getGhost3().paint(g, this);
    getGhost4().paint(g, this);

    /* Draw the border around the game in case it was overwritten by ghost movement or something */
    g.setColor(Color.WHITE);
    g.drawRect(19,19,382,382);

  }

  public Image getTitleScreenImage() {
    return titleScreenImage;
  }

  public Image getGameOverImage() {
    return gameOverImage;
  }

  public Image getWinScreenImage() {
    return winScreenImage;
  }

  public Player getPlayer() {
    return player;
  }

  public Ghost getGhost1() {
    return ghost1;
  }

  public Ghost getGhost2() {
    return ghost2;
  }

  public Ghost getGhost3() {
    return ghost3;
  }

  public Ghost getGhost4() {
    return ghost4;
  }

  public long getTimer() {
    return timer;
  }

  public int getCurrScore() {
    return currScore;
  }

  public int getHighScore() {
    return highScore;
  }

  public boolean isClearHighScores() {
    return clearHighScores;
  }

  public boolean[][] getState() {
    return state;
  }

  public boolean[][] getPellets() {
    return pellets;
  }

  public int getGridSize() {
    return gridSize;
  }

  public int getMax() {
    return max;
  }

  public boolean isStopped() {
    return stopped;
  }

  public void setStopped(boolean stopped) {
    this.stopped = stopped;
  }

  public boolean isTitleScreen() {
    return titleScreen;
  }

  public boolean isWinScreen() {
    return winScreen;
  }

  public boolean isOverScreen() {
    return overScreen;
  }

  public boolean isDemo() {
    return demo;
  }

  public void setDemo(boolean demo) {
    this.demo = demo;
  }

  public int getNew() {
    return New;
  }

  public GameSounds getSounds() {
    return sounds;
  }

  @Override
  public Font getFont() {
    return font;
  }

  public void setTimer(long timer) {
    this.timer = timer;
  }

  public void setWinScreen(boolean winScreen) {
    this.winScreen = winScreen;
  }

  public void setOverScreen(boolean overScreen) {
    this.overScreen = overScreen;
  }

  public void setCurrScore(int currScore) {
    this.currScore = currScore;
  }

  public void setTitleScreen(boolean titleScreen) {
    this.titleScreen = titleScreen;
  }

  public void setNew(int aNew) {
    New = aNew;
  }
}
