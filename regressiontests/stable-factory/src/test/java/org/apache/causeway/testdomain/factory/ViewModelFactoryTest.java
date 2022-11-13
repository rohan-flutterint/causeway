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
package org.apache.causeway.testdomain.factory;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_headless.class,
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class ViewModelFactoryTest extends CausewayIntegrationTestAbstract {

    // -- VIEW MODEL SAMPLES
    
    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class SimpleViewModel {
        @Inject private RepositoryService repository;
        
        public boolean areInjectionPointsResolved() {
            return repository!=null;
        }
    }

    @DomainObject(nature = Nature.VIEW_MODEL)
    @RequiredArgsConstructor
    public static class AdvancedViewModel {

        @Inject private RepositoryService repository;
        @Getter private final String name;
        
        public boolean areInjectionPointsResolved() {
            return repository!=null;
        }
    }
    
    // -- TESTS
    
    @Test
    void sampleViewModel_shouldHave_injectionPointsResolved() {
        val sampleViewModel = factoryService.viewModel(SimpleViewModel.class);

        assertTrue(sampleViewModel.areInjectionPointsResolved());
    }
    
    @Test
    void advancedViewModel_shouldHave_injectionPointsResolved() {
        val advancedViewModel = factoryService.viewModel(new AdvancedViewModel("aName"));
        
        assertTrue(advancedViewModel.areInjectionPointsResolved());
        assertEquals("aName", advancedViewModel.getName());
    }

    
}
