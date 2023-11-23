/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.metamodel.facets.members.iconfa;

import org.apache.causeway.applib.layout.component.CssClassFaPosition;

public interface FaStaticFacet
extends FaFacet, FaLayersProvider {

    //TODO[CAUSEWAY-3646] remove - can be directly queried from FA layers
    /**
     * Position of <i>Font Awesome</i> icon, relative to its accompanied title.
     */
    CssClassFaPosition getPosition();

}
