# SpringMVC[^1]

## Definition

Spring Web MVC is the original web framework built on the Servlet API and has been included in the Spring Framework from the very beginning. The formal name, "Spring Web MVC," comes from the name of its source module **spring-webmvc**, but it is more commonly known as "Spring MVC".[^2]

Web 应用的核心就是 **处理 HTTP 请求响应**。

在前后端分离的开发环境下，交互是通过字符串完成的。对象会序列化成字符串传输，传输到前端后再反序列化成响应的内容。



## 基本前后端交互

```java
@Controller
public class HelloWorldController {

    @ResponseBody
    @RequestMapping("/hello")
    public String helloWorld() {
        return "Hello SpringMVC, 你好～";
    }
}
```

1. 声明 controller
2. 将 url 和对应的方法做 mapping
3. return 的默认返回值是做页面跳转，可以使用  @ResponseBody 转换成响应体

4. 启动主程序，spring 会自动搞定 tomcat、servlet 等一系列需要的内容

   output:

   ```java
    :: Spring Boot ::                (v3.3.5)
   
    : Starting Springmvc01HelloworldApplication using Java 17.0.12 with PID 13206 
    : No active profile set, falling back to 1 default profile: "default"
    : Tomcat initialized with port 8080 (http)
    : Starting service [Tomcat]
    : Starting Servlet engine: [Apache Tomcat/10.1.31]
    : Initializing Spring embedded WebApplicationContext
    : Root WebApplicationContext: initialization completed in 305 ms
    : Tomcat started on port 8080 (http) with context path '/'
    : Started Springmvc01HelloworldApplication in 0.583 seconds (process running for 0.779)
    : Initializing Spring DispatcherServlet 'dispatcherServlet'
    : Initializing Servlet 'dispatcherServlet'
    : Completed initialization in 1 ms
   ```

5. 访问 localhost:8080/mapping 即可



## @ResponseBody

声明方法中的 return 从做页面跳转变为返回数据（响应体）



## @RestController

```java
@Controller
@ResponseBody
public @interface RestController {
    @AliasFor(
        annotation = Controller.class
    )
    String value() default "";
}
```

@RestController 注解标记于类上，是前后端分离开发的**标准注解**。

声明此类为 Controller 且其中均为 @ResponseBody



## @RequestMapping

将某一个方法和对应的路径做绑定，接收到路径就启动对应的方法执行。

### 通配符

在路径位置允许通配符的出现。通配符有**精确优先原则**。

**?**：匹配任意**一个字符**

*****：匹配任意**多个字符**

******：匹配任意**多层路径**。只能存在于路径最后

### 请求限定

```java
RequestMethod[] method() default {}; // 限定请求方式

String[] params() default {}; // 限定请求参数

String[] headers() default {}; // 限定请求头

String[] consumes() default {}; // 限定请求内容类型

String[] produces() default {}; // 指定给浏览器响应内容的类型
```

#### method()

```
public enum RequestMethod {
    GET,
    HEAD,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE;
}
```

##### Postman

为了测试不同的请求方式，可以使用 [postman](https://www.postman.com/downloads/) 来完成。

#### params()

have 2 situations. 

if write params = "age", means must include field age;

if write params = "age=18", means that must contain "age=18".

Can also write like "age!=18".

```java
@RequestMapping(value = "/params", params = "age")
public String testParams() {
    return "testParams";
}

@RequestMapping(value = "/paramsAndValue", params = "age=18")
public String testParamsAndValue() {
    return "testParamsAndValue";
}
```

#### headers()

请求头中必须包含 key 为 haha 的 K-V 对

```java
@RequestMapping(value = "/test3",headers = "haha")
public String test3() {
    return "test3";
}
```

#### consumes()

限定**请求的数据类型**必须为 json

```java
@RequestMapping(value = "/test4", consumes = "application/json")
public String test4() {
    return "test4";
}
```

#### produces()

指定给浏览器**响应体内的内容类型**。

如指定为 "text/html;charset=utf-8"，则浏览器会将接收到的响应体的内容按照 html 格式解析。

```java
@RequestMapping(value = "/test5", produces = "text/html;charset=utf-8")
public String test5() {
    return "<h1>Test5</h1>";
}
```



## 请求处理

### 分别收集请求参数

```java
@RestController
public class IndexDemoController {

    @RequestMapping("/handle01")
    /*
    http://localhost:8080/handle01?
    username=1&
    password=2&
    cellphone=1&
    agreement=on
     */
    public String handle01(String username,String password,String cellphone, String agreement) {
        System.out.println(username);
        System.out.println(password);
        System.out.println(cellphone);
        System.out.println(agreement);
        return "ok!";
    }
}
```

请求为注释内的内容。requestMapping 的地址为 form 的 action。

普通变量的键值对可以自动封装。

**默认情况下，请求中的每个参数都会携带内容，如果内容为空，包装类型则自动封装为 null，基本类型自动为默认值。**

#### **@RequestParam("name")** 

在直接写变量进行自动封装的时候，要求**变量名为请求中 key 的名字**。如果不匹配，可以用 **@RequestParam("name")** 进行映射。

在使用 **@RequestParam("name")**  时，要求该注解修饰的参数必须有对应的值，如果请求中不携带则会报错。在此情况下，可以使用 required=false 来使其可以不携带参数，不携带参数时的规则与默认相同，即如果内容为空，包装类型则自动封装为 null，基本类型自动为默认值。

同时，@RequestParam 注解还有 defaultValue 属性，如果没有携带参数则使用 defaultValue。在有defaultValue 的情况下，只有无 defaultValue 且无 required=false 才会报错。

### Pojo 收集请求参数

```java
@RequestMapping("handle03")
public String handle03(Person person) {
    System.out.println(person);
    return "ok!";
}
```

如果目标方法的参数是一个 pojo，SpringMVC 会利用反射自动映射封装。

如果请求参数没带（url 中无键值对），则会封装为 null。如果url 中的值为空，则封装为空字符串。

**没带参数指 url 中无键值对**

pojo 的 defaultValue 可以直接在 pojo 中 `private String username="defaultValue";`

### 获取请求头

```java
@RequestMapping("handle04")
public String handle04(@RequestHeader("host") String host) {
    System.out.println(host);
    return "OK";
}
```

使用 @RequestHeader("key") 注解即可获得请求头中的内容。

### 获取 cookie

```java
@RequestMapping("handle05")
public String handle05(@CookieValue("cookie1") String cookie1) {
    System.out.println(cookie1);
    return "OK";
}
```

使用 @CookieValue("key") 获取 cookie 的值，同一个请求中可以有多个 cookie 被获取。

### pojo 级连封装复杂属性

> http://localhost:8080/handle06?username=zhangsan&password=psw&cellphone=173&
> address.province=hebei&address.city=normal&address.area=uni&sex=男&
> hobby=足球&hobby=篮球&grade=四年级&agreement=on

对于这种复杂属性的封装，Spring 依旧可以完成 pojo 自动注入。pojo 的属性与请求参数类型对应即可。

```java
package com.forty2.training.springmvc.bean;

import lombok.Data;

@Data
public class Person {
    private String username;
    private String password;
    private String cellphone;
    private String agreement;
    private Address address;
    private String[] hobby;
    private String grade;
}

@Data
class Address {
    private String province;
    private String city;
    private String area;
}

```

```java
public String handle06(Person person) {
    System.out.println(person);
    return "ok";
}
```

*output:* Person(username=zhangsan, password=psw, cellphone=173, agreement=on, address=Address(province=hebei, city=normal, area=uni), hobby=[足球, 篮球], grade=四年级)

### 使用 @RequestBody 封装 json 对象

1. 使用 Postman 可以在请求体里添加 json 对象

2. @RequestBody 可以将请求体内的 json 对象以字符串形式接收

   ```json
   {
       "username":"ethan",
       "password":"password",
       "cellphone":"173",
       "agreement":"true",
       "hobby":["ball","yellow"],
       "grade":"grade12",
       "address":{
           "province":"hebei",
           "city":"rockhometown",
           "area":"qiaoxi"
       }
   }
   ```

   ```java
   @RequestMapping("/handle07")
   public String handle07(@RequestBody String json) {
       System.out.println(json);
       return "ok";
   }
   ```

   *output:* 

   ```json
   {
       "username":"ethan",
       "password":"password",
       "cellphone":"173",
       "agreement":"true",
       "hobby":["ball","yellow"],
       "grade":"grade12",
       "address":{
           "province":"hebei",
           "city":"rockhometown",
           "area":"qiaoxi"
       }
   }
   ```

3. 或，可以在 @RequestBody 后接 pojo，Spring 可完成自动封装

   ```java
   @RequestMapping("/handle08")
   public String handle07(@RequestBody Person person) {
       System.out.println(person);
       return "ok";
   }
   ```

   *output:* Person(username=ethan, password=password, cellphone=173, agreement=true, address=Address(province=hebei, city=rockhometown, area=qiaoxi), hobby=[ball, yellow], grade=grade12)

### 文件上传

**路径不太会写**，but playload like:

```
1. username: 1
2. password: 1
3. cellphone: 1
4. headerImg: (binary)
5. lifeImg: (binary)
6. lifeImg: (binary)
7. agreement: on
```

```java
@RequestMapping("/handle08")
public String handle08(Person person,
                       @RequestParam("lifeImg") MultipartFile[] lifeImgs,
                       @RequestParam("headerImg") MultipartFile headerImg) throws IOException {
    headerImg.transferTo(new File("context"));

    for (MultipartFile lifeImg : lifeImgs) {
        lifeImg.transferTo(new File("context"));
    }
    System.out.println(person);
    return "ok!";
}
```

不是 json 不需要使用 RequestBody 接收，直接用 Person person 就会完成自动封装。

### 取出整个请求

```java
@RequestMapping("/handle09")
public String handle09(HttpEntity<String> httpEntity) {
    System.out.println(httpEntity.getHeaders());
    System.out.println("=======");
    System.out.println(httpEntity.getBody());
    return "ok!";
}
```

HttpEntity 封装整个原始请求，包含请求头和请求体。

其中的泛型用来声明**请求体里的内容类型**。

为 String 则将 json 封装为 String。为 pojo 类型时将自动封装转化。



## 响应处理

### 返回 json

```java
@RequestMapping("/resp01")
public Person resp01() {
    Person person = new Person();
    return person;
}
```

在 SpringMVC 中，会自动把返回的对象转换成 json 格式 。

因为 @RestController 内部隐含了一个 @ResponseBody，用于将返回的内容写回到响应体中。

### 文件下载

格式相对固定，可封装工具类。

```java
@RequestMapping("/download")
public ResponseEntity<byte[]> download() throws Exception {
    FileInputStream fileInputStream = new FileInputStream("/Users/ethan/Desktop/IMG_778679F84A40-1.jpeg");
    byte[] bytes = fileInputStream.readAllBytes();

    return ResponseEntity.ok()
      			.contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(bytes.length)
            .header("Content-Disposition", "attachment;filename=FileName.jpeg")
            .body(bytes);
}
```

ResponseEntity: 拿到整个响应数据，含响应头、响应体、状态码。如果是文件下载，则响应体内的内容就是 byte[]，由泛型指定即可。

return 中是核心的返回逻辑：

- ResponseEntity.ok()：ok 里可传泛型。给 pojo 则转成 json，给文件流则开始下载。

- 如果直接 ok 给文件流，则前端不清楚内容类型，返回的是纯粹的 byte[]。所以通常采用链式处理填充内容。
  1. .ok() 返回无参构造
  2. 在 .ok() 后链式设定：
     1. 内容类型为文件流
     2. 设置内容长度
     3. 在 header 中设定 Content-Disposition 内容处理方式为 attachment 附件，并设置文件名
     4. 在 body 中放入文件流所读入的字节数组即可（使用 fis.readAllBytes() 即可）

#### bugFixs

- 文件名为中文时可能出现乱码。

  ```java
  String encode = URLEncoder.encode("中文.jpg", "UTF-8");
  "attachment;filename=" + encode
  ```

- 文件太大会内存溢出 out of memory，因为 bytes[] 是从输入流中 readAllBytes() 得到的。

  解决方法：

  1. 将 ResponseEntity 中响应体的类型设置为 InputStreamResource
  2. `InputStreamResource resource = new InputStreamResource(inputStream);` 替换掉 readAllBytes() ，将输入流封装成一个输入流资源对象
  3. ResponseEntity.body() 中由 byte[] 替换为该 InputStreamResource 对象即可
  4. 由于没有 byte[]，.contentLength() 中可以传入 inputStream.available() 返回的长度

#### 固定模板

```java
@RequestMapping("/download")
public ResponseEntity<InputStreamResource> download() throws IOException {
    FileInputStream fileInputStream = new FileInputStream("/Users/ethan/Desktop/IMG_778679F84A40-1.jpeg");
    String encode = URLEncoder.encode("文件名.jpeg", StandardCharsets.UTF_8);
    InputStreamResource inputStreamResource = new InputStreamResource(fileInputStream);

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(fileInputStream.available())
            .header("Content-Disposition", "attachment;filename=" + encode)
            .body(inputStreamResource);
}
```

### 总结

return 中：

@ResponseBody + return 对象 = 响应 json 等非页面数据

HttpEntity<> or **ResponseEntity<>** = 响应完全自定义的响应头、响应体

String = 逻辑视图地址



## RESTful

Representational State Transfer 表现层状态转移，是一种软件架构风格。

完整理解为 Resource Representational State Transfer，资源表现形式状态转移。即**使用资源名作为 URI**，**使用 HTTP 请求方式表示对资源的操作**。

例如，将订单视为资源。则路径为 */order。对 order 的增删改查分别使用不同的请求方式而不是修改路径。

以 emp 的增删改查为例，**restful api 设计如下**：

| URI        | 请求方式 | 请求体      | 作用           | 返回数据        |
| ---------- | -------- | ----------- | -------------- | --------------- |
| /emp/{id}  | GET      | 空          | 查询 id 号员工 | emp json        |
| /emp       | POST     | emp json    | 新增某员工     | T/F             |
| /emp       | PUT      | emp json    | 修改某员工     | T/F             |
| /emp/{id}  | DELETE   | 空          | 删除某员工     | T/F             |
| /emps      | GET      | 空/查询条件 | 查询所有员工   | List\<emp> json |
| /emps/page | GET      | 空/分页条件 | 查询所有员工   | 分页数据 json   |

API: Web 应用**暴露出的**让别人访问的**请求路径**

### CRUD 案例

#### Pojo & Dao

1. In dao, use JdbcTemplate to wirte basic crud (Can be found in [Spring 事务](../Spring/Spring事务.md)), like:

   ```java
   @Component
   public class EmployeeDaoImpl implements EmployeeDao {
       @Autowired
       JdbcTemplate jdbcTemplate;
   
       @Override
       public Employee getEmployeeById(Long id) {
           String sql = "select * from employee where id = ?";
           return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Employee.class), id);
       }
   
       @Override
       public void addEmployee(Employee employee) {
           String sql = "insert into employee (name, age, email, gender, address, salary) values (?, ?, ?, ?, ?, ?)";
           jdbcTemplate.update(sql, employee.getName(), employee.getAge(), employee.getEmail(), employee.getGender(), employee.getAddress(), employee.getSalary());
       }
   
       @Override
       public void updateEmployee(Employee employee) {
           String sql = "update employee set name=?, age=?, email=?, gender=?, address=? where id=?";
           jdbcTemplate.update(sql, employee.getName(), employee.getAge(), employee.getEmail(), employee.getGender(), employee.getAddress(), employee.getId());
       }
   
       @Override
       public void deleteEmployee(Long id) {
           String sql = "delete from employee where id=?";
           jdbcTemplate.update(sql, id);
       }
   }
   ```

2. write Pojo

#### Service

在**更新**时，service 层调 dao 时需要考虑**防 null 处理**。

Service 是被 controller 调用的，controller 层传过来的对象可能是有 null 值的，如果 service 不对此进行处理直接转交给 dao，则 dao 会将 null update 为新值，显然不合理。

解决方式是：

1. 查到数据库中需要处理对象的原值，封装为 pojo

2. 将 pojo 中需要修改的参数（非 null 参数）做更新。更新时用 **SpringUtils.hasText()** 做校验，SpringUtils.hasText() 只有不是 null、不是空串、不是空白字符时才会 return true。

   ```java
   @Override
   public void updateEmployee(Employee employee) {
       if (employee.getId() == null) return;
       Employee temp = employeeDao.getEmployeeById(employee.getId());
   
       // about String
       if (StringUtils.hasText(employee.getName())) {
           temp.setName(employee.getName());
       }
   
       if (StringUtils.hasText(employee.getEmail())) {
           temp.setEmail(employee.getEmail());
       }
   
       if (StringUtils.hasText(employee.getAddress())) {
               temp.setAddress(employee.getAddress());
           }
           
           if (StringUtils.hasText(employee.getGender())){
               temp.setGender(employee.getGender());
           }
           
           // about int
           if (employee.getAge() != null && employee.getAge() >= 0) {
               temp.setAge(employee.getAge());
           }
           
           // about BigDecimal
           if (employee.getSalary() != null) {
               temp.setSalary(employee.getSalary());
           }
   
           // finished
           employeeDao.updateEmployee(employee);
       }
   ```

3. 将更新好的交给 dao update

#### Controller

```java
package com.forty2.training.rest.crud.controller;

@RestController
public class EmployeeRestController {

    @Autowired
    EmployeeService employeeService;

    @RequestMapping(value = "/employee/{id}", method = RequestMethod.GET)
    public Employee get(@PathVariable("id") Long id) {
        return employeeService.getEmployeeById(id);
    }

    @DeleteMapping("/employee/{id}")
    public String delete(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return "ok!";
    }

    @PostMapping("/employee")
    public String add(@RequestBody Employee employee) {
        employeeService.saveEmployee(employee);
        return "ok!";
    }

    @PutMapping("/employee")
    public String update(@RequestBody Employee employee) {
        employeeService.updateEmployee(employee);
        return "ok!";
    }
}

```

在 Restful 风格下，可以使用传统的 RequestMapping + method = 请求方式 的方式来完成对不同请求的处理

也可以使用 **@xxxMapping** 这种 rest 映射的注解来简化完成。

路径中可以使用 {} 来包裹变量，并用 **@PathVariable** 来与形参绑定。

##### code & msg 下的返回值统一

在上例中，前端收到的返回值是 json 和 String 混合的，不利于前端操作，所以我们希望给前端一个统一的返回值。

在常规做法中，统一返回 json，并且在 json 中定义一组 **状态码及 msg** 以供前端判断。如，200 为正确，其余均为失败。

前端和后端就是通过状态码和 msg 来交互状态信息，以供前端对不同的情况做出不同的效果显示。

```json
{
  "code" : 200,					// 业务状态码
  "msg" : "余额不足",    // 服务端提示消息
  "data" : {} or []    // 返回给前端的数据
}
```

1. 定义一个 R 类型，用于统一结果的返回。数据作为其中的 data。并提供有 data 及无 data 的两个重载版本。

   ```java
   @Data
   public class R {
       private Integer code;
       private String msg;
       private Object data;
   
       public static R ok() {
           R r = new R();
           r.setCode(200);
           r.setMsg("ok");
           return r;
       }
   
       public static R ok(Object data) {
           R r = ok();
           r.setData(data);
           return r;
       }
   }
   ```

2. 优化上文中的 Controller 代码

   ```java
   @RestController
   public class EmployeeRestController {
   
       @Autowired
       EmployeeService employeeService;
   
       @GetMapping("/employee/{id}")
       public R get(@PathVariable("id") Long id) {
           return R.ok(employeeService.getEmployeeById(id));
       }
   
       @DeleteMapping("/employee/{id}")
       public R delete(@PathVariable("id") Long id) {
           employeeService.deleteEmployee(id);
           return R.ok();
       }
   
       @PostMapping("/employee")
       public R add(@RequestBody Employee employee) {
           employeeService.saveEmployee(employee);
           return R.ok();
       }
   
       @PutMapping("/employee")
       public R update(@RequestBody Employee employee) {
           employeeService.updateEmployee(employee);
           return R.ok();
       }
   }
   ```

3. 至此完成前后端通过 json 的格式统一

4. 提供失败的方法

   ```java
   public static R error() {
       R r = new R();
       r.setCode(500);
       r.setMsg("error");
       return r;
   }
   
   public static R error(Integer code, String msg) {
       R r = new R();
       r.setCode(code);
       r.setMsg(msg);
       return r;
   }
   
   public static R error(Integer code, String msg, Object data) {
       R r = error(code, msg);
       r.setData(data);
       return r;
   }
   ```

#### @PathVariable 其他写法

- /recourses/{name} 
  - **最多使用**。
  - {} 中的值封装到 name 变量中
- /recourses/{*path}。
  - {} 中的值封装到 path 变量中。*path 指 resources 后的多层路径。
    - /recourses/img.png -> **path=/img.png**
    - /recourses/css/spring.css -> **path=/css/spring.css**
- /recourses/{filename:\\\w+}.后缀，正则写法的约束
  - {} 中的值封装到 filename 变量中。filename 满足 \\\w+正则要求
  - /recourses/{filename:\\\\w+}.后缀
  - /recourses/{filename:\\\\w+}.后缀

## CORS Policy 跨源资源共享

### 同源策略

**浏览器**为了安全性考虑，默认会遵循**同源策略**，要求 请求的服务器 和 当前项目所在的服务器 必须是同一个源（同一个服务器）。如果不是，请求就会被拦截。

- 同源策略只限制 ajax 请求、图片、文档、CSS、js 等。

- 直接用地址发请求不被限制，但从**另一个地址中**，**悄悄地**向不同源的地址发请求则会被限制。

*eg:* 同源策略要求 / 以前的内容完全一样。localhost/emp 和 localhost/api/emp 就不是同源。在本例中 使用浏览器地址栏 直接发送请求到 localhost/api/emp 是可行的，但从 localhost/emp 中（通过某个按钮？）悄悄的向 localhost/api/emp 发请求则被限制。

### CORS

同源策略会引起 **跨域(源)资源共享 CORS**，即当前要访问的域名和目前所在的域名不符。

复杂的跨域请求会发送两次：

1. options 请求：预检请求。浏览器会先发送 options 请求，询问服务器是否允许当前域名进行跨域访问。服务器若允许，则会在这次请求的响应体中携带 Access-Control-Allow-Origin = ? 字段，通知浏览器跨域资源共享被允许。
2. 在服务器允许后才会发送真正的请求。

### CORS 的解决方法

1. 前端解决：按下不表
2. 后端解决：允许前端跨域即可。前端默认不能跨域是因为遵循了浏览器的安全标准，不允许悄悄的发请求。所以只要 被发送请求的服务器 声明 允许特定的服务器跨域访问我即可。

#### 后端解决方案

##### 原理

服务器在给浏览器的响应头中添加字段：Access-Control-Allow-Origin = *，允许的访问来源为全部（或特定的域名）

##### 实现

在 RestController 上添加 @CrossOrigin 允许跨域注解，该 Controller 下的所有方法都将允许跨域的资源访问。此注解亦可添加在方法上，则只有该方法对应的 api 允许跨域访问。



## 最佳实践

### 拦截器

SpringMVC 内置**拦截器**机制，允许 **在请求被目标方法处理的前后** 进行拦截，执行一些**额外操作**。比如：权限验证、日志记录、数据共享等。

#### 实现步骤

1. **实现 HandlerInterceptor 接口**的**组件**即可成为拦截器。重写对应的方法即可在内部完成逻辑处理。

   preHandle() return true 则放行，目标方法可以继续执行。

   ```java
   @Component
   public class MyHandlerInterceptor implements HandlerInterceptor {
       @Override
       public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
           System.out.println("preHandle...");
           return true;
       }
   
       @Override
       public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
           System.out.println("postHandle...");
       }
   
       @Override
       public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
           System.out.println("afterCompletion...");
       }
   }
   ```

2. 创建配置类，并使用 @Configuration 注解声明这是一个专门对 SpringMVC 底层做配置的配置类，该配置类需要完成 WebMvcConfigurer 组件的注册，对 SpringMVC 底层做配置就基于这个 WebMvcConfigurer 组件来完成。 

3. 向容器中注册 WebMvcConfigurer 组件。声明 WebMvcConfigurer 有两种方法：

   - 让配置类实现 WebMvcConfigurer 接口，则配置类自动成为 WebMvcConfigure 组件

     ```java
     public class MySpringMvcConfig implements WebMvcConfigurer {}
     ```

   - 在配置类中，使用 @Bean 添加 new 出的 WebMvcConfigurer 组件。

     本例代码是将 myWebMvcConfigurer() 方法 return 的返回值对象作为组件注入，并对该匿名类重写拦截器注册的方法，进行拦截器的注册。

     ```java
     @Bean
     WebMvcConfigurer myWebMvcConfigurer() {
         return new WebMvcConfigurer() {
             @Override
             public void addInterceptors(InterceptorRegistry registry) {
                 WebMvcConfigurer.super.addInterceptors(registry);
             }
         };
     }
     ```

4. 为拦截器设置拦截路径。

   在本例中，MyHandlerInterceptor 应当为自动注入的，不能在 registry.addInterceptor(myHandlerInterceptor) 中 new 新的 MyHandlerInterceptor 对象，否则违反 Spring 的单例哲学。

   ```java
   @Configuration 
   public class MySpringMvcConfig implements WebMvcConfigurer {
       @Autowired
       MyHandlerInterceptor myHandlerInterceptor;
   
       @Override
       public void addInterceptors(InterceptorRegistry registry) {
           registry.addInterceptor(myHandlerInterceptor)
                   .addPathPatterns("/**");
       }
   }
   ```

5. 执行顺序效果：**preHandle -> 目标方法 -> postHandle -> afterCompletion**

6. 因为拦截器的 preHandle 可以通过返回值限定是否放行，所以可以实现权限检查等需要 执行/不执行 的功能。不执行时，可以使用 response.getWriter().write("error 通知")

### 异常处理

#### 编程式异常处理

通过 try - catch - finally 完成。但在实际业务中，可能每个操作都会有对应的异常，所以使用编程式异常处理方式会非常繁琐。

#### 声明式异常处理

Spring 提供了一系列注解，可以通过注解简化异常处理。推荐使用。

使用 @ExceptionHandler(value = ArithmeticException.class) 声明异常类型后，对应异常发生时则会转移至对应的异常处理函数完成异常的处理。

如果 Controller 本类出现异常，会自动的在本类中找有没有 @ExceptionHandler 标注的方法。如果有，则调用该方法，该方法的返回值就是客户端收到的请求。在下例中，因为是 @RestController，隐含 @ResponseBody，所以返回值以 R 对象的 json 形式返回。 

```java
@RestController
public class AnnoExceptionController {

    @GetMapping("/hello")
    public R hello(@RequestParam(value = "i", defaultValue = "2") Integer i) {
        int j = 10 / i;
        return R.ok(j);
    }

    @ExceptionHandler(value = ArithmeticException.class)
    public R handleArithmeticException(ArithmeticException e) {
        return R.error(233, "异常发生, " + e.getMessage());
    }
}
```

*output:* 

```json
{
    "code": 233,
    "msg": "异常发生, / by zero",
    "data": null
}
```

@ExceptionHandler 的 value 是数组，所以可以同一个方法处理多个异常。

```java
public @interface ExceptionHandler {
    Class<? extends Throwable>[] value() default {};
}
```

如果不需要严谨的分异常处理，甚至可以使用一个方法接收所有的 Throwable，like：

```java
@ExceptionHandler(Throwable.class)
public R handleArithmeticException(Throwable throwable) {
    return R.error(233, "Throwable: " + throwable.getMessage());
}
```

在有**多个** @ExceptionHandler 都能处理一个异常时，则**精确优先**。最精确的异常处理器会被调用。

#### 全局异常处理

@ExceptionHandler 只对**注解所处类中**的 Throwable 做处理，其他 Controller 中的异常无法被处理。

可以通过 @ControllerAdvice 来完成全局异常的统一处理。

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public R error(Exception e) {
        return R.error(500, e.getMessage());
    }
}
```

@ControllerAdvice 中的 Advice 有增强含义，其会告诉 SpringMVC 该组件专门负责全局异常的处理。因为此注解被 @Component 修饰，所以会自动加入容器。

由于 @ExceptionHandler 返回的是自定的 json 格式 R 对象，所以此类/方法亦需要使用 @ResponseBody 声明返回为 json。

SpringMVC 提供了一个合成注解 **@RestControllerAdvice**

#### 最终的异常处理

一旦出现异常，**前端关心**的是不同的状态码，其需要根据不同的状态码做不同响应，如控制页面跳转等。

**后端关心**正确的业务流程，因为关心错误则需要无限的判断来完成，而关心正确的业务流程则只有一条链路可走。

所以**异常处理的最佳实践**是：

1. 后端只编写正确的业务逻辑。如果执行期间出现问题，如参数不正确等，后端可以通过抛异常的方式提前中断业务逻辑，让前端感知异常。

2. 在中断业务的时候，必须让上层及以上的链路知道中断原因。推荐抛出**业务异常**，以和其他异常做区分。

   允许使用业务异常枚举来构造业务异常对象抛出。

   ```java
   package exception;
   public class BizException extends RuntimeException {
       private Integer code;
       private String message;
   
       public BizException(Integer code, String message) {
           this.code = code;
           this.message = message;
       }
   
       public BizException(BizExceptionEnum exceptionEnum) {
           this.code = exceptionEnum.getCode();
           this.message = exceptionEnum.getMessage();
       }
   }
   ```

3. 大型系统可能会出现海量的异常情况，所以需要固化异常处理文档，保证 code 和 message 一一对照。

   推荐使用**枚举**进行有限可能列举。

   ```java
   package exception;
   public enum BizExceptionEnum {
       ORDER_CLOSED(10001, "订单已关闭"),
       ORDER_EXIST(10002, "订单已存在");
     
       @Getter
       private Integer code;
       @Getter
       private String message;
   
       private BizExceptionEnum(Integer code, String message) {
           this.code = code;
           this.message = message;
       }
   }
   ```

4. 封装好业务异常及其枚举之后，就可以在全局异常处理类中写一个异常的处理器，并通过传入的 BizException 来获得 code & message 传回前端。

   ```java
   @ExceptionHandler(value = BizException.class)
   public R bizException(BizException e) {
       return R.error(e.getCode(), e.getMessage());
   }
   ```

#### 步骤汇总

1. 必须有业务异常类 BizException
2. 必须有异常枚举类 BizExceptionEnum，列举项目中每个模块将会出现的所有异常情况
3. 编写业务代码的时候，只编写正确逻辑。
4. 如果出现预期的问题（如订单不存在），以抛异常的方式中断逻辑并通知上层。
5. 全局异常处理器，专门用于处理所有异常，返回给前端约定好的 code and message。

#### 总结

在业务逻辑运行的过程中，只要发生异常，则直接抛出业务异常，中断业务逻辑。

抛出异常后由全局异常处理器接收并返回 code 和 message 给前端。

至此业务异常处理的链路被打通。

### 数据校验

JSR 303 是 Java 为 Bean 数据合法性校验提供的标准框架。通过**在 Bean 属性上**标注 @NotNull、@Max 等标准注解指定校验规则。

#### 使用流程

1. **引入校验依赖 spring-boot-starter-validation**

2. **定义封装数据的 Java bean**

3. **给 bean 的字段标注注解，并指定校验错误的提示信息**

   ```java
   @Data
   public class Employee {
       private Long id;
       
       @NotBlank(message = "姓名不为空")
       private String name;
       
       @NotNull(message = "年龄不能为空")
       @Max(value = 150, message = "年龄不能超过 150")
       @Min(value = 0, message = "年龄不能小于 0")
       private Integer age;
       
       @Email(message = "邮箱格式不正确")
       private String email;
       
       private String gender;
       private String address;
       private BigDecimal salary;
   }
   ```

4. **使用 @Valid、@Validated 开启校验**

   ```java
   @PostMapping("/employee")
   public R add(@RequestBody @Valid Employee employee) {
       employeeService.saveEmployee(employee);
       return R.ok();
   }
   ```

5. 使用 **BingdingResult** 封装校验结果。如果有错，则从 BindingResult 中获得所有出错的属性及其相关信息，并按封装好的 json R 格式返回给前端。

   ```java
   @PostMapping("/employee")
   public R add(@RequestBody @Valid Employee employee, BindingResult bindingResult) {
       if (!bindingResult.hasErrors()) {
           employeeService.saveEmployee(employee);
           return R.ok();
       }
   
       Map<String, String> errorsMap = new HashMap<>();
   
       for (FieldError fieldError : bindingResult.getFieldErrors()) {
           String field = fieldError.getField();
           String message = fieldError.getDefaultMessage();
           errorsMap.put(field, message);
       }
   
       return R.error(500, "校验失败", errorsMap);
   }
   ```

6. 结合**全局异常处理**，统一处理数据校验错误【推荐】

   在上述写法中，不同的校验均需要写重复代码。所以可以使用 全局异常处理 抽取出重复代码进行复用。

   经接收 Throwable 的全局异常处理器 e.getClass() 发现，校验失败时实际抛出的异常为 MethodArgumentNotValidException，so 抽取。

   ```java
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public R methodArgumentNotValidException(MethodArgumentNotValidException e) {
       BindingResult bindingResult = e.getBindingResult();
   
       Map<String, String> errorMap = new HashMap<>();
       for (FieldError fieldError : bindingResult.getFieldErrors()) {
           errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
       }
   
       return R.error(500, "数据校验异常", errorMap);
   }
   ```

Optional：

1. 使用 **自定义校验注解 + 校验器（implements ConstraintValidator）**完成字段自定义校验规则

   如果想给 gender 添加二元性别 only 的校验，可以使用正则，也可以自定义校验注解，并完成校验器的书写。

   注解书写照抄 @NotNull，其中由 @Constraint(validatedBy = {}) 指定的 class 来完成真正的校验功能

   ```java
   @Target(ElementType.FIELD)
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   @Constraint(validatedBy = {GenderValidator.class})
   public @interface Gender {
       String message() default "{jakarta.validation.constraints.NotNull.message}";
       Class<?>[] groups() default {};
       Class<? extends Payload>[] payload() default {};
   }
   ```

   随后完成校验器的规则编写

   ```java
   public class GenderValidator implements ConstraintValidator<Gender, String> {
       @Override
       public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
           return "male".equals(value) || "female".equals(value);
       }
   }
   ```

2. 结合校验注解 message 属性与 i18n 文件，实现错误消息的国际化。

   1. 使用占位符来填充错误消息的位置

      ```java
      @Gender(message = "{gender.message}")
      private String gender;
      ```

   2. new properties file named as `messages.properties`  in resource to map {gender.message}, and this file name is default in Spring and if wanna config it need to change spring.messages.basename = `filename` in application.properties

   3. In messages file, write gender.message = xx to replace {gender.message}

   4. if have multiple languages support, can set files name as ``messages_en_US.properties`` or ```messages_zh_CN.properties```

   5. Then, i18n works. If wanna test other language environment, can change it in HTTP request head. It is automatically.

### different O

Pojo: java bean, basic java class

Dao: database access object

TO: transfer object

VO: view/value object, used to encapsulation front-end object，给前端的查询返回数据的时候很可能要进行脱敏，如 password 等内容，所以专门有一个脱敏后的 java bean 对象更方便。

#### Vo

> 解决了不同层之间共享同一个数据封装对象的情况。
>
> 通常来说，POJO 是依照数据库设计的，用于数据库操作的封装对象，应当和前端版本隔离。

1. have 2 packages, vo.req, vp.resp

2. 不同的操作封装不同的 pojo

3. 对应的 controller 使用对应的 pojo。可以解决脱敏、并解决校验规则在不同 restful api 下冲突的问题。

   ```java
   @PostMapping("/employee")
   public R add(@RequestBody @Valid EmployeeAddVo vo){
       Employee employee = new Employee();
       BeanUtils.copyProperties(vo, employee);
       employeeService.saveEmployee(employee);
       return R.ok();
   }
   ```

   Spring provides **`BeanUtils.copyProperties()`** , can transfer vo to pojo designed for database

### 接口文档

Swagger 可以快速生成实时接口文档，遵循 OpenAPI。

**Knife4j** 是基于 Swagger 上的增强套件，将使用 [Knife4j](https://doc.xiaominfo.com)。

遵循文档导入依赖，cv 配置文件放进 resources/application.yaml，修改 packages-to-scan 到 controller 包即可

最后，访问 Knife4j 的文档地址：`http://ip:port/doc.html` 即可查看文档

#### 更详细的信息描述

> 使用的是 Swagger 的注解

- @Tag 标记在 Controller 类上，描述 Controller 对应的资源
- @Operation 标记在方法上，使用 summary 描述方法的作用
- @Schema 标记在 vo 的 class 上，用于描述该 vo 的作用，如 @Schema("用于提交员工修改的数据")
- @Schema 标记在 vo 的 filed 上，用于描述该属性的作用，如 @Schema("员工姓名")
- @Parameters 标记在方法上，其中使用 @Parameter 说明形参

### 日期处理

使用 @JsonFormate(pattern = "yyyy-mm-dd HH:mm:ss", timezone= "GMT+8")



## 引用

遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

[^1]: https://www.bilibili.com/video/BV14WtLeDEit/?p=33&share_source=copy_web&vd_source=732a79db14c78dbec659a1afbe66586e

[^2]: https://docs.spring.io/spring-framework/reference/web/webmvc.html