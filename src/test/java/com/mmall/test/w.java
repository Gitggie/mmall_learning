package com.mmall.test;

import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author Wuleijian
 * @Date 2018/4/18 18:31
 * @Description
 */
@ContextConfiguration("classpath:applicationContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class w {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testDao() {
        User user = userMapper.selectByPrimaryKey(1);
        System.out.println(user);
    }
}
