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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Show process list executor.
 */
@RequiredArgsConstructor
public final class ShowProcessListExecutor implements DatabaseAdminQueryExecutor {
    
    private final boolean showFullProcesslist;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new TransparentMergedResult(getQueryResult());
    }
    
    private QueryResult getQueryResult() {
        Collection<Process> processes = ProxyContext.getInstance().getContextManager().getPersistServiceFacade().getModeFacade().getProcessService().getProcessList();
        if (processes.isEmpty()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        List<MemoryQueryResultDataRow> rows = processes.stream().map(this::getMemoryQueryResultDataRow).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private MemoryQueryResultDataRow getMemoryQueryResultDataRow(final Process process) {
        List<Object> rowValues = new ArrayList<>(8);
        rowValues.add(process.getId());
        rowValues.add(process.getUsername());
        rowValues.add(process.getHostname());
        rowValues.add(process.getDatabaseName());
        rowValues.add(process.isIdle() ? "Sleep" : "Execute");
        rowValues.add(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - process.getStartMillis()));
        String sql = null;
        if (process.isIdle()) {
            rowValues.add("");
        } else {
            int processDoneCount = process.getCompletedUnitCount().get();
            String statePrefix = "Executing ";
            rowValues.add(statePrefix + processDoneCount + "/" + process.getTotalUnitCount().get());
            sql = process.getSql();
        }
        if (null != sql && sql.length() > 100 && !showFullProcesslist) {
            sql = sql.substring(0, 100);
        }
        rowValues.add(null != sql ? sql : "");
        return new MemoryQueryResultDataRow(rowValues);
    }
    
    private QueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = new ArrayList<>(8);
        columns.add(new RawQueryResultColumnMetaData("", "Id", "Id", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "User", "User", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Host", "Host", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "db", "db", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Command", "Command", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Time", "Time", Types.VARCHAR, "VARCHAR", 10, 0));
        columns.add(new RawQueryResultColumnMetaData("", "State", "State", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Info", "Info", Types.VARCHAR, "VARCHAR", 120, 0));
        return new RawQueryResultMetaData(columns);
    }
}
