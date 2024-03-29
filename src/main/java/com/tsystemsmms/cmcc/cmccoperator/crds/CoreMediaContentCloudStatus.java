/*
 * Copyright (c) 2022. T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tsystemsmms.cmcc.cmccoperator.crds;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;

@Getter
@Setter
@ToString
public class CoreMediaContentCloudStatus extends ObservedGenerationAwareStatus {
    @PrinterColumn
    @JsonPropertyDescription("Error indication, or empty string")
    String error = "";

    @JsonPropertyDescription("Error message if state is error")
    String errorMessage = "";

    @JsonPropertyDescription("Additional state flags")
    HashMap<String, String> flags = new HashMap<>();

    @JsonPropertyDescription("Currently executing job")
    String job = "";

    @PrinterColumn
    @JsonPropertyDescription("Which milestone has been reached in configuring all components")
    Milestone milestone = Milestone.Created;

    @JsonPropertyDescription("Unused")
    @Deprecated
    String ownedResourceRefs = "[]";
}
