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

package com.github.ydespreaux.spring.data.elasticsearch.core.mapping;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.Score;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Version;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class SimpleElasticsearchPersistentEntityTest {

    private static SimpleElasticsearchPersistentProperty createProperty(SimpleElasticsearchPersistentEntity<?> entity,
                                                                        String field) {

        TypeInformation<?> type = entity.getTypeInformation();
        Property property = Property.of(type, ReflectionUtils.findField(entity.getType(), field));
        return new SimpleElasticsearchPersistentProperty(property, entity, SimpleTypeHolder.DEFAULT);

    }

    @Test
    void shouldThrowExceptionGivenVersionPropertyIsNotLong() {
        assertThrows(MappingException.class, () -> {
            // given
            TypeInformation typeInformation = ClassTypeInformation.from(EntityWithWrongVersionType.class);
            SimpleElasticsearchPersistentEntity<EntityWithWrongVersionType> entity = new SimpleElasticsearchPersistentEntity<>(
                    typeInformation);

            SimpleElasticsearchPersistentProperty persistentProperty = createProperty(entity, "version");

            // when
            entity.addPersistentProperty(persistentProperty);
        });
    }

    @Test
    void shouldThrowExceptionGivenMultipleVersionPropertiesArePresent() {
        assertThrows(MappingException.class, () -> {
            // given
            TypeInformation typeInformation = ClassTypeInformation.from(EntityWithMultipleVersionField.class);
            SimpleElasticsearchPersistentEntity<EntityWithMultipleVersionField> entity = new SimpleElasticsearchPersistentEntity<>(
                    typeInformation);

            SimpleElasticsearchPersistentProperty persistentProperty1 = createProperty(entity, "version1");

            SimpleElasticsearchPersistentProperty persistentProperty2 = createProperty(entity, "version2");

            entity.addPersistentProperty(persistentProperty1);
            // when
            entity.addPersistentProperty(persistentProperty2);
        });
    }

    @Test // DATAES-462
    void rejectsMultipleScoreProperties() {

        SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();

        assertThatExceptionOfType(MappingException.class) //
                .isThrownBy(() -> context.getRequiredPersistentEntity(TwoScoreProperties.class)) //
                .withMessageContaining("first") //
                .withMessageContaining("second");
    }

    static class TwoScoreProperties {

        @Score
        float first;
        @Score
        float second;
    }

    private class EntityWithWrongVersionType {

        @Version
        private String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    // DATAES-462

    private class EntityWithMultipleVersionField {

        @Version
        private Long version1;
        @Version
        private Long version2;

        public Long getVersion1() {
            return version1;
        }

        public void setVersion1(Long version1) {
            this.version1 = version1;
        }

        public Long getVersion2() {
            return version2;
        }

        public void setVersion2(Long version2) {
            this.version2 = version2;
        }
    }
}
