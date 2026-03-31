package org.example.ecmo.component;

import lombok.extern.slf4j.Slf4j;
import org.example.ecmo.mapper.SysUserMapper;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BloomFilterInitializer implements CommandLineRunner {
    @Autowired
    private RBloomFilter<String> userBloomFilter;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public void run(String... args) throws Exception {
        List<String> allUsernames = sysUserMapper.findAllUsernames();

        allUsernames.forEach(username->userBloomFilter.add(username));
        log.info("布隆过滤器初始化完成，共加载:{}个用户名", allUsernames.size());
        log.info("布隆过滤器配置 - 预计容量:{} ", userBloomFilter.getExpectedInsertions());
        log.info("布隆过滤器配置 - 误判率:{} ",userBloomFilter.getFalseProbability());


    }
}
