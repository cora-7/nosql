package com.bjtu.redis;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Scanner;

/**
 *  SpringBootApplication
 * 用于代替 @SpringBootConfiguration（@Configuration）、 @EnableAutoConfiguration 、 @ComponentScan。
 * <p>
 * SpringBootConfiguration（Configuration） 注明为IoC容器的配置类，基于java config
 * EnableAutoConfiguration 借助@Import的帮助，将所有符合自动配置条件的bean定义加载到IoC容器
 * ComponentScan 自动扫描并加载符合条件的组件
 */
@SpringBootApplication
public class RedisDemoApplication {

    public static void main(String[] args) throws Exception{
        //SpringApplication.run(RedisDemoApplication.class, args);
        MyJedis myjedis=new MyJedis();
        //设置文件监听
        FileMonitor fm = new FileMonitor(500);//设置监听周期
        fm.monitor("src/main/resources", new FileListener(myjedis)); //指定文件夹添加监听
        fm.start(); //启动监听

        //获取action和counter的hashmap
        HashMap<String,HashMap> actionsMap=myjedis.getActions();
        HashMap<String,HashMap> countersMap=myjedis.getCounters();
        //System.out.println(actionsMap);
        //System.out.println(countersMap);
        HashMap<String,String> action,counter;

        System.out.println("Redis Counter-18301140 徐奕珂");
        showChoice();
        Scanner Input=new Scanner(System.in);
        String choice=Input.nextLine();

        while (!choice.equals("0")){
            if(choice.equals("1"))
            {
                action=actionsMap.get("ADD_USER");
                String actName=(String)action.get("action");
                counter=countersMap.get(actName);
                String num= (String)counter.get("valueFields");
                myjedis.addUser(num);
                myjedis.showUserCount(); //显示总人数
            }
            else if(choice.equals("2"))
            {
                action=actionsMap.get("DEL_USER");
                String actName=(String)action.get("action");
                counter=countersMap.get(actName);
                String num=(String)counter.get("valueFields");
                myjedis.delUser(num);
                myjedis.showUserCount(); //显示总人数
            }
            else if(choice.equals("3")){
                myjedis.showUserCount(); //显示总人数
            }
            else if(choice.equals("4"))
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
            else if(choice.equals("5"))
            {
                myjedis.setKeys("enterSet");
                myjedis.setKeys("leaveSet");
            }
            else if(choice.equals("6"))
            {
                myjedis.zsetKeys("enterZset");
                myjedis.zsetKeys("leaveZset");
            }
            else if(choice.equals("7"))
            {
                myjedis.deleteAllData();
            }
            else
                System.out.println("输入不合法，重新选择操作！");
            showChoice();
            choice=Input.nextLine();
        }
    }

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
}

/*

总结：

1、获取运行环境信息和回调接口。例如ApplicationContextIntializer、ApplicationListener。
完成后，通知所有SpringApplicationRunListener执行started()。

2、创建并准备Environment。
完成后，通知所有SpringApplicationRunListener执行environmentPrepared()

3、创建并初始化 ApplicationContext 。例如，设置 Environment、加载配置等
完成后，通知所有SpringApplicationRunListener执行contextPrepared()、contextLoaded()

4、执行 ApplicationContext 的 refresh，完成程序启动
完成后，遍历执行 CommanadLineRunner、通知SpringApplicationRunListener 执行 finished()

参考：
https://blog.csdn.net/zxzzxzzxz123/article/details/69941910
https://www.cnblogs.com/shamo89/p/8184960.html
https://www.cnblogs.com/trgl/p/7353782.html

分析：

1） 创建一个SpringApplication对象实例，然后调用这个创建好的SpringApplication的实例方法

public static ConfigurableApplicationContext run(Object source, String... args)

public static ConfigurableApplicationContext run(Object[] sources, String[] args)

2） SpringApplication实例初始化完成并且完成设置后，就开始执行run方法的逻辑了，
方法执行伊始，首先遍历执行所有通过SpringFactoriesLoader可以查找到并加载的
SpringApplicationRunListener，调用它们的started()方法。


public SpringApplication(Object... sources)

private final Set<Object> sources = new LinkedHashSet<Object>();

private Banner.Mode bannerMode = Banner.Mode.CONSOLE;

...

private void initialize(Object[] sources)

3） 创建并配置当前SpringBoot应用将要使用的Environment（包括配置要使用的PropertySource以及Profile）。

private boolean deduceWebEnvironment()

4） 遍历调用所有SpringApplicationRunListener的environmentPrepared()的方法，通知Environment准备完毕。

5） 如果SpringApplication的showBanner属性被设置为true，则打印banner。

6） 根据用户是否明确设置了applicationContextClass类型以及初始化阶段的推断结果，
决定该为当前SpringBoot应用创建什么类型的ApplicationContext并创建完成，
然后根据条件决定是否添加ShutdownHook，决定是否使用自定义的BeanNameGenerator，
决定是否使用自定义的ResourceLoader，当然，最重要的，
将之前准备好的Environment设置给创建好的ApplicationContext使用。

7） ApplicationContext创建好之后，SpringApplication会再次借助Spring-FactoriesLoader，
查找并加载classpath中所有可用的ApplicationContext-Initializer，
然后遍历调用这些ApplicationContextInitializer的initialize（applicationContext）方法
来对已经创建好的ApplicationContext进行进一步的处理。

8） 遍历调用所有SpringApplicationRunListener的contextPrepared()方法。

9） 最核心的一步，将之前通过@EnableAutoConfiguration获取的所有配置以及其他形式的
IoC容器配置加载到已经准备完毕的ApplicationContext。

10） 遍历调用所有SpringApplicationRunListener的contextLoaded()方法。

11） 调用ApplicationContext的refresh()方法，完成IoC容器可用的最后一道工序。

12） 查找当前ApplicationContext中是否注册有CommandLineRunner，如果有，则遍历执行它们。

13） 正常情况下，遍历执行SpringApplicationRunListener的finished()方法、
（如果整个过程出现异常，则依然调用所有SpringApplicationRunListener的finished()方法，
只不过这种情况下会将异常信息一并传入处理）


private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type)

private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type,
			Class<?>[] parameterTypes, Object... args)

public void setInitializers

private Class<?> deduceMainApplicationClass()

public ConfigurableApplicationContext run(String... args)

private void configureHeadlessProperty()

private SpringApplicationRunListeners getRunListeners(String[] args)

public static List<String> loadFactoryNames(Class<?> factoryClass, ClassLoader classLoader)


*/
