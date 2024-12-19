# Mybatis[^1]

## Mybatis VS JDBC

在 jdbc 的情况下，dao 需要准备接口和实现类。但在 Mybatis 中，只需要准备接口和接口的映射文件，省略了实现类的过程。

映射文件需要准备在 resources 目录下，可以通过 **mybatisx** 插件自动完成配置文件的生成。



## Mybatis Hello World 

1. add basic dependencies from spring boot starter

   - Mybatis framework
   - Mysql Driver
   - Lombok (optional)
   - Spring web (optional)

2. link to dabatase in application.properties

3. prepare pojo and dao

4. write dao interface, and use **@Mapper** annotation to tell Spring that this interface is used by Mabatis to operate database

   ```java
   @Mapper
   public interface EmpMapper {
       Emp getEmpById(Integer id);
   }
   ```

5. use MabatisX to generate counterpart mapper into resources/mapper, like:

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
   <mapper namespace="com.forty2.training.mybatis.helloworld.dao.EmpMapper">
       
   </mapper>
   ```

   namespace is the full name of interface, used to bind xml and interfaces.

6. In application.properties, tell Mybatis the location of xml files.

   ```java
   mybatis.mapper-locations=classpath:mapper/**.xml
   ```

7. open `mybatis.configuration.map-underscore-to-camel-case=true`

### basic Operation

#### select

```xml
<!-- Emp getEmpById(Integer id); -->
<select id="getEmpById" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select *
    from t_emp
    where id = #{id}
</select>
```

in this case, id indicates the method in the interface, resultType means the type of result, which should be filled by full class name.

```xml
<!--     List<Emp> getAllEmps(); -->
<select id="getAllEmps" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select * from t_emp
</select>
```

返回值是集合，resultType 依旧写集合中元素的类型 

#### update

```xml
<!-- void updateEmp(Emp emp); -->
<update id="updateEmp">
    update t_emp
    set emp_name   = #{empName},
        age        = #{age},
        emp_salary = #{empSalary}
    where id = #{id}
</update>
```

#### insert

```xml
<!-- void addEmp(Emp emp); -->
<insert id="addEmp">
    insert into t_emp(emp_name, age, emp_salary)
        value (#{empName}, #{age}, #{empSalary})
</insert>
```

#### delete

```xml
<!-- void deleteEmpById(Integer id); -->
<delete id="deleteEmpById">
    delete
    from t_emp
    where id = #{id}
</delete>
```



## open Logging 日志

if wanna see sql statement in terminal, can set in application.properties

```properties
logging.level.com.forty2.training.mybatis.helloworld.mapper = debug
```

**logging.level** used to set different log levels, and after this to **set the target package**.

In this case, log info above debug level from com.forty2.training.mybatis.helloworld.mapper will be shown in the terminal.

``` 
2024-11-12T16:26:43.278+08:00 DEBUG 27702 --- [mybatis-01-helloworld] [           main] c.f.t.m.h.m.EmpMapper.deleteEmpById      : ==>  Preparing: delete from t_emp where id = ?
2024-11-12T16:26:43.289+08:00 DEBUG 27702 --- [mybatis-01-helloworld] [           main] c.f.t.m.h.m.EmpMapper.deleteEmpById      : ==> Parameters: 3(Integer)
2024-11-12T16:26:43.290+08:00 DEBUG 27702 --- [mybatis-01-helloworld] [           main] c.f.t.m.h.m.EmpMapper.deleteEmpById      : <==    Updates: 0
```



## 自增 id 回填

```xml
<!-- void addEmp(Emp emp); -->
<insert id="addEmp" useGeneratedKeys="true" keyProperty="id">
    insert into t_emp(emp_name, age, emp_salary)
        value (#{empName}, #{age}, #{empSalary})
</insert>
```

useGeneratedKeys 声明本次自增的 id 将是数据库自动生成的。

keyProperty 指明取回此自增 id 时装入传入对象的哪个属性。

```java
@Test
public void test2() {
    Emp emp = new Emp();
    emp.setEmpName("xu");
    emp.setAge(33);
    emp.setEmpSalary(8800.00);

    empMapper.addEmp(emp);
    System.out.println(emp.getId());
}
```

output: 5



## 参数传递

### #{}

底层使用 preparedStatement 完成，先生成占位符，后填充。无注入风险。

```
Preparing: select * from t_emp where id = ?
Parameters: 1(Integer)
Total: 1
```

### ${}

底层使用字符串直接拼接成 sql 语句，有注入风险

``````
Preparing: select * from t_emp where id = 1
Parameters: 
Total: 1
``````



### 最佳实践

为每一个接口中的形参使用 @Param 指定别名，此别名将供 xml 中 #{别名} 使用。

```java
getEmployee(@Param("id") int id, @Param("name") String name);
```

xml 中映射为 #{id}，此处 id 取自 @Param 的参数。



## 表名拼接

```xml
<!-- Emp getEmpByIdWithTableName(Integer id, String tableName); -->
<select id="getEmpByIdWithTableName" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select * from #{tableName} where id = #{id}
</select>
```

此种方法无法将表名使用 preparedStatement 填充，**表名位置需要使用 ${} 拼接**。



## 结果封装

> 返回对象或普通类型：resultType = 全类名
>
> 返回集合：resultType = 集合中元素的全类名

### Basic

List、基本数据类型、pojo 等用**全类名**书写。基本数据类型 Mybatis 提供了简化版。

### Map

**Map 的 resultType 依旧是集合内元素对象的全类名**，key 可以通过 @MapKey("") 指定。

#### 错误

如果如下写，resultType="java.util.Map"，虽然能成功获取到 Emp 对象，但在获取 class 时会发现是 HashMap。

```xml
<!-- @MapKey("id")
     Map<Integer, Emp> getAllEmpsMap(); -->
<select id="getAllEmpsMap" resultType="java.util.Map">
    select * from t_emp
</select>
```

```java
@Test
public void test7() {
    Map<Integer, Emp> allEmpsMap = empMapper.getAllEmpsMap();
    System.out.println(allEmpsMap.get(2).getClass());
}
```

output: java.lang.ClassCastException: class **java.util.HashMap** cannot be cast to class com.forty2.training.mybatis.helloworld.pojo.Emp 

#### 正确

所以正确的写法是，即使返回值是装进 **Map 里**，依旧使用集合内**元素的类型作为 resultType**，并使用 @MapKey 指定 k 值，Mybatis 自动填充 v 值。

```java
<!-- @MapKey("id")
     Map<Integer, Emp> getAllEmpsMap(); -->
<select id="getAllEmpsMap" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select * from t_emp
</select>
```

```java
@Test
public void test7() {
    Map<Integer, Emp> allEmpsMap = empMapper.getAllEmpsMap();
    System.out.println(allEmpsMap.get(2).getClass());
}
```



## ResultMap

### 场景

Pojo 属性和数据库中的字段无法对应时，封装为 null

解决方法：

1. 在 sql 中使用别名
2. 使用驼峰命名的映射
3. 使用 ResultMap（自定义结果集）

### 使用

定义 resultMap。第一个 id 用于映射，type 为管理的 pojo 的类型。第二个 id 声明主键的映射规则，result 声明数据库中列与 pojo 的属性的映射规则。

而后在 select 标签的 resultMap 处完成 id 映射即可。

```java
<resultMap id="EmpRM" type="com.forty2.training.mybatis.helloworld.pojo.Emp">
    <id column="id" property="id"/>
    <result column="emp_name" property="empName"/>
    <result column="age" property="age"/>
    <result column="emp_salary" property="empSalary"/>
</resultMap>

<select id="getEmpById" resultMap="EmpRM">
    select *
    from t_emp
    where id = #{id}
</select>
```

### 最佳实践

1. 开启驼峰命名
2. 对于驼峰命名搞不定的复杂关系，用 resultMap 来完成自定义映射



## 关联查询

### association 一对一

指定自定义对象的封装规则。一般用于联合查询中**一对一关系**的封装，比如一个订单只能对应一个客户。

每行 new 一个新对象映射好返回。

- javaType 指定关联的 bean 的类型
- select 指定分步查询调用的方法
- 指定分步查询传递的参数列

#### 案例

按照 id 查询订单及下单的客户信息。一对一关系。

1. 向其中一方加入另一方的主键。如，给 order 中加入属性 customerId。

2. 定义 resultMap 的映射规则，将另一个对象的属性和 sql 中返回的某些列映射。其中，用 association 标签嵌套映射主 pojo 中的对象，property 为对应的属性名，javaType 说明类型，供反射使用。反射会调用空参构造器，然后按照子标签一一赋值。

   ```xml
   <resultMap id="OrderRM" type="com.forty2.training.mybatis.helloworld.pojo.Order">
       <id column="id" property="id"/>
       <result column="address" property="address"/>
       <result column="amount" property="amount"/>
       <result column="customer_id" property="customerId"/>
       <association property="customer" javaType="com.forty2.training.mybatis.helloworld.pojo.Customer">
           <id column="c_id" property="id"/>
           <result column="customer_name" property="customerName"/>
           <result column="phone" property="phone"/>
       </association>
   </resultMap>
   ```

### Collection 一对多

指定自定义对象的封装规则。一般用于联合查询中**一对多关系**的封装，比如一个用户对应着多个订单。

数据库的查询结果可能有很多行，但前面的 customer 信息都是相同的，区别只是后面不同的 order。所以 Mybatis 在查到第二行时，就不会再创建新的 customer 对象装结果，而是在发现 collection 标签之后，自动把接下来每行里的 order 信息 new 新对象封装好，依次放进 List orders 里。这就是 collection 标签的意义。

- ofType 指定集合中 bean 的类型

#### 案例

按照 id 查询客服以及客户下单的所有信息。一对多关系，一个客户可能有很多订单。

1. 向客户中添加一个 List 用于存放所有 Order。

2. 定义 resultMap，使用 Collection。

   ```xml
   <resultMap id="CusRM" type="com.forty2.training.mybatis.helloworld.pojo.Customer">
       <id column="c_id" property="id"/>
       <result column="customer_name" property="customerName"/>
       <result column="phone" property="phone"/>
       <collection property="orders" ofType="com.forty2.training.mybatis.helloworld.pojo.Order">
           <id column="id" property="id"/>
           <result column="address" property="address"/>
           <result column="amount" property="amount"/>
           <result column="customer_id" property="customerId"/>
       </collection>
   </resultMap>
   
   <select id="getCustomerByIdWithOrders" resultMap="CusRM">
       select c.id c_id,
              c.customer_name,
              c.phone,
              o.*
       from t_customer c
                left join t_order o on c.id = o.customer_id
       where c.id = #{id}
   </select>
   ```

### 分步查询

在 association 和 collection 封装过程中，可以使用 select + column 指定分步查询逻辑

- select 指定分步查询调用的方法
- column 指定分步查询传递的参数
  - 传递单个：直接写列名，表示将这列的值作为参数传递给下一个查询
  - 传递多个：column="{prop1=col1, prop2=col2}"，下一个查询使用 prop1、prop2 取值

#### 案例

按照 id 查询客户 以及 他下的所有订单

##### 原生写法

1. 分别查询客户信息、该客户 id 下的订单信息

   ```java
   @Mapper
   public interface OrderCustomerStepMapper {
       Customer getCustomerById(Long id);
   
       List<Order> getOrdersByCustomerId(Long customerId);
   }
   ```

   ```xml
   <select id="getCustomerById" resultType="com.forty2.training.mybatis.helloworld.pojo.Customer">
       select *
       from t_customer
       where id = #{id}
   </select>
   
   <select id="getOrdersByCustomerId" resultType="com.forty2.training.mybatis.helloworld.pojo.Order">
       select *
       from t_order
       where customer_id = #{customerId}
   </select>
   ```

2. 获得客户信息、该客户的所有订单，在 test 或 service 中手动封装

   ```java
   @Test
   public void test1() {
       Customer customer = orderCustomerStepMapper.getCustomerById(1L);
       List<Order> orders = orderCustomerStepMapper.getOrdersByCustomerId(customer.getId());
       customer.setOrders(orders);
       System.out.println(customer);
   }
   ```

此处可见，是我们手动调用两次方法完成的。Mybatis 支持利用自动分布查询机制，将手动调用变为自动调用。

##### 自动分步查询

###### column 单值

```java
Customer getCustomerByIdAndOrderStep(Long id);
```

```xml
  <resultMap id="CustomerOrderStepRM" type="com.forty2.training.mybatis.helloworld.pojo.Customer">
      <id column="id" property="id"/>
      <result column="customer_name" property="customerName"/>
      <result column="phone" property="phone"/>
      <collection property="orders"
                  ofType="com.forty2.training.mybatis.helloworld.pojo.Order" select="com.forty2.training.mybatis.helloworld.mapper.OrderCustomerStepMapper.getOrdersByCustomerId" column="id"
      >
        
      </collection>
  </resultMap>

  <select id="getCustomerByIdAndOrderStep" resultMap="CustomerOrderStepRM">
      select *
      from t_customer
      where id = #{id}
  </select>
```

因为 Customer 中有 List orders，所以直接 select * 即可，自动分步查询可将第二步查回的 order 数组封装给 Customer 中的 List。

当 collection 有 select 参数时，means 封装 orders 属性时，该属性应当是一个集合。但这个集合需要用另一个 sql 方法来查询，另一个 sql 的返回值作为该集合的值。

**collection 的参数**分别为：

1. property 用于对应 Customer 的属性；
2. ofType 指明集合的元素类型；
3. select 指明第二步自动查询时调用哪个方法。在方法执行完成时，会自动把结果封装进 collection 对应的 property 中。
4. column 用于传参，其内容的含义是 将第一次查询结果中的哪一列 作为参数 传递给第二次查询。

*output*:

```bash
==>    Preparing: select * from t_customer where id = ?
==>    Parameters: 1(Long)
====>  Preparing: select * from t_order where customer_id = ?
====>  Parameters: 1(Long)
<====  Total: 2
<==    Total: 1

Customer(id=1, customerName=张三, phone=13100000000, orders=[Order(id=1, address=西安市雁塔区, amount=99.98, customerId=1, customer=null), Order(id=2, address=北京市, amount=199.00, customerId=1, customer=null)])
```

###### column 多值

```java
List<Order> getOrdersByCustomerId(@Param("cid") Long customerId, @Param("name_") String name);
```

```xml
column="{cid=id, name_=customer_name}"
```

前为 param 指定的别名，后为第一次查询时返回的查询结果的列名。

###### 超级分步天坑

超级分步（多次分步）是，**务必保证最后一次方法的调用为 resultType 的**，来暂停 select 之间的循环调用。否则将会 StackOverFlow。

因为如触发循环调用，resultMap 会再调用其中的 collection 中的 select，而 select 中的 collection 又会调用下一次。

而使用 resultType 会按照默认规则封装，不会再存在 resultMap 中的 collection，自然也就不会再存在分步查询，所以令超级分步的最后一步为 resultType 则安全。

#### 延迟加载

假设 customer 中有 customer info & order info。

在延迟加载**未开启**时，即使操作只用了分步查询第一步中的 customer info such as customer_id，处于分步查询第二步的 order info 依旧会被自动执行，按照 resultMap 的规则封装进 customer，但其实功能中根本用不到 order info，第二步查询在这个 case 中是冗余的。

所以延迟加载（kind of 懒加载）出现了。

在延迟加载**开启**的情况下，如果程序中没有访问位于分步查询第二步的 order info，那么分步查询的第二步就暂时不会被执行（类似于挂起），直到 order info 相关的内容被访问时，分步查询的第二步才会开始执行。

##### 全局配置

在 application.properties 中有两条和 [mybatis 延迟加载相关的配置信息](https://mybatis.org/mybatis-3/configuration.html)

```properties
mybatis.configuration.lazy-loading-enabled=true
mybatis.configuration.aggressive-lazy-loading=false
```

| Setting               | Description                                                  | Valid Values  | Default                |
| --------------------- | ------------------------------------------------------------ | ------------- | ---------------------- |
| lazyLoadingEnabled    | Globally enables or disables lazy loading. **When enabled, all relations will be lazily loaded.** This value can be superseded for a specific relation by using the `fetchType` attribute on it. | true or false | false                  |
| aggressiveLazyLoading | **When enabled, any method call will load all the lazy properties of the object.** Otherwise, each property is loaded on demand (see also `lazyLoadTriggerMethods`). | true or false | false (true in ≤3.4.1) |



## 动态 Sql

### if & where

> 查询员工信息，按照员工名、薪资或员工名薪资来查询。

在原生写法下，需要使用三个方法，其中之一 eg like。name 或 salary 任意为空都会导致该 sql 缺少某字段而失效。

```xml
<!--Emp selectEmpByEmpNameAndSalary(@Param("name") String name, @Param("salary") BigDecimal salary);-->
<select id="selectEmpByEmpNameAndSalary" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select *
    from t_emp
    where emp_name = #{name}
      and emp_salary = #{salary}
</select>
```

为了解决代码的可复用性，引入了动态 sql。本案例可写为：

```xml
<!--Emp selectEmpByEmpNameAndSalary(@Param("name") String name, @Param("salary") BigDecimal salary);-->
<select id="selectEmpByEmpNameAndSalary" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select *
    from t_emp
    <where>
        <if test="name!=null">
            and emp_name = #{name}
        </if>
        <if test="salary != null">
            and emp_salary = #{salary}
        </if>
    </where>
</select>
```

使用 if 标签，来把符合 test 条件的 sql 条件拼装进最后的 sql 语句里。

where 标签能够根据 有没有 if 生效，决定 where 是否出现，同时可以消除 where 标签中多余的 and 和 or。

### if & set

> 更新员工

本案例中，原生写法如下。本写法导致的问题是，如果传入的 Emp pojo 中的某些值未使用 setter 赋值，为 null，则数据库中的也会被修改为 null。但现实情况是，大部分需要使用 update 的场景，往往 pojo 中只会 set 主键和需要被修改的值。使用原生方法需要将不变的值也赋值，繁琐，故引入动态 sql。

```xml
<!--void updateEmp(Emp emp);-->
<update id="updateEmp">
    update t_emp
    set emp_name   = #{empName},
        age        = #{age},
        emp_salary = #{empSalary}
    where id = #{id}
</update>
```

与上例原理相同，可写为如下。if 判断是否拼接进 sql，set 决定自身是否出现，及处理其中的语法问题，如多逗号等

```xml
<!--void updateEmp(Emp emp);-->
<update id="updateEmp">
    update t_emp
    <set>
        <if test="empName != null">
            emp_name = #{empName},
        </if>
        <if test="age!=null">
            age = #{age},
        </if>
        <if test="empSalary!=null">
            emp_salary = #{empSalary},
        </if>
    </set>
    where id = #{id}
</update>
```

### choose.. when.. otherwise

本质上，choose when otherwise 就是一个自带 break 的 switch case。

choose 是 switch，when 是 case 和条件，执行后自动 break，otherwise 是 default。

```xml
<!--Emp queryEmpByNameAndSalaryWhen(@Param("name") String name, @Param("salary") BigDecimal salary);-->
<select id="queryEmpByNameAndSalaryWhen">
    select * from t_emp
    <where>
        <choose>
            <when test="name != null">
                emp_name = #{name}
            </when>
            <when test="salary > 3000">
                emp_salary = #{salary}
            </when>
            <otherwise>
                id = 1
            </otherwise>
        </choose>
    </where>
</select>
```

### for each

用来遍历、循环。常用于批量插入场景及批量执行单个 sql。

#### 批量查询

> 查询指定 id 集合中的员工

sql 的原生写法为

```sql
select * from t_emp where id IN (1,3,5,7)
```

似乎 mybatis 中应当写为如下代码。但如何确定 List  ids 集合的长度一定为 4 呢？如果长度不同就不适用？于是提供了 for each 的 dynamic sql from mybatis

```xml
<!--List<Emp> queryEmpsByIds(@Param("ids") List<Integer> ids);-->
<select id="queryEmpsByIds" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select *
    from t_emp
    where id IN (#{ids[0]}, #{ids[1]}, #{ids[2]}, #{ids[3]})
</select>
```

dynamic sql like

```xml
<!--List<Emp> queryEmpsByIds(@Param("ids") List<Integer> ids);-->
<select id="queryEmpsByIds" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select *
    from t_emp
    where id IN (
    <foreach collection="ids" item="id" separator=",">
        #{id}
    </foreach>
    )
</select>
```

**foreach** 标签能用来遍历 List Map 数组等各种集合。

其中 **collection** 用于绑定形参位置的集合，**item** 等于对每个集合元素称呼，same as `for(Integer id : ids)` /  `for(Object item : collection)` 这种增强 for 的写法。

在 foreach 中，直接写 #{item} 即可。这样就能实现类似 `select * from t where id in(item, item ...)` 的形式。

由于 in 中每个元素都需要以逗号分隔，所以可以使用 **separator** 属性来指定分隔符。

还有两个隐藏属性，分别是 **open** 和 **close**，用于指定 foreach 的前缀后缀。使用这两个属性可以简化可读性和行数为下。同时，当使用 open + close 将 `where id IN ()` 装入时，不开始遍历则这些内容不会出现，更合理。

```xml
<!--List<Emp> queryEmpsByIds(@Param("ids") List<Integer> ids);-->
<select id="queryEmpsByIds" resultType="com.forty2.training.mybatis.helloworld.pojo.Emp">
    select *
    from t_emp
    <foreach collection="ids" item="id" separator="," open="where id IN (" close=")">
        #{id}
    </foreach>
</select>
```

此外，如果 collection 为空，将报错。所以将 foreach 放入 if 中，判断一下集合是否为空，不为空才进入 foreach 更合理。此处代码省略不表。

#### 批量插入

> 批量插入集合中的员工

动态 sql 的批量添加如下：

```xml
<!--void insertEmps(@Param("emps") List<Emp> emps);-->
<insert id="insertEmps">
    insert into t_emp(emp_name, age, emp_salary)
    <if test="emps!=null">
        <foreach collection="emps" item="emp" separator="," open="values">
            (#{emp.empName},#{emp.age},#{emp.empSalary})
        </foreach>
    </if>
</insert>
```

#### 批量修改

> 批量修改 id 为 xx 的员工名为 员工名+id

原生 sql 为

```sql
update t_emp set emp_name = 'zhang1' where id = 1;
update t_emp set emp_name = 'zhang2' where id = 2;
update t_emp set emp_name = 'zhang3' where id = 3;
```

动态 sql 为

```xml
<!--void updateEmps(@Param("emps") List<Emp> emps);-->
<update id="updateEmps">
    <foreach collection="emps" item="emp" separator=";">
        update t_emp
        <set>
            <if test="emp.empName != null">
                emp_name = #{emp.empName},
            </if>
            <if test="emp.empSalary != null">
                emp_salary = #{emp.empSalary},
            </if>
            <if test="emp.age != null">
                age = #{emp.age},
            </if>
        </set>
        where id = #{emp.id}
    </foreach>
</update>
```

sql 语法无问题但报错 org.springframework.jdbc.**BadSqlGrammarException**，原因为 数据库连接层面 不支持一条查询中包含多 sql

在 application.properties 的 sql url 中开启即可。

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mybatis-example?allowMultiQueries=true
```

**在 url 后使用 ?参数** 的形式，将 **allowMultiQueries=true** 携带上。之后解决

### Sql 片段

可以将所有可以复用的 sql 片段抽取出来统一管理，减少冗余代码的编写，同时统一管理也更方便

sql 用于抽取可复用的片段，include 用于引用可复用的片段。refid 指向 sql 片段的 id。

```xml
<sql id="names">
    需要抽取的复用内容
</sql>
```

```xml
select
	<include refid="names"/>
```

此时 select 中的 include 就会被替换成 sql 片段中的内容。



## 缓存机制

Mybatis 拥有两级缓存机制

一级缓存默认开启，其事务级别是 **当前事务共享**

二级缓存需要手动开启，其事务级别是 **所有事务共享**



## 插件机制

Mybatis 底层使用拦截器 interceptor 提供插件功能，在其 jar 包下的 plugins 包中可见。方便用户在 sql 执行前后进行拦截、增强。

拦截器可以拦截 **四大对象** 的执行：

- ParameterHandler 处理 sql 的参数对象
- ResultSetHandler 处理 sql 返回的结果集
- StatementHandler 数据库的处理对象，用于执行 sql 语句
- Executor Mybatis 的执行器，用于执行增删改查操作



## 分页插件

[PageHelper](https://pagehelper.github.io) 是可以用在 Mybatis 中的一个强大的分页插件。其就是利用 Mybatis 的插件机制，在底层编写了分页的 Interceptor，每次 sql 查询之前会自动拼装分页数据。

参见[文档](https://pagehelper.github.io) 或 [视频](【尚硅谷2024雷神版SSM教程，基于AI的全新ssm框架实战-哔哩哔哩】 https://b23.tv/3qHtbCx) 使用即可。



## 逆向工程

Mybatisx 插件提供了逆向生成功能，可以根据数据库表一键生成常用的 crud 和 pojo。



## 日志

application.properties 中，logging.level.com.xx = debug



## CRUD 作业

### 技术栈

SpringBoot + Spring + SpringMVC + Mybatis

### 要求

1. 员工表 ✅
2. 基本的 CRUD ✅
3. 分页查询 ✅
4. RESTful 风格 ✅
5. 数据校验 ✅
6. 全局统一异常处理（业务异常及校验异常）✅
7. 引入 VO 分层
8. 引入 swagger 文档
9. 在接口文档中完成测试
10. 解决跨域资源共享问题
11. 前端工程化的实现



## 引用

遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

[^1]: https://www.bilibili.com/video/BV14WtLeDEit/?p=33&share_source=copy_web&vd_source=732a79db14c78dbec659a1afbe66586e
