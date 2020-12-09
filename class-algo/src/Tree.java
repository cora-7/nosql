import java.util.ArrayList;

//先序遍历
class Preorder{
    public ArrayList<Integer> preorderTraverse(TreeNode root){
        ArrayList<Integer> tra =new ArrayList<>();
        traverse(root,tra);
        return tra;
    }

    //把root为根节点的preorder加入结果数组里
    public void traverse(TreeNode root, ArrayList<Integer> tra) {
        if(root==null) {
            return;
        }
        tra.add(root.value);//中
        traverse(root.left,tra);//左
        traverse(root.right,tra);//右
    }
}

//分治法
class Divcon{
    //从根结点开始先序遍历
    public ArrayList<Integer> preorderDivCon(TreeNode root){
        ArrayList<Integer> ar =new ArrayList<>();

        if(root==null) {
            return ar;
        }

        //分
        ArrayList<Integer> left = preorderDivCon(root.left);
        ArrayList<Integer> right = preorderDivCon(root.right);

        //治
        ar.add(root.value);
        ar.addAll(left);
        ar.addAll(right);
        return ar;
    }
}


class FindMinSumRoot{
    private int minSum;
    private TreeNode minRoot;
    public TreeNode findSubtree(TreeNode root) {
        minSum=Integer.MAX_VALUE;
        minRoot=null;
        getSum(root);
        return minRoot;
    }
    private int getSum(TreeNode root) {
        if(root==null) {
            return 0;
        }
        int sum=getSum(root.left)+getSum(root.right)+root.value;
        if(sum<minSum) {
            minSum=sum;
            minRoot=root;
        }

        return sum;
    }
}




