package com.example.dhcpms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dhcpms.common.Result;
import com.example.dhcpms.entity.Owner;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.mapper.OwnerMapper;
import com.example.dhcpms.service.OwnerService;
import com.example.dhcpms.service.UserService;
import com.example.dhcpms.util.JwtUtil;
import com.example.dhcpms.vo.OwnerVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User 控制器（整合：登录/注册/验证码 + 业主信息管理）
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OwnerMapper ownerMapper;


    // ===================== 原有基础用户CRUD接口 =====================
    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /**
     * 根据 ID 查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<?> save(@RequestBody User user) {
        boolean success = userService.save(user);
        return success ? Result.success("新增用户成功") : Result.error("新增用户失败");
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Result<?> update(@RequestBody User user) {
        boolean success = userService.updateById(user);
        return success ? Result.success("更新用户成功") : Result.error("更新用户失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("删除用户成功") : Result.error("删除用户失败");
    }

    // ===================== 登录接口 =====================
    /**
     * 登录接口（纯业务版）
     * @param request 登录请求参数（支持两种登录方式）
     * @return 统一格式的响应
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody(required = false) Map<String, String> request) {
        try {
            log.info("登录请求参数：{}", request);

            // 1. 基础参数判空
            if (request == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "登录参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            // 2. 提取核心参数
            String loginType = request.get("loginType") != null ? request.get("loginType").trim() : "";
            String roleStr = request.get("role") != null ? request.get("role").trim() : "";
            boolean loginSuccess = false;
            Map<String, Object> successResponse = new HashMap<>();
            User loginUser = null;

            // 3. 校验角色参数
            if (roleStr.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "请选择登录角色");
                return ResponseEntity.badRequest().body(error);
            }

            Integer selectedRole;
            try {
                selectedRole = Integer.parseInt(roleStr);
                // 校验角色值是否合法（1-物业人员，2-业主，3-物业经理）
                if (selectedRole < 1 || selectedRole > 3) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 400);
                    error.put("msg", "角色参数不合法");
                    return ResponseEntity.badRequest().body(error);
                }
            } catch (NumberFormatException e) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "角色参数格式错误");
                return ResponseEntity.badRequest().body(error);
            }

            // 4. 账号密码登录
            if ("password".equals(loginType)) {
                String username = request.get("username") != null ? request.get("username").trim() : "";
                String password = request.get("password") != null ? request.get("password").trim() : "";

                if (username.isEmpty() || password.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 400);
                    error.put("msg", "用户名和密码不能为空");
                    return ResponseEntity.badRequest().body(error);
                }

                loginUser = userService.findByUsername(username);
                if (loginUser == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 401);
                    error.put("msg", "用户名不存在");
                    return ResponseEntity.status(401).body(error);
                }

                // 校验角色是否匹配
                if (!loginUser.getRole().equals(selectedRole)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 403);
                    error.put("msg", "角色不匹配，请选择正确的角色登录");
                    return ResponseEntity.status(403).body(error);
                }

                // 校验账号状态
                if (loginUser.getStatus() != null && loginUser.getStatus() == 0) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 403);
                    error.put("msg", "账号已被禁用，请联系管理员");
                    return ResponseEntity.status(403).body(error);
                }

                boolean passwordMatch = loginUser.getPassword().equals(password)
                        || passwordEncoder.matches(password, loginUser.getPassword());
                if (!passwordMatch) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 401);
                    error.put("msg", "密码错误");
                    return ResponseEntity.status(401).body(error);
                }

                loginSuccess = true;
            }
            // 5. 手机号验证码登录
            else if ("sms".equals(loginType)) {
                String phone = request.get("phone") != null ? request.get("phone").trim() : "";
                String code = request.get("code") != null ? request.get("code").trim() : "";

                if (phone.isEmpty() || code.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 400);
                    error.put("msg", "手机号和验证码不能为空");
                    return ResponseEntity.badRequest().body(error);
                }

                loginUser = userService.findByPhone(phone);
                if (loginUser == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 401);
                    error.put("msg", "手机号未注册");
                    return ResponseEntity.status(401).body(error);
                }

                // 校验角色是否匹配
                if (!loginUser.getRole().equals(selectedRole)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 403);
                    error.put("msg", "角色不匹配，请选择正确的角色登录");
                    return ResponseEntity.status(403).body(error);
                }

                // 校验账号状态
                if (loginUser.getStatus() != null && loginUser.getStatus() == 0) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 403);
                    error.put("msg", "账号已被禁用，请联系管理员");
                    return ResponseEntity.status(403).body(error);
                }

                boolean codeMatch = verifySmsCode(phone, code);
                if (!codeMatch) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("code", 401);
                    error.put("msg", "验证码错误或已过期");
                    return ResponseEntity.status(401).body(error);
                }

                loginSuccess = true;
            }
            // 6. 未选择登录方式
            else {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "请选择登录方式（密码/验证码）");
                return ResponseEntity.badRequest().body(error);
            }

            // 7. 登录成功：生成JWT Token并返回
            if (loginSuccess && loginUser != null) {
                String token = jwtUtil.generateToken(loginUser.getId(), loginUser.getRole());
                successResponse.put("code", 200);

                // 根据角色返回不同的提示信息
                String roleMsg = "";
                switch (loginUser.getRole()) {
                    case 1:
                        roleMsg = "物业人员";
                        break;
                    case 2:
                        roleMsg = "业主";
                        break;
                    case 3:
                        roleMsg = "物业经理";
                        break;
                }

                successResponse.put("msg", "登录成功：" + roleMsg);
                successResponse.put("token", token);
                successResponse.put("user", loginUser);
                successResponse.put("role", loginUser.getRole());
                return ResponseEntity.ok(successResponse);
            }

            // 兜底错误
            Map<String, Object> error = new HashMap<>();
            error.put("code", 401);
            error.put("msg", "登录失败，请检查登录信息");
            return ResponseEntity.status(401).body(error);

        } catch (Exception e) {
            log.error("登录接口异常", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("msg", "服务器内部错误：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ===================== 注册接口 =====================
    /**d
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String buildingNo = request.get("buildingNo");
            String roomNo = request.get("roomNo");
            String name = request.get("name");
            String idCard = request.get("idCard");
            String password = request.get("password");

            // 参数判空
            if (phone == null || phone.trim().isEmpty()
                    || buildingNo == null || buildingNo.trim().isEmpty()
                    || roomNo == null || roomNo.trim().isEmpty()
                    || name == null || name.trim().isEmpty()
                    || idCard == null || idCard.trim().isEmpty()
                    || password == null || password.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            // 校验手机号是否已注册
            if (userService.existsByPhone(phone)) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "手机号已绑定账号");
                return ResponseEntity.badRequest().body(error);
            }

            // 校验业主信息是否匹配物业备案数据
            if (!validateOwnerInfo(roomNo, name, idCard)) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "业主信息与备案数据不一致");
                return ResponseEntity.badRequest().body(error);
            }

            // 创建用户和业主信息
            User user = new User();
            user.setUsername(phone); // 默认用户名为手机号
            user.setPassword(passwordEncoder.encode(password)); // 密码加密
            user.setPhone(phone);
            user.setName(name);
            user.setRole(2); // 业主角色
            user.setStatus(1); // 正常状态
            user.setCreateTime(LocalDateTime.now());
            userService.save(user);

            Owner owner = new Owner();
            owner.setUserId(user.getId());
            owner.setBuildingNo(buildingNo.trim()); // 直接使用前端传入的楼栋号
            owner.setRoomNo(roomNo);
            owner.setIdCard(idCard);
            owner.setRegisterTime(LocalDateTime.now());
            ownerService.save(owner);

            Map<String, Object> success = new HashMap<>();
            success.put("code", 200);
            success.put("msg", "注册成功");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            log.error("注册接口异常", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("msg", "注册失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ===================== 验证码相关接口 =====================
    /**
     * 发送验证码接口
     */
    @PostMapping("/send-sms")
    public ResponseEntity<Map<String, Object>> sendSms(@RequestBody(required = false) Map<String, String> request) {
        try {
            log.info("===== /user/send-sms 被调用 =====");
            log.info("接收参数：{}", request);

            // 1. 参数判空
            String phone = "";
            if (request == null || request.get("phone") == null || request.get("phone").trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "手机号不能为空");
                return ResponseEntity.badRequest().body(error);
            } else {
                phone = request.get("phone").trim();
            }

            // 2. 校验手机号是否已注册
            if (userService.existsByPhone(phone)) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "手机号已绑定账号");
                return ResponseEntity.badRequest().body(error);
            }

            // 3. 生成6位随机验证码
            String code = String.format("%06d", (int) (Math.random() * 1000000));
            log.info("验证码已生成: {}", code);

            // 4. 存储验证码到Redis（5分钟有效期）
            try {
                redisTemplate.opsForValue().set("sms_code:" + phone, code, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("Redis操作失败: {}", e.getMessage(), e);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 500);
                error.put("msg", "服务器内部错误：Redis存储失败");
                return ResponseEntity.status(500).body(error);
            }

            // 5. 统一返回JSON格式
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "验证码发送成功");
            result.put("data", code); // 测试用，实际不返回给前端

            log.info("返回响应：{}", result);
            return ResponseEntity.ok(result);

        } catch (Throwable e) {
            log.error("发送验证码接口异常", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("msg", "服务器内部错误：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 校验验证码接口
     */
    @PostMapping("/validate-code")
    public ResponseEntity<Map<String, Object>> validateCode(@RequestBody(required = false) Map<String, String> request) {
        try {
            log.info("===== /user/validate-code 被调用 =====");
            log.info("接收参数：{}", request);

            // 1. 参数判空
            String phone = "";
            String code = "";
            if (request == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "手机号和验证码不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            phone = request.get("phone") != null ? request.get("phone").trim() : "";
            code = request.get("code") != null ? request.get("code").trim() : "";

            if (phone.isEmpty() || code.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "手机号和验证码不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            // 2. 从Redis获取验证码
            String storedCode = null;
            try {
                storedCode = redisTemplate.opsForValue().get("sms_code:" + phone);
            } catch (Exception e) {
                log.error("Redis操作失败: {}", e.getMessage(), e);
                Map<String, Object> error = new HashMap<>();
                error.put("code", 500);
                error.put("msg", "服务器内部错误：Redis读取失败");
                return ResponseEntity.status(500).body(error);
            }

            // 3. 校验验证码
            if (storedCode == null || !storedCode.equals(code)) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", 400);
                error.put("msg", "验证码错误或已过期");
                return ResponseEntity.badRequest().body(error);
            }

            // 4. 验证成功，返回统一格式
            Map<String, Object> success = new HashMap<>();
            success.put("code", 200);
            success.put("msg", "验证码校验成功");
            return ResponseEntity.ok(success);

        } catch (Exception e) {
            log.error("校验验证码接口异常", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("msg", "服务器内部错误：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ===================== 业主信息管理CRUD接口（新增整合） =====================
    /**
     * 分页查询业主列表
     */
    @GetMapping("/owner")
    public Result<IPage<OwnerVO>> getOwnerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword) {
        Page<Owner> page = new Page<>(pageNum, pageSize);
        IPage<Owner> ownerPage = ownerService.getOwnerList(page, searchType, keyword);

        // 转换为 OwnerVO
        IPage<OwnerVO> voPage = ownerPage.convert(owner -> {
            OwnerVO vo = new OwnerVO();
            vo.setId(owner.getId());
            vo.setUserId(owner.getUserId());
            vo.setRoomNo(owner.getBuildingNo() + "-" + owner.getRoomNo());
            vo.setIdCard(owner.getIdCard());
            vo.setRegisterTime(owner.getRegisterTime());

            // 关联查询 User 表获取姓名和电话
            User user = userService.getById(owner.getUserId());
            if (user != null) {
                vo.setOwnerName(user.getName());
                vo.setPhone(user.getPhone());
            }

            // 复制 Owner 的所有详细信息
            vo.setHouseArea(owner.getHouseArea());
            vo.setInternalArea(owner.getInternalArea());
            vo.setHouseLayout(owner.getHouseLayout());
            vo.setHouseUsage(owner.getHouseUsage());
            vo.setHouseStructure(owner.getHouseStructure());
            vo.setOwnershipType(owner.getOwnershipType());
            vo.setCoOwnerName(owner.getCoOwnerName());
            vo.setCoOwnerIdCard(owner.getCoOwnerIdCard());
            vo.setPropertyCertNo(owner.getPropertyCertNo());
            vo.setPropertyUnitNo(owner.getPropertyUnitNo());
            vo.setRegistrationDate(owner.getRegistrationDate());
            vo.setRegistrationAuthority(owner.getRegistrationAuthority());
            vo.setLandUseYears(owner.getLandUseYears());
            vo.setLandNature(owner.getLandNature());
            vo.setIsMortgaged(owner.getIsMortgaged());
            vo.setMortgageAmount(owner.getMortgageAmount());
            vo.setMortgageeName(owner.getMortgageeName());
            vo.setIsSeized(owner.getIsSeized());
            vo.setHasResidenceRight(owner.getHasResidenceRight());
            vo.setDeliveryDate(owner.getDeliveryDate());
            vo.setRegisteredUsage(owner.getRegisteredUsage());
            vo.setHasParkingSpace(owner.getHasParkingSpace());
            vo.setHasStorageRoom(owner.getHasStorageRoom());
            vo.setParkingSpaceType(owner.getParkingSpaceType());
            vo.setParkingSpaceNo(owner.getParkingSpaceNo());

            return vo;
        });

        return Result.success(voPage);
    }

    /**
     * 新增业主
     */
    @PostMapping("/owner")
    public Result<?> addOwner(@RequestBody Map<String, Object> ownerData) {
        try {
            // 1. 创建 User 对象
            User user = new User();
            user.setUsername((String) ownerData.get("username"));
            user.setName((String) ownerData.get("name"));
            user.setPhone((String) ownerData.get("phone"));
            user.setPassword(passwordEncoder.encode((String) ownerData.get("password")));
            user.setRole(convertToInteger(ownerData.get("role")));
            user.setStatus(convertToInteger(ownerData.get("status")));
            user.setCreateTime(LocalDateTime.now());

            // 2. 保存 User
            userService.save(user);

            // 3. 创建 Owner 对象
            Owner owner = new Owner();
            owner.setUserId(user.getId());
            owner.setIdCard((String) ownerData.get("idCard"));
            owner.setBuildingNo((String) ownerData.get("buildingNo"));
            owner.setRoomNo((String) ownerData.get("roomNo"));

            // 设置 Owner 的其他字段
            owner.setHouseArea(convertToBigDecimal(ownerData.get("houseArea")));
            owner.setInternalArea(convertToBigDecimal(ownerData.get("internalArea")));
            owner.setHouseLayout((String) ownerData.get("houseLayout"));
            owner.setHouseUsage((String) ownerData.get("houseUsage"));
            owner.setHouseStructure((String) ownerData.get("houseStructure"));
            owner.setOwnershipType((String) ownerData.get("ownershipType"));
            owner.setCoOwnerName((String) ownerData.get("coOwnerName"));
            owner.setCoOwnerIdCard((String) ownerData.get("coOwnerIdCard"));
            owner.setPropertyCertNo((String) ownerData.get("propertyCertNo"));
            owner.setPropertyUnitNo((String) ownerData.get("propertyUnitNo"));
            owner.setRegistrationDate(convertToLocalDate(ownerData.get("registrationDate")));
            owner.setRegistrationAuthority((String) ownerData.get("registrationAuthority"));
            owner.setLandUseYears(convertToInteger(ownerData.get("landUseYears")));
            owner.setLandNature((String) ownerData.get("landNature"));
            owner.setIsMortgaged(convertToBoolean(ownerData.get("isMortgaged")));
            owner.setMortgageAmount(convertToBigDecimal(ownerData.get("mortgageAmount")));
            owner.setMortgageeName((String) ownerData.get("mortgageeName"));
            owner.setIsSeized(convertToBoolean(ownerData.get("isSeized")));
            owner.setHasResidenceRight(convertToBoolean(ownerData.get("hasResidenceRight")));
            owner.setDeliveryDate(convertToLocalDate(ownerData.get("deliveryDate")));
            owner.setRegisteredUsage((String) ownerData.get("registeredUsage"));
            owner.setHasParkingSpace(convertToBoolean(ownerData.get("hasParkingSpace")));
            owner.setParkingSpaceType((String) ownerData.get("parkingSpaceType"));
            owner.setParkingSpaceNo((String) ownerData.get("parkingSpaceNo"));
            owner.setHasStorageRoom(convertToBoolean(ownerData.get("hasStorageRoom")));
            owner.setRegisterTime(LocalDateTime.now());

            // 4. 保存 Owner
            ownerService.save(owner);

            return Result.success("新增业主成功");
        } catch (Exception e) {
            log.error("新增业主失败", e);
            return Result.error("新增业主失败：" + e.getMessage());
        }
    }

    /**
     * 修改业主信息
     */
    @PutMapping("/owner/{id}")
    public Result<?> updateOwner(@PathVariable Long id, @RequestBody Map<String, Object> ownerData) {
        try {
            // 1. 查询原有 Owner 信息
            Owner oldOwner = ownerService.getById(id);
            if (oldOwner == null) {
                return Result.error("业主记录不存在");
            }

            Long userId = oldOwner.getUserId();

            // 2. 更新 User 信息
            User user = userService.getById(userId);
            if (user != null) {
                user.setUsername((String) ownerData.get("username"));
                user.setName((String) ownerData.get("name"));
                user.setPhone((String) ownerData.get("phone"));
                // 编辑时如果需要修改密码，可以在此添加逻辑
                user.setRole(convertToInteger(ownerData.get("role")));
                user.setStatus(convertToInteger(ownerData.get("status")));
                userService.updateById(user);
            }

            // 3. 更新 Owner 信息
            Owner owner = new Owner();
            owner.setId(id);
            owner.setUserId(userId);
            owner.setIdCard((String) ownerData.get("idCard"));
            owner.setBuildingNo((String) ownerData.get("buildingNo"));
            owner.setRoomNo((String) ownerData.get("roomNo"));

            // 设置 Owner 的其他字段
            owner.setHouseArea(convertToBigDecimal(ownerData.get("houseArea")));
            owner.setInternalArea(convertToBigDecimal(ownerData.get("internalArea")));
            owner.setHouseLayout((String) ownerData.get("houseLayout"));
            owner.setHouseUsage((String) ownerData.get("houseUsage"));
            owner.setHouseStructure((String) ownerData.get("houseStructure"));
            owner.setOwnershipType((String) ownerData.get("ownershipType"));
            owner.setCoOwnerName((String) ownerData.get("coOwnerName"));
            owner.setCoOwnerIdCard((String) ownerData.get("coOwnerIdCard"));
            owner.setPropertyCertNo((String) ownerData.get("propertyCertNo"));
            owner.setPropertyUnitNo((String) ownerData.get("propertyUnitNo"));
            owner.setRegistrationDate(convertToLocalDate(ownerData.get("registrationDate")));
            owner.setRegistrationAuthority((String) ownerData.get("registrationAuthority"));
            owner.setLandUseYears(convertToInteger(ownerData.get("landUseYears")));
            owner.setLandNature((String) ownerData.get("landNature"));
            owner.setIsMortgaged(convertToBoolean(ownerData.get("isMortgaged")));
            owner.setMortgageAmount(convertToBigDecimal(ownerData.get("mortgageAmount")));
            owner.setMortgageeName((String) ownerData.get("mortgageeName"));
            owner.setIsSeized(convertToBoolean(ownerData.get("isSeized")));
            owner.setHasResidenceRight(convertToBoolean(ownerData.get("hasResidenceRight")));
            owner.setDeliveryDate(convertToLocalDate(ownerData.get("deliveryDate")));
            owner.setRegisteredUsage((String) ownerData.get("registeredUsage"));
            owner.setHasParkingSpace(convertToBoolean(ownerData.get("hasParkingSpace")));
            owner.setParkingSpaceType((String) ownerData.get("parkingSpaceType"));
            owner.setParkingSpaceNo((String) ownerData.get("parkingSpaceNo"));
            owner.setHasStorageRoom(convertToBoolean(ownerData.get("hasStorageRoom")));

            // 4. 更新数据库
            ownerService.updateById(owner);

            return Result.success("修改业主信息成功");
        } catch (Exception e) {
            log.error("修改业主失败", e);
            return Result.error("修改业主失败：" + e.getMessage());
        }
    }

    /**
     * 工具方法：转换为 Integer
     */
    private Integer convertToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 工具方法：转换为 BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return new BigDecimal(value.toString());
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 工具方法：转换为 Boolean
     */
    private Boolean convertToBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value) || "是".equals((String) value) || "1".equals((String) value);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return false;
    }

    /**
     * 工具方法：转换为 LocalDate
     */
    private LocalDate convertToLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }


    /**
     * 删除业主（逻辑删除）
     */
    @DeleteMapping("/owner/{id}")
    public Result<?> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return Result.success("删除业主成功");
    }

    // ===================== 私有工具方法 =====================
    /**
     * 真实验证码校验方法
     */
    private boolean verifySmsCode(String phone, String code) {
        // 生产环境替换为真实Redis校验逻辑
        String redisCode = redisTemplate.opsForValue().get("sms_code:" + phone);
        return code.equals(redisCode) && redisTemplate.getExpire("sms_code:" + phone) > 0;
    }

    /**
     * 校验业主信息是否匹配备案数据
     */
    private boolean validateOwnerInfo(String roomNo, String name, String idCard) {
        // 模拟校验逻辑（实际开发中需连接物业备案系统）
        return true;
    }

    /**
     * 提取楼栋号（例如：A1-101 → A1）
     */
    private String extractBuildingNo(String roomNo) {
        if (roomNo == null || !roomNo.contains("-")) {
            return roomNo;
        }
        return roomNo.split("-")[0];
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

    /**
     * 修改密码接口
     * @param request 修改密码请求参数
     * @return 操作结果
     */
    @PutMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody UpdatePasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 校验参数
            if (request.getUserId() == null) {
                response.put("code", 400);
                response.put("msg", "用户 ID 不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (!StringUtils.hasText(request.getOldPassword()) ||
                    !StringUtils.hasText(request.getNewPassword())) {
                response.put("code", 400);
                response.put("msg", "原密码和新密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 查询用户
            User user = userService.getById(request.getUserId());
            if (user == null) {
                response.put("code", 404);
                response.put("msg", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 验证原密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                response.put("code", 401);
                response.put("msg", "原密码错误");
                return ResponseEntity.status(401).body(response);
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            boolean success = userService.updateById(user);

            if (success) {
                response.put("code", 200);
                response.put("msg", "密码修改成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("msg", "密码修改失败");
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("修改密码失败", e);
            response.put("code", 500);
            response.put("msg", "修改密码失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 修改手机号接口
     * @param request 修改手机号请求参数
     * @return 操作结果
     */
    @PutMapping("/update-phone")
    public ResponseEntity<Map<String, Object>> updatePhone(@RequestBody UpdatePhoneRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 校验参数
            if (request.getUserId() == null) {
                response.put("code", 400);
                response.put("msg", "用户 ID 不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            if (!StringUtils.hasText(request.getNewPhone())) {
                response.put("code", 400);
                response.put("msg", "新手机号不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 手机号格式校验
            if (!request.getNewPhone().matches("^1[3-9]\\d{9}$")) {
                response.put("code", 400);
                response.put("msg", "手机号格式不正确");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查新手机号是否已被其他用户使用
            User existingUser = userService.findByPhone(request.getNewPhone());
            if (existingUser != null && !existingUser.getId().equals(request.getUserId())) {
                response.put("code", 400);
                response.put("msg", "该手机号已被其他账号使用");
                return ResponseEntity.badRequest().body(response);
            }

            // TODO: 校验短信验证码（如果有验证码功能）
            // if (!verifySmsCode(request.getNewPhone(), request.getCode())) {
            //     response.put("code", 401);
            //     response.put("msg", "验证码错误或已过期");
            //     return ResponseEntity.status(401).body(response);
            // }

            // 查询用户
            User user = userService.getById(request.getUserId());
            if (user == null) {
                response.put("code", 404);
                response.put("msg", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 更新手机号
            user.setPhone(request.getNewPhone());
            // 如果用户名是手机号，同步更新用户名
            if (user.getUsername() != null && user.getUsername().matches("^1[3-9]\\d{9}$")) {
                user.setUsername(request.getNewPhone());
            }

            boolean success = userService.updateById(user);

            if (success) {
                response.put("code", 200);
                response.put("msg", "手机号修改成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 500);
                response.put("msg", "手机号修改失败");
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("修改手机号失败", e);
            response.put("code", 500);
            response.put("msg", "修改手机号失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据用户 ID 获取业主详细信息
     * @param userId 用户 ID
     * @return 业主详细信息
     */
    @GetMapping("/owner-detail/{userId}")
    public ResponseEntity<Map<String, Object>> getOwnerDetailByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 新增：校验 userId 是否为空
            if (userId == null) {
                response.put("code", 400);
                response.put("msg", "用户 ID 不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 查询用户信息
            User user = userService.getById(userId);
            if (user == null) {
                response.put("code", 404);
                response.put("msg", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 查询业主信息
            LambdaQueryWrapper<Owner> ownerQueryWrapper = new LambdaQueryWrapper<>();
            ownerQueryWrapper.eq(Owner::getUserId, userId);
            Owner owner = ownerMapper.selectOne(ownerQueryWrapper);

            // 组装返回数据（包含 User 和 Owner 的所有字段）
            Map<String, Object> resultData = new HashMap<>();
            if (owner != null) {
                // 复制 Owner 的所有字段
                resultData.put("id", owner.getId());
                resultData.put("userId", owner.getUserId());
                resultData.put("idCard", owner.getIdCard());
                resultData.put("buildingNo", owner.getBuildingNo());
                resultData.put("roomNo", owner.getRoomNo());
                resultData.put("houseArea", owner.getHouseArea());
                resultData.put("internalArea", owner.getInternalArea());
                resultData.put("houseLayout", owner.getHouseLayout());
                resultData.put("houseUsage", owner.getHouseUsage());
                resultData.put("houseStructure", owner.getHouseStructure());
                resultData.put("ownershipType", owner.getOwnershipType());
                resultData.put("coOwnerName", owner.getCoOwnerName());
                resultData.put("coOwnerIdCard", owner.getCoOwnerIdCard());
                resultData.put("propertyCertNo", owner.getPropertyCertNo());
                resultData.put("propertyUnitNo", owner.getPropertyUnitNo());
                resultData.put("registrationDate", owner.getRegistrationDate());
                resultData.put("registrationAuthority", owner.getRegistrationAuthority());
                resultData.put("landUseYears", owner.getLandUseYears());
                resultData.put("landNature", owner.getLandNature());
                resultData.put("isMortgaged", owner.getIsMortgaged());
                resultData.put("mortgageAmount", owner.getMortgageAmount());
                resultData.put("mortgageeName", owner.getMortgageeName());
                resultData.put("isSeized", owner.getIsSeized());
                resultData.put("hasResidenceRight", owner.getHasResidenceRight());
                resultData.put("deliveryDate", owner.getDeliveryDate());
                resultData.put("registeredUsage", owner.getRegisteredUsage());
                resultData.put("hasParkingSpace", owner.getHasParkingSpace());
                resultData.put("hasStorageRoom", owner.getHasStorageRoom());
                resultData.put("parkingSpaceType", owner.getParkingSpaceType());
                resultData.put("parkingSpaceNo", owner.getParkingSpaceNo());
                resultData.put("registerTime", owner.getRegisterTime());
            }

            // 添加 User 的字段
            resultData.put("username", user.getUsername());
            resultData.put("name", user.getName());
            resultData.put("phone", user.getPhone());
            resultData.put("role", user.getRole());
            resultData.put("status", user.getStatus());

            response.put("code", 200);
            response.put("msg", "查询成功");
            response.put("data", resultData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取业主详细信息失败", e);
            response.put("code", 500);
            response.put("msg", "获取业主详细信息失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 修改密码请求 DTO
    @Data
    public static class UpdatePasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;
    }

    // 修改手机号请求 DTO
    @Data
    public static class UpdatePhoneRequest {
        private Long userId;
        private String newPhone;
        private String code;
    }
}