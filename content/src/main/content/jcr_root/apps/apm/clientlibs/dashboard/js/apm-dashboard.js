/*-
 * ========================LICENSE_START=================================
 * AEM Permission Management
 * %%
 * Copyright (C) 2013 - 2016 Cognifide Limited
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
(function (window, $) {

    let uiHelper = $(window).adaptTo("foundation-ui");

    $(window).adaptTo("foundation-registry").register(
        "foundation.collection.action.action", {
            name: "dashboard.dryrun",
            handler: function (name, el, config, collection, selections) {
                const selected = selections[0].attributes['data-path'].value;
                runOnAuthor(selected, "DRY_RUN");
            }
        });

    $(window).adaptTo("foundation-registry").register(
        "foundation.collection.action.action", {
            name: "dashboard.runonauthor",
            handler: function (name, el, config, collection, selections) {
                const selected = selections[0].attributes['data-path'].value;
                runOnAuthor(selected, "RUN");
            }
        });

    $(window).adaptTo("foundation-registry").register(
        "foundation.collection.action.action", {
            name: "dashboard.runonpublish",
            handler: function (name, el, config, collection, selections) {
                const selected = selections[0].attributes['data-path'].value;
                runOnPublish(selected);
            }
        });

    function runOnAuthor(scriptPath, mode) {

        $.ajax({
            type: "POST",
            url: "/bin/cqsm/run-background?file=" + scriptPath + "&mode="
            + mode,
            dataType: "html",
            success: function (data) {
                const jobId = JSON.parse(data).id;

                (function checkStatus(jobId) {
                    $.ajax({
                        type: "GET",
                        url: "/bin/cqsm/run-background?id=" + jobId,
                        dataType: "html",
                        success: function (data) {
                            const dataObject = JSON.parse(data);
                            if (dataObject.type === 'running') {
                                setTimeout(function () {
                                    checkStatus(jobId)
                                }, 1000);
                            } else if (dataObject.type === 'finished') {
                                console.log(scriptPath + " finished: " + JSON.stringify(dataObject.entries));
                                switch(mode){
                                    case 'DRY_RUN':
                                        uiHelper.notify('info', 'Dry Run executed successfully', 'Info');
                                        break;
                                    case 'RUN':
                                        uiHelper.notify('info', 'Run on author executed successfully', 'Info');
                                        break;
                                }
                            } else if (dataObject.type === 'unknown') {
                                switch(mode) {
                                    case 'DRY_RUN':
                                        uiHelper.alert('Dry Run wasn\'t executed successfully', data.responseJSON.message, 'error');
                                        break;
                                    case 'RUN':
                                        uiHelper.alert('Run on author wasn\'t executed successfully', data.responseJSON.message, 'error');
                                        break;
                                }
                            }
                        }
                    });
                })(jobId);
            }
        });
    }

    function runOnPublish(fileName) {

        $.ajax({
            type: "GET",
            url: "/bin/cqsm/replicate?run=publish&fileName=" + fileName,
            dataType: "json",
            success: function (data) {
                console.log("publish response: " + JSON.stringify(data));
                uiHelper.notify('info', 'Run on publish executed successfully', 'info');
            },
            error: function (data) {
                console.log("publish  response: " + JSON.stringify(data));
                uiHelper.alert('Run on publish wasn\'t executed successfully', data.responseJSON.message, 'error');
            }
        });
    }

})(window, jQuery);
