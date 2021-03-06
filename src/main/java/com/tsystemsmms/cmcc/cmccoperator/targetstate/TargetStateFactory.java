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

/**
 * Spring Bean that generates a target state object suitable for use in a reconciler.
 */
public interface TargetStateFactory {
    /**
     * Create a target state object for the given custom resource.
     *
     * @param cmcc custom resource
     * @return target state
     */
    TargetState buildTargetState(CustomResource cmcc);
}
