/*
 * Copyright (c) 2022. T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tsystemsmms.cmcc.cmccoperator.ingress;

import com.tsystemsmms.cmcc.cmccoperator.targetstate.TargetState;

public class OnlyLangUrlMappingBuilderFactory implements UrlMappingBuilderFactory {
    private final IngressBuilderFactory ingressBuilderFactory;

    public OnlyLangUrlMappingBuilderFactory(IngressBuilderFactory ingressBuilderFactory) {
        this.ingressBuilderFactory = ingressBuilderFactory;
    }

    @Override
    public String getName() {
        return "onlylang";
    }

    @Override
    public UrlMappingBuilder instance(TargetState targetState, String serviceName) {
        return new OnlyLangUrlMappingBuilder(ingressBuilderFactory, targetState, serviceName);
    }
}
