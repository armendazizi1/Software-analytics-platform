package software_analytics.group2.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Triplet<X, Y, Z> {

    private final X first;
    private final Y second;
    private final Z third;

}


