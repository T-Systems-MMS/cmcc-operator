/*
 * Copyright (c) 2022. T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tsystemsmms.cmcc.cmccoperator;

import com.tsystemsmms.cmcc.cmccoperator.ingress.*;
import com.tsystemsmms.cmcc.cmccoperator.targetstate.*;
import com.tsystemsmms.cmcc.cmccoperator.resource.ResourceReconcilerManager;
import com.tsystemsmms.cmcc.cmccoperator.utils.YamlMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CMCCOperatorApplication {

    @Bean
    @ConditionalOnProperty(value="cmcc.useCrd", havingValue = "true", matchIfMissing = true)
    public CoreMediaContentCloudReconciler coreMediaContentCloudReconciler(
            KubernetesClient kubernetesClient,
            TargetStateFactory targetStateFactory) {
        return new CoreMediaContentCloudReconciler(
                kubernetesClient,
                targetStateFactory);
    }

    @Bean
    @ConditionalOnProperty(value="cmcc.useConfigMap", havingValue = "true", matchIfMissing = false)
    public CmccConfigMapReconciler cmccConfigMapReconciler(
            KubernetesClient kubernetesClient,
            TargetStateFactory targetStateFactory,
            YamlMapper yamlMapper) {
        return new CmccConfigMapReconciler(
                kubernetesClient,
                targetStateFactory,
                yamlMapper);
    }

    @Bean
    public ResourceNamingProviderFactory resourceNamingProviderFactory() {
        return new DefaultResourceNamingProviderFactory();
    }

    @Bean
    public ResourceReconcilerManager resourceReconciler(KubernetesClient kubernetesClient) {
        return new ResourceReconcilerManager(kubernetesClient);
    }

    @Bean
    @ConditionalOnProperty(value = "cmcc.ingressbuilder", havingValue = "blueprint")
    public CmccIngressGeneratorFactory blueprintIngressGeneratorFactory(IngressBuilderFactory ingressBuilderFactory) {
        return new BlueprintCmccIngressGeneratorFactory(ingressBuilderFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "cmcc.ingressbuilder", havingValue = "onlylang")
    public CmccIngressGeneratorFactory onlylangIngressGeneratorFactory(IngressBuilderFactory ingressBuilderFactory) {
        return new OnlyLangCmccIngressGeneratorFactory(ingressBuilderFactory);
    }

    @Bean
    public IngressBuilderFactory ingressBuilderFactory() {
        return new NginxIngressBuilderFactory();
    }

    @Bean
    public TargetStateFactory targetStateFactory(BeanFactory beanFactory,
                                                 KubernetesClient kubernetesClient,
                                                 CmccIngressGeneratorFactory cmccIngressGeneratorFactory,
                                                 ResourceNamingProviderFactory resourceNamingProviderFactory,
                                                 ResourceReconcilerManager resourceReconcilerManager,
                                                 YamlMapper yamlMapper) {
        return new DefaultTargetStateFactory(beanFactory,
                kubernetesClient,
                cmccIngressGeneratorFactory,
                resourceNamingProviderFactory,
                resourceReconcilerManager,
                yamlMapper);
    }

    @Bean
    public YamlMapper yamlMapper() {
        return new YamlMapper();
    }

    public static void main(String[] args) {
        SpringApplication.run(CMCCOperatorApplication.class, args);
    }

}
