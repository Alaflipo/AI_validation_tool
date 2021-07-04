package proc.sketches;
import processing.core.PApplet;

import java.io.IOException;

class NavPoint {
    private PApplet sketch;

    int[] location;     //Location of the NavPoint in the grid
    int size;           //Size of the rendered square (size x size)
    int face;           //Which face of the cube the NavPoint is on. {1,2,3,4,5,6}
    int faceEdge;       //The edge of the face this NavPoint is on.
    //12 indicates: on face 1 at the edge of 2.
    //123 indicates: on face 1 at the edge of 2 and the edge of 3
    //0 indicates that this face is not on an edge

    int index;          //Index of this point in the NavPoint Array
    int numOfNavPoints; //Total amount of NavPoints

    private Occupation occupation;

    //The following 2 values are used for rendering NavPoints in the correct place and are in alignment with these
    //values in the NavCube class.
    private int faceLength;
    private int faceSurface;

    /**
     * Create a NavPoint. Should only be done by NavCube!
     * @param loc location of this NavPoint.
     * @param size length of the side of the cell represented by this NavPoint.
     * @param face the face of the cube this NavPoint is on.
     * @param faceEdge which edge or corner this NavPoint is on.
     *                 0 means this points is not on any edge.
     *                 12 means this point is on face 1 at the edge with face 2.
     *                 123 means this point is on face 1 at the corner with faces 2 and 3.
     *                 Except for the first digit (the face), digits are increasing.
     * @param index Index of this point in the NavPoint Array.
     * @param numOfNavPoints total amount of NavPoints
     */
    public NavPoint(PApplet sketch, int[] loc, int size, int face, int faceEdge, int index, int numOfNavPoints){
        this.sketch = sketch;
        this.location = loc;
        this.size = size;
        this.face = face;
        this.faceEdge = faceEdge;
        this.occupation = Occupation.EMPTY;
        this.index = index;
        this.numOfNavPoints = numOfNavPoints;
        this.faceSurface = numOfNavPoints / 6;
        this.faceLength = (int) Math.sqrt(faceSurface);
    }

    public void showPoint() {
        sketch.rectMode(sketch.CENTER);
        sketch.fill(255);
        sketch.stroke(0);
        if (occupation == Occupation.AIHEAD || occupation == Occupation.AITAIL) {
            sketch.fill(0, 102, 153);
        }
        if (occupation == Occupation.PLAYERHEAD || occupation == Occupation.PLAYERTAIL) {
            sketch.fill(102, 0, 153);
        }
        if (occupation == Occupation.POWERUP) {
            sketch.fill(200, 0, 0);
        }
        sketch.rect(-1 * this.location[1], -1 * this.location[0], size, size);
        //fill(0, 102, 153);
        //text(this.index, -1 * this.location[1], -1 * this.location[0]);
    }

    /**
     * Sets the occupation of the NavPoint using an Enum.
     * Possible options are: EMPTY, PLAYERTAIL, PLAYERHEAD, AITAIL, AIHEAD, POWERUP
     * @param occupation the new occupation of the NavPoint
     */
    public void setOccupation(Occupation occupation){
        this.occupation = occupation;
    }

    /**
     * Gets the occupation of the NavPoint using an Enum.
     * Possible options are: EMPTY, PLAYERTAIL, PLAYERHEAD, AITAIL, AIHEAD, POWERUP
     */
    public Occupation getOccupation(){
        return this.occupation;
    }

    /**
     * Gets the face this NavPoint is on
     */
    public int getFace(){ return this.face;}

    /**
     * Gets the face length of the cube
     */
    public int getFaceLength(){ return this.faceLength;}

    /**
     * Gets the face size of the navPoint
     */
    public int getSize(){ return this.size;}

    public int getIndex() {return this.index; }

    public int getLocation(int dimension) {
        return location[dimension];
    }

    public double getAngleTo(NavPoint other, int direction) {
        int[] base;
        switch (direction) {
            case 0:
                base = new int[]{-1,0};
                break;
            case 1:
                base = new int[]{0,-1};
                break;
            case 2:
                base = new int[]{1,0};
                break;
            case 3:
                base = new int[]{0,1};
                break;
            default:
                base = new int[]{-1,0};
        }
        int[] dir = new int[2];
        dir[0] = other.location[0] - this.location[0];
        dir[1] = other.location[1] - this.location[1];

        double angle = Math.acos((base[0] * dir[0] + base[1] * dir[1]) / (Math.sqrt(base[0]*base[0] + base[1]*base[1]) * Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1])));
        return angle;
    }

    /**
     * Gets the face surface of the cube
     */
    public int getFaceSurface() {
        return faceSurface;
    }
}