package proc.sketches;

import processing.core.PApplet;

import java.util.Random;                //for generation random numbers

public class Genetics {
    PApplet sketch;

    private int generation;         //current generation number
    private int popSize;            //the size of the population
    private Player[] players;       //Array with the current players in the population

    private int radius;             //the radius of the cube being played in
    private int tileSize;           //the size of the tiles generated in the cube

    private int maxGameTime;        //maximal amount of steps the AI can make before terminating
    public float score;             //the average score of the population
    private int highScore;          //the highest score in the population


    Genetics(PApplet sketch, int popSize, int radius, int tileSize) {
        this.sketch = sketch;
        this.popSize = popSize;
        generation = 0;

        this.radius = radius;
        this.tileSize = tileSize;

        maxGameTime = 1000;
        score = 0;

        players = new Player[popSize];

        for (int i = 0 ; i < popSize; i++) {
            players[i] = new Player(this.sketch);
        }
    }

    /**
     * main method for this class that runs the logical game loop, it keeps track of the games
     * that are being played, builds new playfields and ensures that the evolution methods
     * are being called and new generations are generated
     *
     */
    public NeuralNetwork[] evolve() {
        for (int i = 0; i < popSize/2; i+=2) {
            //reset the playing field
            Player player1 = players[i];
            Player player2 = players[i+1];
            PlayField playField = new PlayField(sketch, radius, tileSize, player1, player2);
            playField.generate();
            //Play for maxGameTime turns
            int time = 0;
            while (time < maxGameTime) {
                playField.updateAI();
                //How score is added can be changed to train the AI on different tactics
                //These settings are the settings for the creating of the hard AI difficulty
                if (player1.hasPU()) {
                    player1.addScore(1);
                    player2.addPowerUp(false);
                }
                if (player1.isAlive()) {
                    //player1.addScore(1);
                    if (!player2.isAlive()) {
                        //player1.addScore(10);
                    }
                }
                if (player2.hasPU()) {
                    player2.addScore(1);
                    player2.addPowerUp(false);
                }
                if (player2.isAlive()) {
                    //player2.addScore(1);
                    if (!player1.isAlive()) {
                        //player2.addScore(10);
                    }
                }
                //if both have died stop playing
                if (!(player1.isAlive() || player2.isAlive())) {
                    break;
                }
                time += 1;
            }
        }
        //if all AI's have played their games, evaluate the fitness
        setFitness();
        generateHighScore();
        //generate a new population of players based on the just calculated fitness
        //the selection procedure for generating a new population works by executing algorithms
        //for selection, mutation and crossover
        NeuralNetwork[] bestBrains = getBestBrains();
        generateNewPopulation();
        generation += 1;
        return bestBrains;
    }

    /**
     * Calculates individual fitness of population after all players in the population have played a game
     * and got a final score
     *
     */
    private void setFitness() {
        int sumScores = 0;
        for (int i = 0; i < popSize; i++) {
            sumScores += this.players[i].getScore();
        }
        for (int i = 0; i < popSize; i++) {
            this.players[i].setFitness((double) this.players[i].getScore() / sumScores);
        }
        score = (float) sumScores/ (float) popSize;
        System.out.println(sumScores);
    }
    /**
     * Determines which player has gained the highest score in the generation
     * and returns this highest scoring player
     *
     */
    private void generateHighScore() {
        int highScore = 0;
        int hsIndex = 0;
        for (int i = 0; i < popSize; i++) {
            int score = players[i].getScore();
            if (score > highScore) {
                highScore = score;
                hsIndex = i;
            }
        }
        this.highScore = players[hsIndex].getScore();
    }

    private NeuralNetwork[] getBestBrains() {
        int highScore = 0;
        int hsIndex = 0;
        int hsIndexPrev = 0;
        for (int i = 0; i < popSize; i++) {
            int score = players[i].getScore();
            if (score > highScore) {
                hsIndexPrev = hsIndex;
                highScore = score;
                hsIndex = i;
            }
        }
        NeuralNetwork[] bestBrains = new NeuralNetwork[]{players[hsIndex].getBrain().copy(), players[hsIndexPrev].getBrain().copy()};
        return bestBrains;
    }

    /**
     * generates a new population based on the fitness of the current population
     * by selection and mutation
     *
     */
    void generateNewPopulation() {
        Player[] newPop = new Player[popSize];
        for (int i = 0; i < popSize; i++) {
            Player parent1 = pickParent();
            Player parent2 = pickParent();
            newPop[i] = crossOver(parent1, parent2);
            //newPop[i] = pickParent();
            newPop[i].mutate(0.1);
        }

        this.players = newPop;
    }

    /**
     * Algorithm for selecting parents
     */
    private Player pickParent() {
        int index = 0;
        Random r = new Random();
        double randomN = r.nextDouble();
        while (randomN >= 0) {
            index++;
            if (index >= popSize) {
                index = popSize - 1;
            }
            randomN -= this.players[index].getFitness();
        }
        NeuralNetwork parentBrain = this.players[index].getBrain();
        Player parent = new Player(sketch);
        parent.setBrain(parentBrain);
        return parent;
    }

    /**
     * Crossover algorithm
     */
    private Player crossOver(Player parent1, Player parent2) {
        Player child = parent1.haveSex(parent2);
        return child;
    }

    public float getScore() {
        return score;
    }

    public int getGeneration() {
        return this.generation;
    }

    public int getHighScore() {
        return this.highScore;
    }

}