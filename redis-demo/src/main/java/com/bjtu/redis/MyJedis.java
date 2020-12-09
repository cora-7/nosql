package com.bjtu.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyJedis {
    String key="totalCount"; //在线总人数
    private Jedis jedis;
    private static HashMap<String,HashMap> actions=new HashMap<>();
    private static HashMap<String,HashMap> counters=new HashMap<>();

    public MyJedis(){
        jedis=JedisInstance.getInstance().getResource();
        initActionsMap();
        initCountersMap();
    }
    public HashMap<String,HashMap> getActions(){
        return actions;
    }
    public HashMap<String,HashMap> getCounters(){
        return counters;
    }


    //增加在线人数
    public void addUser(String value){
        //获取当前时间
        Date date=new Date();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmm");
        String enterTime =dateFormat.format(date);
        SimpleDateFormat argformat=new SimpleDateFormat("HHmmss");
        String arg=argformat.format(date);
        int enterarg=Integer.parseInt(arg);

        System.out.println(enterTime+"时刻 进入用户"+value+"位");
        if(jedis.get(key)==null)
            jedis.set(key, "0");
        //对每条进入数据进行记录
        int num=Integer.parseInt(value);
            for(int i=0;i<num;i++){
                jedis.incr(key);
                jedis.rpush("enterList", enterTime); //存入list
                jedis.sadd("enterSet", enterTime); //存入set
                jedis.zadd("enterZset",enterarg,enterTime); //存入zset
            }
    }

    //减少在线人数
    public void delUser(String value){
        //获取当前时间
        Date date=new Date();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmm");
        String leaveTime =dateFormat.format(date);
        SimpleDateFormat argformat=new SimpleDateFormat("HHmmss");
        String arg=argformat.format(date);
        int leavearg=Integer.parseInt(arg);

        int num=Integer.parseInt(value); //减少用户数
        if(jedis.get(key)==null || Integer.parseInt(jedis.get(key))==0){
            System.out.println("当前无在线用户");
        }
        else if(Integer.parseInt(jedis.get(key))<num){
            System.out.println("离开用户数超过在线用户总数，操作失败");
        }
        else{
            System.out.println(leaveTime+"时刻 离开用户"+value+"位");
            //对每条离开数据进行记录
            for(int i=0;i<num;i++){
                jedis.decr(key);
                jedis.rpush("leaveList", leaveTime); //存入list
                jedis.sadd("leaveSet", leaveTime); //存入set
                jedis.zadd("leaveZset",leavearg,leaveTime); //存入zset
            }
        }
    }

    //显示在线人数
    public void showUserCount(){
        if(jedis.get(key)==null){
            System.out.println("当前无用户在线");
        }
        else{
            System.out.println("当前在线用户人数为："+jedis.get(key));
        }
    }

    //获取指定时段内的围观用户变动
    public void showUserFreq(String stime){
        String[] re = stime.split(" ");
        String begin=re[0];
        String end=re[1];
        long beginTime =Long.valueOf(begin), endTime =Long.valueOf(end);
        List<String> enterlist = jedis.lrange("enterList",0,-1); //进入list
        List<String> leavelist = jedis.lrange("leaveList",0,-1); //离开list

        if(endTime<beginTime){
            System.out.println("格式不合法,无法获取");
            return;
        }

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

        if(leavelist.size()==0)
            System.out.println("该时段内无用户离开");
        else{
            int leaveNum=0;
            long t;
            for(int i=0;i<leavelist.size();i++){
                t=Long.valueOf(leavelist.get(i));
                if(t>=beginTime&&t<=endTime){
                    System.out.println("时刻"+t+" 有用户离开");
                    leaveNum++;
                }
            }
            System.out.println(stime+"期间 离开用户"+leaveNum+"位");
        }
    }

    //获取并展示对应key的list
    public void showList(String listkey, boolean InOrOut){
        List<String> list = jedis.lrange(listkey,0,-1);
        String a="";
        if(InOrOut)
            a="进入";
        else
            a="离开";
        if(list.size()==0){
            System.out.println("所有时间内无用户"+a);
        }
        else{
            for(int i=0; i<list.size(); i++) {
                System.out.println("时刻"+list.get(i)+" 有用户"+a);
            }
        }
    }

    //获取并展示对应key的set
    public void setKeys(String setkey){
        Set<String> set = jedis.smembers(setkey);
        if(set.size()==0)
            System.out.println(setkey+"为空");
        else{
            System.out.println(setkey+"为：");
            System.out.println(set);
        }
    }
    //获取并展示按照key值排序后的zset
    public void zsetKeys(String zsetkey){
        Set<String> zset = jedis.zrange(zsetkey,0,-1);
        if(zset.size()==0)
            System.out.println(zsetkey+"为空");
        else{
            System.out.println(zsetkey+"为：");
            System.out.println(zset);
        }
    }

    public void deleteAllData()
    {
        jedis.flushAll();
        System.out.println("数据已清空！");
    }


    //读取json文件转化为string
    public static String JsonToString(String fileName){
        String jsonString = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            fileReader.close();
            jsonString = sb.toString();
            return jsonString;
        } catch (IOException e) {
            System.out.println("读取json文件出错");
            e.printStackTrace();
            return null;
        }
    }

    //获取actions
    public void initActionsMap(){
        if(actions!=null){
            actions.clear();
        }
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
                JSONObject o = (JSONObject) showArr.get(m);
                String show = (String) o.get("counterName");
                action.put("show", show);
            }
            JSONArray actArr = elem.getJSONArray("action");
            if (actArr != null) {
                for (int n = 0; n < actArr.size(); n++) {
                    JSONObject o = (JSONObject) actArr.get(n);
                    String operation = (String) o.get("counterName");
                    action.put("action", operation);
                }
            }
            actions.put(name, action);
        }
    }
    //获取counters
    public void initCountersMap(){
        if(counters!=null){
            counters.clear();
        }
        String s = JsonToString("src/main/resources/counters.json");
        JSONObject jsonobj = JSON.parseObject(s);
        JSONArray array = jsonobj.getJSONArray("counters");
        for (int i = 0; i < array.size(); i++) {
            JSONObject elem = (JSONObject) array.get(i);
            HashMap<String, String> counter = new HashMap<>();

            String counterName = (String) elem.get("counterName");
            counter.put("counterName", counterName);

            String counterIndex= (String) elem.get("counterIndex");
            counter.put("counterIndex", counterIndex);

            String type = (String) elem.get("type");
            counter.put("type", type);

            String keyFields = (String) elem.get("keyFields");
            counter.put("keyFields", keyFields);

            String valueFields = (String) elem.get("valueFields");
            if(valueFields!=null){
                counter.put("valueFields", valueFields);
            }

            String fields = (String) elem.get("fields");
            if(fields!=null){
                counter.put("fields", fields);
            }
            counters.put(counterName, counter);
        }
    }
}

