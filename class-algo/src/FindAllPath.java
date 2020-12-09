import java.util.ArrayList;
import java.util.List;

public class FindAllPath {

    public static List<String> findAllPath(TreeNode root) {
        List<String> paths = new ArrayList<>();

        if(root == null) {
            return paths;
        }

        List<String> leftPaths = findAllPath(root.left);
        List<String> rightPaths = findAllPath(root.right);

        for(String path : leftPaths) {
            paths.add(root.value + "->" + path);
        }

        for(String path : rightPaths) {
            paths.add(root.value + "->" + path);
        }

        if(paths.isEmpty()) {
            paths.add("" + root.value);
        }

        return paths;
    }

}