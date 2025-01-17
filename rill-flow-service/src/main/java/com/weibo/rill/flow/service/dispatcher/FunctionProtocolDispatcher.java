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

package com.weibo.rill.flow.service.dispatcher;

import com.weibo.rill.flow.common.exception.TaskException;
import com.weibo.rill.flow.common.model.BizError;
import com.weibo.rill.flow.interfaces.dispatcher.DispatcherExtension;
import com.weibo.rill.flow.interfaces.model.http.HttpParameter;
import com.weibo.rill.flow.interfaces.model.resource.Resource;
import com.weibo.rill.flow.interfaces.model.strategy.DispatchInfo;
import com.weibo.rill.flow.interfaces.model.task.TaskInfo;
import com.weibo.rill.flow.interfaces.model.task.FunctionTask;
import com.weibo.rill.flow.service.invoke.HttpInvokeHelper;
import com.weibo.rill.flow.service.statistic.DAGResourceStatistic;
import com.weibo.rill.flow.olympicene.core.switcher.SwitcherManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.Optional;


@Slf4j
@Service("functionDispatcher")
public class FunctionProtocolDispatcher implements DispatcherExtension {
    @Autowired
    private HttpInvokeHelper httpInvokeHelper;
    @Autowired
    private DAGResourceStatistic dagResourceStatistic;
    @Autowired
    private SwitcherManager switcherManagerImpl;

    @Override
    public String handle(Resource resource, DispatchInfo dispatchInfo) {
        String executionId = dispatchInfo.getExecutionId();
        Map<String, Object> input = dispatchInfo.getInput();
        TaskInfo taskInfo = dispatchInfo.getTaskInfo();
        String taskInfoName = taskInfo.getName();
        String requestType = ((FunctionTask) taskInfo.getTask()).getRequestType();
        MultiValueMap<String, String> header = dispatchInfo.getHeaders();

        try {
            HttpParameter requestParams = httpInvokeHelper.functionRequestParams(executionId, taskInfoName, resource, input);
            Optional.of(requestParams)
                    .map(it -> requestParams.getHeader())
                    .ifPresent(header::setAll);
            String url = httpInvokeHelper.buildUrl(resource, requestParams.getQueryParams());
            int maxInvokeTime = switcherManagerImpl.getSwitcherState("ENABLE_FUNCTION_DISPATCH_RET_CHECK") ? 2 : 1;
            HttpMethod method = Optional.ofNullable(requestType).map(String::toUpperCase).map(HttpMethod::resolve).orElse(HttpMethod.POST);
            Map<String, Object> body = method != HttpMethod.POST ? null : requestParams.getBody();

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, header);
            String ret = httpInvokeHelper.invokeRequest(executionId, taskInfoName, url, requestEntity, method, maxInvokeTime);
            dagResourceStatistic.updateUrlTypeResourceStatus(executionId, taskInfoName, resource.getResourceName(), ret);
            return ret;
        } catch (RestClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            dagResourceStatistic.updateUrlTypeResourceStatus(executionId, taskInfoName, resource.getResourceName(), responseBody);
            throw new TaskException(BizError.ERROR_INVOKE_URI.getCode(),
                    String.format("dispatchTask http fails status code: %s text: %s", e.getRawStatusCode(), responseBody));
        }
    }

    @Override
    public String getName() {
        return "function";
    }

}
