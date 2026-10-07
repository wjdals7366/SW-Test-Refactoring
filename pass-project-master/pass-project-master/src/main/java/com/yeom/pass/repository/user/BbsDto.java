package com.yeom.pass.repository.user;

import lombok.*;

public class BbsDto {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class SearchRequest{
        private String name;
        private String keyword;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class PostRequest{
        private String title;
        private String content;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class UpdateRequest{
        private Long bbs_id;
        private String title;
        private String content;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class CommentRequest{
        private Long bbs_id;
        private String body;
    }
}
