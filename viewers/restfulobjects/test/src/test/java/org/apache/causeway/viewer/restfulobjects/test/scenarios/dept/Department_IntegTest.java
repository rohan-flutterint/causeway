/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.restfulobjects.test.scenarios.dept;

import lombok.val;

import java.util.Optional;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import org.apache.causeway.viewer.restfulobjects.test.domain.dom.Department;

import org.approvaltests.Approvals;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.viewer.restfulobjects.test.scenarios.Abstract_IntegTest;

import org.springframework.transaction.annotation.Propagation;


public class Department_IntegTest extends Abstract_IntegTest {

    @Test
    @UseReporter(DiffReporter.class)
    public void exists() {

        // given
        Bookmark bookmark = transactionService.callTransactional(Propagation.REQUIRED, () -> {
            Department classics = departmentRepository.findByName("Classics");
            return bookmarkService.bookmarkFor(classics).orElseThrow();
        }).valueAsNonNullElseFail();

        Invocation.Builder request = restfulClient.request(String.format("/objects/%s/%s", bookmark.getLogicalTypeName(), bookmark.getIdentifier()));

        // when
        val response = request.get();

        // then
        val entity = response.readEntity(String.class);

        assertThat(response)
                .extracting(Response::getStatus)
                .isEqualTo(Response.Status.OK.getStatusCode());
        Approvals.verify(entity, jsonOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    public void does_not_exist() {

        // given
        Invocation.Builder request = restfulClient.request("/objects/university.dept.Department/9999999");

        // when
        val response = request.get();

        // then
        val entity = response.readEntity(String.class);

        assertThat(response)
                .extracting(Response::getStatus)
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        Approvals.verify(entity, jsonOptions());


    }

}
