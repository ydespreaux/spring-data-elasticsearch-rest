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

import com.github.ydespreaux.spring.data.elasticsearch.annotations.Index;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexedDocument;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@IndexedDocument(
        index = @Index(
                name = "books",
                settingsAndMappingPath = "classpath:indices/book.index"
        )
)
public class Book {

    @Id
    private UUID documentId;
    @Version
    private Long version;
    private String title;
    private String description;
    private Double price;
    private LocalDate publication;
    private LocalDateTime lastUpdated;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(getDocumentId(), book.getDocumentId()) &&
                Objects.equals(getTitle(), book.getTitle()) &&
                Objects.equals(getDescription(), book.getDescription()) &&
                Objects.equals(getPrice(), book.getPrice()) &&
                Objects.equals(getPublication(), book.getPublication());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDocumentId(), getTitle(), getDescription(), getPrice(), getPublication());
    }

}
