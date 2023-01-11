# Pipeline: Input Step Notification Plugin

Send notification after [input](https://github.com/jenkinsci/pipeline-input-step-plugin) step started, approved, aborted.

Support notifier are as follows:
- Http Notifier: Send notification to the configured endpoint using HTTP/HTTPS protocol with **POST** method after Input step started, approved, aborted

Some apis described in [Pipeline Stage View Plugin](https://github.com/jenkinsci/pipeline-stage-view-plugin/blob/master/rest-api/src/main/java/com/cloudbees/workflow/rest/endpoints/RunAPI.java) 
and [Pipeline Input Step Plugin](https://github.com/jenkinsci/pipeline-input-step-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/support/steps/input/InputStepExecution.java) 
may be helpful when using this plugin, the data in the notification can help you assemble the request.

Examples:
- http://${JENKINS_URL}/${JOB_URL}/wfapi/pendingInputActions
- http://${JENKINS_URL}/${JOB_URL}/wfapi/nextPendingInputAction
- http://${JENKINS_URL}/${JOB_URL}/wfapi/inputSubmit?inputId=${INPUT_ID}
- http://${JENKINS_URL}/${JOB_URL}/input/${INPUT_ID}/proceedEmpty
- http://${JENKINS_URL}/${JOB_URL}/input/${INPUT_ID}/abort
