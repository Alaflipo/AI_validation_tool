package proc.sketches;


import processing.core.PApplet;
import java.util.Random;                //for generation random numbers

class PlayField {
    PApplet sketch;
    NavCube cube;
    Player player1;
    Player player2;
    NavPoint PowerUp;

    PlayField(PApplet sketch, int radius, int navPointSize) {
        this.sketch = sketch;
        cube = new NavCube(sketch, radius, navPointSize);
        player1 = new Player(sketch);
        player2 = new Player(sketch);
    }

    PlayField(PApplet sketch, int radius, int navPointSize, Player p1, Player p2) {
        this.sketch = sketch;
        cube = new NavCube(sketch, radius, navPointSize);
        player1 = p1;
        player2 = p2;
    }

    public void generate() {
        cube.generateCube();
        cube.connectNeighbours();
        Random r = new Random();
        int loc1 = (int) (Math.round((cube.faceLength * 0.5 * cube.faceLength)) - 1);
        //int loc1 = r.nextInt(cube.getNavPoints().length);
        player1.setLocation(cube.getNavPoints()[loc1]);
        int loc2 = (int) (Math.round((cube.faceLength * cube.faceLength * 5) + (cube.faceLength * 0.5 * cube.faceLength)) - 1);
        //int loc2 = r.nextInt(cube.getNavPoints().length);
        player2.setLocation(cube.getNavPoints()[loc2]);

        cube.addPlayer(player1.getLocation());
        cube.addPlayer(player2.getLocation());

        PowerUp = cube.insertPowerUp();
    }

    public void show() {
        cube.drawCube();
    }

    public void updateAI() {
        //Think about what the next move should be
        NavPoint playerLocation1 = player1.getLocation();
        double[] surroundings1 = convertToNNInput(playerLocation1, player1.getDirection(), player1.getFOV());
        player1.think(surroundings1);

        NavPoint newLocation1 = cube.getNeighbours()[playerLocation1.getIndex()][player1.getDirection()];
        int newDirection1 = getNewDirection(playerLocation1, newLocation1, player1.getDirection());

        NavPoint playerLocation2 = player2.getLocation();
        double[] surroundings2 = convertToNNInput(playerLocation2, player2.getDirection(), player2.getFOV());
        player2.think(surroundings2);

        NavPoint newLocation2 = cube.getNeighbours()[playerLocation2.getIndex()][player2.getDirection()];
        int newDirection2 = getNewDirection(playerLocation2, newLocation2, player2.getDirection());

        //perform the movement
        if (newLocation1.getOccupation() == Occupation.AITAIL || newLocation1.getOccupation() == Occupation.AIHEAD) {
            player1.kill();
        }

        if (newLocation1.getOccupation() == Occupation.POWERUP) {
            player1.addPowerUp(true);
            PowerUp = cube.resetPowerUp();
        }

        if (player1.isAlive()) {
            playerLocation1.setOccupation(Occupation.AITAIL);
            newLocation1.setOccupation(Occupation.AIHEAD);
            player1.setLocation(newLocation1);
            player1.setDirection(newDirection1);
        }

        if (newLocation2.getOccupation() == Occupation.AITAIL || newLocation2.getOccupation() == Occupation.AIHEAD) {
            player2.kill();
        }

        if (newLocation2.getOccupation() == Occupation.POWERUP) {
            player2.addPowerUp(true);
            PowerUp = cube.resetPowerUp();
        }

        if (player2.isAlive()) {
            playerLocation2.setOccupation(Occupation.AITAIL);
            newLocation2.setOccupation(Occupation.AIHEAD);
            player2.setLocation(newLocation2);
            player2.setDirection(newDirection2);
        }
    }

    /**
     * sets the occupation in terms of double values of the cubestate
     * within the field of vision.
     */
    private double[] convertToNNInput(NavPoint currentPoint, int direction, int fov) {
        NavPoint[] cubeState = getSurroundings(currentPoint, direction, fov);
        double[] inputValues = new double[cubeState.length + 1];

        for (int i = 0; i < cubeState.length; i++) {
            if (cubeState[i] == null) {
                inputValues[i] = 1;
            } else {
                switch (cubeState[i].getOccupation()) {
                    case EMPTY:
                        inputValues[i] = 0;
                        break;
                    case PLAYERTAIL:
                        inputValues[i] = -1;
                        break;
                    case AITAIL:
                        inputValues[i] = -1;
                        break;
                    case PLAYERHEAD:
                        inputValues[i] = -1;
                        break;
                    case AIHEAD:
                        inputValues[i] = -1;
                        break;
                    case POWERUP:
                        inputValues[i] = 1;
                        break;
                }
            }
        }
        inputValues[cubeState.length] = currentPoint.getAngleTo(PowerUp, direction);
        //testValues(inputValues, fov);
        return inputValues;
    }

    private void testValues(double[] inputValues, int fov) {
        for (int i = 0; i < inputValues.length; i++) {
            if (i % (fov * 2 + 1) == 0) {
                System.out.println();
            }
            System.out.print(inputValues[i]);
            System.out.print(" ");
        }
        System.out.println();
    }

    /**
     * This method is VERY nice. Have you ever noticed that the direction should change when going over an
     * edge of the cube? Well, that is where this method comes into place! Get your new, fresh direction here
     * for the price of O(1)! Very nice indeed.
     * For example: going from face 3 to 6, the direction changes from top(3) to bottom(1)
     * @param p1 is the position you are going away from
     * @param p2 is the position you are going towards
     * @param direction is the direction you are heading now, needed for some cases
     * @return the new direction if the two faces p1 and p2 are on, are different. The old direction if the two faces p1 and p2 are on, are similar.
     */
    public int getNewDirection(NavPoint p1, NavPoint p2, int direction) {
        if (p1.face != p2.face) {
            if (p1.face == 2) {
                if (p2.face == 3 || p2.face == 4) {
                    //direction changes in the logic from top(3) or bottom(1) to right(2)
                    return 2;
                }
                return direction;
            } else if (p1.face == 3) {
                if (p2.face == 4 || p2.face == 1) {
                    //direction does not change
                    return direction;
                } else {
                    //else the direction always changes to bottom(1)
                    return 1;
                }
            } else if (p1.face == 4) {
                if (p2.face == 3 || p2.face == 1) {
                    //direction does not change
                    return direction;
                } else {
                    //else the direction always changes to top(3)
                    return 3;
                }
            } else if (p1.face == 5) {
                if (p2.face == 3 || p2.face == 4) {
                    //direction changes in the logic from top(3) or bottom(1) to left(0)
                    return 0;
                }
                return direction;
            } else if (p1.face == 6) {
                if (p2.face == 3) {
                    return 1;
                } else if (p2.face == 4) {
                    return 3;
                }
                return direction;
            }
        }
        return direction;
    }

    /**
     * Method to generate the surroundings within the FOV around the AI's head
     * it generates in the following way:
     * first it analyses the axis 90 degrees from the direction where the AI is going
     * From there it looks to the neighbours above and below.
     */
    public NavPoint[] getSurroundings(NavPoint location, int direction, int FOV) {

        NavPoint[][] surroundings = new NavPoint[2 * FOV + 1][2 * FOV + 1];
        NavPoint[][] neighbours = cube.getNeighbours();

        NavPoint leftLocation = location;
        for (int i = 0; i < FOV; i++) {
            direction = (direction + 1) % 4;
            NavPoint newLocation = neighbours[leftLocation.getIndex()][direction];
            direction = ((((getNewDirection(leftLocation, newLocation, direction) - 1) % 4) + 4) % 4);
            leftLocation = newLocation;
        }
        int iIndex = 0;
        int jIndex = FOV;
        NavPoint yLocation = leftLocation;
        int baseDirection = direction;

        for (int i = 0; i < FOV * 2 + 1; i++) {

            surroundings[jIndex][iIndex] = yLocation;
            jIndex -= 1;

            //go up from the middle location
            for (int j = 0; j < FOV; j++) {
                NavPoint newLocation = neighbours[yLocation.getIndex()][direction];
                direction = getNewDirection(yLocation, newLocation, direction);
                yLocation = newLocation;
                surroundings[jIndex][iIndex] = yLocation;
                jIndex -= 1;
            }
            yLocation = leftLocation;
            direction = (baseDirection + 2) % 4;
            //go down from the middle location
            jIndex = FOV + 1;
            for (int j = 0; j < FOV; j++) {
                NavPoint newLocation = neighbours[yLocation.getIndex()][direction];
                direction = getNewDirection(yLocation, newLocation, direction);
                yLocation = newLocation;
                surroundings[jIndex][iIndex] = yLocation;
                jIndex += 1;
            }
            //move one to the right
            iIndex += 1;
            baseDirection = (((baseDirection - 1) % 4) + 4) % 4;
            NavPoint newLocation = neighbours[leftLocation.getIndex()][(baseDirection)];
            direction = getNewDirection(leftLocation, newLocation, baseDirection);

            //reset everything
            baseDirection = (direction + 1) % 4;
            direction = baseDirection;
            leftLocation = newLocation;
            yLocation = leftLocation;
            jIndex = FOV;
        }

        NavPoint[] surroundings1D = new NavPoint[(2 * FOV + 1) * (2 * FOV + 1)];

        int index = 0;
        for (int i = 0; i < 2 * FOV + 1; i++) {
            for (int j = 0; j < 2 * FOV + 1; j++) {
                surroundings1D[index] = surroundings[i][j];
                index++;
            }
        }
        return surroundings1D;
    }
}