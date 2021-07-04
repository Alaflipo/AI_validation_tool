package proc.sketches;

import processing.core.PApplet;
import processing.event.KeyEvent;

import java.util.ArrayList;

enum Occupation {
    EMPTY,
    PLAYERTAIL,
    AITAIL,
    PLAYERHEAD,
    AIHEAD,
    POWERUP
}

public class Main extends PApplet {

    Genetics genetics;
    NeuralNetwork[] bestBrains;
    PlayField game;
    ArrayList<Float> scores;

    int moveTime = 1000;
    int timer = 0;

    int navPointSize = 20;
    int radius = 5;
    int popSize = 1000;

    public void settings() {
        size(1000, 700);
        scores = new ArrayList<Float>();
        scores.add((float)0);
        genetics = new Genetics(this, popSize, navPointSize, radius);
        bestBrains = genetics.evolve();
        Player player1 = new Player(this);
        player1.setBrain(bestBrains[0]);
        Player player2 = new Player(this);
        player2.setBrain(bestBrains[1]);
        game = new PlayField(this, radius, navPointSize, player1, player2);
        game.generate();
        scores.add(genetics.getScore());
        //game = new PlayField(radius, navPointSize);
        //game.generate();
    }

    public void draw(){
        background(0);
        //game.updateAI();
        //game.show();
        game.updateAI();
        game.show();
        if (!game.player1.isAlive() && !game.player2.isAlive()) {
            print("lost!");
            timer = moveTime + 1;
        }

        if (timer >= moveTime) {
            bestBrains = genetics.evolve();
            if (scores.size() > 200) {
                scores.remove(0);
            }
            scores.add(genetics.getScore());
            Player player1 = new Player(this);
            player1.setBrain(bestBrains[0]);
            Player player2 = new Player(this);
            player2.setBrain(bestBrains[1]);
            game = new PlayField(this, radius, navPointSize, player1, player2);

            game.generate();

            timer = 0;
        }

        drawGraph();
        timer += 1;
        delay(200);
    }

    void drawGraph() {
        pushMatrix();
        rectMode(CORNER);
        translate(500, 0);
        fill(0);
        stroke(255);
        rect(0,0,400,200);
        fill(255);
        textSize(24);
        text("Generation: " + genetics.getGeneration(), 20, 50);
        text("Average pop score: " + genetics.getScore(), 20, 100);
        text("Score best AI: " + genetics.getHighScore(), 20, 150);
        popMatrix();
        pushMatrix();
        translate(500,460);
        fill(0);
        stroke(255);
        rect(0,0,400,200);
        fill(255);
        textSize(12);
        text("Generation --> ", 10, 220);
        text("^", -21, 140);
        text("|", -20, 150);
        text("|", -20, 160);
        text("Score", -40, 180);
        stroke(255);
        strokeWeight(5);
        for(int i = 0 ; i < scores.size() - 1; i++) {
            line(i * 2, ((100 - scores.get(i)))* (float) 2, (i+1) * 2, ((float)(100 - scores.get(i + 1))) * (float) 2);
        }
        strokeWeight(1);
        popMatrix();
    }

    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
        //if (key == 'a') {
        //    board.getPlayer().setDirection(0);
        //} else if (key == 'd') {
        //    board.getPlayer().setDirection(1);
        if (key == 'o') {
            moveTime = 0;
            print(moveTime);
        } else if (key == 'p') {
            moveTime = 1000;
            print(moveTime);
        } else if (key == 's') {
            bestBrains[0].saveWeights("weights.txt");
        }
    }

    public static void main(String... args){
        String[] processingArgs = {"AIValidation"};
        Main mainProcessing = new Main();
        PApplet.runSketch(processingArgs, mainProcessing);
    }

}
