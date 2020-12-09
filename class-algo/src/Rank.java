import java.util.Arrays;

public class Rank {

    public static String[] reRank(String[] input) {
        if(input==null||input.length==0)
        {
            return new String[0];
        }
        int i=0;
        int len=input.length;
        int r=0,b=0,g=0;
        for(i=0;i<len;++i)
        {
            if(input[i].equals("r"))r++;
            else if(input[i].equals("g"))g++;
            else if(input[i].equals("b"))b++;
        }

        for(i=0;i<r;++i) input[i]="r";
        for(i=r;i<r+g;++i) input[i]="g";
        for(i=r+g;i<len;++i) input[i]="b";
        return input;
    }
    public static String[] reRankTwoPointers(String[] input) {
        if(input==null||input.length==0)
        {
            return new String[0];
        }
        int i=0;
        int left=0,right=input.length-1;
        while(i<=right)
        {
            if(input[i].equals("r"))
            {
                String temp=input[i];
                input[i]=input[left];
                input[left]=temp;
                i++;left++;
            }
            else if(input[i].equals("g"))
            {
                i++;
            }
            else if(input[i].equals("b"))
            {
                String temp2=input[i];
                input[i]=input[right];
                input[right]=temp2;
                right--;
            }
        }
        return input;
    }
    public static void main(String[] args) {
        String[] input=new String[]{"r","r","b","g","b","r","g"};
        Arrays.asList(reRank(input)).forEach(e->System.out.print(e));
        System.out.println();
        input=new String[]{"r","r","b","g","b","r","g"};
        Arrays.asList(reRankTwoPointers(input)).forEach(e->System.out.print(e));
    }
}