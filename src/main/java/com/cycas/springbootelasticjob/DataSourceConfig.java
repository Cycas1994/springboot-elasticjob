package com.cycas.springbootelasticjob;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author naxin
 * @Description:
 * @date 2020/10/2915:22
 */
@Configuration
public class DataSourceConfig {

    @Bean("datasource")
    @ConfigurationProperties("spring.datasource.druid.log")
    public DataSource dataSourceTow() {
        return DruidDataSourceBuilder.create().build();
    }
}
