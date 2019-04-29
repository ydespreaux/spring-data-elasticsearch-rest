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
 *
 */

package com.github.ydespreaux.spring.data.elasticsearch.core.completion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
class SuggestItem implements Comparable<SuggestItem> {
    private String item;
    private Float score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuggestItem)) return false;
        SuggestItem that = (SuggestItem) o;
        return Objects.equals(getItem(), that.getItem()) &&
                Objects.equals(getScore(), that.getScore());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getScore());
    }

    @Override
    public int compareTo(SuggestItem other) {
        int scoreCompare = this.score.compareTo(other.getScore());
        if (scoreCompare == 0) {
            return this.item.compareTo(other.getItem());
        }
        return scoreCompare;
    }
}