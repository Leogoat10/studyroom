package com.jason.classroom.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jason.classroom.common.dto.LoginDTO;
import com.jason.classroom.common.lang.Result;
import com.jason.classroom.entity.Admin;
import com.jason.classroom.entity.Room;
import com.jason.classroom.entity.Table;
import com.jason.classroom.entity.User;
import com.jason.classroom.mapper.AdminMapper;
import com.jason.classroom.mapper.RoomMapper;
import com.jason.classroom.mapper.TableMapper;
import com.jason.classroom.mapper.UserMapper;
import com.jason.classroom.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.classroom.service.RoomService;
import com.jason.classroom.service.UserService;
import com.jason.classroom.util.ChineseUtils;
import com.jason.classroom.util.JwtUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类，处理管理员相关操作
 * </p>
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Resource
    private JwtUtils jwtUtils; // JWT工具，用于生成和验证token
    @Resource
    private AdminMapper adminMapper; // 管理员数据访问接口
    @Resource
    private RoomMapper roomMapper; // 教室数据访问接口
    @Resource
    private UserMapper userMapper; // 用户数据访问接口
    @Resource
    private TableMapper tableMapper;

    /**
     * 管理员登录功能，验证用户名和密码，返回JWT token
     *
     * @param loginDTO 登录数据传输对象，包含用户名和密码
     * @param response HttpServletResponse对象，用于设置响应头部的token
     * @return Admin 管理员对象，如果验证成功返回管理员信息
     */
    @Override
    public Admin login(LoginDTO loginDTO, HttpServletResponse response) {
        // 查询数据库中的管理员信息
        Admin admin = this.getOne(new QueryWrapper<Admin>().eq("username", loginDTO.getUsername()));
        // 如果管理员不存在，抛出异常
        Assert.notNull(admin,"管理员不存在");

        // 密码匹配验证
        if (!admin.getPassword().equals(loginDTO.getPassword())){
            return null; // 密码错误，返回null
        }

        // 生成JWT token
        String jwt = jwtUtils.generateToken(admin.getId());

        // 设置响应头部，返回生成的JWT token
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        return admin; // 返回管理员对象
    }

    /**
     * 获取所有用户列表
     *
     * @return Map 返回包含用户数据的Map
     */
    @Override
    public Map<String,Object> userList() {
        Map<String,Object> map = new HashMap<>();
        // 查询所有用户的Map集合
        List<Map<String, Object>> users = userMapper.selectMaps(new QueryWrapper<User>());
        map.put("data", users); // 设置用户数据
        map.put("code", 0); // 状态码
        map.put("msg", "获取数据成功"); // 提示信息
        return map;
    }

    /**
     * 获取所有教室列表
     *
     * @return Map 返回包含教室数据的Map
     */
    @Override
    public Map<String,Object> roomList() {
        Map<String,Object> map = new HashMap<>();
        // 查询所有教室的Map集合
        List<Map<String, Object>> rooms = roomMapper.selectMaps(new QueryWrapper<Room>());
        map.put("data", rooms); // 设置教室数据
        map.put("code", 0); // 状态码
        map.put("msg", "获取数据成功"); // 提示信息
        return map;
    }

    /**
     * 删除指定编号的教室
     *
     * @param roomnum 教室编号
     */
    @Override
    public void deleteRoom(String roomnum) {
        // 根据教室编号删除教室
        roomMapper.delete(new QueryWrapper<Room>().eq("num", roomnum));
    }

    /**
     * 添加新教室
     *
     * @param room 教室对象，包含教室信息
     * @return Result 返回操作结果
     */
    @Override
    public Result addRoom(Room room) {
        // 检查自习室编号是否已存在
        Room existingRoom = roomMapper.selectOne(new QueryWrapper<Room>().eq("num", room.getNum()));
        if (existingRoom != null) {
            return Result.fail("自习室编号已存在");
        }

        // 设置默认状态为1（可预约）
        if(room.getStatus() == null) {
            room.setStatus(1);
        }

        // 插入新自习室到数据库
        roomMapper.insert(room);

        // 同步自动添加桌子的数据表
        if(room.getMaxrow() != null && room.getCapacity() != null) {
            try {
                addTablesForRoom(room);
            } catch (Exception e) {
                // 如果添加桌子失败，回滚自习室添加操作
                roomMapper.deleteById(room.getId());
                return Result.fail("添加桌子数据失败: " + e.getMessage());
            }
        }

        // 返回成功操作结果
        return Result.succ("添加成功");
    }

    /**
     * 为自习室自动添加桌子
     * @param room 自习室信息
     */
    private void addTablesForRoom(Room room) {
        int maxRows = room.getMaxrow();
        int capacity = room.getCapacity();

        // 计算每排的桌子数量（向上取整）
        int tablesPerRow = (int) Math.ceil((double) capacity / maxRows);

        // 生成桌子数据
        List<Table> tables = new ArrayList<>();
        for (int row = 1; row <= maxRows; row++) {
            for (int col = 1; col <= tablesPerRow; col++) {
                // 如果已经达到总容量，则停止添加
                if (tables.size() >= capacity) {
                    break;
                }

                Table table = new Table();
                table.setRoom(room.getNum());
                table.setX(row);
                table.setY(col);
                table.setStatus(1); // 默认状态为可预约
                tables.add(table);
            }
        }
        // 批量插入桌子数据
        if (!tables.isEmpty()) {
            tableMapper.insertBatch(tables);
        }
    }


    /**
     * 编辑教室信息
     * @param num 教室编号
     * @param capacity 教室容量
     * @param school 学校名称
     */
    @Override
    public Result editRoom(String originalNum, String num, Integer capacity, String school, Integer status) {
        // 如果编号被修改了，检查新编号是否已存在
        if(!originalNum.equals(num)) {
            Room existingRoom = roomMapper.selectOne(new QueryWrapper<Room>().eq("num", num));
            if (existingRoom != null) {
                return Result.fail("自习室编号已存在");
            }
        }

        // 根据原始编号查询教室信息
        Room selectOne = roomMapper.selectOne(new QueryWrapper<Room>().eq("num", originalNum));
        if(selectOne == null) {
            return Result.fail("自习室不存在");
        }

        // 更新教室信息
        selectOne.setNum(num);
        selectOne.setCapacity(capacity);
        selectOne.setSchool(school);
        selectOne.setStatus(status);

        // 更新数据库中的教室信息
        roomMapper.updateById(selectOne);

        return Result.succ("修改成功");
    }

    /**
     * 获取管理员信息
     *
     * @param username 管理员用户名
     * @return Result 返回包含管理员信息的结果
     */
    @Override
    public Result adminInfo(String username) {
        // 根据用户名查询管理员信息
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", username));

        // 返回包含管理员ID和用户名的Map
        return Result.succ(MapUtil.builder()
                .put("id", admin.getId()) // 管理员ID
                .put("username", admin.getUsername()) // 管理员用户名
                .map()
        );
    }
    /*
    * 更新学生信誉分
    * */
    @Override
    public Result updatePoints(String Username, int points) {
        // 根据用户名查询教室信息
        User selectOne = userMapper.selectOne(new QueryWrapper<User>().eq("username", Username));
        if(selectOne == null) {
            return Result.fail("用户不存在");
        }

        selectOne.setPoints(points);

        // 更新数据库中的教室信息
        userMapper.updateById(selectOne);

        return Result.succ("修改成功");
    }
}
