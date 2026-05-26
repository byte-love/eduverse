package com.eduverse.data.service;


import com.eduverse.data.model.dto.TodayDataDTO;
import com.eduverse.data.model.vo.TodayDataVO;

/**
 * @author wusongsong
 * @since 2022/10/13 9:27
 **/
public interface TodayDataService {

    /**
     * 获取今日数据
     * @return
     */
    TodayDataVO get();

    /**
     * 设置今日数据
     * @param todayDataDTO
     */
    void set(TodayDataDTO todayDataDTO);
}