package software_analytics.group2.backend.utility;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListUtility {

    private ListUtility() {
    }

    /**
     * Method to check if there are new issue in the project update.
     *
     * @param oldList : list of already present issues.
     * @param newList : list of new issues found.
     * @return : a new merged lis if there are new issues, otherwise the old list.
     */
    public static <T> List<T> verifyMergeList(List<T> oldList, List<T> newList) {
        return (!newList.isEmpty()) ? mergeList(newList, oldList) : oldList;
    }

    /**
     * Method that merges the two given list.
     *
     * @param first  : first list to merge.
     * @param second : second list to merge.
     * @param <T>    : type of the lists.
     * @return : merged list.
     */
    private static <T> List<T> mergeList(List<T> first, List<T> second) {
        return Stream.of(first, second)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
