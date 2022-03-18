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

import com.tsystemsmms.cmcc.cmccoperator.components.ComponentSpecBuilder;
import com.tsystemsmms.cmcc.cmccoperator.components.generic.MongoDBComponent;
import com.tsystemsmms.cmcc.cmccoperator.components.generic.MySQLComponent;
import com.tsystemsmms.cmcc.cmccoperator.crds.ComponentSpec;
import com.tsystemsmms.cmcc.cmccoperator.crds.CoreMediaContentCloud;
import com.tsystemsmms.cmcc.cmccoperator.crds.Milestone;
import com.tsystemsmms.cmcc.cmccoperator.ingress.CmccIngressGeneratorFactory;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.tsystemsmms.cmcc.cmccoperator.utils.Utils.concatOptional;

/**
 * Create the runtime config based on the CRD data.
 */
@Slf4j
public class DefaultTargetState extends AbstractTargetState {
    public DefaultTargetState(BeanFactory beanFactory, KubernetesClient kubernetesClient, CmccIngressGeneratorFactory cmccIngressGeneratorFactory, ResourceNamingProviderFactory resourceNamingProviderFactory, CoreMediaContentCloud cmcc) {
        super(beanFactory, kubernetesClient, cmccIngressGeneratorFactory, resourceNamingProviderFactory, cmcc);
    }

    @Override
    public boolean converge() {
        Milestone previousMilestone = getCmcc().getStatus().getMilestone();

        if (cmcc.getSpec().getWith().getDatabases()) {
            componentCollection.addAll(List.of(
                    ComponentSpecBuilder.ofType("mongodb")
                            .withMilestone(Milestone.Created)
                            .build(),
                    ComponentSpecBuilder.ofType("mysql")
                            .withMilestone(Milestone.Created)
                            .build()
            ));
        }


        if (cmcc.getSpec().getWith().getManagement()) {
            componentCollection.addAll(List.of(
                    ComponentSpecBuilder.ofType("cae").withKind("preview").build(),
                    ComponentSpecBuilder.ofType("cae-feeder").withKind("live").build(),
                    ComponentSpecBuilder.ofType("cae-feeder").withKind("preview").build(),
                    ComponentSpecBuilder.ofType("content-feeder").build(),
                    ComponentSpecBuilder.ofType("content-server").withKind("cms").withMilestone(Milestone.DatabasesReady).build(),
                    ComponentSpecBuilder.ofType("content-server").withKind("mls").withMilestone(Milestone.DatabasesReady).build(),
                    ComponentSpecBuilder.ofType("elastic-worker").build(),
                    ComponentSpecBuilder.ofType("solr").withKind("leader").withMilestone(Milestone.Created).build(),
                    ComponentSpecBuilder.ofType("studio-client").build(),
                    ComponentSpecBuilder.ofType("studio-server").build(),
                    ComponentSpecBuilder.ofType("user-changes").build(),
                    ComponentSpecBuilder.ofType("workflow-server").withMilestone(Milestone.ContentServerReady).build(),
                    ComponentSpecBuilder.ofType("overview").withMilestone(Milestone.ManagementReady).build(),

                    ComponentSpecBuilder.ofType("management-tools")
                            .withName("initcms")
                            .withMilestone(Milestone.ContentServerReady)
                            .withArgs(List.of("change-passwords"))
                            .build()
            ));
        }

        // remember secrets for RLSs
        if (cmcc.getSpec().getWith().getDelivery().getRls() != 0
                || cmcc.getSpec().getWith().getDelivery().getMinCae() > 1
                || cmcc.getSpec().getWith().getDelivery().getMaxCae() > cmcc.getSpec().getWith().getDelivery().getMinCae()) {
            throw new RuntimeException("Unable to configure RLS and HPA, not implemented yet");
        }
        if (cmcc.getSpec().getWith().getDelivery().getMinCae() == 1) {
            componentCollection.addAll(List.of(
                    ComponentSpecBuilder.ofType("cae").withKind("live").build()
            ));
        }

        // trigger components to request clientResourceRefs, so we can build the config maps for the database servers
        requestRequiredResources();

        if (cmcc.getSpec().getWith().getDatabases()) {
            componentCollection.addAll(List.of(
                    ComponentSpecBuilder.ofType("mongodb")
                            .withMilestone(Milestone.Created)
                            .withExtra(MongoDBComponent.createUsersFromClientSecrets(this))
                            .build(),
                    ComponentSpecBuilder.ofType("mysql")
                            .withMilestone(Milestone.Created)
                            .withExtra(MySQLComponent.createUsersFromClientSecrets(this))
                            .build()
            ));
        }

        // allow explicitly declared components to override default ones
        componentCollection.addAll(cmcc.getSpec().getComponents());

        switch (cmcc.getStatus().getMilestone()) {
            case Created:
                if (cmcc.getSpec().getWith().getDatabases()) {
                    advanceToMilestoneAfterComponentsReady(List.of(
                            ComponentSpecBuilder.ofType("mongodb").build(),
                            ComponentSpecBuilder.ofType("mysql").build()
                    ), Milestone.DatabasesReady);
                } else {
                    cmcc.getStatus().setMilestone(Milestone.DatabasesReady);
                }
                break;
            case DatabasesReady:
                advanceToMilestoneAfterComponentsReady(List.of(
                        ComponentSpecBuilder.ofType("content-server").withKind("cms").build(),
                        ComponentSpecBuilder.ofType("content-server").withKind("mls").build() /*,
                        ComponentSpecBuilder.ofType("solr").withKind("leader").build(),
                        ComponentSpecBuilder.ofType("workflow-server").build()*/
                ), Milestone.ContentServerInitialized);
                break;
            case ContentServerInitialized:
                // secrets can be created right away at Created, the components simply won't use them until ManagementReady
                if (isReady("initcms")) {
                    cmcc.getStatus().setMilestone(Milestone.ContentServerReady);
                    // as part of the transition, kick the content server by scaling it to 0; the next round will turn it up to 1 again
                    scaleComponent(componentCollection.getServiceNameFor("content-server", "cms"));
                    scaleComponent(componentCollection.getServiceNameFor("content-server", "mls"));
                }
                break;
            case ContentServerReady:
                advanceToMilestoneAfterComponentsReady(List.of(
                        ComponentSpecBuilder.ofType("content-server").withKind("cms").build(),
                        ComponentSpecBuilder.ofType("content-server").withKind("mls").build() ,
                        ComponentSpecBuilder.ofType("solr").withKind("leader").build(),
                        ComponentSpecBuilder.ofType("workflow-server").build()),
                        Milestone.ManagementReady);
                break;
            case ManagementReady:
                if (componentCollection.containsName("import")) {
                    advanceToMilestoneAfterJobSucceeds("import", Milestone.Ready);
                }
                break;
        }

        return previousMilestone == getCmcc().getStatus().getMilestone();
    }

    /**
     * Check if the named job has completed successfully, then move to the desired milestone.
     *
     * @param jobName   component name of job
     * @param milestone to advance to
     */
    private void advanceToMilestoneAfterJobSucceeds(String jobName, Milestone milestone) {
        if (isReady(jobName)) {
            log.debug("advancing milestone");
            cmcc.getStatus().setMilestone(milestone);
        }
    }

    private void advanceToMilestoneAfterComponentsReady(Collection<ComponentSpec> components, Milestone milestone) {
        if (isReady(components)) {
            log.debug("Advancing to {}", milestone);
            cmcc.getStatus().setMilestone(milestone);
        }
    }

    private boolean isReady(String jobName) {
        jobName = concatOptional(cmcc.getSpec().getDefaults().getNamePrefix(), jobName);
        Job job = kubernetesClient.batch().v1().jobs().inNamespace(cmcc.getMetadata().getNamespace()).withName(jobName).get();
        boolean ready = job != null && job.getStatus() != null && job.getStatus().getSucceeded() != null && job.getStatus().getSucceeded() > 0;
        if (ready) {
            log.debug("job {}: has succeeded", jobName);
        } else {
            log.debug("job {}: waiting for successful completion", jobName);
        }
        return ready;
    }

    private boolean isReady(Collection<ComponentSpec> components) {
        LinkedList<ComponentSpec> notReady = new LinkedList<>();
        for (ComponentSpec component : components) {
            if (!isComponentStatefulSetReady(component))
                notReady.add(component);
        }
        if (notReady.size() == 0) {
            log.debug("Components are ready: {}", componentCollectionToString(components));
        } else {
            log.debug("Waiting for components to become ready: {}", componentCollectionToString(notReady));
        }
        return notReady.size() == 0;
    }
}
