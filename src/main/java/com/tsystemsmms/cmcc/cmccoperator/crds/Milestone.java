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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Milestone {
    Created,
    DatabasesReady,
    ContentServerInitialized,
    ContentServerReady,
    ManagementReady,
    Ready,
    Never;

    @JsonIgnore
    private Milestone next = null;

    static {
        Milestone[] values = Milestone.values();
        // the next value; when we reach the second to last value, we stay there.
        for (int i = 0; i < values.length - 2; i++) {
            values[i].next = values[i + 1];
        }
        values[values.length - 2].next = values[values.length - 2];
        values[values.length - 1].next = values[values.length - 1];
    }

    public Milestone getNext() {
        return this.next;
    }
}
