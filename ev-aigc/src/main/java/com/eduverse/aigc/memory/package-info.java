/*
这段代码的主要目的是启用 Spring Framework 提供的 null-safety（空安全）检查，
为 com.eduverse.aigc.memory 这个包下的所有类、方法和字段提供关于 null 值的编译时或静态分析时的约束和提示。
*/
@NonNullApi// 这个注解声明了该包内所有方法的参数（parameters）和返回值（return values） 默认情况下不应该为 null
@NonNullFields// 这个注解声明了该包内所有字段（fields） 默认情况下不应该被初始化为 null，并且在访问时也不应该期望其为 null
package com.eduverse.aigc.memory;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;