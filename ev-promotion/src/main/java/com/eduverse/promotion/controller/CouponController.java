package com.eduverse.promotion.controller;

import com.eduverse.common.domain.dto.PageDTO;
import com.eduverse.promotion.domain.dto.CouponFormDTO;
import com.eduverse.promotion.domain.dto.CouponIssueFormDTO;
import com.eduverse.promotion.domain.query.CouponQuery;
import com.eduverse.promotion.domain.vo.CouponDetailVO;
import com.eduverse.promotion.domain.vo.CouponPageVO;
import com.eduverse.promotion.domain.vo.CouponVO;
import com.eduverse.promotion.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 优惠券的规则信息 控制器
 */
@Tag(name = "优惠券相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final ICouponService couponService;

    @Operation(summary = "新增优惠券接口")
    @PostMapping
    public void saveCoupon(@RequestBody @Valid CouponFormDTO dto){
        couponService.saveCoupon(dto);
    }

    @Operation(summary = "分页查询优惠券接口")
    @GetMapping("/page")
    public PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query){
        return couponService.queryCouponByPage(query);
    }

    @Operation(summary = "根据id查询优惠券接口")
    @GetMapping("/{id}")
    public CouponDetailVO queryCouponById(@Parameter(description = "优惠券id") @PathVariable("id") Long id){
        return couponService.queryCouponById(id);
    }

    @Operation(summary = "发放优惠券接口")
    @PutMapping("/{id}/issue")
    public void beginIssue(@RequestBody @Valid CouponIssueFormDTO dto) {
        couponService.beginIssue(dto);
    }

    @Operation(summary = "暂停发放优惠券接口")
    @PutMapping("/{id}/pause")
    public void pauseIssue(@Parameter(description = "优惠券id") @PathVariable("id") Long id) {
        couponService.pauseIssue(id);
    }

    @Operation(summary = "查询发放中的优惠券列表")
    @GetMapping("/list")
    public List<CouponVO> queryIssuingCoupons(){
        return couponService.queryIssuingCoupons();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("{id}")
    public void deleteById(@Parameter(description = "优惠券id") @PathVariable("id") Long id) {
        couponService.deleteById(id);
    }
}
