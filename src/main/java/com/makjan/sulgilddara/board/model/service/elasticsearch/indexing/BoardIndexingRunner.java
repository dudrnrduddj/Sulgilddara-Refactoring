package com.makjan.sulgilddara.board.model.service.elasticsearch.indexing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "elasticsearch.indexing.enabled",
        havingValue = "true"
)
public class BoardIndexingRunner implements CommandLineRunner {

    private final BoardIndexingService indexingService;

    public BoardIndexingRunner(BoardIndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Override
    public void run(String... args) throws Exception {
        indexingService.indexAllBoards();
    }
}