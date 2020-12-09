
package com.bjtu.redis;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import java.io.File;

public class FileListener implements FileAlterationListener {//文件监听类
    MyJedis mj;
    public  FileListener(MyJedis mj){
        this.mj=mj;
    }
    @Override
    public void onStart(FileAlterationObserver observer) {//文件初始化
        // System.out.println();
    }

    @Override
    public void onDirectoryCreate(File directory) {//有操作创建了新的文件夹
        //System.out.println("onDirectoryCreate:" + directory.getName());
    }

    @Override
    public void onDirectoryChange(File directory) {//有操作改变了新的文件夹
        //System.out.println("onDirectoryChange:" + directory.getName());
    }

    @Override
    public void onDirectoryDelete(File directory) {//有操作删除了文件夹
        //System.out.println("onDirectoryDelete:" + directory.getName());
    }

    @Override
    public void onFileCreate(File file) {//有操作新建了文件
        //System.out.println("onFileCreate" + file.getName());
    }

    @Override
    public void onFileChange(File file) {//有文件改变
        mj.initActionsMap();
        mj.initCountersMap();
        System.out.println(file.getName() + "文件被更改，正在重新读取......");
        System.out.println("重新读取成功！");
    }

    @Override
    public void onFileDelete(File file) {//有文件删除
        System.out.println(file.getName() + "文件被删除！");
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        //System.out.println("监听停止");
    }


}