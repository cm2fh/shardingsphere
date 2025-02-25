/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shadow.metadata.nodepath;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.node.spi.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.spi.DatabaseRuleNodeProvider;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowRuleNodeProviderTest {
    
    private final DatabaseRuleNodeProvider provider = TypedSPILoader.getService(DatabaseRuleNodeProvider.class, ShadowRuleConfiguration.class);
    
    @Test
    void assertGetDatabaseRuleNode() {
        DatabaseRuleNode actual = provider.getDatabaseRuleNode();
        assertThat(actual.getNamedItems().size(), is(3));
        assertTrue(actual.getNamedItems().contains(ShadowRuleNodeProvider.SHADOW_ALGORITHMS));
        assertTrue(actual.getNamedItems().contains(ShadowRuleNodeProvider.TABLES));
        assertTrue(actual.getNamedItems().contains(ShadowRuleNodeProvider.DATA_SOURCES));
        assertThat(actual.getUniqueItems().size(), is(1));
        assertTrue(actual.getUniqueItems().contains(ShadowRuleNodeProvider.DEFAULT_SHADOW_ALGORITHM_NAME));
        assertThat(actual.getRuleType(), is(ShadowRuleNodeProvider.RULE_TYPE));
    }
}
