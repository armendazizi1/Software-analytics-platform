package software_analytics.group2.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.model.file.DiffFileRename;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "fileDifferences")
public class CommitFilesDifference {

    @Id
    private String id;
    private List<String> added;
    private List<String> modified;
    private List<String> deleted;
    private List<DiffFileRename> renamed;
    private int addedLines;
    private int deletedLines;

    public CommitFilesDifference() {
        this.added = new ArrayList<>();
        this.modified = new ArrayList<>();
        this.deleted = new ArrayList<>();
        this.renamed = new ArrayList<>();
    }

    public CommitFilesDifference(List<String> added, List<String> modified, List<String> deleted,
                                 List<DiffFileRename> renamed) {
        this.added = added;
        this.modified = modified;
        this.deleted = deleted;
        this.renamed = renamed;
    }

    /**
     * @return list of all the added, deleted and modified files
     */
    public List<String> getAllFilesWithoutRename() {
        List<String> files = new ArrayList<>();
        files.addAll(added);
        files.addAll(modified);
        files.addAll(deleted);
        return files;
    }

    /**
     * @return list of all the files in the commits with the new path
     */
    public List<String> getAllFilesBeforeRename() {
        List<String> files = getAllFilesWithoutRename();
        renamed.stream().map(DiffFileRename::getTo).forEach(files::add);
        return files;
    }

    /**
     * @return list of all the files in the commits with the old path
     */
    public List<String> getAllFilesAfterRename() {
        List<String> files = getAllFilesWithoutRename();
        renamed.stream().map(DiffFileRename::getFrom).forEach(files::add);
        return files;
    }

    public int getAllModifiedSize() {
        return modified.size() + (int) renamed.stream().filter(l -> l.getSimilarity() < 100).count();
    }

    @Override
    public String toString() {
        return "CommitFilesDifference{" +
                "id='" + id + '\'' +
                ", added=" + added +
                ", modified=" + modified +
                ", deleted=" + deleted +
                ", renamed=" + renamed +
                ", addedLines=" + addedLines +
                ", deletedLines=" + deletedLines +
                '}';
    }
}
