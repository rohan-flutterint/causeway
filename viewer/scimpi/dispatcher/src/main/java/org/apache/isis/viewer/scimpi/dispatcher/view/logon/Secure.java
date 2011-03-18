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


package org.apache.isis.viewer.scimpi.dispatcher.view.logon;

import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.UserlessSession;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public class Secure extends AbstractElementProcessor {
    private static final String LOGIN_VIEW = "login-view";

    public void process(Request request) {
        boolean isLoggedIn = !(IsisContext.getSession().getAuthenticationSession() instanceof UserlessSession);
        if (!isLoggedIn) {
            String view = request.getOptionalProperty(LOGIN_VIEW, "/login.shtml");
            request.getContext().redirectTo(view);
        }
    }

    public String getName() {
        return "secure";
    }

}

