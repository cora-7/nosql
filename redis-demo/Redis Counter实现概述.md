# Redis Counter

### 18301140 徐奕珂



## 一、要求实现

- 常用操作有封装（MyJedis.java）

  ```java
  public void addUser(String value){
  	jedis.incr(key);
      jedis.rpush("enterList", enterTime);
      jedis.sadd("enterSet", enterTime);
      jedis.zadd("enterZset",enterarg,enterTime);
  }
  
  public void delUser(String value){
  	jedis.decr(key);
      jedis.rpush("enterList", enterTime);
      jedis.sadd("enterSet", enterTime);
      jedis.zadd("enterZset",enterarg,enterTime);
  }
  
  public void showUserCount(){
      jedis.get(key);
  }
  
  public void showList(String listkey, boolean InOrOut){
      List<String> list = jedis.lrange(listkey,0,-1);
  }
  
  public void setKeys(String setkey){
      Set<String> set = jedis.smembers(setkey);
  }
  
  public void zsetKeys(String zsetkey){
      Set<String> zset = jedis.zrange(zsetkey,0,-1);
  }
  
  public void deleteAllData()
  {
      jedis.flushAll();
  }
  ```

  

- 完成了freq周期统计的功能（MyJedis.java）

  ```java
  public void showUserFreq(String stime){
  	String[] re = stime.split(" ");
      String begin=re[0];
      String end=re[1];
      long beginTime =Long.valueOf(begin), endTime =Long.valueOf(end);
      List<String> enterlist = jedis.lrange("enterList",0,-1); //进入list
      List<String> leavelist = jedis.lrange("leaveList",0,-1); //离开list
  
      System.out.println("在"+beginTime+"-"+endTime+"时段内:");
      if(enterlist.size()==0)
      	System.out.println("该时段内无用户进入");
      else{
          int enterNum=0;
          long t;
          for(int i=0;i<enterlist.size();i++){
              t=Long.valueOf(enterlist.get(i));
              if(t>=beginTime&&t<=endTime){
              System.out.println("时刻"+t+" 有用户进入");
              enterNum++;
          	}
  		}
      	System.out.println(stime+"期间 进入用户"+enterNum+"位");
      }
      //以上为enterlist的统计过程，leavelist的freq周期统计同理
  }
  ```

  

- 完成了counter的json定义（counters.json, actions.json）

  ```json
    "counters": [
      {
        "counterName": "addUser",
        "counterIndex": "1",
        "type": "num",
        "keyFields": "userCount",
        "valueFields": "1"
      }
      ...]
      "actions": [
      {
        "name": "ADD_USER",
        "actionIndex": 1,
        "describe": "人数增加",
        "show": [
          {
            "counterName": "showUserCount"
          }
        ]
        "action": [
          {
            "counterName": "addUser"
          }
          ]
      }
  	...]
  ```

  

- 完成了json的读取操作（MyJedis.java）

  ```java
  //读取json文件转化为string
  public static String JsonToString(String fileName){
  	...
      }
  
  //获取actions
  public void initActionsMap(){
      String s = JsonToString("src/main/resources/actions.json");
      JSONObject jsonobj = JSON.parseObject(s);
      JSONArray array = jsonobj.getJSONArray("actions");
  
      for (int i = 0; i < array.size(); i++) {
  		JSONObject elem = (JSONObject) array.get(i);
  		HashMap<String, String> action = new HashMap<>();
          String name = (String) elem.get("name");
          action.put("name", name);
          String describe = (String) elem.get("describe");
          action.put("describe", describe);
  
          JSONArray showArr = elem.getJSONArray("show");
          for (int m = 0; m < showArr.size(); m++) {
          	...
              action.put("show", show);
          }
          JSONArray actArr = elem.getJSONArray("action");
  		if (actArr != null) {
           	...
              action.put("action", operation);
      	}
          actions.put(name, action);
      }
      //获取counters同理
      ...
  }
  ```

  

- 有服务的client的调用（JedisInstance.java）

  ```java
      public static void showChoice(){
          System.out.println("");
          System.out.println("==========操作选项==========");
          System.out.println("1.增加在线用户");
          System.out.println("2.减少在线用户");
          System.out.println("3.获取当前总人数");
          System.out.println("4.获取时段内用户变化");
          System.out.println("5.查看Set");
          System.out.println("6.查看ZSet");
          System.out.println("7.删除所有数据");
          System.out.println("0.退出程序");
          System.out.println("请输入对应的操作序号：");
      }
  	Scanner Input=new Scanner(System.in);
      String choice=Input.nextLine();
  ```
  
  
  
- 能够在程序不停止运行的情况下自适应json文件的变化（FileMonitor.java, FileListener.java）

  ```java
  public class FileMonitor {
      FileAlterationMonitor monitor;
      public FileMonitor(long interval) {
          monitor = new FileAlterationMonitor(interval);
      }
  
      public void monitor(String path, FileAlterationListener listener) {//给文件添加监听
          FileAlterationObserver observer = new FileAlterationObserver(new File(path));
          monitor.addObserver(observer);
          observer.addListener(listener);
      }
      ...
  }
  
  public class FileListener implements FileAlterationListener {
      MyJedis mj;
      public  FileListener(MyJedis mj){
          this.mj=mj;
      }
      
      @Override
      public void onFileChange(File file) {//有文件改变
          mj.initActionsMap();
          mj.initCountersMap();
          System.out.println(file.getName() + "文件被更改，正在重新读取......");
          System.out.println("重新读取成功！");
      }
  }
  ```



## 二、操作调用

本计数器实现设定场景为直播间在线人数统计，为模拟实际情景涉及的需求可能，共提供8项操作，分别为：

1. 增加在线用户
2. 减少在线用户
3. 获取当前总人数
4. 获取时段内用户变化
5. 查看Set
6. 查看ZSet
7. 删除所有数据
8. 退出程序（对应序号为0）

通过在控制台输入对应操作序号，即可调用相关函数实行对应操作，从而完成对计数器功能实现的测试。

所调用的相关函数在MyJedis.java中完成实现。

```java
        while (!choice.equals("0")) //退出程序
        {
            if(choice.equals("1")) //增加在线用户
            {
                action=actionsMap.get("ADD_USER");
                String actName=(String)action.get("action");
                counter=countersMap.get(actName);
                String num= (String)counter.get("valueFields");
                myjedis.addUser(num);
                myjedis.showUserCount(); //显示总人数
            }
            else if(choice.equals("2")) //减少在线用户
            {
                action=actionsMap.get("DEL_USER");
                String actName=(String)action.get("action");
                counter=countersMap.get(actName);
                String num=(String)counter.get("valueFields");
                myjedis.delUser(num);
                myjedis.showUserCount(); //显示总人数
            }
            else if(choice.equals("3")){ //获取当前总人数
                myjedis.showUserCount(); //显示总人数
            }
            else if(choice.equals("4")) //获取时段内用户变化
            {
                //查看List
                System.out.println("在所有时间内");
                System.out.println("enterList为：");
                myjedis.showList("enterList",true);
                System.out.println("leaveList为：");
                myjedis.showList("leaveList",false);

                System.out.println("");
                action=actionsMap.get("SHOW_USER_FREQ");
                String actName=(String)action.get("show");
                counter=countersMap.get(actName);
                String period=(String)counter.get("valueFields");
                myjedis.showUserFreq(period);
            }
            else if(choice.equals("5")) //查看Set
            {
                myjedis.setKeys("enterSet");
                myjedis.setKeys("leaveSet");
            }
            else if(choice.equals("6")) //查看ZSet
            {
                myjedis.zsetKeys("enterZset");
                myjedis.zsetKeys("leaveZset");
            }
            else if(choice.equals("7")) //删除所有数据
            {
                myjedis.deleteAllData();
            }
            else
                System.out.println("输入不合法，重新选择操作！");
            showChoice();
            choice=Input.nextLine();
        }
```

