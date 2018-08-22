/*
 * ========================LICENSE_START=================================
 * AEM Permission Management
 * %%
 * Copyright (C) 2013 Cognifide Limited
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package com.cognifide.cq.cqsm.core.macro

import com.cognifide.cq.cqsm.ApmLangParserHelper
import com.cognifide.cq.cqsm.api.scripts.Script
import spock.lang.Specification

class MacroTest extends Specification {

    def "registers macros"() {
        given:
        def parser = ApmLangParserHelper.createParserUsingFile("/macros.apm")
        def script = Mock(Script)
        script.apm >> parser.apm()
        def macroRegistrar = new MacroRegistrar()

        when:
        def result = macroRegistrar.findMacroDefinitions(new MacroRegister(), script)

        then:
        result.get("macro1") != null
        result.get("macro2") != null
        result.get("macro3") != null
        result.get("macro4") == null
    }
}