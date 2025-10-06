/*
 * Copyright 2025 IQKV Foundation Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqkv.sample.webmvc.dashboard.shared.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;

import com.iqkv.boot.http.ProblemDetailWithCause;
import org.junit.jupiter.api.Test;

class ProblemDetailWithCauseTest {

  @Test
  void testProblemDetailWithCauseBuilder() {
    ProblemDetailWithCause problemDetail = ProblemDetailWithCause.ProblemDetailWithCauseBuilder
        .instance()
        .withStatus(400)
        .withType(URI.create("https://example.com/problem"))
        .withTitle("Bad Request")
        .withDetail("Invalid input")
        .withInstance(URI.create("https://example.com/instance"))
        .build();

    assertNotNull(problemDetail);
    assertEquals(400, problemDetail.getStatus());
    assertEquals(URI.create("https://example.com/problem"), problemDetail.getType());
    assertEquals("Bad Request", problemDetail.getTitle());
    assertEquals("Invalid input", problemDetail.getDetail());
    assertEquals(URI.create("https://example.com/instance"), problemDetail.getInstance());
  }

  @Test
  void testProblemDetailWithCauseBuilderDefaults() {
    ProblemDetailWithCause problemDetail = ProblemDetailWithCause.ProblemDetailWithCauseBuilder
        .instance()
        .withStatus(500)
        .build();

    assertNotNull(problemDetail);
    assertEquals(500, problemDetail.getStatus());
    assertNull(problemDetail.getType());
    assertNull(problemDetail.getTitle());
    assertNull(problemDetail.getDetail());
    assertNull(problemDetail.getInstance());
  }

  @Test
  void testProblemDetailWithCauseBuilderChaining() {
    ProblemDetailWithCause problemDetail = ProblemDetailWithCause.ProblemDetailWithCauseBuilder
        .instance()
        .withStatus(404)
        .withTitle("Not Found")
        .withDetail("Resource not found")
        .build();

    assertNotNull(problemDetail);
    assertEquals(404, problemDetail.getStatus());
    assertEquals("Not Found", problemDetail.getTitle());
    assertEquals("Resource not found", problemDetail.getDetail());
  }
}