package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.vo.CartVo;

/**
 * @author : pengyao
 * @Date: 2018/6/21 11: 34
 */
public interface ICartService {
    ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count);
    ServiceResponse<CartVo> deleteProduct(Integer userId, String productIds);
    ServiceResponse<CartVo> list(Integer userId);
    ServiceResponse<CartVo> selectOrUnSelect(Integer userId,Integer productId,Integer checked);
    ServiceResponse<Integer> getCartProductCount(Integer userId);
}
