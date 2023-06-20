package com.youlai.mall.oms.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.mall.oms.dto.SeataOrderDTO;
import com.youlai.mall.oms.model.entity.OmsOrder;
import com.youlai.mall.oms.model.query.OrderPageQuery;

/**
 * 「管理端」订单业务接口
 *
 * @author haoxr
 * @since 2020/12/30
 */
public interface OmsOrderService extends IService<OmsOrder> {
    /**
     * 订单分页列表
     *
     * @param queryParams
     * @return
     */
    IPage<OmsOrder> listOrderPages(OrderPageQuery queryParams);


}

