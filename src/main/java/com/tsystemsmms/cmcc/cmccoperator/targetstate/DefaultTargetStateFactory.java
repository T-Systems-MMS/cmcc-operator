/*
 * Copyright (c) 2022. T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tsystemsmms.cmcc.cmccoperator.targetstate;

import com.tsystemsmms.cmcc.cmccoperator.customresource.CustomResource;
import com.tsystemsmms.cmcc.cmccoperator.ingress.CmccIngressGeneratorFactory;
import com.tsystemsmms.cmcc.cmccoperator.resource.ResourceReconcilerManager;
import com.tsystemsmms.cmcc.cmccoperator.utils.YamlMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.BeanFactory;

/**
 * Create a DefaultTargetState based on a CustomResource custom resource.
 */
public class DefaultTargetStateFactory implements TargetStateFactory {
    private final BeanFactory beanFactory;
    private final KubernetesClient kubernetesClient;
    private final CmccIngressGeneratorFactory cmccIngressGeneratorFactory;
    private final ResourceNamingProviderFactory resourceNamingProviderFactory;
    private final ResourceReconcilerManager resourceReconcilerManager;
    private final YamlMapper yamlMapper;

    public DefaultTargetStateFactory(BeanFactory beanFactory,
                                     KubernetesClient kubernetesClient,
                                     CmccIngressGeneratorFactory cmccIngressGeneratorFactory,
                                     ResourceNamingProviderFactory resourceNamingProviderFactory,
                                     ResourceReconcilerManager resourceReconcilerManager,
                                     YamlMapper yamlMapper) {
        this.beanFactory = beanFactory;
        this.kubernetesClient = kubernetesClient;
        this.cmccIngressGeneratorFactory = cmccIngressGeneratorFactory;
        this.resourceNamingProviderFactory = resourceNamingProviderFactory;
        this.resourceReconcilerManager = resourceReconcilerManager;
        this.yamlMapper = yamlMapper;
    }

    @Override
    public TargetState buildTargetState(CustomResource cmcc) {
        return new DefaultTargetState(beanFactory,
                kubernetesClient,
                cmccIngressGeneratorFactory,
                resourceNamingProviderFactory,
                resourceReconcilerManager,
                yamlMapper,
                cmcc);
    }
}
