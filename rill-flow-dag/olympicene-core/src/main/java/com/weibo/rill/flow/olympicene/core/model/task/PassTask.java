/*
 *  Copyright 2021-2023 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.weibo.rill.flow.olympicene.core.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.weibo.rill.flow.interfaces.model.strategy.Degrade;
import com.weibo.rill.flow.interfaces.model.mapping.Mapping;
import com.weibo.rill.flow.interfaces.model.strategy.Progress;
import com.weibo.rill.flow.interfaces.model.strategy.Timeline;
import com.weibo.rill.flow.interfaces.model.task.BaseTask;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@JsonTypeName("pass")
public class PassTask extends BaseTask {

    @JsonCreator
    public PassTask(@JsonProperty("name") String name,
                    @JsonProperty("category") String category,
                    @JsonProperty("next") String next,
                    @JsonProperty("inputMappings") List<Mapping> inputMappings,
                    @JsonProperty("outputMappings") List<Mapping> outputMappings,
                    @JsonProperty("progress") Progress progress,
                    @JsonProperty("degrade") Degrade degrade,
                    @JsonProperty("timeline") Timeline timeline,
                    @JsonProperty("isKeyCallback") boolean isKeyCallback,
                    @JsonProperty("keyExp") String keyExp,
                    @JsonProperty("parameters") Map parameters) {
        super(name, category, next, false, inputMappings, outputMappings, progress, degrade, timeline, isKeyCallback, keyExp, parameters);
        Optional.ofNullable(timeline).ifPresent(it -> it.setTimeoutInSeconds(null));
    }

    @Override
    public List<BaseTask> subTasks() {
        return new ArrayList<>();
    }
}
