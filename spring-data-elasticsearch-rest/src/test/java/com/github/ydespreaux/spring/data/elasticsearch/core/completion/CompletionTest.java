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

package com.github.ydespreaux.spring.data.elasticsearch.core.completion;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CompletionTest {


    @Test
    void builder() {
        Completion completion = Completion.builder()
                .weight(10)
                .input(new String[]{"input1"})
                .build();
        assertThat(completion.getInput(), is(equalTo(new String[]{"input1"})));
        assertThat(completion.getWeight(), is(equalTo(10)));
    }

    @Test
    void setInput() {
        Completion completion = new Completion();
        completion.setInput(new String[]{"input1"});
        assertThat(completion.getInput(), is(equalTo(new String[]{"input1"})));
    }

    @Test
    void setWeight() {
        Completion completion = new Completion();
        completion.setWeight(10);
        assertThat(completion.getWeight(), is(equalTo(10)));
    }

}
