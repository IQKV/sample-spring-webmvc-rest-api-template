/*
 * Copyright 2025 IQKV Team.
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

package com.iqkv.sample.webmvc.dashboard.web.rest.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import com.iqkv.boot.mvc.rest.errors.ProblemDetailWithCause;
import org.junit.jupiter.api.Test;

class ProblemDetailWithCauseTest {

  @Test
  void testProblemDetailWithCauseBuilder() {
    // Create a cause
    ProblemDetailWithCause cause = ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
        .withStatus(400)
        .withTitle("Bad Request")
        .withDetail("Invalid input")
        .withType(URI.create("about:blank"))
        .build();

    // Create a main problem detail with a cause
    ProblemDetailWithCause problemDetailWithCause = ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
        .withStatus(500)
        .withTitle("Internal Server Error")
        .withDetail("Something went wrong")
        .withType(URI.create("about:blank"))
        .withCause(cause)
        .build();

    // Verify main problem detail
    assertEquals(500, problemDetailWithCause.getStatus());
    assertEquals("Internal Server Error", problemDetailWithCause.getTitle());
    assertEquals("Something went wrong", problemDetailWithCause.getDetail());
    assertEquals(URI.create("about:blank"), problemDetailWithCause.getType());
    assertNotNull(problemDetailWithCause.getCause());

    // Verify the cause
    assertEquals(400, problemDetailWithCause.getCause().getStatus());
    assertEquals("Bad Request", problemDetailWithCause.getCause().getTitle());
    assertEquals("Invalid input", problemDetailWithCause.getCause().getDetail());
    assertEquals(URI.create("about:blank"), problemDetailWithCause.getCause().getType());
  }
}
