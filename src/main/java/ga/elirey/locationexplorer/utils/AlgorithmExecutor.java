package ga.elirey.locationexplorer.utils;

import ga.elirey.locationexplorer.basis.Localizable;

import java.util.List;

public interface AlgorithmExecutor<E extends Localizable> {

    List<E> apply(List<E> points);

    String getName();
}
