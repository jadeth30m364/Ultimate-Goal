package org.firstinspires.ftc.teamcode.Auto.vision;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;
/* This is the pipeline
 * A pipeline takes the video frame from the camera, and processes the frames
 * Then it returns an image that can be displayed on the robot controller's screen
 * You can also add rectangles to the image sent to the robot controller
 */

enum stack_pos{
    NONE, ONE, FOUR,
}

public class StackDetector extends OpenCvPipeline {
    private Mat workingMatrix = new Mat();
    private stack_pos stackPos = stack_pos.NONE;
    private Telemetry t;
    private final Rect firstRect = new Rect(
            new Point(120, 0),
            new Point(150, 30)
    );

    private final Rect secondRect = new Rect(
            new Point(120, 30),
            new Point(150, 60)
    );

    private final Rect thirdRect = new Rect(
            new Point(120, 60),
            new Point(150, 90)
    );

    private final double threshold_value = 0.9;

    public StackDetector(Telemetry t){
        t = this.t;
    }

    private final Scalar donut_found = new Scalar(0, 255, 0);
    private final Scalar donut_not_found = new Scalar(255, 0, 0);

    @Override
    // A matix of pixels (the picture the camera takes)
    public final Mat processFrame(Mat input){
        input.copyTo(workingMatrix);

        if(workingMatrix.empty()){
            return input;
        }

        Imgproc.cvtColor(workingMatrix, workingMatrix, Imgproc.COLOR_RGB2HSV); //Looking for a color range
        //Define the range of colors we are looking for
        Scalar lowHSV = new Scalar(62, 32, 100); //Hue, Saturation, Values
        Scalar highHSV = new Scalar(54, 100, 29);

        //Thresholding the image, only show yellow and nothing else (Grayscaled)
        Core.inRange(workingMatrix, lowHSV, highHSV, workingMatrix);

        /* Create a submatrix
         * These are not the real numbers, need to find real numbers later
         * (rowStart, rowEnd, colStart, colEnd) creates a square
         * We are trying to find the hight of the start stack so I made 3 submatrix stacked on top of each other to measure the hight
         */
        Mat firstSubMat = workingMatrix.submat(firstRect);
        Mat secondSubMatrix = workingMatrix.submat(secondRect);
        Mat thirdSubMartix = workingMatrix.submat(thirdRect);



        // Sum of the matix to find where the yello ends

        // sumElems creates the sum of every matrix value, val[0] gets the yellow(white) value
        double firstSubMatTotal = Core.sumElems(firstSubMat).val[0];
        double secondSubMatrixTotal = Core.sumElems(secondSubMatrix).val[0];
        double thirdSubMartixTotal = Core.sumElems(thirdSubMartix).val[0];

        //Get the percentage of yellow in the submatrix
        //255 is the max value of a grayscaled pixel
        double percentInFSM = firstSubMatTotal/firstRect.area()/255;
        double percentInSSM = secondSubMatrixTotal/secondRect.area()/255;
        double percentInTSM = thirdSubMartixTotal/thirdRect.area()/255;

        //Compare the color of each submatrix


        if(percentInTSM > threshold_value){
            stackPos = stack_pos.FOUR;
            Imgproc.rectangle(workingMatrix, firstRect, donut_found);
            Imgproc.rectangle(workingMatrix, secondRect, donut_found);
            Imgproc.rectangle(workingMatrix, thirdRect, donut_found);
        }else if(percentInFSM > percentInSSM){
            stackPos = stack_pos.ONE;
            Imgproc.rectangle(workingMatrix, firstRect, donut_found);
            Imgproc.rectangle(workingMatrix, secondRect, donut_not_found);
            Imgproc.rectangle(workingMatrix, thirdRect, donut_not_found);
        }else{
            stackPos = stack_pos.NONE;
            Imgproc.rectangle(workingMatrix, firstRect, donut_not_found);
            Imgproc.rectangle(workingMatrix, secondRect, donut_not_found);
            Imgproc.rectangle(workingMatrix, thirdRect, donut_not_found);
        }
        t.addData("Top raw value: ", firstSubMatTotal);
        t.addData("Middle raw value: ", secondSubMatrixTotal);
        t.addData("Bottom raw value: ", thirdSubMartixTotal);
        t.addData("Top percentage: ", percentInFSM + "%");
        t.addData("Middle percentage: ", percentInSSM + "%");
        t.addData("Bottom percentage: ", percentInTSM + "%");
        t.addData("Stack postion: ", stackPos);
        t.update();

        firstSubMat.release();
        secondSubMatrix.release();
        thirdSubMartix.release();

        // OpenCV returns an image with the matrix on it
        return workingMatrix;
    }

    public stack_pos getStackPos(){
        return stackPos;
    }
}
