package ga.elirey.locationexplorer.basis;

import java.util.OptionalDouble;
import java.util.OptionalLong;

public interface Traceable extends LocationFormat {

    /**
     * Get the start date of the trace
     *
     * @return optional timestamp as long
     */
    OptionalLong getStartDate();

    /**
     * Get the end date of the trace
     *
     * @return optional timestamp as long
     */
    OptionalLong getEndDate();

    /**
     * Difference between start date and end date as duration
     *
     * @return a duration as string
     */
    String getDuration();

    /**
     * Get the average speed
     *
     * @return average speed
     */
    OptionalDouble getAverageSpeed();

    /**
     * Get the average heading
     *
     * @return average heading
     */
    OptionalDouble getAverageHeading();

    /**
     * Get the average accuracy
     *
     * @return average accuracy
     */
    OptionalDouble getAverageAccuracy();
}
