package com.example.PosSystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PosSystemApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void productImagesTableHasPrimaryKey() {
		Integer primaryKeyColumnCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
				WHERE UPPER(TABLE_NAME) = 'PRODUCT_IMAGES'
				  AND CONSTRAINT_TYPE = 'PRIMARY KEY'
				""", Integer.class);

		assertThat(primaryKeyColumnCount).isNotNull().isGreaterThan(0);
	}

}
