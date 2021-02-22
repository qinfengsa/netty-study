package com.qinfengsa.common.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * @author qinfengsa
 * @date 2021/02/14 18:55
 */
@Data
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 5728734191467889456L;

    private int id;

    private String name;

    private int age;

    private String addr;
}
