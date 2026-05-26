package com.eduverse.aigc.service;

import com.eduverse.api.client.course.CourseClient;
import com.eduverse.api.dto.course.CourseBaseInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseSyncService {

    private final CourseClient courseClient;
    private final VectorStore vectorStore;

    private static final int MAX_DETAIL_LENGTH = 500;

    public int syncCourses() {
        List<CourseBaseInfoDTO> courses = courseClient.getPublishedCourseBaseInfos();
        if (courses == null || courses.isEmpty()) {
            log.warn("No published courses found for vector store sync");
            return 0;
        }

        List<Document> documents = new ArrayList<>(courses.size());
        for (CourseBaseInfoDTO course : courses) {
            String text = buildDocumentText(course);
            documents.add(Document.builder()
                    .text(text)
                    .metadata("courseId", course.getId())
                    .metadata("name", course.getName())
                    .build());
        }

        vectorStore.add(documents);
        log.info("Synced {} courses to vector store", documents.size());
        return documents.size();
    }

    private String buildDocumentText(CourseBaseInfoDTO course) {
        StringBuilder sb = new StringBuilder();
        sb.append("[课程ID: ").append(course.getId()).append("] ");
        sb.append("课程名称: ").append(course.getName());

        if (course.getPrice() != null) {
            sb.append(", 价格: ").append(course.getPrice() / 100.0).append("元");
        }
        if (Boolean.TRUE.equals(course.getFree())) {
            sb.append(" (免费课程)");
        }
        if (course.getValidDuration() != null) {
            sb.append(", 有效期: ").append(course.getValidDuration()).append("个月");
        }
        if (course.getUsePeople() != null && !course.getUsePeople().isBlank()) {
            sb.append(", 适用人群: ").append(course.getUsePeople());
        }
        if (course.getIntroduce() != null && !course.getIntroduce().isBlank()) {
            sb.append(", 课程介绍: ").append(course.getIntroduce());
        }
        if (course.getDetail() != null && !course.getDetail().isBlank()) {
            String detail = course.getDetail();
            if (detail.length() > MAX_DETAIL_LENGTH) {
                detail = detail.substring(0, MAX_DETAIL_LENGTH);
            }
            sb.append(", 课程详情: ").append(detail);
        }
        if (course.getCataTotalNum() != null) {
            sb.append(", 课时数: ").append(course.getCataTotalNum());
        }

        return sb.toString();
    }
}
