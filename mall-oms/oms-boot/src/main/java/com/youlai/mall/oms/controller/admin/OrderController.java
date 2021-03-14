package com.youlai.mall.oms.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.common.enums.BusinessTypeEnum;
import com.youlai.common.enums.QueryModeEnum;
import com.youlai.common.redis.component.BusinessNoGenerator;
import com.youlai.common.result.Result;
import com.youlai.common.result.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Api(tags = "【系统管理】订单服务")
@RestController("AdminOrderController")
@RequestMapping("/api.admin/v1/orders")
@Slf4j
@AllArgsConstructor
public class OrderController {

    private IOmsOrderService iOmsOrderService;

    @ApiOperation(value = "列表分页", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "queryMode", value = "查询模式", paramType = "query", dataType = "QueryModeEnum"),
            @ApiImplicitParam(name = "page", value = "页码", paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "limit", value = "每页数量", paramType = "query", dataType = "Long"),
            @ApiImplicitParam(name = "orderSn", value = "订单编号", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "订单状态", paramType = "query", dataType = "Integer"),
            @ApiImplicitParam(name = "startDate", value = "开始日期", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "结束日期", paramType = "query", dataType = "String"),
    })
    @GetMapping
    public Result list(
            String queryMode,
            Integer page,
            Integer limit,
            String orderSn,
            Integer status,
            String startDate,
            String endDate
    ) {
        QueryModeEnum queryModeEnum = QueryModeEnum.getValue(queryMode);
        switch (queryModeEnum) {
            case PAGE:
                LambdaQueryWrapper<OmsOrder1> queryWrapper = new LambdaQueryWrapper<OmsOrder1>()
                        .like(StrUtil.isNotBlank(orderSn), OmsOrder1::getOrderSn, orderSn)
                        .eq(status != null, OmsOrder1::getStatus, status)
                        .apply(StrUtil.isNotBlank(startDate),
                                "date_format (gmt_crate,'%Y-%m-%d') >= date_format('" + startDate + "','%Y-%m-%d')")
                        .apply(StrUtil.isNotBlank(endDate),
                                "date_format (gmt_crate,'%Y-%m-%d') <= date_format('" + endDate + "','%Y-%m-%d')")
                        .orderByDesc(OmsOrder1::getGmtModified)
                        .orderByDesc(OmsOrder1::getGmtCreate);
                Page<OmsOrder1> result = iOmsOrderService.page(new Page<>(page, limit), queryWrapper);
                return Result.success(result.getRecords(), result.getTotal());
            default:
                return Result.failed(ResultCode.QUERY_MODE_IS_NULL);
        }
    }

    @ApiOperation(value = "订单详情", httpMethod = "GET")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path", dataType = "Long")
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        OrderBO order = iOmsOrderService.getByOrderId(id);
        return Result.success(order);
    }

    @ApiOperation(value = "订单提交", httpMethod = "POST")
    @ApiImplicitParam(name = "orderBO", value = "实体JSON对象", required = true, paramType = "body", dataType = "OrderBO")
    @PostMapping
    public Result add(@RequestBody OrderBO orderBO) {
        boolean status = iOmsOrderService.save(orderBO);
        return Result.judge(status);
    }

    @ApiOperation(value = "修改订单", httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path", dataType = "Long"),
            @ApiImplicitParam(name = "order", value = "实体JSON对象", required = true, paramType = "body", dataType = "OmsOrder")
    })
    @PutMapping(value = "/{id}")
    public Result update(
            @PathVariable Long id,
            @RequestBody OmsOrder1 order) {
        boolean status = iOmsOrderService.updateById(order);
        return Result.judge(status);
    }

    @ApiOperation(value = "局部更新", httpMethod = "PATCH")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path", dataType = "Long"),
            @ApiImplicitParam(name = "status", value = "订单状态", paramType = "query", dataType = "Integer")
    })
    @PatchMapping(value = "/{id}")
    public Result patch(@PathVariable Long id,
                        @RequestParam Integer status) {
        LambdaUpdateWrapper<OmsOrder1> updateWrapper = new LambdaUpdateWrapper<OmsOrder1>().eq(OmsOrder1::getId, id);
        updateWrapper.set(status != null, OmsOrder1::getStatus, status);
        boolean result = iOmsOrderService.update(updateWrapper);
        return Result.judge(result);
    }

    @ApiOperation(value = "订单详情", httpMethod = "GET")
    @ApiImplicitParam(name = "id", value = "订单ID", required = true, paramType = "path", dataType = "Long")
    @GetMapping("/{id}/detail")
    public Result orderDetail(@PathVariable Long id) {
        OmsOrder1 order = iOmsOrderService.getById(id);
        return Result.success(order);
    }

    private BusinessNoGenerator businessNoGenerator;

    @PostMapping("/order_sn")
    public Result generateOrderSn() {
        String orderSn = businessNoGenerator.generate(BusinessTypeEnum.ORDER);
        log.info("订单编号:{}", orderSn);
        return Result.success(orderSn);
    }
}