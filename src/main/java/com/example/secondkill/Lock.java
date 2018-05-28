package com.example.secondkill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zengxc
 * @since 2018/5/28
 */
@Getter
@Setter
@AllArgsConstructor
public class Lock  implements Serializable {
	private long currTime;
	private long timeOut;
}
