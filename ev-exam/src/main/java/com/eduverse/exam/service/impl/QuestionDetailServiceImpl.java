package com.eduverse.exam.service.impl;

import com.eduverse.exam.domain.po.QuestionDetail;
import com.eduverse.exam.mapper.QuestionDetailMapper;
import com.eduverse.exam.service.IQuestionDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 题目 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-09-02
 */
@Service
public class QuestionDetailServiceImpl extends ServiceImpl<QuestionDetailMapper, QuestionDetail> implements IQuestionDetailService {

}
