package com.example.dhcpms.controller;

import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Owner 控制器
 */
@RestController
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    /**
     * 查询所有业主
     */
    @GetMapping("/list")
    public List<Owner> list() {
        return ownerService.list();
    }

    /**
     * 根据 ID 查询业主
     */
    @GetMapping("/{id}")
    public Owner getById(@PathVariable Long id) {
        return ownerService.getById(id);
    }

    /**
     * 新增业主
     */
    @PostMapping
    public boolean save(@RequestBody Owner owner) {
        return ownerService.save(owner);
    }

    /**
     * 更新业主
     */
    @PutMapping
    public boolean update(@RequestBody Owner owner) {
        return ownerService.updateById(owner);
    }

    /**
     * 删除业主
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return ownerService.removeById(id);
    }


    // ====================== 新增：测试接口 ======================
    /**
     * POST测试接口
     */
    @PostMapping("/test01")
    public String test(@RequestBody String body) {
        // 后端打印"post已请求"
        System.out.println("post已请求，请求体内容：" + body);
        return "post请求成功";
    }

    /**
     * GET测试接口
     */
    @GetMapping("/test02")
    public String get() {
        // 后端打印"get已请求"
        System.out.println("get已请求");
        return "get请求成功";
    }
}
