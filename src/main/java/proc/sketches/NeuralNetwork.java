package proc.sketches;

import Jama.*;                          //library for easy Matrix representation and linear algebra
import processing.core.PApplet;
import java.io.File;                    //for creating new files
import java.io.FileWriter;              //for writing weights to a txt file
import java.io.IOException;             //for error handling
import java.io.FileNotFoundException;   //for error handling
import java.net.URISyntaxException;
import java.util.Scanner;               //for reading weights from a txt file
import java.util.Random;                //for generation random numbers

public class NeuralNetwork {
    PApplet sketch;

    int input_nodes;    //number of input nodes
    int[] hidden_nodes; //number of hidden nodes per hidden layer
    int output_nodes;   //number of output nodes
    int layers;         //number of total layers in the network

    //Array of size (layers - 1) storing the weights of the network between each two preceding layers
    // represented in matrix objects
    private Matrix[] weights;

    /**
     * Creates a neural network of a certain size and amount of hidden layers
     * (which can be specified) with all random double weights between -1 and 1
     * @param input number of input nodes
     * @param hidden number of input nodes per hidden layer
     * @param output number of output nodes
     */
    public NeuralNetwork(PApplet sketch, int input, int[] hidden, int output) {
        this.sketch = sketch;
        input_nodes = input;
        hidden_nodes = hidden;
        output_nodes = output;
        layers = hidden.length + 2;

        weights = new Matrix[hidden.length + 1];

        //make sure the weight matrices all have the correct size
        //the first column of each matrix is for the bias
        weights[0] = new Matrix(hidden[0], input + 1);
        for (int i = 1; i < hidden.length; i++) {
            weights[i] = new Matrix(hidden[i], hidden[i - 1] + 1);
        }
        weights[hidden.length] = new Matrix(output, hidden[hidden.length - 1] + 1);

        Random r = new Random();

        //give each element in the matrices a random value between -1 and 1
        for (Matrix weight : weights) {
            for (int i = 0; i < weight.getRowDimension(); i++) {
                for (int j = 0; j < weight.getColumnDimension(); j++) {
                    double randNum = 2 * r.nextDouble() - 1;
                    weight.set(i, j, randNum);
                }
            }
        }
    }

    /**
     * returns the full array of weights of the neural network
     */
    public Matrix[] getWeights() {
        return weights;
    }

    /**
     * sets the full weights of the neural network
     * @param newWeights array containing matrices for each individual layer
     */
    public void setWeights(Matrix[] newWeights) {
        weights = newWeights;
    }

    /**
     * Feedforward algorithm which returns the output of the network represented in double values
     * @param input_data an array of double values representing the current state
     *                   of the environment (preferably values in the range of [-1,1])
     */
    public double[] feedForward(double[] input_data) {
        double[][] input_data_bias = prepareData(input_data);
        Matrix input = new Matrix(input_data_bias);
        Matrix hidden = weights[0].times(input);
        activation(hidden);
        for (int i = 1; i < layers - 1; i++) {
            hidden = addBias(hidden);
            hidden = weights[i].times(hidden);
            activation(hidden);
        }
        Matrix output = hidden;
        return output.getColumnPackedCopy();
    }

    /**
     * Applies the activation function over all elements of the input matrix.
     * In this case the sigmoid function is used as activations: sigmoid(x) = 1/(1+e^(-x))
     * @param input matrix on which the activation function should be applied
     */
    private void activation(Matrix input) {
        for (int i = 0; i < input.getRowDimension(); i++) {
            input.set(i, 0, 1 /(1 + Math.exp(-1 * input.get(i, 0))));
        }
    }

    /**
     * adds a bias node to the input and transforms the input to a nx1 matrix
     * @param data data which should be transformed
     */
    private double[][] prepareData(double[] data) {
        double[][] data_bias = new double[data.length + 1][1];
        data_bias[0][0] = 1;
        for (int i = 0; i < data.length; i++) {
            data_bias[i + 1][0] = data[i];
        }
        return data_bias;
    }

    /**
     * adds a bias node to the calculated hidden nodes matrix
     * @param hidden calculated values of the hidden nodes
     */
    private Matrix addBias(Matrix hidden) {
        int newLength = hidden.getRowDimension() + 1;
        Matrix newHidden = new Matrix(newLength, 1);
        newHidden.set(0, 0, 1);
        newHidden.setMatrix(1, newLength - 1, 0, 0, hidden);
        return newHidden;
    }

    /**
     * Applies a mutation algorithm to this matrix. Each weight has a specified chance
     * (in this case 0.1) to be changed ever so slightly (changed by a value between
     * -0.1 and 0.1)
     */
    public void mutate(double chance) {
        Random r = new Random();
        for (Matrix weight : weights) {
            for (int i = 0; i < weight.getRowDimension(); i++) {
                for (int j = 0; j < weight.getColumnDimension(); j++) {
                    double pick = r.nextDouble();
                    if (pick < chance) {
                        double randNum = 0.2 * r.nextDouble() - 0.1;
                        weight.set(i, j, weight.get(i, j) + randNum);
                    }
                }
            }
        }
    }

    /**
     * copies the current neural network and returns a new neural network containing
     * the same weights.
     */
    NeuralNetwork copy() {
        NeuralNetwork newNN = new NeuralNetwork(sketch, input_nodes, hidden_nodes, output_nodes);
        Matrix[] newWeights = new Matrix[layers - 1];
        for (int i = 0; i < layers - 1; i++) {
            newWeights[i] = weights[i].copy();
        }
        newNN.setWeights(newWeights);
        return newNN;
    }

    /**
     * prints the weights of the neural network
     */
    public void printWeights() {
        for (int i = 0; i < layers - 1; i++) {
            weights[i].print(5, 2);
            System.out.println();
        }
    }

    /**
     * Implements crossover by making a choice of either picking the weight
     * of this neural network or picking the weight of the neural network parent2
     * and creating a new array of weights (in size identical to this neural net)
     * and returns a new neural net initialized with the new weights
     * @param parent2 the neural net to apply crossover with
     */
    public NeuralNetwork crossOver(NeuralNetwork parent2) {
        NeuralNetwork child = new NeuralNetwork(sketch, this.input_nodes, this.hidden_nodes, this.output_nodes);
        Matrix[] childWeights = this.weights;
        Matrix[] weightsP2 = parent2.getWeights();

        Random r = new Random();
        for (int k = 0; k < childWeights.length; k++) {
            for (int i = 0; i < childWeights[k].getRowDimension(); i++) {
                for (int j = 0; j < childWeights[k].getColumnDimension(); j++) {
                    double randNum = r.nextDouble();
                    if (randNum < 0.5) {
                        childWeights[k].set(i, j, weights[k].get(i, j));
                    } else {
                        childWeights[k].set(i, j, weightsP2[k].get(i, j));
                    }
                }
            }
        }
        child.setWeights(childWeights);
        return child;

    }

    /**
     * saves the weights to a txt file in the following format:
     * Matrix weights layer 1 (input x hidden_layer[0])
     *                    <empty>
     * Matrix weights layer 2 (hidden_layer[0] x hidden_layer[1])
     *                    <empty>
     *                       .
     *                       .
     *                    <empty>
     * Matrix weights layer n (hidden_layer[n] x output)
     *
     * @param fileName name of the file you want to save to
     */
    public void saveWeights(String fileName) {
        //creates a new file
        try {
            File weightsFile = new File(fileName);
            if (! weightsFile.createNewFile()) {
                System.out.println("The file already exists!");
            }
        } catch (IOException e) {
            System.out.println("Error: could not create new file");
            e.printStackTrace();
        }

        //writes to the newly crated file
        try {
            FileWriter file = new FileWriter(fileName);
            for (int k = 0; k < layers - 1; k++) {
                double[][] wLayer = weights[k].getArray();
                for (double[] doubles : wLayer) {
                    for (int j = 0; j < wLayer[0].length; j++) {
                        file.write(doubles[j] + " ");
                    }
                    file.write(System.lineSeparator());
                }
                file.write(System.lineSeparator());
            }
            file.close();
        } catch (IOException e) {
            System.out.println("Error: could not write to the file");
            e.printStackTrace();
        }
    }

    /**
     * reads the weights from a txt file (in the current game directory)
     * with the following format:
     * Matrix weights layer 1 (input x hidden_layer[0])
     *                    <empty>
     * Matrix weights layer 2 (hidden_layer[0] x hidden_layer[1])
     *                    <empty>
     *                       .
     *                       .
     *                    <empty>
     * Matrix weights layer n (hidden_layer[n] x output)
     * @param fileName name of the file you want read from
     */
    public void readWeights(String fileName) {
        try {
            File weightsFile = new File(fileName);
            Scanner fileReader = new Scanner(weightsFile);
            int layerCount = 0;
            int lineCount = 0;
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                if (!line.isEmpty()) {
                    String[] newWeights = line.split(" ");
                    for (int i = 0; i < newWeights.length - 1; i++) {
                        double weight = Double.parseDouble(newWeights[i]);
                        System.out.print(weight);
                        System.out.print(" ");
                        weights[layerCount].set(lineCount, i, weight);
                    }
                    lineCount++;
                    System.out.println("");
                } else {
                    lineCount = 0;
                    layerCount += 1;
                }
                System.out.println("");
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: this file does not exist!");
            e.printStackTrace();
        }
    }
}