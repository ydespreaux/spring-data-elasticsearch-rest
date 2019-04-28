/*
 * Copyright (C) 2018 Yoann Despréaux
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING . If not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr
 */
package com.github.ydespreaux.spring.data.elasticsearch.entities;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.*;
import lombok.*;
import org.springframework.data.annotation.Id;

/**
 * ParentEntity
 *
 * @author Yoann Despréaux
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Parent(name = "join_field", type = "question")
@IndexedDocument(index = @Index(name = Question.INDEX, settingsAndMappingPath = "classpath:indices/parent-child.index"))
public class Question {

    public static final String INDEX = "questions";

    @Id
    private String id;
    private String description;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @Child(type = "answer", isParent = true)
    public static class Answer extends Question {

        @ParentId
        private String parentId;

        public Answer(String id, String parentId, String description) {
            super(id, description);
            this.parentId = parentId;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @Child(type = "vote")
    public static class Vote extends Answer {

        private Integer stars;

        public Vote(String id, String parentId, String description, Integer stars) {
            super(id, parentId, description);
            this.stars = stars;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @Child(type = "comment")
    public static class Comment extends Question {

        @ParentId
        private String parentId;

        public Comment(String id, String parentId, String description) {
            super(id, description);
            this.parentId = parentId;
        }
    }
}
