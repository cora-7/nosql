
public class TreeNode {
    public TreeNode left;
    public TreeNode right;
    public int value;

    public TreeNode() {
    }
    public TreeNode(int v) {
        this.value = v;
    }
    public TreeNode(TreeNode l,TreeNode r,int v){
        this.left=l;
        this.right=r;
        this.value=v;
    }
}