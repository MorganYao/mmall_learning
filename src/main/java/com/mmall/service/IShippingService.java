package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;

/**
 * @author : pengyao
 * @Date: 2018/6/21 22: 30
 */
public interface IShippingService {
    ServiceResponse add(Integer userId, Shipping shipping);
    ServiceResponse<String> del(Integer userId, Integer shippingId);
    ServiceResponse update(Integer userId,Shipping shipping);
    ServiceResponse<Shipping> select(Integer userId, Integer shippingId);
    ServiceResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
