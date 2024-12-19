# Lambda
## 函数对象化

```java
interface Lamda {  
    int calculate(int a, int b);  
}  
  
public class WhatIsLambda {  
    public static void main(String[] args) {  
  
        Lamda lamda = (a, b) -> a + b;  
        System.out.println(lamda.calculate(1, 2));  
    }  
}
```

在本例中，`(a, b) -> a + b` 是函数本身，传入参数 a 和 b，return a + b。其参数和返回值属性可以通过接口中的方法来确定。

该表达式用多态接受了一个 interface Lamda 类型的 **函数对象**，该函数对象可以作为参数传递，这是 **函数对象化** 的意义。

通过接口中的方法名，即可以调用该方法。

## 行为参数化

### Lambda 的基本语法

> 函数对象 = 参数 -> 逻辑部分
> 
> 如果逻辑部分只有一行，则逻辑部分整个会被作为返回结果

`student.age < 18` 的函数对象为， `student -> student.age < 18`

`student.sex.equals('M')` 的函数对象为，`student -> student.sex.equals('M')`

有函数对象，则需要一个接口用于执行该对象。由于上述函数对象的形参均为 Student 对象，返回值均为布尔类型，所以接口方法应当匹配。

```java
interface Lambda{
	boolean test(Student student);
}
```

```java
public class ActionAsParam {  
    static List<Student> filter(List<Student> students, Lambda lamda) {  
        List<Student> filteredStudents = new ArrayList<>();  
        for (Student student : students) {  
            if (lamda.test(student)) {  
                filteredStudents.add(student);  
            }  
        }  
        return filteredStudents;  
    }  
  
    public static void main(String[] args) {  
        List<Student> students = new ArrayList<>();  
        students.add(new Student(1, "Li", 23));  
        students.add(new Student(2, "Zhang", 26));  
  
        System.out.println(filter(students, student -> student.getName().equals("Li")));  
    }  
}
```

在本代码中，首先编写 filter 作为过滤器，传入 Student List 和 lambda 函数对象，按照 lambda 函数对象执行后的结果来完成过滤。这样实现了一个 filter 可以按不同规则过滤，只需要将规则（行为）对象化后作为参数传入即可。

`student -> student.getName().equals("Li"))` 就是传入的函数。其参数是 Student，返回值是布尔类型，执行后会被 if 接收布尔类型来完成余下判断操作。

此处箭头左侧的参数，箭头右侧 return 的返回值都与 Lambda 接口中唯一的方法相同，所以可以用该方法名，来调用这个函数对象。

使用这种行为参数化的写法，可以不用将判断逻辑等行为抽取出来，作为传入的参数。解耦并提高了复用性。

## 延迟执行

> 没听懂，跳过了，回头用了再听 
> 
> 【黑马程序员Java函数式编程全套视频教程，Lambda表达式、Stream流、函数式编程一套全通关】 【精准空降到 07:10】 https://www.bilibili.com/video/BV1fz421C7tj/?p=5&share_source=copy_web&vd_source=732a79db14c78dbec659a1afbe66586e&t=430

此处示例 code 为 `logger.debug({}, () -> expensive());`

利用 () -> expensive() 函数对象 替换直接传入的 expensive() 函数来实现延迟执行。

## Lambda 表达式 及 方法引用 

### Lambda

1. `(int a, int b) -> a + b;` 
   
   明确指出参数类型，业务逻辑只有一行，自动作为返回值
   
2. `(int a, int b) -> {int c = a + b; return c;}` 
   
   代码多余一行，需要括号及 return
   
3. `(a, b) -> a + b;` 
   
   通过代码上下文（上下文中其接口）能确认参数、返回值类型，则省略不写。该函数对象的类型是其接口类型。
   
   ```java
   interface Lambda{
	   int op(int a, int b);
   }

	Lambda lambda = (a, b) -> a + b;
	```

### 方法引用


> 左侧既可以是类型名，也可以是对象。
> 
> 右侧需要是方法名
> 
> 方法引用展开后是什么样，只需要确认其参数即可。确认参数后其他都可根据参数推导出。

1. `Math::max`  等价于 (int a, int b) -> Math.max(a, b); 是静态方法。
   
   因为是逻辑部分是单行，所以将返回一个该行函数的结果。 返回值类型同函数定义。
   
2. `Student::getName` 等价于 (Student stu) -> stu.getName(); 非静态方法。
   
   因为是逻辑部分是单行，所以将返回一个该行函数的结果。返回值类型同函数定义。
   
3. `System.out::println` 等价于 (Object o) -> System.out.println(o); 非静态方法。
   
   此例中，System.out 是对象。println 的定义是 void，所以返回为 void。

4. `Student::new` 等价于 () -> new Student(); 非静态方法。
   
   因为是逻辑部分是单行，所以将返回一个该行函数的结果。返回值类型同函数定义。
   
### 函数式接口

> **仅包含一个抽象方法**
> 
> 用 @FunctionalInterface 注解检查

将函数对象归为一类之后，用 函数式接口 来表示他们的类型

被归为一类的函数对象，要：

1. 参数个数类型相同
2. 返回值类型相同

JDK 提供的常见函数式接口：

 1. Runnable 
    () -> void
    
 2. Callable
    () -> T
    
 3. Comparator
    (T, T) -> int
    
 4. ﻿﻿﻿Consumer, BiConsumer, IntConsumer, LongConsumer, DoubleConsumer
    (T) -> void, Bi 是两参, Int 指参数是 int
    
 5. Function, BiFunction, Int Long Double ...  
    (T) -> R
    
 6. Predicate, BiPredicate, Int Long Double ...  
    (T) -> boolean
    
 7. Supplier, Int Long Double ...  
    () -> T
    
 8. UnaryOperator, BinaryOperator, Int Long Double ...  
    (T) -> T, Unary 一参