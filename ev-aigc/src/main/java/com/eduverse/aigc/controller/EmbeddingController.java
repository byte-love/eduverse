package com.eduverse.aigc.controller;


import cn.hutool.core.collection.CollStreamUtil;
import com.eduverse.aigc.service.CourseSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 向量数据库控制器
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/embedding")
public class EmbeddingController {

    private final VectorStore vectorStore;

    private final EmbeddingModel embeddingModel;

    private final CourseSyncService courseSyncService;

    @PostMapping()
    public void saveVectorStore(@RequestParam List<String> messages) {
        // 构建文档
        List<Document> documentList =
                CollStreamUtil.toList
                        (messages, message -> Document.builder().text(message).build());
        // 保存到向量数据库中
        vectorStore.add(documentList);
    }

    @GetMapping
    public EmbeddingResponse embed(@RequestParam("message") String message) {
        return this.embeddingModel.embedForResponse(List.of(message));
    }

    @DeleteMapping
    public void deleteVectorStore(@RequestParam("ids") List<String> ids) {
        // 删除向量数据库中的数据
        this.vectorStore.delete(ids);
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam("message") String message) {
        return this.vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
    }

    @GetMapping("/search/all")
    public List<Document> searchAll() {
        // 搜索全部数据
        return this.vectorStore.similaritySearch(SearchRequest.builder().query("").topK(999).build());
    }

    @PostMapping("/sync-courses")
    public Map<String, Object> syncCourses() {
        int count = courseSyncService.syncCourses();
        return Map.of("success", true, "syncedCount", count);
    }

}
