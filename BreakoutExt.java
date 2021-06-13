// Breakout Game:
// part 1 "Creating the racket"
// part 2 "Creating the ball"
// part 3 "Reflection to the racket"
// part 4 "Add bricks"
// part 5 "Gaming with bricks"
// part 6 "Last changes"

package com.shpp.p2p.cs.sshaposhnikova.assignment4;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import com.shpp.cs.a.graphics.WindowProgram;
import acm.util.RandomGenerator;

import java.awt.*;
import java.awt.event.MouseEvent;


public class BreakoutExt extends WindowProgram {

    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /**
     * Dimensions of game board (usually the same)
     */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Width of a brick
     */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    /**
     * Velocity of ball
     */
    private static final int BALL_VELOCITY = 10;

    @Override
    public void run() {
        //include music
        double[] congrats = StdAudio.read("assets/celebrate.wav");
        click = StdAudio.read("assets/click.wav");
        loseRound = StdAudio.read("assets/lose1.wav");
        loseGame = StdAudio.read("assets/lose2.wav");
        currTime = System.nanoTime();
        //The first part is creating racket and adding mouse listeners
        createRack();
        addMouseListeners();
        //The fourth part is creating bricks
        createRectangleOfBricks();
        //The second part is creating a ball and adding velocity features to it
        createBall();
        //The third part is making the ball reflect to the racket
        defineBallVelocity();
        ballMove();
        //the seventh part is adding music
        if (numOfBricks == 0)
            StdAudio.play(speedUp(congrats));
    }

    //-------------------------------  PART 1 -------------------------------

    //This method creates racket
    public void createRack() {
        startRackX = getWidth() / 2 - PADDLE_WIDTH / 2;
        rack = new GRect(startRackX, getHeight() - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        rack.setFilled(true);
        add(rack);
    }

    //Called on mouse move and the racket to follow it
    public void mouseMoved(MouseEvent e) {
        startRackX = e.getX();
        double newRackX = e.getX() - rack.getWidth() / 2.0;
        if (rack != null) {
            if (newRackX < rack.getWidth() / 2.0)
                newRackX += rack.getWidth() / 2.0;
            else if (newRackX > (getWidth() - rack.getWidth()))
                newRackX -= rack.getWidth() / 2.0;
            rack.setLocation(newRackX, getHeight() - PADDLE_Y_OFFSET);
        }
        //If click not happen, ball should follow the racket
        if (!isRoundEnded)
            ball.setLocation(newRackX + rack.getWidth() / 2 - BALL_RADIUS,
                    getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT - BALL_RADIUS);
    }

    //These variables define racket itself and start mouse X position of racket
    private GRect rack;
    private double startRackX;

    //------------------------------- PART 2 -------------------------------

    //This method creates ball
    private void createBall() {
        startBallX = getWidth() / 2 - BALL_RADIUS;
        ball = new GOval(startBallX, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT - BALL_RADIUS,
                BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFilled(true);
        add(ball);
    }

    //This method leads ball using velocity, reflection, vx, vy
    private void defineBallVelocity() {
        vy = -3.0;
        //vx defines randomly
        RandomGenerator rgen = RandomGenerator.getInstance();
        vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5))
            vx = -vx;
    }

    //This method makes the ball move
    private void ballMove() {
        for (int i = 0; i < 3; i++) {
            isRoundEnded = false;
            waitForClick();
            //The ball should reach bottom of the screen
            // in order to get out of the loop and stop moving
            while (ball.getY() <= getHeight() - BALL_RADIUS * 2) {
                if (isRoundEnded) {
                    pause(BALL_VELOCITY);
                    ball.move(vx, vy);
                }
                reflection();
                if (numOfBricks == 0) {
                    isRoundEnded = true;
                    youWon();
                    break;
                }
            }
            if (numOfBricks == 0)
                break;
            else StdAudio.play(speedUp(loseRound));
        }
        if (numOfBricks != 0){
            youLost();
            StdAudio.play(loseGame);
        }
    }

    //When mouse pressed - move ball and give reflection
    public void mouseClicked(MouseEvent e) {
        StdAudio.play(speedUp(click));
        //Click happened and the game started
        isRoundEnded = true;
    }

    //This method gives reflection
    private void reflection() {
        GObject collider = getCollidingObject();
        //left and right borders
        if (ball.getX() <= 0 || ball.getX() >= getWidth() - BALL_RADIUS * 2) {
            vx = -vx;
        }
        //top border or if colliding obj is racket
        if (ball.getY() <= 0 || collider == rack) { // || ball.getY() <= getHeight() - BALL_RADIUS * 2
            vy = -vy;
        }
        //reflection from other objects - bricks

        if (collider != rack && collider != null) {
            vy = -vy;
            remove(collider);
            numOfBricks--;
            //the sound slows the ball
            //StdAudio.play(speedUp(click));
        }
    }

    //These variables define the ball itself and start mouse X position of the ball
    private GOval ball;
    private double startBallX;

    //This variable defines if fail happened
    private boolean isRoundEnded;

    //This variable defines velocity of the ball
    private double vx, vy;

    //-------------------------------  PART 3 -------------------------------

    //This method makes the ball reflect to the racket
    private GObject getCollidingObject() {
        GObject collidingObject;
        collidingObject = getElementAt(ball.getX(), ball.getY());
        //If there is an object
        if (collidingObject != null)
            return collidingObject;
            //If there is no object in left top angle
        else {
            collidingObject = getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS);
            //If there is an object
            if (collidingObject != null)
                return collidingObject;
                //If there is no object in left top angle and left bottom angle
            else {
                collidingObject = getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY());
                //If there is an object
                if (collidingObject != null)
                    return collidingObject;
                    //If there is no object in left top angle, left bottom angle and right top angle
                else {
                    collidingObject = getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS);
                    //If there is an object
                    if (collidingObject != null)
                        return collidingObject;
                        //If there is no object in left top angle, left bottom angle,
                        // right top angle and right bottom angle - then there is no object at all
                    else
                        return null;
                }
            }
        }
    }

    // Also changes in part 2 reflection() method

    //-------------------------------  PART 4 -------------------------------

    //This method creates the whole rectangle of bricks using method createRowOfBricks() and changes color of them
    private void createRectangleOfBricks() {
        for (int i = 0; i < NBRICK_ROWS; i++) {
            brickY = BRICK_Y_OFFSET + BRICK_HEIGHT * i + BRICK_SEP * i;
            //Colors
            if (i % 10 == 0 || i % 10 == 1)
                brickColor = Color.RED;
            else if (i % 10 == 2 || i % 10 == 3)
                brickColor = Color.ORANGE;
            else if (i % 10 == 4 || i % 10 == 5)
                brickColor = Color.YELLOW;
            else if (i % 10 == 6 || i % 10 == 7)
                brickColor = Color.GREEN;
            else brickColor = Color.CYAN;
            createRowOfBricks();
        }
    }

    //This method creates each row of bricks
    private void createRowOfBricks() {
        //The course of events if number of bricks in row is even
        if (isEven(NBRICKS_PER_ROW)) {
            for (int i = NBRICKS_PER_ROW / 2; i > -NBRICKS_PER_ROW / 2; i--) {
                //The formula of X coordinate of each brick
                brickX = getWidth() / 2 - BRICK_WIDTH * i - BRICK_SEP * (i - 0.5);
                createBrick();
            }
        } else { //The course of events if number of bricks in row is odd
            for (int i = NBRICKS_PER_ROW / 2; i > -NBRICKS_PER_ROW; i--) {
                //The formula of X coordinate of each brick
                brickX = getWidth() / 2 - BRICK_WIDTH * (i + 0.5) - BRICK_SEP * i;
                createBrick();
            }
        }
    }

    //This method creates each brick
    public void createBrick() {
        GRect brick = new GRect(brickX, brickY, BRICK_WIDTH, BRICK_HEIGHT);
        brick.setFilled(true);
        brick.setFillColor(brickColor);
        brick.setColor(brickColor);
        add(brick);
    }

    //This method checks if the number is even
    private boolean isEven(int num) {
        if (num % 2 == 0)
            return true;
            //Else the number is odd
        else
            return false;
    }

    //These variable define X and Y of bricks
    private double brickX, brickY;
    //This variable color of bricks
    private Color brickColor;

    //-------------------------------  PART 5 -------------------------------

    // Changes in part 2 reflection() method

    //-------------------------------  PART 6 -------------------------------

    //Changes in part 2 ballMove() and reflection() methods

    //This method tells you, that you won
    private void youWon() {
        GLabel won = new GLabel("You Won!", getWidth() / 2, getHeight() / 2);
        won.setFont("Verdana-25");
        add(won);
    }

    //This method tells you, that you lost
    private void youLost() {
        GLabel won = new GLabel("You Lost! Try again!", getWidth()/2-200, getHeight()/2);
        won.setFont("Verdana-25");
        add(won);
    }

    //This variable defines number of bricks
    private int numOfBricks = NBRICKS_PER_ROW * NBRICK_ROWS;

    //-------------------------------  PART 7 -------------------------------

    //add sounds

    /*
      Given a sound clip, returns a new sound clip that's twice as fast
      as the original.

      @param clip The original sound clip.
      @return A sped-up version of that sound clip.
     */
    private double[] speedUp(double[] clip) {
        /* We only need less space. */
        double[] result = new double[clip.length / 4];

        /* Sample from twice the current position. */
        for (int i = 0; i < result.length/4; i++) {
            result[i] = clip[i * 2];
        }

        return result;
    }

    //These variables define different sounds
    private double[] loseRound;
    private double[] loseGame;
    private double[] click;
    private double currTime;
}
