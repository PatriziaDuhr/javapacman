/* Drew Schuster */
import javax.swing.*;
import java.awt.event.*;
import javax.swing.JApplet;
import java.awt.*;
import java.lang.*;

/* This class contains the entire game... most of the game logic is in the Board class but this
   creates the gui and captures mouse and keyboard input, as well as controls the game states */
public class Pacman extends JApplet implements MouseListener, KeyListener
{ 

  /* These timers are used to kill title, game over, and victory screens after a set idle period (5 seconds)*/
  long titleTimer = -1;
  long timer = -1;

  /* Create a new board */
  Board b=new Board(); 

  /* This timer is used to do request new frames be drawn*/
  javax.swing.Timer frameTimer;
 

  /* This constructor creates the entire game essentially */   
  public Pacman()
  {
    b.requestFocus();

    /* Create and set up window frame*/
    JFrame f=new JFrame(); 
    f.setSize(420,460);

    /* Add the board to the frame */
    f.add(b,BorderLayout.CENTER);

    /*Set listeners for mouse actions and button clicks*/
    b.addMouseListener(this);  
    b.addKeyListener(this);  

    /* Make frame visible, disable resizing */
    f.setVisible(true);
    f.setResizable(false);

    /* Set the New flag to 1 because this is a new game */
    b.setNew(1);

    /* Manually call the first frameStep to initialize the game. */
    stepFrame(true);

    /* Create a timer that calls stepFrame every 30 milliseconds */
    frameTimer = new javax.swing.Timer(30,new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          stepFrame(false);
        }
      });

    /* Start the timer */
    frameTimer.start();

    b.requestFocus();
  }

  /* This repaint function repaints only the parts of the screen that may have changed.
     Namely the area around every player ghost and the menu bars
  */
  public void repaint()
  {
    if (b.getPlayer().isTeleport())
    {
      b.repaint(b.getPlayer().getLastX() -20, b.getPlayer().getLastY() -20,80,80);
      b.getPlayer().setTeleport(false);
    }
    b.repaint(0,0,600,20);
    b.repaint(0,420,600,40);
    b.repaint(b.getPlayer().getX() -20, b.getPlayer().getY() -20,80,80);
    b.repaint(b.getGhost1().getX() -20, b.getGhost1().getY() -20,80,80);
    b.repaint(b.getGhost2().getX() -20, b.getGhost2().getY() -20,80,80);
    b.repaint(b.getGhost3().getX() -20, b.getGhost3().getY() -20,80,80);
    b.repaint(b.getGhost4().getX() -20, b.getGhost4().getY() -20,80,80);
  }

  /* Steps the screen forward one frame */
  public void stepFrame(boolean New)
  {
    /* If we aren't on a special screen than the timers can be set to -1 to disable them */
    if (!b.isTitleScreen() && !b.isWinScreen() && !b.isOverScreen())
    {
      timer = -1;
      titleTimer = -1;
    }

    /* If we are playing the dying animation, keep advancing frames until the animation is complete */
    if (b.getPlayer().getDying() >0)
    {
      b.repaint();
      return;
    }

    /* New can either be specified by the New parameter in stepFrame function call or by the state
       of b.New.  Update New accordingly */ 
    New = New || (b.getNew() !=0) ;

    /* If this is the title screen, make sure to only stay on the title screen for 5 seconds.
       If after 5 seconds the user hasn't started a game, start up demo mode */
    if (b.isTitleScreen())
    {
      if (titleTimer == -1)
      {
        titleTimer = System.currentTimeMillis();
      }

      long currTime = System.currentTimeMillis();
      if (currTime - titleTimer >= 5000)
      {
        b.setTitleScreen(false);
        b.setDemo(true);
        titleTimer = -1;
      }
      b.repaint();
      return;
    }
 
    /* If this is the win screen or game over screen, make sure to only stay on the screen for 5 seconds.
       If after 5 seconds the user hasn't pressed a key, go to title screen */
    else if (b.isWinScreen() || b.isOverScreen())
    {
      if (timer == -1)
      {
        timer = System.currentTimeMillis();
      }

      long currTime = System.currentTimeMillis();
      if (currTime - timer >= 5000)
      {
        b.setWinScreen(false);
        b.setOverScreen(false);
        b.setTitleScreen(true);
        timer = -1;
      }
      b.repaint();
      return;
    }


    /* If we have a normal game state, move all pieces and update pellet status */
    if (!New)
    {
      /* The pacman player has two functions, demoMove if we're in demo mode and move if we're in
         user playable mode.  Call the appropriate one here */
      if (b.isDemo())
      {
        b.getPlayer().demoMove();
      }
      else
      {
        b.getPlayer().move();
      }

      /* Also move the ghosts, and update the pellet states */
      b.getGhost1().move();
      b.getGhost2().move();
      b.getGhost3().move();
      b.getGhost4().move();
      b.getPlayer().updatePellet();
      b.getGhost1().updatePellet();
      b.getGhost2().updatePellet();
      b.getGhost3().updatePellet();
      b.getGhost4().updatePellet();
    }

    /* We either have a new game or the user has died, either way we have to reset the board */
    if (b.isStopped() || New)
    {
      /*Temporarily stop advancing frames */
      frameTimer.stop();

      /* If user is dying ... */
      while (b.getPlayer().getDying() >0)
      {
        /* Play dying animation. */
        stepFrame(false);
      }

      /* Move all game elements back to starting positions and orientations */
      b.getPlayer().setCurrDirection('L');
      b.getPlayer().setDirection('L');
      b.getPlayer().setDesiredDirection('L');
      b.getPlayer().setX(200);
      b.getPlayer().setY(300);
      b.getGhost1().setX(180);
      b.getGhost1().setY(180);
      b.getGhost2().setX(200);
      b.getGhost2().setY(180);
      b.getGhost3().setX(220);
      b.getGhost3().setY(180);
      b.getGhost4().setX(220);
      b.getGhost4().setY(180);

      /* Advance a frame to display main state*/
      b.repaint(0,0,600,600);

      /*Start advancing frames once again*/
      b.setStopped(false);
      frameTimer.start();
    }
    /* Otherwise we're in a normal state, advance one frame*/
    else
    {
      repaint(); 
    }
  }  

  /* Handles user key presses*/
  public void keyPressed(KeyEvent e) 
  {
    /* Pressing a key in the title screen starts a game */
    if (b.isTitleScreen())
    {
      b.setTitleScreen(false);
      return;
    }
    /* Pressing a key in the win screen or game over screen goes to the title screen */
    else if (b.isWinScreen() || b.isOverScreen())
    {
      b.setTitleScreen(true);
      b.setWinScreen(false);
      b.setOverScreen(false);
      return;
    }
    /* Pressing a key during a demo kills the demo mode and starts a new game */
    else if (b.isDemo())
    {
      b.setDemo(false);
      /* Stop any pacman eating sounds */
      b.getSounds().nomNomStop();
      b.setNew(1);
      return;
    }

    /* Otherwise, key presses control the player! */ 
    switch(e.getKeyCode())
    {
      case KeyEvent.VK_LEFT:

       b.getPlayer().setDesiredDirection('L');
       break;     
      case KeyEvent.VK_RIGHT:
       b.getPlayer().setDesiredDirection('R');
       break;     
      case KeyEvent.VK_UP:
       b.getPlayer().setDesiredDirection('U');
       break;     
      case KeyEvent.VK_DOWN:
       b.getPlayer().setDesiredDirection('D');
       break;     
    }

    repaint();
  }

  /* This function detects user clicks on the menu items on the bottom of the screen */
  public void mousePressed(MouseEvent e){
    if (b.isTitleScreen() || b.isWinScreen() || b.isOverScreen())
    {
      /* If we aren't in the game where a menu is showing, ignore clicks */
      return;
    }

    /* Get coordinates of click */
    int x = e.getX();
    int y = e.getY();
    if ( 400 <= y && y <= 460)
    {
      if ( 100 <= x && x <= 150)
      {
        /* New game has been clicked */
        b.setNew(1);
      }
      else if (180 <= x && x <= 300)
      {
        /* Clear high scores has been clicked */
        b.clearHighScores();
      }
      else if (350 <= x && x <= 420)
      {
        /* Exit has been clicked */
        System.exit(0);
      }
    }
  }
  
 
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
  public void mouseClicked(MouseEvent e){}
  public void keyReleased(KeyEvent e){}
  public void keyTyped(KeyEvent e){}
  
  /* Main function simply creates a new pacman instance*/
  public static void main(String [] args)
  {
      Pacman c = new Pacman();
  } 
}
