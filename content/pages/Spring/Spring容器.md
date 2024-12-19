# Spring 容器[^1]

## 组件注册

组件：具有一定功能的对象

容器：管理组件（创建、获取、保存、销毁）

> 控制反转是一种思想，而依赖注入是 Spring 实现这种思想的方式

IOC：Inversion of control 控制反转

- 控制：资源的控制权（资源的创建、获取、销毁等）
- 反转：从 Programmer 控制转交给 Spring

DI：Dependency Injection 依赖注入

- 依赖：组件之间的依赖关系，如 News Controller 依赖于 NewsServices
- 注入：通过 setter、构造器等方式自动注入（赋值）



### Basic Spring Environment Creating In Idea

1. create a parent project, and make it packing to pom
2. create new model by using SpringBoot option



### 主程序类、主入口类

```java
/**
 * 主入口类、主程序类：
 * run spring application and return a ConfigurableApplicationContext,
 * which is an interface that extends from ApplicationContext.
 */

@SpringBootApplication
public class Spring01IocApplication {

    public static void main(String[] args) {

        // 1. ApplicationContext: spring 的应用上下文，即 spring 的容器
        ConfigurableApplicationContext ioc = SpringApplication.run(Spring01IocApplication.class, args);
        System.out.println("ioc is: " + ioc);

        // 2. 获取容器中所有组件的名字，可验证容器中有哪些组件
        String[] beanDefinitionNames = ioc.getBeanDefinitionNames();
        for (String name : beanDefinitionNames) {
            System.out.println("name = " + name);
        }
    }
```



### 注册组件的方式

``````java
@SpringBootApplication
public class example {

    public static void main(String[] args) {
        ...
    }
  
    // important! 此处的并不是 Person 的构造方法，只是一个普通的方法名，所以不会造成递归或循环调用的问题
    @Bean
    public Person person() {
        Person p = new Person();
        p.setName("Ethan");
        p.setAge(23);
        p.setGender("Male");
        return p;
    }
}
``````



### 组件的命名规则

- if do not set bean's name mannually, methods' name is bean's name. Like in above code, the bean's name is person in default.
- The way to set bean's name is that key in name in `@Bean(name)`, for example:

``````java
    @Bean("example_name")
    public Person person() {
        Person p = new Person();
        p.setName("Ethan");
        p.setAge(23);
        p.setGender("Male");
        return p;
    }
``````



### 组件特性

- 名字、类型、对象、作用域
- 组件名要求全局唯一
  - 在使用 `@Bean("name")`  方式注册组件时，注解的重名不会被编译器检查到，在代码级别是可以显示为给两个组件注册了同样的名字的
  - 但在实际的 Spring 执行过程中，只有先声明的组件会成功注册进容器
- 容器启动过程中就会创建组件对象
- 组件是**单实例的**



### 获取组件的方式

#### 通过组件的名字获取

``````java
Person example = (Person) ioc.getBean("example");
``````

#### 通过组件的类型获取

```java
Person bean = ioc.getBean(Person.class);
```

#### 通过组件的名字及类型获取

```java
Person person = ioc.getBean("example", Person.class);
```

#### 通过组件类型，获取到该类型的所有组件

```java
Map<String, Person> beansOfType = ioc.getBeansOfType(Person.class);
beansOfType.forEach((k, v) -> {
    System.out.println(v);
});
```



### MVC 分层模型对应注解

- @Configuration for Configuration Class
- @Controller for Controller Class
- @Service for Service Class
- @Repository for DAO Class
- @Component for Other Classes



### Configuration

- 组件：框架的底层配置
  - 配置文件：指定配置的文件
  - **配置类**：分类管理组件的配置，配置类也是容器中的一种组件
- 配置类的实现方式
  1. new config directory
  2. new configClass and use @Configuration to declare it
  3. then move things like components into this ConfigClass to register them

```java
@Configuration
public class PersonConfig {
    @Bean("example")
    public Person person() {
        Person p = new Person();
        p.setName("Ethan");
        p.setAge(23);
        p.setGender("Male");
        return p;
    }
}
```

*ConfigClass is also a component placed in the container.*



### @bean in ConfigurationClass VS @Component

- 使用 `@Configuration` 修饰的类是配置类，通常包含通过 `@Bean` 注解注册的方法，称为工厂方法。

  - Spring 会**保证**当配置类中的 `@Bean` 方法被多次调用时，返回的是**同一个对象**，这体现了 Spring 的单例哲学。
  - Spring 会为 `@Configuration` 类生成 CGLIB 代理，**确保** `@Bean` 方法的返回值是**单例**。

- `@Component` 类在**默认**情况下（单例模式）也是**单例**的。Spring 容器在启动时会自动装配这些组件，并在需要时返回**相同的实例**。

  - 只有在设置为 `prototype` 作用域时，每次调用 `@Component` 的方法才会创建新的对象。

  

### @ComponentScan

默认情况下，分层注解能起作用的前提是，这些组件必须在主程序所在的包 及其子包结构下

如果想要扫描其它包下的组件，则需要在主入口类使用 `@ComponentScan(basePackages = "..")` 注解，在主入口类声明组件扫描的范围。一般声明到稍大一级的范围，以开启组件的批量扫描

但更建议按照规范编程，将 Controller 等包规范放在和主程序类平级的位置



### 第三方组件导入容器

> 因为第三方组件的源码通常无法修改，所以无法通过标记分层注解的方式来导入容器
>
> 故可用：

1. 使用配置类，在配置类中写好工厂方法，手动加入容器中，like:

   ```java
   @Configuration
   public class ExampleConfig{
     @Bean
     public A a(){
       return new A();
     }
   }
   ```

2. 在主入口类或容器中任何一个组件上使用 `@Import(Example.class)` 注解，来导入组件，仅需导入一次
3. 更推荐使用一个单独的配置类，如 `AppConfig` 来管理与整个程序相关的注解



### @Scope

>  用于调整组件的作用域，默认是 `@Scope("singleton")` 单实例



1.  @Scope("prototype")，调整组件为 非单实例
2.  @Scope("singleton")，调整组件为 单实例
3.  @Scope("request")，调整组件为 同一个请求单实例
4.  @Scope("session")，调整组件为 同一次会话单实例



容器创建的过程中，就把所有单实例的组件创建完成并加入容器

当组件设置为非单实例（多例）时，容器启动时不会创建非单实例组件的对象，相反，什么时候获取什么时候创建



### @Lazy

> 用于 `@Scope("singleton")` 时，因为单实例时，所有组件是在容器初始化时创建好的，所以对于有懒加载需求的组件，可以使用 `@Lazy` 来设置。



### @FactoryBean<>

> 如果制造某些对象的过程比较复杂，不是用注解可以直接搞定的，则可以使用 FactoryBean，在 getObject() 中写复杂逻辑



- FactoryBean<> is a interface, and which class implements this interface, which class converts to a bean factory automatically.

- the class who implements FactoryBean<> need to be added `@Component` annotation to be putted into container.

- This interface has 3 methods need to override.

  - ```java
    public Car getObject() throws Exception {}
    
    // factory will use this method to create object and put it into container automatically.
    ```

  - ```java
    public Class<?> getObjectType() {}
    
    // use this method to return the type of object for 多态
    ```

  - ```java
    public boolean isSingleton() {}
    
    // make it return true & Singleton by default.
    ```



### @Conditional

> 此注解可以令组件在满足某些条件时才被创建并加载进容器

1. condition 是个接口，应当有一个类实现 Condition 接口，并在该接口的方法内进行条件的判断
2. Conditional 内的参数为实现了接口的 `ExampleCondition.class`
3. `ioc.getEnvironment()` 可以获得有关于环境变量的集合
4. 环境变量可以在 Run/Debug Configuration 中的 Environment variables 中手动设定，如 `os=mac`
5. 如没有环境变量的设置栏，可以 Run/Debug Configuration - Build And Run - Modify Options 中添加
6. `@Conditional` can be used for both Class and Method. If both Class and Method have `@Conditional`, the annotation on class is priorer than methods.



### Conditional 派生注解

有一系列派生注解可以使用

使用派生注解可以免去手写 ExampleCondition 类并手动实现 Condition-matches 方法的过程



---



## 注入

### @Autowired

> 自动装配。
>
> 原理是 Spring 调用容器的 getBean() 获得符合条件的组件，自动装配。



自动装配流程：

1. 按照类型找
   1. 有且只有一个组件，则注入，不考虑组件名
   2. 找到多个组件，则按照组件名继续匹配
      1. 如果找到同名组件，直接注入
      2. 如果找不到同名组件，则报错



@Autowired 的适用方式

```java
@Autowired
Person bill; // 注入一个

@Autowired
List<Person> people; // 注入查询到的多个

@Autowired
Map<String, Person> peopleMap; // 使用 Map，注入查询到的多个。Key 为组件名，Value 为组件

@Autowired
ApplicationContext ioc; // 注入 ioc 容器本体
```



### @Qualify & @Primary

> **Error**: Field person in UserService required a single bean, but 3 were found.
>
>  **Solution**: Consider marking one of the beans as `@Primary`, updating the consumer to accept multiple beans, or using `@Qualifier ` to identify the bean that should be consumed



当使用 `@Autowired` 查询到多个组件导致无法自动注入时，可以使用 `@Qualify(Component's name)` 精确指定需要注入的组件

亦可以在组件上使用 `@Primary` 标记为默认组件。当使用 `@Autowired` 查询到多个组件导致无法自动注入时，若这些组件中某一个被标注为 `@Primary` 则自动注入该组件。

**important**：同类型组件有多个，且使用 `@Primary` 标记了一个默认组件后，自动注入的将一直是该默认组件，**不再根据组件名进行匹配**。若需要注入非默认组件，可以使用 `@Qualify` 进行声明。



### @Resource

> import jakarta.annotation.Resource;
>
> jakarta 前身是 JavaX，是 JavaEE 的标准组织，具有更强的通用性



@Resource 也能实现自动注入，其与 @Autowired 的最大区别是，一个是 Java 标准下的，一个是 Spring 下的。这意味着所有具有容器功能的接口都支持 @Resource，然而 @Autowired 只有 Spring 下的框架支持。



@Autowired(required = false) 允许组件不存在时，令需要注入的属性为空，但 @Resource 没有类似的操作。



### 构造器注入

```java
@Repository
@Data
@ToString
public class UserDao {

    Person person;

    @Autowired
    public UserDao(Person person){
        this.person = person;
    }
}
```

可以使用 @Autowired 对 **构造器的形参** 进行自动注入。

**Very Recommend**。因为使用构造器注入，即使脱离了 Spring 环境手动 new 对象，依旧可以在 new 的时候手动为形参传参。而使用 @Autowired 在属性上时，则无法在手动创建对象时为属性赋值。



### Setter 方法注入

```java
  @Autowired
  public void setPerson(Person person) {
      this.person = person;
  }
```

可以使用 @Autowired 对 **setter方法的形参** 进行自动注入。



在 JavaBean 规范中，属性通常由 `getX()` 和 `setX()` 方法定义，而不一定与类中的某个具体变量名直接关联。Spring 在创建对象的过程中，如果发现某类型有对应的 `get/set` 方法，则会将该方法对应的变量视为属性，并自动调用 `set` 方法进行依赖注入。



```java
@Repository
@Data
@ToString
public class UserDao {

    Person dog;

    @Autowired
    public void setPerson(Person person) {
        this.dog = person;
    }
}
```

此代码依旧能完成 Person dog 属性的注入，因为 @Autowired 实际注入的是形参位置的 Person.class，再通过 Person.class 朝 this.dog 赋值。所以 person 和 dog 即使不重名也能完成注入。



### Aware 感知接口

> Aware 接口下有一系列子接口，用于一系列内容。可以通过这些感知接口自带的 set 方法获取内容给属性。
>
> 感知接口自带的 set 方法无需 @Autowired，Spring 就会自动注入这些方法的形参，将想要感知的内容传送来。

```java
@Service
public class xxAwareService implements EnvironmentAware{
    Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String typeOfEnvironment(){
        return environment.getProperty("spring.application.name");
    }
}
```



### @Value

@Autowired 是用来自动注入**组件**的，但一些基本数据类型，如 `String name` 是无法通过 @Autowired 完成注入的。

如果想给基本数据类型完成注入，则需要使用 @Value，如：

1. 直接用字面量给属性赋值

   ```java
   @Value("name")
   private String name;
   ```

2. 从配置文件中动态取出某一项的值

   ```java
   @Value("${dog.age}")
   private int age;
   ```

   配置文件（如 application.properties）

   ```properties
   dog.age=7
   ```

   亦可以设置取不到时的默认值

   ```java
   @Value("${dog.age:20}")
   private int age;
   ```

3. 在 `@Value("#{SpEL}")` 中写 SpEL (Spring Expression Language) Spring 表达式语言

   ```java
   @Value("#{10*${dog.age}}")
   private String color;
   ```

   赋 uuid 的场景：

   ```java
   @Value("#{T(java.util.UUID).randomUUID().toString()}") 
   private String uuid;
   ```

   此处的 T(type) 是 SpEL 的语法，用来表示 Java.lang.Class 类的实例，可以视作 Java 中直接写类名。

   只有 Java.lang 下的类可以省略包名。

   此写法一般用于引用常量及静态方法（因其可直接被类名调用）[^2]



### @PropertySource

在默认情况下，application.properties 是当前项目的配置文件。但将所有配置集中在一个配置文件中可读性极差。

故，可以为需要的类创建单独的 .properties, 并在该类上使用 @PropertySource 注解声明配置文件的来源（映射）。

```java
	/**
	 * Indicate the resource locations of the properties files to be loaded.
	 * <p>The default {@link #factory() factory} supports both traditional and
	 * XML-based properties file formats &mdash; for example,
	 * {@code "classpath:/com/myco/app.properties"} or {@code "file:/path/to/file.xml"}.
	 * <p>As of Spring Framework 6.1, resource location wildcards are also
	 * supported &mdash; for example, {@code "classpath*:/config/*.properties"}.
	 * <p>{@code ${...}} placeholders will be resolved against property sources already
	 * registered with the {@code Environment}. See {@linkplain PropertySource above}
	 * for examples.
	 * <p>Each location will be added to the enclosing {@code Environment} as its own
	 * property source, and in the order declared (or in the order in which resource
	 * locations are resolved when location wildcards are used).
	 */
	String[] value();
```

由文档，发现应当使用 `@PropertySource("classpath:/com/myco/app.properties")` or `@PropertySource("file:/path/to/file.xml")`



#### classpath: & file:

##### classpath:

当使用 `classpath:` 来引用资源文件，Java 默认从 `target/classes` 目录开始查找。因为在构建或编译后，所有资源文件（比如 `src/main/resources` 下的文件）会被复制到 `target/classes` 目录下，因此不需要在路径中包含 `target/classes`，直接写资源文件的相对路径即可。`target/classes` 是项目在构建后用于运行时加载类和资源的根目录。



##### file:

当使用 `file:` 前缀来引用资源时，路径指的是文件系统中的实际路径，而不是编译后的 `classpath` 路径。需要指定从操作系统文件系统中的起始位置来查找文件。`file:` 前缀指向的是文件系统中的路径，必须提供该文件在本地磁盘的完整路径或相对路径（相对于当前工作目录）。



### ResourceUtil

Spring 提供的一个工具类，提供 `ResourceUtil.getFile()` 等一系列方法，通过 `.getFile()` 方法，可以使用 classpath: 来获得 File，简化了文件操作。

```java 
File file = ResourceUtil.getFile("classpath:abc.jpg");

// 如需获得 io 流，则
new FileInputStream(file);
```



### @Profile

在不同的环境下会有不同的配置需求，此时则可以使用 @Profile 注解，从 application.properties 中快速切换。

@Profile can be used on class and method.



For example, if you have 3 databases corresponding respectively for dev, test, and production environment.

1. Firstly, writting a class to save info about each database. Like:

   ```java
   @Data
   public class MyDataSource {
       private String url;
       private String username;
       private String password;
       private String driver;
   }
   ```

   **important:** do not use `@Component` to inject this component into container caz if you do this, it will be singleton and will be inject into container at the beginning of creating this container, but actually you need to have 3 different beans, and injecting them depending on different settings. So, the way to deal with it is to writting a config class and creating beans by yourself.



2. So, Secondly, writting the config class used to register beans by yourself.

   ```java
   @Configuration
   public class DataSourceConfig {
   
       @Bean("dev")
       public MyDataSource dev() {
           MyDataSource myDataSource = new MyDataSource();
           myDataSource.setUrl("for dev");
           myDataSource.setUsername("for dev");
           myDataSource.setPassword("for dev");
           myDataSource.setDriver("for dev");
           return myDataSource;
       }
   
       @Bean("test")
       public MyDataSource test() {
           MyDataSource myDataSource = new MyDataSource();
           myDataSource.setUrl("for test");
           myDataSource.setUsername("for test");
           myDataSource.setPassword("for v");
           myDataSource.setDriver("for test");
           return myDataSource;
       }
   
       @Bean("production")
       public MyDataSource production() {
           MyDataSource myDataSource = new MyDataSource();
           myDataSource.setUrl("for production");
           myDataSource.setUsername("for production");
           myDataSource.setPassword("for production");
           myDataSource.setDriver("for production");
           return myDataSource;
       }
   }
   ```

   

3. Thirdly, writting the class where you need to use this DataSource class, in this case it might be a Dao class.

   ```java
   @Repository
   public class DeliveryDao {
       
       @Autowired
       MyDataSource dataSource; // Error
       
       public void saveDelivery() {
           System.out.println(dataSource + "is saving..");
       }
   }
   ```

   here have a bug caz there are three MyDataSource in container. Need to find a way to choose which one need to be used.

   there are many ways in normal condition, for example you can use @Primary & @Qualifier, but in this way you need to change core code very often, it is not a good way. Or maybe you can use @Conditional to choose a bean to @Autowired, it works but Spring provides a **more effective way**, that is **@Profile**. It allows you to switch different beans **in application.properties.**

   By the way, `@Profile` is actually **extends** from `@Conditional`.



4. Adding @Profile

   ```java
   @Configuration
   public class DataSourceConfig {
   
       @Profile({"dev", "default"}) // default added here
       @Bean("dev")
       public MyDataSource dev() {
           MyDataSource myDataSource = new MyDataSource();
           myDataSource.setUrl("for dev");
           myDataSource.setUsername("for dev");
           myDataSource.setPassword("for dev");
           myDataSource.setDriver("for dev");
           return myDataSource;
       }
   
       @Profile("test")
       @Bean("test")
       public MyDataSource test() {
           MyDataSource myDataSource = new MyDataSource();
           myDataSource.setUrl("for test");
           myDataSource.setUsername("for test");
           myDataSource.setPassword("for v");
           myDataSource.setDriver("for test");
           return myDataSource;
       }
   ```

   - @Profile("环境标识")：利用环境标识能实现 只有当这个环境被激活的时候，该组件才会被装入容器中
   - default 环境必须存在，由源码发现其底层是 String[]，故可以将某一个环境标识设置为包含 default 的数组

   

   测试:

   ```java
   @SpringBootApplication
   public class Spring01IocApplication {
   
       public static void main(String[] args) {
           ConfigurableApplicationContext context = SpringApplication.run(Spring01IocApplication.class, args);
           DeliveryDao bean = context.getBean(DeliveryDao.class);
           bean.saveDelivery();
       }
   }
     
   Output: MyDataSource(url=for dev, username=for dev, password=for dev, driver=for dev) is saving..
   ```



5. 如需切换不同环境，只需要在 application.config 中使用 kv 对 切换

   ```properties
   spring.profiles.active=test
   ```



## 原生方式使用

```java
@SpringBootApplication
public class Spring01IocApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ioc = SpringApplication.run(Spring01IocApplication.class, args);
        }
    }
```

`SpringApplication.run(Spring01IocApplication.class, args);` 这种启动方式是来自 Springboot 的。

如果想要使用 Spring framework 的原生方式启动，应手动 new ioc 容器。在本例中可 new ClassPathXmlApplicationContext 类，其意为在 **使用类路径下的某个 xml 文件作为 Spring 本身的配置文件来完成容器的创建**。

```java
public class Spring01IocApplication {
    public static void main(String[] args) {
        ApplicationContext ioc = new ClassPathXmlApplicationContext("classpath:spring-ioc.xml");
    }
```

配置文件如：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
```

使用该方法创建的容器，如果没有手动注册各类组件，则其中的组件为空，为一个纯净的容器。

xml 的注册方式：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="person" class="com.forty2.training.spring.ioc.pojo.Person">
        <property name="name" value="ethan"/>
        <property name="age" value="23"/>
        <property name="gender" value="Male"/>
    </bean>
</beans>
```

开启批量扫描（扫描使用注解注册过的）:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans 
		...
    <context:component-scan base-package="com.forty2.training.spring.ioc"/> 
</beans>
```

导入外部属性文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans 
		...
    <context:property-placeholder location="dog.properties"/>
</beans>
```

由此可见 xml 方式的原生使用很繁琐，所以淘汰为使用 Springboot 及注解、注解类的方式使用



## 组件生命周期

### @Bean 的生命周期

```java
@Configuration
public class UserConfig {

    @Bean(initMethod = "initUser", destroyMethod = "destroyUser")
    public User user(){
        return new User();
    }
}
```

使用 `@Bean` 注解中的两个参数 `initMethod` & `destroyMethod` 来引用 User 中的方法，以此来将这两个方法设置为**初始化时执行的**，及**销毁时执行的**。

```java
@Data
public class User {
    private String username;
    private String password;
    
    public User(){
        System.out.println("构造器..");
    }

    public void initUser(){
        System.out.println("@Bean 初始化, initUser..");
    }

    public void destroyUser(){
        System.out.println("@Bean 销毁, destroyUser..");
    }
}
```

this pojo has constructer, init method and destroy method, 执行顺序经测试为：

```java
 :: Spring Boot ::                (v3.3.4)

2024-10-22T16:42:09.832+08:00  INFO 29230 --- [spring-01-ioc] [           main] c.f.t.spring.ioc.Spring01IocApplication  : Starting Spring01IocApplication using Java 17.0.12 with PID 29230 (/Users/ethan/IdeaProjects/ssm-parent/spring-01-ioc/target/classes started by ethan in /Users/ethan/IdeaProjects/ssm-parent)
  
2024-10-22T16:42:09.833+08:00  INFO 29230 --- [spring-01-ioc] [           main] c.f.t.spring.ioc.Spring01IocApplication  : No active profile set, falling back to 1 default profile: "default"
  
构造器..
  
com.forty2.training.spring.ioc.pojo.Car@36453307 组件中的属性值的自动注入..
  
@Bean 初始化, initUser..
  
2024-10-22T16:42:10.041+08:00  INFO 29230 --- [spring-01-ioc] [           main] c.f.t.spring.ioc.Spring01IocApplication  : Started Spring01IocApplication in 0.325 seconds (process running for 0.486)
  
== 容器初始化完成 ==
  
User(username=null, password=null, car=com.forty2.training.spring.ioc.pojo.Car@36453307) bean 开始执行..
  
@Bean 销毁, destroyUser..

Process finished with exit code 0
```







### InitializingBean & DisposableBean

#### InitializingBean

Can also make pojo class implements **InitializingBean** and overrides a function called afterPropertiesSet. 

This function will be executed **after all setter finishing**.



#### DisposableBean

This function will be executed **after ** beans' code running.



### @PostConstruct

对 pojo 中的任意方法均可使用此注解修饰，当修饰时，该方法将在构造器后执行



### @Predestroy

对 pojo 中的任意方法均可使用此注解修饰，当修饰时，该方法将在组件销毁前执行



### BeanPostProcessor

> 回调感知：某步骤发生后调用的函数，用来通知 “xx发生了”。



以上的生命周期感知函数都没有返回值，所以只能用于修改对象值，而不能修改对象的类型。而 BeanPostProcessor 不同，它有返回值。也就是说 BeanPostProcessor 能够做强制拦截，获取输入的组件和组件名，return 一个新的 Object，以此就能实现对象类型的修改，进行人工的再包装。

```java
@Component
public class MyTestBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

皆传入 bean 和 bean 的名字，允许对 bean 进行修改之后 return 给下一步。



@Autowired 等注解的实现过程：

在构造器创建后，BeanPostProcessor 来检查所有的属性和注解，如果发现有属性被 @Autowired 修饰，则利用反射为其赋值，以此实现了自动注入。可以说 @Autowired 就是由 BeanPostProcessor 来实现的。



### 组件生命周期总结

![img](../../imgs/BeanLifeCycle.svg)



### 参照 @Autowired 的原理手写一个自动给 uuid 赋值的注解

> 利用反射。
>
> 验证属性上是否有注解。如果有，则使用 setter 反射赋值即可。



1. 写一个 UUID 注解

   ```java
   @Target({ElementType.FIELD})
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   public @interface UUID {
   }
   ```

2. 写一个需要用到 @UUID 实现自动生成 uuid 的 pojo

   ```java
   @Component("UUID_pojo")
   @Data
   public class UUID_pojo {
       @UUID
       private String uuid;
   }
   ```

3. 写一个 BeanPostProcessor

   ```java
   @Component
   public class UUID_BeanPostProcessor implements BeanPostProcessor {
       @Override
       public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
           // 此处发现 BeanPostProcessor 是应用全局的，所以应当判断一下被拦截的 bean 是否是想要处理的 bean
           if (bean instanceof UUID_pojo) {
               // 如果是，则利用反射注入值。
               // 先获取类对象
               Class<?> beanClass = bean.getClass();
   
               try {
                   // 获得可能被 @UUID 注解修饰的属性
                   Field uuidFiled = beanClass.getDeclaredField("uuid");
                   // 获得 @UUID 注解
                   com.forty2.training.spring.ioc.annotation.UUID annotation = uuidFiled.getAnnotation(com.forty2.training.spring.ioc.annotation.UUID.class);
   
                   // 如果属性被 @UUID 注解修饰
                   if (annotation != null) {
                       // 找到 set 方法
                       Method setUuid = beanClass.getDeclaredMethod("setUuid",String.class);
                       uuidFiled.setAccessible(true);
                       // 在 bean 实例上，注入自动生成的 uuid
                       setUuid.invoke(bean, UUID.randomUUID().toString());
                   }
               } catch (Exception e) {
                   throw new RuntimeException(e);
               }
           }
           return bean;
       }
   }
   
   ```

4. 测试

   ```java
   @SpringBootApplication
   public class Spring01IocApplication {
       public static void main(String[] args) {
           ConfigurableApplicationContext context = SpringApplication.run(Spring01IocApplication.class, args);
           System.out.println(context.getBean("UUID_pojo"));
       }
   }
   ```



## 单元测试

Spring 提供了单元测试类的注解，使用 `@SpringBootTest` 修饰即可，用于测试容器功能。



该类对存储位置有要求，即：**单元测试类所在的包，一定是主程序所在的包**。

![testClassName](../../imgs/TestClassName.png)



```java
@SpringBootTest
public class HelloTest {
    @Autowired
    User user;

    @Test
    public void test01() {
        System.out.println("user: " + user);
    }
}
```

在 SpringBootTest 中，组件可以使用 @Autowired 完成自动注入，省略获得组件并 getBean() 的过程。



PS: **@Autowired not applicable to local variables**, which means that you need to used it out of function.



----



## 引用

遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

[^1]:  https://www.bilibili.com/video/BV14WtLeDEit/?p=33&share_source=copy_web&vd_source=732a79db14c78dbec659a1afbe66586e
[^2]: https://blog.csdn.net/ZBZBZB12138/article/details/122597702
