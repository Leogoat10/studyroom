package com.jason.classroom.controller;


import com.jason.classroom.common.lang.Result;
import com.jason.classroom.common.vo.TableVO;
import com.jason.classroom.entity.Table;
import com.jason.classroom.service.TableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 *  前端控制器
 * </p>
 */
@RestController
@RequestMapping("/table")
@Api(tags = {"座位接口"})
public class TableController {
    @Autowired
    private TableService tableService;
    /**
     * 查询指定自习室中的所有座位信息
     * 该接口根据自习室名称查询其中的所有座位信息
     * @param room 自习室的名称
     * @return 返回指定自习室中所有座位的相关信息
     */
    @GetMapping("/gettables/{room}")
    @ApiOperation(value = "查询指定自习室中的所有座位接口")
    public Result table4List(@PathVariable(name = "room") String room){
        return tableService.table4List(room);
    }
    /**
     * 订位置
     * 该接口用于用户预定指定座位，设置开始和结束时间
     * @param tableVO 包含座位信息的对象，包括开始时间和结束时间
     * @return 返回座位预定操作的结果
     */
    @PostMapping("/ordertable")
    @ApiOperation(value = "订座位接口")
    public Result orderTable(TableVO tableVO){
        Table table = new Table();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime starttime = LocalDateTime.parse(tableVO.getStarttime(), df);
        LocalDateTime endtime = LocalDateTime.parse(tableVO.getEndtime(), df);
        //设置座位的预定信息
        table.setId(tableVO.getId());
        table.setStatus(tableVO.getStatus());
        table.setRoom(tableVO.getRoom());
        table.setStarttime(starttime);
        table.setEndtime(endtime);
        //调用服务层方法进行座位预定
        return tableService.orderTable(table);
    }

    /**
     * 获取可用的座位
     * 该接口用于查询指定编号的可用座位
     * @param tablenum 座位编号
     * @return 返回可用座位的信息
     */
    @GetMapping("/getabletable")
    @ApiOperation(value = "获取可用的座位接口")
    public Result getAbleTable(String tablenum){
        return tableService.getAbleTable(tablenum);
    }

    /**
    * 更新座位状态
    * */
    @PostMapping("/update")
    @ApiOperation(value = "更新座位状态接口")
    public Result updateTable(TableVO tableVO) {
        return tableService.updateTable(tableVO);
    }
}



