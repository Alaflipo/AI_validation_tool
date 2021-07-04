package proc.sketches;

import processing.core.PApplet;

public class Player {
    private PApplet sketch;
    private int player;             //whether this player is player 1 or 2
    private boolean alive;          //If false, this player died (may be redundant, but who knows :) )
    private double speed;           //Speed of the player
    private boolean inverted;       //If true, the player's controls are inverted
    public NavPoint currentPoint;   //Current point that the player is on.
    //Change this halfway between a move
    public int[] location;          //Position of this player, in coordinates (since the player is not exactly in the NavPoint)
    private boolean[] powerUps;     //Which power-ups the player is affected by
    private boolean hasPowerUp;
    //[0] -> speed increase, [1] -> speed decrease, [2] -> inverted controls
    //More can be added by adding 1 to the length of this array
    //and adding its effect in usePowerUp under case (length + 1)

    private NeuralNetwork brain;        //holds the neural network which makes decisions
    private int fov;                    //distance the player can see (can see in a box of size fov around him)
    private int direction;              //direction the player is moving to
    //0 --> left, 1 --> up, 2 --> right, 3 --> down

    //TODO Morris, Michel here. Change 1 to down and 3 to up pls :)
    //TODO Talking about your code of course, not the comments


    //TODO: to be determined how to evaluate score and fitness
    private int score;                  //holds the current score of the player
    private double fitness;                //holds the current fitness of the player

    /**
     * Constructs a AI character not yet on the board
     * AI is initially alive, controls are not inverted, and speed is normal
     * The player's starting location is found and no power-ups are applied yet
     */
    public Player(PApplet sketch){
        this.sketch = sketch;
        //Initialize vital values
        alive = true;
        speed = 1.0; //Subject to change
        //Idea for start of game: speed is 0 until game starts?
        inverted = false;
        currentPoint = null;

        direction = 3; //Initially, go up. May be subject to change, though it makes the most sense to me :)

        //Find the current location, subject to change
        location = new int[2];

        //Initialize power-ups, all false
        powerUps = new boolean[3];
        hasPowerUp = false;
        for (int i = 0; i < powerUps.length; i++){
            powerUps[i] = false;
        }
        score = 1;
        fov = 2;
        brain = new NeuralNetwork(sketch, (fov * 2 + 1) * (fov * 2 + 1) + 1, new int[]{8,8}, 3);
        //String weightsFile = "PUWeights.txt";
        //brain.readWeights(weightsFile);
    }

    public NavPoint getLocation() {
        return currentPoint;
    }

    public void setLocation(NavPoint location) {
        currentPoint = location;
    }

    public void setDirection(int dir) {
        direction = dir;
    }

    public int getDirection() {
        return direction;
    }

    public void setStartPoint(NavPoint startPoint) {
        this.currentPoint = startPoint;
    }

    /**
     * Gets the current speed of this player
     * @return speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Sets the speed to a new value
     * @param newSpeed the new speed of the player
     */
    public void setSpeed(double newSpeed){
        speed = newSpeed;
    }

    public void addPowerUp(boolean PU) {
        hasPowerUp = PU;
    }

    public boolean hasPU() {
        return hasPowerUp;
    }

    /**
     * Affects the player with a specific power-up
     * @param powerUp Index of the power-up: 0 -> speed increase, 1 -> speed decrease, 2 -> controls
     */
    public void usePowerUp(int powerUp){
        //Don't apply the same power-up twice
        if (!powerUps[powerUp]){
            //Enable this power-up
            powerUps[powerUp] = true;

            //Case distinction for power-up effects
            switch (powerUp){
                case 0:
                    //Maybe some effect?
                    setSpeed(0.5 * speed);
                    //Going to find out how to do a timer soon!
                    //Maybe some effect?
                    setSpeed(2 * speed);
                    break;

                case 1:
                    //Maybe some effect?
                    setSpeed(2 * speed);
                    //Going to find out how to do a timer soon!
                    //Maybe some effect?
                    setSpeed(0.5 * speed);
                    break;

                case 2:
                    //Maybe some effect?
                    inverted = true;
                    //Going to find out how to do a timer soon!
                    //Maybe some effect?
                    inverted = false;
                    break;
            }

            powerUps[powerUp] = false;
        }
    }

    /**
     * Change direction through a button press. (This needs to be implemented using Input I believe, that'll come later)
     */
    public void changeDirection(){
        //direction = some new direction by input
    }

    /**
     * sets the new direction of the player based on the highest value (highest certainty)
     * of the output of the neural network
     * output values:
     *  - 0 --> turn left
     *  - 1 --> go straight
     *  - 2 --> turn right
     * @param input array of double values containing the current cubestate
     */
    public void think(double[] input) {
        double[] output = brain.feedForward(input);
        double maxValue = 0;
        int maxIndex = 0;
        for (int i = 0; i < output.length; i++) {
            if (output[i] > maxValue) {
                maxIndex = i;
                maxValue = output[i];
            }
        }
        switch (maxIndex) {
            case 0: direction -= 1;
            case 1: break;
            case 2: direction += 1;
        }
        direction = (((direction % 4) + 4) % 4);

    }

    /**
     * mutates the brain of the AI player by changing the weights with a certain chance
     * @param mutationChance the chance that a weight in
     */
    public void mutate(double mutationChance) {
        this.brain.mutate(mutationChance);
    }

    /**
     * changes the weights of the brain by reading new weights from provided file
     * @param fileWithNewBrain name of the file you want read from
     */
    public void changeBrain(String fileWithNewBrain) {
        brain.readWeights(fileWithNewBrain);
    }

    /**
     * implements the crossover algorithm by combining half of the brain of parent1 and
     * half of the brain of player 2 into a new brain.
     * This also creates a new player called child and returns it
     * @param parent2 second player to combine weights with
     */
    public Player haveSex(Player parent2) {
        Player child = new Player(sketch);
        NeuralNetwork childBrain = brain.crossOver(parent2.getBrain());
        child.setBrain(childBrain);
        return child;
    }

    public int getScore() { return score; }
    public void addScore(int amount) { this.score += amount; }

    public double getFitness() { return fitness; }
    public void setFitness(double fitness) { this.fitness = fitness; }

    public NeuralNetwork getBrain() { return brain.copy(); }
    public void setBrain(NeuralNetwork brain) { this.brain = brain; }

    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }

    public int getFOV() { return fov; }


}
