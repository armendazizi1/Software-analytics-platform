package software_analytics.group2.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Quartet<W, X, Y, Z> {

    private final W first;
    private final X second;
    private final Y third;
    private final Z fourth;
}

