package software_analytics.group2.backend.model.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiffFileRename {
    String from;
    String to;
    int similarity;
}

