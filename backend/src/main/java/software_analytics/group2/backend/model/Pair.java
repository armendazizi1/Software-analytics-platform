package software_analytics.group2.backend.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<X, Y> {

    private final X left;
    private final Y right;

}
