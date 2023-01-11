package org.jenkins.plugins.pipeline.input.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jasper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputNotificationEvent {

    /**
     * the absolute URL of Jenkins, such as http://localhost/jenkins/
     */
    private String jenkinsUrl;

    /**
     * the URL of job, relative to the context root of Hudson, such as "job/foo/32/"
     */
    private String jobUrl;

    /**
     * job full name
     */
    private String jobFullName;

    /**
     * job build number
     */
    private int buildNumber;

    /**
     * input custom id
     */
    private String inputId;

    /**
     * who can proceed this input
     */
    private String submitter;

    /**
     * the one who approve/reject this input
     */
    private String approver;

    /**
     * input result, can be PENDING, APPROVED, ABORTED
     * PENDING: input started
     * APPROVED: input approved
     * ABORTED: input rejected
     */
    private String result;

}
