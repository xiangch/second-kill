package com.example.secondkill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zengxc
 * @since 2018/5/28
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Stock implements Serializable{
	private int id;
	private String name;
	private  int count;
	private int sale;
	private int version;
}
