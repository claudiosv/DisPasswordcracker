import java.util.ArrayList;

public class Interval {
    public int intervalSize;
    public int lowerBound;
    public int upperBound;

    public Interval(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    //method used by the server to divide the interval
    public ArrayList<Interval> getIntervals(int problemSize, int intervalNumber){
        ArrayList<Interval> intervals = new ArrayList<>();
        int leftover = problemSize%intervalNumber; //should work
        if(leftover == 0 ){
            intervalSize = problemSize / intervalNumber;
            for (int i = 0; i < problemSize; i+= intervalSize) {
                intervals.add(new Interval(i, i + intervalSize));
            }
        }
        else {
            intervalSize = (problemSize - leftover) / intervalNumber;
            for (int i = 0; i < (problemSize - leftover); i+= intervalSize) {
                intervals.add(new Interval(i, i + intervalSize));
            }
            intervals.add(new Interval(problemSize - leftover, problemSize));
        }

        return intervals;
    }
}
