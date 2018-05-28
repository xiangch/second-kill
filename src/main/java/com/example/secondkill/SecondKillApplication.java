package com.example.secondkill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

@SpringBootApplication
@RestController
public class SecondKillApplication {
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Resource(name = "stringRedisTemplate")
	private ValueOperations<String, String> strOps;
	@Autowired
	private RedisTemplate redisTemplate;
	@Resource(name = "redisTemplate")
	private ValueOperations<String, Object> objOps;

	@GetMapping("/set")
	public String set(String key, String val) {
		strOps.set(key, val);
		return val;
	}

	@GetMapping("/get/{key}")
	public String get(@PathVariable String key) {
		return strOps.get(key);
	}


	@GetMapping("/init")
	public String initStock() {
		objOps.set("stock", new Stock(1, "手机", 10, 0, 0));
		redisTemplate.delete("orderSet");
		for(int i=0;i<10;i++) {
			redisTemplate.delete("lock."+i);
		}
		return "init success";
	}

	@GetMapping("/stock")
	public Stock stock() {
		return getStock();
	}

	@GetMapping("/orders")
	public Set<StockOrder> orders() {
		return getOrderSet();
	}

	@GetMapping("/createOrder")
	public ResponseEntity<StockOrder> createOrder() {

		//校验库存
		Stock stock = checkStock();
		//扣库存
		saleStock(stock);
		//创建订单
		return  ResponseEntity.ok(createOrder(stock));
	}
	private StockOrder createOrder(Stock stock){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StockOrder stockOrder = new StockOrder(stock.getVersion(),stock.getId(),stock.getName(),sf.format(new Date()));
		addOrderSet(stockOrder);
		return stockOrder;
	}
	private Stock checkStock() {
		Stock stock = getStock();
		if (stock.getCount() == stock.getSale()) {
			throw new RuntimeException("库存不足");
		}
		return stock;
	}

	private void saleStock(Stock stock){
		boolean lock = objOps.setIfAbsent("lock."+stock.getVersion(),new Lock(System.currentTimeMillis(),System.currentTimeMillis()+100));
		if(!lock){
			throw new RuntimeException("库存已被更新");
		}

		stock.setSale(stock.getSale()+1);
		stock.setVersion(stock.getVersion()+1);
		objOps.set("stock",stock);
	}


	private Stock getStock() {
		return (Stock) objOps.get("stock");
	}

	private Set<StockOrder> getOrderSet(){
		return redisTemplate.opsForSet().members("orderSet");
	}
	private void addOrderSet(StockOrder order){
		 redisTemplate.opsForSet().add("orderSet",order);
	}
	@Bean
	public CommandLineRunner inits(){
		return strings -> initStock();
	}

	public static void main(String[] args) {
		SpringApplication.run(SecondKillApplication.class, args);
	}
}
