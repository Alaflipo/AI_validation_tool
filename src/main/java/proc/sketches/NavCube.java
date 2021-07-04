package proc.sketches;

import processing.core.PApplet;

import java.util.Random;                //for generation random numbers

class NavCube {
    private PApplet sketch;

    int size;                    //Size of the cube: (2*size + 1)^3
    int faceLength; //Length of the face of a cube (face is a square (faceSize * faceSize)
    int faceSurface; //Surface area of a surface, as the square (faceSize * faceSize)
    int navPointSize;            //Size of the NavPoint squares

    NavPoint[] navPoints;    //Array of all NavPoints in the game.
    //Face 1 has indices [0,8], 2 has [9,17] ... 6 has [45,53]]
    NavPoint[][] neighbours; //Array of NavPoints, where [i][j] is point [j], which is a neighbour of [i]
    //i in range [0, lastNavPoint]
    //j in range [0,3], where {0,1,2,3} == {left, bottom, right, top}

    /**
     * Create a cube. The volume is (2 * size + 1)^3. Its face length is 2 * size + 1.
     * Its face surface is (face length) * (face length).
     * @param size size of the cube as described above
     * @param navPointSize the length of the cell of every NavPoint,
     *                     also the manhattan distance between two NavPoints
     */
    public NavCube(PApplet sketch, int size, int navPointSize){
        this.sketch = sketch;
        this.size = size;
        this.navPointSize = navPointSize;
        faceLength = 2 * size + 1;
        faceSurface = faceLength * faceLength;
        navPoints = new NavPoint[6 * faceSurface];
        neighbours = new NavPoint[6 * faceSurface][4];
    }

    /**
     * Call this to generate the navigation cube in the game.
     * Goes over all 6 faces and generate them with NavPoints.
     * Afterwards, connects all these points to their immediate neighbours
     */
    void generateCube(){
        /* Generated cube is as follows:
            -----
            | 3 |
        -----------------
        | 2 | 1 | 5 | 6 |
        -----------------
            | 4 |
            -----*/

        //Generate the front face, at face location (0,0)
        int[] initialLoc = new int[]{0,0};
        generateFace(initialLoc, 1, 123, 12, 124, 14, 145, 15, 135, 13);

        //Generate the left face, at face location (-1,0)
        initialLoc[1] = faceLength * navPointSize;
        generateFace(initialLoc, 2, 236, 26, 246, 24, 214, 21, 213, 23);

        //Generate the top face, at face location (0,1)
        initialLoc[1] = 0;
        initialLoc[0] = faceLength * navPointSize;
        generateFace(initialLoc, 3, 326, 32, 312, 31, 315, 35, 356, 36);

        //Generate the bottom face, at face location (0,-1)
        initialLoc[0] = - faceLength * navPointSize;
        generateFace(initialLoc, 4, 412, 42, 426, 46, 456, 45, 415, 41);

        //Generate the right face, at face location (1,0)
        initialLoc[1] = - faceLength * navPointSize;
        initialLoc[0] = 0;
        generateFace(initialLoc, 5, 513, 51, 514, 54, 546, 56, 536, 53);

        //Generate the back face, at face location (2,0)
        initialLoc[1] = -2 * faceLength * navPointSize;
        generateFace(initialLoc, 6, 635, 65, 645, 64, 624, 62, 623, 63);

        //connect all NavPoints to their neighbours
        connectNeighbours();
    }

    /**
     * Generates a face of the cube. Only to be used in generateCube()!
     * NavPoints are placed with a specific interval, dependent on their size, on a face.
     * For points at an edge, the specific edge(s) is/are given (as a number xyz, with x face and y < z).

     * @param loc the location of the face
     * @param face the face in question
     * @param tlc top left corner
     * @param le  left edge
     * @param blc bottom left corner
     * @param be  bottom edge
     * @param brc bottom right corner
     * @param re  right edge
     * @param trc top right corner
     * @param te  top edge
     */
    void generateFace(int[] loc, int face, int tlc, int le, int blc, int be, int brc, int re, int trc, int te){
        /* NavPoints are inserted as follows:
         ---------
         | 1 2 3 |
         | 4 5 6 |
         | 7 8 9 |
         ---------*/

        int nextIndex = (face - 1) * faceSurface;
        //Generate a NavPoint on the face in regular intervals. From top left to the right and then down
        for (int i = size; i >= -size; i--){        //i++ -> next row    (so y-axis)
            for (int j = size; j >= -size; j--){    //j++ -> next column (so x-axis)
                int[] location = new int[]{i * navPointSize + loc[0], j * navPointSize + loc[1], 0};
                NavPoint nav;

                //TopLeft Corner
                if (i == size && j == size){
                    nav = new NavPoint(sketch, location, navPointSize, face, tlc, nextIndex, 6 * faceSurface);
                }

                //BottomLeft Corner
                else if (i == -size && j == size){
                    nav = new NavPoint(sketch, location, navPointSize, face, blc, nextIndex, 6 * faceSurface);
                }

                //BottomRight Corner
                else if (i == -size && j == -size){
                    nav = new NavPoint(sketch, location, navPointSize, face, brc, nextIndex, 6 * faceSurface);
                }

                //TopRight Corner
                else if (i == size && j == -size){
                    nav = new NavPoint(sketch, location, navPointSize, face, trc, nextIndex, 6 * faceSurface);
                }

                //Left Edge
                else if (j == size){
                    nav = new NavPoint(sketch, location, navPointSize, face, le, nextIndex, 6 * faceSurface);
                }

                //Right Edge
                else if (j == -size){
                    nav = new NavPoint(sketch, location, navPointSize, face, re, nextIndex, 6 * faceSurface);
                }

                //Top Edge
                else if (i == size){
                    nav = new NavPoint(sketch, location, navPointSize, face, te, nextIndex, 6 * faceSurface);
                }

                //Bottom Edge
                else if (i == -size){
                    nav = new NavPoint(sketch, location, navPointSize, face, be, nextIndex, 6 * faceSurface);
                }

                //No edge
                else {
                    nav = new NavPoint(sketch, location, navPointSize, face, 0, nextIndex, 6 * faceSurface);
                }

                navPoints[nextIndex] = nav;
                nextIndex++;
            }
        }
    }

    public void addPlayer(NavPoint location) {
        location.setOccupation(Occupation.AIHEAD);
    }

    public NavPoint insertPowerUp() {
        Random r = new Random();
        boolean foundEmpty = false;
        int randInt = 0;
        while (!foundEmpty) {
            randInt = r.nextInt(navPoints.length);
            if (navPoints[randInt].getOccupation() == Occupation.EMPTY) {
                navPoints[randInt].setOccupation(Occupation.POWERUP);
                foundEmpty = true;
            }
        }
        return navPoints[randInt];
    }

    public NavPoint resetPowerUp() {
        for(int i = 0; i < navPoints.length; i++) {

            if (navPoints[i].getOccupation() == Occupation.POWERUP) {
                navPoints[i].setOccupation(Occupation.EMPTY);
            }
        }
        return insertPowerUp();
    }

    /**
     * Connects all the created NavPoints to their neighbours.
     * Only to be used in generateCube!
     * Neighbours are directly neighbouring NavPoints above, below and to the left and right side of this NavPoint.
     * Edge cases are included, where a point on a certain edge is an edge case.
     */
    void connectNeighbours(){
        //Go over every created navPoint
        for (int i = 0; i < navPoints.length; i++){

            //Find the left neighbour in case the point is on some left edge
            //Face 1
            if (navPoints[i].faceEdge == 12 || navPoints[i].faceEdge == 123 || navPoints[i].faceEdge == 124){
                neighbours[i][0] = navPoints[i + faceSurface + faceLength - 1];
            }

            //Face 2
            else if (navPoints[i].faceEdge == 26 || navPoints[i].faceEdge == 236 || navPoints[i].faceEdge == 246){
                neighbours[i][0] = navPoints[i + 4 * faceSurface + faceLength - 1];
            }

            //Face 3
            else if (navPoints[i].faceEdge == 32 || navPoints[i].faceEdge == 326 || navPoints[i].faceEdge == 312){
                neighbours[i][0] = navPoints[faceSurface + ((i % faceSurface) / faceLength)];
            }

            //Face 4
            else if (navPoints[i].faceEdge == 42 || navPoints[i].faceEdge == 412 || navPoints[i].faceEdge == 426){
                neighbours[i][0] = navPoints[i - faceSurface - 1 - (i % faceSurface) - ((i % faceSurface) / faceLength)];
            }

            //Face 5
            else if (navPoints[i].faceEdge == 51 || navPoints[i].faceEdge == 513 || navPoints[i].faceEdge == 514){
                neighbours[i][0] = navPoints[i - 4 * faceSurface + faceLength - 1];
            }

            //Face 6
            else if (navPoints[i].faceEdge == 65 || navPoints[i].faceEdge == 635 || navPoints[i].faceEdge == 645){
                neighbours[i][0] = navPoints[i - faceSurface + faceLength - 1];
            }

            //Not on an edge
            else {
                neighbours[i][0] = navPoints[i - 1];
            }

            //Find the bottom neighbour in case the point is on some bottom edge
            //Face 1
            if (navPoints[i].faceEdge == 14 || navPoints[i].faceEdge == 145 || navPoints[i].faceEdge == 124){
                neighbours[i][1] = navPoints[i + 2 * faceSurface + faceLength];
            }

            //Face 2
            else if (navPoints[i].faceEdge == 24 || navPoints[i].faceEdge == 214 || navPoints[i].faceEdge == 246){
                neighbours[i][1] = navPoints[i + 2 * faceSurface - ((i % faceLength) * (faceLength + 1))];
            }

            //Face 3
            else if (navPoints[i].faceEdge == 31 || navPoints[i].faceEdge == 315 || navPoints[i].faceEdge == 312){
                neighbours[i][1] = navPoints[i % faceLength];
            }

            //Face 4
            else if (navPoints[i].faceEdge == 46 || navPoints[i].faceEdge == 456 || navPoints[i].faceEdge == 426){
                neighbours[i][1] = navPoints[6 * faceSurface - 1 - (i % faceLength)];
            }

            //Face 5
            else if (navPoints[i].faceEdge == 54 || navPoints[i].faceEdge == 546 || navPoints[i].faceEdge == 514){
                neighbours[i][1] = navPoints[i - faceSurface - (faceLength - 1 - (i % faceLength)) * (faceLength - 1)];
            }

            //Face 6
            else if (navPoints[i].faceEdge == 64 || navPoints[i].faceEdge == 624 || navPoints[i].faceEdge == 645){
                neighbours[i][1] = navPoints[4 * faceSurface - 1 - (i % faceLength)];
            }

            //Not on an edge
            else {
                neighbours[i][1] = navPoints[i + faceLength];
            }

            //Find the right neighbour in case the point is on some right edge
            //Face 1
            if (navPoints[i].faceEdge == 15 || navPoints[i].faceEdge == 145 || navPoints[i].faceEdge == 135){
                neighbours[i][2] = navPoints[i + 4 * faceSurface - faceLength + 1];
            }

            //Face 2
            else if (navPoints[i].faceEdge == 21 || navPoints[i].faceEdge == 214 || navPoints[i].faceEdge == 213){
                neighbours[i][2] = navPoints[i - faceSurface - faceLength + 1];
            }

            //Face 3
            else if (navPoints[i].faceEdge == 35 || navPoints[i].faceEdge == 315 || navPoints[i].faceEdge == 356){
                neighbours[i][2] = navPoints[i + 2 * faceSurface - (((i % faceSurface) / faceLength) * (faceLength + 1))];
            }

            //Face 4
            else if (navPoints[i].faceEdge == 45 || navPoints[i].faceEdge == 456 || navPoints[i].faceEdge == 415){
                neighbours[i][2] = navPoints[i + faceSurface + ((faceLength - 1 - ((i % faceSurface) / faceLength)) * (faceLength - 1))];
            }

            //Face 5
            else if (navPoints[i].faceEdge == 56 || navPoints[i].faceEdge == 546 || navPoints[i].faceEdge == 536){
                neighbours[i][2] = navPoints[i + faceSurface - faceLength + 1];
            }

            //Face 6
            else if (navPoints[i].faceEdge == 62 || navPoints[i].faceEdge == 624 || navPoints[i].faceEdge == 623){
                neighbours[i][2] = navPoints[i - 4 * faceSurface - faceLength + 1];
            }

            //Not on an edge
            else {
                neighbours[i][2] = navPoints[i + 1];
            }

            //Find the top neighbour in case the point is on some top edge
            //Face 1
            if (navPoints[i].faceEdge == 13 || navPoints[i].faceEdge == 123 || navPoints[i].faceEdge == 135){
                neighbours[i][3] = navPoints[i + 3 * faceSurface - faceLength];
            }

            //Face 2
            else if (navPoints[i].faceEdge == 23 || navPoints[i].faceEdge == 213 || navPoints[i].faceEdge == 236){
                neighbours[i][3] = navPoints[2 * faceSurface + (faceLength * (i % faceSurface))];
            }

            //Face 3
            else if (navPoints[i].faceEdge == 36 || navPoints[i].faceEdge == 326 || navPoints[i].faceEdge == 356){
                neighbours[i][3] = navPoints[5 * faceSurface + faceLength - 1 - (i % faceSurface)];
            }

            //Face 4
            else if (navPoints[i].faceEdge == 41 || navPoints[i].faceEdge == 412 || navPoints[i].faceEdge == 415){
                neighbours[i][3] = navPoints[faceSurface - faceLength + (i % faceSurface)];
            }

            //Face 5
            else if (navPoints[i].faceEdge == 53 || navPoints[i].faceEdge == 536 || navPoints[i].faceEdge == 513){
                neighbours[i][3] = navPoints[3 * faceSurface - 1 - (faceLength * (i % faceSurface))];
            }

            //Face 6
            else if (navPoints[i].faceEdge == 63 || navPoints[i].faceEdge == 635 || navPoints[i].faceEdge == 623){
                neighbours[i][3] = navPoints[2 * faceSurface + faceLength - 1 - (i % faceSurface)];
            }

            //Not on an edge
            else {
                neighbours[i][3] = navPoints[i - faceLength];
            }
        }
    }

    /**
     * Used to display the cube to the screen using processing methods
     */
    public void drawCube() {
        sketch.pushMatrix();
        sketch.translate(faceLength * navPointSize * (float)0.5, faceLength * navPointSize * (float)0.5);
        sketch.translate(faceLength * navPointSize, faceLength * navPointSize);
        for (int i = 0; i < navPoints.length; i++) {
            navPoints[i].showPoint();
        }
        sketch.popMatrix();
    }

    public NavPoint[] getNavPoints() {
        return navPoints;
    }
    public NavPoint[][] getNeighbours() {
        return neighbours;
    }

}