package ga.elirey.locationexplorer.data;

import lombok.Value;

@Value
public class FilterOptions {

      boolean isPathOptimizerEnabled;
      int optimizationCoefficient;
      boolean isGpsJumpFilterEnabled;
      boolean isWayPointIncluded;

   public static FilterOptions useDefault(){
      return new FilterOptions(true, 3, true, false);
    }

    public FilterOptions(boolean isPathOptimizerEnabled, int optimizationCoefficient, boolean isGpsJumpFilterEnabled, boolean isWayPointIncluded) {
        this.isPathOptimizerEnabled = isPathOptimizerEnabled;
        this.optimizationCoefficient = optimizationCoefficient;
        this.isGpsJumpFilterEnabled = isGpsJumpFilterEnabled;
        this.isWayPointIncluded = isWayPointIncluded;
    }
}
